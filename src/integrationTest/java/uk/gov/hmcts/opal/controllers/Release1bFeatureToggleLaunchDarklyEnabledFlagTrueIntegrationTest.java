package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.RequestBuilder;
import uk.gov.hmcts.opal.AbstractIntegrationTest;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=true",
    "launchdarkly.sdk-key=test-sdk-key",
    "launchdarkly.default-flag-values.release-1b=false"
})
class Release1bFeatureToggleLaunchDarklyEnabledFlagTrueIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private LDClientInterface ldClient;

    @ParameterizedTest(name = "{0}")
    @MethodSource("uk.gov.hmcts.opal.controllers.util.Release1bFeatureToggleRequestUtil#gatedRequests")
    void shouldNotReturnFeatureDisabledProblemWhenLaunchDarklyFlagIsTrue(String endpointName, RequestBuilder request)
        throws Exception {
        when(ldClient.boolVariation(eq(RELEASE_1B), any(LDContext.class), anyBoolean())).thenReturn(true);

        mockMvc.perform(request)
            .andExpect(result -> {
                assertThat(result.getResponse().getStatus()).isNotEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
                assertThat(result.getResponse().getContentAsString())
                    .doesNotContain("https://hmcts.gov.uk/problems/feature-disabled");
            });
    }
}
