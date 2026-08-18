package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildCreatedResponse;

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
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
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
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<String> addNote(
        @RequestBody AddNoteRequest request,
        @RequestHeader("If-Match") String ifMatch,
        @RequestHeader("Business-Unit-Id") Short businessUnitId) {

        log.debug(":POST:postDefendantAccountSearch: query: \n{}", request.toPrettyJson());
        String response = notesService.addNote(request, ifMatch, businessUnitId);

        return buildCreatedResponse(response);
    }
}
