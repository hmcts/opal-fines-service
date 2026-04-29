package uk.gov.hmcts.opal.workflows.draftaccount;

import io.restassured.response.Response;
import java.util.Map;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountActions;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;
import uk.gov.hmcts.opal.assertions.draftaccount.DraftAccountAssertions;

/**
 * Coordinates draft-account GET workflows that combine a request action with response assertions.
 */
public class DraftAccountGetWorkflow {
    private final DraftAccountActions actions = new DraftAccountActions();
    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();
    private final DraftAccountAssertions assertions = new DraftAccountAssertions();

    /**
     * Requests draft-account summaries using the supplied filters.
     *
     * @param filters query-parameter filters to apply to the list request.
     */
    public void requestDraftAccounts(Map<String, String> filters) {
        actions.getDraftAccounts(filters);
    }

    /**
     * Retrieves the single created draft account and asserts that its response body matches the
     * supplied expected values.
     *
     * @param expectedData field names and values expected in the response body.
     */
    public void getSingleCreatedDraftAccountAndAssertContains(Map<String, String> expectedData) {
        Response response = actions.getSingleCreatedDraftAccount();
        responseAssertions.assertResponseContains(response, expectedData);
    }

    /**
     * Retrieves the single created draft account and asserts the full happy-path response,
     * including status, headers, and the expected body fields.
     *
     * @param expectedData field names and values expected in the response body.
     */
    public void getSingleCreatedDraftAccountAndAssertRetrieved(Map<String, String> expectedData) {
        Response response = actions.getSingleCreatedDraftAccount();
        assertions.assertRetrievedDraftAccount(response, expectedData);
    }

    /**
     * Asserts that the latest draft-account summary response only contains the supplied business
     * unit identifier.
     *
     * @param businessUnitId business-unit identifier expected in every returned summary.
     */
    public void assertLatestDraftAccountsForBusinessUnit(String businessUnitId) {
        assertions.assertLatestSummaryFields(Map.of("business_unit_id", businessUnitId), true);
    }

    /**
     * Asserts that the latest draft-account summary response reports the supplied business-unit
     * name in every returned summary.
     *
     * @param businessUnitName business-unit name expected in every returned summary.
     */
    public void assertLatestDraftAccountsBusinessUnitName(String businessUnitName) {
        assertions.assertLatestSummaryFields(Map.of("account_snapshot.business_unit_name", businessUnitName), true);
    }

    /**
     * Asserts that the latest draft-account summary response only contains the supplied account
     * status.
     *
     * @param accountStatus account status expected in every returned summary.
     */
    public void assertLatestDraftAccountsWithStatus(String accountStatus) {
        assertions.assertLatestSummaryFields(Map.of("account_status", accountStatus), true);
    }

    /**
     * Asserts that the latest draft-account summary response only contains the supplied
     * submitted-by identifier.
     *
     * @param submittedBy submitted-by identifier expected in every returned summary.
     */
    public void assertLatestDraftAccountsSubmittedBy(String submittedBy) {
        assertions.assertLatestSummaryFields(Map.of("submitted_by", submittedBy), true);
    }

    /**
     * Asserts that the latest draft-account summary response only contains the supplied account
     * status and submitted-by identifier.
     *
     * @param accountStatus account status expected in every returned summary.
     * @param submittedBy submitted-by identifier expected in every returned summary.
     */
    public void assertLatestDraftAccountsWithStatusAndSubmittedBy(String accountStatus, String submittedBy) {
        assertions.assertLatestSummaryFields(
            Map.of("account_status", accountStatus, "submitted_by", submittedBy),
            true
        );
    }
}
