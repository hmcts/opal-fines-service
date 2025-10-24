package uk.gov.hmcts.opal.steps.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.utils.DraftAccountUtils;
import uk.gov.hmcts.opal.steps.draftaccount.DraftAccountDeleteSteps;

public class DataCleanUp extends BaseStepDef {

    private static final Logger log = LoggerFactory.getLogger(DataCleanUp.class);

    // Use an instance
    private final DraftAccountDeleteSteps deleter = new DraftAccountDeleteSteps();

    @Before(order = Integer.MIN_VALUE)
    public void resetPerScenarioState() {
        DraftAccountUtils.clearDraftAccountIds();
        DraftAccountUtils.clearDraftAccountCreatedAtTime();
        DraftAccountUtils.clearInitialAccountStatusDate();
    }

    @After(order = Integer.MAX_VALUE)
    public void cleanUpData() {
        try {
            // ignoreMissing=true so pipelines don’t fail if the record was already deleted
            deleter.actualDeleteAllCreatedDraftAccounts(true);
        } catch (AssertionError | RuntimeException ex) {
            log.error("Draft account cleanup encountered an error: {}", ex.getMessage(), ex);
        } finally {
            // Always clear the recorded IDs/timestamps so the next scenario starts clean
            DraftAccountUtils.clearDraftAccountIds();
            DraftAccountUtils.clearDraftAccountCreatedAtTime();
            DraftAccountUtils.clearInitialAccountStatusDate();
        }
    }
}
