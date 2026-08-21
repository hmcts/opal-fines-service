package uk.gov.hmcts.opal.controllers;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1c-payment=false"
})
@DisplayName("Interface Jobs Process Feature Toggle Disabled Integration Tests")
class InterfaceJobsProcessFeatureToggleDisabledIntegrationTest extends AbstractIntegrationTest {

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

    @Test
    @DisplayName("PO-2593 - Disabled processing returns feature-disabled response")
    @JiraStory("PO-2593")
    @JiraEpic("PO-2468")
    void processJobs_returnsFeatureDisabledWhenRelease1cPaymentIsDisabled() throws Exception {
        mockMvc.perform(post(URL)
                .header(AUTHORIZATION, AUTH_HEADER)
                .contentType(APPLICATION_JSON)
                .content(REQUEST))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.retriable").value(false));

        verifyNoInteractions(interfaceJobService);
    }
}
