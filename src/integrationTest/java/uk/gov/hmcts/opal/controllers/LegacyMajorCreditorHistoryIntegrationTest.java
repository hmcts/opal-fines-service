package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;
import static uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountService.GET_MAJOR_CREDITOR_ACCOUNT_HISTORY;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse.LegacyCreditorTransactionStatusReference;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse.LegacyCreditorTransactionTypeReference;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse.LegacyMajorCreditorHistoryDetails;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse.LegacyMajorCreditorHistoryItem;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

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

    @MockitoBean
    private GatewayService gatewayService;

    @BeforeEach
    void setUp() {
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(UserStateUtil.permissionUser((short) 77, SEARCH_AND_VIEW_ACCOUNTS));
    }

    @Test
    @DisplayName("PO-2659 INT.01 returns transaction history for major creditor from legacy")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    void getHistory_returnsLegacyTransactions() throws Exception {
        stubLegacyResponse();

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
            .andExpect(jsonPath("$.historyItems[0].details.statusDate").value("2026-01-31T10:30:00"))
            .andExpect(jsonPath("$.historyItems[0].details.associatedRecordType").value("creditor_accounts"))
            .andExpect(jsonPath("$.historyItems[0].details.associatedRecordId").value("99264300000001"))
            .andExpect(jsonPath("$.historyItems[0].details.accountNumber").value("87654321"))
            .andExpect(jsonPath("$.historyItems[0].details.defendantAccountNumber").value("12345678"))
            .andExpect(jsonPath("$.historyItems[0].details.defendantAccountId").value(99000000000001L))
            .andExpect(jsonPath("$.historyItems[1].details.transactionType.transactionType").value("MADJ"));

        GetMajorCreditorAccountHistoryLegacyRequest request = captureLegacyRequest();
        assertThat(request.getCreditorAccountId()).isEqualTo(String.valueOf(MAJOR_CREDITOR_ACCOUNT_ID));
        assertThat(request.getFromDate()).isNull();
        assertThat(request.getToDate()).isNull();
        assertThat(request.getItemTypes()).isNull();
    }

    @Test
    @DisplayName("PO-2659 INT.05 and INT.06 forwards inclusive date filters to legacy")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    void getHistory_forwardsDateFiltersAndItemTypes() throws Exception {
        stubLegacyResponse();

        getHistory("dateFrom", "2026-01-25", "dateTo", "2026-01-31", "itemTypes", "note")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems", hasSize(3)));

        GetMajorCreditorAccountHistoryLegacyRequest request = captureLegacyRequest();
        assertThat(request.getFromDate()).hasToString("2026-01-25");
        assertThat(request.getToDate()).hasToString("2026-01-31");
        assertThat(request.getItemTypes()).containsExactly("Note");
    }

    @Test
    @DisplayName("PO-2659 returns 400 when dateFrom is after dateTo")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    void getHistory_whenDateRangeInvalidReturns400BeforeLegacyCall() throws Exception {
        getHistory("dateFrom", "2026-01-31", "dateTo", "2026-01-25")
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(gatewayService);
    }

    @Test
    @DisplayName("PO-2659 INT.10 requires Search and View Accounts permission in at least one business unit")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    void getHistory_enforcesPermission() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(UserStateUtil.noPermissionsUser());

        getHistory()
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(gatewayService);
    }

    @Test
    @DisplayName("PO-2659 INT.11 returns only documented fields for legacy major creditor history")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    void getHistory_returnsOnlyDocumentedFields() throws Exception {
        stubLegacyResponse();

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
    void getHistory_whenLegacyGatewayReturnsNotFoundReturns404() throws Exception {
        stubGatewayException(HttpClientErrorException.create(
            HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        getHistory()
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("PO-2659 returns 408 when legacy gateway times out")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    void getHistory_whenLegacyGatewayTimesOutReturns408() throws Exception {
        stubGatewayException(HttpClientErrorException.create(
            HttpStatusCode.valueOf(408), "Request Timeout", HttpHeaders.EMPTY, null, null));

        getHistory()
            .andExpect(status().isRequestTimeout())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("PO-2659 returns 503 when legacy gateway is unavailable")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    void getHistory_whenLegacyGatewayUnavailableReturns503() throws Exception {
        stubGatewayException(HttpServerErrorException.create(
            HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", HttpHeaders.EMPTY, null, null));

        getHistory()
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("PO-2659 returns 500 when legacy gateway returns server error")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    void getHistory_whenLegacyGatewayReturnsServerErrorReturns500() throws Exception {
        stubGatewayException(HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpHeaders.EMPTY, null, null));

        getHistory()
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("PO-2659 INT.12 repeated legacy GETs return deterministic content")
    @JiraStory("PO-2659")
    @JiraEpic("PO-2655")
    void getHistory_isDeterministicForStableLegacyData() throws Exception {
        stubLegacyResponse();

        String firstResponse = getHistory()
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        getHistory()
            .andExpect(status().isOk())
            .andExpect(content().json(firstResponse));
    }

    private void stubLegacyResponse() {
        when(gatewayService.postToGateway(
            eq(GET_MAJOR_CREDITOR_ACCOUNT_HISTORY),
            eq(GetMajorCreditorAccountHistoryLegacyResponse.class),
            any(),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse(), null, null));
    }

    private void stubGatewayException(RuntimeException exception) {
        when(gatewayService.postToGateway(
            eq(GET_MAJOR_CREDITOR_ACCOUNT_HISTORY),
            eq(GetMajorCreditorAccountHistoryLegacyResponse.class),
            any(),
            isNull()
        )).thenThrow(exception);
    }

    private GetMajorCreditorAccountHistoryLegacyRequest captureLegacyRequest() {
        ArgumentCaptor<GetMajorCreditorAccountHistoryLegacyRequest> requestCaptor =
            ArgumentCaptor.forClass(GetMajorCreditorAccountHistoryLegacyRequest.class);
        verify(gatewayService).postToGateway(
            eq(GET_MAJOR_CREDITOR_ACCOUNT_HISTORY),
            eq(GetMajorCreditorAccountHistoryLegacyResponse.class),
            requestCaptor.capture(),
            isNull()
        );
        return requestCaptor.getValue();
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

    private GetMajorCreditorAccountHistoryLegacyResponse legacyResponse() {
        return GetMajorCreditorAccountHistoryLegacyResponse.builder()
            .version(7L)
            .historyItems(List.of(
                historyItem("MJUSR2", "Major User Two", "MJF002", "PAYMNT", "Payment",
                            LocalDateTime.of(2026, 1, 25, 9, 15), new BigDecimal("-25.50")),
                historyItem("MJUSR4", "Major User Four", "MJF004", "MADJ", "Manual Adjustment",
                            LocalDateTime.of(2026, 1, 31, 10, 30), new BigDecimal("31.00")),
                historyItem("MJUSR3", "Major User Three", "MJF003", "MADJ", "Manual Adjustment",
                            LocalDateTime.of(2026, 1, 31, 10, 30), new BigDecimal("-31.00"))
            ))
            .build();
    }

    private LegacyMajorCreditorHistoryItem historyItem(
        String postedBy,
        String postedByName,
        String paymentReference,
        String transactionType,
        String transactionTypeDisplayName,
        LocalDateTime postedDate,
        BigDecimal amount
    ) {
        return LegacyMajorCreditorHistoryItem.builder()
            .postedDetails(new LegacyPostedDetails(postedDate, postedBy, postedByName))
            .type("Financial")
            .amount(amount)
            .details(LegacyMajorCreditorHistoryDetails.builder()
                .transactionType(LegacyCreditorTransactionTypeReference.builder()
                    .transactionType(transactionType)
                    .transactionTypeDisplayName(transactionTypeDisplayName)
                    .build())
                .paymentReference(paymentReference)
                .status(LegacyCreditorTransactionStatusReference.builder()
                    .creditorTransactionStatus("R")
                    .creditorTransactionStatusDisplayName("Reversed")
                    .build())
                .statusDate(postedDate)
                .associatedRecordType("creditor_accounts")
                .associatedRecordId(String.valueOf(MAJOR_CREDITOR_ACCOUNT_ID))
                .accountNumber("87654321")
                .defendantAccountNumber("12345678")
                .defendantAccountId(99000000000001L)
                .build())
            .build();
    }
}
