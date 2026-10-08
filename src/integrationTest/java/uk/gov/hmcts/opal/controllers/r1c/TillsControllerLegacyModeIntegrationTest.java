package uk.gov.hmcts.opal.controllers.r1c;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1c-payment=true",
    "launchdarkly.default-flag-values.is-legacy-mode=true",
    "launchdarkly.enabled=false"
})
@DisplayName("Tills Controller Legacy Mode Integration Test")
class TillsControllerLegacyModeIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("PO-2575 INT.08 - Rejects GET tills in legacy mode")
    @JiraStory("PO-2575")
    @JiraEpic("PO-2532")
    void getTills_rejectsLegacyMode() throws Exception {
        mockMvc.perform(get("/tills").with(userStateStub.getAuthenticaitonRequestPostProcessor()))
            .andExpect(status().isNotFound());
    }
}
