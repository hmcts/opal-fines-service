package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.legacy.MajorCreditorHistoryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.MajorCreditorHistoryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.MajorCreditorHistoryLegacyResponse.LegacyTransactionStatusReference;
import uk.gov.hmcts.opal.dto.legacy.MajorCreditorHistoryLegacyResponse.LegacyTransactionTypeReference;
import uk.gov.hmcts.opal.dto.legacy.MajorCreditorHistoryLegacyResponse.LegacyHistoryDetails;
import uk.gov.hmcts.opal.dto.legacy.MajorCreditorHistoryLegacyResponse.LegacyHistoryItem;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.dto.response.GetMajorCreditorHistoryResponse;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionDetailsHistory;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.MajorCreditorHistoryItemHistory;
import uk.gov.hmcts.opal.generated.model.NoteDetailsHistory;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest;

class MajorCreditorHistoryLegacyMapperTest extends AbstractMapperTest {

    @Autowired
    private MajorCreditorHistoryLegacyMapper mapper;

    @Test
    void toLegacyRequest_mapsRequestValuesAndItemTypes() {
        MajorCreditorHistoryLegacyRequest result = mapper.toLegacyRequest(
            123L,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31),
            List.of("financial,note")
        );

        assertEquals("123", result.getCreditorAccountId());
        assertEquals(LocalDate.of(2026, 1, 1), result.getFromDate());
        assertEquals(LocalDate.of(2026, 1, 31), result.getToDate());
        assertEquals(List.of("Financial", "Note"), result.getItemTypes());
    }

    @Test
    void toLegacyRequest_mapsEmptyItemTypesToNull() {
        MajorCreditorHistoryLegacyRequest result = mapper.toLegacyRequest(
            123L,
            null,
            null,
            List.of(" ")
        );

        assertEquals("123", result.getCreditorAccountId());
        assertEquals(null, result.getFromDate());
        assertEquals(null, result.getToDate());
        assertEquals(null, result.getItemTypes());
    }

    @Test
    void toOpal_mapsLegacyHistoryResponse() {
        MajorCreditorHistoryLegacyResponse legacy =
            MajorCreditorHistoryLegacyResponse.builder()
                .version(7L)
                .historyItems(List.of(LegacyHistoryItem.builder()
                    .postedDetails(new LegacyPostedDetails(
                        LocalDateTime.of(2026, 1, 31, 10, 30),
                        "MJUSR3",
                        "Major User Three"
                    ))
                    .type("Financial")
                    .amount(new BigDecimal("-31.00"))
                    .details(LegacyHistoryDetails.builder()
                        .transactionType(LegacyTransactionTypeReference.builder()
                            .transactionType("MADJ")
                            .transactionTypeDisplayName("Manual Adjustment")
                            .build())
                        .paymentReference("MJF003")
                        .status(LegacyTransactionStatusReference.builder()
                            .creditorTransactionStatus("R")
                            .creditorTransactionStatusDisplayName("Reversed")
                            .build())
                        .statusDate(LocalDateTime.of(2026, 1, 31, 10, 30))
                        .associatedRecordType("creditor_accounts")
                        .associatedRecordId("99264300000001")
                        .accountNumber("87654321")
                        .defendantAccountNumber("12345678")
                        .defendantAccountId(999L)
                        .build())
                    .build()))
                .build();

        GetMajorCreditorHistoryResponse result = mapper.toOpal(legacy);

        assertEquals(BigInteger.valueOf(7), result.getVersion());
        assertNotNull(result.getPayload());
        assertEquals(1, result.getPayload().getHistoryItems().size());

        MajorCreditorHistoryItemHistory item = result.getPayload().getHistoryItems().getFirst();
        assertEquals(MajorCreditorHistoryItemHistory.TypeEnum.FINANCIAL, item.getType());
        assertEquals(new BigDecimal("-31.00"), item.getAmount());
        assertEquals("2026-01-31", item.getPostedDetails().getPostedDate().toString());
        assertEquals("MJUSR3", item.getPostedDetails().getPostedBy());
        assertEquals("Major User Three", item.getPostedDetails().getPostedByName());

        CreditorTransactionDetailsHistory details = (CreditorTransactionDetailsHistory)item.getDetails();
        assertEquals(
            CreditorTransactionTypeReferenceCommon.TransactionTypeEnum.MADJ,
            details.getTransactionType().getTransactionType()
        );
        assertEquals("Manual Adjustment", details.getTransactionType().getTransactionTypeDisplayName());
        assertEquals("MJF003", details.getPaymentReference());
        assertEquals(
            CreditorTransactionStatusReferenceCommon.CreditorTransactionStatusEnum.R,
            details.getStatus().getCreditorTransactionStatus()
        );
        assertEquals("Reversed", details.getStatus().getCreditorTransactionStatusDisplayName());
        assertEquals(LocalDateTime.of(2026, 1, 31, 10, 30), details.getStatusDate());
        assertEquals("creditor_accounts", details.getAssociatedRecordType());
        assertEquals("99264300000001", details.getAssociatedRecordId());
        assertEquals("87654321", details.getAccountNumber());
        assertEquals("12345678", details.getDefendantAccountNumber());
        assertEquals(999L, details.getDefendantAccountId());
    }

    @Test
    void toOpal_mapsLegacyNoteHistoryResponse() {
        MajorCreditorHistoryLegacyResponse legacy =
            MajorCreditorHistoryLegacyResponse.builder()
                .version(7L)
                .historyItems(List.of(LegacyHistoryItem.builder()
                    .postedDetails(new LegacyPostedDetails(
                        LocalDateTime.of(2026, 1, 31, 10, 30),
                        "MJUSR3",
                        "Major User Three"
                    ))
                    .type("Note")
                    .details(LegacyHistoryDetails.builder()
                        .noteText("History note")
                        .build())
                    .build()))
                .build();

        GetMajorCreditorHistoryResponse result = mapper.toOpal(legacy);

        MajorCreditorHistoryItemHistory item = result.getPayload().getHistoryItems().getFirst();
        assertEquals(MajorCreditorHistoryItemHistory.TypeEnum.NOTE, item.getType());
        NoteDetailsHistory details = (NoteDetailsHistory)item.getDetails();
        assertEquals("History note", details.getNoteText());
    }

    @Test
    void toOpal_mapsNullLegacyResponseToEmptyPayloadWithDefaultVersion() {
        GetMajorCreditorHistoryResponse result = mapper.toOpal(null);

        assertEquals(BigInteger.ONE, result.getVersion());
        assertNotNull(result.getPayload());
        assertEquals(List.of(), result.getPayload().getHistoryItems());
    }

    @Test
    void toOpal_ordersHistoryItemsNewestFirstWithDeterministicTieHandling() {
        MajorCreditorHistoryLegacyResponse legacy =
            MajorCreditorHistoryLegacyResponse.builder()
                .version(7L)
                .historyItems(List.of(
                    historyItem("MJF002", LocalDateTime.of(2026, 1, 25, 9, 15)),
                    historyItem("MJF004", LocalDateTime.of(2026, 1, 31, 10, 30)),
                    historyItem("MJF003", LocalDateTime.of(2026, 1, 31, 10, 30))
                ))
                .build();

        GetMajorCreditorHistoryResponse result = mapper.toOpal(legacy);

        assertEquals(
            List.of("MJF003", "MJF004", "MJF002"),
            result.getPayload().getHistoryItems().stream()
                .map(MajorCreditorHistoryItemHistory::getDetails)
                .map(CreditorTransactionDetailsHistory.class::cast)
                .map(CreditorTransactionDetailsHistory::getPaymentReference)
                .toList()
        );
    }

    private LegacyHistoryItem historyItem(String paymentReference, LocalDateTime postedDate) {
        return LegacyHistoryItem.builder()
            .postedDetails(new LegacyPostedDetails(postedDate, "MJUSR", "Major User"))
            .type("Financial")
            .details(LegacyHistoryDetails.builder()
                .transactionType(LegacyTransactionTypeReference.builder()
                    .transactionType("MADJ")
                    .transactionTypeDisplayName("Manual Adjustment")
                    .build())
                .paymentReference(paymentReference)
                .associatedRecordId("99264300000001")
                .build())
            .build();
    }
}
