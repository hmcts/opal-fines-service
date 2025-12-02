package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.opal.utils.RequestSupport;

public class GenericExceptionPathStepDef extends BaseStepDef {

    @When("I attempt to hit an endpoint that doesn't exist")
    public void hitNonExistentEndpoint() {
        RequestSupport.responseProcessor(
            SerenityRest
                .given()
                .spec(RequestSupport.postRequestSpec("/nonExistentEndpoint", "{}").build())
                .contentType("application/xml")
                .accept("*/*")
                .when()
                .post()
                .then()
        );
    }
}
