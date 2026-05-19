package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1b=false")
class OpalDefendantAccountImpositionsFeatureToggleIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    @Test
    @DisplayName("OPAL: Get Defendant Account Impositions returns feature-disabled problem when release-1b is off")
    void getDefendantAccountImpositions_whenRelease1bDisabled_returnsFeatureDisabledProblem() throws Exception {
        mockMvc.perform(get(URL_BASE + "/77/impositions")
                            .header("Authorization", "Bearer test-token")
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.status").value(405))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"))
            .andExpect(jsonPath("$.retriable").value(false));
    }
}
