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
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
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
        AddNoteRequest request = addNoteRequest();
        String expectedResponse = "legacy-note-id";

        when(legacyNotesService.addNote(request, IF_MATCH, userState, BUSINESS_UNIT_ID)).thenReturn(expectedResponse);

        String actualResponse = notesProxy.addNote(request, IF_MATCH, userState, BUSINESS_UNIT_ID);

        assertEquals(expectedResponse, actualResponse);
        verify(legacyNotesService).addNote(request, IF_MATCH, userState, BUSINESS_UNIT_ID);
        verifyNoInteractions(notesService);
    }

    @Test
    void addNote_shouldRouteToOpalWithoutResolvingLocalAccountContext_whenInOpalMode() {
        setLegacyMode(false);
        AddNoteRequest request = addNoteRequest();
        String expectedResponse = "opal-note-id";

        when(notesService.addNote(request, IF_MATCH, userState, BUSINESS_UNIT_ID)).thenReturn(expectedResponse);

        String actualResponse = notesProxy.addNote(request, IF_MATCH, userState, BUSINESS_UNIT_ID);

        assertEquals(expectedResponse, actualResponse);
        verify(notesService).addNote(request, IF_MATCH, userState, BUSINESS_UNIT_ID);
        verifyNoInteractions(legacyNotesService);
    }

    @Test
    void addNote_shouldRouteExistingAccountContextToOpal() {
        AddNoteRequest request = addNoteRequest();
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

    private static AddNoteRequest addNoteRequest() {
        Note note = Note.builder()
            .recordType(RecordType.DEFENDANT_ACCOUNTS)
            .recordId(DEFENDANT_ACCOUNT_ID.toString())
            .noteText("test")
            .noteType("AA")
            .build();
        return new AddNoteRequest(note);
    }
}
