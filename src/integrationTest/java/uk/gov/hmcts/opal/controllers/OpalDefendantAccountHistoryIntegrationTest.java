package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.contains;
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
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

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
        authorise((short) 78, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);

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
                collection_order, payment_card_requested, originator_name
            ) VALUES (
                262200, 0, 78, '262200A', 0.00, 500.00, 500.00, 'L', 'N', 'N', 'Fine', 'N', 'N',
                'History Sending Court'
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
                posted_by_name, associated_record_type, associated_record_id
            ) VALUES (
                26220004, 262200, TIMESTAMP '2026-01-04 09:00:00', 'hist-user-4', 'PAYMNT',
                -50.00, 'NC', 'PAY262200', 'History payment', 'C', TIMESTAMP '2026-01-04 10:00:00',
                -50.00, 'History User Four', 'defendant_accounts', '262200'
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
        jdbcTemplate.update("DELETE FROM document_instances WHERE document_instance_id = 26220001");
        jdbcTemplate.update("DELETE FROM notes WHERE note_id = 26220005 OR associated_record_id = '262200'");
        jdbcTemplate.update("DELETE FROM defendant_transactions WHERE defendant_account_id = 262200");
        jdbcTemplate.update("DELETE FROM impositions WHERE defendant_account_id = 262200");
        jdbcTemplate.update("DELETE FROM creditor_accounts WHERE creditor_account_id = 262200");
        jdbcTemplate.update("DELETE FROM payment_terms WHERE defendant_account_id = 262200");
        jdbcTemplate.update("DELETE FROM enforcements WHERE defendant_account_id = 262210");
        jdbcTemplate.update("DELETE FROM defendant_accounts WHERE defendant_account_id = 262210");
        jdbcTemplate.update("DELETE FROM enforcements WHERE defendant_account_id = 262200");
        jdbcTemplate.update("DELETE FROM amendments WHERE amendment_id = 26220001 OR associated_record_id = '262200'");
        jdbcTemplate.update("DELETE FROM defendant_accounts WHERE defendant_account_id = 262200");
        jdbcTemplate.update("DELETE FROM courts WHERE court_id = 262200");
        jdbcTemplate.update("DELETE FROM results WHERE result_id IN ('HST01', 'HST02')");
    }

    @Test
    @DisplayName("PO-2622: INT.01 mixed history items returned and ordered")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_mixedItems_returnsAllItemsNewestFirst() throws Exception {
        // Arrange
        // Test data is inserted in BeforeEach.

        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .header("Authorization", userStateStub.getBearerToken())
        );

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":getDefendantAccountHistory_mixedItems_returnsAllItemsNewestFirst: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        // Assert
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

    @Test
    @DisplayName("PO-2622: INT.02 amendments mapping and multiplicity")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_amendments_returnsAllAmendmentRows() throws Exception {
        // Arrange
        jdbcTemplate.update("""
            INSERT INTO amendments (
                amendment_id, business_unit_id, associated_record_type, associated_record_id, amended_date,
                amended_by, field_code, old_value, new_value, case_reference, function_code
            ) VALUES
            (
                26220006, 78, 'defendant_accounts', '262200', TIMESTAMP '2026-01-06 08:00:00',
                'hist-user-6', 2, 'Old two', 'New two', 'CASE-HIST-2', 'UPD'
            ),
            (
                26220007, 78, 'defendant_accounts', '262200', TIMESTAMP '2026-01-07 08:00:00',
                'hist-user-7', 1, 'Old three', 'New three', 'CASE-HIST-3', 'UPD'
            )
            """);

        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("itemTypes", "amendment")
                .header("Authorization", userStateStub.getBearerToken())
        );

        // Assert
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems", hasSize(3)))
            .andExpect(jsonPath("$.historyItems[*].type",
                contains("Amendment", "Amendment", "Amendment")))
            .andExpect(jsonPath("$.historyItems[0].details.attributeName").value("Major Creditor Code"))
            .andExpect(jsonPath("$.historyItems[0].details.oldValue").value("Old three"))
            .andExpect(jsonPath("$.historyItems[0].details.newValue").value("New three"))
            .andExpect(jsonPath("$.historyItems[1].details.attributeName").value("Name"))
            .andExpect(jsonPath("$.historyItems[1].details.oldValue").value("Old two"))
            .andExpect(jsonPath("$.historyItems[2].details.attributeName").value("Major Creditor Code"))
            .andExpect(jsonPath("$.historyItems[2].details.oldValue").value("Old value"));
    }

    @Test
    @DisplayName("PO-2622: INT.03 enforcements mapping and multiplicity")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_enforcements_returnsAllEnforcementRows() throws Exception {
        // Arrange
        insertResult("HST02", "Second history enforcement");

        jdbcTemplate.update("""
            INSERT INTO enforcements (
                enforcement_id, defendant_account_id, posted_date, posted_by, result_id, reason, jail_days,
                warrant_reference, case_reference, hearing_date, hearing_court_id, posted_by_name,
                enforcement_account_type
            ) VALUES
            (
                26220008, 262200, TIMESTAMP '2026-01-06 09:00:00', 'hist-user-8', 'HST02',
                'Second enforcement reason', 21, 'WR262201', 'CASE-HIST-2',
                TIMESTAMP '2026-02-06 10:00:00', 262200, 'History User Eight', 'COLL'
            ),
            (
                26220009, 262200, TIMESTAMP '2026-01-07 09:00:00', 'hist-user-9', 'HST01',
                'Repeated enforcement reason', 28, 'WR262202', 'CASE-HIST-3',
                TIMESTAMP '2026-02-07 10:00:00', 262200, 'History User Nine', 'COLL'
            )
            """);

        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("itemTypes", "enforcement")
                .header("Authorization", userStateStub.getBearerToken())
        );

        // Assert
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems", hasSize(3)))
            .andExpect(jsonPath("$.historyItems[*].type",
                contains("Enforcement", "Enforcement", "Enforcement")))
            .andExpect(jsonPath("$.historyItems[0].details.enforcementAction").value("HST01"))
            .andExpect(jsonPath("$.historyItems[0].details.daysInDefault").value(28))
            .andExpect(jsonPath("$.historyItems[0].details.warrantNumber").value("WR262202"))
            .andExpect(jsonPath("$.historyItems[1].details.enforcementAction").value("HST02"))
            .andExpect(jsonPath("$.historyItems[1].details.reason").value("Second enforcement reason"))
            .andExpect(jsonPath("$.historyItems[2].details.enforcementAction").value("HST01"))
            .andExpect(jsonPath("$.historyItems[2].details.daysInDefault").value(14));
    }

    @Test
    @DisplayName("PO-2622: INT.03b enforcements with null hearing court are still returned")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_enforcementsWithNullHearingCourt_returnsRows() throws Exception {
        jdbcTemplate.update("""
            INSERT INTO defendant_accounts (
                defendant_account_id, version_number, business_unit_id, account_number, amount_paid,
                account_balance, amount_imposed, account_status, allow_writeoffs, allow_cheques, account_type,
                collection_order, payment_card_requested, originator_name
            ) VALUES (
                262210, 0, 78, '262210A', 0.00, 125.00, 125.00, 'L', 'N', 'N', 'Fine', 'N', 'N',
                'History Sending Court'
            ) ON CONFLICT (defendant_account_id) DO NOTHING
            """);

        jdbcTemplate.update("""
            INSERT INTO enforcements (
                enforcement_id, defendant_account_id, posted_date, posted_by, result_id, reason, jail_days,
                warrant_reference, case_reference, hearing_date, hearing_court_id, posted_by_name,
                enforcement_account_type
            ) VALUES (
                26221001, 262210, TIMESTAMP '2026-01-08 09:00:00', 'hist-user-null-court', 'HST01',
                'Null hearing court enforcement', 11, 'WR262210', 'CASE-HIST-NULL',
                TIMESTAMP '2026-02-08 10:00:00', NULL, 'History User Null Court', 'COLL'
            ) ON CONFLICT (enforcement_id) DO NOTHING
            """);

        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/262210/history")
                .queryParam("itemTypes", "enforcement")
                .header("Authorization", userStateStub.getBearerToken())
        );

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems", hasSize(1)))
            .andExpect(jsonPath("$.historyItems[0].type").value("Enforcement"))
            .andExpect(jsonPath("$.historyItems[0].details.enforcementAction").value("HST01"))
            .andExpect(jsonPath("$.historyItems[0].details.reason").value("Null hearing court enforcement"))
            .andExpect(jsonPath("$.historyItems[0].details.hearingCourt").doesNotExist());
    }

    @Test
    @DisplayName("PO-2622: INT.04 notes and payment terms mapping")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_notesAndPaymentTerms_returnsAllRows() throws Exception {
        // Arrange
        jdbcTemplate.update("""
            INSERT INTO notes (
                note_id, note_type, associated_record_type, associated_record_id, note_text, posted_date,
                posted_by, posted_by_name
            ) VALUES (
                26220010, 'AA', 'defendant_accounts', '262200', 'Second account note',
                TIMESTAMP '2026-01-06 09:00:00', 'hist-user-10', 'History User Ten'
            )
            """);

        jdbcTemplate.update("""
            INSERT INTO payment_terms (
                payment_terms_id, defendant_account_id, posted_date, posted_by, terms_type_code, effective_date,
                instalment_period, instalment_amount, instalment_lump_sum, jail_days, extension, account_balance,
                posted_by_name, active
            ) VALUES (
                26220011, 262200, TIMESTAMP '2026-01-07 09:00:00', 'hist-user-11', 'B',
                TIMESTAMP '2026-02-07 00:00:00', 'M', 35.00, 125.00, 9, FALSE, 450.00,
                'History User Eleven', FALSE
            )
            """);

        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("itemTypes", "note,paymentTerms")
                .header("Authorization", userStateStub.getBearerToken())
        );

        // Assert
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems", hasSize(4)))
            .andExpect(jsonPath("$.historyItems[*].type",
                contains("Payment terms", "Note", "Note", "Payment terms")))
            .andExpect(jsonPath("$.historyItems[0].details.payment_terms_type.payment_terms_type_code")
                .value("B"))
            .andExpect(jsonPath("$.historyItems[0].details.instalment_amount").value(35.00))
            .andExpect(jsonPath("$.historyItems[1].details.noteText").value("Second account note"))
            .andExpect(jsonPath("$.historyItems[2].details.noteText").value("History account note"))
            .andExpect(jsonPath("$.historyItems[3].details.payment_terms_type.payment_terms_type_code")
                .value("I"));
    }

    @Test
    @DisplayName("PO-2622: INT.05 transactions mapping for repeated actions")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_transactions_returnsRepeatedActionsAsDistinctItems() throws Exception {
        // Arrange
        jdbcTemplate.update("""
            INSERT INTO creditor_accounts (
                creditor_account_id, business_unit_id, account_number, creditor_account_type, prosecution_service,
                from_suspense, hold_payout, pay_by_bacs, version_number
            ) VALUES (
                262200, 78, 'CR262200', 'MJ', TRUE, FALSE, FALSE, FALSE, 0
            ) ON CONFLICT (creditor_account_id) DO NOTHING
            """);

        jdbcTemplate.update("""
            INSERT INTO impositions (
                imposition_id, defendant_account_id, posted_date, posted_by, posted_by_name, result_id,
                imposing_court_id, imposed_date, imposed_amount, paid_amount, creditor_account_id, completed
            ) VALUES (
                26220012, 262200, TIMESTAMP '2026-01-06 08:00:00', 'hist-user-12',
                'History User Twelve', 'HST01', 262200, TIMESTAMP '2026-01-06 08:00:00',
                -125.00, 0.00, 262200, FALSE
            ) ON CONFLICT (imposition_id) DO NOTHING
            """);

        jdbcTemplate.update("""
            INSERT INTO defendant_transactions (
                defendant_transaction_id, defendant_account_id, posted_date, posted_by, transaction_type,
                transaction_amount, payment_method, payment_reference, text, status, status_date, status_amount,
                posted_by_name, associated_record_type, associated_record_id, imposed_amount
            ) VALUES (
                26220012, 262200, TIMESTAMP '2026-01-06 09:00:00', 'hist-user-12', 'PAYMNT',
                -25.00, 'NC', 'PAY262201', 'Second history payment', 'P',
                TIMESTAMP '2026-01-06 10:00:00', -25.00, 'History User Twelve',
                'impositions', '26220012', -125.00
            )
            """);

        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("itemTypes", "financial")
                .header("Authorization", userStateStub.getBearerToken())
        );

        // Assert
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems", hasSize(2)))
            .andExpect(jsonPath("$.historyItems[*].type", contains("Financial", "Financial")))
            .andExpect(jsonPath("$.historyItems[0].amount").value(-25.00))
            .andExpect(jsonPath("$.historyItems[0].details.transactionType.transactionType").value("PAYMNT"))
            .andExpect(jsonPath("$.historyItems[0].details.paymentMethod.paymentMethod").value("NC"))
            .andExpect(jsonPath("$.historyItems[0].details.paymentReference").value("PAY262201"))
            .andExpect(jsonPath("$.historyItems[0].details.additionalInformation")
                .value("Second history payment"))
            .andExpect(jsonPath("$.historyItems[0].details.status.defendantTransactionStatus").value("P"))
            .andExpect(jsonPath("$.historyItems[0].details.status.defendantTransactionStatusDisplayName")
                .value("Partially-reversed"))
            .andExpect(jsonPath("$.historyItems[0].details.associatedRecordType").value("impositions"))
            .andExpect(jsonPath("$.historyItems[0].details.associatedRecordId").value("26220012"))
            .andExpect(jsonPath("$.historyItems[0].details.impositionDate").value("2026-01-06"))
            .andExpect(jsonPath("$.historyItems[0].details.impositionCode").value("HST01"))
            .andExpect(jsonPath("$.historyItems[0].details.amountImposed").value(-125.00))
            .andExpect(jsonPath("$.historyItems[1].amount").value(-50.00))
            .andExpect(jsonPath("$.historyItems[1].details.paymentReference").value("PAY262200"))
            .andExpect(jsonPath("$.historyItems[1].details.associatedRecordType").value("defendant_accounts"))
            .andExpect(jsonPath("$.historyItems[1].details.associatedRecordId").value("262200"))
            .andExpect(jsonPath("$.historyItems[1].details.accountNumber").value("262200A"))
            .andExpect(jsonPath("$.historyItems[1].details.sendingCourt").value("History Sending Court"));
    }

    @Test
    @DisplayName("PO-2622: INT.07 dateFrom and dateTo filters are inclusive")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_dateFilters_returnItemsWithinInclusiveRange() throws Exception {
        // Arrange
        // Test data is inserted in BeforeEach.

        // Act
        ResultActions dateFromResult = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("dateFrom", "2026-01-03")
                .header("Authorization", userStateStub.getBearerToken())
        );

        ResultActions dateToResult = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("dateTo", "2026-01-02")
                .header("Authorization", userStateStub.getBearerToken())
        );

        // Assert
        dateFromResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems", hasSize(3)))
            .andExpect(jsonPath("$.historyItems[*].type",
                contains("Note", "Financial", "Payment terms")));

        dateToResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems", hasSize(2)))
            .andExpect(jsonPath("$.historyItems[*].type",
                contains("Enforcement", "Amendment")));
    }

    @Test
    @DisplayName("PO-2622: INT.08 itemTypes filter supports a single category")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_singleItemType_returnsOnlyMatchingItems() throws Exception {
        // Arrange
        // Test data is inserted in BeforeEach.

        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("itemTypes", "note")
                .header("Authorization", userStateStub.getBearerToken())
        );

        // Assert
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems", hasSize(1)))
            .andExpect(jsonPath("$.historyItems[0].type").value("Note"))
            .andExpect(jsonPath("$.historyItems[0].details.noteText").value("History account note"));
    }

    @Test
    @DisplayName("PO-2622: INT.08 itemTypes filter supports multiple categories")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_multipleItemTypes_returnsOnlyMatchingItems() throws Exception {
        // Arrange
        // Test data is inserted in BeforeEach.

        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("itemTypes", "note", "paymentTerms")
                .header("Authorization", userStateStub.getBearerToken())
        );

        // Assert
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems", hasSize(2)))
            .andExpect(jsonPath("$.historyItems[*].type", contains("Note", "Payment terms")));
    }

    @Test
    @DisplayName("PO-2622: INT.09 combined filters return deterministic intersected results")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_combinedFilters_returnDeterministicIntersectedResults() throws Exception {
        // Arrange
        // Test data is inserted in BeforeEach.

        // Act
        ResultActions firstResult = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("dateFrom", "2026-01-02")
                .queryParam("dateTo", "2026-01-04")
                .queryParam("itemTypes", "financial", "enforcement", "amendment")
                .header("Authorization", userStateStub.getBearerToken())
        );

        String firstBody = firstResult.andReturn().getResponse().getContentAsString();

        ResultActions secondResult = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("dateFrom", "2026-01-02")
                .queryParam("dateTo", "2026-01-04")
                .queryParam("itemTypes", "financial", "enforcement", "amendment")
                .header("Authorization", userStateStub.getBearerToken())
        );

        // Assert
        firstResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems", hasSize(2)))
            .andExpect(jsonPath("$.historyItems[*].type", contains("Financial", "Enforcement")));

        secondResult.andExpect(status().isOk())
            .andExpect(content().json(firstBody));
    }

    @Test
    @DisplayName("PO-2622: INT.10 missing authentication returns 401")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_missingAuthentication_returnsUnauthorized() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .header("Authorization", "Bearer ")
        );

        // Assert
        result.andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("Unauthorized"))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @DisplayName("PO-2622: INT.10 missing Search and View Accounts permission returns 403")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_missingPermission_returnsForbidden() throws Exception {
        // Arrange
        userStateStub.setupWithNoPermissions();

        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .header("Authorization", userStateStub.getBearerToken())
        );

        // Assert
        result.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @DisplayName("PO-2622: INT.10 unknown defendant account returns 404")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_unknownDefendantAccount_returnsNotFound() throws Exception {
        // Arrange
        // Authorised user is configured in BeforeEach.

        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/999999999/history")
                .header("Authorization", userStateStub.getBearerToken())
        );

        // Assert
        result.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PO-2622: INT.11 response contains only OpenAPI-documented fields")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_responseStructure_containsOnlyDocumentedFields() throws Exception {
        // Arrange
        jdbcTemplate.update("""
            INSERT INTO document_instances (
                document_instance_id, document_id, business_unit_id, generated_date, generated_by,
                associated_record_type, associated_record_id, status, document_content
            ) VALUES (
                26220001, 'ABD', 78, TIMESTAMP '2026-01-06 09:00:00', 'hist-doc-user',
                'defendant_accounts', '262200', 'New', '<doc><account>262200</account></doc>'
            ) ON CONFLICT (document_instance_id) DO NOTHING
            """);

        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .header("Authorization", userStateStub.getBearerToken())
        );

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":getDefendantAccountHistory_responseStructure_containsOnlyDocumentedFields: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        // Assert: Root level has only documented fields
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems").isArray())
            // Verify each history item has only documented fields
            .andExpect(jsonPath("$.historyItems[0].postedDetails").exists())
            .andExpect(jsonPath("$.historyItems[0].postedDetails.posted_date").exists())
            .andExpect(jsonPath("$.historyItems[0].postedDetails.posted_by").exists())
            .andExpect(jsonPath("$.historyItems[0].type").exists())
            .andExpect(jsonPath("$.historyItems[0].details").exists())
            // Amount field should exist for Financial items only
            .andExpect(jsonPath("$.historyItems[1].amount").exists())
            .andExpect(jsonPath("$.historyItems", hasSize(5)))
            .andExpect(jsonPath("$.historyItems[2].details.posted_details").doesNotExist())
            .andExpect(jsonPath("$.historyItems[2].details.extension").doesNotExist())
            // Verify no generated orders/notices type is returned.
            .andExpect(jsonPath("$.historyItems[*].type",
                contains(
                    "Note",
                    "Financial",
                    "Payment terms",
                    "Enforcement",
                    "Amendment"
                )));
    }

    @Test
    @DisplayName("PO-2622: AC6 same-day mixed-source events are ordered deterministically")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_sameDayMixedSources_orderedDeterministically() throws Exception {
        // Arrange
        jdbcTemplate.update("""
            INSERT INTO amendments (
                amendment_id, business_unit_id, associated_record_type, associated_record_id, amended_date,
                amended_by, field_code, old_value, new_value, case_reference, function_code
            ) VALUES (
                26220020, 78, 'defendant_accounts', '262200', TIMESTAMP '2026-01-10 10:30:45.123',
                'same-day-user-1', 1, 'Old', 'New', 'CASE-SAME-DAY', 'UPD'
            ) ON CONFLICT (amendment_id) DO NOTHING
            """);

        jdbcTemplate.update("""
            INSERT INTO enforcements (
                enforcement_id, defendant_account_id, posted_date, posted_by, result_id, reason, jail_days,
                warrant_reference, case_reference, hearing_date, hearing_court_id, posted_by_name,
                enforcement_account_type
            ) VALUES (
                26220021, 262200, TIMESTAMP '2026-01-10 10:30:45.456', 'same-day-user-2', 'HST01',
                'Same day enforcement', 7, 'WR_SAME', 'CASE-SAME-DAY', TIMESTAMP '2026-02-10 10:00:00',
                262200, 'Same Day User Two', 'COLL'
            ) ON CONFLICT (enforcement_id) DO NOTHING
            """);

        jdbcTemplate.update("""
            INSERT INTO defendant_transactions (
                defendant_transaction_id, defendant_account_id, posted_date, posted_by, transaction_type,
                transaction_amount, payment_method, payment_reference, text, status, status_date, status_amount,
                posted_by_name, associated_record_type, associated_record_id
            ) VALUES (
                26220022, 262200, TIMESTAMP '2026-01-10 10:30:45.789', 'same-day-user-3', 'PAYMNT',
                -10.00, 'NC', 'PAY_SAME', 'Same day payment', 'C', TIMESTAMP '2026-01-10 10:30:45.790',
                -10.00, 'Same Day User Three', 'defendant_accounts', '262200'
            ) ON CONFLICT (defendant_transaction_id) DO NOTHING
            """);

        // Act
        ResultActions firstCall = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("dateFrom", "2026-01-10")
                .queryParam("dateTo", "2026-01-10")
                .header("Authorization", userStateStub.getBearerToken())
        );

        String firstResponse = firstCall.andReturn().getResponse().getContentAsString();

        ResultActions secondCall = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("dateFrom", "2026-01-10")
                .queryParam("dateTo", "2026-01-10")
                .header("Authorization", userStateStub.getBearerToken())
        );

        log.info(":getDefendantAccountHistory_sameDayMixedSources_orderedDeterministically: First call response:\n{}",
            ToJsonString.toPrettyJson(firstResponse));

        // Assert
        firstCall.andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems", hasSize(3)))
            .andExpect(jsonPath("$.historyItems[*].type",
                contains("Enforcement", "Amendment", "Financial")))
            .andExpect(jsonPath("$.historyItems[0].details.enforcementAction").value("HST01"))
            .andExpect(jsonPath("$.historyItems[1].details.oldValue").value("Old"))
            .andExpect(jsonPath("$.historyItems[2].amount").value(-10.00));

        secondCall.andExpect(status().isOk())
            .andExpect(content().json(firstResponse));
    }

    private void insertResult(String resultId, String resultTitle) {
        jdbcTemplate.update("""
            INSERT INTO results (
                result_id, result_title, result_title_cy, result_type, active, imposition, imposition_accruing,
                enforcement, enforcement_override, further_enforcement_warn, further_enforcement_disallow,
                enforcement_hold, requires_enforcer, generates_hearing, generates_warrant, collection_order,
                extend_ttp_disallow, extend_ttp_preserve_last_enf, prevent_payment_card, lists_monies,
                manual_enforcement, allow_payment_terms, requires_employment_data, allow_additional_action,
                requires_lja
            ) VALUES (
                ?, ?, ?, 'Result', TRUE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE,
                FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE
            ) ON CONFLICT (result_id) DO NOTHING
            """, resultId, resultTitle, resultTitle);
    }
}
