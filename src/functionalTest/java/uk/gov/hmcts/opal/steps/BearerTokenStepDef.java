package uk.gov.hmcts.opal.steps;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;

import java.util.concurrent.ConcurrentHashMap;

import static net.serenitybdd.rest.SerenityRest.then;


public class BearerTokenStepDef extends BaseStepDef {

    protected static String DEFAULT_USER = "opal-test@hmcts.net";
    protected static ThreadLocal<String> TOKEN = new ThreadLocal<>();
    protected static ThreadLocal<String> ALT_TOKEN = new ThreadLocal<>();
    private static final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();

    public String getAccessTokenForUser(String user) {
        return tokenCache.computeIfAbsent(user, this::fetchAccessToken);
    }

    private String fetchAccessToken(String user) {
        SerenityRest.given()
            .accept("*/*")
            .header("X-User-Email", user)
            .contentType("application/json")
            .when()
            .get(getTestUrl() + "/api/testing-support/token/user");

        then().assertThat().statusCode(200);
        return then().extract().body().jsonPath().getString("accessToken");
    }

    @BeforeAll
    public void setDefaultToken() {
        String accessToken = tokenCache.computeIfAbsent(DEFAULT_USER, this::fetchDefaultUserToken);
        TOKEN.set(accessToken);
    }

    private String fetchDefaultUserToken(String user) {
        SerenityRest.given()
            .accept("*/*")
            .header("X-User-Email", user)
            .contentType("application/json")
            .when()
            .get(getTestUrl() + "/api/testing-support/token/test-user");
        then().assertThat().statusCode(200);
        return then().extract().body().jsonPath().getString("accessToken");
    }

    protected static String getToken() {
        if (ALT_TOKEN.get() == null) {
            return TOKEN.get();
        } else {
            return ALT_TOKEN.get();
        }
    }

    @When("I am testing as the {string} user")
    public void setTokenWithUser(String user) {
        String accessToken = getAccessTokenForUser(user);
        ALT_TOKEN.set(accessToken);
    }

    public static void clearCurrentToken() {
        ALT_TOKEN.remove();
    }

    @AfterAll
    public static void clearCache() {
        tokenCache.clear();
        clearCurrentToken();
    }
}
