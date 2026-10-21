package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

/**
 * Defines E2E steps for deleting manual tills through testing support.
 */
public class ManualTillsDeleteStepDef extends BaseStepDef {

    private static final String TESTING_SUPPORT_TILLS_URI = "/testing-support/tills";
    private static final long NON_EXISTENT_TILL_ID = 999999999999L;

    /**
     * Calls the deployed testing-support endpoint with an ID that is not present in the test data.
     * This proves the endpoint is available without deleting data needed by another scenario.
     */
    @When("I delete a non-existent manual till using testing support")
    public void deleteNonExistentManualTillUsingTestingSupport() {
        authorisedJsonRequest()
            .queryParam("ids", NON_EXISTENT_TILL_ID)
            .when()
            .delete(getTestUrl() + TESTING_SUPPORT_TILLS_URI);
    }

    /**
     * Calls the endpoint with an empty IDs parameter to exercise request validation.
     */
    @When("I delete manual tills using testing support with an empty IDs value")
    public void deleteManualTillsWithEmptyIdsValue() {
        authorisedJsonRequest()
            .queryParam("ids", "")
            .when()
            .delete(getTestUrl() + TESTING_SUPPORT_TILLS_URI);
    }

    /**
     * Calls the endpoint with a non-numeric IDs parameter to exercise request validation.
     */
    @When("I delete manual tills using testing support with a malformed IDs value")
    public void deleteManualTillsWithMalformedIdsValue() {
        authorisedJsonRequest()
            .queryParam("ids", "not-a-till-id")
            .when()
            .delete(getTestUrl() + TESTING_SUPPORT_TILLS_URI);
    }

    /**
     * Confirms validation failures use the shared HTTP Problem Detail response shape.
     *
     * @param expectedStatus expected HTTP error status.
     */
    @Then("the manual-till deletion error matches the standard problem detail contract for status {int}")
    public void manualTillDeletionErrorMatchesStandardProblemDetailContract(int expectedStatus) {
        then()
            .statusCode(expectedStatus)
            .body("status", equalTo(expectedStatus))
            .body("title", notNullValue())
            .body("detail", notNullValue())
            .body("type", startsWith("https://hmcts.gov.uk/problems/"));
    }
}
