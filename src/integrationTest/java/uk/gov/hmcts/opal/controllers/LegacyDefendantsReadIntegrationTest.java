package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.LegacyDefendantsReadIntegrationTest")
class LegacyDefendantsReadIntegrationTest extends AbstractLegacyDefendantsIntegrationTest {

    @Test
    @DisplayName("LEGACY: Get header summary for non-existent ID returns 500")
    @JiraStory("PO-1907")
    @JiraEpic("PO-812")
    void getHeaderSummary_Legacy_500() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/500/header-summary").header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":getHeaderSummary_Legacy_500: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    void testGetPaymentTerms() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/77/payment-terms/latest").header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetPaymentTerms: Response body:\n{}", ToJsonString.toPrettyJson(body));

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
    }

    void testGetDefendantAtAGlance() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/77/at-a-glance").header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetPaymentTerms: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value("DEF-ACC-00012345"))
            .andExpect(jsonPath("$.account_number").value("ACCT-9876543210"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant"))
            .andExpect(jsonPath("$.is_youth").value(false))
            .andExpect(jsonPath("$.party_details.party_id").value(nullValue()))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
            .andExpect(jsonPath("$.party_details.organisation_details").value(nullValue()))
            .andExpect(jsonPath("$.party_details.individual_details.title").value("Mr"))
            .andExpect(jsonPath("$.party_details.individual_details.surname").value("Rivers"))
            .andExpect(jsonPath("$.party_details.individual_details.date_of_birth").value("1989-05-23"))
            .andExpect(jsonPath("$.party_details.individual_details.age").value("36"))
            .andExpect(jsonPath("$.party_details.individual_details.national_insurance_number").value("QQ123456C"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases.length()").value(1))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[0]").isMap())
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[0].*").isEmpty())
            .andExpect(jsonPath("$.address.address_line_1").value("10 Example Street"))
            .andExpect(jsonPath("$.address.address_line_2").value("Flat 2B"))
            .andExpect(jsonPath("$.address.address_line_3").value("Sample District"))
            .andExpect(jsonPath("$.address.address_line_4").value("Sampletown"))
            .andExpect(jsonPath("$.address.address_line_5").value("Exampleshire"))
            .andExpect(jsonPath("$.address.postcode").value("AB1 2CD"))
            .andExpect(jsonPath("$.language_preferences.document_language_preference.language_code")
                .value(nullValue()))
            .andExpect(jsonPath("$.language_preferences.document_language_preference.language_display_name")
                .value(nullValue()))
            .andExpect(jsonPath("$.language_preferences.hearing_language_preference.language_code")
                .value(nullValue()))
            .andExpect(jsonPath("$.language_preferences.hearing_language_preference.language_display_name")
                .value(nullValue()))
            .andExpect(jsonPath("$.payment_terms.payment_terms_type.payment_terms_type_code").value("P"))
            .andExpect(jsonPath("$.payment_terms.payment_terms_type.payment_terms_type_display_name").value("Paid"))
            .andExpect(jsonPath("$.payment_terms.effective_date").value("2025-10-01"))
            .andExpect(jsonPath("$.payment_terms.instalment_period").value(nullValue()))
            .andExpect(jsonPath("$.payment_terms.lump_sum_amount").value(0.00))
            .andExpect(jsonPath("$.payment_terms.instalment_amount").value(50.00))
            .andExpect(jsonPath("$.enforcement_status.last_enforcement_action.last_enforcement_action_id")
                .value(nullValue()))
            .andExpect(jsonPath("$.enforcement_status.last_enforcement_action.last_enforcement_action_title")
                .value(nullValue()))
            .andExpect(jsonPath("$.enforcement_status.collection_order_made").value(false))
            .andExpect(jsonPath("$.enforcement_status.default_days_in_jail").value(0))
            .andExpect(jsonPath("$.enforcement_status.enforcement_override.enforcement_override_result")
                .value(nullValue()))
            .andExpect(jsonPath("$.enforcement_status.enforcement_override.enforcer.enforcer_id")
                .value(nullValue()))
            .andExpect(jsonPath("$.enforcement_status.enforcement_override.enforcer.enforcer_name")
                .value(nullValue()))
            .andExpect(jsonPath("$.enforcement_status.enforcement_override.lja.lja_id").value(nullValue()))
            .andExpect(jsonPath("$.enforcement_status.enforcement_override.lja.lja_name").value(nullValue()))
            .andExpect(jsonPath("$.enforcement_status.last_movement_date").value("2025-09-30"))
            .andExpect(jsonPath("$.comments_and_notes.account_comment")
                .value("Account imported from legacy system on 2025-09-01."))
            .andExpect(jsonPath("$.comments_and_notes.free_text_note_1")
                .value("Customer agreed to monthly instalments."))
            .andExpect(jsonPath("$.comments_and_notes.free_text_note_2").value("Preferred contact: letter."))
            .andExpect(jsonPath("$.comments_and_notes.free_text_note_3")
                .value("Next review due after three payments."));
    }
}
