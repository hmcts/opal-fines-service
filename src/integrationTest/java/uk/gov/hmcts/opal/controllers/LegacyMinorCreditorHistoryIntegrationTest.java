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
import static uk.gov.hmcts.opal.controllers.shared.util.UserStateUtil.noPermissionsUser;
import static uk.gov.hmcts.opal.controllers.shared.util.UserStateUtil.permissionUser;

import jakarta.xml.bind.JAXBException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.opal.dto.legacy.MinorCreditorHistoryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.MinorCreditorHistoryLegacyResponse.LegacyCreditorTransactionStatusReference;
import uk.gov.hmcts.opal.dto.legacy.MinorCreditorHistoryLegacyResponse.LegacyCreditorTransactionTypeReference;
import uk.gov.hmcts.opal.dto.legacy.MinorCreditorHistoryLegacyResponse.LegacyMinorCreditorAccountHistoryItem;
import uk.gov.hmcts.opal.dto.legacy.MinorCreditorHistoryLegacyResponse.LegacyMinorCreditorHistoryDetails;
import uk.gov.hmcts.opal.dto.legacy.MinorCreditorHistoryLegacyResponse.LegacyMinorCreditorHistoryPostedDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.util.XmlUtil;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@DisplayName("Legacy Minor Creditor History Integration Tests")
@Tag("ExtendedTest")
@Slf4j(topic = "opal.LegacyMinorCreditorHistoryIntegrationTest")
class LegacyMinorCreditorHistoryIntegrationTest extends AbstractIntegrationTest {

    private static final String HISTORY_URL = "/minor-creditor-accounts/{accountId}/history";
    private static final String LEGACY_ACTION = "LIBRA.get_minor_creditor_account_history";
    private static final String AUTH_HEADER = "Bearer test-token";
    private static final long MINOR_CREDITOR_ACCOUNT_ID = 99264500000001L;
    private static final short BUSINESS_UNIT_ID = 32645;
    private static final String HISTORY_RESPONSE_SCHEMA = "opal/minor-creditor/getMinorCreditorHistoryResponse.json";

    @MockitoBean
    private UserStateService userStateService;

    @MockitoBean
    private GatewayService gatewayService;

    @MockitoBean
    private LoggingService loggingService;

    @Autowired
    private JsonSchemaValidationService jsonSchemaValidationService;

    @BeforeEach
    void setUpAuthorisedUser() {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(permissionUser(
            BUSINESS_UNIT_ID,
            FinesPermission.SEARCH_AND_VIEW_ACCOUNTS
        ));
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);
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
            .andExpect(jsonPath("$.historyItems[0].postedDetails.posted_date").value("2026-04-01T09:00:00"))
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
            .andExpect(jsonPath("$.historyItems[2].details.statusDate").value("2026-04-01T00:00:00"))
            .andExpect(jsonPath("$.historyItems[3].type").value("Financial"))
            .andExpect(jsonPath("$.historyItems[3].details.paymentReference").value("PAY-2"))
            .andExpect(jsonPath("$.historyItems[4].type").value("Note"))
            .andExpect(jsonPath("$.historyItems[4].details.noteText").value("Legacy same-day note"));

        jsonSchemaValidationService.validateOrError(result.andReturn().getResponse().getContentAsString(),
            HISTORY_RESPONSE_SCHEMA);

        ArgumentCaptor<LegacyGetMinorCreditorAccountHistoryRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyGetMinorCreditorAccountHistoryRequest.class);
        verify(gatewayService).postToGateway(
            eq(LEGACY_ACTION),
            eq(MinorCreditorHistoryLegacyResponse.class),
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
            eq(MinorCreditorHistoryLegacyResponse.class),
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
            eq(MinorCreditorHistoryLegacyResponse.class),
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

    @Test
    @JiraStory("PO-11461")
    @JiraEpic("PO-2653")
    @DisplayName("Legacy history preserves nested financial and note details with date filters")
    void getMinorCreditorHistory_whenDetailsNested_preservesDetails() throws Exception {
        mockLegacyResponse(nestedLegacyResponse());

        ResultActions result =
            getHistory(MINOR_CREDITOR_ACCOUNT_ID, "dateFrom", "2025-01-01", "dateTo", "2026-11-30", "itemTypes",
                "amendment,financial,note");

        result.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.historyItems", hasSize(4)))
            .andExpect(jsonPath("$.historyItems[3].type").value("Note"))
            .andExpect(jsonPath("$.historyItems[3].details.noteText").value("Nested legacy note"));

        for (int index = 0; index < 3; index++) {
            String itemPath = "$.historyItems[" + index + "]";

            result.andExpect(jsonPath(itemPath + ".type").value("Financial"))
                .andExpect(jsonPath(itemPath + ".amount").value((index + 1) * 10))
                .andExpect(jsonPath(itemPath + ".postedDetails.posted_date").value("2026-04-01T09:00:00"))
                .andExpect(jsonPath(itemPath + ".postedDetails.posted_by").value("LEGUSR"))
                .andExpect(jsonPath(itemPath + ".postedDetails.posted_by_name").value("Legacy User"))
                .andExpect(jsonPath(itemPath + ".details.transactionType.transactionType").value("PAYMNT"))
                .andExpect(jsonPath(itemPath + ".details.paymentReference").value("PAY-" + (index + 1)))
                .andExpect(jsonPath(itemPath + ".details.status.creditorTransactionStatus").value("C"))
                .andExpect(jsonPath(itemPath + ".details.statusDate").value("2026-04-01T00:00:00"))
                .andExpect(jsonPath(itemPath + ".details.associatedRecordType").value("defendant_accounts"))
                .andExpect(jsonPath(itemPath + ".details.associatedRecordId").value("99264500001001"))
                .andExpect(jsonPath(itemPath + ".details.accountNumber").value("MC2645"))
                .andExpect(jsonPath(itemPath + ".details.defendantAccountNumber").value("DEF2645"))
                .andExpect(jsonPath(itemPath + ".details.defendantAccountId").value(99264500001001L));
        }

        new JsonSchemaValidationService().validateOrError(result.andReturn().getResponse().getContentAsString(),
            HISTORY_RESPONSE_SCHEMA);

        ArgumentCaptor<LegacyGetMinorCreditorAccountHistoryRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyGetMinorCreditorAccountHistoryRequest.class);

        verify(gatewayService).postToGateway(eq(LEGACY_ACTION), eq(MinorCreditorHistoryLegacyResponse.class),
            requestCaptor.capture(), isNull());

        assertThat(requestCaptor.getValue().getCreditorAccountId()).isEqualTo(
            String.valueOf(MINOR_CREDITOR_ACCOUNT_ID));
        assertThat(requestCaptor.getValue().getFromDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(requestCaptor.getValue().getToDate()).isEqualTo(LocalDate.of(2026, 11, 30));
        assertThat(requestCaptor.getValue().getItemTypes()).isNull();
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

    private void mockLegacyResponse(MinorCreditorHistoryLegacyResponse legacyResponse) {
        when(gatewayService.postToGateway(
            eq(LEGACY_ACTION),
            eq(MinorCreditorHistoryLegacyResponse.class),
            any(LegacyGetMinorCreditorAccountHistoryRequest.class),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse, null, null));
    }

    private MinorCreditorHistoryLegacyResponse legacyResponse() {
        LocalDateTime postedDate = LocalDateTime.of(2026, 4, 1, 9, 0);

        return MinorCreditorHistoryLegacyResponse.builder()
            .historyItems(List.of(
                financialItem(postedDate, "PAY-1", BigDecimal.valueOf(10L)),
                amendmentItem(postedDate, "12345678", "87654321"),
                financialItem(postedDate, "PAY-2", BigDecimal.valueOf(20L)),
                amendmentItem(postedDate, "11112222", "33334444"),
                noteItem(postedDate, "Legacy same-day note")
            ))
            .build();
    }

    private MinorCreditorHistoryLegacyResponse filteredLegacyResponse() {
        LocalDateTime postedDate = LocalDateTime.of(2026, 4, 15, 9, 0);

        return MinorCreditorHistoryLegacyResponse.builder()
            .historyItems(List.of(
                noteItem(postedDate, "Filtered legacy note"),
                financialItem(postedDate, "PAY-FILTERED", BigDecimal.valueOf(15L))
            ))
            .build();
    }

    private LegacyMinorCreditorAccountHistoryItem amendmentItem(
        LocalDateTime postedDate,
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

    private LegacyMinorCreditorAccountHistoryItem noteItem(LocalDateTime postedDate, String noteText) {
        return LegacyMinorCreditorAccountHistoryItem.builder()
            .postedDetails(postedDetails(postedDate))
            .type("Note")
            .details(LegacyMinorCreditorHistoryDetails.builder()
                .noteText(noteText)
                .build())
            .build();
    }

    private LegacyMinorCreditorAccountHistoryItem financialItem(
        LocalDateTime postedDate,
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
                .statusDate(LocalDate.of(2026, 4, 1))
                .associatedRecordType("defendant_accounts")
                .associatedRecordId("99264500001001")
                .accountNumber("MC2645")
                .defendantAccountNumber("DEF2645")
                .defendantAccountId(99264500001001L)
                .build())
            .amount(amount)
            .build();
    }

    private LegacyMinorCreditorHistoryPostedDetails postedDetails(LocalDateTime postedDate) {
        return LegacyMinorCreditorHistoryPostedDetails.builder()
            .postedDate(postedDate)
            .postedBy("LEGUSR")
            .postedByName("Legacy User")
            .build();
    }

    private MinorCreditorHistoryLegacyResponse nestedLegacyResponse() throws JAXBException {
        String xml = """
        <response>
            <history_items>
                <history_items_element>
                    <posted_details>
                        <posted_date>2026-04-01T09:00:00</posted_date>
                        <posted_by>LEGUSR</posted_by>
                        <posted_by_name>Legacy User</posted_by_name>
                    </posted_details>
                    <type>Note</type>
                    <details>
                        <note_details>
                            <note_text>Nested legacy note</note_text>
                        </note_details>
                    </details>
                </history_items_element>
                %s
                %s
                %s
          </history_items>
        </response>
            """.formatted(
            nestedFinancialItemXml("PAY-1", 10),
            nestedFinancialItemXml("PAY-2", 20),
            nestedFinancialItemXml("PAY-3", 30)
        );

        return XmlUtil.unmarshalXmlString(xml, MinorCreditorHistoryLegacyResponse.class);
    }

    private String nestedFinancialItemXml(String paymentReference, int amount) {
        return """
        <history_items_element>
          <posted_details>
            <posted_date>2026-04-01T09:00:00</posted_date>
            <posted_by>LEGUSR</posted_by>
            <posted_by_name>Legacy User</posted_by_name>
          </posted_details>
          <type>Financial</type>
          <details>
            <creditor_transaction_details>
              <transaction_type>
                <transaction_type>PAYMNT</transaction_type>
                <transaction_type_display_name>Payment</transaction_type_display_name>
              </transaction_type>
              <payment_reference>%s</payment_reference>
              <status>
                <creditor_transaction_status>C</creditor_transaction_status>
                <creditor_transaction_status_display_name>Completed</creditor_transaction_status_display_name>
              </status>
              <status_date>2026-04-01</status_date>
              <associated_record_type>defendant_accounts</associated_record_type>
              <associated_record_id>99264500001001</associated_record_id>
              <account_number>MC2645</account_number>
              <defendant_account_number>DEF2645</defendant_account_number>
              <defendant_account_id>99264500001001</defendant_account_id>
            </creditor_transaction_details>
          </details>
          <amount>%d</amount>
        </history_items_element>
        """.formatted(paymentReference, amount);
    }
}
