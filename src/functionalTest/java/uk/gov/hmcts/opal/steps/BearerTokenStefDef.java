package uk.gov.hmcts.opal.steps;

import io.cucumber.java.BeforeAll;
import net.serenitybdd.rest.SerenityRest;

import static net.serenitybdd.rest.SerenityRest.then;


public class BearerTokenStefDef extends BaseStepDef {
    protected static String TOKEN;

    @BeforeAll
    public static void setToken() {
        SerenityRest.given()
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + "/api/testing-support/token/test-user");
        then().assertThat().statusCode(200);
        TOKEN = then().extract().body().jsonPath().getString("access_token");
        System.out.println("Token - " + TOKEN);
    }

    protected static String getToken() {
        return TOKEN;
    }

}
