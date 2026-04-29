package uk.gov.hmcts.opal.utils;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountCleanupActions;
import uk.gov.hmcts.opal.context.ScenarioContextHolder;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.defendantaccount.DefendantAccountDeleteStep;

/**
 * Cucumber hooks that reset and clean up shared draft-account scenario state.
 */
public class DataCleanUp extends BaseStepDef {

    private static final Logger log = LoggerFactory.getLogger(DataCleanUp.class);

    // Use instances so the existing step logic can be reused from the hooks.
    private final DefendantAccountDeleteStep defendantAccountDeleter = new DefendantAccountDeleteStep();
    private final DraftAccountCleanupActions draftAccountCleanupActions = new DraftAccountCleanupActions();

    /**
     * Clears draft-account IDs and timestamps before each scenario starts.
     */
    @Before(order = Integer.MIN_VALUE)
    public void resetPerScenarioState() {
        ScenarioContextHolder.reset();
    }

    /**
     * Attempts to remove any draft accounts created during the scenario and always clears the
     * remembered scenario state afterwards.
     */
    @After(order = Integer.MAX_VALUE)
    public void cleanUpData() {
        try {
            // Cleanup should always run with the default authenticated test user.
            BearerTokenStepDef.clearTokenOverride();
            defendantAccountDeleter.deleteCreatedDefendantAccounts();

            // ignoreMissing=true so pipelines don’t fail if the record was already deleted
            draftAccountCleanupActions.deleteAllCreatedDraftAccounts(true);
        } catch (AssertionError | RuntimeException ex) {
            log.error("Draft account cleanup encountered an error: {}", ex.getMessage(), ex);
        } finally {
            // Always clear the recorded scenario context so the next scenario starts clean
            ScenarioContextHolder.clear();
        }
    }
}
