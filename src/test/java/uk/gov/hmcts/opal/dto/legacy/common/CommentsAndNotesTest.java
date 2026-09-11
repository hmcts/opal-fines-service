package uk.gov.hmcts.opal.dto.legacy.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.dto.ToJsonString;

class CommentsAndNotesTest {

    @Test
    void toJsonString_serializesFieldsInSnakeCase() {
        CommentsAndNotes commentsAndNotes = CommentsAndNotes.builder()
            .accountComment("Account comment")
            .freeTextNote1("Note one")
            .freeTextNote2("Note two")
            .freeTextNote3("Note three")
            .build();

        JsonNode json = ToJsonString.getObjectMapper().readTree(
            ToJsonString.getObjectMapper().writeValueAsString(commentsAndNotes)
        );

        assertEquals("Account comment", json.get("account_comment").asString());
        assertEquals("Note one", json.get("free_text_note_1").asString());
        assertEquals("Note two", json.get("free_text_note_2").asString());
        assertEquals("Note three", json.get("free_text_note_3").asString());

        assertFalse(json.has("accountComment"));
        assertFalse(json.has("freeTextNote1"));
        assertFalse(json.has("freeTextNote2"));
        assertFalse(json.has("freeTextNote3"));
    }

    @Test
    void toJsonString_omitsNullFields() throws Exception {
        CommentsAndNotes commentsAndNotes = CommentsAndNotes.builder()
            .accountComment("Account comment")
            .freeTextNote2("Note two")
            .build();

        JsonNode json = ToJsonString.getObjectMapper().readTree(
            ToJsonString.getObjectMapper().writeValueAsString(commentsAndNotes)
        );

        assertEquals("Account comment", json.get("account_comment").asString());
        assertEquals("Note two", json.get("free_text_note_2").asString());
        assertFalse(json.has("free_text_note_1"));
        assertFalse(json.has("free_text_note_3"));
    }
}
