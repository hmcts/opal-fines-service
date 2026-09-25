package uk.gov.hmcts.opal.service.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.AddNoteRequestNotes;
import uk.gov.hmcts.opal.generated.model.NoteCommon;
import uk.gov.hmcts.opal.generated.model.NoteCommon.NoteTypeEnum;
import uk.gov.hmcts.opal.generated.model.NoteCommon.RecordTypeEnum;
import uk.gov.hmcts.opal.service.AccountNoteContext;
import uk.gov.hmcts.opal.service.legacy.LegacyNotesService;
import uk.gov.hmcts.opal.service.opal.OpalNotesService;

@ExtendWith(MockitoExtension.class)
class NotesProxyTest extends ProxyTestsBase {

    private static final String IF_MATCH = "1";
    private static final Short BUSINESS_UNIT_ID = 78;
    private static final Long DEFENDANT_ACCOUNT_ID = 770000004141L;

    @Mock private OpalNotesService notesService;
    @Mock private LegacyNotesService legacyNotesService;
    @Mock private UserState userState;

    @InjectMocks
    private NotesProxy notesProxy;

    @Test
    void addNote_shouldRouteToLegacyWithoutResolvingLocalAccountContext_whenInLegacyMode() {
        setLegacyMode(true);
        AddNoteRequestNotes request = addNoteRequestNotes();
        String expectedResponse = "legacy-note-id";

        when(legacyNotesService.addNote(request, IF_MATCH, userState, BUSINESS_UNIT_ID)).thenReturn(expectedResponse);

        String actualResponse = notesProxy.addNote(request, IF_MATCH, userState, BUSINESS_UNIT_ID);

        assertEquals(expectedResponse, actualResponse);
        verify(legacyNotesService).addNote(request, IF_MATCH, userState, BUSINESS_UNIT_ID);
        verifyNoInteractions(notesService);
    }

    @Test
    void addNote_shouldRouteToOpalService_whenInOpalMode() {
        setLegacyMode(false);
        AddNoteRequestNotes request = addNoteRequestNotes();
        String expectedResponse = "opal-note-id";

        when(notesService.addNote(request, IF_MATCH, userState, BUSINESS_UNIT_ID)).thenReturn(expectedResponse);

        String actualResponse = notesProxy.addNote(request, IF_MATCH, userState, BUSINESS_UNIT_ID);

        assertEquals(expectedResponse, actualResponse);
        verify(notesService).addNote(request, IF_MATCH, userState, BUSINESS_UNIT_ID);
        verifyNoInteractions(legacyNotesService);
    }

    @Test
    void addNote_shouldRouteExistingAccountContextToOpal() {
        AddNoteRequestNotes request = addNoteRequestNotes();
        AccountNoteContext target = new AccountNoteContext(
            DefendantAccountEntity.class,
            DEFENDANT_ACCOUNT_ID,
            BUSINESS_UNIT_ID,
            AssociatedRecordType.DEFENDANT_ACCOUNTS
        );
        String expectedResponse = "opal-note-id";

        when(notesService.addNote(request, IF_MATCH, userState, target)).thenReturn(expectedResponse);

        String actualResponse = notesProxy.addNote(request, IF_MATCH, userState, target);

        assertEquals(expectedResponse, actualResponse);
        verify(notesService).addNote(request, IF_MATCH, userState, target);
        verifyNoInteractions(legacyNotesService);
    }

    private static AddNoteRequestNotes addNoteRequestNotes() {
        NoteCommon note = NoteCommon.builder()
            .recordType(RecordTypeEnum.DEFENDANT_ACCOUNTS)
            .recordId(DEFENDANT_ACCOUNT_ID.toString())
            .noteText("test")
            .noteType(NoteTypeEnum.AA)
            .build();
        return AddNoteRequestNotes.builder().activityNote(note).build();
    }
}
