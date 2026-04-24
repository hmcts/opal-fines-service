package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountActions;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountRequestFactory;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;

/**
 * Defines Cucumber steps for creating draft accounts.
 */
public class DraftAccountPostSteps extends BaseStepDef {
    private final DraftAccountActions actions = new DraftAccountActions();
    private final DraftAccountRequestFactory requestFactory = new DraftAccountRequestFactory();

    /**
     * Creates a draft account from the values supplied in the scenario table.
     *
     * @param accountData Cucumber table containing the draft-account values for the request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if an account fixture file cannot be read.
     */
    @When("I create a draft account with the following details")
    public void postDraftAccount(DataTable accountData) throws JSONException, IOException {
        actions.createDraftAccount(accountData.asMap(String.class, String.class));
    }

    /**
     * Creates each draft account described by the scenario table and stores the generated
     * identifiers for later steps.
     *
     * @param accountData Cucumber table containing one row per draft account to create.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if an account fixture file cannot be read.
     */
    @When("I create the following draft accounts and store their IDs")
    public void postDraftAccountsAndStoreIds(DataTable accountData) throws JSONException, IOException {
        actions.createDraftAccountsAndStoreIds(accountData.asMaps(String.class, String.class));
    }

    /**
     * Creates a draft account using the raw HTTP client instead of SerenityRest.
     *
     * @param accountData Cucumber table containing the draft-account values for the request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if an account fixture file cannot be read.
     */
    @When("I create a draft account with the following details using a raw HTTP client")
    public void postDraftAccountUsingRawHttpClient(DataTable accountData) throws JSONException, IOException {
        actions.createDraftAccountUsingRawHttpClient(accountData.asMap(String.class, String.class));
    }

    /**
     * Stores the created draft-account ID from the latest response for later steps.
     */
    @Then("I store the created draft account ID")
    public void storeCreatedDraftAccountId() {
        actions.storeCreatedDraftAccountIdFromLastResponse();
    }

    /**
     * Stores the `created_at` timestamp from the latest draft-account response.
     */
    @Then("I store the created draft account created_at time")
    public void storeDraftAccountCreatedTime() {
        String createdAt = then().extract().body().jsonPath().getString("created_at");
        scenarioContext().setDraftAccountCreatedAtTime(createdAt);
    }

    /**
     * Stores the initial `account_status_date` from the latest draft-account response.
     */
    @Then("I store the created draft account initial account_status_date")
    public void storeDraftAccountInitialAccountStatusDate() {
        String initialAccountStatusDate = then().extract().body().jsonPath().getString("account_status_date");
        scenarioContext().setInitialAccountStatusDate(initialAccountStatusDate);
    }

    /**
     * Asserts that the draft account response contains the following data.
     *
     * @param data Cucumber table containing the expected values for the assertion.
     */
    @Then("The draft account response contains the following data")
    public void draftAccountResponseContains(DataTable data) {
        Map<String, String> expectedData = data.asMap(String.class, String.class);

        for (String key : expectedData.keySet()) {
            String expected = expectedData.get(key);
            String actual = then().extract().body().jsonPath().getString(key);
            assertEquals(expected, actual, "Values are not equal for field '" + key + "'");
        }
    }

    /**
     * Asserts that the latest draft-account response returned the expected HTTP status code.
     *
     * @param statusCode expected HTTP status code.
     */
    @Then("The draft account response returns {int}")
    public void draftAccountResponse(int statusCode) {
        var httpResponse = scenarioContext().consumeLatestHttpResponse();
        if (httpResponse != null) {
            assertEquals(statusCode, httpResponse.statusCode(), "Unexpected HTTP status");
            return;
        }

        then().assertThat()
            .statusCode(statusCode);
    }

    /**
     * Attempts to create a draft account using an invalid bearer token.
     *
     * @param createdBy user identifier to place in the `submitted_by` field.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I attempt to create a draft account with an invalid token using created by ID {string}")
    public void postDraftAccountWithInvalidToken(String createdBy) throws JSONException {
        JSONObject postBody;
        try {
            postBody = requestFactory.buildDefaultCreateRequestBody("77", createdBy);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load the default draft-account fixture", e);
        }

        jsonRequestWithToken("invalidToken")
            .body(postBody.toString())
            .when()
            .post(getTestUrl() + DRAFT_ACCOUNTS_URI);

    }

    /**
     * Attempts to create a draft account while requesting an unsupported response content type.
     *
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if an account fixture file cannot be read.
     */
    @When("I attempt to create a draft account with an unsupported content type")
    public void postDraftAccountWithUnsupportedContentType() throws JSONException, IOException {
        JSONObject postBody = requestFactory.buildDefaultCreateRequestBody("77", "BUUID");

        authorisedJsonRequest()
            .accept("text/plain")
            .body(postBody.toString())
            .when()
            .post(getTestUrl() + DRAFT_ACCOUNTS_URI);

    }

    /**
     * Attempts to create a draft account with an unsupported request media type.
     *
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if an account fixture file cannot be read.
     */
    @When("I attempt to create a draft account with an unsupported media type")
    public void postDraftAccountWithUnsupportedMediaType() throws JSONException, IOException {
        JSONObject postBody = requestFactory.buildDefaultCreateRequestBody("77", "BUUID");

        authorisedJsonRequest()
            .contentType("application/xml")
            .body(postBody.toString())
            .when()
            .post(getTestUrl() + DRAFT_ACCOUNTS_URI);

    }

}
