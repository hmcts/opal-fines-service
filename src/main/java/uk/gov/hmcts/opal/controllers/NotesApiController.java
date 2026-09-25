package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildCreatedResponse;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.NotesApi;
import uk.gov.hmcts.opal.generated.model.AddNoteRequestNotes;
import uk.gov.hmcts.opal.service.NotesService;

@RestController
@Slf4j(topic = "opal.NotesApiController")
@AllArgsConstructor
public class NotesApiController implements NotesApi {

    private final NotesService notesService;

    @Override
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<String> addNote(Short businessUnitId, String ifMatch, AddNoteRequestNotes request) {

        log.debug(":POST:postDefendantAccountSearch: query: \n{}", request.toPrettyJson());
        String response = notesService.addNote(request, ifMatch, businessUnitId);

        return buildCreatedResponse(response);
    }

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/notes/add",
        produces = { "application/json", "application/json+problem" },
        consumes = { "application/json" })
    public ResponseEntity<String> addNoteTemp(
        @NotNull @Parameter(name = "Business-Unit-Id", description = "Business unit identifier", required = true,
            in = ParameterIn.HEADER) @RequestHeader(value = "Business-Unit-Id", required = true) Short businessUnitId,
        @NotNull @Parameter(name = "If-Match",
            description = "Version of the defendant account. DB Mapping - defendant_accounts.version", required = true,
            in = ParameterIn.HEADER) @RequestHeader(value = "If-Match", required = true) String ifMatch,
        @Parameter(name = "AddNoteRequestNotes", required = true) @Valid @RequestBody
        AddNoteRequestNotes addNoteRequestNotes) {
        //todo PO-8990 remove after FE merge
        return addNote(businessUnitId, ifMatch, addNoteRequestNotes);
    }
}
