package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.history.DefendantTransactionDetails;
import uk.gov.hmcts.opal.dto.history.EnforcementDetails;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.dto.history.NoteDetails;
import uk.gov.hmcts.opal.dto.history.PaymentTermsDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHistoryRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHistoryResponse.LegacyDefendantAccountHistoryDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHistoryResponse.LegacyDefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHistoryResponse.LegacyHistoryTypeReference;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.dto.legacy.common.CourtReference;

class LegacyDefAccServiceHistoryTest extends AbstractLegacyDefAccServiceTest {

    @Test
    void createGetDefendantAccountHistoryRequest_mapsFilterToLegacyRequest() {
        DefendantAccountHistoryFilter filter = DefendantAccountHistoryFilter.builder()
            .dateFrom(LocalDate.of(2026, 5, 11))
            .dateTo(LocalDate.of(2026, 5, 12))
            .itemTypes(List.of(HistoryItemType.ENFORCEMENT, HistoryItemType.NOTE))
            .build();

        LegacyGetDefendantAccountHistoryRequest request =
            LegacyDefendantAccountService.createGetDefendantAccountHistoryRequest(99000000000001L, filter);

        assertEquals("99000000000001", request.getDefendantAccountId());
        assertEquals(LocalDate.of(2026, 5, 11), request.getFromDate());
        assertEquals(LocalDate.of(2026, 5, 12), request.getToDate());
        assertEquals(List.of("Enforcement", "Note"), request.getItemTypes());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getHistory_success_mapsMixedLegacyHistoryItems() {
        LegacyGetDefendantAccountHistoryResponse responseBody = LegacyGetDefendantAccountHistoryResponse.builder()
            .version(9L)
            .historyItems(List.of(
                amendmentItem(),
                paymentTermsItem(),
                enforcementItem(),
                financialItem(),
                noteItem()
            ))
            .build();

        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHistoryResponse>>any()
        )).thenReturn(responseBody);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>(responseBody.toXml(), HttpStatus.OK));

        DefendantAccountHistoryFilter filter = DefendantAccountHistoryFilter.builder()
            .dateFrom(LocalDate.of(2026, 5, 11))
            .dateTo(LocalDate.of(2026, 5, 12))
            .itemTypes(List.of(HistoryItemType.ENFORCEMENT, HistoryItemType.NOTE))
            .build();

        DefendantAccountHistoryResponse out = legacyDefendantAccountService.getHistory(99000000000001L, filter);

        assertNotNull(out);
        assertEquals(BigInteger.valueOf(9L), out.getVersion());
        assertEquals(5, out.getHistoryItems().size());
        assertEquals(HistoryItemType.FINANCIAL, out.getHistoryItems().get(0).getType());
        assertEquals(HistoryItemType.PAYMENT_TERMS, out.getHistoryItems().get(1).getType());
        assertEquals("Reason text",
            ((EnforcementDetails) out.getHistoryItems().get(3).getDetails()).getReason());
        assertEquals("Account note text",
            ((NoteDetails) out.getHistoryItems().get(2).getDetails()).getNoteText());
        assertEquals("Extension reason",
            ((PaymentTermsDetails) out.getHistoryItems().get(1).getDetails()).getReasonForExtension());
        assertEquals("PAY123",
            ((DefendantTransactionDetails) out.getHistoryItems().get(0).getDetails()).getPaymentReference());
        assertEquals(new BigDecimal("-25.50"), out.getHistoryItems().get(0).getAmount());
        assertEquals(LocalDateTime.of(2026, 5, 12, 10, 15), out.getHistoryItems().get(0).getEventDateTime());
        assertEquals(HistoryItemType.AMENDMENT, out.getHistoryItems().get(4).getType());

        ArgumentCaptor<LegacyGetDefendantAccountHistoryRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyGetDefendantAccountHistoryRequest.class);
        verify(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.GET_DEFENDANT_ACCOUNT_HISTORY),
            eq(LegacyGetDefendantAccountHistoryResponse.class),
            requestCaptor.capture(),
            eq(null)
        );
        assertEquals("99000000000001", requestCaptor.getValue().getDefendantAccountId());
        assertEquals(List.of("Enforcement", "Note"), requestCaptor.getValue().getItemTypes());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getHistory_success_withNullEntity_returnsDefaultEmptyResponse() {
        when(restClient.responseSpec.body(
            Mockito.<ParameterizedTypeReference<LegacyGetDefendantAccountHistoryResponse>>any()
        )).thenReturn(null);
        when(restClient.responseSpec.toEntity(String.class))
            .thenReturn(new ResponseEntity<>("<response/>", HttpStatus.OK));

        DefendantAccountHistoryResponse out =
            legacyDefendantAccountService.getHistory(1L, DefendantAccountHistoryFilter.builder().build());

        assertNotNull(out);
        assertEquals(BigInteger.ONE, out.getVersion());
        assertNotNull(out.getHistoryItems());
        assertEquals(0, out.getHistoryItems().size());
    }

    private LegacyDefendantAccountHistoryItem amendmentItem() {
        return LegacyDefendantAccountHistoryItem.builder()
            .postedDetails(postedDetails(2026, 5, 10, 9, 0, "amend-user", "Amend User"))
            .type("Amendment")
            .details(LegacyDefendantAccountHistoryDetails.builder()
                .attributeName("Account status")
                .oldValue("Old")
                .newValue("New")
                .build())
            .build();
    }

    private LegacyDefendantAccountHistoryItem enforcementItem() {
        return LegacyDefendantAccountHistoryItem.builder()
            .postedDetails(postedDetails(2026, 5, 11, 9, 30, "enf-user", "Enforcement User"))
            .type("Enforcement")
            .details(LegacyDefendantAccountHistoryDetails.builder()
                .enforcementAction("HST01")
                .daysInDefault(14)
                .warrantNumber("WR123")
                .hearingDate(LocalDate.of(2026, 6, 1))
                .hearingCourt(CourtReference.builder().courtId(44L).courtName("North Court").build())
                .caseNumber("CASE-1")
                .reason("Reason text")
                .earliestDateOfRelease(LocalDate.of(2026, 7, 1))
                .build())
            .build();
    }

    private LegacyDefendantAccountHistoryItem noteItem() {
        return LegacyDefendantAccountHistoryItem.builder()
            .postedDetails(postedDetails(2026, 5, 11, 12, 0, "note-user", "Note User"))
            .type("Note")
            .details(LegacyDefendantAccountHistoryDetails.builder()
                .noteText("Account note text")
                .build())
            .build();
    }

    private LegacyDefendantAccountHistoryItem paymentTermsItem() {
        return LegacyDefendantAccountHistoryItem.builder()
            .postedDetails(postedDetails(2026, 5, 12, 8, 45, "pt-user", "Payment Terms User"))
            .type("Payment terms")
            .details(LegacyDefendantAccountHistoryDetails.builder()
                .daysInDefault(7)
                .dateDaysInDefaultImposed(LocalDate.of(2026, 5, 12))
                .reasonForExtension("Extension reason")
                .paymentTermsType(new LegacyPaymentTermsType(LegacyPaymentTermsType.PaymentTermsTypeCode.I))
                .effectiveDate(LocalDate.of(2026, 5, 20))
                .instalmentPeriod(new LegacyInstalmentPeriod(LegacyInstalmentPeriod.InstalmentPeriodCode.M))
                .lumpSumAmount(new BigDecimal("100.00"))
                .instalmentAmount(new BigDecimal("25.00"))
                .build())
            .build();
    }

    private LegacyDefendantAccountHistoryItem financialItem() {
        return LegacyDefendantAccountHistoryItem.builder()
            .postedDetails(postedDetails(2026, 5, 12, 10, 15, "fin-user", "Financial User"))
            .type("Financial")
            .amount(new BigDecimal("-25.50"))
            .details(LegacyDefendantAccountHistoryDetails.builder()
                .transactionType(LegacyHistoryTypeReference.builder()
                    .transactionType("PAYMNT")
                    .transactionTypeDisplayName("Payment")
                    .build())
                .paymentMethod(LegacyHistoryTypeReference.builder()
                    .paymentMethod("NC")
                    .paymentMethodDisplayName("National cheque")
                    .build())
                .paymentReference("PAY123")
                .additionalInformation("Extra info")
                .writeOff(LegacyHistoryTypeReference.builder()
                    .writeOffType("TRNOUT")
                    .writeOffTypeDisplayName("Transfer out")
                    .build())
                .status(LegacyHistoryTypeReference.builder()
                    .defendantTransactionStatus("P")
                    .defendantTransactionStatusDisplayName("Partially-reversed")
                    .build())
                .statusDate(LocalDateTime.of(2026, 5, 12, 10, 20))
                .associatedRecordType("defendant_accounts")
                .associatedRecordId("262200")
                .accountNumber("262200A")
                .sendingCourt("North Court")
                .impositionDate(LocalDate.of(2026, 5, 12))
                .impositionCode("HST01")
                .amountImposed(new BigDecimal("-125.00"))
                .build())
            .build();
    }

    private LegacyPostedDetails postedDetails(int year, int month, int day, int hour, int minute, String postedBy,
                                              String postedByName) {
        return new LegacyPostedDetails(LocalDateTime.of(year, month, day, hour, minute), postedBy, postedByName);
    }
}
