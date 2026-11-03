package uk.gov.hmcts.opal.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class NoteEntityTest {

    @Test
    void constructor_populatesNoteFields() {
        NoteEntity note = new NoteEntity(
            NoteType.AA, "Payment arrangement agreed",
            AssociatedRecordType.DEFENDANT_ACCOUNTS, "12345");

        assertEquals(NoteType.AA, note.getNoteType());
        assertEquals("Payment arrangement agreed", note.getNoteText());
        assertEquals(AssociatedRecordType.DEFENDANT_ACCOUNTS, note.getAssociatedRecordType());
        assertEquals("12345", note.getAssociatedRecordId());
        assertNull(note.getNoteId());
        assertNull(note.getPostedDate());
        assertNull(note.getBusinessUnitUserId());
        assertNull(note.getPostedByUsername());
    }
}
