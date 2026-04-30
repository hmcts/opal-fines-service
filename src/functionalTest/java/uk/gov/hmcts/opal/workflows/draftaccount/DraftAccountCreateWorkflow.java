package uk.gov.hmcts.opal.workflows.draftaccount;

import io.restassured.response.Response;
import java.io.IOException;
import java.util.Map;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountActions;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;
import uk.gov.hmcts.opal.assertions.draftaccount.DraftAccountAssertions;

/**
 * Coordinates draft-account creation workflows used by feature setup and happy-path create
 * assertions.
 */
public class DraftAccountCreateWorkflow {
    private final DraftAccountActions actions = new DraftAccountActions();
    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();
    private final DraftAccountAssertions assertions = new DraftAccountAssertions();

    /**
     * Creates a draft account, asserts that the API accepted it, and stores the generated
     * identifier for later steps.
     *
     * @param accountData field values used to build the create request.
     * @throws JSONException if the JSON request body cannot be assembled.
     * @throws IOException if a supporting draft-account fixture cannot be loaded.
     */
    public void createDraftAccountAndStoreId(Map<String, String> accountData) throws JSONException, IOException {
        Response response = actions.createDraftAccount(accountData);
        responseAssertions.assertStatus(response, 201);
        actions.storeCreatedDraftAccountId(response);
    }

    /**
     * Creates a draft account, stores the generated identifier, and remembers the timestamps needed
     * for later replace-flow assertions.
     *
     * @param accountData field values used to build the create request.
     * @throws JSONException if the JSON request body cannot be assembled.
     * @throws IOException if a supporting draft-account fixture cannot be loaded.
     */
    public void createDraftAccountAndPrepareForReplacement(Map<String, String> accountData)
        throws JSONException, IOException {
        Response response = actions.createDraftAccount(accountData);
        responseAssertions.assertStatus(response, 201);
        actions.storeCreatedDraftAccountId(response);
        actions.storeDraftAccountCreatedAtTime(response);
        actions.storeInitialAccountStatusDate(response);
    }

    /**
     * Asserts that the most recent draft-account create response represents a successful create
     * operation with the expected body fields.
     *
     * @param expectedData field names and values expected in the create response body.
     */
    public void assertLastCreateSucceeded(Map<String, String> expectedData) {
        Response response = SerenityRest.lastResponse();
        assertions.assertCreatedDraftAccount(response, expectedData);
        actions.storeCreatedDraftAccountId(response);
    }
}
