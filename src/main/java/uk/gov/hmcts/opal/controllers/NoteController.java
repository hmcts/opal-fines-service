package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.service.NoteService;



@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    @Operation(summary = "Creates a new note in the Opal Fines Notes table assigning an ID.")
    public ResponseEntity<NoteDto> createNote(@RequestBody NoteDto noteDto) {
        NoteDto savedNoteDto = noteService.saveNote(noteDto);
        return new ResponseEntity<>(savedNoteDto, HttpStatus.CREATED);
    }



}
