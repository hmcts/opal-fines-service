package uk.gov.hmcts.opal.controllers;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles("integration")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=false",
    "launchdarkly.default-flag-values.release-1c-write-off=true"
})
@DisplayName("Defendant account search release-1b disabled Integration Test")
class DefendantAccountSearchRelease1bDisabledIntegrationTest extends AbstractIntegrationTest {

    private static final String DEFENDANTS_SEARCH_URL = "/defendant-accounts/search";

    @MockitoBean
    private DefendantAccountService defendantAccountService;

    @Test
    @DisplayName("POST /defendant-accounts/search is unavailable when release-1b is disabled")
    @JiraStory("PO-3768")
    @JiraEpic("PO-3685")
    @JiraTestKey("PO-7572")
    void postDefendantAccountsSearch_returnsFeatureDisabledWhenRelease1bDisabled() throws Exception {
        mockMvc.perform(post(DEFENDANTS_SEARCH_URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(searchCriteria(true)))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"));

        verifyNoInteractions(defendantAccountService);
    }

    private String searchCriteria(boolean consolidationSearch) {
        return """
            {
              "active_accounts_only": true,
              "business_unit_ids": [78],
              "reference_number": null,
              "defendant": {
                "include_aliases": true,
                "organisation": false,
                "address_line_1": "Lumber",
                "postcode": "MA4 1AL",
                "organisation_name": null,
                "exact_match_organisation_name": null,
                "surname": "Graham",
                "exact_match_surname": true,
                "forenames": "Anna",
                "exact_match_forenames": true,
                "birth_date": "1980-02-03",
                "national_insurance_number": "A11111A"
              },
              "consolidation_search": %s
            }
            """.formatted(consolidationSearch);
    }
}
