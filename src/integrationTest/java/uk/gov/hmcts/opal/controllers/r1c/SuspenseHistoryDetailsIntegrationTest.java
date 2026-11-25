package uk.gov.hmcts.opal.controllers.r1c;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.VIEW_SUSPENSE_ITEMS;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@DisplayName("Provisional suspense history detail mappings")
@Sql(scripts = {
    "classpath:db/insertData/insert_into_suspense_item_summary.sql",
    "classpath:db/insertData/insert_into_suspense_history_linked_details.sql"
}, executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = {
    "classpath:db/deleteData/delete_from_suspense_history_linked_details.sql",
    "classpath:db/deleteData/delete_from_suspense_item_summary.sql"
}, executionPhase = AFTER_TEST_CLASS)
@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1c-suspense=true")
class SuspenseHistoryDetailsIntegrationTest extends AbstractIntegrationTest {

    @ParameterizedTest
    @CsvSource({"99000000979801, Individual, Dr Jo Payer, CQ, '8 Payer Road, KL1 2MN'",
        "99000000979802, Organisation, Payer Ltd, NC, 9 Payer Road"})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Identified manual payments map individual and organisation payer information")
    void getHistory_mapsManualPayer(long id, String type, String name, String method, String address) throws Exception {
        String details = transaction(id) + ".details";
        history()
            .andExpect(jsonPath(details + ".payer_details.payer_type", contains(type)))
            .andExpect(jsonPath(details + ".payer_details.payer_name", contains(name)))
            .andExpect(jsonPath(details + ".payer_details.payer_address", contains(address)))
            .andExpect(jsonPath(details + ".payment_details.payment_method", contains(method)))
            .andExpect(jsonPath(details + ".court_fee_code").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(longs = {99000000979803L, 99000000979804L})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Court fee and court fee overpayment details use the linked fee rather than stale JSON")
    void getHistory_mapsCourtFee(long id) throws Exception {
        String details = transaction(id) + ".details";
        history()
            .andExpect(jsonPath(details + ".court_fee_code", contains("HISTORY")))
            .andExpect(jsonPath(details + ".payment_details.payment_method", contains("PO")))
            .andExpect(jsonPath(details + ".payment_details.received_from", contains("Applicant")))
            .andExpect(jsonPath(details + ".payer_details.payer_name", contains("Court Payer Ltd")));
    }

    @ParameterizedTest
    @ValueSource(longs = {99000000979805L, 99000000979806L})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Defendant account and transaction references resolve the defendant's name and account number")
    void getHistory_mapsDefendant(long id) throws Exception {
        String details = transaction(id) + ".details";
        history()
            .andExpect(jsonPath(details + ".defendant_account_number", contains(12345678)))
            .andExpect(jsonPath(details + ".defendant_name.party_id", contains("99000000979801")))
            .andExpect(jsonPath(details + ".defendant_name.organisation_flag", contains(false)))
            .andExpect(jsonPath(details + ".defendant_name.individual_details.title", contains("Ms")))
            .andExpect(jsonPath(details + ".defendant_name.individual_details.forenames", contains("Alex")))
            .andExpect(jsonPath(details + ".defendant_name.individual_details.surname", contains("Defendant")))
            .andExpect(jsonPath(details + ".creditor_summary").doesNotExist());
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Organisation defendants use organisation details")
    void getHistory_mapsOrganisationDefendant() throws Exception {
        String details = transaction(99000000979807L) + ".details";
        history()
            .andExpect(jsonPath(details + ".defendant_account_number", contains(23456789)))
            .andExpect(jsonPath(details + ".defendant_name.organisation_flag", contains(true)))
            .andExpect(jsonPath(details + ".defendant_name.organisation_details.organisation_name",
                contains("Defendant Ltd")))
            .andExpect(jsonPath(details + ".defendant_name.individual_details").doesNotExist());
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Minor creditor details include the individual summary and full address")
    void getHistory_mapsMinorCreditor() throws Exception {
        String details = transaction(99000000979808L) + ".details";
        history()
            .andExpect(jsonPath(details + ".creditor_account_number", contains("MINOR-IND")))
            .andExpect(jsonPath(details + ".creditor_summary.creditor_account_id", contains(99000000979801L)))
            .andExpect(jsonPath(details + ".creditor_summary.creditor_account_type_reference.creditor_account_type",
                contains("MN")))
            .andExpect(jsonPath(details + ".creditor_summary.individual_name.surname", contains("Creditor")))
            .andExpect(jsonPath(details + ".creditor_address.address_line_1", contains("3 Creditor Road")))
            .andExpect(jsonPath(details + ".creditor_address.address_line_4", contains("County")))
            .andExpect(jsonPath(details + ".creditor_address.address_line_5", contains("District")))
            .andExpect(jsonPath(details + ".creditor_address.postcode", contains("CD3 4EF")));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Creditor transaction references resolve major creditor names and addresses")
    void getHistory_mapsMajorCreditorThroughTransaction() throws Exception {
        String details = transaction(99000000979809L) + ".details";
        history()
            .andExpect(jsonPath(details + ".creditor_account_number", contains("MAJOR-ORG")))
            .andExpect(jsonPath(details + ".creditor_summary.major_creditor_name", contains("History Major Creditor")))
            .andExpect(jsonPath(details + ".creditor_summary.creditor_account_type_reference.creditor_account_type",
                contains("MJ")))
            .andExpect(jsonPath(details + ".creditor_address.address_line_1", contains("6 Major Road")));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Minor organisation creditors use the company name")
    void getHistory_mapsOrganisationCreditor() throws Exception {
        String details = transaction(99000000979810L) + ".details";
        history()
            .andExpect(jsonPath(details + ".creditor_summary.minor_creditor_organisation_flag", contains(true)))
            .andExpect(jsonPath(details + ".creditor_summary.company_name.organisation_name", contains("Creditor Ltd")))
            .andExpect(jsonPath(details + ".creditor_summary.individual_name", everyItem(nullValue())));
    }

    @ParameterizedTest
    @CsvSource({"99000000979811, REPAY-BACS, Pay by BACS", "99000000979812, REPAY-CHEQUE, Pay by Cheque"})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("RP requires a repayment creditor and uses its payment method")
    void getHistory_mapsRepayment(long id, String number, String method) throws Exception {
        String details = transaction(id) + ".details";
        history()
            .andExpect(jsonPath(details + ".creditor_account_number", contains(number)))
            .andExpect(jsonPath(details + ".creditor_payment_method", contains(method)))
            .andExpect(jsonPath(details + ".creditor_summary", hasSize(1)))
            .andExpect(jsonPath(details + ".creditor_address", hasSize(1)));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("IN resolves an imposition, its defendant and its creditor")
    void getHistory_mapsAdjustment() throws Exception {
        String details = transaction(99000000979813L) + ".details";
        history()
            .andExpect(jsonPath(details + ".imposition_id", contains(99000000979801L)))
            .andExpect(jsonPath(details + ".defendant_account_number", contains(12345678)))
            .andExpect(jsonPath(details + ".defendant_name.individual_details.surname", contains("Defendant")))
            .andExpect(jsonPath(details + ".creditor_summary.creditor_account_id", contains(99000000979801L)))
            .andExpect(jsonPath(details + ".creditor_address.postcode", contains("CD3 4EF")));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("RV uses transaction text as the reversal description")
    void getHistory_mapsReversal() throws Exception {
        history().andExpect(jsonPath(transaction(99000000979814L) + ".details.reversal_description",
            contains("Duplicate allocation reversed")));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Automatic payments include a linked numeric defendant account number")
    void getHistory_mapsAutomaticDefendantAccountNumber() throws Exception {
        String details = transaction(99000000979830L) + ".details";
        history()
            .andExpect(jsonPath(details + ".defendant_account_number", contains(12345678)))
            .andExpect(jsonPath(details + ".originator_reference", contains("AUTO-REF")))
            .andExpect(jsonPath(details + ".payment_source", contains("NATWEST")));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Central fund creditor details use the business unit's configuration address")
    void getHistory_mapsCentralFund() throws Exception {
        String details = transaction(99000000979834L) + ".details";
        history()
            .andExpect(jsonPath(details + ".creditor_summary.creditor_account_type_reference.creditor_account_type",
                contains("CF")))
            .andExpect(jsonPath(details + ".creditor_address.address_line_1", contains("7 Central Road")));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("A repayment creditor reached through an imposition is not a supported RP reference")
    void getHistory_doesNotRepayAnImpositionReference() throws Exception {
        history()
            .andExpect(jsonPath(transaction(99000000979843L), hasSize(1)))
            .andExpect(jsonPath(transaction(99000000979843L) + ".details", everyItem(nullValue())));
    }

    @ParameterizedTest
    @ValueSource(longs = {99000000979844L, 99000000979845L, 99000000979846L, 99000000979847L, 99000000979848L})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Overflow and noncanonical transaction and payment references remain unmatched")
    void getHistory_rejectsInvalidNumericReferences(long id) throws Exception {
        history()
            .andExpect(jsonPath(transaction(id), hasSize(1)))
            .andExpect(jsonPath(transaction(id) + ".details", everyItem(nullValue())));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Valid nineteen-digit bigint record IDs still resolve")
    void getHistory_resolvesLargeRecordIds() throws Exception {
        history().andExpect(jsonPath(transaction(99000000979849L) + ".details.defendant_account_number",
            contains(87654321)));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("An overflowing optional defendant reference preserves the automatic payment")
    void getHistory_preservesAutomaticPaymentsWithInvalidAccountReferences() throws Exception {
        String details = transaction(99000000979850L) + ".details";
        history()
            .andExpect(jsonPath(details + ".originator_reference", contains("OVERFLOW-ACCOUNT")))
            .andExpect(jsonPath(details + ".defendant_account_number", everyItem(nullValue())));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Durable miscellaneous-account payer details override stale payment JSON")
    void getHistory_mapsCurrentPayer() throws Exception {
        String details = transaction(99000000979835L) + ".details.payer_details";
        history()
            .andExpect(jsonPath(details + ".payer_name", contains("Current Payer Ltd")))
            .andExpect(jsonPath(details + ".payer_address", contains("5 Payer Road, EF5 6GH")));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("UN transactions for identified miscellaneous items use payer details")
    void getHistory_mapsIdentifiedMiscellaneousPayment() throws Exception {
        history(99000000979801L)
            .andExpect(jsonPath("$.suspense_transactions", hasSize(1)))
            .andExpect(jsonPath("$.suspense_transactions[0].details.payer_details.payer_name").value("Dr Jo Payer"))
            .andExpect(jsonPath("$.suspense_transactions[0].details.relevant_details").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(longs = {99000000979815L, 99000000979816L, 99000000979817L, 99000000979818L,
        99000000979819L, 99000000979820L, 99000000979821L, 99000000979822L, 99000000979823L,
        99000000979824L, 99000000979825L, 99000000979826L, 99000000979827L, 99000000979828L,
        99000000979829L, 99000000979831L, 99000000979832L, 99000000979833L, 99000000979836L,
        99000000979837L, 99000000979838L, 99000000979839L, 99000000979841L, 99000000979842L})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Unmatched, incomplete, ambiguous and cross-business-unit combinations retain null details")
    void getHistory_returnsNullForUnmatchedCombinations(long id) throws Exception {
        history()
            .andExpect(jsonPath(transaction(id), hasSize(1)))
            .andExpect(jsonPath(transaction(id) + ".details", everyItem(nullValue())));
    }

    private ResultActions history() throws Exception {
        return history(99000000979800L).andExpect(jsonPath("$.suspense_transactions", hasSize(49)));
    }

    private ResultActions history(long itemId) throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 29101, VIEW_SUSPENSE_ITEMS);
        return mockMvc.perform(get("/suspense-items/{suspenseItemId}/history", itemId)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .accept(MediaType.APPLICATION_JSON)
                .header("Business-Unit-Id", 29101)
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private static String transaction(long id) {
        return "$.suspense_transactions[?(@.transaction_id == " + id + ")]";
    }
}
