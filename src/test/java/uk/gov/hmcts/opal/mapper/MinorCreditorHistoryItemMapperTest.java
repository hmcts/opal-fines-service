package uk.gov.hmcts.opal.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType.AMENDMENT;
import static uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType.FINANCIAL;
import static uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType.NOTE;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItem;
import uk.gov.hmcts.opal.generated.model.AmendmentTypeCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionDetailsHistory;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.NoteDetailsHistory;
import uk.gov.hmcts.opal.generated.model.PostedDetailsCommon;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorAmendmentHistoryProjection;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorNoteHistoryProjection;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorTransactionHistoryProjection;

class MinorCreditorHistoryItemMapperTest {

    private final MinorCreditorHistoryItemMapper mapper = new MinorCreditorHistoryItemMapper();

    @Test
    void toHistoryItem_mapsAmendmentDetails() {
        // Arrange
        LocalDateTime postedDate = LocalDateTime.of(2026, 1, 31, 10, 30);
        MinorCreditorAmendmentHistoryProjection projection =
            mock(MinorCreditorAmendmentHistoryProjection.class);
        when(projection.getAmendmentId()).thenReturn(11L);
        when(projection.getPostedDate()).thenReturn(postedDate);
        when(projection.getPostedBy()).thenReturn("AMENDUSR");
        when(projection.getPostedByName()).thenReturn("Amend User");
        when(projection.getAttributeName()).thenReturn("Hold Pay Out");
        when(projection.getOldValue()).thenReturn("false");
        when(projection.getNewValue()).thenReturn("true");

        // Act
        MinorCreditorHistoryItem mapped = mapper.toHistoryItem(projection);

        // Assert
        assertThat(mapped.sourceType()).isEqualTo(AMENDMENT);
        assertThat(mapped.sourceId()).isEqualTo(11L);
        assertThat(mapped.postedDate()).isEqualTo(postedDate);
        assertThat(mapped.responseItem().getType()).isEqualTo(AMENDMENT.responseType());
        assertThat(mapped.responseItem().getAmount()).isNull();
        assertThat(mapped.responseItem().getPostedDetails().getPostedDate()).isEqualTo(postedDate.toLocalDate());
        assertThat(mapped.responseItem().getPostedDetails().getPostedBy()).isEqualTo("AMENDUSR");
        assertThat(mapped.responseItem().getPostedDetails().getPostedByName()).isEqualTo("Amend User");

        AmendmentTypeCommon details = (AmendmentTypeCommon) mapped.responseItem().getDetails();
        assertThat(details.getAttributeName()).isEqualTo("Hold Pay Out");
        assertThat(details.getOldValue()).isEqualTo("false");
        assertThat(details.getNewValue()).isEqualTo("true");
    }

    @Test
    void toHistoryItem_mapsNoteDetails() {
        // Arrange
        LocalDateTime postedDate = LocalDateTime.of(2026, 1, 30, 9, 15);
        MinorCreditorNoteHistoryProjection projection = mock(MinorCreditorNoteHistoryProjection.class);
        when(projection.getNoteId()).thenReturn(12L);
        when(projection.getPostedDate()).thenReturn(postedDate);
        when(projection.getPostedBy()).thenReturn("NOTEUSR");
        when(projection.getPostedByName()).thenReturn("Note User");
        when(projection.getNoteText()).thenReturn("Review creditor");

        // Act
        MinorCreditorHistoryItem mapped = mapper.toHistoryItem(projection);

        // Assert
        assertThat(mapped.sourceType()).isEqualTo(NOTE);
        assertThat(mapped.sourceId()).isEqualTo(12L);
        assertThat(mapped.postedDate()).isEqualTo(postedDate);
        assertThat(mapped.responseItem().getType()).isEqualTo(NOTE.responseType());
        assertThat(mapped.responseItem().getAmount()).isNull();
        assertThat(mapped.responseItem().getPostedDetails().getPostedDate()).isEqualTo(postedDate.toLocalDate());

        NoteDetailsHistory details = (NoteDetailsHistory) mapped.responseItem().getDetails();
        assertThat(details.getNoteText()).isEqualTo("Review creditor");
    }

    @Test
    void toHistoryItem_mapsTransactionDetails() {
        // Arrange
        LocalDateTime postedDate = LocalDateTime.of(2026, 1, 29, 8, 0);
        LocalDateTime statusDate = LocalDateTime.of(2026, 1, 29, 8, 30);
        MinorCreditorTransactionHistoryProjection projection =
            mock(MinorCreditorTransactionHistoryProjection.class);
        when(projection.getCreditorTransactionId()).thenReturn(13L);
        when(projection.getPostedDate()).thenReturn(postedDate);
        when(projection.getPostedBy()).thenReturn("PAYUSR");
        when(projection.getPostedByName()).thenReturn("Payment User");
        when(projection.getTransactionType()).thenReturn("PAYMNT");
        when(projection.getTransactionAmount()).thenReturn(BigDecimal.valueOf(42L));
        when(projection.getPaymentReference()).thenReturn("PMT001");
        when(projection.getStatus()).thenReturn("C");
        when(projection.getStatusDate()).thenReturn(statusDate);
        when(projection.getAssociatedRecordType()).thenReturn("defendant_accounts");
        when(projection.getAssociatedRecordId()).thenReturn("70000000000000");
        when(projection.getAccountNumber()).thenReturn("HOLD1234");
        when(projection.getDefendantAccountNumber()).thenReturn("DEF123456");
        when(projection.getDefendantAccountId()).thenReturn(70000000000000L);

        // Act
        MinorCreditorHistoryItem mapped = mapper.toHistoryItem(projection);

        // Assert
        assertThat(mapped.sourceType()).isEqualTo(FINANCIAL);
        assertThat(mapped.sourceId()).isEqualTo(13L);
        assertThat(mapped.responseItem().getType()).isEqualTo(FINANCIAL.responseType());
        assertThat(mapped.responseItem().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(42L));

        CreditorTransactionDetailsHistory details =
            (CreditorTransactionDetailsHistory) mapped.responseItem().getDetails();
        assertThat(details.getTransactionType().getTransactionType().getValue()).isEqualTo("PAYMNT");
        assertThat(details.getTransactionType().getTransactionTypeDisplayName()).isEqualTo("PAYMNT");
        assertThat(details.getPaymentReference()).isEqualTo("PMT001");
        assertThat(details.getStatus().getCreditorTransactionStatus().getValue()).isEqualTo("C");
        assertThat(details.getStatus().getCreditorTransactionStatusDisplayName()).isEqualTo("C");
        assertThat(details.getStatusDate()).isEqualTo(statusDate);
        assertThat(details.getAssociatedRecordType()).isEqualTo("defendant_accounts");
        assertThat(details.getAssociatedRecordId()).isEqualTo("70000000000000");
        assertThat(details.getAccountNumber()).isEqualTo("HOLD1234");
        assertThat(details.getDefendantAccountNumber()).isEqualTo("DEF123456");
        assertThat(details.getDefendantAccountId()).isEqualTo(70000000000000L);
    }

    @Test
    void toHistoryItem_mapsNullTransactionOptionals() {
        // Arrange
        LocalDateTime postedDate = LocalDateTime.of(2026, 1, 29, 8, 0);
        MinorCreditorTransactionHistoryProjection projection =
            mock(MinorCreditorTransactionHistoryProjection.class);
        when(projection.getCreditorTransactionId()).thenReturn(13L);
        when(projection.getPostedDate()).thenReturn(postedDate);
        when(projection.getTransactionType()).thenReturn("PAYMNT");
        when(projection.getTransactionAmount()).thenReturn(BigDecimal.valueOf(42L));
        when(projection.getDefendantAccountId()).thenReturn(null);

        // Act
        MinorCreditorHistoryItem mapped = mapper.toHistoryItem(projection);

        // Assert
        CreditorTransactionDetailsHistory details =
            (CreditorTransactionDetailsHistory) mapped.responseItem().getDetails();
        assertThat(details.getPaymentReference()).isNull();
        assertThat(details.getStatus()).isNull();
        assertThat(details.getStatusDate()).isNull();
        assertThat(details.getAssociatedRecordType()).isNull();
        assertThat(details.getAssociatedRecordId()).isNull();
        assertThat(details.getAccountNumber()).isNull();
        assertThat(details.getDefendantAccountNumber()).isNull();
        assertThat(details.getDefendantAccountId()).isNull();
    }

    @Test
    void toReferenceObjects_mapsDisplayNamesFromCodes() {
        // Arrange
        String transactionType = "PAYMNT";
        String status = "C";

        // Act
        CreditorTransactionTypeReferenceCommon typeReference =
            mapper.toCreditorTransactionType(transactionType);
        CreditorTransactionStatusReferenceCommon statusReference =
            mapper.toCreditorTransactionStatus(status);

        // Assert
        assertThat(typeReference.getTransactionType().getValue()).isEqualTo(transactionType);
        assertThat(typeReference.getTransactionTypeDisplayName()).isEqualTo(transactionType);
        assertThat(statusReference.getCreditorTransactionStatus().getValue()).isEqualTo(status);
        assertThat(statusReference.getCreditorTransactionStatusDisplayName()).isEqualTo(status);
    }

    @Test
    void toPostedDetails_mapsDateComponentAndPostedUser() {
        // Arrange
        LocalDateTime postedDate = LocalDateTime.of(2026, 1, 31, 23, 59);

        // Act
        PostedDetailsCommon postedDetails = mapper.toPostedDetails(postedDate, "POSTUSR", "Post User");

        // Assert
        assertThat(postedDetails.getPostedDate()).isEqualTo(postedDate.toLocalDate());
        assertThat(postedDetails.getPostedBy()).isEqualTo("POSTUSR");
        assertThat(postedDetails.getPostedByName()).isEqualTo("Post User");
    }
}
