package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.service.NoteServiceInterface;


@RestController
@RequestMapping("/api/notes")
@Tag(name = "Notes Controller")
public class NoteController {

    private final NoteServiceInterface noteService;

    public NoteController(@Qualifier("noteServiceProxy") NoteServiceInterface noteService) {
        this.noteService = noteService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Creates a new note in the Opal Fines Notes table assigning an ID.")
    public ResponseEntity<NoteDto> createNote(@RequestBody NoteDto noteDto) {
        NoteDto savedNoteDto = noteService.saveNote(noteDto);
        return new ResponseEntity<>(savedNoteDto, HttpStatus.CREATED);
    }



}
