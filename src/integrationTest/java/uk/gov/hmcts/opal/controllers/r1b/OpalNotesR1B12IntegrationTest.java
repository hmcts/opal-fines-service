package uk.gov.hmcts.opal.controllers.r1b;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.generated.model.AddNoteRequestNotes;
import uk.gov.hmcts.opal.generated.model.NoteCommon;
import uk.gov.hmcts.opal.generated.model.NoteCommon.NoteTypeEnum;
import uk.gov.hmcts.opal.generated.model.NoteCommon.RecordTypeEnum;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b-1-2=true"
})
@Slf4j(topic = "opal.OpalNotesR1B12IntegrationTest")
@DisplayName("Opal Notes R1B12 Integration Test")
@Sql(
    scripts = {
        "classpath:db/insertData/insert_into_defendant_accounts.sql",
        "classpath:db/insertData/insert_into_creditor_accounts.sql"
    }, executionPhase = BEFORE_TEST_CLASS
)
@Sql(
    scripts = {
        "classpath:db/deleteData/delete_from_creditor_accounts.sql",
        "classpath:db/deleteData/delete_from_defendant_accounts.sql"
    },
    executionPhase = AFTER_TEST_CLASS
)
public class OpalNotesR1B12IntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private NoteRepository noteRepository;

    @MockitoBean
    private LoggingService loggingService;

    @BeforeEach
    void setup() {
        userStateStub.addPermissions((short) 78, ADD_ACCOUNT_ACTIVITY_NOTES, SEARCH_AND_VIEW_ACCOUNTS);
    }

    @Test
    @JiraEpic("PO-812")
    @JiraStory("PO-11203")
    @DisplayName("Add Note Stores BusinessUnitUserId In PostedBy For Defendant Accounts")
    void addNote_storesBusinessUnitUserIdInPostedByForDefendantAccounts() throws Exception {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        AddNoteRequestNotes requestNotes = createAddNoteRequestNotes(77L, RecordTypeEnum.DEFENDANT_ACCOUNTS);

        MvcResult result = mockMvc.perform(post("/notes")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
            .header(HttpHeaders.IF_MATCH, "0")
            .header("Business-Unit-Id", "78")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestNotes)))
            .andExpect(status().isCreated()).andReturn();

        Long noteId = Long.valueOf(result.getResponse().getContentAsString());
        NoteEntity savedNote = noteRepository.findById(noteId).orElseThrow();

        assertThat(savedNote.getAssociatedRecordType()).isEqualTo(AssociatedRecordType.DEFENDANT_ACCOUNTS);
        assertThat(savedNote.getAssociatedRecordId()).isEqualTo(Long.toString(77L));
        assertThat(savedNote.getBusinessUnitUserId()).isEqualTo("L078JG").isNotEqualTo("78");

        mockMvc.perform(get("/defendant-accounts/77/history")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
                .header("Business-Unit-Id", "78"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems[0].type").value("Note"))
            .andExpect(jsonPath("$.historyItems[0].details.noteText")
                .value("Business-unit user integration test"))
            .andExpect(jsonPath("$.historyItems[0].postedDetails.posted_by").value("L078JG"))
            .andExpect(jsonPath("$.historyItems[0].postedDetails.posted_by_name").value("Pablo"));
    }

    @Test
    @JiraEpic("PO-812")
    @JiraStory("PO-11203")
    @DisplayName("Add Note Stores BusinessUnitUserId In PostedBy For Creditor Accounts")
    void addNote_storesBusinessUnitUserIdInPostedByForCreditorAccounts() throws Exception {
        AddNoteRequestNotes requestNotes = createAddNoteRequestNotes(100000000001L, RecordTypeEnum.CREDITOR_ACCOUNTS);

        MvcResult result = mockMvc.perform(post("/notes")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken())
                .header(HttpHeaders.IF_MATCH, "1")
                .header("Business-Unit-Id", "78")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestNotes)))
            .andExpect(status().isCreated()).andReturn();

        Long noteId = Long.valueOf(result.getResponse().getContentAsString());
        NoteEntity savedNote = noteRepository.findById(noteId).orElseThrow();

        assertThat(savedNote.getAssociatedRecordType()).isEqualTo(AssociatedRecordType.CREDITOR_ACCOUNTS);
        assertThat(savedNote.getAssociatedRecordId()).isEqualTo(Long.toString(100000000001L));
        assertThat(savedNote.getBusinessUnitUserId()).isEqualTo("L078JG").isNotEqualTo("78");
    }

    private AddNoteRequestNotes createAddNoteRequestNotes(long recordId, RecordTypeEnum recordType) {
        return AddNoteRequestNotes.builder()
            .activityNote(NoteCommon.builder()
                .noteText("Business-unit user integration test")
                .recordId(Long.toString(recordId))
                .recordType(recordType)
                .noteType(NoteTypeEnum.AA)
                .build())
            .build();
    }
}
