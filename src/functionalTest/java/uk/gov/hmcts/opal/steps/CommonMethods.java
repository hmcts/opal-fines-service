package uk.gov.hmcts.opal.steps;

import net.serenitybdd.rest.SerenityRest;

import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class CommonMethods extends BaseStepDef {

    public void getRequest(String refDataUri) {
        SerenityRest
            .given()
            .accept("*/*")
            .header("Authorization", "Bearer " + getToken())
            .contentType("application/json")
            .when()
            .get(getTestUrl() + refDataUri);
    }

}
