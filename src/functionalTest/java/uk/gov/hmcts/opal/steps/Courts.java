package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;

import static uk.gov.hmcts.opal.config.Constants.COURTS_REF_DATA_URI;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class Courts extends BaseStepDef {

    @When("I make a request to the court ref data api with")
    public void getRequestToCourtsRefData() {
        SerenityRest
            .given()
            .accept("*/*")
            .header("Authorization", "Bearer " + getToken())
            .contentType("application/json")
            .when()
            .get(getTestUrl() + COURTS_REF_DATA_URI).then().log().all();
    }
}
