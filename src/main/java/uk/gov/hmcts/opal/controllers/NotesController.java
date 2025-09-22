package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.service.NotesService;

@RestController
@RequestMapping("/notes")
@Slf4j(topic = "opal.NotesController")
@Tag(name = "Notes Controller")
@AllArgsConstructor
public class NotesController {

    private final NotesService notesService;

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "adds a note to an entity")
    public ResponseEntity<String> addNote(
        @RequestBody
        AddNoteRequest request,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue,
        @RequestHeader("If-Match") String ifMatchHeader) {

        log.debug(":POST:postDefendantAccountSearch: query: \n{}", request.toPrettyJson());

        String trimmed = ifMatchHeader.replace("\"", "");
        Long expectedVersion = Long.valueOf(trimmed);

        String response =
            notesService.addNote(request, expectedVersion, authHeaderValue);

        return buildResponse(response);
    }

}
