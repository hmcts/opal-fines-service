package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;

/**
 * Defines feature-toggle request steps for release-1c-write-off defendant account search behaviour.
 */
public class Release1cFeatureToggleStepDef extends BaseStepDef {

    private static final String DEFENDANT_ACCOUNT_SEARCH_URI = "/defendant-accounts/search";

    /**
     * Calls defendant account search with the release-1c-write-off consolidated search parameter enabled.
     */
    @When("I call the defendant account search endpoint with consolidated search enabled")
    public void callDefendantAccountSearchWithConsolidatedSearchEnabled() {
        callDefendantAccountSearch(true);
    }

    /**
     * Calls defendant account search without requesting release-1c-write-off consolidated search behaviour.
     */
    @When("I call the defendant account search endpoint without consolidated search")
    public void callDefendantAccountSearchWithoutConsolidatedSearch() {
        callDefendantAccountSearch(false);
    }

    /**
     * Asserts that the latest problem response reports a release-1c-write-off feature-toggle rejection.
     */
    @Then("the release 1c write-off feature-disabled response is returned")
    public void release1cFeatureDisabledResponseIsReturned() {
        then()
            .log().ifValidationFails()
            .body("title", equalTo("Feature Disabled"));
    }

    private void callDefendantAccountSearch(boolean consolidationSearch) {
        authorisedJsonRequest()
            .body(searchCriteria(consolidationSearch))
            .when()
            .post(getTestUrl() + DEFENDANT_ACCOUNT_SEARCH_URI);
    }

    private String searchCriteria(boolean consolidationSearch) {
        return """
            {
              "active_accounts_only": true,
              "business_unit_ids": [78],
              "reference_number": null,
              "defendant": {
                "include_aliases": true,
                "organisation": false,
                "address_line_1": "Lumber",
                "postcode": "MA4 1AL",
                "organisation_name": null,
                "exact_match_organisation_name": null,
                "surname": "Graham",
                "exact_match_surname": true,
                "forenames": "Anna",
                "exact_match_forenames": true,
                "birth_date": "1980-02-03",
                "national_insurance_number": "A11111A"
              },
              "consolidation_search": %s
            }
            """.formatted(consolidationSearch);
    }
}
