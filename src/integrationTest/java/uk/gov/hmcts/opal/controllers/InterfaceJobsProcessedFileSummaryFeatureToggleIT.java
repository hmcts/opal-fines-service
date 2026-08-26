package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=true",
    "launchdarkly.sdk-key=test-sdk-key",
    "launchdarkly.default-flag-values.release-1c-payment=false"
})
@DisplayName("Interface Jobs Processed File Summary Feature Toggle Integration Tests")
class InterfaceJobsProcessedFileSummaryFeatureToggleIT extends AbstractIntegrationTest {

    private static final long INTERFACE_JOB_ID = 123L;
    private static final String URL = "/interface-jobs/" + INTERFACE_JOB_ID + "/processed-file-summary";

    @MockitoBean
    private LDClientInterface ldClient;

    @MockitoBean
    private DynamicConfigService dynamicConfigService;

    @Test
    @DisplayName("PO-2576 AC Feature Flags - returns feature disabled response when release-1c-payment is disabled")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void returnsFeatureDisabledWhenRelease1cPaymentIsDisabled() throws Exception {
        when(ldClient.boolVariation(eq(RELEASE_1C_PAYMENT), any(LDContext.class), anyBoolean()))
            .thenReturn(false);

        mockMvc.perform(get(URL)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.retriable").value(false));

        verify(ldClient, times(1)).boolVariation(eq(RELEASE_1C_PAYMENT), any(LDContext.class), anyBoolean());
    }

    @Test
    @DisplayName("PO-2576 AC Feature Flags - allows endpoint logic when release-1c-payment is enabled")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void allowsEndpointLogicWhenRelease1cPaymentIsEnabled() throws Exception {
        when(ldClient.boolVariation(eq(RELEASE_1C_PAYMENT), any(LDContext.class), anyBoolean()))
            .thenReturn(true);

        mockMvc.perform(get(URL)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));

        verify(ldClient, times(1)).boolVariation(eq(RELEASE_1C_PAYMENT), any(LDContext.class), anyBoolean());
    }

    @Test
    @DisplayName("PO-2576 INT.05 - rejects unsupported Accept header")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void rejectsUnsupportedAcceptHeader() throws Exception {
        when(ldClient.boolVariation(eq(RELEASE_1C_PAYMENT), any(LDContext.class), anyBoolean()))
            .thenReturn(true);

        mockMvc.perform(get(URL)
                .accept(MediaType.APPLICATION_XML))
            .andExpect(status().isNotAcceptable());

        verify(ldClient, times(0)).boolVariation(eq(RELEASE_1C_PAYMENT), any(LDContext.class), anyBoolean());
    }

    @Test
    @DisplayName("PO-2576 INT.09 - returns feature disabled response outside OPAL mode")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void returnsFeatureDisabledResponseOutsideOpalMode() throws Exception {
        when(ldClient.boolVariation(eq(RELEASE_1C_PAYMENT), any(LDContext.class), anyBoolean()))
            .thenReturn(true);
        when(dynamicConfigService.isLegacyMode()).thenReturn(true);

        mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"));
    }
}
