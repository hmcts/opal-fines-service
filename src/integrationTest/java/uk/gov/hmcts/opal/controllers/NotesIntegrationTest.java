package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.dto.ToJsonString;

abstract class NotesIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/notes";

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @BeforeEach
    void setupUserState() {
        Mockito.when(userState.anyBusinessUnitUserHasPermission(Mockito.any())).thenReturn(true);

        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.any())).thenReturn(userState);
    }

    @DisplayName("OPAL: POST /notes/add creates note for defendant account [PO-1566]")
    void postNotesImpl(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

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
        final Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class, 77L
        );

        // Act
        ResultActions result =
            mockMvc.perform(
                post(URL_BASE + "/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .header("authorization", "Bearer some_value")
                    .header(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"")
            );

        // Assert
        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":testPostNotes response:\n{}", ToJsonString.toPrettyJson(body));
        result.andExpect(status().isCreated());
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

        ResultActions resultActions =
            mockMvc.perform(
                post(URL_BASE + "/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("authorization", "Bearer some_value")
                    .header("If-Match", "1")); // Add this line

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostNotes: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound());
    }

    @DisplayName("post notes - user without permission [PO-1566]")
    void postNotes_UserWithoutPermission(Logger log) throws Exception {
        // Create user with no permissions
        UserState restrictedUser = UserState.builder()
            .userId(99L)
            .userName("restricted-user")
            .businessUnitUser(Set.of())
            .build();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(restrictedUser);

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
                .header("If-Match", "1"));

        resultActions.andExpect(status().isForbidden());
    }

    @DisplayName("post notes for a defendant account in legacy [PO-1975]")
    void legacyTestAddNoteSuccess(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

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
                    .header("If-Match", "1"));

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostNotes: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isCreated());
    }

    @DisplayName("post notes for a defendant account ID that does not exist in legacy [PO-1975]")
    void legacyTestAddNote500Error(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

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
                    .header("If-Match", "5"));

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostNotes: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().is5xxServerError());

    }
}
