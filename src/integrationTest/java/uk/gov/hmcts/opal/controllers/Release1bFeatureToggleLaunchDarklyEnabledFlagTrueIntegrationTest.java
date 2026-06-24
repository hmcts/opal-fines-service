package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
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
    "launchdarkly.default-flag-values.release-1b=false"
})
class Release1bFeatureToggleLaunchDarklyEnabledFlagTrueIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private LDClientInterface ldClient;

    @Test
    @JiraStory("PO-3762")
    @JiraEpic("PO-3685")
    @JiraTestKey("PO-7715")
    void shouldNotReturnFeatureDisabledProblemWhenLaunchDarklyFlagIsTrue_firstEndpoint() throws Exception {
        Arguments firstRequest = uk.gov.hmcts.opal.controllers.util.Release1bFeatureToggleRequestUtil.firstGatedRequest();
        Object[] values = firstRequest.get();
        assertFeatureEnabled((RequestBuilder) values[1]);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("uk.gov.hmcts.opal.controllers.util.Release1bFeatureToggleRequestUtil#remainingGatedRequests")
    @JiraTestKey("PO-8191")
    void shouldNotReturnFeatureDisabledProblemWhenLaunchDarklyFlagIsTrue(String endpointName, RequestBuilder request)
        throws Exception {
        assertFeatureEnabled(request);
    }

    private void assertFeatureEnabled(RequestBuilder request) throws Exception {
        when(ldClient.boolVariation(eq(RELEASE_1B), any(LDContext.class), anyBoolean())).thenReturn(true);

        mockMvc.perform(request)
            .andExpect(result -> {
                assertThat(result.getResponse().getStatus()).isNotEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
                assertThat(result.getResponse().getContentAsString())
                    .doesNotContain("https://hmcts.gov.uk/problems/feature-disabled");
            });

        verify(ldClient, times(1)).boolVariation(eq(RELEASE_1B), any(LDContext.class), anyBoolean());
    }
}
