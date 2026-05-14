package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.AbstractIntegrationTest;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1a=true",
    "launchdarkly.default-flag-values.release-1b=false",
    "launchdarkly.enabled=false"
})
@DisplayName("ResultController release-1b disabled Integration Test")
class ResultControllerRelease1bDisabledIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /results remains available without filtering when release-1b is disabled")
    void getResultsWithoutFilters_returnsOkWhenRelease1bDisabled() throws Exception {
        mockMvc.perform(get("/results"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /results supports result_ids when release-1b is disabled")
    void getResultsWithResultIdsOnly_returnsOkWhenRelease1bDisabled() throws Exception {
        mockMvc.perform(get("/results?result_ids=UNKNOWN"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "active",
        "manual_enforcement_only",
        "generates_hearing",
        "enforcement",
        "enforcement_override"
    })
    @DisplayName("GET /results rejects filtering parameters when release-1b is disabled")
    void getResultsWithFilteringParameter_returnsFeatureDisabledWhenRelease1bDisabled(String parameter)
        throws Exception {
        mockMvc.perform(get("/results?" + parameter + "=true"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"));
    }
}
