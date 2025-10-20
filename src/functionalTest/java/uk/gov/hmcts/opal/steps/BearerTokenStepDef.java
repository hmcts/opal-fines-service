package uk.gov.hmcts.opal.steps;

import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import java.util.concurrent.ConcurrentHashMap;

import static net.serenitybdd.rest.SerenityRest.then;

public class BearerTokenStepDef extends BaseStepDef {

    static Logger log = LoggerFactory.getLogger(BearerTokenStepDef.class.getName());
    private static final String DEFAULT_USER = "opal-test@hmcts.net";
    private static final ThreadLocal<String> TOKEN = new ThreadLocal<>();
    private static final ThreadLocal<String> ALT_TOKEN = new ThreadLocal<>();
    private static final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();

    @Before
    public void resetAltToken() {
        ALT_TOKEN.remove();
    }

    public String getAccessTokenForUser(String user) {
        return tokenCache.computeIfAbsent(user, BearerTokenStepDef::fetchAccessToken);
    }

    private static String fetchAccessToken(String user) {
        return fetchToken(user);
    }

    @BeforeAll
    public static void setDefaultToken() {
        TOKEN.set(tokenCache.computeIfAbsent(DEFAULT_USER, BearerTokenStepDef::fetchAccessToken));
    }

    private static String fetchToken(String user) {
        SerenityRest.given()
            .accept("*/*")
            .header("X-User-Email", user)
            .contentType("application/json")
            .when()
            .get(getTestUrl() + "/testing-support/token/user");

        then().assertThat().statusCode(200);
        return then().extract().body().jsonPath().getString("access_token");
    }

    public static String getToken() {
        return ALT_TOKEN.get() != null ? ALT_TOKEN.get() : TOKEN.get();
    }

    @When("I am testing as the {string} user")
    public void setTokenWithUser(String user) {
        ALT_TOKEN.set(getAccessTokenForUser(user));
    }

    @When("I set an invalid token")
    public void setInvalidToken() {
        ALT_TOKEN.set("invalid-token");
    }

    @When("I call {word} {string} without a token")
    public void callWithoutToken(String method, String path) {
        SerenityRest.given()
            .baseUri(getTestUrl())
            .accept("*/*")
            .when()
            .request(method, path);
    }

    @When("I call {word} {string} with an invalid token")
    public void callWithInvalidToken(String method, String path) {
        SerenityRest.given()
            .baseUri(getTestUrl())
            .accept("*/*")
            .header("Authorization", "Bearer invalidToken")
            .when()
            .request(method, path);
    }

    private static String fetchExpiredToken(String user) {
        // Create a dummy JWT that looks real but has an expired "exp" timestamp
        long now = System.currentTimeMillis() / 1000L;
        long expired = now - 3600; // expired 1 hour ago

        // Build standard JWT header + payload
        String header = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

        String payloadJson = String.format(
            "{\"sub\":\"%s\",\"email\":\"%s\",\"iat\":%d,\"exp\":%d}",
            user, user, expired - 60, expired
        );
        String payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

        // Signature: use a dummy secret, since we just need it to *look* valid
        String signature = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("expired-signature".getBytes(StandardCharsets.UTF_8));

        // Combine header, payload, and signature into a JWT
        String expiredToken = String.format("%s.%s.%s", header, payload, signature);

        // Log what we generated (optional)
        log.info("Generated expired token for user {})", user);

        return expiredToken;
    }

    @When("I am testing with an expired token for the {string} user")
    public void setExpiredTokenForUser(String user) {
        ALT_TOKEN.set(fetchExpiredToken(user));
    }


    @AfterAll
    public static void clearCache() {
        tokenCache.clear();
        ALT_TOKEN.remove();
        TOKEN.remove();
    }
}
