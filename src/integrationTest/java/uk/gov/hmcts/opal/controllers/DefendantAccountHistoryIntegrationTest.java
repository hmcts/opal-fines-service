package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@DisplayName("Defendant Account History Controller Integration Tests")
@Slf4j(topic = "opal.DefendantAccountHistoryIntegrationTest")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(scripts = "classpath:db/insertData/insert_opal_account_history_base.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_opal_account_history.sql", executionPhase = AFTER_TEST_METHOD)
class DefendantAccountHistoryIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    private static final long DEFENDANT_ACCOUNT_ID = 262200L;

    @BeforeEach
    void authoriseUser() {
        authorise((short) 78, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
    }

    @Test
    @DisplayName("PO-2622: INT.01 mixed history items returned and ordered")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7619")
    void getDefendantAccountHistory_mixedItems_returnsAllItemsNewestFirst() throws Exception {
        // Arrange
        // Test data is inserted in BeforeEach.

        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @Sql(scripts = "classpath:db/insertData/insert_opal_account_history_amendments.sql",
        executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("PO-2622: INT.02 amendments mapping and multiplicity")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7630")
    void getDefendantAccountHistory_amendments_returnsAllAmendmentRows() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("itemTypes", "amendment")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @Sql(scripts = "classpath:db/insertData/insert_opal_account_history_enforcements.sql",
        executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("PO-2622: INT.03 enforcements mapping and multiplicity")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7632")
    void getDefendantAccountHistory_enforcements_returnsAllEnforcementRows() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("itemTypes", "ENFORCEMENT")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @Sql(scripts = "classpath:db/insertData/insert_opal_account_history_null_court.sql",
        executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("PO-2622: INT.03b enforcements with null hearing court are still returned")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7633")
    void getDefendantAccountHistory_enforcementsWithNullHearingCourt_returnsRows() throws Exception {
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/262210/history")
                .queryParam("itemTypes", "enforcement")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @Sql(scripts = "classpath:db/insertData/insert_opal_account_history_terms.sql",
        executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("PO-2622: INT.04 notes and payment terms mapping")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7620")
    void getDefendantAccountHistory_notesAndPaymentTerms_returnsAllRows() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("itemTypes", "note,paymentTerms")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @Sql(scripts = "classpath:db/insertData/insert_opal_account_history_transactions.sql",
        executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("PO-2622: INT.05 transactions mapping for repeated actions")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7628")
    void getDefendantAccountHistory_transactions_returnsRepeatedActionsAsDistinctItems() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("itemTypes", "financial")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @JiraTestKey("PO-7631")
    void getDefendantAccountHistory_dateFilters_returnItemsWithinInclusiveRange() throws Exception {
        // Arrange
        // Test data is inserted in BeforeEach.

        // Act
        ResultActions dateFromResult = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("dateFrom", "2026-01-03")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("Authorization", userStateStub.getBearerToken())
        );

        ResultActions dateToResult = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("dateTo", "2026-01-02")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @JiraTestKey("PO-7627")
    void getDefendantAccountHistory_singleItemType_returnsOnlyMatchingItems() throws Exception {
        // Arrange
        // Test data is inserted in BeforeEach.

        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("itemTypes", "note")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @JiraTestKey("PO-7622")
    void getDefendantAccountHistory_multipleItemTypes_returnsOnlyMatchingItems() throws Exception {
        // Arrange
        // Test data is inserted in BeforeEach.

        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("itemTypes", "note", "paymentTerms")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @JiraTestKey("PO-7624")
    void getDefendantAccountHistory_combinedFilters_returnDeterministicIntersectedResults() throws Exception {
        // Arrange
        // Test data is inserted in BeforeEach.

        // Act
        ResultActions firstResult = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("dateFrom", "2026-01-02")
                .queryParam("dateTo", "2026-01-04")
                .queryParam("itemTypes", "financial", "enforcement", "amendment")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("Authorization", userStateStub.getBearerToken())
        );

        String firstBody = firstResult.andReturn().getResponse().getContentAsString();

        ResultActions secondResult = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("dateFrom", "2026-01-02")
                .queryParam("dateTo", "2026-01-04")
                .queryParam("itemTypes", "financial", "enforcement", "amendment")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @DisplayName("PO-2622: INT.10 missing authentication returns 403")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7621")
    void getDefendantAccountHistory_missingAuthentication_returnsForbidden() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .header("Authorization", "Bearer ")
        );

        // Assert
        result.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @DisplayName("PO-2622: INT.10 missing Search and View Accounts permission returns 403")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7625")
    void getDefendantAccountHistory_missingPermission_returnsForbidden() throws Exception {
        // Arrange
        userStateStub.setupWithNoPermissions();

        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @JiraTestKey("PO-7623")
    void getDefendantAccountHistory_unknownDefendantAccount_returnsNotFound() throws Exception {
        // Arrange
        // Authorised user is configured in BeforeEach.

        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/999999999/history")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("Authorization", userStateStub.getBearerToken())
        );

        // Assert
        result.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"))
            .andExpect(jsonPath("$.reason").doesNotExist());
    }

    @Test
    @Sql(scripts = "classpath:db/insertData/insert_opal_account_history_document.sql",
        executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("PO-2622: INT.11 response contains only OpenAPI-documented fields")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7629")
    void getDefendantAccountHistory_responseStructure_containsOnlyDocumentedFields() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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
    @Sql(scripts = "classpath:db/insertData/insert_opal_account_history_same_day.sql",
        executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("PO-2622: AC6 same-day mixed-source events are ordered deterministically")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7626")
    void getDefendantAccountHistory_sameDayMixedSources_orderedDeterministically() throws Exception {
        // Act
        ResultActions firstCall = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("dateFrom", "2026-01-10")
                .queryParam("dateTo", "2026-01-10")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("Authorization", userStateStub.getBearerToken())
        );

        String firstResponse = firstCall.andReturn().getResponse().getContentAsString();

        ResultActions secondCall = mockMvc.perform(
            get(URL_BASE + "/" + DEFENDANT_ACCOUNT_ID + "/history")
                .queryParam("dateFrom", "2026-01-10")
                .queryParam("dateTo", "2026-01-10")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
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

}
