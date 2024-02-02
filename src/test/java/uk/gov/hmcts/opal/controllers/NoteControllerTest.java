package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.NotesSearchDto;
import uk.gov.hmcts.opal.service.opal.NoteService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteControllerTest {

    @Mock
    private NoteService noteService;

    @InjectMocks
    private NoteController noteController;

    @Test
    void testCreateNote_Success() {
        // Arrange
        NoteDto noteDtoRequest = NoteDto.builder().build();
        NoteDto noteDtoResponse = NoteDto.builder().noteId(1L).build(); //some id assigned by db sequence

        when(noteService.saveNote(any(NoteDto.class))).thenReturn(noteDtoResponse);

        // Act
        ResponseEntity<NoteDto> response = noteController.createNote(noteDtoRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(noteDtoResponse, response.getBody());
        verify(noteService, times(1)).saveNote(any(NoteDto.class));
    }

    @Test
    public void testFindNoteByAssociated_Success() {
        // Arrange
        NoteDto mockNote = new NoteDto();
        List<NoteDto> mockResponse = List.of(mockNote);

        when(noteService.searchNotes(any(NotesSearchDto.class))).thenReturn(mockResponse);

        // Act
        ResponseEntity<List<NoteDto>> responseEntity = noteController.getNotesByAssociatedRecord("type", "1");

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(noteService, times(1)).searchNotes(any(
            NotesSearchDto.class));

    }

    @Test
    public void testFindNoteByAssociated_NoContent() {
        when(noteService.searchNotes(any(NotesSearchDto.class))).thenReturn(null);

        // Act
        ResponseEntity<List<NoteDto>> responseEntity = noteController.getNotesByAssociatedRecord("type", "1");

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(noteService, times(1)).searchNotes(any(
            NotesSearchDto.class));
    }


    @Test
    public void testNotesSearch_Success() {
        // Arrange
        NoteDto mockNote = new NoteDto();
        List<NoteDto> mockResponse = List.of(mockNote);

        when(noteService.searchNotes(any(NotesSearchDto.class))).thenReturn(mockResponse);

        // Act
        NotesSearchDto criteria = NotesSearchDto.builder().build();
        ResponseEntity<List<NoteDto>> responseEntity = noteController.postNotesSearch(criteria);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(noteService, times(1)).searchNotes(any(
            NotesSearchDto.class));

    }

    @Test
    public void testNotesSearch_NoContent() {
        when(noteService.searchNotes(any(NotesSearchDto.class))).thenReturn(null);

        // Act
        NotesSearchDto criteria = NotesSearchDto.builder().build();
        ResponseEntity<List<NoteDto>> responseEntity = noteController.postNotesSearch(criteria);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(noteService, times(1)).searchNotes(any(
            NotesSearchDto.class));
    }
}
