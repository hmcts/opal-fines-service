package uk.gov.hmcts.opal.controllers.r1c;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.shared.util.DefendantSearchRequestUtil.searchRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true",
    "launchdarkly.default-flag-values.release-1c-write-off=false"
})
@DisplayName("Defendant account search release-1c-write-off disabled Integration Test")
@Tag("ExtendedTest")
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
class DefendantSearchR1cDisabledTest extends AbstractIntegrationTest {

    private static final String DEFENDANTS_SEARCH_URL = "/defendant-accounts/search";

    @MockitoBean
    private LoggingService loggingService;

    @MockitoSpyBean
    private DefendantAccountServiceProxy defendantAccountServiceProxy;

    @Test
    @DisplayName("POST /defendant-accounts/search remains available without consolidated search")
    @JiraStory("PO-3768")
    @JiraEpic("PO-3685")
    @JiraTestKey("PO-7573")
    void postDefendantAccountsSearch_returnsOkWithoutConsolidatedSearchWhenRelease1cDisabled() throws Exception {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        mockMvc.perform(post(DEFENDANTS_SEARCH_URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest("178A", false, false))))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1));

        verify(defendantAccountServiceProxy).searchDefendantAccounts(any());
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService).personalDataAccessLogAsync(captor.capture());
        assertThat(captor.getValue().getBusinessIdentifier())
            .isEqualTo("Search Account Results - Individual Defendants");
    }

    @Test
    @DisplayName("POST /defendant-accounts/search rejects consolidated search when release-1c-write-off is disabled")
    @JiraStory("PO-3768")
    @JiraEpic("PO-3685")
    @JiraTestKey("PO-7574")
    void postDefendantAccountsSearch_returnsFeatureDisabledForConsolidatedSearchWhenRelease1cDisabled()
        throws Exception {
        mockMvc.perform(post(DEFENDANTS_SEARCH_URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest("178A", false, true))))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"));

        verifyNoInteractions(defendantAccountServiceProxy, loggingService);
    }
}
