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
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=false"
})
@DisplayName("ResultController release-1b disabled Integration Test")
class ResultControllerRelease1bDisabledIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /results remains available without filtering when release-1b is disabled")
    @JiraStory("PO-3765")
    @JiraEpic("PO-3685")
    @JiraTestKey("PO-7771")
    void getResultsWithoutFilters_returnsOkWhenRelease1bDisabled() throws Exception {
        mockMvc.perform(get("/results"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /results supports result_ids when release-1b is disabled")
    @JiraStory("PO-3765")
    @JiraEpic("PO-3685")
    @JiraTestKey("PO-7770")
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
    @JiraStory("PO-3765")
    @JiraEpic("PO-3685")
    @JiraTestKey("PO-7772")
    void getResultsWithFilteringParameter_returnsFeatureDisabledWhenRelease1bDisabled(String parameter)
        throws Exception {
        mockMvc.perform(get("/results?" + parameter + "=true"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"));
    }
}
