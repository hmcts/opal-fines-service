package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse.LegacyCreditorTransactionStatusReference;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse.LegacyCreditorTransactionTypeReference;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse.LegacyMinorCreditorAccountHistoryItem;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse.LegacyMinorCreditorHistoryDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse.LegacyMinorCreditorHistoryPostedDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryRequest;
import uk.gov.hmcts.opal.dto.response.GetMinorCreditorHistoryResponse;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryFilters;
import uk.gov.hmcts.opal.generated.model.AmendmentTypeCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionDetailsHistory;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.MinorCreditorHistoryItemHistory;
import uk.gov.hmcts.opal.generated.model.NoteDetailsHistory;

class MinorCreditorHistoryLegacyMapperTest {

    private final LegacyMinorCreditorHistoryMapper mapper = new LegacyMinorCreditorHistoryMapper();

    @Test
    void toLegacyRequest_whenNoFilters_mapsAccountIdAndOmitsItemTypes() {
        // Arrange
        MinorCreditorHistoryFilters filters = MinorCreditorHistoryFilters.from(null, null, null);

        // Act
        LegacyGetMinorCreditorAccountHistoryRequest result = mapper.toLegacyRequest(101L, filters);

        // Assert
        assertEquals("101", result.getCreditorAccountId());
        assertNull(result.getFromDate());
        assertNull(result.getToDate());
        assertNull(result.getItemTypes());
    }

    @Test
    void toLegacyRequest_whenDateRangeAndSingleType_mapsFilters() {
        // Arrange
        LocalDate dateFrom = LocalDate.of(2026, 1, 10);
        LocalDate dateTo = LocalDate.of(2026, 1, 31);
        MinorCreditorHistoryFilters filters = MinorCreditorHistoryFilters.from(dateFrom, dateTo, List.of("note"));

        // Act
        LegacyGetMinorCreditorAccountHistoryRequest result = mapper.toLegacyRequest(202L, filters);

        // Assert
        assertEquals("202", result.getCreditorAccountId());
        assertEquals(dateFrom, result.getFromDate());
        assertEquals(dateTo, result.getToDate());
        assertEquals(List.of("Note"), result.getItemTypes());
    }

    @Test
    void toLegacyRequest_whenAllTypesSelected_omitsItemTypes() {
        // Arrange
        MinorCreditorHistoryFilters filters = MinorCreditorHistoryFilters.from(
            null,
            null,
            List.of("note,financial,amendment")
        );

        // Act
        LegacyGetMinorCreditorAccountHistoryRequest result = mapper.toLegacyRequest(303L, filters);

        // Assert
        assertNull(result.getItemTypes());
    }

    @Test
    void toLegacyRequest_whenSomeTypes_mapsTypesInStableOrder() {
        // Arrange
        MinorCreditorHistoryFilters filters = MinorCreditorHistoryFilters.from(
            null,
            null,
            List.of("note,financial")
        );

        // Act
        LegacyGetMinorCreditorAccountHistoryRequest result = mapper.toLegacyRequest(404L, filters);

        // Assert
        assertEquals(List.of("Financial", "Note"), result.getItemTypes());
    }

    @Test
    void toOpal_whenLegacyResponseNull_returnsEmptyHistoryWithoutVersion() {
        // Arrange

        // Act
        GetMinorCreditorHistoryResponse result = mapper.toOpal(null);

        // Assert
        assertNull(result.getVersion());
        assertEquals(0, result.getPayload().getHistoryItems().size());
    }

    @Test
    void toOpal_mapsItemsAndSortsNewestToOldest() {
        // Arrange
        LegacyGetMinorCreditorAccountHistoryResponse legacy = LegacyGetMinorCreditorAccountHistoryResponse.builder()
            .historyItems(List.of(
                noteItem(LocalDate.of(2026, 1, 11), "note one"),
                amendmentItem(LocalDate.of(2026, 1, 12), "Address", "Old", "New"),
                financialItem(LocalDate.of(2026, 1, 13), "PAYMNT", "P", BigDecimal.valueOf(25.50))
            ))
            .build();

        // Act
        GetMinorCreditorHistoryResponse result = mapper.toOpal(legacy);

        // Assert
        List<MinorCreditorHistoryItemHistory> historyItems = result.getPayload().getHistoryItems();
        assertEquals(3, historyItems.size());
        assertEquals(MinorCreditorHistoryItemHistory.TypeEnum.FINANCIAL, historyItems.get(0).getType());
        assertEquals(LocalDate.of(2026, 1, 13), historyItems.get(0).getPostedDetails().getPostedDate());
        assertEquals(MinorCreditorHistoryItemHistory.TypeEnum.AMENDMENT, historyItems.get(1).getType());
        assertEquals(MinorCreditorHistoryItemHistory.TypeEnum.NOTE, historyItems.get(2).getType());
    }

    @Test
    void toOpal_mapsAmendmentDetails() {
        // Arrange
        LegacyGetMinorCreditorAccountHistoryResponse legacy = LegacyGetMinorCreditorAccountHistoryResponse.builder()
            .historyItems(List.of(amendmentItem(LocalDate.of(2026, 2, 1), "Sort code", "123456", "654321")))
            .build();

        // Act
        GetMinorCreditorHistoryResponse result = mapper.toOpal(legacy);

        // Assert
        MinorCreditorHistoryItemHistory historyItem = result.getPayload().getHistoryItems().getFirst();
        AmendmentTypeCommon details = assertInstanceOf(AmendmentTypeCommon.class, historyItem.getDetails());
        assertEquals("Sort code", details.getAttributeName());
        assertEquals("123456", details.getOldValue());
        assertEquals("654321", details.getNewValue());
        assertNull(historyItem.getAmount());
    }

    @Test
    void toOpal_mapsNoteDetails() {
        // Arrange
        LegacyGetMinorCreditorAccountHistoryResponse legacy = LegacyGetMinorCreditorAccountHistoryResponse.builder()
            .historyItems(List.of(noteItem(LocalDate.of(2026, 2, 2), "Minor creditor note")))
            .build();

        // Act
        GetMinorCreditorHistoryResponse result = mapper.toOpal(legacy);

        // Assert
        NoteDetailsHistory details = assertInstanceOf(
            NoteDetailsHistory.class,
            result.getPayload().getHistoryItems().getFirst().getDetails()
        );
        assertEquals("Minor creditor note", details.getNoteText());
    }

    @Test
    void toOpal_mapsFinancialDetailsUsingCodeAsDisplayName() {
        // Arrange
        LegacyGetMinorCreditorAccountHistoryResponse legacy = LegacyGetMinorCreditorAccountHistoryResponse.builder()
            .historyItems(List.of(financialItem(
                LocalDate.of(2026, 2, 3),
                "PAYMNT",
                "P",
                BigDecimal.valueOf(15.25)
            )))
            .build();

        // Act
        GetMinorCreditorHistoryResponse result = mapper.toOpal(legacy);

        // Assert
        MinorCreditorHistoryItemHistory historyItem = result.getPayload().getHistoryItems().getFirst();
        CreditorTransactionDetailsHistory details = assertInstanceOf(
            CreditorTransactionDetailsHistory.class,
            historyItem.getDetails()
        );
        assertEquals(BigDecimal.valueOf(15.25), historyItem.getAmount());
        assertEquals(
            CreditorTransactionTypeReferenceCommon.TransactionTypeEnum.PAYMNT,
            details.getTransactionType().getTransactionType()
        );
        assertEquals("PAYMNT", details.getTransactionType().getTransactionTypeDisplayName());
        assertEquals(
            CreditorTransactionStatusReferenceCommon.CreditorTransactionStatusEnum.P,
            details.getStatus().getCreditorTransactionStatus()
        );
        assertEquals("P", details.getStatus().getCreditorTransactionStatusDisplayName());
        assertEquals(LocalDateTime.of(2026, 2, 3, 12, 30), details.getStatusDate());
        assertEquals("Minor creditor", details.getAssociatedRecordType());
        assertEquals("777", details.getAssociatedRecordId());
        assertEquals("MC-1", details.getAccountNumber());
        assertEquals("DA-1", details.getDefendantAccountNumber());
        assertEquals(909L, details.getDefendantAccountId());
    }

    private LegacyMinorCreditorAccountHistoryItem amendmentItem(
        LocalDate postedDate,
        String attributeName,
        String oldValue,
        String newValue) {

        return LegacyMinorCreditorAccountHistoryItem.builder()
            .postedDetails(postedDetails(postedDate))
            .type("Amendment")
            .details(LegacyMinorCreditorHistoryDetails.builder()
                .attributeName(attributeName)
                .oldValue(oldValue)
                .newValue(newValue)
                .build())
            .amount(BigDecimal.TEN)
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
        String transactionType,
        String status,
        BigDecimal amount) {

        return LegacyMinorCreditorAccountHistoryItem.builder()
            .postedDetails(postedDetails(postedDate))
            .type("Financial")
            .details(LegacyMinorCreditorHistoryDetails.builder()
                .transactionType(LegacyCreditorTransactionTypeReference.builder()
                    .transactionType(transactionType)
                    .transactionTypeDisplayName("Ignored display name")
                    .build())
                .paymentReference("PAY-1")
                .status(LegacyCreditorTransactionStatusReference.builder()
                    .creditorTransactionStatus(status)
                    .creditorTransactionStatusDisplayName("Ignored status")
                    .build())
                .statusDate(LocalDateTime.of(2026, 2, 3, 12, 30))
                .associatedRecordType("Minor creditor")
                .associatedRecordId("777")
                .accountNumber("MC-1")
                .defendantAccountNumber("DA-1")
                .defendantAccountId(909L)
                .build())
            .amount(amount)
            .build();
    }

    private LegacyMinorCreditorHistoryPostedDetails postedDetails(LocalDate postedDate) {
        return LegacyMinorCreditorHistoryPostedDetails.builder()
            .postedDate(postedDate)
            .postedBy("user1")
            .postedByName("User One")
            .build();
    }
}
