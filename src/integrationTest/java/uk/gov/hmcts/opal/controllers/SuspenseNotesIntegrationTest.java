package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.MANAGE_SUSPENSE_ITEMS;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_SUSPENSE;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_SUSPENSE_ENABLED_PROPERTY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.NoteType;
import uk.gov.hmcts.opal.generated.model.AddNoteRequestNotes;
import uk.gov.hmcts.opal.generated.model.NoteCommon;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.SuspenseItemRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@Sql("classpath:db/insertData/insert_into_suspense_notes.sql")
@Sql(scripts = "classpath:db/deleteData/delete_from_suspense_notes.sql", executionPhase = AFTER_TEST_METHOD)
class SuspenseNotesIntegrationTest extends AbstractIntegrationTest {

    private static final short BUSINESS_UNIT_ID = 10403;
    private static final long ITEM_ID = 99000000104031L;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private SuspenseItemRepository suspenseItemRepository;

    @MockitoBean
    private FeatureToggleApi featureToggleApi;

    @BeforeEach
    void setUpSuspenseNotes() {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(BUSINESS_UNIT_ID, MANAGE_SUSPENSE_ITEMS);
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(RELEASE_1B, RELEASE_1B_ENABLED_PROPERTY, false))
            .thenReturn(true);
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            RELEASE_1C_SUSPENSE, RELEASE_1C_SUSPENSE_ENABLED_PROPERTY)).thenReturn(true);
    }

    @Test
    @JiraStory("PO-10403")
    @JiraEpic("PO-9048")
    void createsSuspenseNoteWithManageSuspenseItemsPermissionAlone() throws Exception {
        String noteId = addNote("/notes", ITEM_ID, "3", BUSINESS_UNIT_ID)
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().getResponse().getContentAsString();

        NoteEntity note = noteRepository.findById(Long.valueOf(noteId)).orElseThrow();
        assertThat(note.getAssociatedRecordType()).isEqualTo(AssociatedRecordType.SUSPENSE_ITEMS);
        assertThat(note.getAssociatedRecordId()).isEqualTo(String.valueOf(ITEM_ID));
        assertThat(note.getNoteType()).isEqualTo(NoteType.AA);
        assertThat(note.getNoteText()).isEqualTo("Suspense item note");
        assertThat(note.getBusinessUnitUserId()).isEqualTo("L010403JG");
        assertThat(note.getPostedByUsername()).isNotBlank();
        assertThat(note.getPostedDate()).isNotNull();
        assertThat(suspenseItemRepository.findById(ITEM_ID).orElseThrow().getVersionNumber()).isEqualTo(4L);
    }

    @Test
    @JiraStory("PO-10403")
    @JiraEpic("PO-9048")
    void addsFirstNoteToItemWithUninitialisedVersion() throws Exception {
        addNote("/notes", ITEM_ID + 1, "0", BUSINESS_UNIT_ID).andExpect(status().isCreated());

        assertThat(suspenseItemRepository.findById(ITEM_ID + 1).orElseThrow().getVersionNumber()).isEqualTo(1L);
    }

    @ParameterizedTest
    @EnumSource(value = FinesPermission.class, names = "MANAGE_SUSPENSE_ITEMS", mode = EnumSource.Mode.EXCLUDE)
    @JiraStory("PO-10403")
    @JiraEpic("PO-9048")
    void rejectsOtherPermissionsIncludingAccountNotesPermission(FinesPermission permission) throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(BUSINESS_UNIT_ID, permission);

        assertForbidden(addNote("/notes", ITEM_ID, "3", BUSINESS_UNIT_ID));

        assertNoNoteCreated();
    }

    @Test
    @JiraStory("PO-10403")
    @JiraEpic("PO-9048")
    void rejectsManageSuspensePermissionInAnotherBusinessUnit() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 78, MANAGE_SUSPENSE_ITEMS);

        assertForbidden(addNote("/notes", ITEM_ID, "3", BUSINESS_UNIT_ID));

        assertNoNoteCreated();
    }

    @Test
    @JiraStory("PO-10403")
    @JiraEpic("PO-9048")
    void rejectsItemOutsideProvidedBusinessUnit() throws Exception {
        userStateStub.addPermissions((short) 78, MANAGE_SUSPENSE_ITEMS);

        assertForbidden(addNote("/notes", ITEM_ID, "3", (short) 78));

        assertNoNoteCreated();
    }

    @Test
    @JiraStory("PO-10403")
    @JiraEpic("PO-9048")
    void rejectsSuspenseNotesWhenFeatureDisabled() throws Exception {
        when(featureToggleApi.isFeatureEnabledWithPropertyValueDefault(
            RELEASE_1C_SUSPENSE, RELEASE_1C_SUSPENSE_ENABLED_PROPERTY)).thenReturn(false);

        addNote("/notes", ITEM_ID, "3", BUSINESS_UNIT_ID).andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"))
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.retriable").value(false));

        assertNoNoteCreated();
    }

    @Test
    @JiraStory("PO-10403")
    @JiraEpic("PO-9048")
    void rejectsStaleVersionWithoutSavingNote() throws Exception {
        addNote("/notes", ITEM_ID, "2", BUSINESS_UNIT_ID)
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/optimistic-locking"))
            .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
            .andExpect(jsonPath("$.retriable").value(false));

        assertNoNoteCreated();
    }

    @Test
    @JiraStory("PO-10403")
    @JiraEpic("PO-9048")
    void returnsNotFoundForMissingSuspenseItem() throws Exception {
        addNote("/notes", 99000000104039L, "3", BUSINESS_UNIT_ID)
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"))
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.retriable").value(false));

        assertNoNoteCreated();
    }

    @Test
    @JiraStory("PO-10403")
    @JiraEpic("PO-9048")
    void oldAddPathDoesNotCreateNote() throws Exception {
        addNote("/notes/add", ITEM_ID, "3", BUSINESS_UNIT_ID).andExpect(status().isNotFound());

        assertNoNoteCreated();
    }

    @Test
    @JiraStory("PO-10403")
    @JiraEpic("PO-9048")
    void rejectsSuspenseNoteWhenIfMatchHeaderIsMissing() throws Exception {
        AddNoteRequestNotes request = suspenseNoteRequest(ITEM_ID);

        mockMvc.perform(post("/notes").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Business-Unit-Id", BUSINESS_UNIT_ID)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/missing-header"))
            .andExpect(jsonPath("$.title").value("Missing Required Header"))
            .andExpect(jsonPath("$.detail").value("Required request header \"If-Match\" is missing"))
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.retriable").value(false));

        assertNoNoteCreated();
    }

    @Test
    @JiraStory("PO-10403")
    @JiraEpic("PO-9048")
    void rejectsSuspenseNoteWhenBusinessUnitIdHeaderIsMissing() throws Exception {
        AddNoteRequestNotes request = suspenseNoteRequest(ITEM_ID);

        mockMvc.perform(post("/notes").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header(HttpHeaders.IF_MATCH, "\"3\"")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/missing-header"))
            .andExpect(jsonPath("$.title").value("Missing Required Header"))
            .andExpect(jsonPath("$.detail").value("Required request header \"Business-Unit-Id\" is missing"))
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.retriable").value(false));

        assertNoNoteCreated();
    }

    @Test
    @JiraStory("PO-10403")
    @JiraEpic("PO-9048")
    void rejectsSuspenseNoteWhenActivityNoteIsMissing() throws Exception {
        mockMvc.perform(post("/notes").contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .header(HttpHeaders.IF_MATCH, "\"3\"")
                .header("Business-Unit-Id", BUSINESS_UNIT_ID)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.retriable").value(false));

        assertNoNoteCreated();
    }

    private ResultActions addNote(String path, long itemId, String version, short businessUnitId) throws Exception {
        AddNoteRequestNotes request = suspenseNoteRequest(itemId);

        return mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)).header("If-Match", "\"" + version + "\"")
            .header("Business-Unit-Id", businessUnitId)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor()));
    }

    private AddNoteRequestNotes suspenseNoteRequest(long itemId) {
        return AddNoteRequestNotes.builder().activityNote(NoteCommon.builder()
            .recordType(NoteCommon.RecordTypeEnum.SUSPENSE_ITEMS).recordId(String.valueOf(itemId))
            .noteType(NoteCommon.NoteTypeEnum.AA).noteText("Suspense item note").build()).build();
    }

    private void assertForbidden(ResultActions result) throws Exception {
        result.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    private void assertNoNoteCreated() {
        assertThat(noteRepository.countByAssociatedRecordId(String.valueOf(ITEM_ID))).isZero();
        assertThat(suspenseItemRepository.findById(ITEM_ID).orElseThrow().getVersionNumber()).isEqualTo(3L);
    }
}
