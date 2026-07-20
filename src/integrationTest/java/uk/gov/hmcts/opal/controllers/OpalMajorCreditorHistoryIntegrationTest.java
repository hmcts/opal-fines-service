package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;

import org.junit.jupiter.api.BeforeEach;
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
    private static final short OTHER_BUSINESS_UNIT_ID = 10;
    private static final long MAJOR_CREDITOR_ACCOUNT_ID = 99264300000001L;
    private static final String URL = "/major-creditor-accounts/{id}/history";

    @BeforeEach
    void setUpAuthorisedUser() {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions(BUSINESS_UNIT_ID, SEARCH_AND_VIEW_ACCOUNTS);
    }

    @Test
    @DisplayName("PO-2654 INT.01 returns all ordered major creditor transactions including duplicate actions")
    @JiraStory("PO-2654")
    @JiraEpic("PO-2655")
    void getHistory_returnsOrderedFinancialHistoryAndEtag() throws Exception {
        ResultActions result = getHistory();

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"4\""))
            .andExpect(jsonPath("$.historyItems", hasSize(4)))
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
            .andExpect(jsonPath("$.historyItems[1].postedDetails.posted_date").value("2026-01-31"))
            .andExpect(jsonPath("$.historyItems[1].details.transactionType.transactionType").value("MADJ"))
            .andExpect(jsonPath("$.historyItems[1].details.paymentReference").value("MJF004"))
            .andExpect(jsonPath("$.historyItems[2].postedDetails.posted_date").value("2026-01-25"))
            .andExpect(jsonPath("$.historyItems[2].details.paymentReference").value("MJF002"))
            .andExpect(jsonPath("$.historyItems[3].postedDetails.posted_date").value("2026-01-05"))
            .andExpect(jsonPath("$.historyItems[3].details.paymentReference").value("MJF001"));
    }

    @Test
    @DisplayName("PO-2654 INT.05 applies dateFrom inclusively to all supported history items")
    @JiraStory("PO-2654")
    @JiraEpic("PO-2655")
    void getHistory_appliesDateFromInclusively() throws Exception {
        ResultActions result = getHistory("dateFrom", "2026-01-25");

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"4\""))
            .andExpect(jsonPath("$.historyItems", hasSize(3)))
            .andExpect(jsonPath("$.historyItems[0].details.paymentReference").value("MJF003"))
            .andExpect(jsonPath("$.historyItems[1].details.paymentReference").value("MJF004"))
            .andExpect(jsonPath("$.historyItems[2].details.paymentReference").value("MJF002"));
    }

    @Test
    @DisplayName("PO-2654 INT.06 applies dateTo inclusively to all supported history items")
    @JiraStory("PO-2654")
    @JiraEpic("PO-2655")
    void getHistory_appliesDateToInclusively() throws Exception {
        ResultActions result = getHistory("dateTo", "2026-01-25");

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"4\""))
            .andExpect(jsonPath("$.historyItems", hasSize(2)))
            .andExpect(jsonPath("$.historyItems[0].details.paymentReference").value("MJF002"))
            .andExpect(jsonPath("$.historyItems[1].details.paymentReference").value("MJF001"));
    }

    @Test
    @DisplayName("GET major creditor history combines the financial item type and date range filters")
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
            .andExpect(jsonPath("$.historyItems", hasSize(3)))
            .andExpect(jsonPath("$.historyItems[0].details.paymentReference").value("MJF003"))
            .andExpect(jsonPath("$.historyItems[1].details.paymentReference").value("MJF004"))
            .andExpect(jsonPath("$.historyItems[2].details.paymentReference").value("MJF002"));
    }

    @Test
    @DisplayName("PO-2654 INT.10 requires Search and View Accounts permission in at least one business unit")
    @JiraStory("PO-2654")
    @JiraEpic("PO-2655")
    void getHistory_enforcesPermissionAcrossBusinessUnits() throws Exception {
        userStateStub.setupWithNoPermissions();

        getHistory()
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        userStateStub.addPermissions(OTHER_BUSINESS_UNIT_ID, SEARCH_AND_VIEW_ACCOUNTS);

        getHistory()
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems", hasSize(4)))
            .andExpect(jsonPath("$.historyItems[0].details.paymentReference").value("MJF003"));
    }

    @Test
    @DisplayName("PO-2654 INT.11 returns only fields documented for major creditor financial history")
    @JiraStory("PO-2654")
    @JiraEpic("PO-2655")
    void getHistory_returnsOnlyDocumentedFields() throws Exception {
        getHistory().andExpect(status().isOk())
            .andExpect(jsonPath("$", allOf(aMapWithSize(1), hasKey("historyItems"))))
            .andExpect(jsonPath("$.historyItems", hasSize(4)))
            .andExpect(jsonPath("$.historyItems[*]", everyItem(allOf(
                aMapWithSize(4), hasKey("postedDetails"), hasKey("type"), hasKey("details"), hasKey("amount")
            ))))
            .andExpect(jsonPath("$.historyItems[*].postedDetails", everyItem(allOf(
                aMapWithSize(3), hasKey("posted_date"), hasKey("posted_by"), hasKey("posted_by_name")
            ))))
            .andExpect(jsonPath("$.historyItems[*].details", everyItem(allOf(
                aMapWithSize(9),
                hasKey("transactionType"),
                hasKey("paymentReference"),
                hasKey("status"),
                hasKey("statusDate"),
                hasKey("associatedRecordType"),
                hasKey("associatedRecordId"),
                hasKey("accountNumber"),
                hasKey("defendantAccountNumber"),
                hasKey("defendantAccountId")
            ))))
            .andExpect(jsonPath("$.historyItems[*].details.transactionType", everyItem(allOf(
                aMapWithSize(2), hasKey("transactionType"), hasKey("transactionTypeDisplayName")
            ))))
            .andExpect(jsonPath("$.historyItems[*].details.status", everyItem(allOf(
                aMapWithSize(2), hasKey("creditorTransactionStatus"),
                hasKey("creditorTransactionStatusDisplayName")
            ))));
    }

    @Test
    @DisplayName("PO-2654 INT.12 repeated GETs return identical content and deterministic tie ordering")
    @JiraStory("PO-2654")
    @JiraEpic("PO-2655")
    void getHistory_isDeterministicForStableData() throws Exception {
        String firstResponse = getHistory()
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems[*].details.paymentReference")
                .value(contains("MJF003", "MJF004", "MJF002", "MJF001")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        getHistory()
            .andExpect(status().isOk())
            .andExpect(content().json(firstResponse));
    }

    private ResultActions getHistory(String... queryParams) throws Exception {
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
