package uk.gov.hmcts.opal.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.service.NoteService;
import uk.gov.hmcts.opal.dto.NoteDto;



@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

        private final NoteService noteService;

        @PostMapping
        public ResponseEntity<NoteDto> createNote(@RequestBody NoteDto noteDto) {
            NoteDto savedNoteDto = noteService.saveNote(noteDto);
            return new ResponseEntity<>(savedNoteDto, HttpStatus.CREATED);
        }



}
