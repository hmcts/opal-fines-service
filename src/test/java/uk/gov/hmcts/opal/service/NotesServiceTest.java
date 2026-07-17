package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import uk.gov.hmcts.opal.service.proxy.NotesProxy;

@ExtendWith(MockitoExtension.class)
class NotesServiceTest {

    public static final String IF_MATCH = "etag-123";
    public static final Short BUSINESS_UNIT_ID = 10;
    @Mock
    private NotesProxy notesProxy;

    @Mock
    private UserStateService userStateService;

    @Mock
    private UserState userState;

    private NotesService notesService;

    @BeforeEach
    void setUp() {
        notesService = new NotesService(notesProxy, userStateService);
    }

    @Test
    void addNote_shouldThrowPermissionNotAllowedException_whenUserLacksPermission() {
        // given
        AddNoteRequest request = new AddNoteRequest();

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.hasBusinessUnitUserWithPermission(
            BUSINESS_UNIT_ID, FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES)).thenReturn(false);

        // when / then
        assertThrows(
            PermissionNotAllowedException.class,
            () -> notesService.addNote(request, IF_MATCH, BUSINESS_UNIT_ID)
        );
    }

    @Test
    void addNote_shouldDelegateToNotesProxy_whenUserHasPermission() {
        // given
        AddNoteRequest request = new AddNoteRequest();
        String expectedResponse = "note-id-456";

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(userState.hasBusinessUnitUserWithPermission(
            BUSINESS_UNIT_ID, FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES)).thenReturn(true);
        when(notesProxy.addNote(request, IF_MATCH, userState, BUSINESS_UNIT_ID)).thenReturn(expectedResponse);

        // when
        String actualResponse = notesService.addNote(request, IF_MATCH, BUSINESS_UNIT_ID);

        // then
        assertEquals(expectedResponse, actualResponse);
    }
}