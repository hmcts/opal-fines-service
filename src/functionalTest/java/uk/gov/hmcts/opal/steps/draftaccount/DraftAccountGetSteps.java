package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.LinkedHashMap;
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
     * Retrieves the single created draft-account without asserting the response body.
     */
    @When("I get the single created draft account without asserting the body")
    public void getSingleDraftAccountWithoutAssertingBody() {
        actions.getSingleCreatedDraftAccountUsingSessionToken();
    }

    /**
     * Retrieves the draft accounts filtering on the business-unit and asserts that the latest
     * response contains the expected values.
     *
     * @param filter filter value to apply to the request.
     * @param data Cucumber table containing the expected values for the assertion.
     */
    @When("I get the draft accounts filtering on the Business unit {string} then the response contains")
    public void getDraftAccountsFilteringOnBU(String filter, DataTable data) {
        workflow.getDraftAccountsAndAssertSummaries(Map.of("business_unit", filter),
                                                    data.asMap(String.class, String.class));
    }

    /**
     * Retrieves the draft accounts filtering on the status and asserts that the latest response
     * contains the expected values.
     *
     * @param filter filter value to apply to the request.
     * @param data Cucumber table containing the expected values for the assertion.
     */
    @When("I get the draft accounts filtering on the Status {string} then the response contains")
    public void getDraftAccountsFilteringOnStatuses(String filter, DataTable data) {
        workflow.getDraftAccountsAndAssertSummaries(Map.of("status", filter), data.asMap(String.class, String.class));
    }

    /**
     * Retrieves the draft accounts filtering on submitted by and asserts that the latest response
     * contains the expected values.
     *
     * @param filter filter value to apply to the request.
     * @param data Cucumber table containing the expected values for the assertion.
     */
    @When("I get the draft accounts filtering on Submitted by {string} then the response contains")
    public void getDraftAccountsFilteringOnSubmittedBy(String filter, DataTable data) {
        workflow.getDraftAccountsAndAssertSummaries(Map.of("submitted_by", filter),
                                                    data.asMap(String.class, String.class));
    }

    /**
     * Retrieves the draft accounts filtering on the status and submitted by and asserts that the
     * latest response contains the expected values.
     *
     * @param statusFilter status filter value to apply to the request.
     * @param submittedByFilter submitted-by filter value to apply to the request.
     * @param data Cucumber table containing the expected values for the assertion.
     */
    @When("I get the draft accounts filtering on the Status {string} and Submitted by {string} "
        + "then the response contains")
    public void getDraftAccountsFilteringOnStatusesAndSubmittedBy(String statusFilter, String submittedByFilter,
                                                                  DataTable data) {
        Map<String, String> filters = new LinkedHashMap<>();
        filters.put("status", statusFilter);
        filters.put("submitted_by", submittedByFilter);
        workflow.getDraftAccountsAndAssertSummaries(filters, data.asMap(String.class, String.class));
    }

    /**
     * Asserts that the filtered draft-account response excludes accounts from the supplied
     * business unit.
     *
     * @param filter filter value to apply to the request.
     */
    @And("The draft account filtered response does not contain accounts in the {string} business unit")
    public void draftAccountFilteredResponseDoesNotContainAccountsInBusinessUnit(String filter) {
        assertions.assertLatestSummaryFieldDoesNotContain("business_unit_id", filter);
    }

    /**
     * Asserts that the filtered draft-account response excludes accounts with the supplied status.
     *
     * @param filter filter value to apply to the request.
     */
    @And("The draft account filtered response does not contain accounts with status {string}")
    public void draftAccountFilteredResponseDoesNotContainAccountsInStatus(String filter) {
        assertions.assertLatestSummaryFieldDoesNotContain("account_status", filter);
    }

    /**
     * Asserts that the filtered draft-account response excludes accounts submitted by the supplied
     * value.
     *
     * @param filter filter value to apply to the request.
     */
    @And("The draft account filtered response does not contain accounts submitted by {string}")
    public void draftAccountFilteredResponseDoesNotContainAccountsSubmittedBy(String filter) {
        assertions.assertLatestSummaryFieldDoesNotContain("submitted_by", filter);
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
     *
     * @param filter filter value to apply to the request.
     */
    @Then("I get the draft accounts filtering on the Business unit {string}")
    public void getTheDraftAccountsFilteringOnTheBusinessUnit(String filter) {
        actions.getDraftAccounts(Map.of("business_unit", filter));
    }

    /**
     * Retrieves the draft accounts.
     */
    @When("I get the draft accounts")
    public void getDraftAccounts() {
        actions.getDraftAccounts(Map.of());
    }
}
