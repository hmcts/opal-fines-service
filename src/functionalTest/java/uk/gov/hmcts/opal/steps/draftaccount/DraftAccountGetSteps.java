package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountActions;
import uk.gov.hmcts.opal.assertions.draftaccount.DraftAccountAssertions;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.workflows.draftaccount.DraftAccountGetWorkflow;

/**
 * Defines Cucumber steps for retrieving draft accounts.
 */
public class DraftAccountGetSteps extends BaseStepDef {
    private final DraftAccountActions actions = new DraftAccountActions();
    private final DraftAccountAssertions assertions = new DraftAccountAssertions();
    private final DraftAccountGetWorkflow workflow = new DraftAccountGetWorkflow();

    /**
     * Retrieves the draft account identified by the supplied draft-account ID.
     *
     * @param draftAccountId draft-account identifier to use for the request.
     */
    @When("I get the draft account {string}")
    public void getDraftAccount(String draftAccountId) {
        actions.getDraftAccount(draftAccountId);
    }


    /**
     * Sends a malformed request to retrieve the draft-account and exercise the
     * internal-server-error path.
     */
    @When("I get the draft account trying to provoke an internal server error")
    public void getDraftAccountInternalServerError() {
        authorisedJsonRequest()
            .urlEncodingEnabled(false)
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "/%20");
    }

    /**
     * Attempts to get a draft-account using an invalid bearer token.
     */
    @When("I attempt to get a draft account with an invalid token")
    public void getDraftAccountWithInvalidToken() {
        jsonRequestWithToken("invalidToken")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "/1234");
    }

    /**
     * Attempts to retrieve the created draft-account using an invalid bearer token.
     */
    @When("I attempt to retrieve the created draft account with an invalid token")
    public void getCreatedDraftAccountWithInvalidToken() {
        String draftAccountId = actions.onlyCreatedDraftAccountIdOrFail();
        jsonRequestWithToken("invalidToken")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
    }

    /**
     * Attempts to get a draft-account using an unsupported content type.
     */
    @When("I attempt to get a draft account with an unsupported content type")
    public void getDraftAccountWithUnsupportedContentType() {
        String draftAccountId = actions.onlyCreatedDraftAccountIdOrFail();
        authorisedJsonRequest()
            .accept("text/plain")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
    }

    /**
     * Retrieves the single created draft-account and asserts that the latest response contains the
     * expected values.
     *
     * @param data Cucumber table containing the expected values for the assertion.
     */
    @When("I get the single created draft account and the response contains")
    public void getSingleDraftAccount(DataTable data) {
        workflow.getSingleCreatedDraftAccountAndAssertContains(data.asMap(String.class, String.class));
    }

    /**
     * Retrieves the single created draft-account and asserts the full happy-path response,
     * including status, headers, and the expected body fields.
     *
     * @param data Cucumber table containing the expected values for the retrieved draft account.
     */
    @Then("the retrieved draft account contains the following data")
    public void retrievedDraftAccountContainsTheFollowingData(DataTable data) {
        workflow.getSingleCreatedDraftAccountAndAssertRetrieved(data.asMap(String.class, String.class));
    }

    /**
     * Retrieves the single created draft-account without asserting the response body.
     */
    @When("I get the single created draft account without asserting the body")
    public void getSingleDraftAccountWithoutAssertingBody() {
        actions.getSingleCreatedDraftAccount();
    }

    /**
     * Requests draft-account summaries for the supplied business unit.
     *
     * @param filter business-unit filter value to apply to the request.
     */
    @When("I request draft accounts for business unit {string}")
    public void requestDraftAccountsForBusinessUnit(String filter) {
        workflow.requestDraftAccounts(Map.of("business_unit", filter));
    }

    /**
     * Requests draft-account summaries for the supplied account status.
     *
     * @param filter account-status filter value to apply to the request.
     */
    @When("I request draft accounts for status {string}")
    public void requestDraftAccountsForStatus(String filter) {
        workflow.requestDraftAccounts(Map.of("status", filter));
    }

    /**
     * Requests draft-account summaries for the supplied submitted-by identifier.
     *
     * @param filter submitted-by filter value to apply to the request.
     */
    @When("I request draft accounts submitted by {string}")
    public void requestDraftAccountsSubmittedBy(String filter) {
        workflow.requestDraftAccounts(Map.of("submitted_by", filter));
    }

    /**
     * Requests draft-account summaries for the supplied account status and submitted-by
     * identifier.
     *
     * @param statusFilter account-status filter value to apply to the request.
     * @param submittedByFilter submitted-by filter value to apply to the request.
     */
    @When("I request draft accounts for status {string} and submitted by {string}")
    public void requestDraftAccountsForStatusAndSubmittedBy(String statusFilter, String submittedByFilter) {
        Map<String, String> filters = new LinkedHashMap<>();
        filters.put("status", statusFilter);
        filters.put("submitted_by", submittedByFilter);
        workflow.requestDraftAccounts(filters);
    }

    /**
     * Asserts that the latest draft-account summary response only contains the supplied business
     * unit identifier.
     *
     * @param businessUnitId business-unit identifier expected in every returned summary.
     */
    @Then("only draft accounts for business unit {string} are returned")
    public void onlyDraftAccountsForBusinessUnitAreReturned(String businessUnitId) {
        workflow.assertLatestDraftAccountsForBusinessUnit(businessUnitId);
    }

    /**
     * Asserts that the latest draft-account summary response reports the supplied business-unit
     * name in every returned summary.
     *
     * @param businessUnitName business-unit name expected in every returned summary.
     */
    @Then("the returned draft accounts identify business unit as {string}")
    public void returnedDraftAccountsIdentifyBusinessUnitAs(String businessUnitName) {
        workflow.assertLatestDraftAccountsBusinessUnitName(businessUnitName);
    }

    /**
     * Asserts that the latest draft-account summary response only contains the supplied account
     * status.
     *
     * @param accountStatus account status expected in every returned summary.
     */
    @Then("only draft accounts with status {string} are returned")
    public void onlyDraftAccountsWithStatusAreReturned(String accountStatus) {
        workflow.assertLatestDraftAccountsWithStatus(accountStatus);
    }

    /**
     * Asserts that the latest draft-account summary response only contains the supplied
     * submitted-by identifier.
     *
     * @param submittedBy submitted-by identifier expected in every returned summary.
     */
    @Then("only draft accounts submitted by {string} are returned")
    public void onlyDraftAccountsSubmittedByAreReturned(String submittedBy) {
        workflow.assertLatestDraftAccountsSubmittedBy(submittedBy);
    }

    /**
     * Asserts that the latest draft-account summary response only contains the supplied account
     * status and submitted-by identifier.
     *
     * @param accountStatus account status expected in every returned summary.
     * @param submittedBy submitted-by identifier expected in every returned summary.
     */
    @Then("only draft accounts with status {string} submitted by {string} are returned")
    public void onlyDraftAccountsWithStatusSubmittedByAreReturned(String accountStatus, String submittedBy) {
        workflow.assertLatestDraftAccountsWithStatusAndSubmittedBy(accountStatus, submittedBy);
    }

    /**
     * Asserts that the filtered draft-account response excludes accounts from the supplied
     * business unit or units.
     *
     * @param filter comma-separated business-unit values that must not appear in the response.
     */
    @And("the returned draft accounts exclude business units {string}")
    public void returnedDraftAccountsExcludeBusinessUnit(String filter) {
        assertions.assertLatestSummaryFieldDoesNotContainAny("business_unit_id", parseCommaSeparatedValues(filter));
    }

    /**
     * Asserts that the filtered draft-account response excludes accounts with the supplied status.
     *
     * @param filter filter value to apply to the request.
     */
    @And("the returned draft accounts exclude status {string}")
    public void returnedDraftAccountsExcludeStatus(String filter) {
        assertions.assertLatestSummaryFieldDoesNotContain("account_status", filter);
    }

    /**
     * Asserts that the filtered draft-account response excludes accounts submitted by the supplied
     * value.
     *
     * @param filter filter value to apply to the request.
     */
    @And("the returned draft accounts exclude accounts submitted by {string}")
    public void returnedDraftAccountsExcludeAccountsSubmittedBy(String filter) {
        assertions.assertLatestSummaryFieldDoesNotContain("submitted_by", filter);
    }

    /**
     * Splits a comma-separated step argument into trimmed values so a single Gherkin step can
     * describe one or more excluded business units.
     *
     * @param values comma-separated values supplied in the step text.
     * @return trimmed non-blank values extracted from the step argument.
     */
    private List<String> parseCommaSeparatedValues(String values) {
        return Arrays.stream(values.split(","))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .toList();
    }

    /**
     * Attempts to get draft accounts using an invalid bearer token.
     */
    @When("I attempt to get draft accounts with an invalid token")
    public void getDraftAccountsWithAnInvalidToken() {
        jsonRequestWithToken("invalidToken")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI);
    }

    /**
     * Attempts to get draft accounts using an unsupported content type.
     */
    @When("I attempt to get draft accounts with an unsupported content type")
    public void getDraftAccountsWithAnUnsupportedContentType() {
        authorisedJsonRequest()
            .accept("text/plain")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI);
    }

    /**
     * Sends a malformed request to retrieve the draft accounts and exercise the
     * internal-server-error path.
     */
    @When("I get the draft accounts trying to provoke an internal server error")
    public void getDraftAccountsToProvokeInternalServerError() {
        authorisedJsonRequest()
            .urlEncodingEnabled(false)
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "?submitted_by=me&not_submitted_by=you");
    }

    /**
     * Retrieves the draft accounts.
     */
    @When("I request all draft accounts")
    public void requestAllDraftAccounts() {
        workflow.requestDraftAccounts(Map.of());
    }
}
