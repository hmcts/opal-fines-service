package uk.gov.hmcts.opal.steps.majorcreditoraccount;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Defines Cucumber steps for the major-creditor account history endpoint.
 */
public class MajorCreditorAccountHistoryStepDef extends BaseStepDef {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String HISTORY_PATH = "/major-creditor-accounts/%d/history";
    private static final long DEFAULT_ACCOUNT_ID = 10770000000041L;
    private static final String HISTORY_RESPONSE_SCHEMA =
        "opal/major-creditor/getMajorCreditorHistoryResponse.json";
    private static final Set<String> HISTORY_TYPES = Set.of("Financial", "Note");

    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();
    private final JsonSchemaValidationService schemaValidationService = new JsonSchemaValidationService();

    private Response firstResponse;
    private Response secondResponse;
    private LocalDate rememberedDateFrom;
    private LocalDate rememberedDateTo;
    private Long lastRequestedAccountId;

    /**
     * Requests major-creditor history for the seeded account using the current scenario token.
     */
    @When("I request major creditor account history for the created major creditor account")
    public void requestMajorCreditorAccountHistoryForCreatedMajorCreditorAccount() {
        getHistory(BearerTokenStepDef.getToken(), DEFAULT_ACCOUNT_ID, null);
    }

    /**
     * Requests major-creditor history for the seeded account twice using the current token.
     */
    @When("I request major creditor account history for the created major creditor account twice")
    public void requestMajorCreditorAccountHistoryForCreatedMajorCreditorAccountTwice() {
        firstResponse = getHistory(BearerTokenStepDef.getToken(), DEFAULT_ACCOUNT_ID, null);
        secondResponse = getHistory(BearerTokenStepDef.getToken(), DEFAULT_ACCOUNT_ID, null);
    }

    /**
     * Requests major-creditor history for the seeded account using a specific test user.
     *
     * @param user user email used to resolve a bearer token.
     */
    @When("the {string} user requests major creditor account history for the created major creditor account")
    public void userRequestsMajorCreditorAccountHistoryForCreatedMajorCreditorAccount(String user) {
        getHistory(BearerTokenStepDef.getAccessTokenForUser(user), DEFAULT_ACCOUNT_ID, null);
    }

    /**
     * Requests major-creditor history for the seeded account without an Authorization header.
     */
    @When("I request major creditor account history for the created major creditor account without a token")
    public void requestMajorCreditorAccountHistoryForCreatedMajorCreditorAccountWithoutToken() {
        getHistory(null, DEFAULT_ACCOUNT_ID, null);
    }

    /**
     * Requests major-creditor history for the seeded account with an invalid Authorization header.
     */
    @When("I request major creditor account history for the created major creditor account with an invalid token")
    public void requestMajorCreditorAccountHistoryForCreatedMajorCreditorAccountWithInvalidToken() {
        getHistory("invalid-token", DEFAULT_ACCOUNT_ID, null);
    }

    /**
     * Requests major-creditor history for the seeded account with a specific query string.
     *
     * @param query query string to append to the request URI.
     */
    @When("I request major creditor account history for the created major creditor account with query {string}")
    public void requestMajorCreditorAccountHistoryForCreatedMajorCreditorAccountWithQuery(String query) {
        getHistory(BearerTokenStepDef.getToken(), DEFAULT_ACCOUNT_ID, query);
    }

    /**
     * Requests major-creditor history for a non-existent account.
     */
    @When("I request major creditor account history for a non-existent major creditor account")
    public void requestMajorCreditorAccountHistoryForNonExistentMajorCreditorAccount() {
        getHistory(BearerTokenStepDef.getToken(), nonExistentMajorCreditorAccountId(), null);
    }

    /**
     * Asserts the successful response follows the documented contract.
     */
    @Then("the major creditor account history response is returned as documented")
    public void majorCreditorAccountHistoryResponseIsReturnedAsDocumented() {
        Response response = net.serenitybdd.rest.SerenityRest.lastResponse();
        responseAssertions.assertStatus(response, 200);
        schemaValidationService.validateOrError(response.getBody().asString(), HISTORY_RESPONSE_SCHEMA);

        JsonNode root = latestJsonBody();
        assertEquals(Set.of("historyItems"), fieldNames(root), "Unexpected top-level history response fields");
        JsonNode historyItems = root.path("historyItems");
        assertTrue(historyItems.isArray(), "historyItems should be an array");

        for (JsonNode historyItem : historyItems) {
            validateHistoryItem(historyItem);
        }
    }

    /**
     * Asserts the repeated major-creditor history responses are identical.
     */
    @Then("the repeated major creditor account history responses are identical")
    public void repeatedMajorCreditorAccountHistoryResponsesAreIdentical() {
        assertEquals(200, firstResponse.statusCode());
        assertEquals(200, secondResponse.statusCode());
        assertEquals(firstResponse.getBody().asString(), secondResponse.getBody().asString());
        assertEquals(firstResponse.getHeader(HttpHeaders.ETAG), secondResponse.getHeader(HttpHeaders.ETAG));
    }

    /**
     * Asserts the latest major-creditor history request was rejected as unauthorized.
     */
    @Then("the major creditor account history request is rejected as unauthorized")
    public void majorCreditorAccountHistoryRequestIsRejectedAsUnauthorized() {
        responseAssertions.assertStatus(net.serenitybdd.rest.SerenityRest.lastResponse(), 401);
    }

    /**
     * Asserts the latest major-creditor history request was rejected as forbidden.
     */
    @Then("the major creditor account history request is rejected as forbidden")
    public void majorCreditorAccountHistoryRequestIsRejectedAsForbidden() {
        responseAssertions.assertStatus(net.serenitybdd.rest.SerenityRest.lastResponse(), 403);
    }

    /**
     * Asserts the latest major-creditor history request was rejected as not found.
     */
    @Then("the major creditor account history request is rejected as not found")
    public void majorCreditorAccountHistoryRequestIsRejectedAsNotFound() {
        responseAssertions.assertStatus(net.serenitybdd.rest.SerenityRest.lastResponse(), 404);
    }

    /**
     * Stores the inclusive date range from the latest history response.
     */
    @Then("I remember the returned major creditor account history date range")
    public void rememberReturnedMajorCreditorAccountHistoryDateRange() {
        List<LocalDate> dates = postedDates();

        if (dates.isEmpty()) {
            rememberedDateFrom = null;
            rememberedDateTo = null;
            return;
        }

        rememberedDateFrom = dates.stream().min(LocalDate::compareTo).orElseThrow();
        rememberedDateTo = dates.stream().max(LocalDate::compareTo).orElseThrow();
    }

    /**
     * Asserts the latest response contains only items on or after the remembered lower bound.
     */
    @Then("the major creditor account history response contains only items on or after the remembered dateFrom")
    public void majorCreditorAccountHistoryResponseContainsOnlyItemsOnOrAfterRememberedDateFrom() {
        if (rememberedDateFrom != null) {
            for (JsonNode historyItem : historyItems()) {
                assertFalse(
                    postedDateOf(historyItem).isBefore(rememberedDateFrom),
                    "History item was before dateFrom boundary"
                );
            }
        }
    }

    /**
     * Asserts the latest response contains only items on or before the remembered upper bound.
     */
    @Then("the major creditor account history response contains only items on or before the remembered dateTo")
    public void majorCreditorAccountHistoryResponseContainsOnlyItemsOnOrBeforeRememberedDateTo() {
        if (rememberedDateTo != null) {
            for (JsonNode historyItem : historyItems()) {
                assertFalse(
                    postedDateOf(historyItem).isAfter(rememberedDateTo),
                    "History item was after dateTo boundary"
                );
            }
        }
    }

    /**
     * Asserts the response includes an item on the remembered lower bound.
     */
    @Then("the major creditor account history response includes an item on the remembered dateFrom")
    public void majorCreditorAccountHistoryResponseIncludesAnItemOnTheRememberedDateFrom() {
        if (rememberedDateFrom != null) {
            assertTrue(postedDates().contains(rememberedDateFrom), "Expected an item on the remembered dateFrom");
        }
    }

    /**
     * Asserts the response includes an item on the remembered upper bound.
     */
    @Then("the major creditor account history response includes an item on the remembered dateTo")
    public void majorCreditorAccountHistoryResponseIncludesAnItemOnTheRememberedDateTo() {
        if (rememberedDateTo != null) {
            assertTrue(postedDates().contains(rememberedDateTo), "Expected an item on the remembered dateTo");
        }
    }

    /**
     * Asserts the history response only contains the expected type when filtered.
     */
    @Then("the major creditor account history contains only the following item types")
    public void majorCreditorAccountHistoryContainsOnlyTheFollowingItemTypes(DataTable dataTable) {
        Set<String> expectedTypes = new LinkedHashSet<>(dataTable.asList(String.class));
        List<String> actualTypes = new ArrayList<>();
        for (JsonNode historyItem : historyItems()) {
            actualTypes.add(typeOf(historyItem));
        }

        assertTrue(actualTypes.stream().allMatch(expectedTypes::contains), "Unexpected history item type returned");
    }

    /**
     * Asserts the latest error response follows the shared ProblemDetail contract.
     *
     * @param expectedStatus expected HTTP status code.
     */
    @Then("the major creditor account history error response matches the standard problem detail contract for status "
        + "{int}")
    public void majorCreditorHistoryErrorResponseMatchesStandardProblemDetailContract(int expectedStatus) {

        Response response = net.serenitybdd.rest.SerenityRest.lastResponse();
        assertEquals(expectedStatus, response.statusCode(), "Unexpected HTTP status");

        JsonNode root = latestJsonBody();
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
     */
    @Then("the major creditor account history error response contains no account data")
    public void majorCreditorHistoryErrorResponseContainsNoAccountData() {
        JsonNode root = latestJsonBody();
        assertTrue(root.path("historyItems").isMissingNode(), "Error response should not include historyItems");
        if (lastRequestedAccountId != null) {
            assertFalse(
                net.serenitybdd.rest.SerenityRest.lastResponse().getBody().asString()
                    .contains(String.valueOf(lastRequestedAccountId)),
                "Error response leaked the creditor account id"
            );
        }
    }

    /**
     * Asserts only documented fields are present in the response body.
     *
     */
    @Then("the major creditor account history response contains only documented fields")
    public void majorCreditorAccountHistoryResponseContainsOnlyDocumentedFields() {
        schemaValidationService.validateOrError(
            net.serenitybdd.rest.SerenityRest.lastResponse().getBody().asString(),
            HISTORY_RESPONSE_SCHEMA
        );
    }

    private Response getHistory(String token, long accountId, String query) {
        lastRequestedAccountId = accountId;
        String url = getTestUrl() + HISTORY_PATH.formatted(accountId) + (query == null ? "" : "?" + query);
        RequestSpecification request = given()
            .accept("*/*")
            .contentType("application/json");
        if (token != null && !token.isBlank()) {
            request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return request.when().get(url);
    }

    private long nonExistentMajorCreditorAccountId() {
        return 92_000_000_000_000L + Math.abs(System.nanoTime() % 10_000_000_000L);
    }

    private JsonNode latestJsonBody() {
        return OBJECT_MAPPER.readTree(net.serenitybdd.rest.SerenityRest.lastResponse().getBody().asString());
    }

    private List<JsonNode> historyItems() {
        JsonNode historyItems = latestJsonBody().path("historyItems");
        assertTrue(historyItems.isArray(), "historyItems should be an array");

        List<JsonNode> items = new ArrayList<>();
        for (JsonNode item : historyItems) {
            items.add(item);
        }
        return items;
    }

    private List<LocalDate> postedDates() {
        return historyItems().stream()
            .map(this::postedDateOf)
            .toList();
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

    private String assertText(JsonNode node, String fieldName) {
        assertTrue(node.isString(), fieldName + " should be a string");
        return node.asString();
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
