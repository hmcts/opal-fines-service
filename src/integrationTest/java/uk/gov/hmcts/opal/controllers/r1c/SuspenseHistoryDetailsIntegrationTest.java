package uk.gov.hmcts.opal.controllers.r1c;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.VIEW_SUSPENSE_ITEMS;
import static uk.gov.hmcts.opal.controllers.shared.util.OpenApiContractAssertions.assertGet200JsonResponseMatchesBundledSpec;

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

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("A single court fee received row with no fee ID returns the OC fallback code")
    void getHistory_mapsOverpaymentWithoutCourtFeeId() throws Exception {
        String payment = transaction(99000000979861L);
        history()
            .andExpect(jsonPath(payment, hasSize(1)))
            .andExpect(jsonPath(payment + ".transaction_type.transaction_type_code", contains("OC")))
            .andExpect(jsonPath(payment + ".details.court_fee_code", contains("OC")))
            .andExpect(jsonPath(payment + ".details.payment_details.payment_method", contains("PO")))
            .andExpect(jsonPath(payment + ".details.payer_details.payer_name", contains("Court Payer Ltd")));
    }

    @ParameterizedTest
    @ValueSource(longs = {99000000979862L, 99000000979863L, 99000000979864L})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Ambiguous fee rows and cross-unit fee links do not use the OC fallback")
    void getHistory_doesNotFallbackForAmbiguousOrCrossUnitCourtFees(long id) throws Exception {
        history()
            .andExpect(jsonPath(transaction(id), hasSize(1)))
            .andExpect(jsonPath(transaction(id) + ".details", everyItem(nullValue())));
    }

    @ParameterizedTest
    @ValueSource(longs = {99000000979805L, 99000000979851L})
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
    @DisplayName("RP linked to an individual defendant account returns defendant repayment details")
    void getHistory_mapsIndividualDefendantRepayment() throws Exception {
        String details = transaction(99000000979826L) + ".details";
        history()
            .andExpect(jsonPath(details + ".defendant_account_number", contains(12345678)))
            .andExpect(jsonPath(details + ".defendant_name.organisation_flag", contains(false)))
            .andExpect(jsonPath(details + ".defendant_name.individual_details.forenames", contains("Alex")))
            .andExpect(jsonPath(details + ".defendant_name.individual_details.surname", contains("Defendant")))
            .andExpect(jsonPath(details + ".creditor_account_number").doesNotExist());
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("RP linked to an organisation defendant account returns organisation repayment details")
    void getHistory_mapsOrganisationDefendantRepayment() throws Exception {
        String details = transaction(99000000979852L) + ".details";
        history()
            .andExpect(jsonPath(details + ".defendant_account_number", contains(23456789)))
            .andExpect(jsonPath(details + ".defendant_name.organisation_flag", contains(true)))
            .andExpect(jsonPath(details + ".defendant_name.organisation_details.organisation_name",
                contains("Defendant Ltd")))
            .andExpect(jsonPath(details + ".creditor_account_number").doesNotExist());
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

    @ParameterizedTest
    @ValueSource(longs = {99000000979814L, 99000000979817L})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("RV returns null details regardless of transaction text or associated account")
    void getHistory_returnsNullDetailsForRv(long id) throws Exception {
        history()
            .andExpect(jsonPath(transaction(id), hasSize(1)))
            .andExpect(jsonPath(transaction(id) + ".transaction_type.transaction_type_code", contains("RV")))
            .andExpect(jsonPath(transaction(id) + ".details", everyItem(nullValue())));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("DQ returns reversal details containing transaction text")
    void getHistory_mapsDishonouredTransactionDescription() throws Exception {
        history().andExpect(jsonPath(transaction(99000000979859L) + ".details.reversal_description",
            contains("Dishonoured payment")));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("DQ returns null details when the required description is unavailable")
    void getHistory_returnsNullForDishonouredTransactionWithoutText() throws Exception {
        String payment = transaction(99000000979832L);
        history()
            .andExpect(jsonPath(payment, hasSize(1)))
            .andExpect(jsonPath(payment + ".details", everyItem(nullValue())));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("UN payments with an originator reference include the linked defendant account")
    void getHistory_mapsImportedPaymentAsAutomatic() throws Exception {
        String details = transaction(99000000979830L) + ".details";
        history()
            .andExpect(jsonPath(details + ".defendant_account_number", contains(12345678)))
            .andExpect(jsonPath(details + ".originator_reference", contains("AUTO-REF")))
            .andExpect(jsonPath(details + ".payment_source", contains("NATWEST")))
            .andExpect(jsonPath(details + ".payment_details").doesNotExist());
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
    @DisplayName("An unresolved optional defendant reference defaults to zero in automatic UN details")
    void getHistory_defaultsUnresolvedAutomaticAccount() throws Exception {
        String details = transaction(99000000979850L) + ".details";
        history()
            .andExpect(jsonPath(details + ".defendant_account_number", contains(0)))
            .andExpect(jsonPath(details + ".originator_reference", contains("OVERFLOW-ACCOUNT")))
            .andExpect(jsonPath(details + ".payment_source", contains("NATWEST")))
            .andExpect(jsonPath(details + ".payment_details").doesNotExist());
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
            .andExpect(jsonPath("$.suspense_transactions", hasSize(2)))
            .andExpect(jsonPath(transaction(99000000979840L) + ".details.payer_details.payer_name",
                contains("Dr Jo Payer")))
            .andExpect(jsonPath(transaction(99000000979840L) + ".details.relevant_details").doesNotExist());
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("MS transactions resolve their linked payment and payer details")
    void getHistory_mapsMiscellaneousTransactionPaymentDetails() throws Exception {
        String payment = transaction(99000000979858L);
        history(99000000979801L)
            .andExpect(jsonPath(payment, hasSize(1)))
            .andExpect(jsonPath(payment + ".transaction_type.transaction_type_code", contains("MS")))
            .andExpect(jsonPath(payment + ".details.payment_details.payment_method", contains("CQ")))
            .andExpect(jsonPath(payment + ".details.payment_details.payment_reference", contains("PAYER-001")))
            .andExpect(jsonPath(payment + ".details.payment_details.received_from", contains("Third Party")))
            .andExpect(jsonPath(payment + ".details.payer_details.payer_type", contains("Individual")))
            .andExpect(jsonPath(payment + ".details.payer_details.payer_name", contains("Dr Jo Payer")));
    }

    @ParameterizedTest
    @ValueSource(longs = {99000000979879L})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("A UN transaction on an OF item does not receive automatic or unidentified details")
    void getHistory_returnsNullForUnmatchedOverpaymentType(long id) throws Exception {
        history(99000000979802L)
            .andExpect(jsonPath(transaction(id), hasSize(1)))
            .andExpect(jsonPath(transaction(id) + ".details", everyItem(nullValue())));
    }

    @ParameterizedTest
    @CsvSource({"99000000979874, 12345678, AUTO-REF, NATWEST",
        "99000000979875, 0, OVERFLOW-ACCOUNT, NATWEST",
        "99000000979876, 0, MANUAL-REF, VARIANT_BANKING",
        "99000000979880, 0, , VARIANT_BANKING",
        "99000000979883, 12345678, DEFENDANT-MANUAL, NATWEST"})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("OF transactions on OF items return automatic details regardless of the payment's auto flag")
    void getHistory_mapsOverpaymentDetails(long id, long accountNumber, String reference, String source)
        throws Exception {
        String payment = transaction(id);
        history(99000000979802L)
            .andExpect(jsonPath("$.suspense_transactions", hasSize(11)))
            .andExpect(jsonPath(payment, hasSize(1)))
            .andExpect(jsonPath(payment + ".details.defendant_account_number", contains((int) accountNumber)))
            .andExpect(jsonPath(payment + ".details.originator_reference", contains(reference)))
            .andExpect(jsonPath(payment + ".details.payment_source", contains(source)))
            .andExpect(jsonPath(payment + ".details.payment_details").doesNotExist())
            .andExpect(jsonPath(payment + ".details.payer_details").doesNotExist());
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("OF miscellaneous-account payments return current payer details instead of automatic details")
    void getHistory_mapsMiscellaneousOverpayment() throws Exception {
        String payment = transaction(99000000979881L);
        history(99000000979802L)
            .andExpect(jsonPath(payment, hasSize(1)))
            .andExpect(jsonPath(payment + ".details.payer_details.payer_type", contains("Organisation")))
            .andExpect(jsonPath(payment + ".details.payer_details.payer_name", contains("Current Payer Ltd")))
            .andExpect(jsonPath(payment + ".details.payer_details.payer_address", contains("5 Payer Road, EF5 6GH")))
            .andExpect(jsonPath(payment + ".details.payment_details.payment_method", contains("NC")))
            .andExpect(jsonPath(payment + ".details.defendant_account_number").doesNotExist())
            .andExpect(jsonPath(payment + ".details.originator_reference").doesNotExist());
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("OF miscellaneous-account selection retains the payment when its cross-unit payer cannot resolve")
    void getHistory_mapsOverpaymentWithUnresolvedPayer() throws Exception {
        String payment = transaction(99000000979882L);
        history(99000000979802L)
            .andExpect(jsonPath(payment, hasSize(1)))
            .andExpect(jsonPath(payment + ".details.payment_details.payment_method", contains("NC")))
            .andExpect(jsonPath(payment + ".details.payer_details", hasSize(1)))
            .andExpect(jsonPath(payment + ".details.payer_details", everyItem(nullValue())))
            .andExpect(jsonPath(payment + ".details.originator_reference").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(longs = {99000000979877L, 99000000979878L})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("OF automatic details retain a null account number when the linked number is invalid")
    void getHistory_mapsInvalidAutomaticAccountNumber(long id) throws Exception {
        String payment = transaction(id);
        history(99000000979802L)
            .andExpect(jsonPath(payment, hasSize(1)))
            .andExpect(jsonPath(payment + ".details.defendant_account_number", everyItem(nullValue())))
            .andExpect(jsonPath(payment + ".details.payment_source", contains("NATWEST")))
            .andExpect(jsonPath(payment + ".details.payment_details").doesNotExist());
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("OF automatic details retain a missing source and originator reference")
    void getHistory_mapsMissingAutomaticSource() throws Exception {
        String payment = transaction(99000000979860L);
        history(99000000979802L)
            .andExpect(jsonPath(payment, hasSize(1)))
            .andExpect(jsonPath(payment + ".details.defendant_account_number", contains(0)))
            .andExpect(jsonPath(payment + ".details.originator_reference", everyItem(nullValue())))
            .andExpect(jsonPath(payment + ".details.payment_source", hasSize(1)))
            .andExpect(jsonPath(payment + ".details.payment_source", everyItem(nullValue())))
            .andExpect(jsonPath(payment + ".details.payment_details").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"payment_source\":null}", "{\"payment_details\":{},\"payer_details\":null}"})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Required nullable payment fields select exactly one details shape in the contract")
    void historyContract_acceptsNullablePaymentDetails(String details) throws Exception {
        assertDetailsContract(details);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{}",
        "{\"payment_source\":123}",
        "{\"payment_details\":{},\"payer_details\":123}",
        "{\"payment_details\":{},\"payer_details\":null,\"payment_source\":null}"
    })
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Nullable payment fields still reject empty, incorrectly typed and mixed details shapes")
    void historyContract_rejectsInvalidDetails(String details) throws Exception {
        assertThrows(AssertionError.class, () -> assertDetailsContract(details));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Identified payments retain payer details when received-from is unknown")
    void getHistory_preservesPayerWithUnknownReceivedFrom() throws Exception {
        String details = transaction(99000000979842L) + ".details";
        history()
            .andExpect(jsonPath(details + ".payer_details.payer_name", contains("Invalid received-from code")))
            .andExpect(jsonPath(details + ".payment_details.payment_method", contains("NC")))
            .andExpect(jsonPath(details + ".payment_details.received_from", everyItem(nullValue())));
    }

    @ParameterizedTest
    @ValueSource(longs = {99000000979815L, 99000000979816L, 99000000979817L, 99000000979818L,
        99000000979819L, 99000000979820L, 99000000979821L, 99000000979822L, 99000000979823L,
        99000000979824L, 99000000979825L, 99000000979827L, 99000000979828L,
        99000000979829L, 99000000979831L, 99000000979833L, 99000000979836L,
        99000000979837L, 99000000979838L, 99000000979839L, 99000000979841L,
        99000000979853L, 99000000979854L, 99000000979855L, 99000000979856L, 99000000979857L})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Unmatched, incomplete, ambiguous and cross-business-unit combinations retain null details")
    void getHistory_returnsNullForUnmatchedCombinations(long id) throws Exception {
        history()
            .andExpect(jsonPath(transaction(id), hasSize(1)))
            .andExpect(jsonPath(transaction(id) + ".details", everyItem(nullValue())));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("XP returns null details even when its defendant transaction reference resolves")
    void getHistory_returnsNullDetailsForXp() throws Exception {
        String payment = transaction(99000000979806L);
        history()
            .andExpect(jsonPath(payment, hasSize(1)))
            .andExpect(jsonPath(payment + ".transaction_type.transaction_type_code", contains("XP")))
            .andExpect(jsonPath(payment + ".details", everyItem(nullValue())));
    }

    @ParameterizedTest
    @ValueSource(longs = {99000000979865L, 99000000979866L, 99000000979867L, 99000000979868L})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Details remain null when required payer data or a creditor address is missing")
    void getHistory_returnsNullForMissingRequiredDetails(long id) throws Exception {
        history()
            .andExpect(jsonPath(transaction(id), hasSize(1)))
            .andExpect(jsonPath(transaction(id) + ".details", everyItem(nullValue())));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("UN details with an originator reference use the automatic shape even for a manual payment")
    void getHistory_mapsManualPaymentWithOriginatorReference() throws Exception {
        String details = transaction(99000000979870L) + ".details";
        history()
            .andExpect(jsonPath(details + ".defendant_account_number", contains(0)))
            .andExpect(jsonPath(details + ".originator_reference", contains("MANUAL-REF")))
            .andExpect(jsonPath(details + ".payment_source", contains("VARIANT_BANKING")))
            .andExpect(jsonPath(details + ".payment_details").doesNotExist())
            .andExpect(jsonPath(details + ".relevant_details").doesNotExist());
    }

    @ParameterizedTest
    @CsvSource({"99000000979869, VARIANT_BANKING", "99000000979872, NATWEST"})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("UN payments with null or empty originator references return unidentified details")
    void getHistory_mapsMissingOriginatorReference(long id, String source) throws Exception {
        String payment = transaction(id);
        history()
            .andExpect(jsonPath(payment, hasSize(1)))
            .andExpect(jsonPath(payment + ".details.payment_details.payment_method", contains("CT")))
            .andExpect(jsonPath(payment + ".details.payment_details.payment_source.payment_source", contains(source)))
            .andExpect(jsonPath(payment + ".details.originator_reference").doesNotExist());
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("UN items without an originator reference use unidentified details even when a payer is known")
    void getHistory_mapsUnidentifiedItemWithKnownPayer() throws Exception {
        String details = transaction(99000000979871L) + ".details";
        history()
            .andExpect(jsonPath(details + ".payment_details.payment_reference", contains("PAYER-001")))
            .andExpect(jsonPath(details + ".payment_details.received_from", contains("Third Party")))
            .andExpect(jsonPath(details + ".payer_details").doesNotExist());
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("A whitespace originator reference counts as nonempty for automatic selection")
    void getHistory_acceptsWhitespaceOriginatorReference() throws Exception {
        String details = transaction(99000000979873L) + ".details";
        history()
            .andExpect(jsonPath(details + ".defendant_account_number", contains(0)))
            .andExpect(jsonPath(details + ".originator_reference", contains(" ")))
            .andExpect(jsonPath(details + ".payment_source", contains("NATWEST")))
            .andExpect(jsonPath(details + ".payment_details").doesNotExist());
    }

    private void assertDetailsContract(String details) throws Exception {
        var body = objectMapper.readTree("""
            {"suspense_transactions":[{"details":%s}]}
            """.formatted(details));
        assertGet200JsonResponseMatchesBundledSpec(body, "/suspense-items/{suspenseItemId}/history");
    }

    private ResultActions history() throws Exception {
        return history(99000000979800L).andExpect(jsonPath("$.suspense_transactions", hasSize(70)));
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
