package uk.gov.hmcts.opal.steps;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;

import static net.serenitybdd.rest.SerenityRest.then;


public class BearerTokenStepDef extends BaseStepDef {
    protected static String TOKEN;
    protected static ThreadLocal<String> ALT_TOKEN = new ThreadLocal<>();

    @BeforeAll
    public static void setToken() {
        SerenityRest.given()
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + "/api/testing-support/token/test-user");
        then().assertThat().statusCode(200);
        TOKEN = then().extract().body().jsonPath().getString("access_token");
    }

    protected static String getToken() {
        if (ALT_TOKEN.get() == null) {
            return TOKEN;
        } else {
            return ALT_TOKEN.get();
        }
    }

    @When("I am testing as the {string} user")
    public void setTokenWithUser(String user) {
        SerenityRest.given()
            .accept("*/*")
            .header("X-User-Email", user)
            .contentType("application/json")
            .when()
            .get(getTestUrl() + "/api/testing-support/token/user");
        then().assertThat().statusCode(200);
        ALT_TOKEN.set(then().extract().body().jsonPath().getString("access_token"));
    }

}
