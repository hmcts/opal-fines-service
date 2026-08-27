package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.PersistenceException;
import jakarta.persistence.QueryTimeoutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsProcessRequest;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1c-payment=true"
})
@DisplayName("Interface Jobs Process Common Response Integration Tests")
class ProcessInterfaceJobsResponseTest extends AbstractIntegrationTest {

    private static final String URL = "/interface-jobs/process";
    private static final String AUTH_HEADER = "Bearer integration-test-token";
    private static final String REQUEST = """
        {
          "interface_jobs": [
            {
              "interface_job_id": 990001,
              "business_unit_id": 77,
              "override_inhibits": true
            }
          ]
        }
        """;

    @MockitoBean
    private InterfaceJobService interfaceJobService;

    @MockitoBean
    private DynamicConfigService dynamicConfigService;

    @Test
    @DisplayName("PO-2593 - Database timeout returns 408")
    @JiraStory("PO-2593")
    @JiraEpic("PO-2468")
    void processJobs_returnsRequestTimeoutForDatabaseTimeout() throws Exception {
        doThrow(new QueryTimeoutException("database timeout"))
            .when(interfaceJobService).process(any(InterfaceJobsProcessRequest.class));

        mockMvc.perform(post(URL)
                .header(AUTHORIZATION, AUTH_HEADER)
                .contentType(APPLICATION_JSON)
                .content(REQUEST))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(408))
            .andExpect(jsonPath("$.retriable").value(true));
    }

    @Test
    @DisplayName("PO-2593 - Persistence failure returns 500")
    @JiraStory("PO-2593")
    @JiraEpic("PO-2468")
    void processJobs_returnsInternalServerErrorForPersistenceFailure() throws Exception {
        doThrow(new PersistenceException("database failure"))
            .when(interfaceJobService).process(any(InterfaceJobsProcessRequest.class));

        mockMvc.perform(post(URL)
                .header(AUTHORIZATION, AUTH_HEADER)
                .contentType(APPLICATION_JSON)
                .content(REQUEST))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.retriable").value(false));
    }
}
