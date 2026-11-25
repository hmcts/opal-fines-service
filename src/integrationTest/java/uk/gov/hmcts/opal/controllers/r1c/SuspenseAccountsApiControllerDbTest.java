package uk.gov.hmcts.opal.controllers.r1c;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.VIEW_SUSPENSE_ITEMS;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@DisplayName("Suspense item summary database integration tests")
@Sql(scripts = {
    "classpath:db/insertData/insert_into_suspense_item_summary.sql",
    "classpath:db/insertData/insert_into_suspense_item_history_payments.sql"
}, executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = {
    "classpath:db/deleteData/delete_from_suspense_item_history_payments.sql",
    "classpath:db/deleteData/delete_from_suspense_item_summary.sql"
}, executionPhase = AFTER_TEST_CLASS)
@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1c-suspense=true")
class SuspenseAccountsApiControllerDbTest extends AbstractIntegrationTest {

    private static final String SUMMARY_URL =
        "/suspense-accounts/{business_unit_id}/suspense-items/{suspense_item_number}/summary";

    private static final String HISTORY_URL = "/suspense-items/{suspenseItemId}/history";

    private static final String AUTH_HEADER = "Bearer some_value";

    @ParameterizedTest
    @EnumSource(value = FinesPermission.class, names = { "VIEW_SUSPENSE_ITEMS", "MANAGE_SUSPENSE_ITEMS" })
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Either suspense permission allows retrieving a stored item and its transaction balance")
    void getSummary_returnsStoredItem(FinesPermission permission) throws Exception {
        getSummary((short) 29101, 100000L, permission)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.business_unit_details.business_unit_id").value(29101))
            .andExpect(jsonPath("$.business_unit_details.business_unit_name").value("Suspense Summary A"))
            .andExpect(jsonPath("$.suspense_item_id").value(99000000979601L))
            .andExpect(jsonPath("$.suspense_item_number").value(100000))
            .andExpect(jsonPath("$.created_date").value("2026-11-03T10:15:30"))
            .andExpect(jsonPath("$.suspense_item_type.item_type").value("CF"))
            .andExpect(jsonPath("$.balance").value("75.25"));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("The same item number in another business unit returns that unit's item")
    void getSummary_distinguishesBusinessUnits() throws Exception {
        getSummary((short) 29102, 100000L, VIEW_SUSPENSE_ITEMS)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.business_unit_details.business_unit_id").value(29102))
            .andExpect(jsonPath("$.business_unit_details.business_unit_name").value("Suspense Summary B"))
            .andExpect(jsonPath("$.suspense_item_id").value(99000000979602L))
            .andExpect(jsonPath("$.suspense_item_number").value(100000))
            .andExpect(jsonPath("$.balance").value("0.00"));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("BIGINT item numbers are preserved and an item without an index entry remains visible")
    void getSummary_preservesLargeItemNumberWithoutIndex() throws Exception {
        getSummary((short) 29101, 3000000000L, VIEW_SUSPENSE_ITEMS)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.suspense_item_id").value(99000000979603L))
            .andExpect(jsonPath("$.suspense_item_number").value(3000000000L))
            .andExpect(jsonPath("$.balance").value("0.00"));
    }

    @ParameterizedTest
    @CsvSource({ "29101, 999999", "29102, 3000000000", "29103, 100000" })
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Missing or mismatched business-unit and item values return not found")
    void getSummary_rejectsMissingOrMismatchedValues(short businessUnitId, long itemNumber) throws Exception {
        SuspenseItemSummaryCommonStrict expected = SuspenseItemSummaryCommonStrict.builder().build();
        ObjectMapper mapper = new ObjectMapper();
        //String expectedString = mapper.writeValueAsString(expected);

        ResultActions actions = getSummary(businessUnitId, itemNumber, VIEW_SUSPENSE_ITEMS)
            .andExpect(status().isOk());
        String result = actions.andReturn().getResponse().getContentAsString();
        SuspenseItemSummaryCommonStrict response = mapper.readValue(result, SuspenseItemSummaryCommonStrict.class);

        assertEquals(expected, response);
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("A user without a suspense permission cannot retrieve an existing item")
    void getSummary_rejectsMissingPermission() throws Exception {
        getSummary((short) 29101, 100000L)
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("A user with a suspense permission can retrieve summary history")
    void getHistory_getWithPermissions() throws Exception {
        getHistoryAndTransactions((short) 29101, 99000000979601L, VIEW_SUSPENSE_ITEMS)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.business_unit_id").value(29101))
            .andExpect(jsonPath("$.suspense_item_id").value(99000000979601L))
            .andExpect(jsonPath("$.suspense_notes").isArray())
            .andExpect(jsonPath("$.suspense_notes.length()").value(0))
            .andExpect(jsonPath("$.suspense_transactions").isArray())
            .andExpect(jsonPath("$.suspense_transactions.length()").value(2));
    }

    @ParameterizedTest
    @CsvSource({"99000000979701, BANK-REFERENCE", "99000000979702, FLAT-REFERENCE"})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Automatic payment history uses the linked payment reference and till source")
    void getHistory_populatesAutomaticPaymentDetails(long transactionId, String reference) throws Exception {
        String transaction = transactionPath(transactionId);
        getPaymentHistory()
            .andExpect(jsonPath(transaction, hasSize(1)))
            .andExpect(jsonPath(transaction + ".details.originator_reference", contains(reference)))
            .andExpect(jsonPath(transaction + ".details.payment_source", contains("NATWEST")))
            .andExpect(jsonPath(transaction + ".details.payment_details").doesNotExist());
    }

    @ParameterizedTest
    @CsvSource({
        "99000000979703, Third Party, CQ, CHEQUE-123, ALLPAY, Payer unknown",
        "99000000979704, Defendant, NC, CASH-456, OTHER, No account reference"
    })
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Manual unidentified history includes wrapped and unwrapped payment information")
    void getHistory_populatesManualUnidentifiedDetails(long transactionId, String receivedFrom, String method,
                                                       String reference, String source, String relevantDetails)
        throws Exception {
        String details = transactionPath(transactionId) + ".details";
        getPaymentHistory()
            .andExpect(jsonPath(details + ".payment_details.received_from", contains(receivedFrom)))
            .andExpect(jsonPath(details + ".payment_details.payment_method", contains(method)))
            .andExpect(jsonPath(details + ".payment_details.payment_reference", contains(reference)))
            .andExpect(jsonPath(details + ".payment_details.payment_source.payment_source", contains(source)))
            .andExpect(jsonPath(details + ".relevant_details", contains(relevantDetails)))
            .andExpect(jsonPath(details + ".originator_reference").doesNotExist());
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Manual applicant payments use the API received-from value")
    void getHistory_populatesApplicantReceivedFrom() throws Exception {
        getPaymentHistory()
            .andExpect(jsonPath(transactionPath(99000000979714L) + ".details.payment_details.received_from",
                contains("Applicant")));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Missing optional payment information does not prevent history being returned")
    void getHistory_handlesMissingOptionalInformation() throws Exception {
        String manual = transactionPath(99000000979705L) + ".details";
        String automatic = transactionPath(99000000979713L) + ".details";
        getPaymentHistory()
            .andExpect(jsonPath(manual + ".payment_details.payment_method", contains("PO")))
            .andExpect(jsonPath(manual + ".payment_details.payment_reference", everyItem(nullValue())))
            .andExpect(jsonPath(manual + ".payment_details.received_from", everyItem(nullValue())))
            .andExpect(jsonPath(manual + ".payment_details.payment_source", everyItem(nullValue())))
            .andExpect(jsonPath(manual + ".relevant_details", everyItem(nullValue())))
            .andExpect(jsonPath(automatic + ".originator_reference", everyItem(nullValue())))
            .andExpect(jsonPath(automatic + ".payment_source", contains("NATWEST")));
    }

    @ParameterizedTest
    @ValueSource(longs = {99000000979706L, 99000000979707L, 99000000979708L,
        99000000979709L, 99000000979710L, 99000000979712L})
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("Unmatched, unrelated and other-business-unit references retain their transaction without details")
    void getHistory_doesNotPopulateUnrelatedPaymentDetails(long transactionId) throws Exception {
        String transaction = transactionPath(transactionId);
        getPaymentHistory()
            .andExpect(jsonPath(transaction, hasSize(1)))
            .andExpect(jsonPath(transaction + ".details", everyItem(nullValue())));
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("A miscellaneous suspense item does not receive unidentified-payment details")
    void getHistory_doesNotTreatMiscellaneousPaymentsAsUnidentified() throws Exception {
        getHistoryAndTransactions((short) 29101, 99000000979701L, VIEW_SUSPENSE_ITEMS)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.suspense_transactions", hasSize(1)))
            .andExpect(jsonPath("$.suspense_transactions[0].transaction_id").value(99000000979711L))
            .andExpect(jsonPath("$.suspense_transactions[0].details").doesNotExist());
    }

    private ResultActions getPaymentHistory() throws Exception {
        return getHistoryAndTransactions((short) 29101, 99000000979700L, VIEW_SUSPENSE_ITEMS)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.suspense_transactions", hasSize(13)));
    }

    private static String transactionPath(long transactionId) {
        return "$.suspense_transactions[?(@.transaction_id == " + transactionId + ")]";
    }

    @Test
    @JiraStory("PO-9026")
    @JiraEpic("PO-9026")
    @DisplayName("A user without a suspense permission cannot retrieve an existing item")
    void getHistoryAndTransactions_rejectsMissingPermission() throws Exception {
        getHistoryAndTransactions((short) 29101, 100000L)
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    private ResultActions getSummary(short businessUnitId, long itemNumber, FinesPermission... permissions)
        throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(businessUnitId, permissions);

        return mockMvc.perform(get(SUMMARY_URL, businessUnitId, itemNumber)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));
    }

    private ResultActions getHistoryAndTransactions(short businessUnitId, long itemId,
        FinesPermission... permissions) throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(businessUnitId, permissions);

        return mockMvc.perform(get(HISTORY_URL, itemId)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .accept(MediaType.APPLICATION_JSON)
            .header("Business-Unit-Id", businessUnitId)
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));
    }

}
