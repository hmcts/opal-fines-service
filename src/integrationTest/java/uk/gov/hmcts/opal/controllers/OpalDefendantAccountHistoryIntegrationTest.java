package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@DisplayName("Defendant Account History Controller Integration Tests")
@Slf4j(topic = "opal.OpalDefendantAccountHistoryIntegrationTest")
class OpalDefendantAccountHistoryIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    private static final long DEFENDANT_ACCOUNT_ID = 262200L;

    @BeforeEach
    void insertHistoryData() {
        jdbcTemplate.update("""
            INSERT INTO results (
                result_id, result_title, result_title_cy, result_type, active, imposition, imposition_accruing,
                enforcement, enforcement_override, further_enforcement_warn, further_enforcement_disallow,
                enforcement_hold, requires_enforcer, generates_hearing, generates_warrant, collection_order,
                extend_ttp_disallow, extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies,
                manual_enforcement, allow_payment_terms, requires_employment_data, allow_additional_action,
                requires_lja
            ) VALUES (
                'HST01', 'History enforcement', 'History enforcement', 'Result', TRUE, FALSE, FALSE,
                TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
                FALSE, FALSE, FALSE, FALSE, FALSE
            ) ON CONFLICT (result_id) DO NOTHING
            """);

        jdbcTemplate.update("""
            INSERT INTO defendant_accounts (
                defendant_account_id, version_number, business_unit_id, account_number, amount_paid,
                account_balance, amount_imposed, account_status, allow_writeoffs, allow_cheques, account_type,
                collection_order, payment_card_requested
            ) VALUES (
                262200, 0, 78, '262200A', 0.00, 500.00, 500.00, 'L', 'N', 'N', 'Fine', 'N', 'N'
            ) ON CONFLICT (defendant_account_id) DO NOTHING
            """);

        jdbcTemplate.update("""
            INSERT INTO courts (court_id, business_unit_id, court_code, name)
            VALUES (262200, 78, 2200, 'History Magistrates Court')
            ON CONFLICT (court_id) DO NOTHING
            """);

        jdbcTemplate.update("""
            INSERT INTO amendments (
                amendment_id, business_unit_id, associated_record_type, associated_record_id, amended_date,
                amended_by, field_code, old_value, new_value, case_reference, function_code
            ) VALUES (
                26220001, 78, 'defendant_accounts', '262200', TIMESTAMP '2026-01-01 08:00:00',
                'hist-user-1', 1, 'Old value', 'New value', 'CASE-HIST', 'UPD'
            ) ON CONFLICT (amendment_id) DO NOTHING
            """);

        jdbcTemplate.update("""
            INSERT INTO enforcements (
                enforcement_id, defendant_account_id, posted_date, posted_by, result_id, reason, jail_days,
                warrant_reference, case_reference, hearing_date, hearing_court_id, posted_by_name,
                enforcement_account_type
            ) VALUES (
                26220002, 262200, TIMESTAMP '2026-01-02 09:00:00', 'hist-user-2', 'HST01',
                'History reason', 14, 'WR262200', 'CASE-HIST', TIMESTAMP '2026-02-02 10:00:00',
                262200, 'History User Two', 'COLL'
            ) ON CONFLICT (enforcement_id) DO NOTHING
            """);

        jdbcTemplate.update("""
            INSERT INTO payment_terms (
                payment_terms_id, defendant_account_id, posted_date, posted_by, terms_type_code, effective_date,
                instalment_period, instalment_amount, instalment_lump_sum, jail_days, extension, account_balance,
                posted_by_name, active
            ) VALUES (
                26220003, 262200, TIMESTAMP '2026-01-03 09:00:00', 'hist-user-3', 'I',
                TIMESTAMP '2026-02-03 00:00:00', 'W', 25.00, 100.00, 7, FALSE, 500.00,
                'History User Three', TRUE
            ) ON CONFLICT (payment_terms_id) DO NOTHING
            """);

        jdbcTemplate.update("""
            INSERT INTO defendant_transactions (
                defendant_transaction_id, defendant_account_id, posted_date, posted_by, transaction_type,
                transaction_amount, payment_method, payment_reference, text, status, status_date, status_amount,
                posted_by_name
            ) VALUES (
                26220004, 262200, TIMESTAMP '2026-01-04 09:00:00', 'hist-user-4', 'PAYMNT',
                -50.00, 'NC', 'PAY262200', 'History payment', 'C', TIMESTAMP '2026-01-04 10:00:00',
                -50.00, 'History User Four'
            ) ON CONFLICT (defendant_transaction_id) DO NOTHING
            """);

        jdbcTemplate.update("""
            INSERT INTO notes (
                note_id, note_type, associated_record_type, associated_record_id, note_text, posted_date,
                posted_by, posted_by_name
            ) VALUES (
                26220005, 'AA', 'defendant_accounts', '262200', 'History account note',
                TIMESTAMP '2026-01-05 09:00:00', 'hist-user-5', 'History User Five'
            ) ON CONFLICT (note_id) DO NOTHING
            """);
    }

    @AfterEach
    void deleteHistoryData() {
        jdbcTemplate.update("DELETE FROM notes WHERE note_id = 26220005 OR associated_record_id = '262200'");
        jdbcTemplate.update("DELETE FROM defendant_transactions WHERE defendant_account_id = 262200");
        jdbcTemplate.update("DELETE FROM payment_terms WHERE defendant_account_id = 262200");
        jdbcTemplate.update("DELETE FROM enforcements WHERE defendant_account_id = 262200");
        jdbcTemplate.update("DELETE FROM amendments WHERE amendment_id = 26220001 OR associated_record_id = '262200'");
        jdbcTemplate.update("DELETE FROM defendant_accounts WHERE defendant_account_id = 262200");
        jdbcTemplate.update("DELETE FROM courts WHERE court_id = 262200");
        jdbcTemplate.update("DELETE FROM results WHERE result_id = 'HST01'");
    }

    @Test
    @DisplayName("PO-2622: INT.01 mixed history items returned and ordered")
    void getDefendantAccountHistory_mixedItems_returnsAllItemsNewestFirst() throws Exception {
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .header("Authorization", "Bearer test-token")
        );

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":getDefendantAccountHistory_mixedItems_returnsAllItemsNewestFirst: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", matchesPattern("\"\\d+\"")))
            .andExpect(jsonPath("$.historyItems", hasSize(5)))
            .andExpect(jsonPath("$.historyItems[0].type").value("Note"))
            .andExpect(jsonPath("$.historyItems[0].details.noteText").value("History account note"))
            .andExpect(jsonPath("$.historyItems[1].type").value("Financial"))
            .andExpect(jsonPath("$.historyItems[1].amount").value(-50.00))
            .andExpect(jsonPath("$.historyItems[1].details.transactionType.transactionType").value("PAYMNT"))
            .andExpect(jsonPath("$.historyItems[2].type").value("Payment terms"))
            .andExpect(jsonPath("$.historyItems[2].details.payment_terms_type.payment_terms_type_code").value("I"))
            .andExpect(jsonPath("$.historyItems[3].type").value("Enforcement"))
            .andExpect(jsonPath("$.historyItems[3].details.enforcementAction").value("HST01"))
            .andExpect(jsonPath("$.historyItems[3].details.daysInDefault").value(14))
            .andExpect(jsonPath("$.historyItems[4].type").value("Amendment"))
            .andExpect(jsonPath("$.historyItems[4].details.oldValue").value("Old value"))
            .andExpect(jsonPath("$.historyItems[4].details.newValue").value("New value"));
    }
}
