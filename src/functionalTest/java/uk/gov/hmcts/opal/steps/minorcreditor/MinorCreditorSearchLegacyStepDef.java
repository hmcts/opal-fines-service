package uk.gov.hmcts.opal.steps.minorcreditor;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.security.SecureRandom;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.workflows.defendantaccount.DefendantAccountEnforcementWorkflow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;

/**
 * Defines functional-test setup and search steps for minor-creditor searches against Legacy Mode.
 */
public class MinorCreditorSearchLegacyStepDef extends BaseStepDef {

    private static final String MINOR_CREDITOR_ACCOUNT_FIXTURE =
        "draftAccounts/accountJson/minorCreditorAccount.json";
    private static final String LEGACY_SAFE_COURT_ID = "770000000001";
    private static final String SEARCH_URL = "/minor-creditor-accounts/search";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final DefendantAccountEnforcementWorkflow accountWorkflow =
        new DefendantAccountEnforcementWorkflow();
    private final List<String> createdDefendantAccountIds = new ArrayList<>();

    private String minorCreditorForenames;
    private String minorCreditorSurname;
    private String minorCreditorAddressLine1;

    /**
     * Creates and publishes a defendant account containing an individual minor creditor.
     *
     * @throws JSONException if the publication payload cannot be assembled.
     * @throws IOException if the account fixture cannot be loaded.
     */
    @Given("a published defendant account exists with an individual minor creditor for search")
    public void publishedDefendantAccountExistsWithIndividualMinorCreditorForSearch()
        throws JSONException, IOException {
        initializeUniqueMinorCreditorDetails();
        createdDefendantAccountIds.clear();
        createAndRememberDefendantAccount();
    }

    /**
     * Creates and publishes two accounts with the same unique minor-creditor search details.
     *
     * @throws JSONException if the publication payload cannot be assembled.
     * @throws IOException if the account fixture cannot be loaded.
     */
    @Given("two published defendant accounts exist with individual minor creditors for search")
    public void twoPublishedDefendantAccountsExistWithIndividualMinorCreditorsForSearch()
        throws JSONException, IOException {
        initializeUniqueMinorCreditorDetails();
        createdDefendantAccountIds.clear();
        createAndRememberDefendantAccount();
        createAndRememberDefendantAccount();
    }

    /**
     * Searches for the individual minor creditor included in the account fixture.
     *
     * @throws JSONException if the search request body cannot be assembled.
     */
    @When("I search for the individual minor creditor created for this scenario")
    public void searchForIndividualMinorCreditorCreatedForScenario() throws JSONException {
        JSONObject creditor = new JSONObject()
            .put("organisation", false)
            .put("forenames", minorCreditorForenames)
            .put("surname", minorCreditorSurname)
            .put("address_line_1", minorCreditorAddressLine1)
            .put("postcode", "TE9 87ST")
            .put("exact_match_forenames", false)
            .put("exact_match_surname", false);

        JSONObject requestBody = new JSONObject()
            .put("active_accounts_only", true)
            .put("business_unit_ids", new JSONArray().put(77))
            .put("creditor", creditor);

        authorisedJsonRequest()
            .body(requestBody.toString())
            .when()
            .post(getTestUrl() + SEARCH_URL);
    }

    private void initializeUniqueMinorCreditorDetails() {
        String uniqueSuffix = uniqueAlphabeticSuffix();
        minorCreditorForenames = "Minor " + uniqueSuffix;
        minorCreditorSurname = "PO" + uniqueSuffix;
        minorCreditorAddressLine1 = "PO " + uniqueSuffix;
    }

    private void createAndRememberDefendantAccount() throws JSONException, IOException {
        accountWorkflow.createEnforceableDefendantAccount(Map.of(
            "business_unit_id", "77",
            "source", "MAC",
            "account", MINOR_CREDITOR_ACCOUNT_FIXTURE,
            "account_type", "Fine",
            "account_status", "Submitted",
            "account_enforcement_court_id", LEGACY_SAFE_COURT_ID,
            "minor_creditor_forenames", minorCreditorForenames,
            "minor_creditor_surname", minorCreditorSurname,
            "minor_creditor_address_line_1", minorCreditorAddressLine1
        ));
        createdDefendantAccountIds.add(scenarioContext().getCreatedDefendantAccountIdOrFail());
    }

    private String uniqueAlphabeticSuffix() {
        StringBuilder suffix = new StringBuilder(12);
        for (int index = 0; index < 12; index++) {
            suffix.append((char) ('A' + SECURE_RANDOM.nextInt(26)));
        }
        return suffix.toString();
    }

    /**
     * Verifies that the search response includes the minor creditor for the account created by
     * this scenario.
     */
    @Then("the minor creditor search response contains the created Legacy creditor")
    public void minorCreditorSearchResponseContainsCreatedLegacyCreditor() {
        String createdDefendantAccountId = createdDefendantAccountIds.get(0);

        SerenityRest.lastResponse()
            .then()
            .assertThat()
            .statusCode(200)
            .body("count", greaterThan(0))
            .body("creditor_accounts.firstnames", hasItem(minorCreditorForenames))
            .body("creditor_accounts.surname", hasItem(minorCreditorSurname))
            .body("creditor_accounts.address_line_1", hasItem(minorCreditorAddressLine1))
            .body("creditor_accounts.postcode", hasItem("TE9 87ST"))
            .body("creditor_accounts.defendant.defendant_account_id", hasItem(createdDefendantAccountId));
    }

    /**
     * Verifies that a search returns both accounts created for the same unique creditor criteria.
     */
    @Then("the minor creditor search response contains both created Legacy creditors")
    public void minorCreditorSearchResponseContainsBothCreatedLegacyCreditors() {
        if (createdDefendantAccountIds.size() != 2) {
            throw new AssertionError("Expected two published defendant accounts for the search");
        }

        SerenityRest.lastResponse()
            .then()
            .assertThat()
            .statusCode(200)
            .body("count", equalTo(2))
            .body("creditor_accounts.firstnames", hasItem(minorCreditorForenames))
            .body("creditor_accounts.surname", hasItem(minorCreditorSurname))
            .body("creditor_accounts.address_line_1", hasItem(minorCreditorAddressLine1))
            .body("creditor_accounts.postcode", hasItem("TE9 87ST"))
            .body("creditor_accounts.defendant.defendant_account_id",
                hasItem(createdDefendantAccountIds.get(0)))
            .body("creditor_accounts.defendant.defendant_account_id",
                hasItem(createdDefendantAccountIds.get(1)));
    }
}
