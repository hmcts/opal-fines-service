package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.When;

/**
 * Defines Cucumber steps for generic exception-path scenarios.
 */
public class GenericExceptionPathStepDef extends BaseStepDef {
    /**
     * Sends a request to a non-existent endpoint to exercise the generic exception path.
     */
    @When("I attempt to hit an endpoint that doesn't exist")
    public void hitNonExistentEndpoint() {
        authorisedJsonRequest()
            .body("{}")
            .when()
            .post(getTestUrl() + "/nonExistentEndpoint");
    }
}
