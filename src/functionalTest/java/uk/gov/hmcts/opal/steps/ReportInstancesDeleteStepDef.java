package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

/**
 * Defines E2E steps for deleting report instances through testing support.
 */
public class ReportInstancesDeleteStepDef extends BaseStepDef {

    private static final String TESTING_SUPPORT_REPORT_INSTANCES_URI = "/testing-support/report-instances";
    private static final long NON_EXISTENT_REPORT_INSTANCE_ID = 999999999999L;

    /**
     * Calls the deployed testing-support endpoint with an ID that is not present in the test data.
     * This proves the endpoint is available without deleting data needed by another scenario.
     */
    @When("I delete a non-existent report instance using testing support")
    public void deleteNonExistentReportInstanceUsingTestingSupport() {
        authorisedJsonRequest()
            .queryParam("ids", NON_EXISTENT_REPORT_INSTANCE_ID)
            .when()
            .delete(getTestUrl() + TESTING_SUPPORT_REPORT_INSTANCES_URI);
    }

    /**
     * Calls the endpoint with an empty IDs parameter to exercise request validation.
     */
    @When("I delete report instances using testing support with an empty IDs value")
    public void deleteReportInstancesWithEmptyIdsValue() {
        authorisedJsonRequest()
            .queryParam("ids", "")
            .when()
            .delete(getTestUrl() + TESTING_SUPPORT_REPORT_INSTANCES_URI);
    }

    /**
     * Calls the endpoint with a non-numeric IDs parameter to exercise request validation.
     */
    @When("I delete report instances using testing support with a malformed IDs value")
    public void deleteReportInstancesWithMalformedIdsValue() {
        authorisedJsonRequest()
            .queryParam("ids", "not-a-report-instance-id")
            .when()
            .delete(getTestUrl() + TESTING_SUPPORT_REPORT_INSTANCES_URI);
    }

    /**
     * Confirms validation failures use the shared HTTP Problem Detail response shape.
     *
     * @param expectedStatus expected HTTP error status.
     */
    @Then("the report-instance deletion error matches the standard problem detail contract for status {int}")
    public void reportInstanceDeletionErrorMatchesStandardProblemDetailContract(int expectedStatus) {
        then()
            .statusCode(expectedStatus)
            .body("status", equalTo(expectedStatus))
            .body("title", notNullValue())
            .body("detail", notNullValue())
            .body("type", startsWith("https://hmcts.gov.uk/problems/"));
    }
}
