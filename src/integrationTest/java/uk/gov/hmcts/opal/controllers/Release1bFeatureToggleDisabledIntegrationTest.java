package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.RequestBuilder;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=false"
})
class Release1bFeatureToggleDisabledIntegrationTest extends AbstractIntegrationTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("uk.gov.hmcts.opal.controllers.util.Release1bFeatureToggleRequestUtil#gatedRequests")
    @JiraStory("PO-3762")
    @JiraEpic("PO-3685")
    @JiraTestKey("PO-7663")
    @JiraTestKey(value = "PO-8518", name = "\"Search Defendant Accounts\"")
    @JiraTestKey(value = "PO-8519", name = "\"Get Defendant Account Header Summary\"")
    @JiraTestKey(value = "PO-8520", name = "\"Get Defendant Account Party\"")
    @JiraTestKey(value = "PO-8521", name = "\"Get Defendant Account At A Glance\"")
    @JiraTestKey(value = "PO-8522", name = "\"Get Major Creditor Account At A Glance\"")
    @JiraTestKey(value = "PO-8523", name = "\"Update Defendant Account\"")
    @JiraTestKey(value = "PO-8524", name = "\"Add Note\"")
    @JiraTestKey(value = "PO-8525", name = "\"Replace Defendant Account Party\"")
    @JiraTestKey(value = "PO-8526", name = "\"Add Defendant Account Party\"")
    @JiraTestKey(value = "PO-8527", name = "\"Remove Defendant Account Party\"")
    @JiraTestKey(value = "PO-8528", name = "\"Get Defendant Account Enforcement Status\"")
    @JiraTestKey(value = "PO-8529", name = "\"Add Defendant Account Enforcement\"")
    @JiraTestKey(value = "PO-8530", name = "\"Remove Defendant Account Enforcement Hold\"")
    @JiraTestKey(value = "PO-8531", name = "\"Get Defendant Account Payment Terms\"")
    @JiraTestKey(value = "PO-8532", name = "\"Add Defendant Account Payment Terms\"")
    @JiraTestKey(value = "PO-8533", name = "\"Add Defendant Account Payment Card Request\"")
    @JiraTestKey(value = "PO-8534", name = "\"Get Defendant Account Fixed Penalty\"")
    @JiraTestKey(value = "PO-8535", name = "\"Get Central Fund\"")
    @JiraTestKey(value = "PO-8536", name = "\"Get Major Creditor Account Header Summary\"")
    @JiraTestKey(value = "PO-8538", name = "\"Search Minor Creditor Accounts\"")
    @JiraTestKey(value = "PO-8539", name = "\"Get Minor Creditor Account Header Summary\"")
    @JiraTestKey(value = "PO-8540", name = "\"Get Minor Creditor Account At A Glance\"")
    @JiraTestKey(value = "PO-8541", name = "\"Get Minor Creditor Account\"")
    @JiraTestKey(value = "PO-8542", name = "\"Get Minor Creditor History\"")
    @JiraTestKey(value = "PO-8543", name = "\"Patch Minor Creditor Account\"")
    @JiraTestKey(value = "PO-8544", name = "\"Get Result By Id\"")
    void shouldReturnFeatureDisabledProblemWhenRelease1bIsDisabled(String endpointName, RequestBuilder request)
        throws Exception {
        mockMvc.perform(request)
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.retriable").value(false));
    }
}
