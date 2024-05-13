package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;

import static uk.gov.hmcts.opal.config.Constants.OFFENCES_REF_DATA_URI;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class Offences extends BaseStepDef{

    @When("I make a request to the offence ref data api with")
    public void iMakeARequestToTheOffenceRefDataApiWith() {
        SerenityRest.given()
            .accept("*/*")
            .header("Authorization", "Bearer " + getToken())
            .contentType("application/json")
            .when()
            .get(getTestUrl() + OFFENCES_REF_DATA_URI).then().log().all();

    }

}
