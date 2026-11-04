package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.generated.model.AddNoteRequestNotes;
import uk.gov.hmcts.opal.generated.model.NoteCommon;
import uk.gov.hmcts.opal.service.proxy.NotesProxy;

@ExtendWith(MockitoExtension.class)
class NotesServiceTest {

    private static final String IF_MATCH = "etag-123";
    private static final Short BUSINESS_UNIT_ID = 10;
    private static final Long DEFENDANT_ACCOUNT_ID = 77L;

    @Mock private NotesProxy notesProxy;
    @Mock private UserStateService userStateService;
    @Mock private UserState userState;

    @InjectMocks
    private NotesService notesService;

    private AddNoteRequestNotes request;

    @BeforeEach
    void setUp() {
        request = addNoteRequest();
    }

    @ParameterizedTest
    @EnumSource(value = NoteCommon.RecordTypeEnum.class, names = {"DEFENDANT_ACCOUNTS", "CREDITOR_ACCOUNTS"})
    void addNote_rejectsAccountNotesWithoutPermission(NoteCommon.RecordTypeEnum recordType) {
        request.getActivityNote().setRecordType(recordType);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.hasBusinessUnitUserWithPermission(
            BUSINESS_UNIT_ID, FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES)).thenReturn(false);

        assertThrows(
            PermissionNotAllowedException.class,
            () -> notesService.addNote(request, IF_MATCH, BUSINESS_UNIT_ID)
        );
        verifyNoInteractions(notesProxy);
    }

    @Test
    void addNote_requiresManageSuspenseItemsPermissionInProvidedBusinessUnit() {
        request.getActivityNote().setRecordType(NoteCommon.RecordTypeEnum.SUSPENSE_ITEMS);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);

        assertThrows(PermissionNotAllowedException.class,
            () -> notesService.addNote(request, IF_MATCH, BUSINESS_UNIT_ID));

        verify(userState).hasBusinessUnitUserWithPermission(BUSINESS_UNIT_ID, FinesPermission.MANAGE_SUSPENSE_ITEMS);
        verifyNoInteractions(notesProxy);
    }

    @Test
    void addNote_allowsSuspenseNotesWithManageSuspenseItemsPermissionAlone() {
        request.getActivityNote().setRecordType(NoteCommon.RecordTypeEnum.SUSPENSE_ITEMS);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.hasBusinessUnitUserWithPermission(BUSINESS_UNIT_ID, FinesPermission.MANAGE_SUSPENSE_ITEMS))
            .thenReturn(true);
        when(notesProxy.addNote(request, IF_MATCH, userState, BUSINESS_UNIT_ID)).thenReturn("456");

        assertEquals("456", notesService.addNote(request, IF_MATCH, BUSINESS_UNIT_ID));
    }

    @Test
    void addNote_shouldDelegateToNotesProxy_whenUserHasPermission() {
        String expectedResponse = "note-id-456";

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.hasBusinessUnitUserWithPermission(
            BUSINESS_UNIT_ID, FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES)).thenReturn(true);
        when(notesProxy.addNote(request, IF_MATCH, userState, BUSINESS_UNIT_ID)).thenReturn(expectedResponse);

        String actualResponse = notesService.addNote(request, IF_MATCH, BUSINESS_UNIT_ID);

        assertEquals(expectedResponse, actualResponse);
        verify(notesProxy).addNote(request, IF_MATCH, userState, BUSINESS_UNIT_ID);
    }

    private static AddNoteRequestNotes addNoteRequest() {
        NoteCommon note = NoteCommon.builder()
            .recordType(NoteCommon.RecordTypeEnum.DEFENDANT_ACCOUNTS)
            .recordId(DEFENDANT_ACCOUNT_ID.toString())
            .noteText("test")
            .noteType(NoteCommon.NoteTypeEnum.AA)
            .build();
        return AddNoteRequestNotes.builder().activityNote(note).build();
    }
}
