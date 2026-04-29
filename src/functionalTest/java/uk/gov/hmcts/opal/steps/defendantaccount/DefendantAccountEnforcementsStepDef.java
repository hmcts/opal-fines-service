package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.json.JSONException;
import uk.gov.hmcts.opal.workflows.defendantaccount.DefendantAccountEnforcementWorkflow;

import java.io.IOException;
import java.util.Map;

/**
 * Defines Cucumber steps for defendant-account enforcement scenarios.
 */
public class DefendantAccountEnforcementsStepDef {
    private final DefendantAccountEnforcementWorkflow workflow = new DefendantAccountEnforcementWorkflow();

    /**
     * Creates and publishes a draft account so the scenario has a defendant account available for
     * enforcement-override operations.
     *
     * @param dataTable Cucumber table containing the source draft-account values for setup.
     * @throws JSONException if the setup payload cannot be created from the supplied values.
     * @throws IOException if a supporting draft-account fixture cannot be loaded.
     */
    @Given("an enforceable defendant account exists with the following details")
    public void enforceableDefendantAccountExists(DataTable dataTable) throws JSONException, IOException {
        workflow.createEnforceableDefendantAccount(dataTable.asMap(String.class, String.class));
    }

    /**
     * Applies an enforcement override to the defendant account created earlier in the scenario.
     *
     * @param dataTable Cucumber table containing the override values for the request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I apply the following enforcement override to the created defendant account")
    public void applyEnforcementOverrideToCreatedDefendantAccount(DataTable dataTable) throws JSONException {
        workflow.applyEnforcementOverride(dataTable.asMap(String.class, String.class));
    }

    /**
     * Asserts that the latest enforcement status contains the expected override values.
     *
     * @param dataTable Cucumber table containing the expected values for the assertion.
     */
    @Then("the created defendant account enforcement status contains the following data")
    public void createdDefendantAccountEnforcementStatusContains(DataTable dataTable) {
        Map<String, String> expectedData = dataTable.asMap(String.class, String.class);
        workflow.assertEnforcementStatusContains(expectedData);
    }
}
