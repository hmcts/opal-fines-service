package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.support.UserServiceStub.stubUserWithAllPermissions;
import static uk.gov.hmcts.opal.support.UserServiceStub.stubUserWithNoPermissions;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.opal.dto.ToJsonString;

@ActiveProfiles(profiles = {"integration-with-spring-security", "legacy"}, inheritProfiles = false)
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.LegacyDefendantsCommentNotesIntegrationTest")
class LegacyDefendantsCommentNotesIntegrationTest extends AbstractLegacyDefendantsIntegrationTest {

    @Test
    @DisplayName("LEGACY: PATCH Update Defendant Account - Update Comment Notes [@PO-1908]")
    void testUpdateDefAcc_CommentNotes_Success() throws Exception {
        stubUserWithAllPermissions(78);

        Integer currentVersion = versionFor(77L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(validToken);
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, String.valueOf(currentVersion));

        String requestJson = commentAndNotesPayload(
            "patch DefAcc comment legacy test",
            "patch DefAcc note one legacy test",
            "patch DefAcc note two legacy test",
            "patch DefAcc note three legacy test"
        );

        ResultActions resultActions = mockMvc.perform(
                patch(URL_BASE + "/77")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andDo(MockMvcResultHandlers.print());

        String etag = resultActions.andReturn().getResponse().getHeader("ETag");
        log.info(":legacy_UpdateDefendantAccount_CommentsNotes_Success ETag: {}", etag);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value("77"))
            .andExpect(jsonPath("$.comment_and_notes.account_comment")
                .value("patch DefAcc comment legacy test"))
            .andExpect(jsonPath("$.comment_and_notes.free_text_note_1")
                .value("patch DefAcc note one legacy test"))
            .andExpect(jsonPath("$.comment_and_notes.free_text_note_2")
                .value("patch DefAcc note two legacy test"))
            .andExpect(jsonPath("$.comment_and_notes.free_text_note_3")
                .value("patch DefAcc note three legacy test"))
            .andExpect(header().string("ETag", "\"" + ++currentVersion + "\""));
    }

    @Test
    @DisplayName("LEGACY: PATCH Update Defendant Account - Update Comment Notes - 400 Error [@PO-1908]")
    void testUpdateDefAcc_CommentNotes_400ErrorForMissingRequiredHeader() throws Exception {
        stubUserWithAllPermissions(78);

        String requestJson = commentAndNotesPayload(
            "patch DefAcc comment legacy test",
            "patch DefAcc note one legacy test",
            "patch DefAcc note two legacy test",
            "patch DefAcc note three legacy test"
        );

        ResultActions actions = mockMvc.perform(
            patch(URL_BASE + "/400")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":Legacy_UpdateDefendantAccount_CommentNotes_400Error body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(header().doesNotExist("ETag"));
    }

    @Test
    @DisplayName("LEGACY: PATCH Update Defendant Account - 401 Unauthorized [@PO-1908, CEP2]")
    void testUpdateDefAcc_CommentNotes_401Unauthorized() throws Exception {
        String requestJson = commentAndNotesPayload(
            "patch DefAcc comment legacy test",
            "patch DefAcc note one legacy test",
            "patch DefAcc note two legacy test",
            "patch DefAcc note three legacy test"
        );

        mockMvc.perform(
                patch(URL_BASE + "/77")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                    .header("Business-Unit-Id", "78")
                    .header(HttpHeaders.IF_MATCH, "0")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("LEGACY: PATCH Update Defendant Account - 403 Forbidden [@PO-1908, CEP3]")
    void testUpdateDefAcc_CommentNotes_403Forbidden() throws Exception {
        stubUserWithNoPermissions(78);

        String requestJson = commentAndNotesPayload(
            "patch DefAcc comment legacy test",
            "patch DefAcc note one legacy test",
            "patch DefAcc note two legacy test",
            "patch DefAcc note three legacy test"
        );

        mockMvc.perform(
                patch(URL_BASE + "/77")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                    .header("Business-Unit-Id", "78")
                    .header(HttpHeaders.IF_MATCH, "0")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("LEGACY: PATCH Update Defendant Account - 400 Bad Request Invalid Payload [@PO-1908, CEP1]")
    void testUpdateDefAcc_CommentNotes_400BadRequest() throws Exception {
        stubUserWithAllPermissions(78);

        String invalidJson = """
            {
              "comment_and_notes": {
                "account_comment": 12345
              },
            
            }
            """;

        ResultActions actions = mockMvc.perform(
            patch(URL_BASE + "/77")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .header("Business-Unit-Id", "78")
                .header(HttpHeaders.IF_MATCH, "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        );

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":Legacy_UpdateDefendantAccount_CommentNotes_400BadRequest body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type")
                .value("https://hmcts.gov.uk/problems/message-not-readable"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.retriable").value(false));
    }
}
