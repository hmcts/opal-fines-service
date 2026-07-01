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
    @JiraTestKey(value = "PO-8546", name = "\"Search Defendant Accounts\"")
    @JiraTestKey(value = "PO-8547", name = "\"Get Defendant Account Header Summary\"")
    @JiraTestKey(value = "PO-8548", name = "\"Get Defendant Account Party\"")
    @JiraTestKey(value = "PO-8549", name = "\"Get Defendant Account At A Glance\"")
    @JiraTestKey(value = "PO-8550", name = "\"Get Major Creditor Account At A Glance\"")
    @JiraTestKey(value = "PO-8551", name = "\"Update Defendant Account\"")
    @JiraTestKey(value = "PO-8552", name = "\"Add Note\"")
    @JiraTestKey(value = "PO-8553", name = "\"Replace Defendant Account Party\"")
    @JiraTestKey(value = "PO-8554", name = "\"Add Defendant Account Party\"")
    @JiraTestKey(value = "PO-8555", name = "\"Remove Defendant Account Party\"")
    @JiraTestKey(value = "PO-8556", name = "\"Get Defendant Account Enforcement Status\"")
    @JiraTestKey(value = "PO-8557", name = "\"Add Defendant Account Enforcement\"")
    @JiraTestKey(value = "PO-8558", name = "\"Remove Defendant Account Enforcement Hold\"")
    @JiraTestKey(value = "PO-8559", name = "\"Get Defendant Account Payment Terms\"")
    @JiraTestKey(value = "PO-8560", name = "\"Add Defendant Account Payment Terms\"")
    @JiraTestKey(value = "PO-8561", name = "\"Add Defendant Account Payment Card Request\"")
    @JiraTestKey(value = "PO-8562", name = "\"Get Defendant Account Fixed Penalty\"")
    @JiraTestKey(value = "PO-8563", name = "\"Get Central Fund\"")
    @JiraTestKey(value = "PO-8564", name = "\"Get Major Creditor Account Header Summary\"")
    @JiraTestKey(value = "PO-8565", name = "\"Get Major Creditor Account At A Glance\"")
    @JiraTestKey(value = "PO-8566", name = "\"Search Minor Creditor Accounts\"")
    @JiraTestKey(value = "PO-8567", name = "\"Get Minor Creditor Account Header Summary\"")
    @JiraTestKey(value = "PO-8568", name = "\"Get Minor Creditor Account At A Glance\"")
    @JiraTestKey(value = "PO-8569", name = "\"Get Minor Creditor Account\"")
    @JiraTestKey(value = "PO-8570", name = "\"Get Minor Creditor History\"")
    @JiraTestKey(value = "PO-8571", name = "\"Patch Minor Creditor Account\"")
    @JiraTestKey(value = "PO-8572", name = "\"Get Result By Id\"")
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
