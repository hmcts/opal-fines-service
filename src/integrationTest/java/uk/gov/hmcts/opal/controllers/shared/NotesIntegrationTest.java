package uk.gov.hmcts.opal.controllers.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES;
import static uk.gov.hmcts.opal.controllers.shared.util.UserStateUtil.allFinesPermissionsToken;
import static uk.gov.hmcts.opal.controllers.shared.util.UserStateUtil.permissionsToken;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.generated.model.NoteCommon;
import uk.gov.hmcts.opal.generated.model.NoteCommon.RecordTypeEnum;
import uk.gov.hmcts.opal.generated.model.AddNoteRequestNotes;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

abstract class NotesIntegrationTest extends AbstractIntegrationTest {

    protected static final String URL_BASE = "/notes";

    @BeforeEach
    void setUp() {
        userStateStub.addPermissions((short) 78, ADD_ACCOUNT_ACTIVITY_NOTES);
    }


    @DisplayName("OPAL: POST /notes/add creates note for defendant account [PO-1566]")
    @JiraStory("PO-1566")
    void postNotesImpl(Logger log) throws Exception {
        // Arrange
        AddNoteRequestNotes request = AddNoteRequestNotes.builder()
            .activityNote(NoteCommon.builder()
                .noteText("test")
                .recordId("77")
                .recordType(RecordTypeEnum.DEFENDANT_ACCOUNTS)
                .noteType(NoteCommon.NoteTypeEnum.AA)
                .build())
            .build();


        final String payload = objectMapper.writeValueAsString(request);
        log.info(":testPostNotes payload: {}", payload);
        // Read the current version immediately before use
        final Integer versionBefore = defendantAccountVersionFor(77L);
        assertThat(versionBefore).isNotNull();
        // Act
        ResultActions result =
            mockMvc.perform(
                post(URL_BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
                    .header("authorization", userStateStub.getBearerToken())
                    .header(HttpHeaders.IF_MATCH, "\"" + versionBefore + "\"")
                    .header("Business-Unit-Id", "78")
                    .with(authentication(allFinesPermissionsToken()))
            );

        // Assert
        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":testPostNotes response:\n{}", ToJsonString.toPrettyJson(body));
        result.andExpect(status().isCreated());

        // Assert defendantAccount version incremented
        final Integer versionAfter = defendantAccountVersionFor(77L);
        assertThat(versionAfter).isEqualTo(versionBefore + 1);
    }

    @DisplayName("post notes for a defendant account ID that does not exist [PO-1566]")
    @JiraStory("PO-1566")
    void postNotes_IDNotFoundError(Logger log) throws Exception {

        AddNoteRequestNotes request = AddNoteRequestNotes.builder()
            .activityNote(NoteCommon.builder()
                .noteText("test")
                .recordId("7")
                .recordType(RecordTypeEnum.DEFENDANT_ACCOUNTS)
                .noteType(NoteCommon.NoteTypeEnum.AA)
                .build())
            .build();

        ResultActions resultActions =
            mockMvc.perform(
                post(URL_BASE + "/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("authorization", userStateStub.getBearerToken())
                    .header("If-Match", "1")
                    .header("Business-Unit-Id", "78")
                    .with(authentication(allFinesPermissionsToken()))
            );

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostNotes: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound());
    }

    @DisplayName("post notes for an invalid defendant account ID [PO-1566]")
    @JiraStory("PO-1566")
    void postNotes_badRequest(Logger log) throws Exception {

        AddNoteRequestNotes request = AddNoteRequestNotes.builder()
            .activityNote(NoteCommon.builder()
                .recordId("ABC1")
                .recordType(RecordTypeEnum.CREDITOR_ACCOUNTS)
                .build())
            .build();

        ResultActions resultActions =
            mockMvc.perform(
                post(URL_BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("authorization", userStateStub.getBearerToken())
                    .header("If-Match", "1")
                    .header("Business-Unit-Id", "78")
                    .with(authentication(allFinesPermissionsToken()))
            );

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostNotes: Response body:\n" + ToJsonString.toPrettyJson(body));
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("post notes - user without permission [PO-1566]")
    @JiraStory("PO-1566")
    void postNotes_UserWithoutPermission(FinesPermission permission) throws Exception {

        AddNoteRequestNotes request = AddNoteRequestNotes.builder()
            .activityNote(NoteCommon.builder()
                .noteText("test note")
                .recordId("77")
                .recordType(RecordTypeEnum.DEFENDANT_ACCOUNTS)
                .noteType(NoteCommon.NoteTypeEnum.AA)
                .build())
            .build();

        ResultActions resultActions = mockMvc.perform(
            post(URL_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("If-Match", "1")
                .header("Business-Unit-Id", "78")
                .with(authentication(permissionsToken((short) 78, permission)))
        );
        resultActions.andExpect(status().isForbidden())
            .andExpect(jsonPath("$.detail")
                .value("You do not have permission to access this resource"));
    }

    protected static Stream<FinesPermission> nonNotesPermissions() {
        return Stream.of(FinesPermission.values())
            .filter(permission -> permission != ADD_ACCOUNT_ACTIVITY_NOTES);
    }

    void postNotes_BusinessUnitAuthorization(BusinessUnitAuthorizationScenario scenario) throws Exception {

        userStateStub.setupWithNoPermissions();

        if (scenario.authenticationBusinessUnitId() != null) {
            userStateStub.addPermissions(scenario.authenticationBusinessUnitId(), scenario.permission());
        }

        AddNoteRequestNotes request = AddNoteRequestNotes.builder()
            .activityNote(NoteCommon.builder()
                .noteText("test note")
                .recordId("77")
                .recordType(RecordTypeEnum.DEFENDANT_ACCOUNTS)
                .noteType(NoteCommon.NoteTypeEnum.AA)
                .build())
            .build();

        var requestBuilder =
            post(URL_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
                .header(HttpHeaders.IF_MATCH, "1")
                .with(authentication(permissionsToken(
                    scenario.authenticationBusinessUnitId(), scenario.permission())));

        if (scenario.requestBusinessUnitId() != null) {
            requestBuilder.header("Business-Unit-Id", scenario.requestBusinessUnitId());
        }

        ResultActions resultActions = mockMvc.perform(requestBuilder);

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostNotes_BusinessUnitAuthorization [{}] response body:\n{}",
            scenario.displayName(), ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().is(scenario.expectedStatus().value()))
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value(scenario.expectedType()))
            .andExpect(jsonPath("$.title").value(scenario.expectedTitle()))
            .andExpect(jsonPath("$.detail").value(scenario.expectedDetail()))
            .andExpect(jsonPath("$.status").value(scenario.expectedStatus().value()))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    protected static Stream<BusinessUnitAuthorizationScenario> businessUnitAuthorizationScenarios() {
        return Stream.of(
            new BusinessUnitAuthorizationScenario(
                "missing Business-Unit-Id header",
                null,
                (short) 78,
                ADD_ACCOUNT_ACTIVITY_NOTES,
                HttpStatus.BAD_REQUEST,
                "https://hmcts.gov.uk/problems/missing-header",
                "Missing Required Header",
                "Required request header \"Business-Unit-Id\" is missing"
            ),
            new BusinessUnitAuthorizationScenario(
                "wrong business unit in header",
                (short) 99,
                (short) 78,
                ADD_ACCOUNT_ACTIVITY_NOTES,
                HttpStatus.FORBIDDEN,
                "https://hmcts.gov.uk/problems/forbidden",
                "Forbidden",
                "You do not have permission to access this resource"
            ),
            new BusinessUnitAuthorizationScenario(
                "permission present in another BU",
                (short) 78,
                (short) 99,
                ADD_ACCOUNT_ACTIVITY_NOTES,
                HttpStatus.FORBIDDEN,
                "https://hmcts.gov.uk/problems/forbidden",
                "Forbidden",
                "You do not have permission to access this resource"
            )
        );
    }

    protected record BusinessUnitAuthorizationScenario(
        String displayName,
        Short requestBusinessUnitId,
        Short authenticationBusinessUnitId,
        FinesPermission permission,
        HttpStatus expectedStatus,
        String expectedType,
        String expectedTitle,
        String expectedDetail
    ) {

        @Override
        public String toString() {
            return displayName;
        }
    }

    @DisplayName("post notes for a defendant account in legacy [PO-1975]")
    @JiraStory("PO-1975")
    void legacyTestAddNoteSuccess(Logger log) throws Exception {

        userStateStub.addPermissions((short) 78, ADD_ACCOUNT_ACTIVITY_NOTES);

        AddNoteRequestNotes request = AddNoteRequestNotes.builder()
            .activityNote(NoteCommon.builder()
                .noteText("test")
                .recordId("77")
                .recordType(RecordTypeEnum.DEFENDANT_ACCOUNTS)
                .noteType(NoteCommon.NoteTypeEnum.AA)
                .build())
            .build();

        ResultActions resultActions =
            mockMvc.perform(
                post(URL_BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("authorization", userStateStub.getBearerToken())
                    .header("If-Match", "1")
                    .header("Business-Unit-Id", 78)
                    .with(authentication(allFinesPermissionsToken()))
            );

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostNotes: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isCreated());
    }

    @DisplayName("post notes for a defendant account ID that does not exist in legacy [PO-1975]")
    @JiraStory("PO-1975")
    void legacyTestAddNote500Error(Logger log) throws Exception {

        userStateStub.addPermissions((short) 78, ADD_ACCOUNT_ACTIVITY_NOTES);

        AddNoteRequestNotes request = AddNoteRequestNotes.builder()
            .activityNote(NoteCommon.builder()
                .noteText("FAIL")
                .recordId("77")
                .recordType(RecordTypeEnum.DEFENDANT_ACCOUNTS)
                .noteType(NoteCommon.NoteTypeEnum.AA)
                .build())
            .build();

        ResultActions resultActions =
            mockMvc.perform(
                post(URL_BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("authorization", userStateStub.getBearerToken())
                    .header("If-Match", "5")
                    .header("Business-Unit-Id", "78")
                    .with(authentication(allFinesPermissionsToken()))
            );

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostNotes: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().is5xxServerError());

    }
}
