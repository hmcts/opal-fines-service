package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_PAYMENT;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=true",
    "launchdarkly.sdk-key=test-sdk-key",
    "launchdarkly.default-flag-values.release-1c-payment=false"
})
@DisplayName("Interface Jobs Create Feature Toggle Integration Tests")
class InterfaceJobsCreateFeatureToggleDisabledIT extends AbstractIntegrationTest {

    private static final String URL = "/interface-jobs";

    @MockitoBean
    private LDClientInterface ldClient;

    @MockitoBean
    private InterfaceJobService interfaceJobService;

    @Test
    @DisplayName("PO-2577 FT.01 - returns feature disabled problem when release-1c-payment is disabled")
    @JiraStory("PO-2577")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-2577-FT-01")
    void returnsFeatureDisabledProblemWhenRelease1cPaymentIsDisabled() throws Exception {
        when(ldClient.boolVariation(eq(RELEASE_1C_PAYMENT), any(LDContext.class), anyBoolean()))
            .thenReturn(false);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("""
                         {
                           "interface_jobs": [
                             {
                               "file_name": "feature-disabled.dat",
                               "source": "NATWEST",
                               "records": "[{\\"account\\":\\"abc123\\"}]",
                               "business_unit_id": 2577,
                               "interface_name": "Feature Disabled Interface Jobs",
                               "created_datetime": "2026-07-14T10:00:00"
                             }
                           ]
                         }
                         """))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.retriable").value(false));

        verify(ldClient, times(1)).boolVariation(eq(RELEASE_1C_PAYMENT), any(LDContext.class), anyBoolean());
        verify(interfaceJobService, times(0)).create(any());
    }
}
