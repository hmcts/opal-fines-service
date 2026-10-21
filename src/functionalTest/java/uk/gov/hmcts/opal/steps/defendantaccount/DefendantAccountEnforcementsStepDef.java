package uk.gov.hmcts.opal.steps.defendantaccount;

import static net.serenitybdd.rest.SerenityRest.lastResponse;
import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.json.JSONException;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.workflows.defendantaccount.DefendantAccountEnforcementWorkflow;

import java.io.IOException;
import java.util.Map;

/**
 * Defines Cucumber steps for defendant-account enforcement scenarios.
 */
public class DefendantAccountEnforcementsStepDef extends BaseStepDef {
    private final DefendantAccountEnforcementWorkflow workflow = new DefendantAccountEnforcementWorkflow();
    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();

    /**
     * Retrieves enforcement status for an existing Legacy defendant account without changing its data.
     *
     * @param defendantAccountId identifier of the Legacy account to retrieve.
     */
    @When("I request legacy enforcement status for defendant account {string}")
    public void requestLegacyEnforcementStatus(String defendantAccountId) {
        authorisedJsonRequest()
            .when()
            .get(getTestUrl() + "/defendant-accounts/" + defendantAccountId + "/enforcement-status");
    }

    /**
     * Distinguishes a JSON null court from an absent property or an empty court object.
     */
    @Then("the enforcement status includes an explicitly null enforcement court")
    public void enforcementStatusIncludesNullCourt() {
        then().contentType(ContentType.JSON);
        Map<String, Object> overview = lastResponse().jsonPath().getMap("enforcement_overview");
        assertNotNull(overview, "Expected enforcement overview to remain available");
        assertTrue(overview.containsKey("enforcement_court"), "Expected enforcement_court property to be present");
        assertNull(overview.get("enforcement_court"), "Expected enforcement_court to be JSON null");
    }

    /**
     * Checks available enforcement information in the response already retrieved by the scenario.
     *
     * @param dataTable expected JSON paths and values.
     */
    @Then("the enforcement status response contains the following data")
    public void enforcementStatusResponseContains(DataTable dataTable) {
        responseAssertions.assertResponseContains(lastResponse(), dataTable.asMap(String.class, String.class));
    }

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
     * Adds an enforcement action to the defendant account created earlier in the scenario.
     *
     * @param dataTable Cucumber table containing the enforcement values for the request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I add the following enforcement action to the created defendant account")
    public void addEnforcementToCreatedDefendantAccount(DataTable dataTable) throws JSONException {
        workflow.addEnforcement(dataTable.asMap(String.class, String.class));
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
