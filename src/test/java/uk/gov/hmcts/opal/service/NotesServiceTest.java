package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.proxy.NotesProxy;

@ExtendWith(MockitoExtension.class)
class NotesServiceTest {

    private static final String IF_MATCH = "etag-123";
    private static final Short BUSINESS_UNIT_ID = 10;
    private static final Long DEFENDANT_ACCOUNT_ID = 77L;

    @Mock private NotesProxy notesProxy;
    @Mock private UserStateService userStateService;
    @Mock private AccountNoteContextFactory accountNoteContextFactory;
    @Mock private UserState userState;

    private NotesService notesService;
    private AddNoteRequest request;
    private AccountNoteContext target;

    @BeforeEach
    void setUp() {
        notesService = new NotesService(notesProxy, userStateService, accountNoteContextFactory);
        request = addNoteRequest();
        target = new AccountNoteContext(
            DefendantAccountEntity.class,
            DEFENDANT_ACCOUNT_ID,
            BUSINESS_UNIT_ID,
            AssociatedRecordType.DEFENDANT_ACCOUNTS
        );
    }

    @Test
    void addNote_shouldThrowPermissionNotAllowedException_whenUserLacksPermission() {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(accountNoteContextFactory.from(request.getActivityNote())).thenReturn(target);
        when(userState.hasBusinessUnitUserWithPermission(
            BUSINESS_UNIT_ID, FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES)).thenReturn(false);

        assertThrows(
            PermissionNotAllowedException.class,
            () -> notesService.addNote(request, IF_MATCH, BUSINESS_UNIT_ID)
        );
    }

    @Test
    void addNote_shouldDelegateToNotesProxy_whenUserHasPermission() {
        String expectedResponse = "note-id-456";

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(accountNoteContextFactory.from(request.getActivityNote())).thenReturn(target);
        when(userState.hasBusinessUnitUserWithPermission(
            BUSINESS_UNIT_ID, FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES)).thenReturn(true);
        when(notesProxy.addNote(eq(request), eq(IF_MATCH), eq(userState), eq(target))).thenReturn(expectedResponse);

        String actualResponse = notesService.addNote(request, IF_MATCH, BUSINESS_UNIT_ID);

        assertEquals(expectedResponse, actualResponse);
        verify(notesProxy).addNote(request, IF_MATCH, userState, target);
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
