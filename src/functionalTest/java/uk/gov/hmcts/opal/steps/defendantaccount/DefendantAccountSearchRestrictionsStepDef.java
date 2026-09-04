package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountActions;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountRequestFactory;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Defines PO-2970 functional steps for the defendant-account search endpoint.
 */
public class DefendantAccountSearchRestrictionsStepDef extends BaseStepDef {

    private static final String REVIEWING_USER = "opal-test-10@dev.platform.hmcts.net";
    private static final String DEFAULT_ACCOUNT_FIXTURE = "draftAccounts/accountJson/adultAccount.json";
    private static final String DEFAULT_BUSINESS_UNIT_ID = "77";
    private static final String SEARCH_URL = "/defendant-accounts/search";
    private static final String BIRTH_DATE = "1980-02-03";
    private static final String POSTCODE = "ZZ1 1ZZ";

    private final DraftAccountActions draftAccountActions = new DraftAccountActions();
    private final DraftAccountRequestFactory requestFactory = new DraftAccountRequestFactory();
    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();
    private final Map<String, Response> allowedSearchResponses = new LinkedHashMap<>();

    private SearchAccountData createdAccount;
    private SearchAccountData startsWithAccount;
    private SearchAccountData containsOnlyAccount;
    private String surnamePrefix;

    /**
     * Calls defendant-account search with national insurance number and one extra field populated.
     *
     * @param conflictingField defendant search field to populate alongside NI number.
     * @throws JSONException if the request payload cannot be assembled.
     */
    @When("I search defendant accounts with national insurance number and {string} populated")
    public void searchWithNationalInsuranceNumberAndAnotherField(String conflictingField) throws JSONException {
        JSONObject defendant = defaultDefendantSearchCriteria()
            .put("national_insurance_number", "QQ123456C");

        switch (conflictingField) {
            case "address_line_1" -> defendant.put("address_line_1", "1 Test Street");
            case "postcode" -> defendant.put("postcode", "AB1 2CD");
            case "organisation_name" -> defendant.put("organisation_name", "Test Organisation");
            case "surname" -> defendant.put("surname", "Smith");
            case "forenames" -> defendant.put("forenames", "John");
            case "birth_date" -> defendant.put("birth_date", "1980-01-01");
            default -> throw new IllegalArgumentException("Unsupported conflicting field: " + conflictingField);
        }

        performSearch(defendant);
    }

    /**
     * Calls defendant-account search with only a dependent personal field populated.
     *
     * @param criterion personal criterion to populate without surname.
     * @throws JSONException if the request payload cannot be assembled.
     */
    @When("I search defendant accounts with only {string} populated")
    public void searchWithOnlyDependentPersonalCriterion(String criterion) throws JSONException {
        JSONObject defendant = defaultDefendantSearchCriteria();

        switch (criterion) {
            case "forenames" -> defendant.put("forenames", "John");
            case "birth_date" -> defendant.put("birth_date", "1980-01-01");
            default -> throw new IllegalArgumentException("Unsupported personal criterion: " + criterion);
        }

        performSearch(defendant);
    }

    /**
     * Creates and publishes a unique account for PO-2970 positive search scenarios.
     *
     * @throws JSONException if any request payload cannot be assembled.
     * @throws IOException if the draft-account fixture cannot be loaded.
     */
    @Given("a published defendant account exists for PO-2970 defendant account search")
    public void publishedDefendantAccountExistsForPo2970Search() throws JSONException, IOException {
        String token = uniqueToken();
        createdAccount = new SearchAccountData(
            "PO2970" + token,
            "SUR" + token,
            "First" + token,
            "QQ" + numericToken() + "C",
            "PO-2970 Address " + token,
            POSTCODE
        );

        createdAccount = createdAccount.withAccountId(createAndPublishAccount(createdAccount));
    }

    /**
     * Executes the positive PO-2970 search cases against the scenario-owned account.
     *
     * @param searchTypes Cucumber table listing the search payload types to execute.
     * @throws JSONException if any request payload cannot be assembled.
     */
    @When("I search for the PO-2970 account using the allowed payloads")
    public void searchForPo2970AccountUsingAllowedPayloads(DataTable searchTypes) throws JSONException {
        allowedSearchResponses.clear();

        for (String searchType : searchTypes.asList()) {
            JSONObject defendant = defaultDefendantSearchCriteria();
            switch (searchType) {
                case "national_insurance_number" ->
                    defendant.put("national_insurance_number", createdAccount.nationalInsuranceNumber());
                case "forenames_and_surname" -> defendant
                    .put("forenames", createdAccount.forenames())
                    .put("surname", createdAccount.surname());
                case "birth_date_and_surname" -> defendant
                    .put("birth_date", BIRTH_DATE)
                    .put("surname", createdAccount.surname());
                case "address_line_1_only" -> defendant.put("address_line_1", createdAccount.addressLine1());
                case "postcode_only" -> defendant.put("postcode", createdAccount.postcode());
                default -> throw new IllegalArgumentException("Unsupported search type: " + searchType);
            }

            allowedSearchResponses.put(searchType, performSearch(defendant));
        }
    }

    /**
     * Verifies that each positive PO-2970 search returned the scenario-owned account.
     */
    @Then("each allowed defendant search returns the created account")
    public void eachAllowedDefendantSearchReturnsTheCreatedAccount() {
        assertNotNull(createdAccount, "A PO-2970 account must have been created before searching");
        assertFalse(allowedSearchResponses.isEmpty(), "No allowed search responses were recorded");

        for (Map.Entry<String, Response> entry : allowedSearchResponses.entrySet()) {
            Response response = entry.getValue();
            responseAssertions.assertStatus(response, 200);
            assertResponseContainsAccountId(response, createdAccount.accountId(), entry.getKey());
        }
    }

    /**
     * Creates two accounts where only one surname starts with the scenario search prefix.
     *
     * @throws JSONException if any request payload cannot be assembled.
     * @throws IOException if the draft-account fixture cannot be loaded.
     */
    @Given("published defendant accounts exist for PO-2970 surname starts-with search")
    public void publishedAccountsExistForPo2970SurnameStartsWithSearch() throws JSONException, IOException {
        String token = uniqueToken();
        surnamePrefix = "SUR" + token;

        startsWithAccount = new SearchAccountData(
            "PO2970A" + token,
            surnamePrefix + "MATCH",
            "Starts" + token,
            "QQ" + numericToken() + "C",
            "PO-2970 Starts Address " + token,
            POSTCODE
        );
        startsWithAccount = startsWithAccount.withAccountId(createAndPublishAccount(startsWithAccount));

        containsOnlyAccount = new SearchAccountData(
            "PO2970B" + token,
            "OTHER" + surnamePrefix + "MATCH",
            "Contains" + token,
            "QQ" + numericToken() + "D",
            "PO-2970 Contains Address " + token,
            POSTCODE
        );
        containsOnlyAccount = containsOnlyAccount.withAccountId(createAndPublishAccount(containsOnlyAccount));
    }

    /**
     * Searches by the unique surname prefix generated for the starts-with scenario.
     *
     * @throws JSONException if the request payload cannot be assembled.
     */
    @When("I search defendant accounts by the PO-2970 surname prefix")
    public void searchDefendantAccountsByPo2970SurnamePrefix() throws JSONException {
        JSONObject defendant = defaultDefendantSearchCriteria()
            .put("surname", surnamePrefix);

        performSearch(defendant);
    }

    /**
     * Verifies that the account whose surname starts with the prefix is returned and the account
     * whose surname only contains the prefix is excluded.
     */
    @Then("the defendant account search returns the PO-2970 starts-with account only")
    public void defendantAccountSearchReturnsPo2970StartsWithAccountOnly() {
        Response response = SerenityRest.lastResponse();
        responseAssertions.assertStatus(response, 200);

        List<String> returnedAccountIds = accountIdsFrom(response);
        assertTrue(
            returnedAccountIds.contains(startsWithAccount.accountId()),
            "Expected starts-with account ID to be returned"
        );
        assertFalse(
            returnedAccountIds.contains(containsOnlyAccount.accountId()),
            "Account whose surname only contains the prefix must not be returned"
        );
    }

    /**
     * Verifies that the latest defendant-account search failure is the expected schema problem.
     */
    @Then("the defendant account search request is rejected as a schema bad request")
    public void defendantAccountSearchRequestIsRejectedAsSchemaBadRequest() {
        SerenityRest.then()
            .assertThat()
            .statusCode(400)
            .body("title", org.hamcrest.Matchers.equalTo("Bad Request"))
            .body("status", org.hamcrest.Matchers.equalTo(400))
            .body("detail", org.hamcrest.Matchers.equalTo("The request does not conform to the required JSON schema"))
            .body("type", org.hamcrest.Matchers.equalTo("https://hmcts.gov.uk/problems/json-schema-validation"))
            .body("retriable", org.hamcrest.Matchers.equalTo(false));
    }

    private String createAndPublishAccount(SearchAccountData accountData) throws JSONException, IOException {
        String originalUser = scenarioContext().getCurrentUserOrDefault(BearerTokenStepDef.DEFAULT_USER);

        JSONObject createRequestBody = buildDraftAccountCreateRequest(accountData);
        Response createResponse = draftAccountActions.createDraftAccount(createRequestBody);
        responseAssertions.assertStatus(createResponse, 201);
        draftAccountActions.storeCreatedDraftAccountId(createResponse);
        String draftAccountId = scenarioContext().getLastDraftAccountIdOrFail();

        actAs(REVIEWING_USER);
        try {
            Response publishResponse = draftAccountActions.patchDraftAccount(
                draftAccountId,
                buildPublishPatchData()
            );
            responseAssertions.assertStatus(publishResponse, 200);
            Object accountId = publishResponse.jsonPath().get("account_id");
            if (accountId == null) {
                accountId = draftAccountActions.getDraftAccount(draftAccountId).jsonPath().get("account_id");
            }
            assertNotNull(accountId, "Expected published draft account to expose account_id");
            return String.valueOf(accountId);
        } finally {
            actAs(originalUser);
        }
    }

    private JSONObject buildDraftAccountCreateRequest(SearchAccountData accountData) throws JSONException, IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("business_unit_id", Long.parseLong(DEFAULT_BUSINESS_UNIT_ID));
        requestBody.put("account_type", "Fine");
        requestBody.put("account_status", JSONObject.NULL);
        requestBody.put("account", buildAccountFixture(accountData));
        return requestBody;
    }

    private JSONObject buildAccountFixture(SearchAccountData accountData) throws IOException, JSONException {
        JSONObject account = requestFactory.loadAccountFixture(DEFAULT_ACCOUNT_FIXTURE);
        account.put("prosecutor_case_reference", accountData.prosecutorCaseReference());
        account.put("account_sentence_date", BIRTH_DATE);

        JSONObject defendant = account.getJSONObject("defendant");
        defendant.put("surname", accountData.surname());
        defendant.put("forenames", accountData.forenames());
        defendant.put("dob", BIRTH_DATE);
        defendant.put("address_line_1", accountData.addressLine1());
        defendant.put("post_code", accountData.postcode());
        defendant.put("national_insurance_number", accountData.nationalInsuranceNumber());

        return account;
    }

    private Map<String, String> buildPublishPatchData() {
        Map<String, String> patchData = new LinkedHashMap<>();
        patchData.put("business_unit_id", DEFAULT_BUSINESS_UNIT_ID);
        patchData.put("account_status", "Publishing Pending");
        patchData.put("If-Match", "0");
        return patchData;
    }

    private Response performSearch(JSONObject defendant) throws JSONException {
        JSONObject requestBody = new JSONObject()
            .put("active_accounts_only", true)
            .put("business_unit_ids", new JSONArray().put(Integer.parseInt(DEFAULT_BUSINESS_UNIT_ID)))
            .put("reference_number", JSONObject.NULL)
            .put("defendant", defendant)
            .put("consolidation_search", false);

        return authorisedJsonRequest()
            .body(requestBody.toString())
            .when()
            .post(getTestUrl() + SEARCH_URL);
    }

    private JSONObject defaultDefendantSearchCriteria() throws JSONException {
        return new JSONObject()
            .put("include_aliases", false)
            .put("organisation", false)
            .put("address_line_1", JSONObject.NULL)
            .put("postcode", JSONObject.NULL)
            .put("organisation_name", JSONObject.NULL)
            .put("exact_match_organisation_name", JSONObject.NULL)
            .put("surname", JSONObject.NULL)
            .put("exact_match_surname", JSONObject.NULL)
            .put("forenames", JSONObject.NULL)
            .put("exact_match_forenames", JSONObject.NULL)
            .put("birth_date", JSONObject.NULL)
            .put("national_insurance_number", JSONObject.NULL);
    }

    private void assertResponseContainsAccountId(Response response, String accountId, String searchType) {
        assertTrue(
            accountIdsFrom(response).contains(accountId),
            "Expected search type '" + searchType + "' to return account ID " + accountId
        );
    }

    private List<String> accountIdsFrom(Response response) {
        List<String> accountIds = response.jsonPath().getList("defendant_accounts.defendant_account_id", String.class);
        assertNotNull(accountIds, "Search response must contain defendant_accounts");
        return accountIds;
    }

    private void actAs(String user) {
        BearerTokenStepDef.setTokenOverride(BearerTokenStepDef.getAccessTokenForUser(user));
        scenarioContext().setCurrentUser(user);
    }

    private String uniqueToken() {
        return Long.toString(System.nanoTime(), 36).toUpperCase(Locale.ROOT);
    }

    private String numericToken() {
        return "%06d".formatted(Math.abs(System.nanoTime()) % 1_000_000L);
    }

    private record SearchAccountData(
        String prosecutorCaseReference,
        String surname,
        String forenames,
        String nationalInsuranceNumber,
        String addressLine1,
        String postcode,
        String accountId
    ) {
        private SearchAccountData(
            String prosecutorCaseReference,
            String surname,
            String forenames,
            String nationalInsuranceNumber,
            String addressLine1,
            String postcode
        ) {
            this(prosecutorCaseReference, surname, forenames, nationalInsuranceNumber, addressLine1, postcode, null);
        }

        private SearchAccountData withAccountId(String accountId) {
            return new SearchAccountData(
                prosecutorCaseReference,
                surname,
                forenames,
                nationalInsuranceNumber,
                addressLine1,
                postcode,
                accountId
            );
        }
    }
}
