package uk.gov.hmcts.opal.utils;

import io.cucumber.java.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.steps.BaseStepDef;
// adjust this import to match your actual package for the step class
import uk.gov.hmcts.opal.steps.draftaccount.DraftAccountDeleteSteps;

public class DataCleanUp extends BaseStepDef {

    private static final Logger log = LoggerFactory.getLogger(DataCleanUp.class);

    // Use an instance
    private final DraftAccountDeleteSteps deleter = new DraftAccountDeleteSteps();

    @After("@cleanUpData")
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
