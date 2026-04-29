package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import org.json.JSONException;
import uk.gov.hmcts.opal.workflows.draftaccount.DraftAccountAuthorisationWorkflow;

/**
 * Defines higher-level authorisation steps for draft-account scenarios.
 */
public class DraftAccountAuthorisationSteps {
    private final DraftAccountAuthorisationWorkflow workflow = new DraftAccountAuthorisationWorkflow();

    /**
     * Attempts to create a draft account as the supplied user.
     *
     * @param user user who should submit the create request.
     * @param dataTable Cucumber table containing the draft-account values for the request.
     * @throws JSONException if the JSON request body cannot be assembled.
     * @throws IOException if a supporting draft-account fixture cannot be loaded.
     */
    @When("the {string} user attempts to create a draft account with the following details")
    public void userAttemptsToCreateDraftAccount(String user, DataTable dataTable)
        throws JSONException, IOException {
        workflow.attemptCreateDraftAccountAsUser(user, dataTable.asMap(String.class, String.class));
    }

    /**
     * Attempts to view the created draft account as the supplied user.
     *
     * @param user user who should attempt to view the created draft account.
     */
    @When("the {string} user attempts to view the created draft account")
    public void userAttemptsToViewCreatedDraftAccount(String user) {
        workflow.attemptToViewCreatedDraftAccountAsUser(user);
    }

    /**
     * Attempts to list draft accounts for the supplied business unit as the supplied user.
     *
     * @param user user who should issue the list request.
     * @param businessUnit business-unit filter value to apply to the list request.
     */
    @When("the {string} user attempts to list draft accounts for business unit {string}")
    public void userAttemptsToListDraftAccountsForBusinessUnit(String user, String businessUnit) {
        workflow.attemptToListDraftAccountsForBusinessUnitAsUser(user, businessUnit);
    }

    /**
     * Requests the visible draft-account list as the supplied user.
     *
     * @param user user who should issue the list request.
     */
    @When("the {string} user requests visible draft accounts")
    public void userRequestsVisibleDraftAccounts(String user) {
        workflow.requestVisibleDraftAccountsAsUser(user);
    }

    /**
     * Attempts to patch the created draft account as the supplied user.
     *
     * @param user user who should issue the patch request.
     * @param dataTable Cucumber table containing the patch values for the request.
     * @throws JSONException if the JSON request body cannot be assembled.
     */
    @When("the {string} user attempts to patch the created draft account with the following details")
    public void userAttemptsToPatchCreatedDraftAccount(String user, DataTable dataTable) throws JSONException {
        workflow.attemptToPatchCreatedDraftAccountAsUser(user, dataTable.asMap(String.class, String.class));
    }

    /**
     * Attempts to replace the created draft account as the supplied user.
     *
     * @param user user who should issue the replace request.
     * @param dataTable Cucumber table containing the replacement values for the request.
     * @throws JSONException if the JSON request body cannot be assembled.
     * @throws IOException if a supporting draft-account fixture cannot be loaded.
     */
    @When("the {string} user attempts to replace the created draft account with the following details")
    public void userAttemptsToReplaceCreatedDraftAccount(String user, DataTable dataTable)
        throws JSONException, IOException {
        workflow.attemptToReplaceCreatedDraftAccountAsUser(user, dataTable.asMap(String.class, String.class));
    }

    /**
     * Asserts that access to the created draft account was denied and no detail fields were
     * exposed to the current user.
     */
    @Then("access to the created draft account is denied")
    public void accessToCreatedDraftAccountIsDenied() {
        workflow.assertCreatedDraftAccountAccessDenied();
    }

    /**
     * Asserts that the created draft account still matches the supplied values.
     *
     * @param dataTable Cucumber table containing the expected persisted draft-account values.
     */
    @Then("the created draft account remains with the following data")
    public void createdDraftAccountRemainsWithTheFollowingData(DataTable dataTable) {
        workflow.assertCreatedDraftAccountRemains(dataTable.asMap(String.class, String.class));
    }

    /**
     * Asserts that the latest request failed with the supplied status code and that the created
     * draft account still matches the supplied values.
     *
     * @param status expected HTTP status code.
     * @param dataTable Cucumber table containing the expected persisted draft-account values.
     */
    @Then("the request is rejected with status {int} and the created draft account remains with the following data")
    public void requestIsRejectedAndCreatedDraftAccountRemains(int status, DataTable dataTable) {
        workflow.assertRejectedAndCreatedDraftAccountRemains(status, dataTable.asMap(String.class, String.class));
    }

    /**
     * Asserts that the latest request was forbidden and that the created draft account still
     * matches the supplied values.
     *
     * @param dataTable Cucumber table containing the expected persisted draft-account values.
     */
    @Then("the request is rejected as forbidden and the created draft account remains with the following data")
    public void requestIsRejectedAsForbiddenAndCreatedDraftAccountRemains(DataTable dataTable) {
        workflow.assertRejectedAndCreatedDraftAccountRemains(403, dataTable.asMap(String.class, String.class));
    }

    /**
     * Asserts that the latest request failed as a bad request and that the created draft account
     * still matches the supplied values.
     *
     * @param dataTable Cucumber table containing the expected persisted draft-account values.
     */
    @Then("the request is rejected as bad request and the created draft account remains with the following data")
    public void requestIsRejectedAsBadRequestAndCreatedDraftAccountRemains(DataTable dataTable) {
        workflow.assertRejectedAndCreatedDraftAccountRemains(400, dataTable.asMap(String.class, String.class));
    }

    /**
     * Asserts that the latest request failed with a conflict and that the created draft account
     * still matches the supplied values.
     *
     * @param dataTable Cucumber table containing the expected persisted draft-account values.
     */
    @Then("the request is rejected as conflict and the created draft account remains with the following data")
    public void requestIsRejectedAsConflictAndCreatedDraftAccountRemains(DataTable dataTable) {
        workflow.assertRejectedAndCreatedDraftAccountRemains(409, dataTable.asMap(String.class, String.class));
    }

    /**
     * Asserts that the current user's visible draft-account list excludes the supplied
     * business-unit identifiers.
     *
     * @param businessUnits comma-separated business-unit identifiers that must be absent.
     */
    @Then("the visible draft accounts exclude business units {string}")
    public void visibleDraftAccountsExcludeBusinessUnits(String businessUnits) {
        workflow.assertVisibleDraftAccountsExcludeBusinessUnits(businessUnits);
    }
}
