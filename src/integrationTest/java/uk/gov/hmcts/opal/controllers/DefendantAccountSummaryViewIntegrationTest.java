package uk.gov.hmcts.opal.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@DisplayName("Defendant Account Summary View Integration Tests")
class DefendantAccountSummaryViewIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    @Test
    @JiraEpic("PO-2332")
    @JiraStory("PO-2334")
    @DisplayName("PO-2334 INT.01 - Get header summary returns has consolidated accounts true")
    void int01_getHeaderSummary_returnsHasConsolidatedAccountsTrue() throws Exception {

        ResultActions resultActions = mockMvc.perform(
            get("/defendant-accounts/990001/header-summary")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
        );

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value("990001"))
            .andExpect(jsonPath("$.account_number").value("990001A"))
            .andExpect(jsonPath("$.account_status_reference").exists())
            .andExpect(jsonPath("$.account_type").value("Fine"))
            .andExpect(jsonPath("$.business_unit_summary").exists())
            .andExpect(jsonPath("$.defendant_party_id").value("990001"))
            .andExpect(jsonPath("$.is_youth").value(false))
            .andExpect(jsonPath("$.has_consolidated_accounts").value(true));
    }

    @Test
    @JiraEpic("PO-2332")
    @JiraStory("PO-2334")
    @DisplayName("PO-2334 INT.02 - Get header summary returns has consolidated accounts false")
    void int02_getHeaderSummary_returnsHasConsolidatedAccountsFalse() throws Exception {

        ResultActions resultActions = mockMvc.perform(
            get("/defendant-accounts/77/header-summary")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
        );

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value("77"))
            .andExpect(jsonPath("$.account_number").value("177A"))
            .andExpect(jsonPath("$.account_status_reference").exists())
            .andExpect(jsonPath("$.account_type").value("Fine"))
            .andExpect(jsonPath("$.payment_state_summary").exists())
            .andExpect(jsonPath("$.party_details").exists())
            .andExpect(jsonPath("$.business_unit_summary").exists())
            .andExpect(jsonPath("$.defendant_party_id").value("77"))
            .andExpect(jsonPath("$.is_youth").value(false))
            .andExpect(jsonPath("$.has_consolidated_accounts").value(false));
    }

    @Test
    @JiraEpic("PO-2332")
    @JiraStory("PO-2334")
    @DisplayName("PO-2629 INT.08 - Get defendant account header summary returns forbidden response")
    void int08_getDefendantAccountHeaderSummary_returnsForbiddenResponse() throws Exception {

        ResultActions resultActions = mockMvc.perform(
            get("/defendant-accounts/77/header-summary")
                .with(userStateStub.getInvalidAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
        );

        resultActions.andExpect(status().isForbidden())
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource"));
    }
}
