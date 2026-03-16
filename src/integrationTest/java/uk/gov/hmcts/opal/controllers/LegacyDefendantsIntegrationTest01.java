package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.LegacyDefendantsIntegrationTest01")
class LegacyDefendantsIntegrationTest01 extends CommonDefendantsIntegrationTest01 {

    @Test
    void testGetHeaderSummaryInd() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/77/header-summary").header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary_Individual: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value("77"))
            .andExpect(jsonPath("$.account_number").value("177A"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant"))
            .andExpect(jsonPath("$.is_youth").value(false))
            .andExpect(jsonPath("$.fixed_penalty_ticket_number").value("888"))
            .andExpect(jsonPath("$.business_unit_summary.business_unit_id").value("78"))
            .andExpect(jsonPath("$.payment_state_summary.imposed_amount").value(700.58))
            .andExpect(jsonPath("$.payment_state_summary.paid_amount").value(200.00))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
            .andExpect(jsonPath("$.party_details.individual_details.surname").value("Graham"))
            .andExpect(jsonPath("$.party_details.organisation_details").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_HEADER_SUMMARY_RESPONSE_SCHEMA);
    }

    @Test
    void testGetHeaderSummaryOrg() throws Exception {
        super.getHeaderSummary_Organisation(log);
    }

    @Test
    void testGetDefendantAccountsPaymentTerms_500Error() throws Exception {
        super.getDefendantAccountPaymentTerms_500Error(log);
    }

    @Test
    void testGetDefendantAccountsAtAGlance_500Error() throws Exception {
        super.getDefendantAccountAtAGlance_500Error(log);
    }

    @Test
    void testGetEnforcementStatus() throws Exception {
        super.testGetEnforcementStatus(log, true);
    }

    @Test
    void testGetEnforcementStatus_missingAuth_returns401() throws Exception {
        super.testGetEnforcementStatus_missingAuthHeader_returns401(log, true);
    }

    @Test
    void testGetEnforcementStatus_forbidden_returns403() throws Exception {
        super.testGetEnforcementStatus_forbidden(log, true);
    }

    @Test
    void testGetEnforcementStatus_timeout_returns408() throws Exception {
        super.testGetEnforcementStatus_timeout(log, true);
    }

    @Test
    void testGetEnforcementStatus_serviceUnavailable_returns503() throws Exception {
        super.testGetEnforcementStatus_serviceUnavailable(log, true);
    }

    @Test
    void testGetEnforcementStatus_serverError_returns500() throws Exception {
        super.testGetEnforcementStatus_serverError(log, true);
    }

}
