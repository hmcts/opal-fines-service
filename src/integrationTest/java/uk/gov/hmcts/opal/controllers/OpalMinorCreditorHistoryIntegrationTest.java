package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noPermissionsUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;

import jakarta.persistence.QueryTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@Sql(scripts = {
    "classpath:db/deleteData/delete_from_po_2642_minor_creditor_history.sql",
    "classpath:db/insertData/insert_into_po_2642_minor_creditor_history.sql"
}, executionPhase = BEFORE_TEST_CLASS)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_po_2642_minor_creditor_history.sql",
    executionPhase = AFTER_TEST_CLASS
)
@DisplayName("Minor Creditor History Controller Integration Tests")
class OpalMinorCreditorHistoryIntegrationTest extends AbstractIntegrationTest {

    private static final String HISTORY_URL = "/minor-creditor-accounts/{accountId}/history";
    private static final String AUTH_HEADER = "Bearer some_value";
    private static final long MINOR_CREDITOR_ACCOUNT_ID = 99264200000001L;
    private static final long NON_MINOR_CREDITOR_ACCOUNT_ID = 99264200000002L;
    private static final long MISSING_CREDITOR_ACCOUNT_ID = 99264299999999L;
    private static final long DEFENDANT_ACCOUNT_ID = 99264200001001L;
    private static final short BUSINESS_UNIT_ID = 32642;

    @MockitoBean
    private UserStateService userStateService;

    @BeforeEach
    void setUpAuthorisedUser() {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(permissionUser(
            BUSINESS_UNIT_ID,
            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS
        ));
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 INT.01/INT.05 returns all amendment history only when filtered")
    void getMinorCreditorHistory_whenFilteredToAmendments_returnsAllAmendments() throws Exception {
        ResultActions result = getHistory("itemTypes", "amendment");

        expectSuccessfulHistoryResponse(result, 3);
        expectAmendment(result, 0, "2026-01-31", "tie-1-old", "tie-1-new");
        expectAmendment(result, 1, "2026-01-31", "tie-2-old", "tie-2-new");
        expectAmendment(result, 2, "2026-01-15", "baseline-old", "baseline-new");
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 INT.02/INT.06 returns all note history only when filtered")
    void getMinorCreditorHistory_whenFilteredToNotes_returnsAllNotes() throws Exception {
        ResultActions result = getHistory("itemTypes", "note");

        expectSuccessfulHistoryResponse(result, 2);
        expectNote(result, 0, "2026-01-31", "Same timestamp PO-2642 note");
        expectNote(result, 1, "2026-01-10", "Older PO-2642 note");
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 INT.03/INT.07 returns all financial history only when filtered")
    void getMinorCreditorHistory_whenFilteredToFinancial_returnsAllFinancialItems() throws Exception {
        ResultActions result = getHistory("itemTypes", "financial");

        expectSuccessfulHistoryResponse(result, 3);
        expectFinancial(result, 0, "2026-01-31", "FIN003", 31.00);
        expectFinancial(result, 1, "2026-01-25", "FIN002", 25.00);
        expectFinancial(result, 2, "2026-01-05", "FIN001", 10.00);
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 INT.04 returns merged history latest-to-oldest")
    void getMinorCreditorHistory_whenNoFilters_returnsMergedLatestToOldestHistory() throws Exception {
        ResultActions result = getHistory();

        expectSuccessfulHistoryResponse(result, 8);
        expectAmendment(result, 0, "2026-01-31", "tie-1-old", "tie-1-new");
        expectAmendment(result, 1, "2026-01-31", "tie-2-old", "tie-2-new");
        expectFinancial(result, 2, "2026-01-31", "FIN003", 31.00);
        expectNote(result, 3, "2026-01-31", "Same timestamp PO-2642 note");
        expectFinancial(result, 4, "2026-01-25", "FIN002", 25.00);
        expectAmendment(result, 5, "2026-01-15", "baseline-old", "baseline-new");
        expectNote(result, 6, "2026-01-10", "Older PO-2642 note");
        expectFinancial(result, 7, "2026-01-05", "FIN001", 10.00);
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 INT.08 applies dateFrom as an inclusive lower bound")
    void getMinorCreditorHistory_whenDateFromProvided_appliesInclusiveLowerBound() throws Exception {
        ResultActions result = getHistory("dateFrom", "2026-01-15");

        expectSuccessfulHistoryResponse(result, 6);
        expectAmendment(result, 0, "2026-01-31", "tie-1-old", "tie-1-new");
        expectAmendment(result, 5, "2026-01-15", "baseline-old", "baseline-new");
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 INT.09 applies dateTo as an inclusive upper bound")
    void getMinorCreditorHistory_whenDateToProvided_appliesInclusiveUpperBound() throws Exception {
        ResultActions result = getHistory("dateTo", "2026-01-10");

        expectSuccessfulHistoryResponse(result, 2);
        expectNote(result, 0, "2026-01-10", "Older PO-2642 note");
        expectFinancial(result, 1, "2026-01-05", "FIN001", 10.00);
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 INT.10 applies an inclusive date range")
    void getMinorCreditorHistory_whenDateRangeProvided_returnsItemsWithinRange() throws Exception {
        ResultActions result = getHistory("dateFrom", "2026-01-10", "dateTo", "2026-01-25");

        expectSuccessfulHistoryResponse(result, 3);
        expectFinancial(result, 0, "2026-01-25", "FIN002", 25.00);
        expectAmendment(result, 1, "2026-01-15", "baseline-old", "baseline-new");
        expectNote(result, 2, "2026-01-10", "Older PO-2642 note");
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 INT.11 combines date and item type filters")
    void getMinorCreditorHistory_whenDateAndTypeFiltersProvided_appliesBothFilters() throws Exception {
        ResultActions result = getHistory(
            "itemTypes", "financial",
            "dateFrom", "2026-01-20",
            "dateTo", "2026-01-31"
        );

        expectSuccessfulHistoryResponse(result, 2);
        expectFinancial(result, 0, "2026-01-31", "FIN003", 31.00);
        expectFinancial(result, 1, "2026-01-25", "FIN002", 25.00);
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 INT.12 orders equal timestamps deterministically")
    void getMinorCreditorHistory_whenTimestampsMatch_ordersByTypeThenSourceId() throws Exception {
        ResultActions result = getHistory("dateFrom", "2026-01-31", "dateTo", "2026-01-31");

        expectSuccessfulHistoryResponse(result, 4);
        expectAmendment(result, 0, "2026-01-31", "tie-1-old", "tie-1-new");
        expectAmendment(result, 1, "2026-01-31", "tie-2-old", "tie-2-new");
        expectFinancial(result, 2, "2026-01-31", "FIN003", 31.00);
        expectNote(result, 3, "2026-01-31", "Same timestamp PO-2642 note");
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 schema assertions verify documented history item shapes")
    void getMinorCreditorHistory_whenHistoryExists_returnsDocumentedPolymorphicShapes() throws Exception {
        ResultActions result = getHistory("dateFrom", "2026-01-31", "dateTo", "2026-01-31");

        expectSuccessfulHistoryResponse(result, 4);
        result.andExpect(jsonPath("$.historyItems[0].type").value("Amendment"))
            .andExpect(jsonPath("$.historyItems[0].postedDetails.posted_date").value("2026-01-31"))
            .andExpect(jsonPath("$.historyItems[0].postedDetails.posted_by").value("AMEND2"))
            .andExpect(jsonPath("$.historyItems[0].postedDetails.posted_by_name").value("Amend User Two"))
            .andExpect(jsonPath("$.historyItems[0].details.attributeName").value("Hold Pay Out"))
            .andExpect(jsonPath("$.historyItems[0].details.oldValue").value("tie-1-old"))
            .andExpect(jsonPath("$.historyItems[0].details.newValue").value("tie-1-new"))
            .andExpect(jsonPath("$.historyItems[0].details.noteText").doesNotExist())
            .andExpect(jsonPath("$.historyItems[0].details.transactionType").doesNotExist())
            .andExpect(jsonPath("$.historyItems[0].amount").value(nullValue()))
            .andExpect(jsonPath("$.historyItems[2].type").value("Financial"))
            .andExpect(jsonPath("$.historyItems[2].details.transactionType.transactionType").value("PAYMNT"))
            .andExpect(jsonPath("$.historyItems[2].details.transactionType.transactionTypeDisplayName").value("PAYMNT"))
            .andExpect(jsonPath("$.historyItems[2].details.paymentReference").value("FIN003"))
            .andExpect(jsonPath("$.historyItems[2].details.status.creditorTransactionStatus").value("C"))
            .andExpect(jsonPath("$.historyItems[2].details.status.creditorTransactionStatusDisplayName").value("C"))
            .andExpect(jsonPath("$.historyItems[2].details.associatedRecordType").value("defendant_accounts"))
            .andExpect(jsonPath("$.historyItems[2].details.associatedRecordId")
                           .value(String.valueOf(DEFENDANT_ACCOUNT_ID)))
            .andExpect(jsonPath("$.historyItems[2].details.accountNumber").value("P264MN01"))
            .andExpect(jsonPath("$.historyItems[2].details.defendantAccountNumber").value("P264DEF1"))
            .andExpect(jsonPath("$.historyItems[2].details.defendantAccountId").value(DEFENDANT_ACCOUNT_ID))
            .andExpect(jsonPath("$.historyItems[2].details.attributeName").doesNotExist())
            .andExpect(jsonPath("$.historyItems[2].details.noteText").doesNotExist())
            .andExpect(jsonPath("$.historyItems[2].amount").value(31.00))
            .andExpect(jsonPath("$.historyItems[3].type").value("Note"))
            .andExpect(jsonPath("$.historyItems[3].details.noteText").value("Same timestamp PO-2642 note"))
            .andExpect(jsonPath("$.historyItems[3].details.attributeName").doesNotExist())
            .andExpect(jsonPath("$.historyItems[3].details.transactionType").doesNotExist())
            .andExpect(jsonPath("$.historyItems[3].amount").value(nullValue()));
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 common response returns 401 when the user is unauthorised")
    void getMinorCreditorHistory_whenUnauthorised_returns401() throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).checkForAuthorisedUser(any());

        getHistory()
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("Unauthorized"));
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 common response returns 403 when the user lacks permission")
    void getMinorCreditorHistory_whenUserLacksPermission_returns403() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionsUser());

        getHistory()
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 common response returns 404 when the account is missing")
    void getMinorCreditorHistory_whenAccountMissing_returns404() throws Exception {
        getHistory(MISSING_CREDITOR_ACCOUNT_ID)
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 common response returns 404 when the account is not a minor creditor")
    void getMinorCreditorHistory_whenAccountIsNotMinorCreditor_returns404() throws Exception {
        getHistory(NON_MINOR_CREDITOR_ACCOUNT_ID)
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 common response returns 408 for a timeout")
    void getMinorCreditorHistory_whenTimeoutOccurs_returns408() throws Exception {
        doThrow(new QueryTimeoutException("timeout"))
            .when(userStateService).checkForAuthorisedUser(any());

        getHistory()
            .andExpect(status().isRequestTimeout())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(true));
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 common response returns 503 for a database connectivity failure")
    void getMinorCreditorHistory_whenDatabaseUnavailable_returns503() throws Exception {
        doThrow(new DataAccessResourceFailureException("db unavailable"))
            .when(userStateService).checkForAuthorisedUser(any());

        getHistory()
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(true));
    }

    @Test
    @JiraStory("PO-2642")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2642 common response returns 500 for an unexpected failure")
    void getMinorCreditorHistory_whenUnexpectedFailureOccurs_returns500() throws Exception {
        doThrow(new ResponseStatusException(INTERNAL_SERVER_ERROR, "Boom"))
            .when(userStateService).checkForAuthorisedUser(any());

        getHistory()
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    private ResultActions getHistory(String... queryParams) throws Exception {
        return getHistory(MINOR_CREDITOR_ACCOUNT_ID, queryParams);
    }

    private ResultActions getHistory(long accountId, String... queryParams) throws Exception {
        MockHttpServletRequestBuilder request = get(HISTORY_URL, accountId)
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
            .accept(MediaType.APPLICATION_JSON);

        for (int index = 0; index < queryParams.length; index += 2) {
            request.queryParam(queryParams[index], queryParams[index + 1]);
        }

        return mockMvc.perform(request);
    }

    private void expectSuccessfulHistoryResponse(ResultActions result, int itemCount) throws Exception {
        result.andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ETAG, "\"4\""))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.historyItems.length()").value(itemCount));
    }

    private void expectAmendment(
        ResultActions result,
        int index,
        String postedDate,
        String oldValue,
        String newValue) throws Exception {

        String itemPath = "$.historyItems[" + index + "]";
        result.andExpect(jsonPath(itemPath + ".type").value("Amendment"))
            .andExpect(jsonPath(itemPath + ".postedDetails.posted_date").value(postedDate))
            .andExpect(jsonPath(itemPath + ".details.attributeName").value("Hold Pay Out"))
            .andExpect(jsonPath(itemPath + ".details.oldValue").value(oldValue))
            .andExpect(jsonPath(itemPath + ".details.newValue").value(newValue));
    }

    private void expectFinancial(
        ResultActions result,
        int index,
        String postedDate,
        String paymentReference,
        double amount) throws Exception {

        String itemPath = "$.historyItems[" + index + "]";
        result.andExpect(jsonPath(itemPath + ".type").value("Financial"))
            .andExpect(jsonPath(itemPath + ".postedDetails.posted_date").value(postedDate))
            .andExpect(jsonPath(itemPath + ".amount").value(amount))
            .andExpect(jsonPath(itemPath + ".details.transactionType.transactionType").value("PAYMNT"))
            .andExpect(jsonPath(itemPath + ".details.paymentReference").value(paymentReference))
            .andExpect(jsonPath(itemPath + ".details.status.creditorTransactionStatus").value("C"))
            .andExpect(jsonPath(itemPath + ".details.accountNumber").value("P264MN01"))
            .andExpect(jsonPath(itemPath + ".details.defendantAccountNumber").value("P264DEF1"))
            .andExpect(jsonPath(itemPath + ".details.defendantAccountId").value(DEFENDANT_ACCOUNT_ID));
    }

    private void expectNote(ResultActions result, int index, String postedDate, String noteText) throws Exception {
        String itemPath = "$.historyItems[" + index + "]";
        result.andExpect(jsonPath(itemPath + ".type").value("Note"))
            .andExpect(jsonPath(itemPath + ".postedDetails.posted_date").value(postedDate))
            .andExpect(jsonPath(itemPath + ".details.noteText").value(noteText));
    }
}
