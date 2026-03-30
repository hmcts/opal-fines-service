package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.json.JSONException;
import uk.gov.hmcts.opal.actions.defendantaccount.DefendantAccountEnforcementsActions;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefendantAccountEnforcementsStepDef {

    private final DefendantAccountEnforcementsActions actions = new DefendantAccountEnforcementsActions();

    @Then("I store the created defendant account ID from the draft account response")
    public void storeCreatedDefendantAccountIdFromDraftAccountResponse() {
        actions.storeCreatedDefendantAccountIdFromLastResponse();
    }

    @When("I get the created defendant account enforcement status")
    public void getCreatedDefendantAccountEnforcementStatus() {
        actions.getCreatedDefendantAccountEnforcementStatus();
    }

    @When("I patch the created defendant account enforcement override with the following details")
    public void patchCreatedDefendantAccountEnforcementOverride(DataTable dataTable) throws JSONException {
        actions.patchCreatedDefendantAccountEnforcementOverride(dataTable.asMap(String.class, String.class));
    }

    @Then("The defendant account enforcement response returns {int}")
    public void defendantAccountEnforcementResponseReturns(int statusCode) {
        then().assertThat().statusCode(statusCode);
    }

    @Then("The defendant account enforcement response contains")
    public void defendantAccountEnforcementResponseContains(DataTable dataTable) {
        Map<String, String> expectedData = dataTable.asMap(String.class, String.class);

        for (Map.Entry<String, String> entry : expectedData.entrySet()) {
            String actual = then().extract().body().jsonPath().getString(entry.getKey());
            assertEquals(entry.getValue(), actual, "Values are not equal for field '" + entry.getKey() + "'");
        }
    }
}
