package uk.gov.hmcts.opal.steps.defendantaccount;

import static net.serenitybdd.rest.SerenityRest.lastResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import static net.serenitybdd.rest.SerenityRest.given;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;
import uk.gov.hmcts.opal.workflows.defendantaccount.DefendantAccountEnforcementWorkflow;

/**
 * Defines Cucumber steps for the defendant-account header-summary endpoint.
 */
public class DefendantAccountHeaderSummaryStepDef extends BaseStepDef {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String BUSINESS_UNIT_ID = "77";
    private static final String SUBMITTED_BY_NAME = "Laura Clerk";
    private static final String HEADER_SUMMARY_PATH = "/defendant-accounts/%d/header-summary";
    private static final long NON_EXISTENT_ACCOUNT_ID = 90_000_000_000_000L;
    private static final List<String> INTERNAL_ERROR_TERMS = List.of(
        "stackTrace",
        "\"trace\"",
        "\"exception\"",
        "jakarta.persistence",
        "org.hibernate",
        "java.lang",
        "uk.gov.hmcts"
    );

    private final DefendantAccountEnforcementWorkflow enforcementWorkflow =
        new DefendantAccountEnforcementWorkflow();

    @Given("a defendant account with header summary data exists for submitted by {string} using fixture {string}")
    public void defendantAccountWithHeaderSummaryDataExistsForSubmittedByUsingFixture(
        String submittedBy,
        String accountFixture
    ) throws Exception {
        actAs(BearerTokenStepDef.DEFAULT_USER);
        enforcementWorkflow.createEnforceableDefendantAccount(headerSummaryAccountData(submittedBy, accountFixture));
    }

    @When("I request defendant account header summary for the created defendant account")
    public void requestDefendantAccountHeaderSummaryForTheCreatedDefendantAccount() {
        getHeaderSummary(BearerTokenStepDef.getToken(), createdDefendantAccountId());
    }

    @When("I request defendant account header summary for the created defendant account without a token")
    public void requestDefendantAccountHeaderSummaryForTheCreatedDefendantAccountWithoutAToken() {
        getHeaderSummary(null, createdDefendantAccountId());
    }

    @When("I request defendant account header summary for the created defendant account with an invalid token")
    public void requestDefendantAccountHeaderSummaryForTheCreatedDefendantAccountWithAnInvalidToken() {
        getHeaderSummary("invalid-token", createdDefendantAccountId());
    }

    @When("the {string} user requests defendant account header summary for the created defendant account")
    public void userRequestsDefendantAccountHeaderSummaryForTheCreatedDefendantAccount(String user) {
        getHeaderSummary(BearerTokenStepDef.getAccessTokenForUser(user), createdDefendantAccountId());
    }

    @When("I request defendant account header summary for a non-existent defendant account")
    public void requestDefendantAccountHeaderSummaryForANonExistentDefendantAccount() {
        getHeaderSummary(BearerTokenStepDef.getToken(), NON_EXISTENT_ACCOUNT_ID);
    }

    @Then("the defendant account header summary response is returned as documented")
    public void defendantAccountHeaderSummaryResponseIsReturnedAsDocumented() throws Exception {
        Response response = lastResponse();
        assertEquals(200, response.statusCode(), "Unexpected header-summary status");

        JsonNode root = OBJECT_MAPPER.readTree(response.getBody().asString());
        assertTrue(root.isObject(), "header-summary response should be a JSON object");
        assertEquals(createdDefendantAccountId(), root.path("defendant_account_id").asLong(),
            "Unexpected defendant_account_id");
        assertTrue(root.path("account_number").isTextual(), "account_number should be present");
        assertTrue(root.path("account_status_reference").isObject(), "account_status_reference should be present");
        assertTrue(root.path("account_type").isTextual(), "account_type should be present");
        assertTrue(root.path("business_unit_summary").isObject(), "business_unit_summary should be present");
        assertTrue(root.path("defendant_account_party_id").isTextual(),
            "defendant_account_party_id should be present");
        assertTrue(root.path("is_youth").isBoolean(), "is_youth should be present");
        assertTrue(root.path("has_consolidated_accounts").isBoolean(),
            "has_consolidated_accounts should be present");
        assertTrue(root.path("debtor_type").isTextual(), "debtor_type should be present");
        assertTrue(root.path("payment_state_summary").isObject(), "payment_state_summary should be present");
        assertTrue(root.path("party_details").isObject(), "party_details should be present");
        assertTrue(root.path("prosecutor_case_reference").isTextual(),
            "prosecutor_case_reference should be present");
        assertTrue(root.path("originator_type").isTextual(), "originator_type should be present");
        assertTrue(root.path("originator_name").isTextual(), "originator_name should be present");
        assertTrue(root.path("collection_order").isBoolean(), "collection_order should be present");
    }

    @Then("the defendant account header summary contains originator type {string}")
    public void defendantAccountHeaderSummaryContainsOriginatorType(String expectedOriginatorType) {
        assertEquals(expectedOriginatorType, lastResponse().jsonPath().getString("originator_type"));
    }

    @Then("the defendant account header summary contains originator name {string}")
    public void defendantAccountHeaderSummaryContainsOriginatorName(String expectedOriginatorName) {
        assertEquals(expectedOriginatorName, lastResponse().jsonPath().getString("originator_name"));
    }

    @Then("the defendant account header summary contains collection order {string}")
    public void defendantAccountHeaderSummaryContainsCollectionOrder(String expectedCollectionOrder) {
        assertEquals(Boolean.parseBoolean(expectedCollectionOrder),
            lastResponse().jsonPath().getBoolean("collection_order"));
    }

    @Then("the defendant account header summary contains party details organisation flag {string}")
    public void defendantAccountHeaderSummaryContainsPartyDetailsOrganisationFlag(String expectedOrganisationFlag) {
        assertEquals(Boolean.parseBoolean(expectedOrganisationFlag),
            lastResponse().jsonPath().getBoolean("party_details.organisation_flag"));
    }

    @Then("the defendant account header summary contains party details forenames {string} surname {string}")
    public void defendantAccountHeaderSummaryContainsPartyDetailsForenamesSurname(
        String expectedForenames,
        String expectedSurname
    ) {
        String actualForenames = lastResponse().jsonPath()
            .getString("party_details.individual_details.forenames");
        String actualSurname = lastResponse().jsonPath().getString("party_details.individual_details.surname");
        assertEquals(expectedForenames, actualForenames);
        assertEquals(expectedSurname, actualSurname);
    }

    @Then("the defendant account header summary contains the expected live values")
    public void defendantAccountHeaderSummaryContainsTheExpectedLiveValues(io.cucumber.datatable.DataTable dataTable)
        throws Exception {

        Map<String, String> expectedValues = dataTable.asMap(String.class, String.class);
        JsonNode root = OBJECT_MAPPER.readTree(lastResponse().getBody().asString());

        for (Map.Entry<String, String> entry : expectedValues.entrySet()) {
            JsonNode actual = root.path(entry.getKey());
            if (actual.isMissingNode() || actual.isNull()) {
                throw new AssertionError("Expected field to be present: " + entry.getKey());
            }
            if ("collection_order".equals(entry.getKey())) {
                assertEquals(Boolean.parseBoolean(entry.getValue()), actual.asBoolean(),
                    "Unexpected value for collection_order");
            } else {
                assertEquals(entry.getValue(), actual.asText(), "Unexpected value for " + entry.getKey());
            }
        }
    }

    @Then("the defendant account header summary error response matches the standard problem detail contract "
        + "for status {int}")
    public void defendantAccountHeaderSummaryErrorResponseMatchesTheStandardProblemDetailContractForStatus(
        int expectedStatus
    ) throws Exception {
        Response response = lastResponse();
        assertEquals(expectedStatus, response.statusCode(), "Unexpected HTTP status");

        JsonNode root = OBJECT_MAPPER.readTree(response.getBody().asString());
        assertTrue(root.isObject(), "Problem detail response should be an object");
        assertTrue(root.path("title").isTextual(), "title should be a string");
        assertTrue(root.path("detail").isTextual(), "detail should be a string");
        assertTrue(root.path("status").isInt(), "status should be an integer");
        assertEquals(expectedStatus, root.path("status").asInt(), "Unexpected status in problem detail");

        assertOptionalText(root.path("type"), "type");
        assertOptionalText(root.path("instance"), "instance");
        assertOptionalText(root.path("operation_id"), "operation_id");
        if (!root.path("retriable").isMissingNode() && !root.path("retriable").isNull()) {
            assertTrue(root.path("retriable").isBoolean(), "retriable should be a boolean");
        }
    }

    @Then("the defendant account header summary error title contains {string}")
    public void defendantAccountHeaderSummaryErrorTitleContains(String expectedText) {
        assertContainsIgnoringCase(lastResponse().jsonPath().getString("title"), expectedText, "title");
    }

    @Then("the defendant account header summary error detail contains {string}")
    public void defendantAccountHeaderSummaryErrorDetailContains(String expectedText) {
        assertContainsIgnoringCase(lastResponse().jsonPath().getString("detail"), expectedText, "detail");
    }

    @Then("the defendant account header summary error is non-retriable")
    public void defendantAccountHeaderSummaryErrorIsNonRetriable() {
        JsonNode root;
        try {
            root = OBJECT_MAPPER.readTree(lastResponse().getBody().asString());
        } catch (Exception exception) {
            throw new AssertionError("Failed to parse problem detail response", exception);
        }
        JsonNode field = root.path("retriable");
        if (field.isMissingNode() || field.isNull()) {
            field = root.path("properties").path("retriable");
        }
        assertTrue(field.isBoolean(), "retriable should be a boolean");
        assertFalse(field.asBoolean(), "retriable should be false");
    }

    @Then("the defendant account header summary error response does not leak internal details")
    public void defendantAccountHeaderSummaryErrorResponseDoesNotLeakInternalDetails() {
        String body = lastResponse().getBody().asString();
        for (String term : INTERNAL_ERROR_TERMS) {
            assertFalse(body.contains(term), "Error response leaked internal detail: " + term);
        }
        assertFalse(body.contains(String.valueOf(NON_EXISTENT_ACCOUNT_ID)),
            "Error response leaked requested account id");
    }

    private Response getHeaderSummary(String token, long defendantAccountId) {
        RequestSpecification request = given()
            .accept("*/*")
            .contentType("application/json");

        if (token != null && !token.isBlank()) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        return request
            .when()
            .get(getTestUrl() + HEADER_SUMMARY_PATH.formatted(defendantAccountId));
    }

    private Map<String, String> headerSummaryAccountData(String submittedBy, String accountFixture) {
        Map<String, String> accountData = new LinkedHashMap<>();
        accountData.put("business_unit_id", BUSINESS_UNIT_ID);
        accountData.put("account", accountFixture);
        accountData.put("account_type", "Fine");
        accountData.put("account_status", "Submitted");
        accountData.put("submitted_by", submittedBy);
        accountData.put("submitted_by_name", SUBMITTED_BY_NAME);
        return accountData;
    }

    private long createdDefendantAccountId() {
        return Long.parseLong(scenarioContext().getCreatedDefendantAccountIdOrFail());
    }

    private void actAs(String user) {
        BearerTokenStepDef.setTokenOverride(BearerTokenStepDef.getAccessTokenForUser(user));
        scenarioContext().setCurrentUser(user);
    }

    private void assertOptionalText(JsonNode field, String fieldName) {
        if (!field.isMissingNode() && !field.isNull()) {
            assertTrue(field.isTextual(), fieldName + " should be a string when present");
        }
    }

    private void assertContainsIgnoringCase(String actual, String expected, String fieldName) {
        assertTrue(
            actual.toLowerCase().contains(expected.toLowerCase()),
            fieldName + " should contain '" + expected + "' but was '" + actual + "'"
        );
    }
}
