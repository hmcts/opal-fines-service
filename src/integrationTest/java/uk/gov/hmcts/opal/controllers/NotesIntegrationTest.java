package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.UserStateService;

abstract class NotesIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/notes";

    @MockitoBean
    UserStateService userStateService;

    @MockitoBean
    private UserState userState;

    @BeforeEach
    void setupUserState() {
        Mockito.when(userState.anyBusinessUnitUserHasPermission(Mockito.any()))
            .thenReturn(true);

        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.any()))
            .thenReturn(userState);
    }

    @DisplayName("post notes defendant accounts [PO-1566]")
    void postNotesImpl(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        Note note = new Note();
        note.setNoteText("test");
        note.setRecordId("77");
        note.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        note.setNoteType("AA");

        AddNoteRequest request = new AddNoteRequest();

        request.setActivityNote(note);

        log.info(":testPostNotes:{}", objectMapper.writeValueAsString(request));

        ResultActions resultActions = mockMvc.perform(post(URL_BASE + "/add")
                                                          .contentType(MediaType.APPLICATION_JSON)
                                                          .content(objectMapper.writeValueAsString(request))
                                                          .header("authorization", "Bearer some_value")
                                                          .header("If-Match", "1"));

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostNotes: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk());

    }

    @DisplayName("post notes for a defendant account ID that does not exist [PO-1566]")
    void postNotes_IDNotFoundError(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        Note note = new Note();
        note.setNoteText("test");
        note.setRecordId("122");
        note.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        note.setNoteType("AA");

        AddNoteRequest request = new AddNoteRequest();

        request.setActivityNote(note);

        ResultActions resultActions = mockMvc.perform(post(URL_BASE + "/add")
                                                          .contentType(MediaType.APPLICATION_JSON)
                                                          .content(objectMapper.writeValueAsString(request))
                                                          .header("authorization", "Bearer some_value")
                                                          .header("If-Match", "1")); // Add this line

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testGetHeaderSummary: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound());
    }

}
