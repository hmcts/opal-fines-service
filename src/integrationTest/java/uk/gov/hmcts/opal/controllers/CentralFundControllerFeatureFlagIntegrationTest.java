package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.AbstractIntegrationTest;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=false"
})
@Slf4j(topic = "opal.CentralFundControllerFeatureFlagIntegrationTest")
@DisplayName("Central Fund Controller Feature Flag Integration Tests")
class CentralFundControllerFeatureFlagIntegrationTest extends AbstractIntegrationTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    @Test
    @DisplayName("PO-2320: GET central fund returns 405 when release-1b is disabled")
    void getCentralFund_whenFeatureDisabled_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(get("/central-funds/73").header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"));
    }
}
