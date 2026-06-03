package uk.gov.hmcts.opal.mapper.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.opal.dto.CourtReferenceDto;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.history.AmendmentDetails;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.history.DefendantTransactionDetails;
import uk.gov.hmcts.opal.dto.history.DefendantTransactionStatusReference;
import uk.gov.hmcts.opal.dto.history.DefendantTransactionTypeReference;
import uk.gov.hmcts.opal.dto.history.EnforcementDetails;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.dto.history.NoteDetails;
import uk.gov.hmcts.opal.dto.history.PaymentMethodReference;
import uk.gov.hmcts.opal.dto.history.PaymentTermsDetails;
import uk.gov.hmcts.opal.dto.history.WriteOffTypeReference;
import uk.gov.hmcts.opal.generated.model.DefendantAccountHistoryItemHistory;
import uk.gov.hmcts.opal.generated.model.DefendantTransactionStatusReferenceCommon.DefendantTransactionStatusEnum;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHistory200Response;
import uk.gov.hmcts.opal.generated.model.InstalmentPeriodCommon.InstalmentPeriodCodeEnum;
import uk.gov.hmcts.opal.generated.model.PaymentTermsTypeCommon.PaymentTermsTypeCodeEnum;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DefendantAccountHistoryResponseMapperTest.MapperTestConfig.class)
@Isolated
class DefendantAccountHistoryResponseMapperTest {

    @Autowired
    private DefendantAccountHistoryResponseMapper mapper;

    @Test
    void mapsAllHistoryItemShapesToGeneratedOpenApiModels() {
        DefendantAccountHistoryResponse response = DefendantAccountHistoryResponse.builder()
            .version(BigInteger.ONE)
            .historyItems(List.of(
                buildAmendmentItem(),
                buildEnforcementItem(),
                buildNoteItem(),
                buildPaymentTermsItem(),
                buildFinancialItem()
            ))
            .build();

        GetDefendantAccountHistory200Response generated = mapper.toGeneratedResponse(response);

        assertNotNull(generated);
        assertEquals(5, generated.getHistoryItems().size());

        DefendantAccountHistoryItemHistory amendment = generated.getHistoryItems().get(0);
        assertEquals(DefendantAccountHistoryItemHistory.TypeEnum.AMENDMENT, amendment.getType());
        assertEquals(LocalDate.of(2026, 1, 1), amendment.getPostedDetails().getPostedDate());
        assertEquals("hist-amend", amendment.getPostedDetails().getPostedBy());
        assertEquals("hist-user-1", amendment.getPostedDetails().getPostedByName());
        uk.gov.hmcts.opal.generated.model.AmendmentTypeCommon amendmentDetails =
            (uk.gov.hmcts.opal.generated.model.AmendmentTypeCommon) amendment.getDetails();
        assertEquals("1", amendmentDetails.getAttributeName());
        assertEquals("Old value", amendmentDetails.getOldValue());
        assertEquals("New value", amendmentDetails.getNewValue());

        DefendantAccountHistoryItemHistory enforcement = generated.getHistoryItems().get(1);
        assertEquals(DefendantAccountHistoryItemHistory.TypeEnum.ENFORCEMENT, enforcement.getType());
        assertEquals("HST01",
            ((uk.gov.hmcts.opal.generated.model.EnforcementDetailsHistory) enforcement.getDetails())
                .getEnforcementAction());
        assertEquals(Integer.valueOf(14),
            ((uk.gov.hmcts.opal.generated.model.EnforcementDetailsHistory) enforcement.getDetails())
                .getDaysInDefault());
        assertEquals(44L,
            ((uk.gov.hmcts.opal.generated.model.EnforcementDetailsHistory) enforcement.getDetails())
                .getHearingCourt().getCourtId());

        DefendantAccountHistoryItemHistory note = generated.getHistoryItems().get(2);
        assertEquals(DefendantAccountHistoryItemHistory.TypeEnum.NOTE, note.getType());
        assertEquals("Account note",
            ((uk.gov.hmcts.opal.generated.model.NoteDetailsHistory) note.getDetails()).getNoteText());

        DefendantAccountHistoryItemHistory paymentTerms = generated.getHistoryItems().get(3);
        assertEquals(DefendantAccountHistoryItemHistory.TypeEnum.PAYMENT_TERMS, paymentTerms.getType());
        uk.gov.hmcts.opal.generated.model.PaymentTermsCommon paymentTermsDetails =
            (uk.gov.hmcts.opal.generated.model.PaymentTermsCommon) paymentTerms.getDetails();
        assertEquals(Integer.valueOf(7), paymentTermsDetails.getDaysInDefault());
        assertEquals(LocalDate.of(2026, 2, 1), paymentTermsDetails.getDateDaysInDefaultImposed());
        assertEquals("Extension reason", paymentTermsDetails.getReasonForExtension());
        assertEquals(PaymentTermsTypeCodeEnum.I, paymentTermsDetails.getPaymentTermsType().getPaymentTermsTypeCode());
        assertEquals(LocalDate.of(2026, 3, 1), paymentTermsDetails.getEffectiveDate());
        assertEquals(InstalmentPeriodCodeEnum.M, paymentTermsDetails.getInstalmentPeriod().getInstalmentPeriodCode());
        assertEquals(new BigDecimal("100.00"), paymentTermsDetails.getLumpSumAmount());
        assertEquals(new BigDecimal("25.00"), paymentTermsDetails.getInstalmentAmount());

        DefendantAccountHistoryItemHistory financial = generated.getHistoryItems().get(4);
        assertEquals(DefendantAccountHistoryItemHistory.TypeEnum.FINANCIAL, financial.getType());
        assertEquals(new BigDecimal("-50.00"), financial.getAmount());
        uk.gov.hmcts.opal.generated.model.DefendantTransactionDetailsHistory transactionDetails =
            (uk.gov.hmcts.opal.generated.model.DefendantTransactionDetailsHistory) financial.getDetails();
        assertEquals("PAYMNT", transactionDetails.getTransactionType().getTransactionType().getValue());
        assertEquals("NC", transactionDetails.getPaymentMethod().getPaymentMethod().getValue());
        assertEquals("PAY123", transactionDetails.getPaymentReference());
        assertEquals("Extra transaction info", transactionDetails.getAdditionalInformation());
        assertEquals("TRNOUT", transactionDetails.getWriteOff().getWriteOffType().getValue());
        assertEquals(DefendantTransactionStatusEnum.PND,
            transactionDetails.getStatus().getDefendantTransactionStatus());
        assertEquals(LocalDateTime.of(2026, 1, 5, 10, 15), transactionDetails.getStatusDate());
        assertEquals("defendant_accounts", transactionDetails.getAssociatedRecordType());
        assertEquals("262200", transactionDetails.getAssociatedRecordId());
        assertEquals("262200A", transactionDetails.getAccountNumber());
        assertEquals("History Sending Court", transactionDetails.getSendingCourt());
        assertEquals(LocalDate.of(2026, 1, 5), transactionDetails.getImpositionDate());
        assertEquals("HST01", transactionDetails.getImpositionCode());
        assertEquals(new BigDecimal("-125.00"), transactionDetails.getAmountImposed());
    }

    @Test
    void mapsNullStatusAndUnsupportedHistoryTypeSafely() {
        assertNull(mapper.toGeneratedTransactionStatus(null));
        assertEquals(DefendantAccountHistoryItemHistory.TypeEnum.NOTE, mapper.mapType(HistoryItemType.NOTE));
        assertNull(mapper.mapType(null));
    }

    @Test
    void mapsCancelledStatusExplicitlyAndRejectsUnsupportedSourceStatuses() {
        assertEquals(DefendantTransactionStatusEnum.CAN,
            mapper.toGeneratedTransactionStatus(DefendantTransactionStatusReference.builder()
                    .defendantTransactionStatus("X")
                    .defendantTransactionStatusDisplayName("Cancelled")
                    .build())
                .getDefendantTransactionStatus());
        assertEquals(DefendantTransactionStatusEnum.CAN,
            mapper.toGeneratedTransactionStatus(DefendantTransactionStatusReference.builder()
                    .defendantTransactionStatus("D")
                    .defendantTransactionStatusDisplayName("Dishonoured")
                    .build())
                .getDefendantTransactionStatus());
        assertEquals(DefendantTransactionStatusEnum.CAN,
            mapper.toGeneratedTransactionStatus(DefendantTransactionStatusReference.builder()
                    .defendantTransactionStatus("R")
                    .defendantTransactionStatusDisplayName("Reversed")
                    .build())
                .getDefendantTransactionStatus());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> mapper.toGeneratedTransactionStatus(DefendantTransactionStatusReference.builder()
                .defendantTransactionStatus("Z")
                .defendantTransactionStatusDisplayName("Unknown")
                .build()));
        assertEquals("Unsupported defendant transaction status: Z", exception.getMessage());
    }

    private DefendantAccountHistoryItem buildAmendmentItem() {
        return DefendantAccountHistoryItem.builder()
            .postedDetails(PostedDetails.builder()
                .postedDate(LocalDateTime.of(2026, 1, 1, 8, 0))
                .postedBy("hist-amend")
                .postedByName("hist-user-1")
                .build())
            .type(HistoryItemType.AMENDMENT)
            .details(AmendmentDetails.builder()
                .attributeName("1")
                .oldValue("Old value")
                .newValue("New value")
                .build())
            .build();
    }

    private DefendantAccountHistoryItem buildEnforcementItem() {
        return DefendantAccountHistoryItem.builder()
            .postedDetails(PostedDetails.builder()
                .postedDate(LocalDateTime.of(2026, 1, 2, 9, 0))
                .postedBy("hist-enf")
                .postedByName("hist-user-2")
                .build())
            .type(HistoryItemType.ENFORCEMENT)
            .details(EnforcementDetails.builder()
                .enforcementAction("HST01")
                .daysInDefault(14)
                .warrantNumber("WR262200")
                .hearingDate(LocalDate.of(2026, 2, 2))
                .hearingCourt(CourtReferenceDto.builder().courtId(44).courtName("Magistrates Court").build())
                .caseNumber("CASE-1")
                .reason("Reason")
                .earliestDateOfRelease(LocalDate.of(2026, 3, 1))
                .build())
            .build();
    }

    private DefendantAccountHistoryItem buildNoteItem() {
        return DefendantAccountHistoryItem.builder()
            .postedDetails(PostedDetails.builder()
                .postedDate(LocalDateTime.of(2026, 1, 3, 10, 0))
                .postedBy("hist-note")
                .postedByName("hist-user-3")
                .build())
            .type(HistoryItemType.NOTE)
            .details(NoteDetails.builder().noteText("Account note").build())
            .build();
    }

    private DefendantAccountHistoryItem buildPaymentTermsItem() {
        return DefendantAccountHistoryItem.builder()
            .postedDetails(PostedDetails.builder()
                .postedDate(LocalDateTime.of(2026, 1, 4, 11, 0))
                .postedBy("hist-pt")
                .postedByName("hist-user-4")
                .build())
            .type(HistoryItemType.PAYMENT_TERMS)
            .details(PaymentTermsDetails.builder()
                .daysInDefault(7)
                .dateDaysInDefaultImposed(LocalDate.of(2026, 2, 1))
                .reasonForExtension("Extension reason")
                .paymentTermsType(PaymentTermsType.fromCode("I"))
                .effectiveDate(LocalDate.of(2026, 3, 1))
                .instalmentPeriod(InstalmentPeriod.fromCode("M"))
                .lumpSumAmount(new BigDecimal("100.00"))
                .instalmentAmount(new BigDecimal("25.00"))
                .build())
            .build();
    }

    private DefendantAccountHistoryItem buildFinancialItem() {
        return DefendantAccountHistoryItem.builder()
            .postedDetails(PostedDetails.builder()
                .postedDate(LocalDateTime.of(2026, 1, 5, 12, 0))
                .postedBy("hist-fin")
                .postedByName("hist-user-5")
                .build())
            .type(HistoryItemType.FINANCIAL)
            .amount(new BigDecimal("-50.00"))
            .details(DefendantTransactionDetails.builder()
                .transactionType(DefendantTransactionTypeReference.builder()
                    .transactionType("PAYMNT")
                    .transactionTypeDisplayName("Payment")
                    .build())
                .paymentMethod(PaymentMethodReference.builder()
                    .paymentMethod("NC")
                    .paymentMethodDisplayName("Card")
                    .build())
                .paymentReference("PAY123")
                .additionalInformation("Extra transaction info")
                .writeOff(WriteOffTypeReference.builder()
                    .writeOffType("TRNOUT")
                    .writeOffTypeDisplayName("Write off")
                    .build())
                .status(DefendantTransactionStatusReference.builder()
                    .defendantTransactionStatus("P")
                    .defendantTransactionStatusDisplayName("Pending")
                    .build())
                .statusDate(LocalDateTime.of(2026, 1, 5, 10, 15))
                .associatedRecordType("defendant_accounts")
                .associatedRecordId("262200")
                .accountNumber("262200A")
                .sendingCourt("History Sending Court")
                .impositionDate(LocalDate.of(2026, 1, 5))
                .impositionCode("HST01")
                .amountImposed(new BigDecimal("-125.00"))
                .build())
            .build();
    }

    @Configuration
    @ComponentScan(basePackages = "uk.gov.hmcts.opal.mapper")
    static class MapperTestConfig {
    }
}
