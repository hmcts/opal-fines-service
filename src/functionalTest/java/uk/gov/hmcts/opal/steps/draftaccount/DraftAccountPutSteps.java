package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountActions;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountRequestFactory;
import uk.gov.hmcts.opal.assertions.draftaccount.DraftAccountAssertions;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.workflows.draftaccount.DraftAccountReplaceWorkflow;

import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;

/**
 * Defines Cucumber steps for replacing draft accounts.
 */
public class DraftAccountPutSteps extends BaseStepDef {
    private final DraftAccountActions actions = new DraftAccountActions();
    private final DraftAccountAssertions assertions = new DraftAccountAssertions();
    private final DraftAccountRequestFactory requestFactory = new DraftAccountRequestFactory();
    private final DraftAccountReplaceWorkflow workflow = new DraftAccountReplaceWorkflow();

    /**
     * Replaces the draft account created earlier in the scenario with the supplied values.
     *
     * @param data Cucumber table containing the replacement values for the request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if an account fixture file cannot be read.
     */
    @When("I update the draft account that was just created with the following details")
    public void updateCreatedDraftAccount(DataTable data) throws JSONException, IOException {
        actions.replaceCreatedDraftAccount(
            data.asMap(String.class, String.class),
            DraftAccountRequestFactory.BusinessUnitIdMode.INTEGER
        );
    }

    /**
     * Asserts that the most recent replace succeeded and that the updated draft account can be
     * retrieved with the expected values.
     *
     * @param data Cucumber table containing the expected values for the retrieved draft account.
     */
    @Then(
        "the created draft account is replaced successfully "
            + "and the retrieved draft account contains the following data"
    )
    public void createdDraftAccountIsReplacedSuccessfullyAndRetrievedDraftAccountContainsTheFollowingData(
        DataTable data
    ) {
        workflow.assertReplacedDraftAccountCanBeRetrieved(data.asMap(String.class, String.class));
    }

    /**
     * Asserts that the `created_at` timestamp is unchanged after the update.
     */
    @Then("the original creation timestamp is preserved")
    public void checkCreatedAtTime() {
        assertions.assertCreatedAtUnchanged(
            SerenityRest.lastResponse(),
            scenarioContext().getDraftAccountCreatedAtTime()
        );
    }

    /**
     * Asserts that the `account_status_date` moved forward after the update.
     */
    @Then("I see the account status date is now after the initial account status date")
    public void checkAccountStatusDate() {
        assertions.assertAccountStatusDateAfter(
            SerenityRest.lastResponse(),
            scenarioContext().getInitialAccountStatusDate()
        );
    }

    /**
     * Attempts to put a draft-account using an invalid bearer token.
     */
    @When("I attempt to put a draft account with an invalid token")
    public void putADraftAccountWithAnInvalidToken() {
        jsonRequestWithToken("invalidToken")
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI);
    }

    /**
     * Attempts to update the created draft account with a deliberately invalid request payload.
     *
     * @param data Cucumber table supplying the invalid values for the request body.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if an account fixture file cannot be read.
     */
    @When("I attempt to put a draft account with an invalid request payload")
    public void putADraftAccountWithAnInvalidRequestPayload(DataTable data) throws JSONException, IOException {
        actions.replaceCreatedDraftAccount(
            data.asMap(String.class, String.class),
            DraftAccountRequestFactory.BusinessUnitIdMode.LONG
        );
    }

    /**
     * Attempts to update a non-existent draft account to exercise the not-found path.
     *
     * @param data Cucumber table supplying the request values.
     * @throws IOException if an account fixture file cannot be read.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I attempt to put a draft account with resource not found")
    public void putADraftAccountWithResourceNotFound(DataTable data) throws IOException, JSONException {
        actions.replaceDraftAccount(
            "999999",
            data.asMap(String.class, String.class),
            DraftAccountRequestFactory.BusinessUnitIdMode.LONG
        );
    }

    /**
     * Attempts to update the created draft account while requesting an unsupported response
     * content type.
     *
     * @param data Cucumber table supplying the request values.
     * @throws IOException if an account fixture file cannot be read.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I attempt to put a draft account with unsupported content type for response")
    public void putADraftAccountWithUnsupportedContentType(DataTable data) throws IOException, JSONException {
        JSONObject postBody = requestFactory.buildReplaceRequestBody(
            data.asMap(String.class, String.class),
            DraftAccountRequestFactory.BusinessUnitIdMode.LONG
        );
        String draftAccountId = actions.onlyCreatedDraftAccountIdOrFail();
        authorisedJsonRequest()
            .accept("text/html")
            .body(postBody.toString())
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
    }

    /**
     * Attempts to update the created draft account with an unsupported request media type.
     *
     */
    @When("I attempt to put a draft account with unsupported media type for request")
    public void putADraftAccountWithUnsupportedMediaTypeForRequest() {
        String draftAccountId = actions.onlyCreatedDraftAccountIdOrFail();
        authorisedJsonRequest()
            .accept("text/plain")
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
    }

    /**
     * Sends a malformed PUT request to exercise the draft-account internal-server-error path.
     */
    @When("I put the draft account trying to provoke an internal server error")
    public void putDraftAccountToProvokeAnInternalServerError() {
        authorisedJsonRequest()
            .accept("application/json")
            .contentType("application/xml")
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI + "?business_unit=%20");
    }
}
