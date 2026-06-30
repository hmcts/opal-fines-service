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

    @ParameterizedTest(name = "{0}")
    @MethodSource("uk.gov.hmcts.opal.controllers.util.Release1bFeatureToggleRequestUtil#gatedRequests")
    @JiraStory("PO-3762")
    @JiraEpic("PO-3685")
    @JiraTestKey(value = "PO-8573", name = "\"Search Defendant Accounts\"")
    @JiraTestKey(value = "PO-8574", name = "\"Get Defendant Account Header Summary\"")
    @JiraTestKey(value = "PO-8575", name = "\"Get Defendant Account Party\"")
    @JiraTestKey(value = "PO-8576", name = "\"Get Defendant Account At A Glance\"")
    @JiraTestKey(value = "PO-8577", name = "\"Get Major Creditor Account At A Glance\"")
    @JiraTestKey(value = "PO-8578", name = "\"Update Defendant Account\"")
    @JiraTestKey(value = "PO-8579", name = "\"Add Note\"")
    @JiraTestKey(value = "PO-8580", name = "\"Replace Defendant Account Party\"")
    @JiraTestKey(value = "PO-8581", name = "\"Add Defendant Account Party\"")
    @JiraTestKey(value = "PO-8582", name = "\"Remove Defendant Account Party\"")
    @JiraTestKey(value = "PO-8583", name = "\"Get Defendant Account Enforcement Status\"")
    @JiraTestKey(value = "PO-8584", name = "\"Add Defendant Account Enforcement\"")
    @JiraTestKey(value = "PO-8585", name = "\"Remove Defendant Account Enforcement Hold\"")
    @JiraTestKey(value = "PO-8586", name = "\"Get Defendant Account Payment Terms\"")
    @JiraTestKey(value = "PO-8587", name = "\"Add Defendant Account Payment Terms\"")
    @JiraTestKey(value = "PO-8588", name = "\"Add Defendant Account Payment Card Request\"")
    void shouldNotReturnFeatureDisabledProblemWhenLaunchDarklyFlagIsTrue(String endpointName, RequestBuilder request)
        throws Exception {
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
