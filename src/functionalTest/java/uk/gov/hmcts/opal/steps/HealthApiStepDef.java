package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.Then;
import uk.gov.hmcts.opal.workflows.HealthApiWorkflow;

/**
 * Defines Cucumber steps for the health endpoint.
 */
public class HealthApiStepDef extends BaseStepDef {
    private final HealthApiWorkflow workflow = new HealthApiWorkflow();

    /**
     * Calls the fines-service health endpoint and asserts that the service reports as UP.
     */
    @Then("I check the health of the fines api")
    public void checkHealthOfFinesApi() {
        workflow.checkHealthApiIsUp();
    }

}
