package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Component
@DisplayName("Integration tests for /defendant-accounts/{id}/payment-terms/latest")
public class DefendantAccountPaymentTermsIntegrationTest extends BaseDefendantAccountsIntegrationTest {

    protected final String getPaymentTermsResponseSchemaLocation() {
        return SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountPaymentTermsResponse.json";
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    @DisplayName("OPAL: Get Defendant Account Payment Terms [@PO-1565] - Happy Path")
    public void testGetPaymentTerms(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Make the 'date_last_amended' deterministic for acct 77
        jdbcTemplate.update(
            "UPDATE defendant_accounts SET last_changed_date = '2024-01-03 00:00:00' WHERE defendant_account_id = 77"
        );

        ResultActions resultActions = mockMvc.perform(
            get(URL_BASE + "/77/payment-terms/latest")
                .header("authorization", "Bearer some_value")
        );

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetPaymentTerms: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.payment_terms.days_in_default").value(120))
            .andExpect(jsonPath("$.payment_terms.date_days_in_default_imposed").isEmpty())
            .andExpect(jsonPath("$.payment_terms.reason_for_extension").isEmpty())
            .andExpect(jsonPath("$.payment_terms.payment_terms_type.payment_terms_type_code").value("B"))
            .andExpect(jsonPath("$.payment_terms.effective_date").value("2025-10-12"))
            .andExpect(jsonPath("$.payment_terms.instalment_period.instalment_period_code").value("W"))
            .andExpect(jsonPath("$.payment_terms.lump_sum_amount").isEmpty())
            .andExpect(jsonPath("$.payment_terms.instalment_amount").isEmpty())
            .andExpect(jsonPath("$.payment_terms.posted_details.posted_date").value("2023-11-03"))
            .andExpect(jsonPath("$.payment_terms.posted_details.posted_by").value("01000000A"))
            .andExpect(jsonPath("$.payment_terms.posted_details.posted_by_name").isEmpty())
            .andExpect(jsonPath("$.payment_card_last_requested").value("2024-01-01"))
            .andExpect(jsonPath("$.payment_terms.extension").value(false))
            .andExpect(jsonPath("$.last_enforcement").value("10"));

        jsonSchemaValidationService.validateOrError(body, getPaymentTermsResponseSchemaLocation());
    }

    @DisplayName("OPAL: Get Defendant Account Payment Terms [@PO-1565] - Not Found (404)")
    public void testGetPaymentTermsLatest_NoPaymentTermFoundForId(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/79/payment-terms/latest")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetPaymentTerms: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.type")
                .value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"));
    }

    @DisplayName("OPAL: Get Defendant Account Payment Terms [@PO-1565] - Internal Server Error (500)")
    public void getDefendantAccountPaymentTerms_500Error(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/500/payment-terms/latest")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @DisplayName("LEGACY: Get Defendant Account Payment Terms [@PO-1565] - Happy Path (legacy schema)")
    public void testLegacyGetPaymentTerms(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/77/payment-terms/latest")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetPaymentTerms: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.payment_terms.days_in_default").value(120))
            .andExpect(jsonPath("$.payment_terms.date_days_in_default_imposed").value("2025-10-12"))
            .andExpect(jsonPath("$.payment_terms.reason_for_extension").value(""))
            .andExpect(jsonPath("$.payment_terms.payment_terms_type.payment_terms_type_code").value("B"))
            .andExpect(jsonPath("$.payment_terms.effective_date").value("2025-10-12"))
            .andExpect(jsonPath("$.payment_terms.instalment_period.instalment_period_code").value("W"))
            .andExpect(jsonPath("$.payment_terms.lump_sum_amount").value(0.00))
            .andExpect(jsonPath("$.payment_terms.instalment_amount").value(0.00))
            .andExpect(jsonPath("$.posted_details.posted_date").value("2023-11-03"))
            .andExpect(jsonPath("$.posted_details.posted_by").value("01000000A"))
            .andExpect(jsonPath("$.posted_details.posted_by_name").value(""))
            .andExpect(jsonPath("$.payment_card_last_requested").value("2024-01-01"))
            .andExpect(jsonPath("$.date_last_amended").value("2024-01-03"))
            .andExpect(jsonPath("$.extension").value(false))
            .andExpect(jsonPath("$.last_enforcement").value("REM"));

        jsonSchemaValidationService.validateOrError(body, getPaymentTermsResponseSchemaLocation());
    }
}
