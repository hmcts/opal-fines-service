package uk.gov.hmcts.opal.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItem;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType;
import uk.gov.hmcts.opal.generated.model.AmendmentTypeCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionDetailsHistory;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.MinorCreditorHistoryItemHistory;
import uk.gov.hmcts.opal.generated.model.MinorCreditorHistoryItemHistoryDetails;
import uk.gov.hmcts.opal.generated.model.NoteDetailsHistory;
import uk.gov.hmcts.opal.generated.model.PostedDetailsCommon;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorAmendmentHistoryProjection;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorNoteHistoryProjection;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorTransactionHistoryProjection;

@Component
public class MinorCreditorHistoryItemMapper {

    public MinorCreditorHistoryItem toHistoryItem(MinorCreditorAmendmentHistoryProjection projection) {
        return buildHistoryItem(
            MinorCreditorHistoryItemType.AMENDMENT,
            projection.getAmendmentId(),
            projection.getPostedDate(),
            projection.getPostedBy(),
            projection.getPostedByName(),
            new AmendmentTypeCommon()
                .attributeName(projection.getAttributeName())
                .oldValue(projection.getOldValue())
                .newValue(projection.getNewValue()),
            null
        );
    }

    public MinorCreditorHistoryItem toHistoryItem(MinorCreditorNoteHistoryProjection projection) {
        return buildHistoryItem(
            MinorCreditorHistoryItemType.NOTE,
            projection.getNoteId(),
            projection.getPostedDate(),
            projection.getPostedBy(),
            projection.getPostedByName(),
            new NoteDetailsHistory().noteText(projection.getNoteText()),
            null
        );
    }

    public MinorCreditorHistoryItem toHistoryItem(MinorCreditorTransactionHistoryProjection projection) {
        return buildHistoryItem(
            MinorCreditorHistoryItemType.FINANCIAL,
            projection.getCreditorTransactionId(),
            projection.getPostedDate(),
            projection.getPostedBy(),
            projection.getPostedByName(),
            new CreditorTransactionDetailsHistory()
                .transactionType(toCreditorTransactionType(projection.getTransactionType()))
                .paymentReference(projection.getPaymentReference())
                .status(toCreditorTransactionStatus(projection.getStatus()))
                .statusDate(projection.getStatusDate())
                .associatedRecordType(projection.getAssociatedRecordType())
                .associatedRecordId(projection.getAssociatedRecordId())
                .accountNumber(projection.getAccountNumber())
                .defendantAccountNumber(projection.getDefendantAccountNumber())
                .defendantAccountId(projection.getDefendantAccountId()),
            projection.getTransactionAmount()
        );
    }

    public PostedDetailsCommon toPostedDetails(LocalDateTime postedDate, String postedBy, String postedByName) {
        return new PostedDetailsCommon()
            .postedDate(postedDate.toLocalDate())
            .postedBy(postedBy)
            .postedByName(postedByName);
    }

    public CreditorTransactionTypeReferenceCommon toCreditorTransactionType(String transactionType) {
        return new CreditorTransactionTypeReferenceCommon()
            .transactionType(CreditorTransactionTypeReferenceCommon.TransactionTypeEnum.fromValue(transactionType))
            .transactionTypeDisplayName(transactionType);
    }

    public CreditorTransactionStatusReferenceCommon toCreditorTransactionStatus(String status) {
        if (status == null) {
            return null;
        }
        return new CreditorTransactionStatusReferenceCommon()
            .creditorTransactionStatus(
                CreditorTransactionStatusReferenceCommon.CreditorTransactionStatusEnum.fromValue(status))
            .creditorTransactionStatusDisplayName(status);
    }

    private MinorCreditorHistoryItem buildHistoryItem(
        MinorCreditorHistoryItemType sourceType,
        Long sourceId,
        LocalDateTime postedDate,
        String postedBy,
        String postedByName,
        MinorCreditorHistoryItemHistoryDetails details,
        BigDecimal amount) {

        return new MinorCreditorHistoryItem(
            sourceType,
            sourceId,
            postedDate,
            new MinorCreditorHistoryItemHistory()
                .postedDetails(toPostedDetails(postedDate, postedBy, postedByName))
                .type(sourceType.responseType())
                .details(details)
                .amount(amount)
        );
    }
}
