package uk.gov.hmcts.opal.steps.defendantaccount;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONException;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.opal.actions.defendantaccount.DefendantAccountEnforcementsActions;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;
import uk.gov.hmcts.opal.workflows.defendantaccount.DefendantAccountEnforcementWorkflow;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.serenitybdd.rest.SerenityRest.given;
import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Defines Cucumber steps for the defendant-account history endpoint.
 */
public class DefendantAccountHistoryStepDef extends BaseStepDef {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String HISTORY_PATH = "/defendant-accounts/%d/history";
    private static final String HISTORY_BUSINESS_UNIT_ID = "77";
    private static final String SEEDED_ENFORCEMENT_OVERRIDE_RESULT_ID = "FWEC";
    private static final String SEEDED_ENFORCER_ID = "770000000001";
    private static final String SEEDED_ENFORCEMENT_ACTION = "NOENF";
    private static final String HISTORY_TEST_USER = "opal-test@dev.platform.hmcts.net";
    private static final String HISTORY_ACCOUNT_FIXTURE = "draftAccounts/accountJson/historyAccount.json";
    private static final String HISTORY_ACCOUNT_TYPE = "Fine";
    private static final String HISTORY_ACCOUNT_STATUS = "Submitted";
    private static final String HISTORY_SUBMITTED_BY_NAME = "Laura Clerk";
    private static final Set<String> HISTORY_TYPES = Set.of(
        "Amendment",
        "Enforcement",
        "Financial",
        "Note",
        "Payment terms"
    );
    private static final List<String> INTERNAL_ERROR_TERMS = List.of(
        "stackTrace",
        "\"trace\"",
        "\"exception\"",
        "jakarta.persistence",
        "org.hibernate",
        "java.lang",
        "uk.gov.hmcts",
        "Defendant Account not found with id"
    );

    private final DefendantAccountEnforcementsActions enforcementActions = new DefendantAccountEnforcementsActions();
    private final DefendantAccountEnforcementWorkflow enforcementWorkflow = new DefendantAccountEnforcementWorkflow();
    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();

    private Response firstResponse;
    private Response secondResponse;
    private LocalDate rememberedDateFrom;
    private LocalDate rememberedDateTo;
    private Long lastRequestedAccountId;

    /**
     * Creates a defendant account with the fixture-backed history data needed by the history
     * scenarios, then adds the amendment history produced by the enforcement override flow.
     *
     * @param submittedBy submitted-by identifier to keep each scenario's setup distinct.
     * @throws JSONException if the setup payload cannot be created.
     * @throws IOException if the supporting account fixture cannot be loaded.
     */
    @Given("a defendant account with history exists for submitted by {string}")
    public void defendantAccountWithHistoryExistsForSubmittedBy(String submittedBy) throws JSONException, IOException {
        actAsHistoryTestUser();
        enforcementWorkflow.createEnforceableDefendantAccount(historyAccountData(submittedBy));
        historyDataExistsForCreatedDefendantAccount();
    }

    /**
     * Adds the history event that is not produced by draft-account publication. The fixture used
     * by the feature already creates notes, payment terms, enforcement history and financial
     * history through the normal publish flow.
     *
     * @throws JSONException if the enforcement override payload cannot be created.
     */
    @Given("history data exists for the created defendant account")
    public void historyDataExistsForCreatedDefendantAccount() throws JSONException {
        Response getResponse = enforcementActions.getCreatedDefendantAccountEnforcementStatus();
        responseAssertions.assertStatus(getResponse, 200);
        assertNotNull(scenarioContext().getDefendantAccountEtag(), "Expected ETag for history setup");

        Response patchResponse = enforcementActions.patchCreatedDefendantAccountEnforcementOverride(Map.of(
            "business_unit_id", HISTORY_BUSINESS_UNIT_ID,
            "enforcement_override_result_id", SEEDED_ENFORCEMENT_OVERRIDE_RESULT_ID,
            "enforcer_id", SEEDED_ENFORCER_ID
        ));

        responseAssertions.assertStatus(patchResponse, 200);
    }

    /**
     * Requests defendant-account history for the account created by the current scenario.
     */
    @When("I request defendant account history for the created defendant account")
    public void requestDefendantAccountHistoryForCreatedDefendantAccount() {
        getHistory(BearerTokenStepDef.getToken(), createdDefendantAccountId(), null);
    }

    /**
     * Requests defendant-account history for the created account with a raw query string.
     *
     * @param query query string to append to the request URI.
     */
    @When("I request defendant account history for the created defendant account with query {string}")
    public void requestCreatedDefendantAccountHistoryWithQuery(String query) {
        getHistory(BearerTokenStepDef.getToken(), createdDefendantAccountId(), query);
    }

    /**
     * Requests defendant-account history using the remembered inclusive lower date boundary.
     */
    @When("I request defendant account history for the created defendant account using the remembered dateFrom "
        + "boundary")
    public void requestCreatedDefendantAccountHistoryUsingRememberedDateFrom() {
        assertRememberedDateRange();
        getHistory(BearerTokenStepDef.getToken(), createdDefendantAccountId(), "dateFrom=" + rememberedDateFrom);
    }

    /**
     * Requests defendant-account history using the remembered inclusive upper date boundary.
     */
    @When("I request defendant account history for the created defendant account using the remembered dateTo boundary")
    public void requestCreatedDefendantAccountHistoryUsingRememberedDateTo() {
        assertRememberedDateRange();
        getHistory(BearerTokenStepDef.getToken(), createdDefendantAccountId(), "dateTo=" + rememberedDateTo);
    }

    /**
     * Requests defendant-account history using the remembered inclusive date range and item-type
     * filter.
     *
     * @param itemTypes comma-separated itemTypes query parameter value.
     */
    @When("I request defendant account history for the created defendant account using the remembered date range "
        + "and itemTypes {string}")
    public void requestCreatedDefendantAccountHistoryUsingRememberedDateRangeAndItemTypes(String itemTypes) {
        getHistory(
            BearerTokenStepDef.getToken(),
            createdDefendantAccountId(),
            rememberedDateRangeQuery(itemTypes)
        );
    }

    /**
     * Requests defendant-account history twice using the remembered inclusive date range and
     * item-type filter.
     *
     * @param itemTypes comma-separated itemTypes query parameter value.
     */
    @When("I request defendant account history for the created defendant account using the remembered date range "
        + "and itemTypes {string} twice")
    public void requestCreatedHistoryUsingRememberedDateRangeAndItemTypesTwice(String itemTypes) {
        String query = rememberedDateRangeQuery(itemTypes);
        firstResponse = getHistory(BearerTokenStepDef.getToken(), createdDefendantAccountId(), query);
        secondResponse = getHistory(BearerTokenStepDef.getToken(), createdDefendantAccountId(), query);
    }

    /**
     * Requests defendant-account history for the created account as a specific test user.
     *
     * @param user user email used to resolve a bearer token.
     */
    @When("the {string} user requests defendant account history for the created defendant account")
    public void userRequestsCreatedDefendantAccountHistory(String user) {
        getHistory(BearerTokenStepDef.getAccessTokenForUser(user), createdDefendantAccountId(), null);
    }

    /**
     * Requests defendant-account history for the created account without an Authorization header.
     */
    @When("I request defendant account history for the created defendant account without a token")
    public void requestCreatedDefendantAccountHistoryWithoutToken() {
        getHistory(null, createdDefendantAccountId(), null);
    }

    /**
     * Requests defendant-account history for an account id generated outside the account-id range
     * used by these tests.
     */
    @When("I request defendant account history for a non-existent defendant account")
    public void requestDefendantAccountHistoryForNonExistentDefendantAccount() {
        getHistory(BearerTokenStepDef.getToken(), nonExistentDefendantAccountId(), null);
    }

    /**
     * Asserts the successful response follows the documented history contract.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the defendant account history response is returned as documented")
    public void defendantAccountHistoryResponseIsReturnedAsDocumented() throws Exception {
        defendantAccountHistoryRequestSucceeds();
        JsonNode root = latestJsonBody();

        assertEquals(Set.of("historyItems"), fieldNames(root), "Unexpected top-level history response fields");
        JsonNode historyItems = root.path("historyItems");
        assertTrue(historyItems.isArray(), "historyItems should be an array");
        assertTrue(historyItems.size() > 0, "historyItems should contain seeded account history");

        for (JsonNode historyItem : historyItems) {
            validateHistoryItem(historyItem);
        }
    }

    /**
     * Asserts the latest defendant-account history request returned HTTP 200.
     */
    @Then("the defendant account history request succeeds")
    public void defendantAccountHistoryRequestSucceeds() {
        then().statusCode(200);
    }

    /**
     * Asserts minimum item-type counts in the latest history response.
     *
     * @param dataTable expected item type to minimum count mappings.
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the defendant account history contains at least the following item counts")
    public void defendantAccountHistoryContainsAtLeastTheFollowingItemCounts(DataTable dataTable)
        throws Exception {

        Map<String, Long> actualCounts = typeCounts();
        dataTable.asMap(String.class, String.class).forEach((type, count) -> {
            long expectedMinimum = Long.parseLong(count);
            long actual = actualCounts.getOrDefault(type, 0L);
            assertTrue(
                actual >= expectedMinimum,
                "Expected at least " + expectedMinimum + " " + type + " item(s), found " + actual
            );
        });
    }

    /**
     * Stores the oldest and newest posted dates from the latest full history response.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("I remember the returned defendant account history date range")
    public void rememberReturnedDefendantAccountHistoryDateRange() throws Exception {
        List<LocalDate> postedDates = historyItems().stream()
            .map(this::postedDateOf)
            .toList();

        assertFalse(postedDates.isEmpty(), "History response should contain dates to remember");
        rememberedDateFrom = postedDates.stream().min(LocalDate::compareTo).orElseThrow();
        rememberedDateTo = postedDates.stream().max(LocalDate::compareTo).orElseThrow();
    }

    /**
     * Asserts the latest history response contains only the supplied item types.
     *
     * @param dataTable expected item type values.
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the defendant account history contains only the following item types")
    public void defendantAccountHistoryContainsOnlyTheFollowingItemTypes(DataTable dataTable) throws Exception {
        Set<String> expectedTypes = new LinkedHashSet<>(dataTable.asList(String.class));
        Set<String> actualTypes = new LinkedHashSet<>();
        for (JsonNode historyItem : historyItems()) {
            actualTypes.add(typeOf(historyItem));
        }

        assertEquals(expectedTypes, actualTypes, "Unexpected defendant-account history item types");
    }

    /**
     * Asserts the seeded enforcement history row is present and schema-compliant.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the defendant account history contains seeded enforcement history")
    public void defendantAccountHistoryContainsSeededEnforcementHistory() throws Exception {
        List<JsonNode> enforcementItems = historyItems().stream()
            .filter(item -> "Enforcement".equals(typeOf(item)))
            .toList();

        assertFalse(enforcementItems.isEmpty(), "Expected seeded enforcement history item");
        boolean containsSeededEnforcement = enforcementItems.stream()
            .map(item -> item.path("details").path("enforcementAction").asText())
            .anyMatch(SEEDED_ENFORCEMENT_ACTION::equals);

        assertTrue(
            containsSeededEnforcement,
            "Expected seeded enforcement action " + SEEDED_ENFORCEMENT_ACTION
        );
    }

    /**
     * Asserts the latest history response contains amendment history.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the defendant account history contains seeded amendment history")
    public void defendantAccountHistoryContainsSeededAmendmentHistory() throws Exception {
        List<JsonNode> amendmentItems = historyItems().stream()
            .filter(item -> "Amendment".equals(typeOf(item)))
            .toList();

        assertFalse(amendmentItems.isEmpty(), "Expected seeded amendment history item");
        amendmentItems.forEach(
            item -> assertText(item.path("details").path("attributeName"), "details.attributeName")
        );
    }

    /**
     * Asserts the latest history response is ordered newest first by posted date.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the defendant account history is ordered newest first")
    public void defendantAccountHistoryIsOrderedNewestFirst() throws Exception {
        LocalDate previous = null;
        for (JsonNode historyItem : historyItems()) {
            LocalDate current = postedDateOf(historyItem);
            if (previous != null) {
                assertFalse(
                    current.isAfter(previous),
                    "History item dates should be ordered newest first: " + current + " after " + previous
                );
            }
            previous = current;
        }
    }

    /**
     * Asserts every returned item is on or after the remembered dateFrom boundary.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the defendant account history response contains only items on or after the remembered dateFrom")
    public void defendantAccountHistoryContainsOnlyItemsOnOrAfterRememberedDateFrom() throws Exception {
        assertRememberedDateRange();
        assertHistoryContainsOnlyItemsOnOrAfter(rememberedDateFrom);
    }

    /**
     * Asserts every returned item is on or before the remembered dateTo boundary.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the defendant account history response contains only items on or before the remembered dateTo")
    public void defendantAccountHistoryContainsOnlyItemsOnOrBeforeRememberedDateTo() throws Exception {
        assertRememberedDateRange();
        assertHistoryContainsOnlyItemsOnOrBefore(rememberedDateTo);
    }

    /**
     * Asserts the latest history response contains at least one item on the remembered dateFrom
     * boundary.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the defendant account history response includes an item on the remembered dateFrom")
    public void defendantAccountHistoryResponseIncludesAnItemOnRememberedDateFrom() throws Exception {
        assertRememberedDateRange();
        assertHistoryIncludesItemOn(rememberedDateFrom);
    }

    /**
     * Asserts the latest history response contains at least one item on the remembered dateTo
     * boundary.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the defendant account history response includes an item on the remembered dateTo")
    public void defendantAccountHistoryResponseIncludesAnItemOnRememberedDateTo() throws Exception {
        assertRememberedDateRange();
        assertHistoryIncludesItemOn(rememberedDateTo);
    }

    /**
     * Asserts repeated identical requests returned identical status, headers and ordered bodies.
     */
    @Then("the repeated defendant account history responses are identical")
    public void repeatedDefendantAccountHistoryResponsesAreIdentical() {
        assertNotNull(firstResponse, "First repeated history response was not captured");
        assertNotNull(secondResponse, "Second repeated history response was not captured");
        assertEquals(200, firstResponse.statusCode(), "Unexpected first repeated request status");
        assertEquals(200, secondResponse.statusCode(), "Unexpected second repeated request status");
        assertEquals(firstResponse.getBody().asString(), secondResponse.getBody().asString());
        assertEquals(firstResponse.getHeader(HttpHeaders.ETAG), secondResponse.getHeader(HttpHeaders.ETAG));
    }

    /**
     * Asserts the latest error response follows the shared ProblemDetail contract.
     *
     * @param expectedStatus expected HTTP status code.
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the defendant account history error response matches the standard problem detail contract for status {int}")
    public void defendantAccountHistoryErrorResponseMatchesStandardProblemDetailContract(int expectedStatus)
        throws Exception {

        Response response = net.serenitybdd.rest.SerenityRest.lastResponse();
        assertEquals(expectedStatus, response.statusCode(), "Unexpected HTTP status");

        JsonNode root = OBJECT_MAPPER.readTree(response.getBody().asString());
        assertTrue(root.isObject(), "Problem detail response should be an object");
        assertText(root.path("title"), "title");
        assertText(root.path("detail"), "detail");
        assertTrue(root.path("status").isInt(), "status should be an integer");
        assertEquals(expectedStatus, root.path("status").asInt(), "Unexpected status in problem detail");

        assertOptionalText(root.path("type"), "type");
        assertOptionalText(root.path("instance"), "instance");
        assertOptionalText(root.path("operation_id"), "operation_id");
        if (!root.path("retriable").isMissingNode() && !root.path("retriable").isNull()) {
            assertTrue(root.path("retriable").isBoolean(), "retriable should be a boolean");
        }
    }

    /**
     * Asserts not-found errors do not expose implementation details.
     */
    @Then("the defendant account history error response does not leak internal details")
    public void defendantAccountHistoryErrorResponseDoesNotLeakInternalDetails() {
        String body = net.serenitybdd.rest.SerenityRest.lastResponse().getBody().asString();
        for (String term : INTERNAL_ERROR_TERMS) {
            assertFalse(body.contains(term), "Error response leaked internal detail: " + term);
        }
        if (lastRequestedAccountId != null) {
            assertFalse(
                body.contains(String.valueOf(lastRequestedAccountId)),
                "Error response leaked requested account id"
            );
        }
    }

    private Response getHistory(String token, long accountId, String query) {
        lastRequestedAccountId = accountId;
        RequestSpecification request = given()
            .accept("*/*")
            .contentType("application/json");

        if (token != null && !token.isBlank()) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        return request
            .when()
            .get(getTestUrl() + HISTORY_PATH.formatted(accountId) + querySuffix(query));
    }

    private String querySuffix(String query) {
        return query == null || query.isBlank() ? "" : "?" + query;
    }

    private long createdDefendantAccountId() {
        return Long.parseLong(scenarioContext().getCreatedDefendantAccountIdOrFail());
    }

    private Map<String, String> historyAccountData(String submittedBy) {
        Map<String, String> accountData = new LinkedHashMap<>();
        accountData.put("business_unit_id", HISTORY_BUSINESS_UNIT_ID);
        accountData.put("account", HISTORY_ACCOUNT_FIXTURE);
        accountData.put("account_type", HISTORY_ACCOUNT_TYPE);
        accountData.put("account_status", HISTORY_ACCOUNT_STATUS);
        accountData.put("submitted_by", submittedBy);
        accountData.put("submitted_by_name", HISTORY_SUBMITTED_BY_NAME);
        return accountData;
    }

    private void actAsHistoryTestUser() {
        BearerTokenStepDef.setTokenOverride(BearerTokenStepDef.getAccessTokenForUser(HISTORY_TEST_USER));
        scenarioContext().setCurrentUser(HISTORY_TEST_USER);
    }

    private long nonExistentDefendantAccountId() {
        return 90_000_000_000_000L + Math.abs(System.nanoTime() % 10_000_000_000L);
    }

    private String rememberedDateRangeQuery(String itemTypes) {
        assertRememberedDateRange();
        return "dateFrom=" + rememberedDateFrom + "&dateTo=" + rememberedDateTo + "&itemTypes=" + itemTypes;
    }

    private void assertRememberedDateRange() {
        assertNotNull(rememberedDateFrom, "No remembered dateFrom boundary");
        assertNotNull(rememberedDateTo, "No remembered dateTo boundary");
    }

    private void assertHistoryContainsOnlyItemsOnOrAfter(LocalDate boundary) throws Exception {
        for (JsonNode historyItem : historyItems()) {
            LocalDate postedDate = postedDateOf(historyItem);
            assertFalse(postedDate.isBefore(boundary), "History item was before dateFrom boundary");
        }
    }

    private void assertHistoryContainsOnlyItemsOnOrBefore(LocalDate boundary) throws Exception {
        for (JsonNode historyItem : historyItems()) {
            LocalDate postedDate = postedDateOf(historyItem);
            assertFalse(postedDate.isAfter(boundary), "History item was after dateTo boundary");
        }
    }

    private void assertHistoryIncludesItemOn(LocalDate expectedDate) throws Exception {
        boolean found = historyItems().stream()
            .map(this::postedDateOf)
            .anyMatch(expectedDate::equals);

        assertTrue(found, "Expected at least one history item on " + expectedDate);
    }

    private JsonNode latestJsonBody() throws Exception {
        return OBJECT_MAPPER.readTree(net.serenitybdd.rest.SerenityRest.lastResponse().getBody().asString());
    }

    private List<JsonNode> historyItems() throws Exception {
        JsonNode historyItems = latestJsonBody().path("historyItems");
        assertTrue(historyItems.isArray(), "historyItems should be an array");

        List<JsonNode> items = new ArrayList<>();
        for (JsonNode item : historyItems) {
            items.add(item);
        }
        return items;
    }

    private Map<String, Long> typeCounts() throws Exception {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (JsonNode historyItem : historyItems()) {
            counts.merge(typeOf(historyItem), 1L, Long::sum);
        }
        return counts;
    }

    private void validateHistoryItem(JsonNode historyItem) {
        assertTrue(historyItem.isObject(), "history item should be an object");

        JsonNode postedDetails = historyItem.path("postedDetails");
        assertTrue(postedDetails.isObject(), "postedDetails should be an object");
        LocalDate.parse(assertText(postedDetails.path("posted_date"), "postedDetails.posted_date"));
        assertOptionalText(postedDetails.path("posted_by"), "postedDetails.posted_by");
        assertOptionalText(postedDetails.path("posted_by_name"), "postedDetails.posted_by_name");

        String type = typeOf(historyItem);
        assertTrue(HISTORY_TYPES.contains(type), "Unexpected history item type: " + type);

        JsonNode details = historyItem.path("details");
        assertTrue(details.isObject(), "details should be an object");
        validateDetails(type, details);

        JsonNode amount = historyItem.path("amount");
        if (!amount.isMissingNode() && !amount.isNull()) {
            assertTrue(amount.isNumber(), "amount should be numeric when present");
        }
    }

    private void validateDetails(String type, JsonNode details) {
        switch (type) {
            case "Amendment" -> validateAmendmentDetails(details);
            case "Enforcement" -> validateEnforcementDetails(details);
            case "Financial" -> validateFinancialDetails(details);
            case "Note" -> assertText(details.path("noteText"), "details.noteText");
            case "Payment terms" -> validatePaymentTermsDetails(details);
            default -> throw new AssertionError("Unsupported history type: " + type);
        }
    }

    private void validateAmendmentDetails(JsonNode details) {
        assertText(details.path("attributeName"), "details.attributeName");
        assertOptionalText(details.path("oldValue"), "details.oldValue");
        assertOptionalText(details.path("newValue"), "details.newValue");
    }

    private void validateEnforcementDetails(JsonNode details) {
        assertText(details.path("enforcementAction"), "details.enforcementAction");
        assertOptionalInteger(details.path("daysInDefault"), "details.daysInDefault");
        assertOptionalText(details.path("warrantNumber"), "details.warrantNumber");
        assertOptionalDate(details.path("hearingDate"), "details.hearingDate");
        assertOptionalObject(details.path("hearingCourt"), "details.hearingCourt");
        assertOptionalText(details.path("caseNumber"), "details.caseNumber");
        assertOptionalText(details.path("reason"), "details.reason");
        assertOptionalDate(details.path("earliestDateOfRelease"), "details.earliestDateOfRelease");
    }

    private void validateFinancialDetails(JsonNode details) {
        JsonNode transactionType = details.path("transactionType");
        assertTrue(transactionType.isObject(), "details.transactionType should be an object");
        assertText(transactionType.path("transactionType"), "details.transactionType.transactionType");
        assertText(
            transactionType.path("transactionTypeDisplayName"),
            "details.transactionType.transactionTypeDisplayName"
        );
        assertOptionalObject(details.path("paymentMethod"), "details.paymentMethod");
        assertOptionalText(details.path("paymentReference"), "details.paymentReference");
        assertOptionalText(details.path("additionalInformation"), "details.additionalInformation");
        assertOptionalObject(details.path("writeOff"), "details.writeOff");
        assertOptionalObject(details.path("status"), "details.status");
        assertOptionalDateTime(details.path("statusDate"), "details.statusDate");
        assertOptionalText(details.path("associatedRecordType"), "details.associatedRecordType");
        assertOptionalText(details.path("associatedRecordId"), "details.associatedRecordId");
        assertOptionalText(details.path("accountNumber"), "details.accountNumber");
        assertOptionalText(details.path("sendingCourt"), "details.sendingCourt");
        assertOptionalDate(details.path("impositionDate"), "details.impositionDate");
        assertOptionalText(details.path("impositionCode"), "details.impositionCode");
        assertOptionalNumber(details.path("amountImposed"), "details.amountImposed");
    }

    private void validatePaymentTermsDetails(JsonNode details) {
        JsonNode paymentTermsType = details.path("payment_terms_type");
        assertTrue(paymentTermsType.isObject(), "details.payment_terms_type should be an object");
        assertText(
            paymentTermsType.path("payment_terms_type_code"),
            "details.payment_terms_type.payment_terms_type_code"
        );
        assertOptionalInteger(details.path("days_in_default"), "details.days_in_default");
        assertOptionalDate(details.path("date_days_in_default_imposed"), "details.date_days_in_default_imposed");
        assertOptionalText(details.path("reason_for_extension"), "details.reason_for_extension");
        assertOptionalDate(details.path("effective_date"), "details.effective_date");
        assertOptionalObject(details.path("instalment_period"), "details.instalment_period");
        assertOptionalNumber(details.path("lump_sum_amount"), "details.lump_sum_amount");
        assertOptionalNumber(details.path("instalment_amount"), "details.instalment_amount");
    }

    private String typeOf(JsonNode historyItem) {
        return assertText(historyItem.path("type"), "type");
    }

    private LocalDate postedDateOf(JsonNode historyItem) {
        return LocalDate.parse(
            assertText(historyItem.path("postedDetails").path("posted_date"), "postedDetails.posted_date")
        );
    }

    private String assertText(JsonNode node, String fieldName) {
        assertTrue(node.isTextual(), fieldName + " should be a string");
        return node.asText();
    }

    private void assertOptionalText(JsonNode node, String fieldName) {
        if (!node.isMissingNode() && !node.isNull()) {
            assertText(node, fieldName);
        }
    }

    private void assertOptionalObject(JsonNode node, String fieldName) {
        if (!node.isMissingNode() && !node.isNull()) {
            assertTrue(node.isObject(), fieldName + " should be an object");
        }
    }

    private void assertOptionalDate(JsonNode node, String fieldName) {
        if (!node.isMissingNode() && !node.isNull()) {
            LocalDate.parse(assertText(node, fieldName));
        }
    }

    private void assertOptionalDateTime(JsonNode node, String fieldName) {
        if (!node.isMissingNode() && !node.isNull()) {
            java.time.LocalDateTime.parse(assertText(node, fieldName));
        }
    }

    private void assertOptionalInteger(JsonNode node, String fieldName) {
        if (!node.isMissingNode() && !node.isNull()) {
            assertTrue(node.isInt(), fieldName + " should be an integer");
        }
    }

    private void assertOptionalNumber(JsonNode node, String fieldName) {
        if (!node.isMissingNode() && !node.isNull()) {
            assertTrue(node.isNumber(), fieldName + " should be numeric");
        }
    }

    private Set<String> fieldNames(JsonNode node) {
        Set<String> names = new LinkedHashSet<>();
        node.properties().forEach(property -> names.add(property.getKey()));
        return names;
    }
}
