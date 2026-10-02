package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildCreatedResponse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
}
