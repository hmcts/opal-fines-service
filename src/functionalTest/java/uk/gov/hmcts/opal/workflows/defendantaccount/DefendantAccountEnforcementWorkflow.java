package uk.gov.hmcts.opal.workflows.defendantaccount;

import io.restassured.response.Response;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONException;
import uk.gov.hmcts.opal.actions.defendantaccount.DefendantAccountEnforcementsActions;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountActions;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;

/**
 * Coordinates the cross-API workflow needed to publish a draft account into a defendant account
 * and then apply and verify an enforcement override.
 */
public class DefendantAccountEnforcementWorkflow extends BaseStepDef {
    private static final String REVIEWING_USER = "opal-test-10@dev.platform.hmcts.net";

    private final DraftAccountActions draftAccountActions = new DraftAccountActions();
    private final DefendantAccountEnforcementsActions enforcementActions = new DefendantAccountEnforcementsActions();
    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();

    /**
     * Creates a draft account, publishes it as a defendant account through the reviewing-user
     * path, and remembers the resulting defendant-account ID for later enforcement steps.
     *
     * @param draftAccountData field values used to create the draft account before publication.
     * @throws JSONException if the draft-account publication payload cannot be created.
     * @throws IOException if a supporting draft-account fixture cannot be loaded.
     */
    public void createEnforceableDefendantAccount(Map<String, String> draftAccountData)
        throws JSONException, IOException {
        String originalUser = scenarioContext().getCurrentUserOrDefault(BearerTokenStepDef.DEFAULT_USER);

        Response createResponse = draftAccountActions.createDraftAccount(draftAccountData);
        responseAssertions.assertStatus(createResponse, 201);
        draftAccountActions.storeCreatedDraftAccountId(createResponse);

        actAs(REVIEWING_USER);
        try {
            Response publishResponse = draftAccountActions.patchCreatedDraftAccount(
                buildPublishPatchData(draftAccountData)
            );
            responseAssertions.assertStatus(publishResponse, 200);
            enforcementActions.storeCreatedDefendantAccountId(publishResponse);
        } finally {
            actAs(originalUser);
        }
    }

    /**
     * Applies an enforcement override to the defendant account created earlier in the scenario.
     * The workflow first retrieves the current enforcement status so it can reuse the returned
     * ETag when issuing the patch request.
     *
     * @param overrideData field values used to build the enforcement-override payload.
     * @throws JSONException if the enforcement-override payload cannot be created.
     */
    public void applyEnforcementOverride(Map<String, String> overrideData) throws JSONException {
        Response getResponse = enforcementActions.getCreatedDefendantAccountEnforcementStatus();
        responseAssertions.assertStatus(getResponse, 200);

        Response patchResponse = enforcementActions.patchCreatedDefendantAccountEnforcementOverride(overrideData);
        responseAssertions.assertStatus(patchResponse, 200);
    }

    /**
     * Retrieves the latest defendant-account enforcement status and asserts that it contains the
     * expected override values.
     *
     * @param expectedData field names and values expected in the enforcement-status response.
     */
    public void assertEnforcementStatusContains(Map<String, String> expectedData) {
        Response response = enforcementActions.getCreatedDefendantAccountEnforcementStatus();
        responseAssertions.assertStatus(response, 200);
        responseAssertions.assertResponseContains(response, expectedData);
    }

    /**
     * Builds the draft-account patch payload needed to publish the created draft account into a
     * defendant account that can be used by the enforcement API.
     *
     * @param draftAccountData field values used to create the source draft account.
     * @return patch payload for the publication step.
     */
    private Map<String, String> buildPublishPatchData(Map<String, String> draftAccountData) {
        Map<String, String> patchData = new LinkedHashMap<>();
        patchData.put("business_unit_id", draftAccountData.get("business_unit_id"));
        patchData.put("account_status", "Publishing Pending");
        patchData.put("validated_by", draftAccountData.get("submitted_by") + "_REVIEWER");
        patchData.put("If-Match", "0");
        return patchData;
    }

    /**
     * Switches the current scenario to act as the supplied user.
     *
     * @param user user alias or email to use for subsequent API calls.
     */
    private void actAs(String user) {
        BearerTokenStepDef.setTokenOverride(BearerTokenStepDef.getAccessTokenForUser(user));
        scenarioContext().setCurrentUser(user);
    }
}
