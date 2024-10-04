package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;

import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class GenericExceptionPathStepDef extends BaseStepDef {
    @When("I attempt to hit an endpoint that doesn't exist")
    public void hitNonExistentEndpoint() {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/xml")
            .body("{}")
            .when()
            .post(getTestUrl() + "/nonExistentEndpoint");
    }
}
