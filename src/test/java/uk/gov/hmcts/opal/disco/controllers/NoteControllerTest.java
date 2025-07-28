package uk.gov.hmcts.opal.disco.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.search.NoteSearchDto;
import uk.gov.hmcts.opal.disco.opal.NoteService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteControllerTest {

    static final String BEARER_TOKEN = "Bearer a_token_here";

    @Mock
    private NoteService noteService;

    @Mock
    private UserStateService userStateService;

    @InjectMocks
    private NoteController noteController;

    @Test
    void testCreateNote_Success() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        NoteDto noteDtoRequest = NoteDto.builder().businessUnitId((short) 50).build();
        NoteDto noteDtoResponse = NoteDto.builder().noteId(1L).build();
        UserState userState = UserStateUtil.permissionUser(
            (short)50, Permissions.ACCOUNT_ENQUIRY, Permissions.ACCOUNT_ENQUIRY_NOTES);

        when(noteService.saveNote(any(NoteDto.class))).thenReturn(noteDtoResponse);
        when(userStateService.getUserStateUsingAuthToken(any())).thenReturn(userState);

        // Act
        ResponseEntity<NoteDto> response = noteController.createNote(noteDtoRequest, BEARER_TOKEN);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(noteDtoResponse, response.getBody());
        verify(noteService, times(1)).saveNote(any(NoteDto.class));
    }

    @Test
    void testFindNoteByAssociated_Success() {
        // Arrange
        NoteDto mockNote = new NoteDto();
        List<NoteDto> mockResponse = List.of(mockNote);

        when(noteService.searchNotes(any(NoteSearchDto.class))).thenReturn(mockResponse);

        // Act
        ResponseEntity<List<NoteDto>> responseEntity = noteController.getNotesByAssociatedRecord("type", "1");

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(noteService, times(1)).searchNotes(any(
            NoteSearchDto.class));

    }

    @Test
    void testFindNoteByAssociated_NoContent() {
        when(noteService.searchNotes(any(NoteSearchDto.class))).thenReturn(null);

        // Act
        ResponseEntity<List<NoteDto>> responseEntity = noteController.getNotesByAssociatedRecord("type", "1");

        // Assert
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        assertNull(responseEntity.getBody());
        verify(noteService, times(1)).searchNotes(any(
            NoteSearchDto.class));
    }


    @Test
    void testNotesSearch_Success() {
        // Arrange
        NoteDto mockNote = new NoteDto();
        List<NoteDto> mockResponse = List.of(mockNote);

        when(noteService.searchNotes(any(NoteSearchDto.class))).thenReturn(mockResponse);

        // Act
        NoteSearchDto criteria = NoteSearchDto.builder().build();
        ResponseEntity<List<NoteDto>> responseEntity = noteController.postNotesSearch(criteria);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(noteService, times(1)).searchNotes(any(
            NoteSearchDto.class));

    }

    @Test
    void testNotesSearch_NoContent() {
        when(noteService.searchNotes(any(NoteSearchDto.class))).thenReturn(null);

        // Act
        NoteSearchDto criteria = NoteSearchDto.builder().build();
        ResponseEntity<List<NoteDto>> responseEntity = noteController.postNotesSearch(criteria);

        // Assert
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        assertNull(responseEntity.getBody());
        verify(noteService, times(1)).searchNotes(any(
            NoteSearchDto.class));
    }


}
