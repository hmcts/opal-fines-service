package uk.gov.hmcts.opal.steps;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

/**
 * Defines deployed-environment steps for the till summary endpoint.
 */
public class TillsStepDef extends BaseStepDef {

    private static final Logger log = LoggerFactory.getLogger(TillsStepDef.class);
    private static final String TILLS_PATH = "/tills";
    private static final String INTERFACE_JOBS_PATH = "/interface-jobs";
    private static final String PROCESS_INTERFACE_JOBS_PATH = "/interface-jobs/process";
    private static final String INTERFACE_JOBS_SUMMARY_PATH = "/interface-jobs/summary";
    private static final String TESTING_SUPPORT_INTERFACE_JOBS_PATH = "/testing-support/interface-jobs";
    private static final short AUTO_PAYMENT_BUSINESS_UNIT_ID = 77;
    private static final String AUTO_PAYMENT_INTERFACE_NAME = "PAYMENTS_IN";
    private static final Duration TILL_PROCESSING_TIMEOUT = Duration.ofMinutes(2);
    private static final Duration TILL_PROCESSING_POLL_INTERVAL = Duration.ofSeconds(2);
    private static final List<String> DOCUMENTED_TILL_FIELDS = List.of(
        "amount",
        "business_unit_name",
        "date_processed",
        "errors",
        "file_name",
        "processed_by",
        "source",
        "till_number");

    private Long createdInterfaceJobId;
    private String createdAutoPaymentFileName;
    private Response generatedTillResponse;
    private Map<String, Object> generatedTill;

    /**
     * Retrieves tills scoped to one business unit for the current authenticated user.
     *
     * @param businessUnitId business unit to request
     */
    @When("I request tills for business unit {int}")
    public void requestTillsForBusinessUnit(int businessUnitId) {
        authorisedJsonRequest()
            .queryParam("business_unit_ids", businessUnitId)
            .when()
            .get(getTestUrl() + TILLS_PATH);
    }

    /**
     * Creates and submits a payment-in file whose bank details match the PO-10529 configuration.
     *
     * @param businessUnitId business unit in which to create the auto-payment till
     */
    @Given("I create and process an auto-payment interface job for business unit {int}")
    public void createAndProcessAutoPaymentInterfaceJob(int businessUnitId) throws JSONException {
        assertEquals(AUTO_PAYMENT_BUSINESS_UNIT_ID, businessUnitId,
            "E2E.02 uses the QA business unit configured for auto-payment processing");
        createdAutoPaymentFileName = "po-2575-auto-payment-" + UUID.randomUUID() + ".json";

        Response createResponse = authorisedJsonRequest()
            .body(createAutoPaymentInterfaceJobRequest().toString())
            .when()
            .post(getTestUrl() + INTERFACE_JOBS_PATH);

        assertEquals(200, createResponse.statusCode(), "Creating the auto-payment interface job must succeed");
        createdInterfaceJobId = createResponse.jsonPath().getLong("interface_jobs[0].interface_job_id");
        assertNotNull(createdInterfaceJobId, "Creating the interface job must return its ID");

        Response processResponse = authorisedJsonRequest()
            .body(new JSONObject().put("interface_jobs", new JSONArray().put(new JSONObject()
                .put("interface_job_id", createdInterfaceJobId)
                .put("business_unit_id", AUTO_PAYMENT_BUSINESS_UNIT_ID)
                .put("override_inhibits", true))).toString())
            .when()
            .post(getTestUrl() + PROCESS_INTERFACE_JOBS_PATH);

        assertEquals(200, processResponse.statusCode(), "Submitting the auto-payment interface job must succeed");
    }

    /**
     * Waits for the asynchronous interface-job consumer to create the till for this scenario's
     * uniquely named source file.
     */
    @When("I request the generated auto-payment till")
    public void requestGeneratedAutoPaymentTill() {
        long deadlineNanos = System.nanoTime() + TILL_PROCESSING_TIMEOUT.toNanos();
        do {
            String interfaceJobStatus = getCreatedInterfaceJobStatus();
            if ("FAILED".equals(interfaceJobStatus) || "IGNORED".equals(interfaceJobStatus)) {
                throw new AssertionError("Auto-payment interface job " + createdInterfaceJobId
                    + " reached terminal status " + interfaceJobStatus + " before a till was created");
            }

            generatedTillResponse = authorisedJsonRequest()
                .queryParam("business_unit_ids", AUTO_PAYMENT_BUSINESS_UNIT_ID)
                .queryParam("auto_payments", true)
                .when()
                .get(getTestUrl() + TILLS_PATH);

            if (generatedTillResponse.statusCode() == 200) {
                List<Map<String, Object>> tills = generatedTillResponse.jsonPath().getList("tills");
                generatedTill = tills == null ? null : tills.stream()
                    .filter(till -> createdAutoPaymentFileName.equals(till.get("file_name")))
                    .findFirst()
                    .orElse(null);
                if (generatedTill != null) {
                    return;
                }
            }
            sleepBeforeTillPoll();
        } while (System.nanoTime() < deadlineNanos);

        throw new AssertionError("No auto-payment till was created for interface job " + createdInterfaceJobId
            + " and file " + createdAutoPaymentFileName + " within " + TILL_PROCESSING_TIMEOUT
            + ". Interface-job status: " + getCreatedInterfaceJobStatus());
    }

    /**
     * Retrieves a combination of filters that cannot match a till in the deployed environment.
     *
     * @param businessUnitId a valid int16 business unit ID reserved here as unmatched test input
     */
    @When("I request allocated auto-payment tills for unmatched business unit {int}")
    public void requestUnmatchedAllocatedAutoPaymentTills(int businessUnitId) {
        authorisedJsonRequest()
            .queryParam("business_unit_ids", businessUnitId)
            .queryParam("statuses", "ALLOCATED")
            .queryParam("auto_payments", true)
            .when()
            .get(getTestUrl() + TILLS_PATH);
    }

    /**
     * Confirms row-level permission filtering or unmatched criteria return the documented empty
     * success contract rather than a permission failure.
     */
    @Then("the tills response is an empty successful response")
    public void tillsResponseIsEmptyAndSuccessful() {
        assertSuccessfulResponse();

        List<Map<String, Object>> tills = then().extract().jsonPath().getList("tills");
        assertNotNull(tills, "Response must contain tills");
        assertTrue(tills.isEmpty(), "Expected no tills for the requested permissions and filters");
    }

    /**
     * Confirms auto-payment results expose only the OpenAPI fields, including the original-file
     * details required by the auto-payment view.
     */
    @Then("the auto-payment tills response contains documented till and original file details")
    public void autoPaymentTillsResponseContainsDocumentedDetails() {
        assertNotNull(generatedTillResponse, "The generated auto-payment till must be requested first");
        assertEquals(200, generatedTillResponse.statusCode(), "Retrieving the generated till must succeed");
        assertEquals(MediaType.APPLICATION_JSON_VALUE, generatedTillResponse.contentType(),
            "The generated till response must be JSON");
        assertNotNull(generatedTill, "The generated auto-payment till must be returned");

        assertEquals(DOCUMENTED_TILL_FIELDS, generatedTill.keySet().stream().sorted().toList(),
            "The generated till must contain only documented fields");
        assertInstanceOf(Number.class, generatedTill.get("till_number"));
        assertInstanceOf(Number.class, generatedTill.get("errors"));
        assertInstanceOf(Number.class, generatedTill.get("amount"));
        assertInstanceOf(String.class, generatedTill.get("business_unit_name"));
        assertInstanceOf(String.class, generatedTill.get("processed_by"));
        assertInstanceOf(String.class, generatedTill.get("date_processed"));
        assertInstanceOf(String.class, generatedTill.get("file_name"));
        assertInstanceOf(String.class, generatedTill.get("source"));
        assertEquals(createdAutoPaymentFileName, generatedTill.get("file_name"));
        assertFalse(((String) generatedTill.get("file_name")).isBlank(), "file_name must be populated");
        assertFalse(((String) generatedTill.get("source")).isBlank(), "source must be populated");
    }

    /**
     * Removes the scenario's job and its cascade-related payment and till data through the
     * deployed test-support API. Cleanup must not hide a failure from the scenario itself.
     */
    @After(order = Integer.MAX_VALUE)
    public void cleanUpCreatedAutoPaymentInterfaceJob() {
        if (createdInterfaceJobId == null) {
            return;
        }

        try {
            Response response = authorisedJsonRequest()
                .queryParam("ids", createdInterfaceJobId)
                .when()
                .delete(getTestUrl() + TESTING_SUPPORT_INTERFACE_JOBS_PATH);
            if (response.statusCode() != 200 && response.statusCode() != 204) {
                log.warn("Unable to clean up auto-payment interface job {}: HTTP {}",
                    createdInterfaceJobId, response.statusCode());
            }
        } catch (RuntimeException e) {
            log.warn("Unable to clean up auto-payment interface job {}", createdInterfaceJobId, e);
        }
    }

    private JSONObject createAutoPaymentInterfaceJobRequest() throws JSONException {
        JSONObject record = new JSONObject()
            .put("receiving_sort_code", "123456")
            .put("receiving_bank_account_number", "01234567")
            .put("receiving_account_type", "5")
            .put("transaction_code", "68")
            .put("originator_sort_code", "654321")
            .put("originator_bank_account_number", "98765432")
            .put("amount_pence", "12345")
            .put("originator_name", "PO-2575 Test Payer")
            .put("originator_reference", "PO2575E2E02")
            .put("originator_beneficiary_name", "Test Court");

        return new JSONObject().put("interface_jobs", new JSONArray().put(new JSONObject()
            .put("file_name", createdAutoPaymentFileName)
            .put("source", "NATWEST")
            .put("records", new JSONArray().put(record).toString())
            .put("record_count", 1)
            .put("total_amount", new BigDecimal("123.45"))
            .put("business_unit_id", AUTO_PAYMENT_BUSINESS_UNIT_ID)
            .put("interface_name", AUTO_PAYMENT_INTERFACE_NAME)
            .put("created_datetime", LocalDateTime.now().withNano(0).toString())));
    }

    private void sleepBeforeTillPoll() {
        try {
            Thread.sleep(TILL_PROCESSING_POLL_INTERVAL.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the auto-payment till", e);
        }
    }

    private String getCreatedInterfaceJobStatus() {
        Response response = authorisedJsonRequest()
            .queryParam("business_unit_ids", AUTO_PAYMENT_BUSINESS_UNIT_ID)
            .queryParam("interface_name", AUTO_PAYMENT_INTERFACE_NAME)
            .when()
            .get(getTestUrl() + INTERFACE_JOBS_SUMMARY_PATH);

        if (response.statusCode() != 200) {
            return "summary request returned HTTP " + response.statusCode();
        }

        List<Map<String, Object>> jobs = response.jsonPath().getList("interface_jobs");
        if (jobs == null) {
            return "job was not returned by the summary API";
        }

        return jobs.stream()
            .filter(job -> ((Number) job.get("interface_job_id")).longValue() == createdInterfaceJobId)
            .map(job -> String.valueOf(job.get("status")))
            .findFirst()
            .orElse("job was not returned by the summary API");
    }

    private void assertSuccessfulResponse() {
        then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }
}
