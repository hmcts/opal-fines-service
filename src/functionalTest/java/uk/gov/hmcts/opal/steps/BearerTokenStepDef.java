package uk.gov.hmcts.opal.steps;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;

import java.util.concurrent.ConcurrentHashMap;

import static net.serenitybdd.rest.SerenityRest.then;

public class BearerTokenStepDef extends BaseStepDef {

    private static final String DEFAULT_USER = "opal-test@hmcts.net";
    private static final ThreadLocal<String> TOKEN = new ThreadLocal<>();
    private static final ThreadLocal<String> ALT_TOKEN = new ThreadLocal<>();
    private static final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();

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
            .get(getTestUrl() + "/api/testing-support/token/user");

        then().assertThat().statusCode(200);
        return then().extract().body().jsonPath().getString("accessToken");
    }

    public static String getToken() {
        return ALT_TOKEN.get() != null ? ALT_TOKEN.get() : TOKEN.get();
    }

    @When("I am testing as the {string} user")
    public void setTokenWithUser(String user) {
        ALT_TOKEN.set(getAccessTokenForUser(user));
    }

    @AfterAll
    public static void clearCache() {
        tokenCache.clear();
        ALT_TOKEN.remove();
        TOKEN.remove();
    }
}
