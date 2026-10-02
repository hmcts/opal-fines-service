package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.logging.LogUtil;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1c-payment=true"
})
@DisplayName("Interface Job Processed File Summary Controller Integration Tests")
@Sql(scripts = "classpath:db/insertData/insert_into_interface_job_processed_file_summary.sql",
     executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_interface_job_processed_file_summary.sql",
     executionPhase = AFTER_TEST_METHOD)
class InterfaceJobProcessedFileSummaryIT extends AbstractIntegrationTest {

    private static final Long INTERFACE_JOB_ID = 257601L;
    private static final Long IGNORED_INTERFACE_JOB_ID = 257602L;
    private static final Long SUMMARY_MISSING_INTERFACE_JOB_ID = 257603L;
    private static final long UNKNOWN_INTERFACE_JOB_ID = 257699L;
    private static final Short BUSINESS_UNIT_ID = 2576;
    private static final String URL = "/interface-jobs/" + INTERFACE_JOB_ID + "/processed-file-summary";
    private static final String IGNORED_URL = "/interface-jobs/" + IGNORED_INTERFACE_JOB_ID
        + "/processed-file-summary";
    private static final int SUMMARY_RESPONSE_FIELD_COUNT = 7;
    private static final int MESSAGE_GROUP_RESPONSE_FIELD_COUNT = 2;
    private static final int MESSAGE_RESPONSE_FIELD_COUNT = 3;
    private static final Instant PDPO_LOGGED_AT = Instant.parse("2026-08-26T12:00:00Z");
    private static final String REQUEST_IP_ADDRESS = "192.0.2.10";

    @MockitoBean
    private UserStateService userStateService;

    @MockitoBean
    private LoggingService loggingService;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUpTestDoubles() {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);
        when(clock.instant()).thenReturn(PDPO_LOGGED_AT);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Test
    @DisplayName("PO-2576 INT.01/INT.04/INT.06 - returns the permitted processed file summary")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void returnsPermittedProcessedFileSummary() throws Exception {

        // Arrange
        stubPermittedOpalUser();

        // Act & Assert
        mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", aMapWithSize(SUMMARY_RESPONSE_FIELD_COUNT)))
            .andExpect(jsonPath("$.file_name").value("processed-summary.dat"))
            .andExpect(jsonPath("$.source").value("NATWEST"))
            .andExpect(jsonPath("$.business_unit_name").value("Processed Summary BU"))
            .andExpect(jsonPath("$.total_amount").value(123.45))
            .andExpect(jsonPath("$.total_records").value(3))
            .andExpect(jsonPath("$.total_errors").value(1))
            .andExpect(jsonPath("$.interface_messages", hasSize(2)))
            .andExpect(jsonPath("$.interface_messages[0].message_text").value("records_read"))
            .andExpect(jsonPath("$.interface_messages[0]", aMapWithSize(MESSAGE_GROUP_RESPONSE_FIELD_COUNT)))
            .andExpect(jsonPath("$.interface_messages[0].messages", hasSize(2)))
            .andExpect(jsonPath("$.interface_messages[0].messages[0]", aMapWithSize(MESSAGE_RESPONSE_FIELD_COUNT)))
            .andExpect(jsonPath("$.interface_messages[0].messages[0].interface_messages_id").value(257631))
            .andExpect(jsonPath("$.interface_messages[0].messages[0].message_data.count").value(3))
            .andExpect(jsonPath("$.interface_messages[0].messages[1].interface_messages_id").value(257633))
            .andExpect(jsonPath("$.interface_messages[0].messages[1].message_data.count").value(4))
            .andExpect(jsonPath("$.interface_messages[1].message_text").value("records_rejected"))
            .andExpect(jsonPath("$.interface_messages[1].messages", hasSize(1)));
    }

    @Test
    @DisplayName("PO-2576 PDPO INT.01/INT.02 - logs the payer consultation with mapped details")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void logsPayerConsultationWithMappedDetails() throws Exception {

        // Arrange
        stubPermittedOpalUser();
        try (MockedStatic<LogUtil> logUtil = Mockito.mockStatic(LogUtil.class)) {
            logUtil.when(LogUtil::getIpAddress).thenReturn(REQUEST_IP_ADDRESS);

            // Act & Assert
            mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

        // Assert
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService).personalDataAccessLogAsync(captor.capture());
        PersonalDataProcessingLogDetails details = captor.getValue();

        assertEquals("View File Processing Summary", details.getBusinessIdentifier());
        assertEquals("Consultation", details.getCategory().getJsonValue());
        assertEquals("1", details.getCreatedBy().getIdentifier());
        assertEquals("OPAL_USER_ID", details.getCreatedBy().getType().getType());
        assertEquals("PAYER", details.getIndividuals().getFirst().getType().getType());
        assertNull(details.getRecipient());
        assertEquals(REQUEST_IP_ADDRESS, details.getIpAddress());
        assertEquals(PDPO_LOGGED_AT.atOffset(ZoneOffset.UTC), details.getCreatedAt());
    }

    @Test
    @DisplayName("PO-2576 INT.02 - rejects a user without business-unit permission")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void rejectsUserWithoutBusinessUnitPermission() throws Exception {

        when(userStateService.getPermittedBusinessUnitIds(
            List.of(BUSINESS_UNIT_ID), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of());

        mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("PO-2576 INT.03 - returns not found for an unknown interface job")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void returnsNotFoundForUnknownInterfaceJob() throws Exception {

        assertNotFound("/interface-jobs/" + UNKNOWN_INTERFACE_JOB_ID + "/processed-file-summary");
    }

    @Test
    @DisplayName("PO-2576 INT.03 - returns not found when an interface job has no processed file summary")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void returnsNotFoundWhenInterfaceJobHasNoProcessedFileSummary() throws Exception {

        assertNotFound("/interface-jobs/" + SUMMARY_MISSING_INTERFACE_JOB_ID + "/processed-file-summary");
    }

    @Test
    @DisplayName("PO-2576 PDPO INT.03 - rejects a caller without an OPAL user identity")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void rejectsCallerWithoutOpalUserIdentity() throws Exception {

        when(userStateService.getPermittedBusinessUnitIds(
            List.of(BUSINESS_UNIT_ID), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS))
            .thenReturn(List.of(BUSINESS_UNIT_ID));
        when(userStateService.getUserStateFromSecurityContext())
            .thenThrow(new AccessDeniedException("Unexpected token type"));

        mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verify(loggingService, never()).personalDataAccessLogAsync(any());
    }

    @Test
    @DisplayName("PO-2576 INT.08; PDPO INT.04 - repeated requests return the same summary and log independently")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void repeatedRequestsAreIdempotentAndLoggedIndependently() throws Exception {

        stubPermittedOpalUser();

        MvcResult firstResult = mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        MvcResult secondResult = mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        assertEquals(firstResult.getResponse().getContentAsString(), secondResult.getResponse().getContentAsString());
        verify(loggingService, times(2)).personalDataAccessLogAsync(any());
    }

    @Test
    @DisplayName("PO-2576 PDPO INT.05 - logging failure does not change the successful response")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void loggingFailureDoesNotChangeSuccessfulResponse() throws Exception {

        stubPermittedOpalUser();
        when(loggingService.personalDataAccessLogAsync(any())).thenThrow(new RuntimeException("logging unavailable"));

        mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.file_name").value("processed-summary.dat"));

        verify(loggingService, times(1)).personalDataAccessLogAsync(any());
    }

    @Test
    @DisplayName("PO-2576 INT.01 - returns a permitted ignored file summary without a till")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void returnsPermittedIgnoredFileSummaryWithoutTill() throws Exception {

        stubPermittedOpalUser();

        mockMvc.perform(get(IGNORED_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.file_name").value("ignored-summary.dat"))
            .andExpect(jsonPath("$.business_unit_name").value("Processed Summary BU"))
            .andExpect(jsonPath("$.total_amount").value(50.00))
            .andExpect(jsonPath("$.total_records").value(2))
            .andExpect(jsonPath("$.total_errors").value(1))
            .andExpect(jsonPath("$.interface_messages[0].message_text").value("records_ignored"));

        verify(loggingService, times(1)).personalDataAccessLogAsync(any());
    }

    @Test
    @DisplayName("PO-2576 INT.02 - rejects an ignored file when its interface job BU is not permitted")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void rejectsIgnoredFileWithoutInterfaceJobBusinessUnitPermission() throws Exception {

        when(userStateService.getPermittedBusinessUnitIds(
            List.of(BUSINESS_UNIT_ID), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(List.of());

        mockMvc.perform(get(IGNORED_URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verify(loggingService, never()).personalDataAccessLogAsync(any());
    }

    private void stubPermittedOpalUser() {
        when(userStateService.getPermittedBusinessUnitIds(
            List.of(BUSINESS_UNIT_ID), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS))
            .thenReturn(List.of(BUSINESS_UNIT_ID));
        when(userStateService.getUserStateFromSecurityContext()).thenReturn(UserStateV2.builder()
            .userId(1L)
            .username("test-user")
            .name("Test User")
            .build());
    }

    private void assertNotFound(String url) throws Exception {
        mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(header().exists("operation_id"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));

        verify(loggingService, never()).personalDataAccessLogAsync(any());
    }
}
