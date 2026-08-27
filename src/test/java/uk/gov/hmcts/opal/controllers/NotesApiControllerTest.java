package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.generated.model.AddNoteRequestNotes;
import uk.gov.hmcts.opal.service.NotesService;


@ExtendWith(MockitoExtension.class)
class NotesApiControllerTest {

    @Mock
    private NotesService notesService;

    @InjectMocks
    private NotesApiController controller;

    @Test
    void addNote_returnsCreatedResponse() {
        AddNoteRequestNotes request = new AddNoteRequestNotes();
        String ifMatch = "etag-123";
        short businessUnitId = 7;
        when(notesService.addNote(request, ifMatch, businessUnitId))
            .thenReturn("note-created");

        ResponseEntity<String> response = controller.addNote(businessUnitId, ifMatch, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("note-created", response.getBody());
    }
}
