package uk.gov.hmcts.opal.steps;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines deployed-environment steps for the interface-job processing endpoint.
 */
public class InterfaceJobsProcessStepDef extends BaseStepDef {

    private static final Logger log = LoggerFactory.getLogger(InterfaceJobsProcessStepDef.class);
    private static final String CREATE_PATH = "/interface-jobs";
    private static final String PROCESS_PATH = "/interface-jobs/process";
    private static final String SUMMARY_PATH = "/interface-jobs/summary";
    private static final String TESTING_SUPPORT_PATH = "/testing-support/interface-jobs";
    private static final String INTERFACE_NAME = "E2E Process Interface Jobs";
    private static final short BUSINESS_UNIT_ID = 78;
    private static final String USER_WITHOUT_PERMISSION = "opal-test-2@dev.platform.hmcts.net";

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
        then()
            .log().ifValidationFails()
            .statusCode(200);
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

    @Then("the eligible jobs are returned as processing by the summary API")
    public void eligibleJobsAreReturnedAsProcessingBySummaryApi() {
        for (ProcessJob job : eligibleJobs) {
            assertJobStatus(job, "PROCESSING");
        }
    }

    /**
     * Confirms the jobs created by the scenario are persisted and visible to the current user.
     */
    @Then("the seeded interface jobs are visible in the summary API")
    public void seededInterfaceJobsAreVisibleInSummaryApi() {
        for (ProcessJob job : eligibleJobs) {
            assertJobStatus(job, "CREATED");
        }
    }

    @Then("the unprocessed mixed-status job remains created")
    public void unprocessedMixedStatusJobRemainsCreated() {
        assertJobStatus(unprocessedJob, "CREATED");
    }

    @Then("the process response is 200 with an empty body")
    public void processResponseIsSuccessfulAndEmpty() {
        then()
            .log().ifValidationFails()
            .statusCode(200);
        assertEquals("", then().extract().asString(), "The successful process response must have no body");
    }

    @After(order = Integer.MAX_VALUE)
    public void cleanUpCreatedInterfaceJobs() {
        List<Long> jobIds = allCreatedJobIds();
        if (jobIds.isEmpty()) {
            return;
        }

        try {
            RequestSpecification cleanupRequest = authorisedJsonRequest();
            jobIds.forEach(jobId -> cleanupRequest.queryParam("ids", jobId));
            Response response = cleanupRequest
                .when()
                .delete(getTestUrl() + TESTING_SUPPORT_PATH);
            if (response.statusCode() != 200 && response.statusCode() != 204) {
                log.warn("Unable to clean up interface jobs {}: HTTP {}", jobIds, response.statusCode());
            }
        } catch (RuntimeException e) {
            log.warn("Unable to clean up interface jobs {}", jobIds, e);
        }
    }

    private ProcessJob createInterfaceJob() throws JSONException {
        final boolean overrideInhibits = eligibleJobs.size() % 2 == 0;
        JSONObject request = new JSONObject()
            .put("interface_jobs", new JSONArray().put(new JSONObject()
                .put("file_name", "e2e-process-" + UUID.randomUUID() + ".dat")
                .put("source", "NATWEST")
                .put("records", "[{\"account\":\"abc123\"}]")
                .put("business_unit_id", BUSINESS_UNIT_ID)
                .put("interface_name", INTERFACE_NAME)
                .put("created_datetime", LocalDateTime.now().withNano(0).toString())));

        authorisedJsonRequest()
            .body(request.toString())
            .when()
            .post(getTestUrl() + CREATE_PATH);

        then()
            .log().ifValidationFails()
            .statusCode(200);
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

    private void assertJobStatus(ProcessJob expectedJob, String expectedStatus) {
        Response response = authorisedJsonRequest()
            .queryParam("business_unit_ids", BUSINESS_UNIT_ID)
            .queryParam("interface_name", INTERFACE_NAME)
            .when()
            .get(getTestUrl() + SUMMARY_PATH);

        assertEquals(200, response.statusCode(), "Expected interface-job summary request to succeed");

        List<Map<String, Object>> summaries = response.jsonPath().getList("interface_jobs");
        Map<String, Object> jobSummary = summaries.stream()
            .filter(summary -> ((Number) summary.get("interface_job_id")).longValue() == expectedJob.id())
            .findFirst()
            .orElseThrow(() -> new AssertionError("Interface job " + expectedJob.id()
                + " was not returned by the summary API"));

        assertEquals(expectedStatus, jobSummary.get("status"),
            "Unexpected status for interface job " + expectedJob.id());
    }

    private List<Long> allCreatedJobIds() {
        List<Long> jobIds = new ArrayList<>();
        eligibleJobs.stream().map(ProcessJob::id).forEach(jobIds::add);
        if (alreadyProcessingJob != null) {
            jobIds.add(alreadyProcessingJob.id());
        }
        if (unprocessedJob != null) {
            jobIds.add(unprocessedJob.id());
        }
        return jobIds.stream().distinct().toList();
    }

    private record ProcessJob(long id, boolean overrideInhibits) {
    }
}
