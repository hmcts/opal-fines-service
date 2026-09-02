package uk.gov.hmcts.opal.steps.interfacejobs;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.time.Duration;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;
import uk.gov.hmcts.opal.utils.TestHttpClient;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

/**
 * Defines functional-test steps for the processed interface-job file summary endpoint.
 */
public class InterfaceJobsProcessedFileSummaryStepDef extends BaseStepDef {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final short BUSINESS_UNIT_ID = 78;
    private static final String INTERFACE_NAME = "E2E Processed File Summary";
    private static final String INTERFACE_JOBS_PATH = "/interface-jobs";
    private static final String PROCESS_PATH = "/interface-jobs/process";
    private static final String SUMMARY_PATH = "/interface-jobs/summary";
    private static final String TESTING_SUPPORT_PATH = "/testing-support/interface-jobs";
    private static final Duration COMPLETION_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(2);
    private static final Set<String> SUMMARY_FIELDS = Set.of(
        "file_name", "source", "business_unit_name", "total_amount", "total_records", "total_errors",
        "interface_messages"
    );
    private static final Set<String> MESSAGE_GROUP_FIELDS = Set.of("message_text", "messages");
    private static final Set<String> MESSAGE_FIELDS = Set.of("interface_messages_id", "message_data", "message_type");

    private Long createdInterfaceJobId;
    private String createdByToken;

    /**
     * Creates an interface job and remembers its generated ID for the current scenario.
     */
    @Given("I create an interface job for processed file summary")
    public void createInterfaceJobForProcessedFileSummary() {
        createdByToken = BearerTokenStepDef.getToken();
        Response response = authorisedJsonRequest()
            .body(createRequestBody())
            .when()
            .post(getTestUrl() + INTERFACE_JOBS_PATH);

        assertEquals(200, response.statusCode(), "Expected interface job creation to succeed");
        createdInterfaceJobId = JsonPath.from(response.asString()).getLong("interface_jobs[0].interface_job_id");
    }

    /**
     * Submits the created interface job to the existing interface-job processor.
     */
    @When("I submit the interface job for processing")
    public void submitInterfaceJobForProcessing() {
        long interfaceJobId = createdInterfaceJobIdOrFail();
        authorisedJsonRequest()
            .body(processRequestBody(interfaceJobId))
            .when()
            .post(getTestUrl() + PROCESS_PATH);

        then().log().ifValidationFails().statusCode(200);
    }

    /**
     * Polls the interface-job summary until the asynchronous processor marks the job completed.
     */
    @When("I wait for the interface job to complete")
    public void waitForInterfaceJobToComplete() {
        long interfaceJobId = createdInterfaceJobIdOrFail();
        long deadline = System.nanoTime() + COMPLETION_TIMEOUT.toNanos();
        String lastStatus = "unknown";

        while (System.nanoTime() < deadline) {
            TestHttpResponse response = TestHttpClient.request(
                "GET",
                getTestUrl() + SUMMARY_PATH + "?business_unit_ids=" + BUSINESS_UNIT_ID
                    + "&interface_name=" + INTERFACE_NAME.replace(" ", "%20"),
                authorisedHeaders(),
                null);
            assertEquals(200, response.statusCode(), "Expected interface-job summary request to succeed");

            List<Map<String, Object>> jobs = JsonPath.from(response.body()).getList("interface_jobs");
            Map<String, Object> job = jobs.stream()
                .filter(item -> ((Number) item.get("interface_job_id")).longValue() == interfaceJobId)
                .findFirst()
                .orElse(null);

            if (job != null) {
                lastStatus = String.valueOf(job.get("status"));
                if ("COMPLETED".equals(lastStatus)) {
                    return;
                }
                if ("FAILED".equals(lastStatus)) {
                    throw new AssertionError("Interface job failed during processing: " + interfaceJobId);
                }
            }

            sleepBeforeNextPoll();
        }

        throw new AssertionError("Timed out waiting for interface job " + interfaceJobId
            + " to complete; last status was " + lastStatus);
    }

    /**
     * Requests the summary using the ID generated during the scenario.
     */
    @When("I request the processed file summary for the completed interface job")
    public void requestProcessedFileSummaryForCompletedInterfaceJob() {
        authorisedJsonRequest()
            .accept("application/json")
            .when()
            .get(getTestUrl() + "/interface-jobs/" + createdInterfaceJobIdOrFail()
                + "/processed-file-summary");
    }

    /**
     * Removes any interface job created by this feature scenario.
     */
    @After(order = Integer.MAX_VALUE)
    public void cleanUpCreatedInterfaceJob() {
        if (createdInterfaceJobId == null) {
            return;
        }

        TestHttpClient.request(
            "DELETE",
            getTestUrl() + TESTING_SUPPORT_PATH + "?ids=" + createdInterfaceJobId,
            authorisedHeaders(createdByToken),
            null);
        createdInterfaceJobId = null;
        createdByToken = null;
    }

    /**
     * Requests the supplied processed-file-summary endpoint using the current scenario user.
     *
     * @param path endpoint path to request.
     */
    @When("I request GET {string}")
    public void requestGet(String path) {
        authorisedJsonRequest()
            .accept("application/json")
            .when()
            .get(getTestUrl() + path);
    }

    /**
     * Checks the documented top-level and nested response structure and field types.
     */
    @Then("the response matches the processed file summary schema")
    public void responseMatchesProcessedFileSummarySchema() {
        JsonNode response = readResponse();

        assertFieldNames(response, SUMMARY_FIELDS, "processed file summary");
        assertTrue(response.path("file_name").isTextual(), "file_name must be a string");
        assertTrue(response.path("source").isTextual(), "source must be a string");
        assertTrue(response.path("business_unit_name").isTextual(), "business_unit_name must be a string");
        assertTrue(response.path("total_amount").isNumber(), "total_amount must be numeric");
        assertTrue(response.path("total_records").isIntegralNumber(), "total_records must be an integer");
        assertTrue(response.path("total_errors").isIntegralNumber(), "total_errors must be an integer");
        assertTrue(response.path("interface_messages").isArray(), "interface_messages must be an array");

        for (JsonNode group : response.path("interface_messages")) {
            assertFieldNames(group, MESSAGE_GROUP_FIELDS, "message group");
            assertTrue(group.path("message_text").isTextual(), "message_text must be a string");
            assertTrue(group.path("messages").isArray(), "messages must be an array");

            for (JsonNode message : group.path("messages")) {
                assertFieldNames(message, MESSAGE_FIELDS, "interface message");
                assertTrue(message.path("interface_messages_id").isIntegralNumber(),
                    "interface_messages_id must be an integer");
                assertTrue(message.path("message_data").isObject(), "message_data must be an object");
                assertTrue(message.path("message_type").isTextual(), "message_type must be a string");
            }
        }
    }

    /**
     * Confirms that the response contains one group per message text and that each group
     * contains a messages array.
     */
    @Then("interface messages are grouped by message text")
    public void interfaceMessagesAreGroupedByMessageText() {
        JsonNode response = readResponse();
        Set<String> messageTexts = new HashSet<>();

        for (JsonNode group : response.path("interface_messages")) {
            String messageText = group.path("message_text").asText();

            assertTrue(messageTexts.add(messageText),
                "Duplicate message group found: " + messageText);
            assertTrue(group.path("messages").isArray(),
                "Each message group must contain a messages array");
        }
    }

    private JsonNode readResponse() {
        try {
            return OBJECT_MAPPER.readTree(then().extract().asString());
        } catch (JacksonException e) {
            throw new AssertionError("Processed file summary response was not valid JSON", e);
        }
    }

    private void assertFieldNames(JsonNode node, Set<String> expectedFields, String objectName) {
        Set<String> actualFields = new HashSet<>();
        node.properties().forEach(property -> actualFields.add(property.getKey()));
        assertEquals(expectedFields, actualFields, "Unexpected fields in " + objectName);
    }

    private String createRequestBody() {
        return """
            {
              "interface_jobs": [
                {
                  "file_name": "e2e-processed-file-summary.dat",
                  "source": "NATWEST",
                  "records": "[{\\\"receiving_sort_code\\\":\\\"123456\\\",\\\"receiving_bank_account_number\\\":\\\"01234567\\\",\\\"receiving_account_type\\\":\\\"5\\\",\\\"transaction_code\\\":\\\"68\\\",\\\"originator_sort_code\\\":\\\"654321\\\",\\\"originator_bank_account_number\\\":\\\"98765432\\\",\\\"amount_pence\\\":\\\"12345\\\",\\\"originator_name\\\":\\\"Test Payer\\\",\\\"originator_reference\\\":\\\"99000001A\\\",\\\"originator_beneficiary_name\\\":\\\"Test Court\\\"}]",
                  "business_unit_id": %d,
                  "interface_name": "%s",
                  "created_datetime": "2026-07-14T10:00:00"
                }
              ]
            }
            """.formatted(BUSINESS_UNIT_ID, INTERFACE_NAME);
    }

    private String processRequestBody(long interfaceJobId) {
        return """
            {
              "interface_jobs": [
                {
                  "interface_job_id": %d,
                  "business_unit_id": %d,
                  "override_inhibits": false
                }
              ]
            }
            """.formatted(interfaceJobId, BUSINESS_UNIT_ID);
    }

    private Map<String, String> authorisedHeaders() {
        return authorisedHeaders(BearerTokenStepDef.getToken());
    }

    private Map<String, String> authorisedHeaders(String token) {
        return Map.of(
            "Accept", "*/*",
            "Content-Type", "application/json",
            "Authorization", "Bearer " + token);
    }

    private long createdInterfaceJobIdOrFail() {
        if (createdInterfaceJobId == null) {
            throw new IllegalStateException("No interface job has been created for this scenario");
        }
        return createdInterfaceJobId;
    }

    private void sleepBeforeNextPoll() {
        try {
            Thread.sleep(POLL_INTERVAL.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for interface job completion", e);
        }
    }
}
