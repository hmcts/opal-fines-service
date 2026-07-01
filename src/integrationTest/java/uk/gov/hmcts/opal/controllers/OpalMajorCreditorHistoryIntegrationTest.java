package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@Sql(
    scripts = "classpath:db/insertData/insert_into_major_creditor_history.sql",
    executionPhase = BEFORE_TEST_CLASS
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_major_creditor_history.sql",
    executionPhase = AFTER_TEST_CLASS
)
@DisplayName("Major Creditor History Opal Integration Tests")
class OpalMajorCreditorHistoryIntegrationTest extends AbstractIntegrationTest {

    private static final short BUSINESS_UNIT_ID = 32643;
    private static final long MAJOR_CREDITOR_ACCOUNT_ID = 99264300000001L;
    private static final String URL = "/major-creditor-accounts/{id}/history";

    @Test
    @DisplayName("GET major creditor history returns ordered financial history and ETag")
    @JiraStory("PO-2654")
    @JiraEpic("PO-2655")
    void getHistory_returnsOrderedFinancialHistoryAndEtag() throws Exception {
        ResultActions result = getHistory();

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"4\""))
            .andExpect(jsonPath("$.historyItems", hasSize(3)))
            .andExpect(jsonPath("$.historyItems[0].postedDetails.posted_date").value("2026-01-31"))
            .andExpect(jsonPath("$.historyItems[0].postedDetails.posted_by").value("MJUSR3"))
            .andExpect(jsonPath("$.historyItems[0].postedDetails.posted_by_name").value("Major User Three"))
            .andExpect(jsonPath("$.historyItems[0].type").value("Financial"))
            .andExpect(jsonPath("$.historyItems[0].amount").value(-31.00))
            .andExpect(jsonPath("$.historyItems[0].details.transactionType.transactionType").value("MADJ"))
            .andExpect(jsonPath("$.historyItems[0].details.transactionType.transactionTypeDisplayName").value("MADJ"))
            .andExpect(jsonPath("$.historyItems[0].details.paymentReference").value("MJF003"))
            .andExpect(jsonPath("$.historyItems[0].details.status.creditorTransactionStatus").value("R"))
            .andExpect(jsonPath("$.historyItems[0].details.status.creditorTransactionStatusDisplayName").value("R"))
            .andExpect(jsonPath("$.historyItems[0].details.statusDate").value("2026-01-31T10:30:00"))
            .andExpect(jsonPath("$.historyItems[0].details.associatedRecordType").value("creditor_accounts"))
            .andExpect(jsonPath("$.historyItems[0].details.associatedRecordId").value("99264300000001"))
            .andExpect(jsonPath("$.historyItems[1].postedDetails.posted_date").value("2026-01-25"))
            .andExpect(jsonPath("$.historyItems[1].details.paymentReference").value("MJF002"))
            .andExpect(jsonPath("$.historyItems[2].postedDetails.posted_date").value("2026-01-05"))
            .andExpect(jsonPath("$.historyItems[2].details.paymentReference").value("MJF001"));
    }

    @Test
    @DisplayName("GET major creditor history applies financial item type and date filters")
    @JiraStory("PO-2654")
    @JiraEpic("PO-2655")
    void getHistory_appliesFinancialItemTypeAndDateFilters() throws Exception {
        ResultActions result = getHistory(
            "itemTypes", "financial",
            "dateFrom", "2026-01-20",
            "dateTo", "2026-01-31"
        );

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"4\""))
            .andExpect(jsonPath("$.historyItems", hasSize(2)))
            .andExpect(jsonPath("$.historyItems[0].details.paymentReference").value("MJF003"))
            .andExpect(jsonPath("$.historyItems[1].details.paymentReference").value("MJF002"));
    }

    private ResultActions getHistory(String... queryParams) throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(BUSINESS_UNIT_ID, SEARCH_AND_VIEW_ACCOUNTS);

        MockHttpServletRequestBuilder request = get(URL, MAJOR_CREDITOR_ACCOUNT_ID)
            .accept(MediaType.APPLICATION_JSON)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken());

        for (int index = 0; index < queryParams.length; index += 2) {
            request.queryParam(queryParams[index], queryParams[index + 1]);
        }

        return mockMvc.perform(request);
    }
}
