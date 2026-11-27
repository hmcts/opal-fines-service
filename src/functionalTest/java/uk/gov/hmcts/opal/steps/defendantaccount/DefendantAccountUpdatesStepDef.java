package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.json.JSONException;
import uk.gov.hmcts.opal.actions.defendantaccount.DefendantAccountUpdatesActions;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import java.io.IOException;

/**
 * Defines Cucumber steps for defendant-account updates.
 */
public class DefendantAccountUpdatesStepDef extends BaseStepDef {
    private final DefendantAccountUpdatesActions actions = new DefendantAccountUpdatesActions();
    private Response atAGlanceResponse;

    /**
     * Patches the created defendant account with the supplied JSON request fixture.
     *
     * @param fixturePath classpath-relative request fixture under features/.
     * @throws IOException if the request fixture cannot be read.
     * @throws JSONException if the request fixture is not valid JSON.
     */
    @When("I patch the created defendant account using fixture {string}")
    public void patchCreatedDefendantAccountUsingFixture(String fixturePath) throws IOException, JSONException {
        actions.patchCreatedDefendantAccountUsingFixture(fixturePath);
    }

    /**
     * Retrieves the at-a-glance response for the defendant account created in this scenario.
     */
    @When("I request at-a-glance for the created defendant account")
    public void requestAtAGlanceForCreatedDefendantAccount() {
        atAGlanceResponse = actions.getCreatedDefendantAccountAtAGlance();
    }

    /**
     * Asserts that comments and notes in the at-a-glance response match the supplied request fixture.
     *
     * @param fixturePath classpath-relative request fixture under features/.
     * @throws IOException if the request fixture cannot be read.
     * @throws JSONException if the request fixture or response is not valid JSON.
     */
    @Then("the at-a-glance response contains comments and notes from fixture {string}")
    public void atAGlanceResponseContainsCommentsAndNotesFromFixture(String fixturePath)
        throws IOException, JSONException {
        actions.assertAtAGlanceCommentsAndNotesMatchFixture(atAGlanceResponse, fixturePath);
    }
}
