package uk.gov.hmcts.opal.utils;

import io.cucumber.java.After;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.draftaccount.DraftAccountDeleteSteps;

public class DataCleanUp extends BaseStepDef {

    @After("@cleanUpData")
    public void cleanUpData() {
        DraftAccountDeleteSteps.deleteAllCreatedDraftAccounts();
        DraftAccountUtils.clearDraftAccountIds();
        DraftAccountUtils.clearDraftAccountCreatedAtTime();
    }
}
