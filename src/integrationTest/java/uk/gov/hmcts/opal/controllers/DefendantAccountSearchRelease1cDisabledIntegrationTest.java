package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PostDefendantAccountSearchResponseDefendantAccount;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles("integration")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true",
    "launchdarkly.default-flag-values.release-1c=false"
})
@DisplayName("Defendant account search release-1c disabled Integration Test")
class DefendantAccountSearchRelease1cDisabledIntegrationTest extends AbstractIntegrationTest {

    private static final String DEFENDANTS_SEARCH_URL = "/defendant-accounts/search";

    @MockitoBean
    private DefendantAccountService defendantAccountService;

    @Test
    @DisplayName("POST /defendant-accounts/search remains available without consolidated search")
    @JiraStory("PO-3768")
    @JiraEpic("PO-3685")
    @JiraTestKey("PO-7573")
    void postDefendantAccountsSearch_returnsOkWithoutConsolidatedSearchWhenRelease1cDisabled() throws Exception {
        PostDefendantAccountSearchResponseDefendantAccount response =
            PostDefendantAccountSearchResponseDefendantAccount.builder()
            .count(0)
            .defendantAccounts(List.of())
            .build();

        when(defendantAccountService.searchDefendantAccounts(
            any(PostDefendantAccountSearchRequestDefendantAccount.class)))
            .thenReturn(response);

        mockMvc.perform(post(DEFENDANTS_SEARCH_URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(searchCriteria(false)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0));

        verify(defendantAccountService).searchDefendantAccounts(
            any(PostDefendantAccountSearchRequestDefendantAccount.class));
        verifyNoMoreInteractions(defendantAccountService);
    }

    @Test
    @DisplayName("POST /defendant-accounts/search rejects consolidated search when release-1c is disabled")
    @JiraStory("PO-3768")
    @JiraEpic("PO-3685")
    @JiraTestKey("PO-7574")
    void postDefendantAccountsSearch_returnsFeatureDisabledForConsolidatedSearchWhenRelease1cDisabled()
        throws Exception {
        when(defendantAccountService.searchDefendantAccounts(
            any(PostDefendantAccountSearchRequestDefendantAccount.class)))
            .thenThrow(new FeatureDisabledException(
                "Feature release-1c is not enabled for defendant account consolidated search"));

        mockMvc.perform(post(DEFENDANTS_SEARCH_URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(searchCriteria(true)))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"));

        verify(defendantAccountService).searchDefendantAccounts(
            any(PostDefendantAccountSearchRequestDefendantAccount.class));
        verifyNoMoreInteractions(defendantAccountService);
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
