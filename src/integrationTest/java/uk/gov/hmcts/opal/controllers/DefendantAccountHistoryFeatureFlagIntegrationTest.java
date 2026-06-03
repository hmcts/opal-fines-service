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
import org.springframework.test.web.servlet.ResultActions;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=false"
})
@DisplayName("Defendant Account History Feature Flag Integration Test")
class DefendantAccountHistoryFeatureFlagIntegrationTest extends AbstractFeatureToggleIntegrationTest {

    @Test
    @DisplayName("PO-2622: GET defendant account history returns 405 when release-1b is disabled")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_whenRelease1bDisabled_returnsMethodNotAllowed() throws Exception {
        // Arrange
        // release-1b is disabled by local test properties.

        // Act
        ResultActions result = mockMvc.perform(
            get("/defendant-accounts/262200/history")
                .header("Authorization", "Bearer test-token")
        );

        // Assert
        result.andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"));
    }
}
