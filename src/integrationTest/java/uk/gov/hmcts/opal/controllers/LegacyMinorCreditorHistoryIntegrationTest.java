package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noPermissionsUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse.LegacyCreditorTransactionStatusReference;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse.LegacyCreditorTransactionTypeReference;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse.LegacyMinorCreditorAccountHistoryItem;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse.LegacyMinorCreditorHistoryDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse.LegacyMinorCreditorHistoryPostedDetails;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@DisplayName("Legacy Minor Creditor History Integration Tests")
@Slf4j(topic = "opal.LegacyMinorCreditorHistoryIntegrationTest")
class LegacyMinorCreditorHistoryIntegrationTest extends AbstractIntegrationTest {

    private static final String HISTORY_URL = "/minor-creditor-accounts/{accountId}/history";
    private static final String LEGACY_ACTION = "LIBRA.get_minor_creditor_account_history";
    private static final String AUTH_HEADER = "Bearer test-token";
    private static final long MINOR_CREDITOR_ACCOUNT_ID = 99264500000001L;
    private static final short BUSINESS_UNIT_ID = 32645;

    @MockitoBean
    private UserStateService userStateService;

    @MockitoBean
    private GatewayService gatewayService;

    @BeforeEach
    void setUpAuthorisedUser() {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(permissionUser(
            BUSINESS_UNIT_ID,
            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS
        ));
    }

    @Test
    @JiraStory("PO-2645")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2645 legacy history returns mapped amendment note and financial items")
    @JiraTestKey("PO-8690")
    void getMinorCreditorHistory_whenLegacyMode_returnsMappedHistoryItems() throws Exception {
        mockLegacyResponse(legacyResponse());

        ResultActions result = getHistory(MINOR_CREDITOR_ACCOUNT_ID);

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.historyItems", hasSize(5)))
            .andExpect(jsonPath("$.historyItems[0].type").value("Amendment"))
            .andExpect(jsonPath("$.historyItems[0].postedDetails.posted_date").value("2026-04-01"))
            .andExpect(jsonPath("$.historyItems[0].details.attributeName").value("BACS Account Number"))
            .andExpect(jsonPath("$.historyItems[0].details.oldValue").value("******78"))
            .andExpect(jsonPath("$.historyItems[0].details.newValue").value("******21"))
            .andExpect(jsonPath("$.historyItems[1].type").value("Amendment"))
            .andExpect(jsonPath("$.historyItems[1].details.attributeName").value("BACS Account Number"))
            .andExpect(jsonPath("$.historyItems[1].details.oldValue").value("******22"))
            .andExpect(jsonPath("$.historyItems[1].details.newValue").value("******44"))
            .andExpect(jsonPath("$.historyItems[2].type").value("Financial"))
            .andExpect(jsonPath("$.historyItems[2].amount").value(10.00))
            .andExpect(jsonPath("$.historyItems[2].details.transactionType.transactionType").value("PAYMNT"))
            .andExpect(jsonPath("$.historyItems[2].details.paymentReference").value("PAY-1"))
            .andExpect(jsonPath("$.historyItems[2].details.status.creditorTransactionStatus").value("C"))
            .andExpect(jsonPath("$.historyItems[3].type").value("Financial"))
            .andExpect(jsonPath("$.historyItems[3].details.paymentReference").value("PAY-2"))
            .andExpect(jsonPath("$.historyItems[4].type").value("Note"))
            .andExpect(jsonPath("$.historyItems[4].details.noteText").value("Legacy same-day note"));

        ArgumentCaptor<LegacyGetMinorCreditorAccountHistoryRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyGetMinorCreditorAccountHistoryRequest.class);
        verify(gatewayService).postToGateway(
            eq(LEGACY_ACTION),
            eq(LegacyGetMinorCreditorAccountHistoryResponse.class),
            requestCaptor.capture(),
            isNull()
        );
        assertThat(requestCaptor.getValue().getCreditorAccountId()).isEqualTo("99264500000001");
        assertThat(requestCaptor.getValue().getFromDate()).isNull();
        assertThat(requestCaptor.getValue().getToDate()).isNull();
        assertThat(requestCaptor.getValue().getItemTypes()).isNull();
    }

    @Test
    @JiraStory("PO-2645")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2645 legacy history forwards date and item type filters")
    @JiraTestKey("PO-8691")
    void getMinorCreditorHistory_whenFiltersProvided_passesFiltersToLegacyGateway() throws Exception {
        mockLegacyResponse(filteredLegacyResponse());

        getHistory(
            MINOR_CREDITOR_ACCOUNT_ID,
            "dateFrom", "2026-04-01",
            "dateTo", "2026-04-30",
            "itemTypes", "note,financial"
        ).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.historyItems", hasSize(2)))
            .andExpect(jsonPath("$.historyItems[0].type").value("Financial"))
            .andExpect(jsonPath("$.historyItems[1].type").value("Note"));

        ArgumentCaptor<LegacyGetMinorCreditorAccountHistoryRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyGetMinorCreditorAccountHistoryRequest.class);
        verify(gatewayService).postToGateway(
            eq(LEGACY_ACTION),
            eq(LegacyGetMinorCreditorAccountHistoryResponse.class),
            requestCaptor.capture(),
            isNull()
        );
        assertThat(requestCaptor.getValue().getCreditorAccountId()).isEqualTo("99264500000001");
        assertThat(requestCaptor.getValue().getFromDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(requestCaptor.getValue().getToDate()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(requestCaptor.getValue().getItemTypes()).containsExactly("Financial", "Note");
    }

    @Test
    @JiraStory("PO-2645")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2645 legacy history repeated requests return deterministic content")
    @JiraTestKey("PO-8692")
    void getMinorCreditorHistory_whenRepeated_returnsSameContent() throws Exception {
        mockLegacyResponse(legacyResponse());

        MvcResult first = getHistory(MINOR_CREDITOR_ACCOUNT_ID)
            .andExpect(status().isOk())
            .andReturn();
        MvcResult second = getHistory(MINOR_CREDITOR_ACCOUNT_ID)
            .andExpect(status().isOk())
            .andReturn();

        assertThat(second.getResponse().getContentAsString())
            .isEqualTo(first.getResponse().getContentAsString());
    }

    @Test
    @JiraStory("PO-2645")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2645 legacy history without permission returns 403")
    @JiraTestKey("PO-8693")
    void getMinorCreditorHistory_whenUserLacksPermission_returnsForbidden() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(noPermissionsUser());

        getHistory(MINOR_CREDITOR_ACCOUNT_ID)
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(gatewayService);
    }

    @Test
    @JiraStory("PO-2645")
    @JiraEpic("PO-2653")
    @DisplayName("PO-2645 legacy history gateway failure returns problem response")
    @JiraTestKey("PO-8694")
    void getMinorCreditorHistory_whenGatewayFails_returnsProblemResponse() throws Exception {
        when(gatewayService.postToGateway(
            eq(LEGACY_ACTION),
            eq(LegacyGetMinorCreditorAccountHistoryResponse.class),
            any(LegacyGetMinorCreditorAccountHistoryRequest.class),
            isNull()
        )).thenThrow(HttpServerErrorException.create(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            HttpHeaders.EMPTY,
            null,
            null
        ));

        getHistory(MINOR_CREDITOR_ACCOUNT_ID)
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    private ResultActions getHistory(long accountId, String... queryParams) throws Exception {
        var request = get(HISTORY_URL, accountId)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER);

        for (int index = 0; index < queryParams.length; index += 2) {
            request.queryParam(queryParams[index], queryParams[index + 1]);
        }

        return mockMvc.perform(request);
    }

    private void mockLegacyResponse(LegacyGetMinorCreditorAccountHistoryResponse legacyResponse) {
        when(gatewayService.postToGateway(
            eq(LEGACY_ACTION),
            eq(LegacyGetMinorCreditorAccountHistoryResponse.class),
            any(LegacyGetMinorCreditorAccountHistoryRequest.class),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null));
    }

    private LegacyGetMinorCreditorAccountHistoryResponse legacyResponse() {
        LocalDate postedDate = LocalDate.of(2026, 4, 1);

        return LegacyGetMinorCreditorAccountHistoryResponse.builder()
            .historyItems(List.of(
                financialItem(postedDate, "PAY-1", BigDecimal.valueOf(10L)),
                amendmentItem(postedDate, "12345678", "87654321"),
                financialItem(postedDate, "PAY-2", BigDecimal.valueOf(20L)),
                amendmentItem(postedDate, "11112222", "33334444"),
                noteItem(postedDate, "Legacy same-day note")
            ))
            .build();
    }

    private LegacyGetMinorCreditorAccountHistoryResponse filteredLegacyResponse() {
        LocalDate postedDate = LocalDate.of(2026, 4, 15);

        return LegacyGetMinorCreditorAccountHistoryResponse.builder()
            .historyItems(List.of(
                noteItem(postedDate, "Filtered legacy note"),
                financialItem(postedDate, "PAY-FILTERED", BigDecimal.valueOf(15L))
            ))
            .build();
    }

    private LegacyMinorCreditorAccountHistoryItem amendmentItem(
        LocalDate postedDate,
        String oldValue,
        String newValue) {

        return LegacyMinorCreditorAccountHistoryItem.builder()
            .postedDetails(postedDetails(postedDate))
            .type("Amendment")
            .details(LegacyMinorCreditorHistoryDetails.builder()
                .attributeName("BACS Account Number")
                .oldValue(oldValue)
                .newValue(newValue)
                .build())
            .build();
    }

    private LegacyMinorCreditorAccountHistoryItem noteItem(LocalDate postedDate, String noteText) {
        return LegacyMinorCreditorAccountHistoryItem.builder()
            .postedDetails(postedDetails(postedDate))
            .type("Note")
            .details(LegacyMinorCreditorHistoryDetails.builder()
                .noteText(noteText)
                .build())
            .build();
    }

    private LegacyMinorCreditorAccountHistoryItem financialItem(
        LocalDate postedDate,
        String paymentReference,
        BigDecimal amount) {

        return LegacyMinorCreditorAccountHistoryItem.builder()
            .postedDetails(postedDetails(postedDate))
            .type("Financial")
            .details(LegacyMinorCreditorHistoryDetails.builder()
                .transactionType(LegacyCreditorTransactionTypeReference.builder()
                    .transactionType("PAYMNT")
                    .transactionTypeDisplayName("Payment")
                    .build())
                .paymentReference(paymentReference)
                .status(LegacyCreditorTransactionStatusReference.builder()
                    .creditorTransactionStatus("C")
                    .creditorTransactionStatusDisplayName("Completed")
                    .build())
                .statusDate(LocalDateTime.of(2026, 4, 1, 9, 30))
                .associatedRecordType("defendant_accounts")
                .associatedRecordId("99264500001001")
                .accountNumber("MC2645")
                .defendantAccountNumber("DEF2645")
                .defendantAccountId(99264500001001L)
                .build())
            .amount(amount)
            .build();
    }

    private LegacyMinorCreditorHistoryPostedDetails postedDetails(LocalDate postedDate) {
        return LegacyMinorCreditorHistoryPostedDetails.builder()
            .postedDate(postedDate)
            .postedBy("LEGUSR")
            .postedByName("Legacy User")
            .build();
    }
}
