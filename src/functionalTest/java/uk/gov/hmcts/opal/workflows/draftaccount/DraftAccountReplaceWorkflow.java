package uk.gov.hmcts.opal.workflows.draftaccount;

import io.restassured.response.Response;
import java.util.Map;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountActions;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;

/**
 * Coordinates draft-account replace workflows that combine a successful replace with a follow-up
 * retrieval assertion.
 */
public class DraftAccountReplaceWorkflow {
    private final DraftAccountActions actions = new DraftAccountActions();
    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();

    /**
     * Asserts that the latest replace succeeded and that the updated draft account can be
     * retrieved with the expected values.
     *
     * @param expectedData field names and values expected when the replaced draft account is
     *                     retrieved.
     */
    public void assertReplacedDraftAccountCanBeRetrieved(Map<String, String> expectedData) {
        Response putResponse = net.serenitybdd.rest.SerenityRest.lastResponse();
        responseAssertions.assertStatus(putResponse, 200);

        Response getResponse = actions.getSingleCreatedDraftAccount();
        responseAssertions.assertStatus(getResponse, 200);
        responseAssertions.assertResponseContains(getResponse, expectedData);
    }
}
