package uk.gov.hmcts.opal.controllers.r1c;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles("integration")
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1b=false",
    "launchdarkly.default-flag-values.release-1c-payment=false"
})
@DisplayName("Mappings with both feature flags disabled")
class MappingsBothFlagsDisabledIT extends AbstractIntegrationTest {

    @ParameterizedTest
    @ValueSource(strings = {"/mappings", "/mappings/unsupported-type"})
    @DisplayName("Missing and unsupported mapping types return feature disabled")
    @JiraStory("PO-10964")
    @JiraEpic("PO-2439")
    void missingOrUnsupportedType_returnsFeatureDisabled(String path) throws Exception {
        mockMvc.perform(get(path))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"))
            .andExpect(jsonPath("$.retriable").value(false));
    }
}
