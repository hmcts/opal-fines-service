package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
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
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1b=true",
    "launchdarkly.enabled=false"
})
@DisplayName("MappingsController Integration Test")
class MappingsControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /mappings/{type} returns defendant account status mappings")
    @JiraStory("PO-3871")
    @JiraEpic("PO-3372")
    void getMappings_returnsSupportedDefendantAccountStatusMappings() throws Exception {
        mockMvc.perform(get("/mappings/defendant-account-status"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(6)))
            .andExpect(jsonPath("$[*].code", contains("CS", "L", "TA", "TO", "TS", "WO")))
            .andExpect(jsonPath("$[*].display_name", contains(
                "Account consolidated",
                "Live",
                "TFO acknowledged",
                "TFO to be acknowledged",
                "TFO to NI/Scotland to be acknowledged",
                "Account written off"
            )))
            .andExpect(jsonPath("$[0].label").doesNotExist());
    }

    @Test
    @DisplayName("GET /mappings/{type} returns 400 with supported types for unsupported mapping type")
    @JiraStory("PO-3871")
    @JiraEpic("PO-3372")
    void getMappings_whenTypeIsUnsupported_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/mappings/unsupported-type"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Unsupported Mapping Type"))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/unsupported-mapping-type"))
            .andExpect(jsonPath("$.detail").value(
                "Unsupported mapping type: unsupported-type. Supported types: defendant-account-status"
            ))
            .andExpect(jsonPath("$.mapping_type").value("unsupported-type"))
            .andExpect(jsonPath("$.supported_types", hasSize(1)))
            .andExpect(jsonPath("$.supported_types[0]").value("defendant-account-status"));
    }

    @Test
    @DisplayName("GET /mappings returns 400 when mapping type is missing")
    @JiraStory("PO-3871")
    @JiraEpic("PO-3372")
    void getMappings_whenTypeIsMissing_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/mappings"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Missing Mapping Type"))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/missing-mapping-type"))
            .andExpect(jsonPath("$.detail").value(
                "Required mapping type is missing. Supported types: defendant-account-status"
            ))
            .andExpect(jsonPath("$.supported_types", hasSize(1)))
            .andExpect(jsonPath("$.supported_types[0]").value("defendant-account-status"));
    }
}
