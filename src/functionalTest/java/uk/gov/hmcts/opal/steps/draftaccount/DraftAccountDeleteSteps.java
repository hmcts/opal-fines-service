package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountCleanupActions;
import uk.gov.hmcts.opal.steps.BaseStepDef;

/**
 * Draft account deletion steps with ETag/If-Match support and verified cleanup.
 */
public class DraftAccountDeleteSteps extends BaseStepDef {
    private final DraftAccountCleanupActions cleanupActions = new DraftAccountCleanupActions();

    /**
     * Deletes the last created draft-account using concurrency control.
     */
    @When("I delete the created draft account using optimistic locking")
    public void deleteLastCreatedWithIfMatch() {
        cleanupActions.deleteLastCreatedDraftAccount(false);
    }

    /**
     * Deletes the last created draft-account ignoring missing resource.
     */
    @When("I delete the created draft account again, ignoring a missing resource")
    public void deleteLastCreatedIgnoringMissingResource() {
        cleanupActions.deleteLastCreatedDraftAccount(true);
    }

    /**
     * Asserts that the last created draft-account can no longer be retrieved.
     */
    @Then("the created draft account is deleted")
    public void createdDraftAccountIsDeleted() {
        cleanupActions.assertLastCreatedDraftAccountDeleted();
    }

    /**
     * Deletes every draft account recorded for the current scenario.
     */
    @Then("I delete the created draft accounts")
    public void deleteAllCreatedDraftAccounts() {
        cleanupActions.deleteAllCreatedDraftAccounts(false);
    }
}
