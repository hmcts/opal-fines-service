package uk.gov.hmcts.opal.controllers;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=false"
})
@DisplayName("Major Creditor Account Header Summary Feature Flag Integration Tests")
class MajorCreditorAccountHeaderSummaryFeatureFlagIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private GatewayService gatewayService;

    @Test
    @DisplayName("PO-2136 feature flag disabled returns 404")
    @JiraStory("PO-2136")
    @JiraEpic("PO-1286")
    @JiraTestKey("PO-7614")
    void getHeaderSummary_whenRelease1bDisabledReturns404() throws Exception {
        mockMvc.perform(get("/major-creditor-accounts/{id}/header-summary", 99000000000800L)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer some_value"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"));

        verifyNoInteractions(gatewayService);
    }
}
