package uk.gov.hmcts.opal.controllers.shared;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration-with-spring-security", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1c-payment=true"
})
@DisplayName("Interface Jobs Process Controller Security Integration Tests")
class ProcessInterfaceJobsSecurityIntegrationTest extends AbstractIntegrationWithSecurityTest {

    private static final String URL = "/interface-jobs/process";
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

    @Test
    @DisplayName("PO-2593 - Missing access token returns 401")
    @JiraStory("PO-2593")
    @JiraEpic("PO-2468")
    void processJobs_returnsUnauthorizedWhenAccessTokenIsMissing() throws Exception {
        mockMvc.perform(post(URL)
                .contentType(APPLICATION_JSON)
                .content(REQUEST))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PO-2593 - Invalid access token returns 401")
    @JiraStory("PO-2593")
    @JiraEpic("PO-2468")
    void processJobs_returnsUnauthorizedWhenAccessTokenIsInvalid() throws Exception {
        mockMvc.perform(post(URL)
                .header(AUTHORIZATION, "Bearer invalid-token")
                .contentType(APPLICATION_JSON)
                .content(REQUEST))
            .andExpect(status().isUnauthorized());
    }
}
