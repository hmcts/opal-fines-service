package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.service.NoteService;

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


}
