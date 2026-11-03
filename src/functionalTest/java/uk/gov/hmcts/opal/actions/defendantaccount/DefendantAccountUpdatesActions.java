package uk.gov.hmcts.opal.actions.defendantaccount;

import io.restassured.response.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Encapsulates fixture-backed defendant-account update requests for functional tests.
 */
public class DefendantAccountUpdatesActions extends BaseStepDef {
    private static final String DEFENDANT_ACCOUNTS_URI = "/defendant-accounts";
    private static final String BUSINESS_UNIT_ID = "77";
    private static final String FIXTURE_RESOURCE_ROOT = "features/";

    private final DefendantAccountEnforcementsActions enforcementActions =
        new DefendantAccountEnforcementsActions();
    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();

    /**
     * Loads a request fixture, obtains the current ETag, and patches the defendant account created
     * during this scenario.
     *
     * @param fixturePath fixture path relative to the functional-test features resource directory.
     * @throws IOException if the fixture cannot be read.
     * @throws JSONException if the fixture is not valid JSON.
     */
    public void patchCreatedDefendantAccountUsingFixture(String fixturePath) throws IOException, JSONException {
        Response enforcementStatus = enforcementActions.getCreatedDefendantAccountEnforcementStatus();
        responseAssertions.assertStatus(enforcementStatus, 200);

        JSONObject requestBody = new JSONObject(readFixture(fixturePath));
        Response patchResponse = authorisedJsonRequest()
            .header("Business-Unit-Id", BUSINESS_UNIT_ID)
            .header("If-Match", scenarioContext().getDefendantAccountEtag())
            .body(requestBody.toString())
            .when()
            .patch(getTestUrl() + DEFENDANT_ACCOUNTS_URI + "/"
                + scenarioContext().getCreatedDefendantAccountIdOrFail());

        responseAssertions.assertStatus(patchResponse, 200);
    }

    /**
     * Retrieves the at-a-glance response for the defendant account created during this scenario.
     *
     * @return at-a-glance response for the created defendant account.
     */
    public Response getCreatedDefendantAccountAtAGlance() {
        return authorisedJsonRequest()
            .when()
            .get(getTestUrl() + DEFENDANT_ACCOUNTS_URI + "/"
                + scenarioContext().getCreatedDefendantAccountIdOrFail() + "/at-a-glance");
    }

    /**
     * Asserts that the at-a-glance comments and notes match all values in the request fixture,
     * including their character lengths.
     *
     * @param response at-a-glance response to check.
     * @param fixturePath fixture path relative to the functional-test features resource directory.
     * @throws IOException if the fixture cannot be read.
     * @throws JSONException if the fixture or response is not valid JSON.
     */
    public void assertAtAGlanceCommentsAndNotesMatchFixture(Response response, String fixturePath)
        throws IOException, JSONException {
        responseAssertions.assertStatus(response, 200);

        JSONObject expectedValues = new JSONObject(readFixture(fixturePath)).getJSONObject("comment_and_notes");
        JSONObject actualValues = new JSONObject(response.getBody().asString()).getJSONObject("comments_and_notes");

        for (String field : List.of(
            "account_comment", "free_text_note_1", "free_text_note_2", "free_text_note_3"
        )) {
            String expected = expectedValues.getString(field);
            String actual = actualValues.getString(field);
            assertEquals(expected, actual, "Unexpected at-a-glance value for " + field);
            assertEquals(expected.length(), actual.length(), "Unexpected at-a-glance length for " + field);
        }
    }

    private String readFixture(String fixturePath) throws IOException {
        String resourcePath = FIXTURE_RESOURCE_ROOT + fixturePath;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Request fixture not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
