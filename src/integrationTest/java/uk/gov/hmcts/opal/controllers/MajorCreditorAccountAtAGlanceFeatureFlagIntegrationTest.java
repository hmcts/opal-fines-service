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
import uk.gov.hmcts.opal.service.MajorCreditorAccountService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=false"
})
@DisplayName("Major Creditor Account At A Glance Feature Flag Integration Tests")
class MajorCreditorAccountAtAGlanceFeatureFlagIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private MajorCreditorAccountService majorCreditorAccountService;

    @Test
    @DisplayName("PO-2132 feature flag disabled returns 405")
    @JiraStory("PO-2132")
    @JiraEpic("FAE: View Major Creditor Account Summary")
    void getAtAGlance_whenRelease1bDisabledReturns405() throws Exception {
        mockMvc.perform(get("/major-creditor-accounts/{id}/at-a-glance", 10770000000041L)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer some_value"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"));

        verifyNoInteractions(majorCreditorAccountService);
    }
}
