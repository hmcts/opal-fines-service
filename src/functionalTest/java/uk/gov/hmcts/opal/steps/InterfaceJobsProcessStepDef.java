package uk.gov.hmcts.opal.steps;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.specification.RequestSpecification;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;
import uk.gov.hmcts.opal.utils.InterfaceJobsProcessDatabaseClient;
import uk.gov.hmcts.opal.utils.InterfaceJobsProcessQueueClient;

/**
 * Defines deployed-environment steps for the interface-job processing endpoint.
 */
public class InterfaceJobsProcessStepDef extends BaseStepDef {

    private static final String CREATE_PATH = "/interface-jobs";
    private static final String PROCESS_PATH = "/interface-jobs/process";
    private static final short BUSINESS_UNIT_ID = 78;
    private static final String USER_WITHOUT_PERMISSION = "opal-test-2@dev.platform.hmcts.net";

    private final InterfaceJobsProcessDatabaseClient databaseClient = new InterfaceJobsProcessDatabaseClient();
    private final InterfaceJobsProcessQueueClient queueClient = new InterfaceJobsProcessQueueClient();
    private final List<ProcessJob> eligibleJobs = new ArrayList<>();
    private ProcessJob alreadyProcessingJob;
    private ProcessJob unprocessedJob;

    @Given("I create two eligible interface jobs for processing")
    public void createEligibleInterfaceJobs() throws JSONException {
        eligibleJobs.clear();
        eligibleJobs.add(createInterfaceJob());
        eligibleJobs.add(createInterfaceJob());
    }

    @Given("I create an interface job that has already begun processing and an unprocessed interface job")
    public void createMixedStatusInterfaceJobs() throws JSONException {
        alreadyProcessingJob = createInterfaceJob();
        unprocessedJob = createInterfaceJob();
        submitProcessRequest(buildProcessRequest(alreadyProcessingJob), authorisedJsonRequest());
        then().statusCode(200);
    }

    @When("I submit the eligible interface jobs for processing")
    public void submitEligibleInterfaceJobs() throws JSONException {
        submitProcessRequest(buildProcessRequest(eligibleJobs), authorisedJsonRequest());
    }

    @When("I submit the mixed-status interface jobs for processing")
    public void submitMixedStatusInterfaceJobs() throws JSONException {
        submitProcessRequest(buildProcessRequest(alreadyProcessingJob, unprocessedJob), authorisedJsonRequest());
    }

    @When("a user without payment processing permission submits the eligible interface jobs for processing")
    public void submitEligibleJobsWithoutPermission() throws JSONException {
        submitProcessRequest(buildProcessRequest(eligibleJobs), jsonRequestWithToken(
            BearerTokenStepDef.getAccessTokenForUser(USER_WITHOUT_PERMISSION)));
    }

    @When("I submit the eligible interface jobs for processing without a token")
    public void submitEligibleJobsWithoutToken() throws JSONException {
        submitProcessRequest(buildProcessRequest(eligibleJobs), jsonRequestWithOptionalToken(null));
    }

    @When("I submit the eligible interface jobs for processing with an invalid token")
    public void submitEligibleJobsWithInvalidToken() throws JSONException {
        submitProcessRequest(buildProcessRequest(eligibleJobs), jsonRequestWithToken("invalid-token"));
    }

    @Then("the eligible jobs are updated in the database")
    public void eligibleJobsAreUpdatedInDatabase() throws SQLException {
        for (ProcessJob job : eligibleJobs) {
            assertJobProcessingState(databaseClient.loadJobState(job.id()), job.overrideInhibits());
        }
    }

    @Then("the eligible jobs are present on the process-interface-files queue")
    public void eligibleJobsArePresentOnQueue() {
        eligibleJobs.forEach(job -> assertTrue(queueClient.eventuallyContainsJob(job.id())));
    }

    @Then("the unprocessed mixed-status job remains unchanged")
    public void unprocessedMixedStatusJobRemainsUnchanged() throws SQLException {
        InterfaceJobsProcessDatabaseClient.JobState job = databaseClient.loadJobState(unprocessedJob.id());
        assertEquals("CREATED", job.status());
        assertFalse(job.overrideInhibits());
        assertNull(job.startedDatetime());
    }

    @Then("the unprocessed mixed-status job is not present on the process-interface-files queue")
    public void unprocessedMixedStatusJobIsAbsentFromQueue() {
        assertFalse(queueClient.eventuallyContainsJob(unprocessedJob.id()));
    }

    @Then("the process response is 200 with an empty body")
    public void processResponseIsSuccessfulAndEmpty() {
        then().statusCode(200);
        assertEquals("", then().extract().asString(), "The successful process response must have no body");
    }

    private ProcessJob createInterfaceJob() throws JSONException {
        final boolean overrideInhibits = eligibleJobs.size() % 2 == 0;
        JSONObject request = new JSONObject()
            .put("interface_jobs", new JSONArray().put(new JSONObject()
                .put("file_name", "e2e-process-" + UUID.randomUUID() + ".dat")
                .put("source", "NATWEST")
                .put("records", "[{\\\"account\\\":\\\"abc123\\\"}]")
                .put("business_unit_id", BUSINESS_UNIT_ID)
                .put("interface_name", "E2E Process Interface Jobs")
                .put("created_datetime", LocalDateTime.now().withNano(0).toString())));

        authorisedJsonRequest()
            .body(request.toString())
            .when()
            .post(getTestUrl() + CREATE_PATH);

        then().statusCode(200);
        JSONArray createdJobs = new JSONObject(then().extract().asString()).getJSONArray("interface_jobs");
        assertEquals(1, createdJobs.length(), "Expected one created interface job");
        return new ProcessJob(createdJobs.getJSONObject(0).getLong("interface_job_id"), overrideInhibits);
    }

    private void submitProcessRequest(JSONObject requestBody, RequestSpecification request) {
        request.body(requestBody.toString())
            .when()
            .post(getTestUrl() + PROCESS_PATH);
    }

    private JSONObject buildProcessRequest(List<ProcessJob> jobs) throws JSONException {
        return buildProcessRequest(jobs.toArray(ProcessJob[]::new));
    }

    private JSONObject buildProcessRequest(ProcessJob... jobs) throws JSONException {
        JSONArray interfaceJobs = new JSONArray();
        for (ProcessJob job : jobs) {
            interfaceJobs.put(new JSONObject()
                .put("interface_job_id", job.id())
                .put("business_unit_id", BUSINESS_UNIT_ID)
                .put("override_inhibits", job.overrideInhibits()));
        }
        return new JSONObject().put("interface_jobs", interfaceJobs);
    }

    private void assertJobProcessingState(InterfaceJobsProcessDatabaseClient.JobState job, boolean overrideInhibits) {
        assertEquals("PROCESSING", job.status());
        assertNotNull(job.startedDatetime());
        assertEquals(overrideInhibits, job.overrideInhibits());
    }

    private record ProcessJob(long id, boolean overrideInhibits) {
    }
}
