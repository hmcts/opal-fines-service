package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.io.IOException;
import java.util.Map;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountActions;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountRequestFactory;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.workflows.draftaccount.DraftAccountCreateWorkflow;
import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;

/**
 * Defines Cucumber steps for creating draft accounts.
 */
public class DraftAccountPostSteps extends BaseStepDef {
    private final DraftAccountActions actions = new DraftAccountActions();
    private final DraftAccountRequestFactory requestFactory = new DraftAccountRequestFactory();
    private final DraftAccountCreateWorkflow workflow = new DraftAccountCreateWorkflow();
    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();

    /**
     * Creates a draft account as scenario setup, asserts that creation succeeded, and stores the
     * generated draft-account ID for later steps.
     *
     * @param accountData Cucumber table containing the draft-account values for the request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if an account fixture file cannot be read.
     */
    @Given("a draft account exists with the following details")
    public void givenDraftAccountExists(DataTable accountData) throws JSONException, IOException {
        workflow.createDraftAccountAndStoreId(accountData.asMap(String.class, String.class));
    }

    /**
     * Creates a draft account as scenario setup, stores the generated ID, and remembers the
     * timestamps required for later replace-flow assertions.
     *
     * @param accountData Cucumber table containing the draft-account values for the request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if an account fixture file cannot be read.
     */
    @Given("a replaceable draft account exists with the following details")
    public void givenReplaceableDraftAccountExists(DataTable accountData) throws JSONException, IOException {
        workflow.createDraftAccountAndPrepareForReplacement(accountData.asMap(String.class, String.class));
    }

    /**
     * Creates a draft account and immediately replaces it so later list scenarios can work with a
     * persisted resubmitted record without exposing the setup workflow in the feature.
     *
     * @param accountData Cucumber table containing the final draft-account values after
     *                    replacement.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if an account fixture file cannot be read.
     */
    @Given("a resubmitted draft account exists with the following details")
    public void givenResubmittedDraftAccountExists(DataTable accountData) throws JSONException, IOException {
        Map<String, String> replacementData =
            new java.util.LinkedHashMap<>(accountData.asMap(String.class, String.class));
        workflow.createDraftAccountAndPrepareForReplacement(replacementData);
        replacementData.put("If-Match", "0");
        actions.replaceCreatedDraftAccount(replacementData, DraftAccountRequestFactory.BusinessUnitIdMode.INTEGER);
        responseAssertions.assertStatus(SerenityRest.lastResponse(), 200);
    }

    /**
     * Creates a draft account from the values supplied in the scenario table.
     *
     * @param accountData Cucumber table containing the draft-account values for the request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if an account fixture file cannot be read.
     */
    @When("I create a draft account with the following details")
    public void postDraftAccount(DataTable accountData) throws JSONException, IOException {
        Response response = actions.createDraftAccount(accountData.asMap(String.class, String.class));
        rememberCreatedDraftAccountState(response);
    }

    /**
     * Creates each draft account described by the scenario table and stores the generated
     * identifiers for later steps.
     *
     * @param accountData Cucumber table containing one row per draft account to create.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if an account fixture file cannot be read.
     */
    @Given("the following draft accounts exist")
    public void givenDraftAccountsExist(DataTable accountData) throws JSONException, IOException {
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
     * Asserts that the draft account response contains the following data.
     *
     * @param data Cucumber table containing the expected values for the assertion.
     */
    @Then("The draft account response contains the following data")
    public void draftAccountResponseContains(DataTable data) {
        responseAssertions.assertResponseContains(then().extract().response(), data.asMap(String.class, String.class));
    }

    /**
     * Asserts that the most recent create response represents a successful draft-account creation,
     * including the expected body fields, ETag header, and response-shape checks.
     *
     * @param data Cucumber table containing the expected values for the create response.
     */
    @Then("the draft account is created successfully with the following data")
    public void draftAccountIsCreatedSuccessfully(DataTable data) {
        workflow.assertLastCreateSucceeded(data.asMap(String.class, String.class));
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
     * Attempts to create a draft account using a user without permission.
     *
     * @param createdBy user identifier to place in the `submitted_by` field.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I attempt to create a draft account with an unauthorised user {string}")
    public void postDraftAccountWithUnauthorisedUSer(String createdBy) throws JSONException {

        JSONObject postBody;
        try {
            postBody = requestFactory.buildDefaultCreateRequestBody("77", createdBy);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load the default draft-account fixture", e);
        }

        loggedAuthorisedJsonRequest()
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

    /**
     * Stores the created draft-account state needed by later steps when creation succeeded.
     *
     * @param response response returned by the create request.
     */
    private void rememberCreatedDraftAccountState(Response response) {
        if (response.statusCode() != 201) {
            return;
        }

        actions.storeCreatedDraftAccountId(response);
        actions.storeDraftAccountCreatedAtTime(response);
        actions.storeInitialAccountStatusDate(response);
    }

}
