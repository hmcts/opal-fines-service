package uk.gov.hmcts.opal.steps.defendantaccount;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;

import java.util.Locale;

import static net.serenitybdd.rest.SerenityRest.given;
import static net.serenitybdd.rest.SerenityRest.lastResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Defines Cucumber steps for the defendant-account consolidated-accounts endpoint.
 */
public class DefendantAccountConsolidatedAccountsStepDef extends BaseStepDef {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CONSOLIDATED_ACCOUNTS_PATH = "/defendant-accounts/%d/consolidated-accounts";
    private static final long NON_EXISTENT_ACCOUNT_ID = 90_000_000_000_000L;

    private Long lastRequestedAccountId;

    /**
     * Requests consolidated accounts for the supplied defendant account using the current scenario
     * user's bearer token.
     *
     * @param defendantAccountId defendant account identifier to request.
     */
    @When("I request consolidated accounts for defendant account {long}")
    public void requestConsolidatedAccounts(long defendantAccountId) {
        getConsolidatedAccounts(BearerTokenStepDef.getToken(), defendantAccountId);
    }

    /**
     * Requests consolidated accounts for an account id outside the functional-test data range.
     */
    @When("I request consolidated accounts for a non-existent defendant account")
    public void requestConsolidatedAccountsForNonExistentDefendantAccount() {
        getConsolidatedAccounts(BearerTokenStepDef.getToken(), NON_EXISTENT_ACCOUNT_ID);
    }

    /**
     * Requests consolidated accounts without an Authorization header.
     *
     * @param defendantAccountId defendant account identifier to request.
     */
    @When("I request consolidated accounts for defendant account {long} without a token")
    public void requestConsolidatedAccountsWithoutToken(long defendantAccountId) {
        getConsolidatedAccounts(null, defendantAccountId);
    }

    /**
     * Requests consolidated accounts with an invalid bearer token.
     *
     * @param defendantAccountId defendant account identifier to request.
     */
    @When("I request consolidated accounts for defendant account {long} with an invalid token")
    public void requestConsolidatedAccountsWithInvalidToken(long defendantAccountId) {
        getConsolidatedAccounts("invalid-token", defendantAccountId);
    }

    /**
     * Asserts the latest error response follows the shared ProblemDetail shape.
     *
     * @param expectedStatus expected HTTP status code.
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the consolidated accounts error response matches the standard problem detail contract for status {int}")
    public void consolidatedAccountsErrorResponseMatchesProblemDetailContract(int expectedStatus) throws Exception {
        Response response = lastResponse();
        assertEquals(expectedStatus, response.statusCode(), "Unexpected HTTP status");

        JsonNode root = latestProblemDetail();
        assertTrue(root.isObject(), "Problem detail response should be a JSON object");
        assertTrue(root.path("title").isString(), "title should be a string");
        assertTrue(root.path("detail").isString(), "detail should be a string");
        assertTrue(root.path("status").isInt(), "status should be an integer");
        assertEquals(expectedStatus, root.path("status").asInt(), "Unexpected status in response body");

        validateOptionalTextField(root.path("type"), "type");
        validateOptionalTextField(root.path("instance"), "instance");
        validateOptionalTextField(root.path("operation_id"), "operation_id");
    }

    /**
     * Asserts the latest consolidated-accounts error title contains the expected text.
     *
     * @param expectedText expected title text fragment.
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the consolidated accounts error title contains {string}")
    public void consolidatedAccountsErrorTitleContains(String expectedText) throws Exception {
        assertContainsIgnoringCase(latestProblemDetail().path("title").asString(), expectedText, "title");
    }

    /**
     * Asserts the latest consolidated-accounts error detail contains the expected text.
     *
     * @param expectedText expected detail text fragment.
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the consolidated accounts error detail contains {string}")
    public void consolidatedAccountsErrorDetailContains(String expectedText) throws Exception {
        assertContainsIgnoringCase(latestProblemDetail().path("detail").asString(), expectedText, "detail");
    }

    /**
     * Asserts the latest not-found response detail includes the requested account id.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the consolidated accounts error detail contains the requested defendant account id")
    public void consolidatedAccountsErrorDetailContainsRequestedDefendantAccountId() throws Exception {
        assertTrue(lastRequestedAccountId != null, "No requested defendant account id has been captured");
        consolidatedAccountsErrorDetailContains(String.valueOf(lastRequestedAccountId));
    }

    /**
     * Asserts the latest consolidated-accounts error response is explicitly non-retriable.
     *
     * @throws Exception if the response body cannot be parsed as JSON.
     */
    @Then("the consolidated accounts error is non-retriable")
    public void consolidatedAccountsErrorIsNonRetriable() throws Exception {
        JsonNode retriable = problemDetailExtensionField("retriable");
        assertTrue(retriable.isBoolean(), "retriable should be a boolean");
        assertFalse(retriable.asBoolean(), "retriable should be false");
    }

    private Response getConsolidatedAccounts(String token, long defendantAccountId) {
        lastRequestedAccountId = defendantAccountId;
        RequestSpecification request = given()
            .accept("*/*")
            .contentType("application/json");

        if (token != null && !token.isBlank()) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        return request
            .when()
            .get(getTestUrl() + CONSOLIDATED_ACCOUNTS_PATH.formatted(defendantAccountId));
    }

    private JsonNode latestProblemDetail() throws Exception {
        return OBJECT_MAPPER.readTree(lastResponse().getBody().asString());
    }

    private JsonNode problemDetailExtensionField(String fieldName) throws Exception {
        JsonNode root = latestProblemDetail();
        JsonNode directField = root.path(fieldName);
        return directField.isMissingNode() ? root.path("properties").path(fieldName) : directField;
    }

    private void validateOptionalTextField(JsonNode field, String fieldName) {
        if (!field.isMissingNode() && !field.isNull()) {
            assertTrue(field.isString(), fieldName + " should be a string when present");
        }
    }

    private void assertContainsIgnoringCase(String actual, String expected, String fieldName) {
        assertTrue(
            actual.toLowerCase(Locale.ROOT).contains(expected.toLowerCase(Locale.ROOT)),
            fieldName + " should contain '" + expected + "' but was '" + actual + "'"
        );
    }
}
