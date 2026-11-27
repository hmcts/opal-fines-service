package uk.gov.hmcts.opal.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

/**
 * Verifies maximum-length activity notes through the notes API and account history.
 */
public class ActivityNoteStepDef extends BaseStepDef {

    private static final int MAX_NOTE_LENGTH = 1000;
    // Both account fixtures belong to business unit 77.
    private static final int FIXTURE_BUSINESS_UNIT_ID = 77;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String noteMarker;
    private String submittedNoteText;

    /**
     * Builds a distinct fixture so history can identify this submission independently of its full content.
     */
    @Given("a valid activity-note fixture containing exactly 1000 characters")
    public void maximumLengthActivityNoteFixture() {
        noteMarker = "PO-11194 " + UUID.randomUUID() + " ";
        String content = "Activity note: account reviewed; follow-up required. ";
        int remainingLength = MAX_NOTE_LENGTH - noteMarker.length();
        submittedNoteText = noteMarker + content.repeat(remainingLength / content.length() + 1)
            .substring(0, remainingLength);
        assertEquals(MAX_NOTE_LENGTH, submittedNoteText.length(), "Fixture note_text length");
    }

    /**
     * Submits the fixture with the current account version and the record type required by addNote.
     *
     * @param accountType defendant or minor-creditor.
     * @throws JSONException if the request cannot be constructed.
     */
    @When("I submit the activity-note fixture for the created {word} account")
    public void submitActivityNote(String accountType) throws JSONException {
        assertNotNull(submittedNoteText, "An activity-note fixture must be prepared before submission");
        String accountId;
        String accountPath;
        String recordType;
        switch (accountType) {
            case "defendant" -> {
                accountId = scenarioContext().getCreatedDefendantAccountIdOrFail();
                accountPath = "/defendant-accounts/";
                recordType = "defendant_accounts";
            }
            case "minor-creditor" -> {
                accountId = Long.toString(scenarioContext().getCreatedMinorCreditorAccountIdOrFail());
                accountPath = "/minor-creditor-accounts/";
                recordType = "creditor_accounts";
            }
            default -> throw new IllegalArgumentException("Unsupported activity-note account type: " + accountType);
        }

        Response summary = authorisedJsonRequest().when()
            .get(getTestUrl() + accountPath + accountId + "/header-summary");
        assertEquals(200, summary.statusCode(), "Account header-summary status");
        String etag = summary.header(HttpHeaders.ETAG);
        assertNotNull(etag, "Account header summary must supply an ETag");

        JSONObject note = new JSONObject()
            .put("note_text", submittedNoteText)
            .put("record_id", accountId)
            .put("record_type", recordType)
            .put("note_type", "AA");
        Response response = authorisedJsonRequest()
            .header(HttpHeaders.IF_MATCH, etag)
            .header("Business-Unit-Id", FIXTURE_BUSINESS_UNIT_ID)
            .body(new JSONObject().put("activity_note", note).toString())
            .when().post(getTestUrl() + "/notes");

        assertEquals(201, response.statusCode(), "Adding a 1000-character activity note must succeed");
    }

    /**
     * Identifies the submitted note by its marker, then checks its entire unmodified content and length.
     */
    @Then("the account history contains the submitted activity note with exactly 1000 characters")
    public void historyContainsMaximumLengthActivityNote() {
        assertNotNull(submittedNoteText, "No activity-note fixture was submitted");
        Response response = SerenityRest.lastResponse();
        assertEquals(200, response.statusCode(), "Account history status");
        JsonNode historyItems = OBJECT_MAPPER.readTree(response.asString()).path("historyItems");
        assertTrue(historyItems.isArray(), "historyItems must be an array");

        int matches = 0;
        for (JsonNode item : historyItems) {
            String text = item.path("details").path("noteText").asString("");
            if (text.startsWith(noteMarker)) {
                matches++;
                assertEquals("Note", item.path("type").asString(), "History item type");
                assertEquals(MAX_NOTE_LENGTH, text.length(), "Retrieved note_text length");
                assertEquals(submittedNoteText, text, "Retrieved note_text must match the submitted fixture");
            }
        }
        assertEquals(1, matches, "History must contain exactly one note with the submitted marker");
    }
}
