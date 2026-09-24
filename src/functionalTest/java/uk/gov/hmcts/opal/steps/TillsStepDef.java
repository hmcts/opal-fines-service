package uk.gov.hmcts.opal.steps;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    // TODO(PO-3630): confirm the manual-till cleanup path specified by the TDIA before enabling AC2.
    private static final String TESTING_SUPPORT_TILLS_PATH = "/testing-support/tills/";
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
    private Set<Long> existingTillIds = Set.of();
    private Long createdTillId;
    private String uniquePayerName;

    /**
     * Submits a request that conforms to the current add-till API schema. The request deliberately
     * has no side effects in the feature-disabled scenario that uses this step.
     */
    @When("I submit a valid request to create a till")
    public void submitValidTillCreateRequest() {
        authorisedJsonRequest()
            .body(validTillCreateRequest())
            .when()
            .post(getTestUrl() + TILLS_PATH);
    }

    /**
     * Submits a request that violates one required part of the add-till API contract.
     *
     * @param invalidRequest description of the invalid request selected by the feature scenario
     */
    @When("^I submit a till creation request (.+)$")
    public void submitInvalidTillCreateRequest(String invalidRequest) {
        String requestBody = switch (invalidRequest) {
            case "without its business unit" -> """
                { "payments_in": [] }
                """;
            case "with no payments" -> """
                { "business_unit_id": 78, "payments_in": [] }
                """;
            case "with payment details omitted" -> """
                { "business_unit_id": 78, "payments_in": [ {} ] }
                """;
            default -> throw new IllegalArgumentException("Unsupported invalid till request: " + invalidRequest);
        };

        authorisedJsonRequest()
            .body(requestBody)
            .when()
            .post(getTestUrl() + TILLS_PATH);
    }

    /**
     * Confirms the endpoint rejection is caused by the Release 1C payment feature toggle.
     */
    @Then("the Release 1C payment feature-disabled response is returned")
    public void release1cPaymentFeatureDisabledResponseIsReturned() {
        then()
            .log().ifValidationFails()
            .body("title", equalTo("Feature Disabled"));
    }

    /**
     * Records the current till IDs before creating a manual till. This depends on PO-10713 adding
     * till_id to the GET /tills response.
     *
     * @param businessUnitId business unit in which the manual till will be created
     */
    @Given("I record the existing till identifiers for business unit {int}")
    public void recordExistingTillIdentifiers(int businessUnitId) {
        Response response = authorisedJsonRequest()
            .queryParam("business_unit_ids", businessUnitId)
            .when()
            .get(getTestUrl() + TILLS_PATH);

        assertEquals(200, response.statusCode(), "Listing existing tills must succeed");
        List<Map<String, Object>> tills = response.jsonPath().getList("tills");
        existingTillIds = tills.stream()
            .map(till -> ((Number) till.get("till_id")).longValue())
            .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Submits one uniquely identifiable criminal payment so the newly created till can be found
     * from the post-create till list.
     */
    @When("I submit a uniquely identifiable valid request to create a till with one criminal payment")
    public void submitUniqueTillWithOneCriminalPayment() {
        submitUniqueTillCreateRequest(1);
    }

    /**
     * Submits two uniquely identifiable criminal payments to prove every request payment is
     * persisted on the created till.
     */
    @When("I submit a uniquely identifiable valid request to create a till with two criminal payments")
    public void submitUniqueTillWithTwoCriminalPayments() {
        submitUniqueTillCreateRequest(2);
    }

    /**
     * Finds the sole till ID added since scenario setup, then retrieves its detailed representation.
     */
    @When("I can retrieve the newly created till")
    public void retrieveNewlyCreatedTill() {
        Response listResponse = authorisedJsonRequest()
            .queryParam("business_unit_ids", 78)
            .when()
            .get(getTestUrl() + TILLS_PATH);
        assertEquals(200, listResponse.statusCode(), "Listing created tills must succeed");

        List<Map<String, Object>> tills = listResponse.jsonPath().getList("tills");
        Set<Long> newTillIds = new HashSet<>();
        for (Map<String, Object> till : tills) {
            Long tillId = ((Number) till.get("till_id")).longValue();
            if (!existingTillIds.contains(tillId)) {
                newTillIds.add(tillId);
            }
        }
        assertEquals(1, newTillIds.size(), "Expected exactly one new till");
        createdTillId = newTillIds.iterator().next();

        authorisedJsonRequest()
            .when()
            .get(getTestUrl() + TILLS_PATH + "/" + createdTillId);
    }

    /**
     * Verifies the created till and its single criminal payment through the GET /tills/{id} contract.
     */
    @Then("the till belongs to business unit {int} and contains one linked criminal payment")
    public void tillHasOneLinkedCriminalPayment(int businessUnitId) {
        assertCreatedTill(businessUnitId, 1);
    }

    /**
     * Verifies every submitted criminal payment is linked to the created till.
     *
     * @param businessUnitId business unit expected on the retrieved till
     */
    @Then("the till belongs to business unit {int} and contains two linked criminal payments")
    public void tillHasTwoLinkedCriminalPayments(int businessUnitId) {
        assertCreatedTill(businessUnitId, 2);
    }

    /**
     * Confirms the retrieved payment contains the request fields that are currently part of the
     * add-till API contract.
     */
    @Then("the payment preserves its defendant account, amount, method, destination type, allocation type "
        + "and third-party payer name")
    public void paymentFieldsArePreserved() {
        Map<String, Object> till = assertCreatedTill(78, 1);
        Map<String, Object> payment = createdTillPayments(till).getFirst();

        assertEquals("123", payment.get("associated_record_id"));
        assertEquals(12.34d, ((Number) payment.get("amount")).doubleValue(), 0.001d);
        assertEquals("NC", payment.get("method"));
        assertEquals("F", payment.get("destination_type"));
        assertEquals("FULL", payment.get("allocation_type"));
        assertEquals(uniquePayerName + "-1", payment.get("third_party_payer_name"));
    }

    /**
     * Removes the manual till using the expected TDIA testing-support route. This remains a best
     * guess until that route is implemented and confirmed; AC2 is therefore tagged @Ignore.
     */
    @Then("I remove the created till using the Testing Support API")
    public void removeCreatedTillUsingTestingSupport() {
        deleteCreatedTill();
    }

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

    @After(order = Integer.MAX_VALUE - 1)
    public void cleanUpCreatedManualTill() {
        if (createdTillId == null) {
            return;
        }

        try {
            deleteCreatedTill();
        } catch (RuntimeException exception) {
            log.warn("Unable to clean up manually created till {}", createdTillId, exception);
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
            .put("business_unit_id", AUTO_PAYMENT_BUSINESS_UNIT_ID)
            .put("interface_name", AUTO_PAYMENT_INTERFACE_NAME)
            .put("created_datetime", LocalDateTime.now().withNano(0).toString())));
    }

    private String validTillCreateRequest() {
        return """
            {
              "business_unit_id": 78,
              "payments_in": [
                {
                  "defendant_account_id": 123,
                  "payment_details": {
                    "amount": 12.34,
                    "method": "Notes & Coins",
                    "destination_type": "Fines",
                    "allocation_type": "FULL",
                    "additional_information": "Defendant"
                  }
                }
              ]
            }
            """;
    }

    private void submitUniqueTillCreateRequest(int paymentCount) {
        uniquePayerName = "PO-3630-functional-" + UUID.randomUUID();
        authorisedJsonRequest()
            .body(uniqueTillCreateRequest(paymentCount))
            .when()
            .post(getTestUrl() + TILLS_PATH);
    }

    private String uniqueTillCreateRequest(int paymentCount) {
        String firstPayment = criminalPaymentJson(123, "12.34", uniquePayerName + "-1");
        if (paymentCount == 1) {
            return """
                { "business_unit_id": 78, "payments_in": [ %s ] }
                """.formatted(firstPayment);
        }

        String secondPayment = criminalPaymentJson(124, "23.45", uniquePayerName + "-2");
        return """
            { "business_unit_id": 78, "payments_in": [ %s, %s ] }
            """.formatted(firstPayment, secondPayment);
    }

    private String criminalPaymentJson(long defendantAccountId, String amount, String payerName) {
        return """
            {
              "defendant_account_id": %d,
              "payment_details": {
                "amount": %s,
                "method": "Notes & Coins",
                "destination_type": "Fines",
                "allocation_type": "FULL",
                "additional_information": "Third Party",
                "third_party_payer_name": "%s"
              }
            }
            """.formatted(defendantAccountId, amount, payerName);
    }

    private Map<String, Object> assertCreatedTill(int businessUnitId, int expectedPaymentCount) {
        Map<String, Object> till = then().extract().as(new TypeRef<>() { });
        assertEquals(businessUnitId, ((Number) till.get("business_unit_id")).intValue());
        List<Map<String, Object>> payments = createdTillPayments(till);
        assertNotNull(payments, "Created till must contain payments_in");
        assertEquals(expectedPaymentCount, payments.size());
        return till;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> createdTillPayments(Map<String, Object> till) {
        return (List<Map<String, Object>>) till.get("payments_in");
    }

    private void deleteCreatedTill() {
        assertNotNull(createdTillId, "A till must be created before cleanup");
        Response response = authorisedJsonRequest()
            .when()
            .delete(getTestUrl() + TESTING_SUPPORT_TILLS_PATH + createdTillId);
        assertTrue(response.statusCode() == 200 || response.statusCode() == 204,
            "Manual till cleanup must return 200 or 204");
        createdTillId = null;
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
