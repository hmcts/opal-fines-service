package uk.gov.hmcts.opal.steps;


import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;

import static uk.gov.hmcts.opal.config.Constants.LJA_REF_DATA_URI;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class LocalJusticeArea extends BaseStepDef {

    @When("I make a request to the LJA ref data api with")
    public void getRequestToLJARefData() {
        SerenityRest
            .given()
            .accept("*/*")
            .header("Authorization", "Bearer " + getToken())
            .contentType("application/json")
            .when()
            .get(getTestUrl() + LJA_REF_DATA_URI);

    }

}
