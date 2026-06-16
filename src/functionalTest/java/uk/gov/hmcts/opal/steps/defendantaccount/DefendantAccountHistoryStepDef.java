package uk.gov.hmcts.opal.steps.defendantaccount;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;

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
        "Defendant Account not found with id",
        "99999999999999"
    );

    private Response firstResponse;
    private Response secondResponse;

    /**
     * Requests defendant-account history using the current scenario user's bearer token.
     *
     * @param accountId defendant-account identifier to request.
     */
    @When("I request defendant account history for account {long}")
    public void requestDefendantAccountHistory(long accountId) {
        getHistory(BearerTokenStepDef.getToken(), accountId, null);
    }

    /**
     * Requests defendant-account history with a raw query string.
     *
     * @param accountId defendant-account identifier to request.
     * @param query query string to append to the request URI.
     */
    @When("I request defendant account history for account {long} with query {string}")
    public void requestDefendantAccountHistoryWithQuery(long accountId, String query) {
        getHistory(BearerTokenStepDef.getToken(), accountId, query);
    }

    /**
     * Requests defendant-account history twice with the same query string.
     *
     * @param accountId defendant-account identifier to request.
     * @param query query string to append to both request URIs.
     */
    @When("I request defendant account history for account {long} with query {string} twice")
    public void requestDefendantAccountHistoryWithQueryTwice(long accountId, String query) {
        firstResponse = getHistory(BearerTokenStepDef.getToken(), accountId, query);
        secondResponse = getHistory(BearerTokenStepDef.getToken(), accountId, query);
    }

    /**
     * Requests defendant-account history as a specific test user.
     *
     * @param user user email used to resolve a bearer token.
     * @param accountId defendant-account identifier to request.
     */
    @When("the {string} user requests defendant account history for account {long}")
    public void userRequestsDefendantAccountHistory(String user, long accountId) {
        getHistory(BearerTokenStepDef.getAccessTokenForUser(user), accountId, null);
    }

    /**
     * Requests defendant-account history without an Authorization header.
     *
     * @param accountId defendant-account identifier to request.
     */
    @When("I request defendant account history for account {long} without a token")
    public void requestDefendantAccountHistoryWithoutToken(long accountId) {
        getHistory(null, accountId, null);
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
     * Asserts exact item-type counts in the latest history response.
     *
     * @param dataTable expected item type to count mappings.
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the defendant account history contains exactly the following item counts")
    public void defendantAccountHistoryContainsExactlyTheFollowingItemCounts(DataTable dataTable)
        throws Exception {

        Map<String, Long> expectedCounts = new LinkedHashMap<>();
        dataTable.asMap(String.class, String.class)
            .forEach((type, count) -> expectedCounts.put(type, Long.parseLong(count)));

        Map<String, Long> actualCounts = typeCounts();
        assertEquals(expectedCounts, actualCounts, "Unexpected defendant-account history item counts");
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

        assertEquals(1, enforcementItems.size(), "Expected exactly one seeded enforcement history item");
        JsonNode details = enforcementItems.getFirst().path("details");
        assertEquals("FSN", details.path("enforcementAction").asText(), "Unexpected enforcement action");
        assertEquals("Test enforcement", details.path("reason").asText(), "Unexpected enforcement reason");
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
     * Asserts every returned item is on or after the supplied posted date.
     *
     * @param date inclusive lower-bound date.
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the defendant account history response contains only items on or after {string}")
    public void defendantAccountHistoryContainsOnlyItemsOnOrAfter(String date) throws Exception {
        LocalDate boundary = LocalDate.parse(date);
        for (JsonNode historyItem : historyItems()) {
            LocalDate postedDate = postedDateOf(historyItem);
            assertFalse(postedDate.isBefore(boundary), "History item was before dateFrom boundary");
        }
    }

    /**
     * Asserts every returned item is on or before the supplied posted date.
     *
     * @param date inclusive upper-bound date.
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the defendant account history response contains only items on or before {string}")
    public void defendantAccountHistoryContainsOnlyItemsOnOrBefore(String date) throws Exception {
        LocalDate boundary = LocalDate.parse(date);
        for (JsonNode historyItem : historyItems()) {
            LocalDate postedDate = postedDateOf(historyItem);
            assertFalse(postedDate.isAfter(boundary), "History item was after dateTo boundary");
        }
    }

    /**
     * Asserts the latest history response contains at least one item on the supplied posted date.
     *
     * @param date expected posted date.
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the defendant account history response includes an item on {string}")
    public void defendantAccountHistoryResponseIncludesAnItemOn(String date) throws Exception {
        LocalDate expectedDate = LocalDate.parse(date);
        boolean found = historyItems().stream()
            .map(this::postedDateOf)
            .anyMatch(expectedDate::equals);

        assertTrue(found, "Expected at least one history item on " + expectedDate);
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
    }

    private Response getHistory(String token, long accountId, String query) {
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
            case "Amendment" -> {
                assertText(details.path("attributeName"), "details.attributeName");
                assertText(details.path("oldValue"), "details.oldValue");
                assertText(details.path("newValue"), "details.newValue");
            }
            case "Enforcement" -> validateEnforcementDetails(details);
            case "Financial" -> validateFinancialDetails(details);
            case "Note" -> assertText(details.path("noteText"), "details.noteText");
            case "Payment terms" -> validatePaymentTermsDetails(details);
            default -> throw new AssertionError("Unsupported history type: " + type);
        }
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
