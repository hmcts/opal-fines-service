package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.actions.defendantaccount.DefendantAccountEnforcementsActions;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountActions;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountRequestFactory;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Defines feature-toggle functional steps for the defendant-account search endpoint.
 */
public class DefendantAccountSearchFeatureToggleStepDef extends BaseStepDef {

    private static final String REVIEWING_USER = "opal-test-10@dev.platform.hmcts.net";
    private static final String DEFAULT_ACCOUNT_FIXTURE = "draftAccounts/accountJson/adultAccount.json";
    private static final String DEFAULT_BUSINESS_UNIT_ID = "77";
    private static final String DEFAULT_SUBMITTED_BY = "DEFENF001";
    private static final String DEFAULT_SUBMITTED_BY_NAME = "Laura Clerk";
    private static final String SEARCH_URL = "/defendant-accounts/search";

    private final DraftAccountActions draftAccountActions = new DraftAccountActions();
    private final DraftAccountRequestFactory requestFactory = new DraftAccountRequestFactory();
    private final DefendantAccountEnforcementsActions enforcementActions =
        new DefendantAccountEnforcementsActions();
    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();

    private String prosecutorCaseReference;
    private String businessUnitId;

    /**
     * Creates and publishes a unique defendant account that can be found reliably by the
     * feature-toggle search scenarios.
     *
     * @throws JSONException if any request payload cannot be assembled.
     * @throws IOException if the draft-account fixture or timeline fixture cannot be loaded.
     */
    @Given("a searchable defendant account exists for feature-toggle search")
    public void searchableDefendantAccountExistsForFeatureToggleSearch() throws JSONException, IOException {
        businessUnitId = DEFAULT_BUSINESS_UNIT_ID;
        prosecutorCaseReference = buildUniqueProsecutorCaseReference();

        String originalUser = scenarioContext().getCurrentUserOrDefault(BearerTokenStepDef.DEFAULT_USER);

        JSONObject createRequestBody = buildDraftAccountCreateRequest();
        Response createResponse = draftAccountActions.createDraftAccount(createRequestBody);
        responseAssertions.assertStatus(createResponse, 201);
        draftAccountActions.storeCreatedDraftAccountId(createResponse);

        actAs(REVIEWING_USER);
        try {
            Response publishResponse = draftAccountActions.patchCreatedDraftAccount(buildPublishPatchData());
            responseAssertions.assertStatus(publishResponse, 200);
            enforcementActions.storeCreatedDefendantAccountId(publishResponse);
        } finally {
            actAs(originalUser);
        }
    }

    /**
     * Calls the defendant-account search endpoint with a prosecutor-case-reference search body
     * that omits the optional consolidation flag.
     *
     * @param caseReference prosecutor case reference to search for.
     * @throws JSONException if the request payload cannot be assembled.
     */
    @When("I search defendant accounts using prosecutor case reference {string} without consolidation")
    public void searchDefendantAccountsWithoutConsolidation(String caseReference) throws JSONException {
        performSearch(caseReference, DEFAULT_BUSINESS_UNIT_ID, null);
    }

    /**
     * Calls the defendant-account search endpoint for the account created in the scenario without
     * sending the consolidation flag.
     *
     * @throws JSONException if the request payload cannot be assembled.
     */
    @When("I search the created defendant account without consolidation")
    public void searchCreatedDefendantAccountWithoutConsolidation() throws JSONException {
        performSearch(prosecutorCaseReference, businessUnitId, false);
    }

    /**
     * Calls the defendant-account search endpoint for the account created in the scenario with the
     * consolidation flag enabled.
     *
     * @throws JSONException if the request payload cannot be assembled.
     */
    @When("I search the created defendant account with consolidation")
    public void searchCreatedDefendantAccountWithConsolidation() throws JSONException {
        performSearch(prosecutorCaseReference, businessUnitId, Boolean.TRUE);
    }

    /**
     * Verifies that basic search remains available and that the response shape excludes the
     * consolidated-search-only fields.
     */
    @Then("the basic defendant account search returns the created account")
    public void basicDefendantAccountSearchReturnsTheCreatedAccount() {
        Response response = SerenityRest.lastResponse();

        responseAssertions.assertStatus(response, 200);
        assertEquals(1, response.jsonPath().getInt("count"));
        assertEquals(
            scenarioContext().getCreatedDefendantAccountIdOrFail(),
            response.jsonPath().getString("defendant_accounts[0].defendant_account_id")
        );
        responseAssertions.assertBodyDoesNotContainField(response, "has_collection_order");
        responseAssertions.assertBodyDoesNotContainField(response, "account_version");
        responseAssertions.assertBodyDoesNotContainField(response, "checks");
    }

    /**
     * Verifies that consolidated search returns the created account and includes the additional
     * fields exposed by the consolidated response.
     */
    @Then("the consolidated defendant account search returns the created account")
    public void consolidatedDefendantAccountSearchReturnsTheCreatedAccount() {
        Response response = SerenityRest.lastResponse();

        responseAssertions.assertStatus(response, 200);
        assertEquals(1, response.jsonPath().getInt("count"));
        assertEquals(
            scenarioContext().getCreatedDefendantAccountIdOrFail(),
            response.jsonPath().getString("defendant_accounts[0].defendant_account_id")
        );
        assertNotNull(response.jsonPath().get("defendant_accounts[0].has_collection_order"));
        assertNotNull(response.jsonPath().get("defendant_accounts[0].account_version"));
        assertNotNull(response.jsonPath().get("defendant_accounts[0].checks.errors"));
        assertNotNull(response.jsonPath().get("defendant_accounts[0].checks.warnings"));
    }

    /**
     * Builds the create-draft-account request body used to seed a unique searchable account for
     * the scenario.
     *
     * @return create request body for the draft-account API.
     * @throws JSONException if the request payload cannot be assembled.
     * @throws IOException if the referenced fixtures cannot be loaded.
     */
    private JSONObject buildDraftAccountCreateRequest() throws JSONException, IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("business_unit_id", Long.parseLong(businessUnitId));
        requestBody.put("submitted_by", DEFAULT_SUBMITTED_BY);
        requestBody.put("submitted_by_name", DEFAULT_SUBMITTED_BY_NAME);
        requestBody.put("account_type", "Fine");
        requestBody.put("account_status", JSONObject.NULL);
        requestBody.put("account", buildUniqueAccountFixture());
        //requestBody.put("timeline_data", requestFactory.loadDefaultTimelineFixture());
        return requestBody;
    }

    /**
     * Loads the shared adult-account fixture and injects unique search data so the scenario can
     * search deterministically without colliding with existing records.
     *
     * @return account payload to embed in the create-draft-account request.
     * @throws IOException if the source fixture cannot be loaded.
     * @throws JSONException if the loaded fixture cannot be updated as JSON.
     */
    private JSONObject buildUniqueAccountFixture() throws IOException, JSONException {
        JSONObject account = requestFactory.loadAccountFixture(DEFAULT_ACCOUNT_FIXTURE);
        account.put("prosecutor_case_reference", prosecutorCaseReference);

        JSONObject defendant = account.getJSONObject("defendant");
        defendant.put(
            "surname",
            ("FTSUR" + prosecutorCaseReference.replace("-", "")).toUpperCase(Locale.ROOT)
        );
        defendant.put("forenames", "Toggle Search");

        return account;
    }

    /**
     * Builds the patch payload used to publish the created draft account into a searchable
     * defendant account.
     *
     * @return patch data for the draft-account publish step.
     */
    private Map<String, String> buildPublishPatchData() {
        Map<String, String> patchData = new LinkedHashMap<>();
        patchData.put("business_unit_id", businessUnitId);
        patchData.put("account_status", "Publishing Pending");
        patchData.put("validated_by", DEFAULT_SUBMITTED_BY + "_REVIEWER");
        patchData.put("If-Match", "0");
        return patchData;
    }

    /**
     * Executes the defendant-account search call using reference-number criteria and an optional
     * consolidation flag.
     *
     * @param caseReference prosecutor case reference to search for.
     * @param buId business-unit identifier to include in the request.
     * @param consolidationSearch optional consolidation flag; {@code null} omits the parameter.
     * @throws JSONException if the request payload cannot be assembled.
     */
    private void performSearch(String caseReference, String buId, Boolean consolidationSearch) throws JSONException {
        JSONObject referenceNumber = new JSONObject()
            .put("account_number", JSONObject.NULL)
            .put("prosecutor_case_reference", caseReference)
            .put("organisation", false);

        JSONObject requestBody = new JSONObject()
            .put("active_accounts_only", true)
            .put("business_unit_ids", new JSONArray().put(Integer.parseInt(buId)))
            .put("reference_number", referenceNumber)
            .put("defendant", JSONObject.NULL);

        if (consolidationSearch != null) {
            requestBody.put("consolidation_search", consolidationSearch);
        }

        authorisedJsonRequest()
            .body(requestBody.toString())
            .when()
            .post(getTestUrl() + SEARCH_URL);
    }

    /**
     * Generates a unique prosecutor-case-reference value for scenario-owned test data.
     *
     * @return unique prosecutor case reference.
     */
    private String buildUniqueProsecutorCaseReference() {
        return "FTR1C" + Long.toString(System.nanoTime(), 36).toUpperCase(Locale.ROOT);
    }

    /**
     * Switches subsequent API calls in the current scenario to act as the supplied user.
     *
     * @param user user alias or email to use for subsequent requests.
     */
    private void actAs(String user) {
        BearerTokenStepDef.setTokenOverride(BearerTokenStepDef.getAccessTokenForUser(user));
        scenarioContext().setCurrentUser(user);
    }
}
