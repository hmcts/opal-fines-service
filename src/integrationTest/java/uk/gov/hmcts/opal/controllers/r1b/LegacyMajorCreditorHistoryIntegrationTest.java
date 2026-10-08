package uk.gov.hmcts.opal.controllers.r1b;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;
import static uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountService.GET_MAJOR_CREDITOR_ACCOUNT_HISTORY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.controllers.shared.util.UserStateUtil;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@DisplayName("Major Creditor History Legacy Integration Tests")
class LegacyMajorCreditorHistoryIntegrationTest extends AbstractIntegrationTest {

    private static final String AUTH_HEADER = "Bearer test-token";
    private static final String URL = "/major-creditor-accounts/{id}/history";
    private static final long MAJOR_CREDITOR_ACCOUNT_ID = 99264300000001L;

    @MockitoBean
    private UserStateService userStateService;

    @BeforeEach
    void setUp() {
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(UserStateUtil.permissionUser((short) 77, SEARCH_AND_VIEW_ACCOUNTS));
    }

    @Test
    @DisplayName("PO-2659 INT.01 returns transaction history for major creditor from legacy")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    @JiraTestKey("PO-10093")
    void getHistory_returnsLegacyTransactions() throws Exception {
        ResultActions result = getHistory();

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"7\""))
            .andExpect(jsonPath("$.historyItems", hasSize(3)))
            .andExpect(jsonPath("$.historyItems[0].postedDetails.posted_date").value("2026-01-31"))
            .andExpect(jsonPath("$.historyItems[0].postedDetails.posted_by").value("MJUSR3"))
            .andExpect(jsonPath("$.historyItems[0].postedDetails.posted_by_name").value("Major User Three"))
            .andExpect(jsonPath("$.historyItems[0].type").value("Financial"))
            .andExpect(jsonPath("$.historyItems[0].amount").value(-31.00))
            .andExpect(jsonPath("$.historyItems[0].details.transactionType.transactionType").value("MADJ"))
            .andExpect(jsonPath("$.historyItems[0].details.transactionType.transactionTypeDisplayName")
                .value("Manual Adjustment"))
            .andExpect(jsonPath("$.historyItems[0].details.paymentReference").value("MJF003"))
            .andExpect(jsonPath("$.historyItems[0].details.status.creditorTransactionStatus").value("R"))
            .andExpect(jsonPath("$.historyItems[0].details.status.creditorTransactionStatusDisplayName")
                .value("Reversed"))
            .andExpect(jsonPath("$.historyItems[0].details.statusDate").value("2026-01-31T00:00:00"))
            .andExpect(jsonPath("$.historyItems[0].details.associatedRecordType").value("creditor_accounts"))
            .andExpect(jsonPath("$.historyItems[0].details.associatedRecordId").value("99264300000001"))
            .andExpect(jsonPath("$.historyItems[0].details.accountNumber").value("87654321"))
            .andExpect(jsonPath("$.historyItems[0].details.defendantAccountNumber").value("12345678"))
            .andExpect(jsonPath("$.historyItems[0].details.defendantAccountId").value(99000000000001L))
            .andExpect(jsonPath("$.historyItems[1].details.transactionType.transactionType").value("MADJ"));

        verifyLegacyRequest(null, null);
    }

    @Test
    @DisplayName("PO-2659 INT.05 and INT.06 forwards inclusive date filters to legacy")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    @JiraTestKey("PO-10090")
    void getHistory_forwardsDateFiltersAndForcesFinancialItemType() throws Exception {
        getHistory("dateFrom", "2026-01-25", "dateTo", "2026-01-31", "itemTypes", "note")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems", hasSize(3)));

        verifyLegacyRequest("2026-01-25", "2026-01-31");
    }

    @Test
    @DisplayName("PO-2659 returns 400 when dateFrom is after dateTo")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    @JiraTestKey("PO-10094")
    void getHistory_whenDateRangeInvalidReturns400BeforeLegacyCall() throws Exception {
        getHistory("dateFrom", "2026-01-31", "dateTo", "2026-01-25")
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoLegacyRequest();
    }

    @Test
    @DisplayName("PO-2659 INT.10 requires Search and View Accounts permission in at least one business unit")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    @JiraTestKey("PO-10089")
    void getHistory_enforcesPermission() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(UserStateUtil.noPermissionsUser());

        getHistory()
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoLegacyRequest();
    }

    @Test
    @DisplayName("PO-2659 INT.11 returns only documented fields for legacy major creditor history")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    @JiraTestKey("PO-10092")
    void getHistory_returnsOnlyDocumentedFields() throws Exception {
        getHistory().andExpect(status().isOk())
            .andExpect(jsonPath("$", allOf(aMapWithSize(1), hasKey("historyItems"))))
            .andExpect(jsonPath("$.historyItems", hasSize(3)))
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
            ))));
    }

    @Test
    @DisplayName("PO-2659 returns 404 when legacy gateway returns not found")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    @JiraTestKey("PO-10087")
    void getHistory_whenLegacyGatewayReturnsNotFoundReturns404() throws Exception {
        stubLegacyError(404, "Not Found");

        getHistory()
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("PO-2659 returns 408 when legacy gateway times out")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    @JiraTestKey("PO-10085")
    void getHistory_whenLegacyGatewayTimesOutReturns408() throws Exception {
        stubLegacyError(408, "Request Timeout");

        getHistory()
            .andExpect(status().isRequestTimeout())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("PO-2659 returns 500 when legacy gateway is unavailable")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    @JiraTestKey("PO-10091")
    void getHistory_whenLegacyGatewayUnavailableReturns500() throws Exception {
        stubLegacyError(503, "Service Unavailable");

        getHistory()
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("PO-2659 returns 500 when legacy gateway returns server error")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    @JiraTestKey("PO-10086")
    void getHistory_whenLegacyGatewayReturnsServerErrorReturns500() throws Exception {
        stubLegacyError(500, "Internal Server Error");

        getHistory()
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("PO-2659 INT.12 repeated legacy GETs return deterministic content")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    @JiraTestKey("PO-10088")
    void getHistory_isDeterministicForStableLegacyData() throws Exception {
        String firstResponse = getHistory()
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        getHistory()
            .andExpect(status().isOk())
            .andExpect(content().json(firstResponse));
    }

    private void stubLegacyError(int status, String message) {
        stubFor(post(urlPathEqualTo("/opal"))
            .atPriority(1)
            .withQueryParam("actionType", equalTo(GET_MAJOR_CREDITOR_ACCOUNT_HISTORY))
            .withRequestBody(matchingJsonPath(
                "$.creditor_account_id", equalTo(String.valueOf(MAJOR_CREDITOR_ACCOUNT_ID))))
            .willReturn(aResponse()
                .withStatus(status)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .withBody("<error><message>" + message + "</message></error>")));
    }

    private void verifyLegacyRequest(String expectedFromDate, String expectedToDate) {
        var request = postRequestedFor(urlPathEqualTo("/opal"))
            .withQueryParam("actionType", equalTo(GET_MAJOR_CREDITOR_ACCOUNT_HISTORY))
            .withRequestBody(matchingJsonPath(
                "$.creditor_account_id", equalTo(String.valueOf(MAJOR_CREDITOR_ACCOUNT_ID))))
            .withRequestBody(matchingJsonPath("$.item_types[0]", equalTo("Financial")));

        if (expectedFromDate == null) {
            request.withRequestBody(matchingJsonPath("$.from_date", absent()));
        } else {
            request.withRequestBody(matchingJsonPath("$.from_date", equalTo(expectedFromDate)));
        }

        if (expectedToDate == null) {
            request.withRequestBody(matchingJsonPath("$.to_date", absent()));
        } else {
            request.withRequestBody(matchingJsonPath("$.to_date", equalTo(expectedToDate)));
        }

        verify(1, request);
    }

    private void verifyNoLegacyRequest() {
        verify(0, postRequestedFor(urlPathEqualTo("/opal"))
            .withQueryParam("actionType", equalTo(GET_MAJOR_CREDITOR_ACCOUNT_HISTORY)));
    }

    private ResultActions getHistory(String... queryParams) throws Exception {
        var request = get(URL, MAJOR_CREDITOR_ACCOUNT_ID)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER);

        for (int index = 0; index < queryParams.length; index += 2) {
            request.queryParam(queryParams[index], queryParams[index + 1]);
        }

        return mockMvc.perform(request);
    }
}
