package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountActions;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.workflows.draftaccount.DraftAccountPatchWorkflow;

/**
 * Defines Cucumber steps for patching draft accounts.
 */
public class DraftAccountPatchSteps extends BaseStepDef {
    private final DraftAccountActions actions = new DraftAccountActions();
    private final DraftAccountPatchWorkflow workflow = new DraftAccountPatchWorkflow();

    /**
     * Returns the most recently created draft-account ID.
     *
     * @return most recently created draft-account identifier.
     */
    private String lastCreatedIdOrFail() {
        return scenarioContext().getLastDraftAccountIdOrFail();
    }

    /**
     * Patches the most recently created draft account with the field values supplied by the
     * scenario.
     *
     * @param data Cucumber table containing the patch values for the request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I patch the draft account with the following details")
    public void patchDraftAccount(DataTable data) throws JSONException {
        actions.patchCreatedDraftAccount(data.asMap(String.class, String.class));
    }

    /**
     * Patches the specified draft account with the field values supplied by the scenario.
     *
     * @param draftAccountId draft-account identifier to use for the request.
     * @param data Cucumber table containing the patch values for the request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I patch the {string} draft account with the following details")
    public void patchDraftAccount(String draftAccountId, DataTable data) throws JSONException {
        actions.patchDraftAccount(draftAccountId, data.asMap(String.class, String.class));
    }

    /**
     * Asserts that the most recent patch succeeded and that the updated draft account can be
     * retrieved with the expected values.
     *
     * @param data Cucumber table containing the expected values for the retrieved draft account.
     */
    @Then(
        "the created draft account is patched successfully "
            + "and the retrieved draft account contains the following data"
    )
    public void createdDraftAccountIsPatchedSuccessfullyAndRetrievedDraftAccountContainsTheFollowingData(
        DataTable data
    ) {
        workflow.assertPatchedDraftAccountCanBeRetrieved(data.asMap(String.class, String.class));
    }

    /**
     * Attempts to patch a draft-account using an unsupported content type.
     */
    @When("I attempt to patch a draft account with an unsupported content type")
    public void patchDraftAccountWithUnsupportedContentType() {
        authorisedJsonRequest()
            .accept("text/plain")
            .when()
            .patch(getTestUrl() + "/draft-accounts/" + "1");
    }

    /**
     * Attempts to patch a draft-account with an unsupported media type.
     */
    @When("I attempt to patch a draft account with an unsupported media type")
    public void patchDraftAccountWithInvalidMediaType() {
        authorisedJsonRequest()
            .accept("application/json")
            .contentType("application/xml")
            .when()
            .patch(getTestUrl() + "/draft-accounts/" + "1");
    }

    /**
     * Sends a malformed patch request to exercise the draft-account internal-server-error path.
     */
    @When("I patch the draft account trying to provoke an internal server error")
    public void patchDraftAccountInternalServerError() {
        authorisedJsonRequest()
            .urlEncodingEnabled(false)
            .when()
            .patch(getTestUrl() + "/draft-accounts/%20");
    }

    /**
     * Attempts to patch the most recently created draft account using an invalid bearer token.
     *
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I attempt to update the draft account with an invalid token")
    public void patchDraftAccountWithInvalidToken() throws JSONException {
        JSONObject patchBody = new JSONObject();
        patchBody.put("account_status", "Publishing Pending");
        patchBody.put("validated_by", "invalidToken");
        patchBody.put("timeline_data", new JSONArray());

        String id = lastCreatedIdOrFail();
        jsonRequestWithToken("invalidToken")
            .body(patchBody.toString())
            .when()
            .patch(getTestUrl() + "/draft-accounts/" + id);
    }
}
