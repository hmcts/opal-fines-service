package uk.gov.hmcts.opal.workflows.draftaccount;

import io.restassured.response.Response;
import java.io.IOException;
import java.util.Map;
import org.json.JSONException;
import uk.gov.hmcts.opal.assertions.draftaccount.DraftAccountAssertions;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountActions;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountRequestFactory;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;

/**
 * Coordinates higher-level authorisation flows for draft-account scenarios so the feature files
 * can describe the business rule under test rather than explicit request plumbing.
 */
public class DraftAccountAuthorisationWorkflow extends BaseStepDef {
    private static final Map<String, String> HIDDEN_DRAFT_ACCOUNT_FIELDS = Map.of(
        "business_unit_id", "",
        "account_type", "",
        "account_status", "",
        "account_snapshot.defendant_name", "",
        "account_snapshot.date_of_birth", "",
        "account_snapshot.account_type", "",
        "account_snapshot.submitted_by", "",
        "account_snapshot.business_unit_name", ""
    );

    private final DraftAccountActions actions = new DraftAccountActions();
    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();
    private final DraftAccountAssertions draftAccountAssertions = new DraftAccountAssertions();

    /**
     * Attempts to create a draft account as the supplied user.
     *
     * @param user user who should submit the create request.
     * @param accountData field values used to build the create payload.
     * @throws JSONException if the JSON request body cannot be assembled.
     * @throws IOException if a supporting draft-account fixture cannot be loaded.
     */
    public void attemptCreateDraftAccountAsUser(String user, Map<String, String> accountData)
        throws JSONException, IOException {
        actAs(user);
        actions.createDraftAccount(accountData);
    }

    /**
     * Retrieves the created draft account as the supplied user.
     *
     * @param user user who should attempt to view the created draft account.
     */
    public void attemptToViewCreatedDraftAccountAsUser(String user) {
        actAs(user);
        actions.getSingleCreatedDraftAccount();
    }

    /**
     * Retrieves the draft-account list filtered by business unit as the supplied user.
     *
     * @param user user who should issue the list request.
     * @param businessUnit business-unit filter value to apply to the list request.
     */
    public void attemptToListDraftAccountsForBusinessUnitAsUser(String user, String businessUnit) {
        actAs(user);
        actions.getDraftAccounts(Map.of("business_unit", businessUnit));
    }

    /**
     * Retrieves the draft-account list without filters as the supplied user.
     *
     * @param user user who should issue the list request.
     */
    public void requestVisibleDraftAccountsAsUser(String user) {
        actAs(user);
        actions.getDraftAccounts(Map.of());
    }

    /**
     * Attempts to patch the created draft account as the supplied user.
     *
     * @param user user who should issue the patch request.
     * @param patchData field values used to build the patch payload.
     * @throws JSONException if the JSON request body cannot be assembled.
     */
    public void attemptToPatchCreatedDraftAccountAsUser(String user, Map<String, String> patchData)
        throws JSONException {
        actAs(user);
        actions.patchCreatedDraftAccount(patchData);
    }

    /**
     * Attempts to replace the created draft account as the supplied user.
     *
     * @param user user who should issue the replace request.
     * @param replacementData field values used to build the replacement payload.
     * @throws JSONException if the JSON request body cannot be assembled.
     * @throws IOException if a supporting draft-account fixture cannot be loaded.
     */
    public void attemptToReplaceCreatedDraftAccountAsUser(String user, Map<String, String> replacementData)
        throws JSONException, IOException {
        actAs(user);
        actions.replaceCreatedDraftAccount(
            replacementData,
            DraftAccountRequestFactory.BusinessUnitIdMode.INTEGER
        );
    }

    /**
     * Asserts that the latest view attempt was forbidden and that no draft-account detail fields
     * were exposed to the current user.
     */
    public void assertCreatedDraftAccountAccessDenied() {
        Response response = net.serenitybdd.rest.SerenityRest.lastResponse();
        responseAssertions.assertStatus(response, 403);
        responseAssertions.assertResponseContains(response, HIDDEN_DRAFT_ACCOUNT_FIELDS);
    }

    /**
     * Asserts that the latest request failed with the supplied status code.
     *
     * @param expectedStatus status code expected from the most recent request.
     */
    public void assertRejectedWithStatus(int expectedStatus) {
        assertLatestStatus(expectedStatus);
    }

    /**
     * Retrieves the created draft account as its creator and asserts that its persisted state
     * still matches the supplied values.
     *
     * @param expectedData field names and values expected when the creator retrieves the draft
     *                     account.
     */
    public void assertCreatedDraftAccountRemains(Map<String, String> expectedData) {
        Response getResponse = getCreatedDraftAccountAsCreator();
        responseAssertions.assertStatus(getResponse, 200);
        responseAssertions.assertResponseContains(getResponse, expectedData);
    }

    /**
     * Retrieves the created draft account as its creator and asserts that the last request failed
     * with the supplied status code before verifying that the persisted state is unchanged.
     *
     * @param expectedStatus status code expected from the most recent request.
     * @param expectedData field names and values expected when the creator retrieves the draft
     *                     account.
     */
    public void assertRejectedAndCreatedDraftAccountRemains(int expectedStatus, Map<String, String> expectedData) {
        assertRejectedWithStatus(expectedStatus);
        assertCreatedDraftAccountRemains(expectedData);
    }

    /**
     * Asserts that the currently visible draft-account summaries do not contain the supplied
     * business-unit identifiers.
     *
     * @param businessUnits comma-separated business-unit identifiers that must be absent.
     */
    public void assertVisibleDraftAccountsExcludeBusinessUnits(String businessUnits) {
        draftAccountAssertions.assertLatestSummaryFieldDoesNotContainAny(
            "business_unit_id",
            java.util.Arrays.stream(businessUnits.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList()
        );
    }

    /**
     * Builds the auth context for a scenario by switching to the supplied user.
     *
     * @param user user alias or email to use for subsequent requests.
     */
    private void actAs(String user) {
        BearerTokenStepDef.setTokenOverride(BearerTokenStepDef.getAccessTokenForUser(user));
        scenarioContext().setCurrentUser(user);
    }

    /**
     * Retrieves the created draft account as the user who created it earlier in the scenario.
     *
     * @return API response returned by the creator's GET request.
     */
    private Response getCreatedDraftAccountAsCreator() {
        String draftAccountId = actions.onlyCreatedDraftAccountIdOrFail();
        String originalUser = scenarioContext().getCurrentUserOrDefault(BearerTokenStepDef.DEFAULT_USER);
        String creatorUser = scenarioContext().getDraftAccountCreatorOrDefault(
            draftAccountId,
            BearerTokenStepDef.DEFAULT_USER
        );

        actAs(creatorUser);
        try {
            return actions.getSingleCreatedDraftAccount();
        } finally {
            actAs(originalUser);
        }
    }

    /**
     * Asserts the status code from the latest response, supporting both Serenity and raw-client
     * request paths.
     *
     * @param expectedStatus expected HTTP status code.
     */
    private void assertLatestStatus(int expectedStatus) {
        var rawResponse = scenarioContext().consumeLatestHttpResponse();
        if (rawResponse != null) {
            org.junit.jupiter.api.Assertions.assertEquals(
                expectedStatus,
                rawResponse.statusCode(),
                "Unexpected HTTP status"
            );
            return;
        }

        responseAssertions.assertStatus(net.serenitybdd.rest.SerenityRest.lastResponse(), expectedStatus);
    }
}
