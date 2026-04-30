package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import uk.gov.hmcts.opal.actions.HealthApiActions;
import uk.gov.hmcts.opal.assertions.HealthApiAssertions;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

/**
 * Defines Cucumber steps for the health endpoint.
 */
public class HealthApiStepDef extends BaseStepDef {
    private final HealthApiActions actions = new HealthApiActions();
    private final HealthApiAssertions assertions = new HealthApiAssertions();

    /**
     * Calls the fines-service health endpoint.
     */
    @When("I request the fines api health status")
    public void requestHealthOfFinesApi() {
        scenarioContext().setLatestHttpResponse(actions.getHealth());
    }

    /**
     * Asserts that the most recent health request reported the service as UP.
     */
    @Then("the fines service reports as up")
    public void finesServiceReportsAsUp() {
        TestHttpResponse response = scenarioContext().consumeLatestHttpResponse();
        Assertions.assertNotNull(response, "No health response is available to assert");
        assertions.assertServiceIsUp(response);
    }

}
