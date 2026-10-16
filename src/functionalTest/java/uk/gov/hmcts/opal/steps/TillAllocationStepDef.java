package uk.gov.hmcts.opal.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import java.util.ArrayList;
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

/**
 * Defines functional coverage for the allocate-tills endpoint.
 */
public class TillAllocationStepDef extends BaseStepDef {

    private static final Logger log = LoggerFactory.getLogger(TillAllocationStepDef.class);
    private static final String TILLS_PATH = "/tills";
    private static final String ALLOCATE_TILLS_PATH = "/tills/allocate";
    // Assumed Testing Support route; enable cleanup when the agreed route is deployed.
    private static final String TESTING_SUPPORT_TILLS_PATH = "/testing-support/tills/";
    private static final short BUSINESS_UNIT_ID = 78;
    private static final long NON_EXISTENT_TILL_ID = Long.MAX_VALUE;

    private final Set<Long> existingTillIds = new HashSet<>();
    private final List<Long> createdTillIds = new ArrayList<>();
    private final List<Long> createdTillNumbers = new ArrayList<>();
    private Response allocationResponse;

    /**
     * Creates one or more identifiable manual tills and records their IDs for allocation and cleanup.
     *
     * @param count number of tills to create
     * @param businessUnitId business unit in which the tills are created
     */
    @Given("I create {int} uniquely identifiable till(s) for allocation in business unit {int}")
    public void createTillsForAllocation(int count, int businessUnitId) {
        assertTrue(count > 0, "At least one till is required for allocation");
        assertEquals(BUSINESS_UNIT_ID, businessUnitId,
            "The allocation test user and manual-till fixture are configured for business unit 78");
        recordExistingTillIds();

        for (int index = 0; index < count; index++) {
            Response createResponse = authorisedJsonRequest()
                .body(uniqueTillCreateRequest())
                .when()
                .post(getTestUrl() + TILLS_PATH);
            assertEquals(201, createResponse.statusCode(), "Creating a till for allocation must succeed");
            captureNewlyCreatedTill();
        }
    }

    /**
     * Confirms the created till records are visible through the requested status filter.
     * The public summary response does not include status, so this uses its documented status filter.
     *
     * @param status till status to filter by
     */
    @Then("all created tills are listed with status {string}")
    public void allCreatedTillsAreListedWithStatus(String status) {
        assertFalse(createdTillNumbers.isEmpty(), "Create tills before checking their status");

        Response response = authorisedJsonRequest()
            .queryParam("business_unit_ids", BUSINESS_UNIT_ID)
            .queryParam("statuses", status)
            .when()
            .get(getTestUrl() + TILLS_PATH);
        assertEquals(200, response.statusCode(), "Filtering tills by status must succeed");

        List<Map<String, Object>> tills = response.jsonPath().getList("tills");
        assertNotNull(tills, "The filtered tills response must contain a tills list");
        List<Long> returnedTillNumbers = tills.stream()
            .map(till -> till.get("till_number"))
            .filter(Number.class::isInstance)
            .map(Number.class::cast)
            .map(Number::longValue)
            .toList();
        assertTrue(new HashSet<>(returnedTillNumbers).containsAll(createdTillNumbers),
            "Expected till numbers " + createdTillNumbers + " under status " + status);
    }

    /**
     * Submits every created till in one allocation request.
     */
    @When("I submit the created tills for allocation")
    public void submitCreatedTillsForAllocation() throws JSONException {
        assertFalse(createdTillIds.isEmpty(), "Create tills before submitting an allocation request");
        JSONArray tills = new JSONArray();
        for (Long tillId : createdTillIds) {
            tills.put(new JSONObject()
                .put("till_id", tillId)
                .put("business_unit_id", BUSINESS_UNIT_ID));
        }

        allocationResponse = authorisedJsonRequest()
            .body(new JSONObject().put("tills", tills).toString())
            .when()
            .post(getTestUrl() + ALLOCATE_TILLS_PATH);
    }

    /**
     * Submits a bulk request whose final till has an incorrect business-unit ID.
     */
    @When("I submit the created tills for allocation with a mismatched business unit")
    public void submitCreatedTillsWithMismatchedBusinessUnit() throws JSONException {
        assertTrue(createdTillIds.size() >= 2, "Create at least two tills before testing a mismatched business unit");
        JSONArray tills = new JSONArray();
        for (int index = 0; index < createdTillIds.size(); index++) {
            short businessUnitId = index == createdTillIds.size() - 1 ? 77 : BUSINESS_UNIT_ID;
            tills.put(new JSONObject()
                .put("till_id", createdTillIds.get(index))
                .put("business_unit_id", businessUnitId));
        }

        authorisedJsonRequest()
            .body(new JSONObject().put("tills", tills).toString())
            .when()
            .post(getTestUrl() + ALLOCATE_TILLS_PATH);
    }

    /**
     * Confirms the endpoint returned its documented bodyless 200 response.
     */
    @Then("till allocation succeeds with no response body")
    public void tillAllocationSucceedsWithNoResponseBody() {
        assertNotNull(allocationResponse, "Submit an allocation request first");
        assertEquals(200, allocationResponse.statusCode(), "Till allocation must succeed");
        assertTrue(allocationResponse.asString().isEmpty(), "The successful allocation response must be bodyless");
    }

    /**
     * Deletes all tills created by the current scenario using the Testing Support API.
     */
    @Then("I remove all created tills using the Testing Support API")
    public void removeAllCreatedTillsUsingTestingSupport() {
        for (Long tillId : List.copyOf(createdTillIds)) {
            deleteTill(tillId);
        }
        createdTillIds.clear();
        createdTillNumbers.clear();
    }

    /**
     * Submits one malformed body selected by the API-schema examples in the feature.
     *
     * @param invalidRequest description of the malformed request
     */
    @When("I submit an invalid till allocation request {string}")
    public void submitInvalidTillAllocationRequest(String invalidRequest) {
        String requestBody = switch (invalidRequest) {
            case "without tills" -> "{}";
            case "with an empty tills list" -> "{ \"tills\": [] }";
            case "with a till missing its ID" -> "{ \"tills\": [{ \"business_unit_id\": 78 }] }";
            case "with a till missing its business unit" -> "{ \"tills\": [{ \"till_id\": 1 }] }";
            case "with a null till entry" -> "{ \"tills\": [null] }";
            default -> throw new IllegalArgumentException(
                "Unsupported invalid till allocation request: " + invalidRequest);
        };

        authorisedJsonRequest()
            .body(requestBody)
            .when()
            .post(getTestUrl() + ALLOCATE_TILLS_PATH);
    }

    /**
     * Submits an allocation request as an authenticated user without payment-processing permission.
     */
    @When("I submit a till allocation request as a user without permission")
    public void submitTillAllocationRequestWithoutPermission() throws JSONException {
        submitAllocationRequest();
    }

    /**
     * Requests allocation of an ID that cannot refer to a test till.
     */
    @When("I submit an allocation request for a till that does not exist")
    public void submitAllocationRequestForMissingTill() throws JSONException {
        submitAllocationRequest();
    }

    /**
     * Submits a validly shaped request to confirm the Release 1C toggle is applied to this endpoint.
     */
    @When("I submit a till allocation request while Release 1C payments are disabled")
    public void submitTillAllocationRequestWithFeatureDisabled() throws JSONException {
        submitAllocationRequest();
    }

    /**
     * Removes any created tills left behind by an interrupted or failed scenario.
     */
    @After(order = Integer.MAX_VALUE - 1)
    public void cleanUpCreatedTills() {
        for (Long tillId : List.copyOf(createdTillIds)) {
            try {
                deleteTill(tillId);
            } catch (RuntimeException exception) {
                log.warn("Unable to clean up manual till {}", tillId, exception);
            }
        }
    }

    private void recordExistingTillIds() {
        Response response = authorisedJsonRequest()
            .queryParam("business_unit_ids", BUSINESS_UNIT_ID)
            .when()
            .get(getTestUrl() + TILLS_PATH);
        assertEquals(200, response.statusCode(), "Listing existing tills must succeed");
        List<Map<String, Object>> tills = response.jsonPath().getList("tills");
        assertNotNull(tills, "The existing tills response must contain a tills list");
        tills.stream()
            .map(till -> ((Number) till.get("till_id")).longValue())
            .forEach(existingTillIds::add);
    }

    private void captureNewlyCreatedTill() {
        Response response = authorisedJsonRequest()
            .queryParam("business_unit_ids", BUSINESS_UNIT_ID)
            .when()
            .get(getTestUrl() + TILLS_PATH);
        assertEquals(200, response.statusCode(), "Listing newly created tills must succeed");

        List<Map<String, Object>> tills = response.jsonPath().getList("tills");
        assertNotNull(tills, "The newly created tills response must contain a tills list");
        Set<Long> newTillIds = new HashSet<>();
        for (Map<String, Object> till : tills) {
            Long tillId = ((Number) till.get("till_id")).longValue();
            if (!existingTillIds.contains(tillId) && !createdTillIds.contains(tillId)) {
                newTillIds.add(tillId);
            }
        }
        assertEquals(1, newTillIds.size(), "Expected exactly one new till from the last create request");

        Long tillId = newTillIds.iterator().next();
        createdTillIds.add(tillId);
        Response detailResponse = authorisedJsonRequest()
            .when()
            .get(getTestUrl() + TILLS_PATH + "/" + tillId);
        assertEquals(200, detailResponse.statusCode(), "Retrieving the created till must succeed");
        Map<String, Object> till = detailResponse.as(new TypeRef<>() { });
        Object tillNumber = till.get("till_number");
        Number createdTillNumber = assertInstanceOf(Number.class, tillNumber,
            "The created till response must contain its till number");
        createdTillNumbers.add(createdTillNumber.longValue());
    }

    private String uniqueTillCreateRequest() {
        String payerName = "PO-3423-functional-" + UUID.randomUUID();
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
                    "additional_information": "Third Party",
                    "third_party_payer_name": "%s"
                  }
                }
              ]
            }
            """.formatted(payerName);
    }

    private void submitAllocationRequest() throws JSONException {
        authorisedJsonRequest()
            .body(new JSONObject().put("tills", new JSONArray().put(new JSONObject()
                .put("till_id", NON_EXISTENT_TILL_ID)
                .put("business_unit_id", BUSINESS_UNIT_ID))).toString())
            .when()
            .post(getTestUrl() + ALLOCATE_TILLS_PATH);
    }

    private void deleteTill(long tillId) {
        Response response = authorisedJsonRequest()
            .when()
            .delete(getTestUrl() + TESTING_SUPPORT_TILLS_PATH + tillId);
        assertTrue(response.statusCode() == 200 || response.statusCode() == 204,
            "Testing Support must remove till " + tillId);
        createdTillIds.remove(tillId);
    }
}
