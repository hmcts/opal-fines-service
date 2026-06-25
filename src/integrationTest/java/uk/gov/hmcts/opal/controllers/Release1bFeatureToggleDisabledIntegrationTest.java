package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
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

    @Test
    @JiraStory("PO-3762")
    @JiraEpic("PO-3685")
    @JiraTestKey("PO-7663")
    void shouldReturnFeatureDisabledProblemWhenRelease1bIsDisabled_firstEndpoint() throws Exception {
        Arguments firstRequest = uk.gov.hmcts.opal.controllers.util.Release1bFeatureToggleRequestUtil.firstGatedRequest();
        Object[] values = firstRequest.get();
        assertFeatureDisabled((RequestBuilder) values[1]);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("uk.gov.hmcts.opal.controllers.util.Release1bFeatureToggleRequestUtil#remainingGatedRequests")
    @JiraStory("PO-3762")
    @JiraEpic("PO-3685")
    @JiraTestKey("PO-8141")
    void shouldReturnFeatureDisabledProblemWhenRelease1bIsDisabled(String endpointName, RequestBuilder request)
        throws Exception {
        assertFeatureDisabled(request);
    }

    private void assertFeatureDisabled(RequestBuilder request) throws Exception {
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
