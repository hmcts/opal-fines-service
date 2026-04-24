package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.json.JSONException;
import uk.gov.hmcts.opal.actions.defendantaccount.DefendantAccountEnforcementsActions;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Defines Cucumber steps for defendant-account enforcement scenarios.
 */
public class DefendantAccountEnforcementsStepDef {

    private final DefendantAccountEnforcementsActions actions = new DefendantAccountEnforcementsActions();

    /**
     * Stores the defendant-account ID returned in the latest draft-account response.
     */
    @Then("I store the created defendant account ID from the draft account response")
    public void storeCreatedDefendantAccountIdFromDraftAccountResponse() {
        actions.storeCreatedDefendantAccountIdFromLastResponse();
    }

    /**
     * Retrieves the created defendant-account enforcement status.
     */
    @When("I get the created defendant account enforcement status")
    public void getCreatedDefendantAccountEnforcementStatus() {
        actions.getCreatedDefendantAccountEnforcementStatus();
    }

    /**
     * Updates the enforcement override for the defendant account created earlier in the scenario.
     *
     * @param dataTable Cucumber table containing the override values for the request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I patch the created defendant account enforcement override with the following details")
    public void patchCreatedDefendantAccountEnforcementOverride(DataTable dataTable) throws JSONException {
        actions.patchCreatedDefendantAccountEnforcementOverride(dataTable.asMap(String.class, String.class));
    }

    /**
     * Asserts that the enforcement-status response returned the expected HTTP status code.
     *
     * @param statusCode expected HTTP status code.
     */
    @Then("The defendant account enforcement response returns {int}")
    public void defendantAccountEnforcementResponseReturns(int statusCode) {
        then().assertThat().statusCode(statusCode);
    }

    /**
     * Asserts that the enforcement response body contains the expected field values.
     *
     * @param dataTable Cucumber table containing the expected values for the assertion.
     */
    @Then("The defendant account enforcement response contains")
    public void defendantAccountEnforcementResponseContains(DataTable dataTable) {
        Map<String, String> expectedData = dataTable.asMap(String.class, String.class);

        for (Map.Entry<String, String> entry : expectedData.entrySet()) {
            String actual = then().extract().body().jsonPath().getString(entry.getKey());
            assertEquals(entry.getValue(), actual, "Values are not equal for field '" + entry.getKey() + "'");
        }
    }
}
