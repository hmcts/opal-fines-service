package uk.gov.hmcts.opal.service.opal.history.majorcreditor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import uk.gov.hmcts.opal.dto.history.AccountHistoryCreditorTransactionDetails;
import uk.gov.hmcts.opal.dto.history.AccountHistoryDetails;
import uk.gov.hmcts.opal.dto.history.AccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.AccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.AccountHistoryItemType;
import uk.gov.hmcts.opal.dto.history.AccountHistoryPostedDetails;
import uk.gov.hmcts.opal.dto.history.AccountHistoryResult;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionDetailsHistory;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.GetMajorCreditorHistory200Response;
import uk.gov.hmcts.opal.generated.model.MajorCreditorHistoryItemHistory;
import uk.gov.hmcts.opal.generated.model.PostedDetailsCommon;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorTransactionHistoryProjection;

public final class MajorCreditorHistoryModelAdapter {

    private MajorCreditorHistoryModelAdapter() {
    }

    public static AccountHistoryFilter toCoreFilter(LocalDate dateFrom, LocalDate dateTo, List<String> itemTypes) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom must be on or before dateTo");
        }

        return AccountHistoryFilter.builder()
            .dateFrom(dateFrom)
            .dateTo(dateTo)
            .itemTypes(toCoreItemTypes(itemTypes))
            .build();
    }

    public static GetMajorCreditorHistory200Response toGeneratedResponse(AccountHistoryResult historyResult) {
        return new GetMajorCreditorHistory200Response()
            .historyItems(historyResult.getHistoryItems().stream()
                .map(MajorCreditorHistoryModelAdapter::toGeneratedItem)
                .toList());
    }

    public static MajorCreditorHistoryItemHistory toGeneratedItem(AccountHistoryItem item) {
        return new MajorCreditorHistoryItemHistory()
            .postedDetails(toGeneratedPostedDetails(item.getPostedDetails()))
            .type(toGeneratedItemType(item.getType()))
            .details(toGeneratedDetails(item.getDetails()))
            .amount(item.getAmount());
    }

    public static AccountHistoryItem toCoreItem(MinorCreditorTransactionHistoryProjection projection) {
        return AccountHistoryItem.builder()
            .postedDetails(AccountHistoryPostedDetails.builder()
                .postedDate(projection.getPostedDate())
                .postedBy(projection.getPostedBy())
                .postedByName(projection.getPostedByName())
                .build())
            .type(AccountHistoryItemType.FINANCIAL)
            .details(AccountHistoryCreditorTransactionDetails.builder()
                .transactionType(projection.getTransactionType())
                .paymentReference(projection.getPaymentReference())
                .status(projection.getStatus())
                .statusDate(projection.getStatusDate())
                .associatedRecordType(projection.getAssociatedRecordType())
                .associatedRecordId(projection.getAssociatedRecordId())
                .accountNumber(projection.getAccountNumber())
                .defendantAccountNumber(projection.getDefendantAccountNumber())
                .defendantAccountId(projection.getDefendantAccountId())
                .build())
            .amount(projection.getTransactionAmount())
            .eventDateTime(projection.getPostedDate())
            .sourceId(projection.getCreditorTransactionId())
            .build();
    }

    private static List<AccountHistoryItemType> toCoreItemTypes(List<String> itemTypes) {
        List<HistoryItemType> historyItemTypes = queryValues(itemTypes).stream()
            .map(HistoryItemType::fromValue)
            .toList();

        validateSupportedItemTypes(historyItemTypes);

        return historyItemTypes.stream()
            .map(MajorCreditorHistoryModelAdapter::toCoreItemType)
            .toList();
    }

    private static List<String> queryValues(List<String> itemTypes) {
        if (itemTypes == null) {
            return List.of();
        }

        return itemTypes.stream()
            .flatMap(rawValue -> rawValue == null ? Stream.of("") : Arrays.stream(rawValue.split(",", -1)))
            .map(String::trim)
            .filter(itemType -> !itemType.isEmpty())
            .toList();
    }

    private static void validateSupportedItemTypes(List<HistoryItemType> itemTypes) {
        List<HistoryItemType> unsupportedItemTypes = itemTypes.stream()
            .filter(itemType -> itemType != HistoryItemType.FINANCIAL && itemType != HistoryItemType.NOTE)
            .toList();

        if (!unsupportedItemTypes.isEmpty()) {
            throw new IllegalArgumentException("itemTypes must contain only financial, note");
        }
    }

    private static AccountHistoryItemType toCoreItemType(HistoryItemType type) {
        return switch (type) {
            case FINANCIAL -> AccountHistoryItemType.FINANCIAL;
            case NOTE -> AccountHistoryItemType.NOTE;
            default -> throw new IllegalArgumentException("Unsupported major creditor history item type: " + type);
        };
    }

    private static PostedDetailsCommon toGeneratedPostedDetails(AccountHistoryPostedDetails postedDetails) {
        if (postedDetails == null) {
            return null;
        }

        return new PostedDetailsCommon()
            .postedDate(postedDetails.getPostedDate() == null ? null : postedDetails.getPostedDate().toLocalDate())
            .postedBy(postedDetails.getPostedBy())
            .postedByName(postedDetails.getPostedByName());
    }

    private static MajorCreditorHistoryItemHistory.TypeEnum toGeneratedItemType(AccountHistoryItemType type) {
        if (type != AccountHistoryItemType.FINANCIAL) {
            throw new IllegalArgumentException("Unsupported major creditor history item type: " + type);
        }

        return MajorCreditorHistoryItemHistory.TypeEnum.FINANCIAL;
    }

    private static CreditorTransactionDetailsHistory toGeneratedDetails(AccountHistoryDetails details) {
        if (details instanceof AccountHistoryCreditorTransactionDetails transactionDetails) {
            return new CreditorTransactionDetailsHistory()
                .transactionType(toCreditorTransactionType(transactionDetails.getTransactionType()))
                .paymentReference(transactionDetails.getPaymentReference())
                .status(toCreditorTransactionStatus(transactionDetails.getStatus()))
                .statusDate(transactionDetails.getStatusDate())
                .associatedRecordType(transactionDetails.getAssociatedRecordType())
                .associatedRecordId(transactionDetails.getAssociatedRecordId())
                .accountNumber(transactionDetails.getAccountNumber())
                .defendantAccountNumber(transactionDetails.getDefendantAccountNumber())
                .defendantAccountId(transactionDetails.getDefendantAccountId());
        }

        throw new IllegalArgumentException(
            "Unsupported major creditor history details type: " + details.getClass().getName()
        );
    }

    private static CreditorTransactionTypeReferenceCommon toCreditorTransactionType(String transactionType) {
        return new CreditorTransactionTypeReferenceCommon()
            .transactionType(CreditorTransactionTypeReferenceCommon.TransactionTypeEnum.fromValue(transactionType))
            .transactionTypeDisplayName(transactionType);
    }

    private static CreditorTransactionStatusReferenceCommon toCreditorTransactionStatus(String status) {
        if (status == null) {
            return null;
        }

        return new CreditorTransactionStatusReferenceCommon()
            .creditorTransactionStatus(
                CreditorTransactionStatusReferenceCommon.CreditorTransactionStatusEnum.fromValue(status))
            .creditorTransactionStatusDisplayName(status);
    }
}
