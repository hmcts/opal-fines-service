package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.hamcrest.Matchers;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class DefendantAccountSearchStepDef extends BaseStepDef {

    @When("I search defendant accounts with consolidation_search true using:")
    public void searchDefendantAccountsConsolidation(DataTable data) {
        Map<String, String> m = data.asMap(String.class, String.class);

        String buId = m.get("business_unit_id");
        String accountNumber = m.get("account_number");
        String consolidationSearch = m.getOrDefault("consolidation_search", "true"); // <-- key bit

        String body = """
    {
      "active_accounts_only": true,
      "business_unit_ids": [%s],
      "reference_number": {
        "account_number": "%s",
        "prosecutor_case_reference": null,
        "organisation": false
      },
      "defendant": null,
      "consolidation_search": %s
    }
    """.formatted(buId, accountNumber, consolidationSearch);

        SerenityRest.given()
            .accept("*/*")
            .header("Authorization", "Bearer " + getToken())
            .contentType("application/json")
            .body(body)
            .when()
            .post(getTestUrl() + "/defendant-accounts/search");
    }

    @Then("the response status code is {int}")
    public void responseStatusCodeIs(int statusCode) {
        then().assertThat().statusCode(statusCode);
    }

    @Then("the response content type is {string}")
    public void responseContentTypeIs(String contentType) {
        // verifies API contract + avoids false positives when server returns HTML error pages etc.
        String actual = then().extract().header("Content-Type");
        // often includes charset, so just check it starts with application/json
        if (actual != null && actual.contains(";")) {
            actual = actual.substring(0, actual.indexOf(";")).trim();
        }
        assertEquals(contentType, actual);
    }

    @Then("the response contains consolidation fields for the first result")
    public void responseContainsConsolidationFieldsForFirstResult() {
        // basic shape assertions
        Integer count;
        count = then().extract().jsonPath().getInt("count");
        assertNotNull("count should be present", count);

        // if count > 0, check the first result has the new fields
        if (count > 0) {
            Boolean hasCollectionOrder = then().extract().jsonPath().getBoolean("defendant_accounts[0].has_collection_order");
            Integer accountVersion = then().extract().jsonPath().getInt("defendant_accounts[0].account_version");

            assertNotNull("has_collection_order should be present", hasCollectionOrder);
            assertNotNull("account_version should be present", accountVersion);

            // checks + arrays exist (can be empty)
            Object checks = then().extract().jsonPath().get("defendant_accounts[0].checks");
            assertNotNull("checks should be present", checks);

            Object warnings = then().extract().jsonPath().get("defendant_accounts[0].checks.warnings");
            Object errors = then().extract().jsonPath().get("defendant_accounts[0].checks.errors");
            assertNotNull("checks.warnings should be present", warnings);
            assertNotNull("checks.errors should be present", errors);
        }
    }

    @Then("the first result has error reference {string}")
    public void firstResultHasErrorReference(String expectedRef) {
        String actualRef = then().extract().jsonPath().getString("defendant_accounts[0].checks.errors[0].reference");
        assertEquals(expectedRef, actualRef);
    }

    @Then("the defendant account search request is forbidden")
    public void assertSearchForbidden() {
        then().assertThat()
            .statusCode(403);
    }

    @Then("the defendant account search response does not include consolidation fields")
    public void responseDoesNotIncludeConsolidationFields() {
        then().assertThat().body("defendant_accounts[0].has_collection_order", Matchers.nullValue());
        then().assertThat().body("defendant_accounts[0].account_version", Matchers.nullValue());
        then().assertThat().body("defendant_accounts[0].checks", Matchers.nullValue());
    }

    @Then("the defendant account search response includes consolidation fields")
    public void responseIncludesConsolidationFields() {
        then().assertThat().body("defendant_accounts[0].has_collection_order", Matchers.notNullValue());
        then().assertThat().body("defendant_accounts[0].account_version", Matchers.notNullValue());
        then().assertThat().body("defendant_accounts[0].checks", Matchers.notNullValue());

        then().assertThat().body("defendant_accounts[0].checks.warnings", Matchers.notNullValue());
        then().assertThat().body("defendant_accounts[0].checks.errors", Matchers.notNullValue());
    }
}
