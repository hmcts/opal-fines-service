package uk.gov.hmcts.opal.steps.minorcreditor;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.actions.minorcreditor.MinorCreditorHistoryFixtureActions;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Defines PO-2971 functional steps for the minor-creditor account search endpoint.
 */
public class MinorCreditorAccountSearchRestrictionsStepDef extends BaseStepDef {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SEARCH_URL = "/minor-creditor-accounts/search";
    private static final String SEARCH_TEST_USER = "opal-test@dev.platform.hmcts.net";

    private final MinorCreditorHistoryFixtureActions fixtureActions = new MinorCreditorHistoryFixtureActions();
    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();

    private MinorCreditorSearchFixture createdFixture;

    /**
     * Removes any minor-creditor account search fixture created during the scenario.
     */
    @After("@MinorCreditorSearch")
    public void deleteCreatedMinorCreditorSearchFixture() {
        if (createdFixture == null) {
            return;
        }

        try {
            Response response = fixtureActions.deleteFixture(createdFixture.creditorAccountId());

            assertTrue(
                response.statusCode() == 204 || response.statusCode() == 404,
                "Unexpected minor-creditor search fixture cleanup status: " + response.statusCode()
            );
        } finally {
            createdFixture = null;
        }
    }

    /**
     * Creates a searchable minor-creditor account through the shared history fixture endpoint.
     *
     * @throws JSONException if the fixture request cannot be created.
     */
    @Given("a minor creditor account exists for PO-2971 minor creditor account search")
    public void minorCreditorAccountExistsForPo2971Search() throws JSONException {
        actAsSearchTestUser();

        String fixtureReference = "PO2971" + uniqueToken();
        Response response = fixtureActions.createFixture(fixtureReference);
        responseAssertions.assertStatus(response, 200);

        JsonNode body = readJson(response);
        long creditorAccountId = assertLong(body.path("creditor_account_id"), "creditor_account_id");
        createdFixture = new MinorCreditorSearchFixture(
            creditorAccountId,
            optionalText(body.path("account_number"), "MCH" + creditorAccountId),
            optionalShort(body.path("business_unit_id"), (short) 77),
            optionalText(body.path("forenames"), "History"),
            optionalText(body.path("surname"), fixtureReference)
        );
    }

    /**
     * Calls minor-creditor account search with account number and one creditor field populated.
     *
     * @param conflictingField creditor search field to populate alongside account number.
     * @throws JSONException if the request payload cannot be assembled.
     */
    @When("I search minor creditor accounts with the created account number and {string} populated")
    public void searchWithAccountNumberAndAnotherField(String conflictingField) throws JSONException {
        JSONObject requestBody = defaultSearchRequest()
            .put("account_number", createdFixtureOrFail().accountNumber())
            .put("creditor", creditorFor(conflictingField));

        performSearch(requestBody);
    }

    /**
     * Calls minor-creditor account search with account number only.
     *
     * @throws JSONException if the request payload cannot be assembled.
     */
    @When("I search minor creditor accounts with only the created account number populated")
    public void searchWithOnlyAccountNumber() throws JSONException {
        JSONObject requestBody = defaultSearchRequest()
            .put("account_number", createdFixtureOrFail().accountNumber());

        performSearch(requestBody);
    }

    /**
     * Calls minor-creditor account search with forenames populated and surname omitted.
     *
     * @throws JSONException if the request payload cannot be assembled.
     */
    @When("I search minor creditor accounts with first name and no last name populated")
    public void searchWithFirstNameAndNoLastName() throws JSONException {
        JSONObject requestBody = defaultSearchRequest()
            .put("creditor", individualCreditor().put("forenames", "John"));

        performSearch(requestBody);
    }

    /**
     * Calls minor-creditor account search with both forenames and surname populated.
     *
     * @throws JSONException if the request payload cannot be assembled.
     */
    @When("I search minor creditor accounts with the created first name and last name populated")
    public void searchWithFirstNameAndLastName() throws JSONException {
        MinorCreditorSearchFixture fixture = createdFixtureOrFail();
        JSONObject requestBody = defaultSearchRequest()
            .put("creditor", individualCreditor()
                .put("forenames", fixture.forenames())
                .put("surname", fixture.surname())
                .put("exact_match_forenames", false)
                .put("exact_match_surname", false));

        performSearch(requestBody);
    }

    /**
     * Verifies that the latest minor-creditor account search failure is the expected schema
     * problem.
     */
    @Then("the minor creditor account search request is rejected as a schema bad request")
    public void minorCreditorAccountSearchRequestIsRejectedAsSchemaBadRequest() {
        then()
            .assertThat()
            .statusCode(400)
            .body("title", equalTo("Bad Request"))
            .body("status", equalTo(400))
            .body("detail", equalTo("The request does not conform to the required JSON schema"))
            .body("type", equalTo("https://hmcts.gov.uk/problems/json-schema-validation"))
            .body("retriable", equalTo(false));
    }

    /**
     * Verifies that the latest minor-creditor account search returned the scenario-owned account.
     */
    @Then("the minor creditor account search returns the created account")
    public void minorCreditorAccountSearchReturnsTheCreatedAccount() {
        MinorCreditorSearchFixture fixture = createdFixtureOrFail();

        then()
            .assertThat()
            .statusCode(200)
            .body("creditor_accounts.creditor_account_id", hasItem(String.valueOf(fixture.creditorAccountId())))
            .body("creditor_accounts.account_number", hasItem(fixture.accountNumber()))
            .body("creditor_accounts.firstnames", hasItem(fixture.forenames()))
            .body("creditor_accounts.surname", hasItem(fixture.surname()))
            .body("creditor_accounts.business_unit_id", hasItem(String.valueOf(fixture.businessUnitId())))
            .body("creditor_accounts.creditor_account_id", not(hasItem(containsString("PO2971"))));
    }

    private void performSearch(JSONObject requestBody) {
        authorisedJsonRequest()
            .body(requestBody.toString())
            .when()
            .post(getTestUrl() + SEARCH_URL);
    }

    private JSONObject defaultSearchRequest() throws JSONException {
        MinorCreditorSearchFixture fixture = createdFixtureOrFail();
        return new JSONObject()
            .put("active_accounts_only", true)
            .put("business_unit_ids", new JSONArray().put(fixture.businessUnitId()));
    }

    private JSONObject creditorFor(String conflictingField) throws JSONException {
        return switch (conflictingField) {
            case "address_line_1" -> individualCreditor().put("address_line_1", "1 Test Street");
            case "postcode" -> individualCreditor().put("postcode", "AB1 2CD");
            case "organisation_name" -> organisationCreditor().put("organisation_name", "Test Organisation");
            case "surname" -> individualCreditor()
                .put("surname", "Smith")
                .put("exact_match_surname", false);
            case "forenames_and_surname" -> individualCreditor()
                .put("forenames", "John")
                .put("surname", "Smith")
                .put("exact_match_forenames", false)
                .put("exact_match_surname", false);
            default -> throw new IllegalArgumentException("Unsupported conflicting field: " + conflictingField);
        };
    }

    private JSONObject individualCreditor() throws JSONException {
        return new JSONObject()
            .put("organisation", false);
    }

    private JSONObject organisationCreditor() throws JSONException {
        return new JSONObject()
            .put("organisation", true)
            .put("exact_match_organisation_name", false);
    }

    private void actAsSearchTestUser() {
        BearerTokenStepDef.setTokenOverride(BearerTokenStepDef.getAccessTokenForUser(SEARCH_TEST_USER));
        scenarioContext().setCurrentUser(SEARCH_TEST_USER);
    }

    private MinorCreditorSearchFixture createdFixtureOrFail() {
        assertNotNull(createdFixture, "No minor-creditor search fixture has been created");
        return createdFixture;
    }

    private JsonNode readJson(Response response) {
        try {
            return OBJECT_MAPPER.readTree(response.getBody().asString());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse response body as JSON", ex);
        }
    }

    private long assertLong(JsonNode node, String fieldName) {
        assertTrue(node.isIntegralNumber(), fieldName + " should be an integer");
        return node.longValue();
    }

    private short optionalShort(JsonNode node, short fallback) {
        return node.isIntegralNumber() ? (short) node.intValue() : fallback;
    }

    private String optionalText(JsonNode node, String fallback) {
        return node.isTextual() && !node.asText().isBlank() ? node.asText() : fallback;
    }

    private String uniqueToken() {
        return Long.toString(Math.abs(System.nanoTime()), 36).toUpperCase();
    }

    private record MinorCreditorSearchFixture(
        long creditorAccountId,
        String accountNumber,
        short businessUnitId,
        String forenames,
        String surname
    ) {
    }
}
