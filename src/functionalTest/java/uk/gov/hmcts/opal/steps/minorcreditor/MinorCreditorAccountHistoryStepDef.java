package uk.gov.hmcts.opal.steps.minorcreditor;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Defines Cucumber steps for the minor-creditor account history endpoint.
 */
public class MinorCreditorAccountHistoryStepDef extends BaseStepDef {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String HISTORY_PATH = "/minor-creditor-accounts/%d/history";
    private static final String HISTORY_FIXTURE_PATH = "/testing-support/minor-creditor-history";
    private static final String HISTORY_TEST_USER = "opal-test@dev.platform.hmcts.net";
    private static final String HISTORY_RESPONSE_SCHEMA =
        "opal/minor-creditor/getMinorCreditorHistoryResponse.json";
    private static final Set<String> HISTORY_TYPES = Set.of("Amendment", "Financial", "Note");

    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();
    private final JsonSchemaValidationService schemaValidationService = new JsonSchemaValidationService();

    private Long createdMinorCreditorAccountId;
    private LocalDate rememberedDateFrom;
    private LocalDate rememberedDateTo;
    private LocalDate excludedDate;

    /**
     * Removes any minor-creditor account history fixture created during the scenario.
     */
    @After("@MinorCreditorHistory")
    public void deleteCreatedMinorCreditorHistoryFixture() {
        if (createdMinorCreditorAccountId == null) {
            return;
        }

        try {
            Response response = given()
                .accept("*/*")
                .when()
                .delete(getTestUrl() + HISTORY_FIXTURE_PATH + "/" + createdMinorCreditorAccountId);

            assertTrue(
                response.statusCode() == 204 || response.statusCode() == 404,
                "Unexpected minor-creditor history fixture cleanup status: " + response.statusCode()
            );
        } finally {
            createdMinorCreditorAccountId = null;
        }
    }

    /**
     * Creates a self-contained minor-creditor account with representative history through the
     * functional-test support API.
     *
     * @param submittedBy scenario identifier used in visible fixture data.
     * @throws JSONException if the fixture request cannot be created.
     */
    @Given("a minor creditor account with representative history exists for submitted by {string}")
    public void minorCreditorAccountWithRepresentativeHistoryExists(String submittedBy) throws JSONException {
        actAsHistoryTestUser();

        Response response = given()
            .accept("*/*")
            .contentType("application/json")
            .body(new JSONObject().put("reference", submittedBy).toString())
            .when()
            .post(getTestUrl() + HISTORY_FIXTURE_PATH);

        responseAssertions.assertStatus(response, 200);

        JsonNode body = readJson(response);
        createdMinorCreditorAccountId = assertLong(body.path("creditor_account_id"), "creditor_account_id");
        rememberedDateFrom = LocalDate.parse(assertText(body.path("date_from"), "date_from"));
        rememberedDateTo = LocalDate.parse(assertText(body.path("date_to"), "date_to"));
        excludedDate = LocalDate.parse(assertText(body.path("excluded_date"), "excluded_date"));
    }

    /**
     * Requests history for the minor creditor created during the current scenario.
     */
    @When("I request minor creditor account history for the created minor creditor account")
    public void requestMinorCreditorHistoryForCreatedMinorCreditorAccount() {
        getHistory(BearerTokenStepDef.getToken(), createdMinorCreditorAccountIdOrFail(), null);
    }

    /**
     * Requests history for the created minor creditor with a remembered date range and item-types
     * filter.
     *
     * @param itemTypes comma-separated itemTypes query parameter value.
     */
    @When("I request minor creditor account history for the created minor creditor account using the remembered "
        + "date range and itemTypes {string}")
    public void requestCreatedMinorCreditorHistoryUsingRememberedDateRangeAndItemTypes(String itemTypes) {
        assertRememberedDateRange();
        getHistory(
            BearerTokenStepDef.getToken(),
            createdMinorCreditorAccountIdOrFail(),
            "dateFrom=" + rememberedDateFrom + "&dateTo=" + rememberedDateTo + "&itemTypes=" + itemTypes
        );
    }

    /**
     * Requests history for the created minor creditor as a specific test user.
     *
     * @param user user email used to resolve a bearer token.
     */
    @When("the {string} user requests minor creditor account history for the created minor creditor account")
    public void userRequestsMinorCreditorHistoryForCreatedMinorCreditorAccount(String user) {
        getHistory(BearerTokenStepDef.getAccessTokenForUser(user), createdMinorCreditorAccountIdOrFail(), null);
    }

    /**
     * Requests history for a generated non-existent minor creditor account.
     */
    @When("I request minor creditor account history for a non-existent minor creditor account")
    public void requestMinorCreditorHistoryForNonExistentMinorCreditorAccount() {
        getHistory(BearerTokenStepDef.getToken(), nonExistentMinorCreditorAccountId(), null);
    }

    /**
     * Requests history for a generated non-existent minor creditor account without a token.
     */
    @When("I request minor creditor account history for a non-existent minor creditor account without a token")
    public void requestMinorCreditorHistoryForNonExistentMinorCreditorAccountWithoutToken() {
        getHistory(null, nonExistentMinorCreditorAccountId(), null);
    }

    /**
     * Requests history for a generated non-existent minor creditor account with an invalid token.
     */
    @When("I request minor creditor account history for a non-existent minor creditor account with an invalid token")
    public void requestMinorCreditorHistoryForNonExistentMinorCreditorAccountWithInvalidToken() {
        getHistory("invalid-token", nonExistentMinorCreditorAccountId(), null);
    }

    /**
     * Asserts the successful response follows the documented history contract and local JSON
     * schema resource derived from the OpenAPI history schema.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the minor creditor account history response is returned as documented")
    public void minorCreditorHistoryResponseIsReturnedAsDocumented() throws Exception {
        Response response = net.serenitybdd.rest.SerenityRest.lastResponse();
        responseAssertions.assertStatus(response, 200);
        schemaValidationService.validateOrError(response.getBody().asString(), HISTORY_RESPONSE_SCHEMA);

        JsonNode root = latestJsonBody();
        assertEquals(Set.of("historyItems"), fieldNames(root), "Unexpected top-level history response fields");
        JsonNode historyItems = root.path("historyItems");
        assertTrue(historyItems.isArray(), "historyItems should be an array");
        assertTrue(historyItems.size() > 0, "historyItems should contain created account history");

        for (JsonNode historyItem : historyItems) {
            validateHistoryItem(historyItem);
        }
    }

    /**
     * Asserts minimum item-type counts in the latest history response.
     *
     * @param dataTable expected item type to minimum count mappings.
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the minor creditor account history contains at least the following item counts")
    public void minorCreditorHistoryContainsAtLeastTheFollowingItemCounts(DataTable dataTable) throws Exception {
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
     * Stores the oldest and newest posted dates from the latest history response.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("I remember the returned minor creditor account history date range")
    public void rememberReturnedMinorCreditorHistoryDateRange() throws Exception {
        List<LocalDate> postedDates = historyItems().stream()
            .map(this::postedDateOf)
            .toList();

        assertFalse(postedDates.isEmpty(), "History response should contain dates to remember");
        rememberedDateFrom = postedDates.stream().min(LocalDate::compareTo).orElseThrow();
        rememberedDateTo = postedDates.stream().max(LocalDate::compareTo).orElseThrow();
    }

    /**
     * Asserts the latest history response is ordered newest first by posted date.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the minor creditor account history is ordered newest first")
    public void minorCreditorHistoryIsOrderedNewestFirst() throws Exception {
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
     * Asserts the unfiltered response includes fixture data outside the remembered filter range.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the minor creditor account history includes records outside the remembered date range")
    public void minorCreditorHistoryIncludesRecordsOutsideRememberedDateRange() throws Exception {
        assertExcludedDate();

        assertTrue(
            historyItems().stream().map(this::postedDateOf).anyMatch(excludedDate::equals),
            "Expected unfiltered history to contain records outside the remembered date range"
        );
    }

    /**
     * Asserts the filtered response excludes fixture data outside the remembered filter range.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the minor creditor account history excludes records outside the remembered date range")
    public void minorCreditorHistoryExcludesRecordsOutsideRememberedDateRange() throws Exception {
        assertExcludedDate();

        for (JsonNode historyItem : historyItems()) {
            assertFalse(
                excludedDate.equals(postedDateOf(historyItem)),
                "History item outside the requested date range was returned"
            );
        }
    }

    /**
     * Asserts the latest history response contains only the supplied item types.
     *
     * @param dataTable expected item type values.
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the minor creditor account history contains only the following item types")
    public void minorCreditorHistoryContainsOnlyTheFollowingItemTypes(DataTable dataTable) throws Exception {
        Set<String> expectedTypes = new LinkedHashSet<>(dataTable.asList(String.class));
        Set<String> actualTypes = new LinkedHashSet<>();
        for (JsonNode historyItem : historyItems()) {
            actualTypes.add(typeOf(historyItem));
        }

        assertEquals(expectedTypes, actualTypes, "Unexpected minor-creditor history item types");
    }

    /**
     * Asserts the latest history response excludes the supplied item types.
     *
     * @param dataTable item type values that must not be returned.
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the minor creditor account history excludes the following item types")
    public void minorCreditorHistoryExcludesTheFollowingItemTypes(DataTable dataTable) throws Exception {
        Set<String> excludedTypes = new LinkedHashSet<>(dataTable.asList(String.class));
        for (JsonNode historyItem : historyItems()) {
            assertFalse(
                excludedTypes.contains(typeOf(historyItem)),
                "Excluded minor-creditor history item type was returned"
            );
        }
    }

    /**
     * Asserts every returned item is on or after the remembered dateFrom boundary.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the minor creditor account history response contains only items on or after the remembered dateFrom")
    public void minorCreditorHistoryContainsOnlyItemsOnOrAfterRememberedDateFrom() throws Exception {
        assertRememberedDateRange();
        for (JsonNode historyItem : historyItems()) {
            assertFalse(
                postedDateOf(historyItem).isBefore(rememberedDateFrom),
                "History item was before dateFrom boundary"
            );
        }
    }

    /**
     * Asserts every returned item is on or before the remembered dateTo boundary.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the minor creditor account history response contains only items on or before the remembered dateTo")
    public void minorCreditorHistoryContainsOnlyItemsOnOrBeforeRememberedDateTo() throws Exception {
        assertRememberedDateRange();
        for (JsonNode historyItem : historyItems()) {
            assertFalse(
                postedDateOf(historyItem).isAfter(rememberedDateTo),
                "History item was after dateTo boundary"
            );
        }
    }

    /**
     * Asserts the latest error response follows the shared ProblemDetail contract.
     *
     * @param expectedStatus expected HTTP status code.
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the minor creditor account history error response matches the standard problem detail contract for "
        + "status {int}")
    public void minorCreditorHistoryErrorResponseMatchesStandardProblemDetailContract(int expectedStatus)
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
     * Asserts error responses do not contain account history data.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the minor creditor account history error response contains no account data")
    public void minorCreditorHistoryErrorResponseContainsNoAccountData() throws Exception {
        JsonNode root = latestJsonBody();
        assertTrue(root.path("historyItems").isMissingNode(), "Error response should not include historyItems");
        if (createdMinorCreditorAccountId != null) {
            assertFalse(
                net.serenitybdd.rest.SerenityRest.lastResponse().getBody().asString()
                    .contains(String.valueOf(createdMinorCreditorAccountId)),
                "Error response leaked the creditor account id"
            );
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

    private long createdMinorCreditorAccountIdOrFail() {
        assertNotNull(createdMinorCreditorAccountId, "No created minor creditor account ID found");
        return createdMinorCreditorAccountId;
    }

    private void actAsHistoryTestUser() {
        BearerTokenStepDef.setTokenOverride(BearerTokenStepDef.getAccessTokenForUser(HISTORY_TEST_USER));
        scenarioContext().setCurrentUser(HISTORY_TEST_USER);
    }

    private long nonExistentMinorCreditorAccountId() {
        return 91_000_000_000_000L + Math.abs(System.nanoTime() % 10_000_000_000L);
    }

    private JsonNode latestJsonBody() throws Exception {
        return OBJECT_MAPPER.readTree(net.serenitybdd.rest.SerenityRest.lastResponse().getBody().asString());
    }

    private JsonNode readJson(Response response) {
        try {
            return OBJECT_MAPPER.readTree(response.getBody().asString());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse response body as JSON", ex);
        }
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

        JsonNode amount = historyItem.path("amount");
        if (!amount.isMissingNode() && !amount.isNull()) {
            assertTrue(amount.isNumber(), "amount should be numeric when present");
        }
    }

    private String typeOf(JsonNode historyItem) {
        return assertText(historyItem.path("type"), "type");
    }

    private LocalDate postedDateOf(JsonNode historyItem) {
        return LocalDate.parse(
            assertText(historyItem.path("postedDetails").path("posted_date"), "postedDetails.posted_date")
        );
    }

    private void assertRememberedDateRange() {
        assertNotNull(rememberedDateFrom, "No remembered dateFrom boundary");
        assertNotNull(rememberedDateTo, "No remembered dateTo boundary");
    }

    private void assertExcludedDate() {
        assertNotNull(excludedDate, "No excluded history fixture date found");
    }

    private String assertText(JsonNode node, String fieldName) {
        assertTrue(node.isTextual(), fieldName + " should be a string");
        return node.asText();
    }

    private Long assertLong(JsonNode node, String fieldName) {
        assertTrue(node.isIntegralNumber(), fieldName + " should be an integer");
        return node.asLong();
    }

    private void assertOptionalText(JsonNode node, String fieldName) {
        if (!node.isMissingNode() && !node.isNull()) {
            assertText(node, fieldName);
        }
    }

    private Set<String> fieldNames(JsonNode node) {
        Set<String> names = new LinkedHashSet<>();
        node.properties().forEach(property -> names.add(property.getKey()));
        return names;
    }
}
