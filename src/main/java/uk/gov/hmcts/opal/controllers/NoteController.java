package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.NotesSearchDto;
import uk.gov.hmcts.opal.service.NoteServiceInterface;

import java.util.List;


@RestController
@RequestMapping("/api/notes")
@Slf4j(topic = "NoteController")
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

    @GetMapping(value = "/{associatedType}/{associatedId}")
    @Operation(summary = "Returns all notes for an associated type & id.")
    public ResponseEntity<List<NoteDto>> getNotesByAssociatedRecord(
        @PathVariable String associatedType, @PathVariable String associatedId) {

        log.info(":GET:getNotesByAssociatedRecord: associated record type: '{}'; id: {}", associatedType, associatedId);

        NotesSearchDto criteria = NotesSearchDto.builder()
            .associatedType(associatedType)
            .associatedId(associatedId)
            .build();

        List<NoteDto> response = noteService
            .searchNotes(criteria);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches notes based upon criteria in request body")
    public ResponseEntity<List<NoteDto>> postNotesSearch(
        @RequestBody NotesSearchDto criteria) {
        log.info(":POST:postNotesSearch: query: \n{}", criteria.toPrettyJson());

        List<NoteDto> response = noteService
            .searchNotes(criteria);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }



}
