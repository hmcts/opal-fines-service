package uk.gov.hmcts.opal.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@DisplayName("Defendant Account At A Glance Payment Terms Integration Tests")
class DefendantAccountAtAGlanceIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    private static final long ACCOUNT_MULTI_TERMS_ONE_ACTIVE = 262901L;
    private static final long ACCOUNT_NO_ACTIVE_TERMS = 262902L;

    @Test
    @DisplayName("PO-2629 INT.01 - At a glance returns the active payment terms summary")
    @JiraStory("PO-2629")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-5811")
    void int01_getAtAGlance_returnsActivePaymentTermsSummary() throws Exception {

        ResultActions resultActions = mockMvc.perform(
            get(URL_BASE + "/{defendantAccountId}/at-a-glance", ACCOUNT_MULTI_TERMS_ONE_ACTIVE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
        );

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value(String.valueOf(ACCOUNT_MULTI_TERMS_ONE_ACTIVE)))
            .andExpect(jsonPath("$.account_number").value("262901A"))
            .andExpect(jsonPath("$.payment_terms.payment_terms_type.payment_terms_type_code").value("B"))
            .andExpect(jsonPath("$.payment_terms.effective_date").value("2025-10-12"))
            .andExpect(jsonPath("$.payment_terms.instalment_period.instalment_period_code").value("W"))
            .andExpect(jsonPath("$.payment_terms.instalment_period.instalment_period_display_name").value("Weekly"))
            .andExpect(jsonPath("$.payment_terms.instalment_amount").doesNotExist())
            .andExpect(jsonPath("$.payment_terms.lump_sum_amount").doesNotExist());
    }

    @Test
    @DisplayName("PO-2629 INT.02 - At a glance returns not found when no active payment terms exist")
    @JiraStory("PO-2629")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-5810")
    void int02_getAtAGlance_returnsNotFound_whenNoActivePaymentTermsExist() throws Exception {

        ResultActions resultActions = mockMvc.perform(
            get(URL_BASE + "/{defendantAccountId}/at-a-glance", ACCOUNT_NO_ACTIVE_TERMS)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .accept(APPLICATION_PROBLEM_JSON)
        );

        resultActions.andExpect(status().isNotFound())
            .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"));
    }
}
