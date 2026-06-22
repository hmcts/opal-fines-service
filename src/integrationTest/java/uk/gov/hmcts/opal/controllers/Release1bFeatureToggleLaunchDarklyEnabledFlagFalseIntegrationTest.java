package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.RequestBuilder;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=true",
    "launchdarkly.sdk-key=test-sdk-key",
    "launchdarkly.default-flag-values.release-1b=true"
})
class Release1bFeatureToggleLaunchDarklyEnabledFlagFalseIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private LDClientInterface ldClient;

    @ParameterizedTest(name = "{0}")
    @MethodSource("uk.gov.hmcts.opal.controllers.util.Release1bFeatureToggleRequestUtil#gatedRequests")
    @JiraStory("PO-3762")
    @JiraEpic("PO-3685")
    @JiraTestKey("PO-7689")
    void shouldReturnFeatureDisabledProblemWhenLaunchDarklyFlagIsFalse(String endpointName, RequestBuilder request)
        throws Exception {
        when(ldClient.boolVariation(eq(RELEASE_1B), any(LDContext.class), anyBoolean())).thenReturn(false);

        mockMvc.perform(request)
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.retriable").value(false));

        verify(ldClient, times(1)).boolVariation(eq(RELEASE_1B), any(LDContext.class), anyBoolean());
    }
}
