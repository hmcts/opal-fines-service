package uk.gov.hmcts.opal.controllers.develop;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
import uk.gov.hmcts.opal.authorisation.model.Role;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.search.NoteSearchDto;
import uk.gov.hmcts.opal.service.NoteServiceInterface;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;
import static uk.gov.hmcts.opal.util.PermissionUtil.getRequiredRole;


@RestController
@RequestMapping("/api/note")
@Slf4j(topic = "NoteController")
@Tag(name = "Notes Controller")
public class NoteController {

    private final NoteServiceInterface noteService;

    private final UserStateService userStateService;

    public NoteController(@Qualifier("noteServiceProxy") NoteServiceInterface noteService,
                          UserStateService userStateService) {
        this.noteService = noteService;
        this.userStateService = userStateService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Creates a new note in the Opal Fines Notes table assigning an ID.")
    public ResponseEntity<NoteDto> createNote(@RequestBody NoteDto noteDto, HttpServletRequest request) {
        log.info(":POST:createNote: {}", noteDto.toPrettyJson());

        UserState userState = userStateService.getUserStateUsingServletRequest(request);
        Role role = getRequiredRole(userState, noteDto.getBusinessUnitId());

        noteDto.setPostedBy(role.getBusinessUserId());
        noteDto.setPostedByUserId(userState.getUserId());
        NoteDto savedNoteDto = noteService.saveNote(noteDto);
        return new ResponseEntity<>(savedNoteDto, HttpStatus.CREATED);
    }

    @GetMapping(value = "/{associatedType}/{associatedId}")
    @Operation(summary = "Returns all notes for an associated type & id.")
    public ResponseEntity<List<NoteDto>> getNotesByAssociatedRecord(
        @PathVariable String associatedType, @PathVariable String associatedId) {

        log.info(":GET:getNotesByAssociatedRecord: associated record type: '{}'; id: {}", associatedType, associatedId);

        NoteSearchDto criteria = NoteSearchDto.builder()
            .associatedType(associatedType)
            .associatedId(associatedId)
            .build();

        List<NoteDto> response = noteService
            .searchNotes(criteria);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches notes based upon criteria in request body")
    public ResponseEntity<List<NoteDto>> postNotesSearch(
        @RequestBody NoteSearchDto criteria) {
        log.info(":POST:postNotesSearch: query: \n{}", criteria.toPrettyJson());

        List<NoteDto> response = noteService
            .searchNotes(criteria);

        return buildResponse(response);
    }


}
