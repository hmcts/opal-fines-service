package uk.gov.hmcts.opal.steps.majorcreditoraccount;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static net.serenitybdd.rest.SerenityRest.given;
import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Defines Cucumber steps for the major-creditor account history endpoint.
 */
public class MajorCreditorAccountHistoryStepDef extends BaseStepDef {

    private static final String HISTORY_PATH = "/major-creditor-accounts/%d/history";
    private static final long DEFAULT_ACCOUNT_ID = 10770000000041L;
    private static final Set<String> TOP_LEVEL_FIELDS = Set.of("historyItems");
    private static final Set<String> HISTORY_ITEM_FIELDS = Set.of("postedDetails", "type", "details", "amount");
    private static final Set<String> POSTED_DETAILS_FIELDS = Set.of("postedDate", "postedBy", "postedByName");
    private static final Set<String> DETAILS_FIELDS = Set.of(
        "transactionType",
        "paymentReference",
        "status",
        "statusDate",
        "associatedRecordType",
        "associatedRecordId",
        "accountNumber",
        "defendantAccountNumber",
        "defendantAccountId"
    );
    private Response firstResponse;
    private Response secondResponse;
    private LocalDate rememberedDateFrom;
    private LocalDate rememberedDateTo;

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
        getHistory(BearerTokenStepDef.getToken(), 999999L, null);
    }

    /**
     * Asserts the successful response follows the documented contract.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the major creditor account history response is returned as documented")
    public void majorCreditorAccountHistoryResponseIsReturnedAsDocumented() throws Exception {
        then().statusCode(200);
        assertDocumentedShape();
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
        then().statusCode(401);
    }

    /**
     * Asserts the latest major-creditor history request was rejected as forbidden.
     */
    @Then("the major creditor account history request is rejected as forbidden")
    public void majorCreditorAccountHistoryRequestIsRejectedAsForbidden() {
        then().statusCode(403);
    }

    /**
     * Asserts the latest major-creditor history request was rejected as not found.
     */
    @Then("the major creditor account history request is rejected as not found")
    public void majorCreditorAccountHistoryRequestIsRejectedAsNotFound() {
        then().statusCode(404);
    }

    /**
     * Stores the inclusive date range from the latest history response.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("I remember the returned major creditor account history date range")
    public void rememberReturnedMajorCreditorAccountHistoryDateRange() throws Exception {
        List<String> postedDates = then().extract().jsonPath()
            .getList("historyItems.postedDetails.postedDate", String.class);

        if (postedDates == null || postedDates.isEmpty()) {
            rememberedDateFrom = null;
            rememberedDateTo = null;
            return;
        }

        List<LocalDate> dates = postedDates.stream()
            .map(LocalDate::parse)
            .toList();
        rememberedDateFrom = dates.stream().min(LocalDate::compareTo).orElseThrow();
        rememberedDateTo = dates.stream().max(LocalDate::compareTo).orElseThrow();
    }

    /**
     * Asserts the latest response contains only items on or after the remembered lower bound.
     */
    @Then("the major creditor account history response contains only items on or after the remembered dateFrom")
    public void majorCreditorAccountHistoryResponseContainsOnlyItemsOnOrAfterRememberedDateFrom() {
        if (rememberedDateFrom != null) {
            assertDateBoundary("postedDetails.postedDate", rememberedDateFrom, true);
        }
    }

    /**
     * Asserts the latest response contains only items on or before the remembered upper bound.
     */
    @Then("the major creditor account history response contains only items on or before the remembered dateTo")
    public void majorCreditorAccountHistoryResponseContainsOnlyItemsOnOrBeforeRememberedDateTo() {
        if (rememberedDateTo != null) {
            assertDateBoundary("postedDetails.postedDate", rememberedDateTo, false);
        }
    }

    /**
     * Asserts the response includes an item on the remembered lower bound.
     */
    @Then("the major creditor account history response includes an item on the remembered dateFrom")
    public void majorCreditorAccountHistoryResponseIncludesAnItemOnTheRememberedDateFrom() {
        if (rememberedDateFrom != null) {
            assertTrue(
                then().extract().jsonPath().getList("historyItems.postedDetails.postedDate", String.class)
                    .contains(rememberedDateFrom.toString()),
                "Expected an item on the remembered dateFrom"
            );
        }
    }

    /**
     * Asserts the response includes an item on the remembered upper bound.
     */
    @Then("the major creditor account history response includes an item on the remembered dateTo")
    public void majorCreditorAccountHistoryResponseIncludesAnItemOnTheRememberedDateTo() {
        if (rememberedDateTo != null) {
            assertTrue(
                then().extract().jsonPath().getList("historyItems.postedDetails.postedDate", String.class)
                    .contains(rememberedDateTo.toString()),
                "Expected an item on the remembered dateTo"
            );
        }
    }

    /**
     * Asserts the history response only contains the expected type when filtered.
     */
    @Then("the major creditor account history contains only the following item types")
    public void majorCreditorAccountHistoryContainsOnlyTheFollowingItemTypes(
        io.cucumber.datatable.DataTable dataTable) {
        List<String> expectedTypes = dataTable.asList();
        List<String> actualTypes = then().extract().jsonPath().getList("historyItems.type", String.class);
        assertTrue(actualTypes.stream().allMatch(expectedTypes::contains), "Unexpected history item type returned");
    }

    /**
     * Asserts only documented fields are present in the response body.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the major creditor account history response contains only documented fields")
    public void majorCreditorAccountHistoryResponseContainsOnlyDocumentedFields() throws Exception {
        assertDocumentedShape();
    }

    private Response getHistory(String token, long accountId, String query) {
        String url = getTestUrl() + HISTORY_PATH.formatted(accountId) + (query == null ? "" : "?" + query);
        RequestSpecification request = given()
            .accept("*/*")
            .contentType("application/json");
        if (token != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return request.when().get(url);
    }

    private void assertDocumentedShape() throws Exception {
        assertEquals(Set.of("historyItems"), then().extract().jsonPath().getMap("").keySet());
        List<java.util.Map<String, Object>> rawItems = then().extract().jsonPath().getList("historyItems");
        if (rawItems == null) {
            return;
        }
        for (java.util.Map<String, Object> item : rawItems) {
            assertTrue(HISTORY_ITEM_FIELDS.containsAll(item.keySet()), "Unexpected history item field returned");
            assertTrue(item.keySet().containsAll(Set.of("postedDetails", "type", "details")),
                "Missing required history item field");
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> postedDetails = (java.util.Map<String, Object>) item.get("postedDetails");
            assertTrue(POSTED_DETAILS_FIELDS.containsAll(postedDetails.keySet()),
                "Unexpected posted details field returned");
            assertTrue(postedDetails.keySet().containsAll(POSTED_DETAILS_FIELDS),
                "Missing required posted details field");
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> details = (java.util.Map<String, Object>) item.get("details");
            assertTrue(DETAILS_FIELDS.containsAll(details.keySet()), "Unexpected detail field returned");
            assertTrue(details.keySet().contains("transactionType"), "Missing required transactionType field");
        }
    }

    private void assertDateBoundary(String jsonPathSuffix, LocalDate boundary, boolean lowerBound) {
        List<String> dates = then().extract().jsonPath().getList("historyItems." + jsonPathSuffix, String.class);
        assertFalse(dates.isEmpty(), "Expected history items in the response");
        for (String date : dates) {
            LocalDate actual = LocalDate.parse(date);
            if (lowerBound) {
                assertTrue(!actual.isBefore(boundary), "Expected dates on or after " + boundary);
            } else {
                assertTrue(!actual.isAfter(boundary), "Expected dates on or before " + boundary);
            }
        }
    }
}
