package uk.gov.hmcts.opal.workflows.draftaccount;

import io.restassured.response.Response;
import java.util.Map;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountActions;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;

/**
 * Coordinates draft-account patch workflows that combine a successful mutation with a follow-up
 * retrieval assertion.
 */
public class DraftAccountPatchWorkflow {
    private final DraftAccountActions actions = new DraftAccountActions();
    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();

    /**
     * Asserts that the latest patch succeeded and that the updated draft account can be retrieved
     * with the expected values.
     *
     * @param expectedData field names and values expected when the updated draft account is
     *                     retrieved.
     */
    public void assertPatchedDraftAccountCanBeRetrieved(Map<String, String> expectedData) {
        Response patchResponse = net.serenitybdd.rest.SerenityRest.lastResponse();
        responseAssertions.assertStatus(patchResponse, 200);

        Response getResponse = actions.getSingleCreatedDraftAccount();
        responseAssertions.assertStatus(getResponse, 200);
        responseAssertions.assertResponseContains(getResponse, expectedData);
    }
}
