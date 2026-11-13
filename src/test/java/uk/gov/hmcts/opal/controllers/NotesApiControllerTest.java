package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_SUSPENSE;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_SUSPENSE_ENABLED_PROPERTY;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.generated.model.AddNoteRequestNotes;
import uk.gov.hmcts.opal.generated.model.NoteCommon;
import uk.gov.hmcts.opal.service.NotesService;


@ExtendWith(MockitoExtension.class)
class NotesApiControllerTest {

    @Mock
    private NotesService notesService;

    @Mock
    private FeatureToggleApi featureToggleApi;

    @InjectMocks
    private NotesApiController controller;

    @ParameterizedTest
    @EnumSource(value = NoteCommon.RecordTypeEnum.class, names = {"DEFENDANT_ACCOUNTS", "CREDITOR_ACCOUNTS"})
    void addNote_returnsCreatedResponse(NoteCommon.RecordTypeEnum recordType) {
        AddNoteRequestNotes request = new AddNoteRequestNotes();
        request.setActivityNote(NoteCommon.builder().recordType(recordType).build());
        String ifMatch = "etag-123";
        short businessUnitId = 7;
        when(notesService.addNote(request, ifMatch, businessUnitId))
            .thenReturn("note-created");

        ResponseEntity<String> response = controller.addNote(businessUnitId, ifMatch, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("note-created", response.getBody());
        verifyNoInteractions(featureToggleApi);
    }

    @Test
    void addNote_rejectsSuspenseNoteWhenFeatureDisabled() {
        AddNoteRequestNotes request = suspenseRequest();

        assertThrows(FeatureDisabledException.class, () -> controller.addNote((short) 7, "1", request));

        verifyNoInteractions(notesService);
    }

    @Test
    void addNote_allowsSuspenseNoteWhenFeatureEnabled() {
        AddNoteRequestNotes request = suspenseRequest();
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            RELEASE_1C_SUSPENSE, RELEASE_1C_SUSPENSE_ENABLED_PROPERTY)).thenReturn(true);
        when(notesService.addNote(request, "1", (short) 7)).thenReturn("123");

        ResponseEntity<String> response = controller.addNote((short) 7, "1", request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("123", response.getBody());
    }

    private AddNoteRequestNotes suspenseRequest() {
        return AddNoteRequestNotes.builder().activityNote(NoteCommon.builder()
            .recordType(NoteCommon.RecordTypeEnum.SUSPENSE_ITEMS).build()).build();
    }
}
