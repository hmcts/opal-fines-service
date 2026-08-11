package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.exception.InterfaceJobQueueException;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.messaging.InterfaceJobQueuePublisher;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1c-payment=true"
})
@Sql(
    scripts = "classpath:db/insertData/insert_interface_jobs_process_test_data.sql",
    executionPhase = BEFORE_TEST_METHOD
)
@Sql(
    scripts = "classpath:db/deleteData/delete_interface_jobs_process_test_data.sql",
    executionPhase = AFTER_TEST_METHOD
)
@DisplayName("Interface Jobs Process Controller Integration Tests")
class InterfaceJobsProcessControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL = "/interface-jobs/process";
    private static final String AUTH_HEADER = "Bearer integration-test-token";
    private static final Short BUSINESS_UNIT_77 = 77;
    private static final Short BUSINESS_UNIT_78 = 78;

    @Autowired
    private InterfaceJobRepository interfaceJobRepository;

    @MockitoBean
    private UserStateService userStateService;

    @MockitoBean
    private InterfaceJobQueuePublisher interfaceJobQueuePublisher;

    @MockitoBean
    private DynamicConfigService dynamicConfigService;

    @Test
    @DisplayName("PO-2593 - Authorised jobs are updated and queued")
    @JiraStory("PO-2593")
    @JiraEpic("PO-2468")
    void processJobs_updatesDatabaseAndPublishesOneMessagePerJob() throws Exception {
        when(dynamicConfigService.isLegacyMode()).thenReturn(false);
        allowBusinessUnits(BUSINESS_UNIT_77, BUSINESS_UNIT_78);

        mockMvc.perform(post(URL)
                .header(AUTHORIZATION, AUTH_HEADER)
                .contentType(APPLICATION_JSON)
                .content(request(990001L, BUSINESS_UNIT_77, true, 990002L, BUSINESS_UNIT_78, false)))
            .andExpect(status().isOk())
            .andExpect(content().string(""));

        verify(interfaceJobQueuePublisher).publish(List.of(990001L, 990002L));
        verify(dynamicConfigService).isLegacyMode();

        Map<Long, InterfaceJobEntity> jobs = findJobs(990001L, 990002L);
        assertEquals(InterfaceJobStatus.PROCESSING, jobs.get(990001L).getStatus());
        assertEquals(InterfaceJobStatus.PROCESSING, jobs.get(990002L).getStatus());
        assertNotNull(jobs.get(990001L).getStartedDateTime());
        assertNotNull(jobs.get(990002L).getStartedDateTime());

        assertTrue(isFileOverrideEnabled(990001L, 991001L));
        assertTrue(isFileOverrideEnabled(990001L, 991002L));
        assertFalse(isFileOverrideEnabled(990002L, 991003L));
    }

    @Test
    @DisplayName("PO-2593 - Missing business-unit permission returns 403")
    @JiraStory("PO-2593")
    @JiraEpic("PO-2468")
    void processJobs_rejectsAnyBusinessUnitWithoutPermission() throws Exception {
        allowBusinessUnits(BUSINESS_UNIT_77);

        mockMvc.perform(post(URL)
                .header(AUTHORIZATION, AUTH_HEADER)
                .contentType(APPLICATION_JSON)
                .content(request(990001L, BUSINESS_UNIT_77, true,
                    990002L, BUSINESS_UNIT_78, true)))
            .andExpect(status().isForbidden());

        verifyNoInteractions(interfaceJobQueuePublisher);
        assertJobStatus(990001L, InterfaceJobStatus.CREATED);
        assertJobStatus(990002L, InterfaceJobStatus.FAILED);
        assertFalse(isFileOverrideEnabled(990001L, 991001L));
        assertFalse(isFileOverrideEnabled(990002L, 991003L));
    }

    @Test
    @DisplayName("PO-2593 - Invalid status returns 409 without changes")
    @JiraStory("PO-2593")
    @JiraEpic("PO-2468")
    void processJobs_rejectsBatchContainingInvalidStatus() throws Exception {
        allowBusinessUnits(BUSINESS_UNIT_77);

        mockMvc.perform(post(URL)
                .header(AUTHORIZATION, AUTH_HEADER)
                .contentType(APPLICATION_JSON)
                .content(request(990001L, BUSINESS_UNIT_77, true,
                    990003L, BUSINESS_UNIT_77, true)))
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(409));

        verifyNoInteractions(interfaceJobQueuePublisher);
        assertJobStatus(990001L, InterfaceJobStatus.CREATED);
        assertJobStatus(990003L, InterfaceJobStatus.COMPLETED);
        assertFalse(isFileOverrideEnabled(990001L, 991001L));
        assertFalse(isFileOverrideEnabled(990003L, 991004L));
    }

    @Test
    @DisplayName("PO-2593 - Missing jobs return 404 without side effects")
    @JiraStory("PO-2593")
    @JiraEpic("PO-2468")
    void processJobs_returnsNotFoundForMissingJobs() throws Exception {
        allowBusinessUnits(BUSINESS_UNIT_77);

        mockMvc.perform(post(URL)
                .header(AUTHORIZATION, AUTH_HEADER)
                .contentType(APPLICATION_JSON)
                .content(request(999999L, BUSINESS_UNIT_77, true)))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(404));

        verifyNoInteractions(interfaceJobQueuePublisher);
    }

    @Test
    @DisplayName("PO-2593 - Empty job list returns 400")
    @JiraStory("PO-2593")
    @JiraEpic("PO-2468")
    void processJobs_rejectsEmptyJobList() throws Exception {
        mockMvc.perform(post(URL)
                .header(AUTHORIZATION, AUTH_HEADER)
                .contentType(APPLICATION_JSON)
                .content("{\"interface_jobs\":[]}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(interfaceJobQueuePublisher);
    }

    @Test
    @DisplayName("PO-2593 - Missing required item property returns 400")
    @JiraStory("PO-2593")
    @JiraEpic("PO-2468")
    void processJobs_rejectsMissingRequiredItemProperty() throws Exception {
        mockMvc.perform(post(URL)
                .header(AUTHORIZATION, AUTH_HEADER)
                .contentType(APPLICATION_JSON)
                .content("{\"interface_jobs\":[{"
                    + "\"interface_job_id\":990001,\"business_unit_id\":77,"
                    + "\"override_inhibits\":true},{"
                    + "\"interface_job_id\":990002,\"business_unit_id\":77}]}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(interfaceJobQueuePublisher);
    }

    @Test
    @DisplayName("PO-2593 - Queue failure rolls back database changes")
    @JiraStory("PO-2593")
    @JiraEpic("PO-2468")
    void processJobs_rollsBackDatabaseWhenQueuePublishingFails() throws Exception {
        allowBusinessUnits(BUSINESS_UNIT_77);
        doThrow(new InterfaceJobQueueException("broker unavailable", new RuntimeException("broker unavailable")))
            .when(interfaceJobQueuePublisher).publish(List.of(990001L));

        mockMvc.perform(post(URL)
                .header(AUTHORIZATION, AUTH_HEADER)
                .contentType(APPLICATION_JSON)
                .content(request(990001L, BUSINESS_UNIT_77, true)))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(503));

        assertJobStatus(990001L, InterfaceJobStatus.CREATED);
        assertFalse(isFileOverrideEnabled(990001L, 991001L));
    }

    private void allowBusinessUnits(Short... businessUnitIds) {
        when(userStateService.getPermittedBusinessUnitIds(
            List.of(businessUnitIds), PROCESS_AND_ALLOCATE_PAYMENTS))
            .thenReturn(List.of(businessUnitIds));
    }

    private Map<Long, InterfaceJobEntity> findJobs(Long... jobIds) {
        return interfaceJobRepository.findAllById(List.of(jobIds)).stream()
            .collect(Collectors.toMap(InterfaceJobEntity::getInterfaceJobId, Function.identity()));
    }

    private void assertJobStatus(long jobId, InterfaceJobStatus expectedStatus) {
        assertEquals(expectedStatus, findJob(jobId).getStatus());
    }

    private boolean isFileOverrideEnabled(long jobId, long fileId) {
        return findJob(jobId).getInterfaceFiles().stream()
            .filter(file -> file.getInterfaceFileId().equals(fileId))
            .map(InterfaceFileEntity::isOverrideInhibits)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Interface file not found: " + fileId));
    }

    private InterfaceJobEntity findJob(long jobId) {
        return interfaceJobRepository.findAllById(List.of(jobId)).stream()
            .findFirst()
            .orElseThrow(() -> new AssertionError("Interface job not found: " + jobId));
    }

    private static String request(Object... values) {
        StringBuilder body = new StringBuilder("{\"interface_jobs\":[");
        for (int index = 0; index < values.length; index += 3) {
            if (index > 0) {
                body.append(',');
            }
            appendJob(body, (Long) values[index], (Short) values[index + 1], (Boolean) values[index + 2]);
        }
        return body.append("]}").toString();
    }

    private static void appendJob(StringBuilder body, long jobId, short businessUnitId, boolean override) {
        body.append("{\"interface_job_id\":").append(jobId)
            .append(",\"business_unit_id\":").append(businessUnitId)
            .append(",\"override_inhibits\":").append(override).append('}');
    }
}
