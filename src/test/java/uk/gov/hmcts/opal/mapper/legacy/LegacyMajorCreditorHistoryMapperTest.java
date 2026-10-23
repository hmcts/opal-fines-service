package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.legacy.LegacyMajorCreditorHistoryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyMajorCreditorHistoryResponse.HistoryDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyMajorCreditorHistoryResponse.HistoryItem;
import uk.gov.hmcts.opal.dto.legacy.LegacyMajorCreditorHistoryResponse.TransactionStatusReference;
import uk.gov.hmcts.opal.dto.legacy.LegacyMajorCreditorHistoryResponse.TransactionTypeReference;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.dto.response.GetMajorCreditorHistoryResponse;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionDetailsHistory;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.MajorCreditorHistoryItemHistory;
import uk.gov.hmcts.opal.generated.model.NoteDetailsHistory;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest;

class LegacyMajorCreditorHistoryMapperTest extends AbstractMapperTest {

    @Autowired
    private LegacyMajorCreditorHistoryMapper mapper;

    @Test
    void toOpal_mapsLegacyHistoryResponse() {
        LegacyMajorCreditorHistoryResponse legacy =
            LegacyMajorCreditorHistoryResponse.builder()
                .version(BigInteger.valueOf(7L))
                .historyItems(List.of(HistoryItem.builder()
                    .postedDetails(new LegacyPostedDetails(
                        LocalDateTime.of(2026, Month.JANUARY, 31, 10, 30),
                        "MJUSR3",
                        "Major User Three"
                    ))
                    .type("Financial")
                    .amount(new BigDecimal("-31.00"))
                    .details(HistoryDetails.builder()
                        .transactionType(TransactionTypeReference.builder()
                            .transactionType("MADJ")
                            .transactionTypeDisplayName("Manual Adjustment")
                            .build())
                        .paymentReference("MJF003")
                        .status(TransactionStatusReference.builder()
                            .creditorTransactionStatus("R")
                            .creditorTransactionStatusDisplayName("Reversed")
                            .build())
                        .statusDate(LocalDate.of(2026, Month.JANUARY, 31))
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
        assertEquals("2026-01-31T10:30", item.getPostedDetails().getPostedDate().toString());
        assertEquals("MJUSR3", item.getPostedDetails().getPostedBy());
        assertEquals("Major User Three", item.getPostedDetails().getPostedByName());

        CreditorTransactionDetailsHistory details = (CreditorTransactionDetailsHistory) item.getDetails();
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
        assertEquals(LocalDateTime.of(2026, Month.JANUARY, 31, 0, 0), details.getStatusDate());
        assertEquals("creditor_accounts", details.getAssociatedRecordType());
        assertEquals("99264300000001", details.getAssociatedRecordId());
        assertEquals("87654321", details.getAccountNumber());
        assertEquals("12345678", details.getDefendantAccountNumber());
        assertEquals(999L, details.getDefendantAccountId());
    }

    @Test
    void toDetails_preservesMissingStatusDate() {
        CreditorTransactionDetailsHistory details = mapper.toDetails(
            HistoryDetails.builder().build());

        assertNull(details.getStatusDate());
    }

    @Test
    void toOpal_mapsLegacyNoteHistoryResponse() {
        LegacyMajorCreditorHistoryResponse legacy =
            LegacyMajorCreditorHistoryResponse.builder()
                .version(BigInteger.valueOf(7L))
                .historyItems(List.of(HistoryItem.builder()
                    .postedDetails(new LegacyPostedDetails(
                        LocalDateTime.of(2026, Month.JANUARY, 31, 10, 30),
                        "MJUSR3",
                        "Major User Three"
                    ))
                    .type("Note")
                    .details(HistoryDetails.builder()
                        .noteText("History note")
                        .build())
                    .build()))
                .build();

        GetMajorCreditorHistoryResponse result = mapper.toOpal(legacy);

        MajorCreditorHistoryItemHistory item = result.getPayload().getHistoryItems().getFirst();
        assertEquals(MajorCreditorHistoryItemHistory.TypeEnum.NOTE, item.getType());
        NoteDetailsHistory details = (NoteDetailsHistory) item.getDetails();
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
        LegacyMajorCreditorHistoryResponse legacy =
            LegacyMajorCreditorHistoryResponse.builder()
                .version(BigInteger.valueOf(7L))
                .historyItems(List.of(
                    historyItem("MJF002", LocalDateTime.of(2026, Month.JANUARY, 25, 9, 15)),
                    historyItem("MJF004", LocalDateTime.of(2026, Month.JANUARY, 31, 10, 30)),
                    historyItem("MJF003", LocalDateTime.of(2026, Month.JANUARY, 31, 10, 30))
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

    @Test
    void mapper_handlesNullAndUnsupportedHistoryDetails() {
        assertNull(mapper.mapDetails(null, HistoryDetails.builder().build()));
        assertNull(mapper.mapDetails("Financial", null));
        assertNull(mapper.toHistoryItemType(null));
        assertNull(mapper.toTransactionTypeEnum(null));
        assertNull(mapper.toTransactionStatusEnum(null));

        assertThrows(IllegalArgumentException.class,
            () -> mapper.mapDetails("Amendment", HistoryDetails.builder().build()));
    }

    private HistoryItem historyItem(String paymentReference, LocalDateTime postedDate) {
        return HistoryItem.builder()
            .postedDetails(new LegacyPostedDetails(postedDate, "MJUSR", "Major User"))
            .type("Financial")
            .details(HistoryDetails.builder()
                .transactionType(TransactionTypeReference.builder()
                    .transactionType("MADJ")
                    .transactionTypeDisplayName("Manual Adjustment")
                    .build())
                .paymentReference(paymentReference)
                .associatedRecordId("99264300000001")
                .build())
            .build();
    }
}
