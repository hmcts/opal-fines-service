package uk.gov.hmcts.opal.controllers.shared;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_PAYMENT;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
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
    "launchdarkly.enabled=true",
    "launchdarkly.sdk-key=test-sdk-key",
    "launchdarkly.default-flag-values.release-1c-payment=false"
})
@DisplayName("Interface Jobs Process Feature Toggle Enabled Integration Tests")
class ProcessInterfaceJobsFlagOnTest extends AbstractIntegrationTest {

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
    private LDClientInterface ldClient;

    @MockitoBean
    private InterfaceJobService interfaceJobService;

    @Test
    @DisplayName("PO-2593 - Enabled processing proceeds and evaluates once")
    @JiraStory("PO-2593")
    @JiraEpic("PO-2468")
    void processJobs_proceedsWhenRelease1cPaymentIsEnabledAndEvaluatesOnce() throws Exception {
        when(ldClient.boolVariation(eq(RELEASE_1C_PAYMENT), any(LDContext.class), anyBoolean()))
            .thenReturn(true);

        mockMvc.perform(post(URL)
                .header(AUTHORIZATION, AUTH_HEADER)
                .contentType(APPLICATION_JSON)
                .content(REQUEST))
            .andExpect(status().isOk())
            .andExpect(content().string(""));

        verify(interfaceJobService).process(any());
        verify(ldClient, times(1))
            .boolVariation(eq(RELEASE_1C_PAYMENT), any(LDContext.class), anyBoolean());
    }
}
