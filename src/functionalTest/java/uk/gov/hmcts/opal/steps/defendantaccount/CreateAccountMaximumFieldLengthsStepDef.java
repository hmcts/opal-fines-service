package uk.gov.hmcts.opal.steps.defendantaccount;

import static net.serenitybdd.rest.SerenityRest.given;
import static net.serenitybdd.rest.SerenityRest.lastResponse;
import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;

/**
 * Read-back checks used by the maximum-length account creation journeys.
 */
public class CreateAccountMaximumFieldLengthsStepDef extends BaseStepDef {

    /** Requests an additional GET representation for the account created in this scenario.
     *
     * @param endpoint endpoint suffix supported for account read-back.
     */
    @When("I request the created defendant account {string} response")
    public void requestCreatedDefendantAccountResponse(String endpoint) {
        if (!java.util.Set.of("at-a-glance", "impositions", "payment-terms/latest", "fixed-penalty", "history")
            .contains(endpoint)) {
            throw new IllegalArgumentException("Unsupported defendant-account read-back endpoint: " + endpoint);
        }
        given()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + BearerTokenStepDef.getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + "/defendant-accounts/"
                + scenarioContext().getCreatedDefendantAccountIdOrFail() + "/" + endpoint);
        then().log().body();
    }

    /** Requests defendant party details using its association ID from the account header summary. */
    @When("I request the created defendant account party details")
    public void requestCreatedDefendantAccountPartyDetails() {
        String accountId = scenarioContext().getCreatedDefendantAccountIdOrFail();
        Response headerSummary = given()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + BearerTokenStepDef.getToken())
            .accept("*/*")
            .when()
            .get(getTestUrl() + "/defendant-accounts/" + accountId + "/header-summary");
        assertEquals(200, headerSummary.statusCode(), "Expected header-summary request to succeed");

        String defendantAccountPartyId = headerSummary.jsonPath().getString("defendant_account_party_id");
        assertNotNull(defendantAccountPartyId, "Header summary must include defendant_account_party_id");
        Response partyDetailsResponse = given()
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + BearerTokenStepDef.getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + "/defendant-accounts/" + accountId
                + "/defendant-account-parties/" + defendantAccountPartyId);
        partyDetailsResponse.then().log().body();
    }

    /** Checks exact response values and their returned character lengths.
     *
     * @param values table with JSON path, expected value and expected length columns.
     */
    @Then("the created defendant account response contains these exact values and lengths")
    public void createdDefendantAccountResponseContainsExactValuesAndLengths(DataTable values) {
        Response response = lastResponse();
        assertEquals(200, response.statusCode(), "Unexpected defendant-account read-back status");
        for (Map<String, String> row : values.asMaps(String.class, String.class)) {
            String path = row.get("json_path");
            String expected = row.get("expected");
            int expectedLength = Integer.parseInt(row.get("length"));
            String actual = response.jsonPath().getString(path);
            assertEquals(expected, actual, "Unexpected value at " + path);
            assertEquals(expectedLength, actual.length(), "Unexpected length at " + path);
        }
    }

    /** Checks exact numeric response values without treating their JSON representation as text length. */
    @Then("the created defendant account response contains these exact numeric values")
    public void createdDefendantAccountResponseContainsExactNumericValues(DataTable values) {
        Response response = lastResponse();
        assertEquals(200, response.statusCode(), "Unexpected defendant-account read-back status");
        for (Map<String, String> row : values.asMaps(String.class, String.class)) {
            String path = row.get("json_path");
            BigDecimal expected = new BigDecimal(row.get("expected"));
            BigDecimal actual = new BigDecimal(response.jsonPath().getString(path));
            assertEquals(0, expected.compareTo(actual), "Unexpected numeric value at " + path);
        }
    }
}
