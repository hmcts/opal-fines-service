package uk.gov.hmcts.opal.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "legacy"})
@DisplayName("Defendant Account Summary View Legacy Integration Test")
@Slf4j(topic = "opal.LegacyDefendantAccountSummaryViewIntegrationTest")
public class LegacyDefendantAccountSummaryViewIntegrationTest extends AbstractIntegrationWithSecurityTest {

    private static final String DEFENDANT_ACCOUNTS_SUMMARY_URL = "/defendant-accounts/{id}/header-summary";

    @Test
    @JiraEpic("PO-2332")
    @JiraStory("PO-2336")
    @DisplayName("PO-2336 INT.01 - Get defendant account header summary maps has_consolidated_accounts to true")
    void int01_getDefendantAccountHeaderSummary_mapsHasConsolidatedAccountsTrue() throws Exception {
        ResultActions resultActions = mockMvc.perform(
            get(DEFENDANT_ACCOUNTS_SUMMARY_URL, 77L)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .accept(MediaType.APPLICATION_JSON)
        );

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
            .andExpect(jsonPath("$.defendant_account_id").value("77"))
            .andExpect(jsonPath("$.account_number").value("177A"))
            .andExpect(jsonPath("$.account_status_reference").exists())
            .andExpect(jsonPath("$.account_type").value("Fine"))
            .andExpect(jsonPath("$.payment_state_summary").exists())
            .andExpect(jsonPath("$.party_details").exists())
            .andExpect(jsonPath("$.business_unit_summary").exists())
            .andExpect(jsonPath("$.defendant_party_id").value("7"))
            .andExpect(jsonPath("$.is_youth").value(false))
            .andExpect(jsonPath("$.has_consolidated_accounts").value(true))
            .andExpect(jsonPath("$.debtor_type").value("Defendant"))
            .andExpect(jsonPath("$.payment_state_summary").exists())
            .andExpect(jsonPath("$.party_details").exists())
            .andExpect(jsonPath("$.parent_guardian_party_id").value("1"))
            .andExpect(jsonPath("$.fixed_penalty_ticket_number").value("888"))
            .andExpect(jsonPath("$.prosecutor_case_reference").value("5"));
    }

    @Test
    @JiraEpic("PO-2332")
    @JiraStory("PO-2336")
    @DisplayName("PO-2336 INT.02 - Get defendant account header summary maps has_consolidated_accounts to false")
    void int02_getDefendantAccountHeaderSummary_mapsHasConsolidatedAccountsFalse() throws Exception {
        ResultActions resultActions = mockMvc.perform(
            get(DEFENDANT_ACCOUNTS_SUMMARY_URL, 10001L)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .accept(MediaType.APPLICATION_JSON)
        );

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
            .andExpect(jsonPath("$.defendant_account_id").value("10001"))
            .andExpect(jsonPath("$.account_number").value("10001A"))
            .andExpect(jsonPath("$.account_status_reference").exists())
            .andExpect(jsonPath("$.account_type").value("Fine"))
            .andExpect(jsonPath("$.payment_state_summary").exists())
            .andExpect(jsonPath("$.party_details").exists())
            .andExpect(jsonPath("$.business_unit_summary").exists())
            .andExpect(jsonPath("$.defendant_party_id").value("9"))
            .andExpect(jsonPath("$.is_youth").value(false))
            .andExpect(jsonPath("$.has_consolidated_accounts").value(false))
            .andExpect(jsonPath("$.debtor_type").value("Defendant"))
            .andExpect(jsonPath("$.payment_state_summary").exists())
            .andExpect(jsonPath("$.party_details").exists())
            .andExpect(jsonPath("$.fixed_penalty_ticket_number").value("999"))
            .andExpect(jsonPath("$.prosecutor_case_reference").value("ORG-REF-10001"));
    }

    @Test
    @JiraEpic("PO-2332")
    @JiraStory("PO-2336")
    @DisplayName("PO-2336 INT.05 - Valid token without the correct permission returns 403 response")
    void int05_getDefendantAccountHeaderSummary_returns403Response() throws Exception {
        ResultActions resultActions = mockMvc.perform(
            get(DEFENDANT_ACCOUNTS_SUMMARY_URL, 77L)
                .with(userStateStub.getInvalidAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .accept(MediaType.APPLICATION_JSON)
        );

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @JiraEpic("PO-2332")
    @JiraStory("PO-2336")
    @DisplayName("PO-2336 INT.07 - not found defendant account header summary returns 404 response")
    void int07_getDefendantAccountHeaderSummary_returns404Response() throws Exception {
        ResultActions resultActions = mockMvc.perform(
            get(DEFENDANT_ACCOUNTS_SUMMARY_URL, 999999L)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .accept(MediaType.APPLICATION_JSON)
        );

        resultActions
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
