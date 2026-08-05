package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import uk.gov.hmcts.opal.utils.TestHttpClient;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Defines E2E steps for deleting interface jobs through testing support.
 *
 * <p>E2E.02 proves the deployed Create, GET summary, and Delete APIs together. The related
 * message, till, and payment-in database records remain covered by integration tests because
 * the deployed APIs do not yet expose a way to create and inspect all of those records.</p>
 */
public class InterfeceJobsDelStepDef extends BaseStepDef {

    private static final String TESTING_SUPPORT_INTERFACE_JOBS_URI = "/testing-support/interface-jobs";
    private static final String INTERFACE_JOBS_URI = "/interface-jobs";
    private static final String INTERFACE_JOBS_SUMMARY_URI = "/interface-jobs/summary";
    private static final long NON_EXISTENT_INTERFACE_JOB_ID = 9_000_000_000_000L;
    private static final short BUSINESS_UNIT_ID = 78;

    private Long createdInterfaceJobId;

    @When("I request deletion of an interface job in the enabled E2E environment")
    public void requestDeletionInEnabledE2eEnvironment() {
        deleteInterfaceJob(NON_EXISTENT_INTERFACE_JOB_ID);
    }

    @Given("an isolated interface job has been created")
    public void createAnIsolatedInterfaceJob() {
        TestHttpResponse response = TestHttpClient.request(
            "POST",
            getTestUrl() + INTERFACE_JOBS_URI,
            authorisedJsonHeaders(),
            createInterfaceJobRequestBody()
        );

        assertEquals(200, response.statusCode(), "Expected interface job creation to succeed");
        createdInterfaceJobId = JsonPath.from(response.body()).getLong("interface_jobs[0].interface_job_id");
    }

    @When("I delete the created interface job using testing support")
    public void deleteCreatedInterfaceJobUsingTestingSupport() {
        deleteInterfaceJob(createdInterfaceJobIdOrFail());
    }

    @Then("the created interface job is returned by the interface-jobs summary API")
    public void createdInterfaceJobIsReturnedByTheSummaryApi() {
        assertTrue(summaryContains(createdInterfaceJobIdOrFail()),
            "Created interface job is not present in the summary response");
    }

    @Then("the created interface job is no longer returned by the interface-jobs summary API")
    public void createdInterfaceJobIsNoLongerReturnedByTheSummaryApi() {
        assertFalse(summaryContains(createdInterfaceJobIdOrFail()),
            "Deleted interface job remains in the summary response");
    }

    private void deleteInterfaceJob(long interfaceJobId) {
        authorisedJsonRequest()
            .queryParam("ids", interfaceJobId)
            .when()
            .delete(getTestUrl() + TESTING_SUPPORT_INTERFACE_JOBS_URI);
    }

    private boolean summaryContains(long interfaceJobId) {
        TestHttpResponse response = TestHttpClient.request(
            "GET",
            getTestUrl() + INTERFACE_JOBS_SUMMARY_URI + "?business_unit_ids=" + BUSINESS_UNIT_ID
                + "&interface_name=" + URLEncoder.encode("E2E Delete Interface Jobs", StandardCharsets.UTF_8),
            authorisedJsonHeaders(),
            null
        );

        assertEquals(200, response.statusCode(), "Expected interface-jobs summary request to succeed");
        List<Long> returnedJobIds = JsonPath.from(response.body())
            .getList("interface_jobs.interface_job_id", Long.class);
        return returnedJobIds.contains(interfaceJobId);
    }

    private Map<String, String> authorisedJsonHeaders() {
        return Map.of(
            "Accept", "*/*",
            "Content-Type", "application/json",
            "Authorization", "Bearer " + BearerTokenStepDef.getToken()
        );
    }

    private String createInterfaceJobRequestBody() {
        return """
               {
                 "interface_jobs": [
                   {
                     "file_name": "e2e-delete-interface-job.dat",
                     "source": "NATWEST",
                     "records": "[{\\"account\\":\\"abc123\\"}]",
                     "business_unit_id": %d,
                     "interface_name": "E2E Delete Interface Jobs",
                     "created_datetime": "2026-07-14T10:00:00"
                   }
                 ]
               }
               """.formatted(BUSINESS_UNIT_ID);
    }

    private long createdInterfaceJobIdOrFail() {
        if (createdInterfaceJobId == null) {
            throw new IllegalStateException("No interface job has been created for this scenario");
        }
        return createdInterfaceJobId;
    }
}
