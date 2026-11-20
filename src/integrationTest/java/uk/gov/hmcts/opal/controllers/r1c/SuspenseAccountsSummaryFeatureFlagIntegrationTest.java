package uk.gov.hmcts.opal.controllers.r1c;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.VIEW_SUSPENSE_ITEMS;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.service.SuspenseAccountsSummaryService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1c=true",
    "launchdarkly.default-flag-values.release-1c-suspense=false"
})
@DisplayName("Suspense item summary with the suspense feature disabled")
class SuspenseAccountsSummaryFeatureFlagIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private SuspenseAccountsSummaryService suspenseAccountsSummaryService;

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Blocks an authorised user without calling the summary service when release-1c-suspense is disabled")
    void getSummary_whenSuspenseDisabled_returnsFeatureDisabled() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 77, VIEW_SUSPENSE_ITEMS);

        mockMvc.perform(get("/suspense-accounts/{business_unit_id}/suspense-items/{suspense_item_number}/summary",
                77, 1L)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(header().doesNotExist(HttpHeaders.ETAG))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"))
            .andExpect(jsonPath("$.retriable").value(false));

        verifyNoInteractions(suspenseAccountsSummaryService);
    }
}
