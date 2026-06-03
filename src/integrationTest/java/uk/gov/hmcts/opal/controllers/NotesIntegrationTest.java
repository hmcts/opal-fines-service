package uk.gov.hmcts.opal.controllers;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.util.DefendantAccountVersionUtil;

import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allFinesPermissionsToken;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noFinesPermissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noFinesPermissionsToken;

import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

abstract class NotesIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/notes";

    @MockitoBean
    UserStateClientService userStateClientService;

    @MockitoBean
    private UserState userState;

    @DisplayName("OPAL: POST /notes/add creates note for defendant account [PO-1566]")
    @JiraStory("PO-1566")
    void postNotesImpl(Logger log) throws Exception {

        // Arrange
        Note note = new Note();
        note.setNoteText("test");
        note.setRecordId("77");
        note.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        note.setNoteType("AA");

        AddNoteRequest request = new AddNoteRequest();
        request.setActivityNote(note);

        final String payload = objectMapper.writeValueAsString(request);
        log.info(":testPostNotes payload: {}", payload);

        // Read the current version immediately before use (avoids distance warning & stale reads)
        final Integer currentVersion = DefendantAccountVersionUtil.getVersion(jdbcTemplate, 77L);

        // Act
        ResultActions result =
            mockMvc.perform(
                post(URL_BASE + "/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .header("authorization", "Bearer some_value")
                    .header(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"")
                    .with(authentication(allFinesPermissionsToken()))
            );

        // Assert
        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":testPostNotes response:\n{}", ToJsonString.toPrettyJson(body));
        result.andExpect(status().isCreated());
    }

    @DisplayName("post notes for a defendant account ID that does not exist [PO-1566]")
    @JiraStory("PO-1566")
    void postNotes_IDNotFoundError(Logger log) throws Exception {

        Note note = new Note();
        note.setNoteText("test");
        note.setRecordId("122");
        note.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        note.setNoteType("AA");

        AddNoteRequest request = new AddNoteRequest();

        request.setActivityNote(note);

        ResultActions resultActions =
            mockMvc.perform(
                post(URL_BASE + "/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("authorization", "Bearer some_value")
                    .header("If-Match", "1")
                    .with(authentication(allFinesPermissionsToken()))
            );

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostNotes: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound());
    }

    @DisplayName("post notes - user without permission [PO-1566]")
    @JiraStory("PO-1566")
    void postNotes_UserWithoutPermission(Logger log) throws Exception {

        UserState userState = noFinesPermissionUser();
        when(userStateClientService.getUserStateV1ByAuthenticatedUser()).thenReturn(Optional.of(userState));

        Note note = new Note();
        note.setNoteText("test note");
        note.setRecordId("77");
        note.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        note.setNoteType("AA");

        AddNoteRequest request = new AddNoteRequest();
        request.setActivityNote(note);

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("authorization", "Bearer some_value")
                .header("If-Match", "1")
                .with(authentication(noFinesPermissionsToken()))
        );

        resultActions.andExpect(status().isForbidden());
    }

    @DisplayName("post notes for a defendant account in legacy [PO-1975]")
    @JiraStory("PO-1975")
    void legacyTestAddNoteSuccess(Logger log) throws Exception {

        Note note = new Note();
        note.setNoteText("test");
        note.setRecordId("77");
        note.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        note.setNoteType("AA");

        AddNoteRequest request = new AddNoteRequest();

        request.setActivityNote(note);

        ResultActions resultActions =
            mockMvc.perform(
                post(URL_BASE + "/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("authorization", "Bearer some_value")
                    .header("If-Match", "1")
                    .with(authentication(allFinesPermissionsToken()))
            );

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostNotes: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isCreated());
    }

    @DisplayName("post notes for a defendant account ID that does not exist in legacy [PO-1975]")
    @JiraStory("PO-1975")
    void legacyTestAddNote500Error(Logger log) throws Exception {

        Note note = new Note();
        note.setNoteText("FAIL");
        note.setRecordId("77");
        note.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        note.setNoteType("AA");

        AddNoteRequest request = new AddNoteRequest();

        request.setActivityNote(note);

        ResultActions resultActions =
            mockMvc.perform(
                post(URL_BASE + "/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("authorization", "Bearer some_value")
                    .header("If-Match", "5")
                    .with(authentication(allFinesPermissionsToken()))
            );

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostNotes: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().is5xxServerError());

    }
}
