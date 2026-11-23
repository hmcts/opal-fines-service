package uk.gov.hmcts.opal.steps.minorcreditor;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Defines functional steps for verifying note poster details in minor-creditor account history.
 */
public class MinorCreditorAccountHistoryNoteStepDef extends BaseStepDef {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final long HISTORY_FIXTURE_BUSINESS_UNIT_ID = 77L;
    private static final String HISTORY_NOTE_TEXT = "PO-11203 minor creditor history note";
    private static final String MINOR_CREDITOR_ACCOUNT_PATH = "/minor-creditor-accounts/%d";
    private static final String HEADER_SUMMARY_PATH = MINOR_CREDITOR_ACCOUNT_PATH + "/header-summary";
    private static final String USER_STATE_PATH = "/testing-support/user-client/0";

    private String currentBusinessUnitUserId;
    private String currentUserDisplayName;

    /**
     * Adds an activity note to the minor-creditor account created for the current scenario.
     *
     * @throws JSONException if the note request cannot be created.
     */
    @When("I add a note to the created minor creditor account")
    public void addNoteToCreatedMinorCreditorAccount() throws JSONException {
        long accountId = scenarioContext().getCreatedMinorCreditorAccountIdOrFail();
        String token = BearerTokenStepDef.getToken();
        JsonNode userState = currentUserState();
        currentBusinessUnitUserId = businessUnitUserId(userState);
        currentUserDisplayName = displayName(userState);

        Response summaryResponse = SerenityRest.given()
            .accept("*/*")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .when()
            .get(getTestUrl() + HEADER_SUMMARY_PATH.formatted(accountId));

        assertEquals(200, summaryResponse.statusCode(), "Unexpected minor-creditor header-summary status");
        String etag = summaryResponse.header(HttpHeaders.ETAG);
        assertNotNull(etag, "The minor-creditor header summary should contain an ETag");

        JSONObject note = new JSONObject()
            .put("note_text", HISTORY_NOTE_TEXT)
            .put("record_id", Long.toString(accountId))
            .put("record_type", "creditor_accounts")
            .put("note_type", "AA");
        JSONObject requestBody = new JSONObject().put("activity_note", note);

        Response response = SerenityRest.given()
            .accept("*/*")
            .contentType("application/json")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .header(HttpHeaders.IF_MATCH, etag)
            .header("Business-Unit-Id", HISTORY_FIXTURE_BUSINESS_UNIT_ID)
            .body(requestBody.toString())
            .when()
            .post(getTestUrl() + "/notes");

        assertEquals(201, response.statusCode(), "Unexpected response when adding the history note");
    }

    /**
     * Asserts history attributes the new note to the current user's business-unit user ID.
     */
    @Then("the new minor creditor history note shows the current business-unit user ID and display name")
    public void historyNoteShowsBusinessUnitUserIdAndDisplayName() {
        assertNewNotePoster(currentBusinessUnitUserId, currentUserDisplayName);
    }

    /**
     * Asserts history attributes the new note to the numeric business-unit ID when the child
     * feature toggle is disabled.
     */
    @Then("the new minor creditor history note shows the numeric business-unit ID and display name")
    public void historyNoteShowsNumericBusinessUnitIdAndDisplayName() {
        assertNewNotePoster(Long.toString(HISTORY_FIXTURE_BUSINESS_UNIT_ID), currentUserDisplayName);
    }

    private JsonNode currentUserState() {
        Response response = SerenityRest.given()
            .accept("*/*")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + BearerTokenStepDef.getToken())
            .when()
            .get(getTestUrl() + USER_STATE_PATH);

        assertEquals(200, response.statusCode(), "Unexpected current-user-state status");
        try {
            return OBJECT_MAPPER.readTree(response.getBody().asString());
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse current user state", exception);
        }
    }

    private String businessUnitUserId(JsonNode userState) {
        JsonNode users = userState.path("business_unit_user");
        assertTrue(users.isArray(), "Current user state should contain business_unit_user entries");

        for (JsonNode user : users) {
            if (user.path("business_unit_id").asLong() == HISTORY_FIXTURE_BUSINESS_UNIT_ID) {
                return requiredText(user.path("business_unit_user_id"), "business_unit_user_id");
            }
        }

        throw new AssertionError(
            "Current user state does not contain a business-unit user for business unit "
                + HISTORY_FIXTURE_BUSINESS_UNIT_ID
        );
    }

    private String displayName(JsonNode userState) {
        JsonNode name = userState.path("name");
        if (name.isString() && !name.asString().isBlank()) {
            return name.asString();
        }
        return requiredText(userState.path("user_name"), "user_name");
    }

    private void assertNewNotePoster(String expectedPostedBy, String expectedPostedByName) {
        JsonNode history = latestJsonBody();
        JsonNode historyItems = history.path("historyItems");
        assertTrue(historyItems.isArray(), "historyItems should be an array");

        int matchingNoteCount = 0;
        for (JsonNode item : historyItems) {
            if (HISTORY_NOTE_TEXT.equals(item.path("details").path("noteText").asString())) {
                matchingNoteCount++;
                JsonNode postedDetails = item.path("postedDetails");
                assertEquals(expectedPostedBy, requiredText(postedDetails.path("posted_by"), "posted_by"));
                assertEquals(expectedPostedByName,
                    requiredText(postedDetails.path("posted_by_name"), "posted_by_name"));
            }
        }

        assertEquals(1, matchingNoteCount, "Expected exactly one matching minor-creditor history note");
    }

    private JsonNode latestJsonBody() {
        try {
            return OBJECT_MAPPER.readTree(SerenityRest.lastResponse().getBody().asString());
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse minor-creditor history response", exception);
        }
    }

    private String requiredText(JsonNode node, String fieldName) {
        assertTrue(node.isString() && !node.asString().isBlank(), fieldName + " should be a non-blank string");
        return node.asString();
    }
}
