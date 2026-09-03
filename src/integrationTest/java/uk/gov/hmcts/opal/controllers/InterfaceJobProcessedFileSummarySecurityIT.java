package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1c-payment=true",
    "launchdarkly.default-flag-values.is-legacy-mode=false"
})
@DisplayName("Interface Job Processed File Summary Security Integration Tests")
@Sql(scripts = "classpath:db/insertData/insert_into_interface_job_processed_file_summary.sql",
     executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_interface_job_processed_file_summary.sql",
     executionPhase = AFTER_TEST_METHOD)
class InterfaceJobProcessedFileSummarySecurityIT extends AbstractIntegrationWithSecurityTest {

    private static final String URL = "/interface-jobs/257601/processed-file-summary";
    private static final short BUSINESS_UNIT_ID = 2576;

    @MockitoBean
    private LoggingService loggingService;

    @Test
    @DisplayName("PO-2576 INT.07 - rejects a request without an access token")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void rejectsRequestWithoutAccessToken() throws Exception {
        mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("PO-2576 INT.07 - rejects a request with an invalid access token")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void rejectsRequestWithInvalidAccessToken() throws Exception {
        mockMvc.perform(get(URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("PO-2576 INT.07 - rejects a valid caller without BU permission")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void rejectsValidCallerWithoutBusinessUnitPermission() throws Exception {
        userStateStub.setupWithNoPermissions();

        mockMvc.perform(get(URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("PO-2576 INT.07 - permits a valid caller with BU permission")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void permitsValidCallerWithBusinessUnitPermission() throws Exception {
        userStateStub.addPermissions(BUSINESS_UNIT_ID,
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        mockMvc.perform(get(URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.file_name").value("processed-summary.dat"));
    }

}
