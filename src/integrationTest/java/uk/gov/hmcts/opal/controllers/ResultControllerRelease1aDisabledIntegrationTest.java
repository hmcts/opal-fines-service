package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.AbstractIntegrationTest;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1a=false",
    "launchdarkly.default-flag-values.release-1b=true",
    "launchdarkly.enabled=false"
})
@DisplayName("ResultController release-1a disabled Integration Test")
class ResultControllerRelease1aDisabledIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /results is unavailable when release-1a is disabled")
    void getResults_returnsFeatureDisabledWhenRelease1aDisabled() throws Exception {
        mockMvc.perform(get("/results"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"));
    }
}
