package uk.gov.hmcts.opal.mapper.legacy;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse.LegacyCreditorTransactionStatusReference;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse.LegacyCreditorTransactionTypeReference;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse.LegacyMinorCreditorAccountHistoryItem;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse.LegacyMinorCreditorHistoryDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryResponse.LegacyMinorCreditorHistoryPostedDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHistoryRequest;
import uk.gov.hmcts.opal.dto.response.GetMinorCreditorHistoryResponse;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryFilters;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItem;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType;
import uk.gov.hmcts.opal.generated.model.AmendmentTypeCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionDetailsHistory;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.GetMinorCreditorHistory200Response;
import uk.gov.hmcts.opal.generated.model.MinorCreditorHistoryItemHistory;
import uk.gov.hmcts.opal.generated.model.MinorCreditorHistoryItemHistoryDetails;
import uk.gov.hmcts.opal.generated.model.NoteDetailsHistory;
import uk.gov.hmcts.opal.generated.model.PostedDetailsCommon;

@Component
public class LegacyMinorCreditorHistoryMapper {

    public LegacyGetMinorCreditorAccountHistoryRequest toLegacyRequest(
        Long minorCreditorAccountId,
        MinorCreditorHistoryFilters filters) {

        return LegacyGetMinorCreditorAccountHistoryRequest.builder()
            .creditorAccountId(String.valueOf(minorCreditorAccountId))
            .fromDate(filters == null ? null : filters.dateFrom())
            .toDate(filters == null ? null : filters.dateTo())
            .itemTypes(toLegacyItemTypes(filters))
            .build();
    }

    public GetMinorCreditorHistoryResponse toOpal(LegacyGetMinorCreditorAccountHistoryResponse legacy) {
        List<MinorCreditorHistoryItem> historyItems = toHistoryItems(legacy);

        return GetMinorCreditorHistoryResponse.builder()
            .payload(new GetMinorCreditorHistory200Response().historyItems(historyItems.stream()
                .sorted(MinorCreditorHistoryItem.ORDERING)
                .map(MinorCreditorHistoryItem::responseItem)
                .toList()))
            .build();
    }

    private List<MinorCreditorHistoryItem> toHistoryItems(LegacyGetMinorCreditorAccountHistoryResponse legacy) {
        List<LegacyMinorCreditorAccountHistoryItem> historyItems = Optional.ofNullable(legacy)
            .map(LegacyGetMinorCreditorAccountHistoryResponse::getHistoryItems)
            .orElse(List.of());

        return IntStream.range(0, historyItems.size())
            .mapToObj(index -> toHistoryItem(historyItems.get(index), (long) index))
            .toList();
    }

    private MinorCreditorHistoryItem toHistoryItem(LegacyMinorCreditorAccountHistoryItem item, Long sequence) {
        MinorCreditorHistoryItemType sourceType = toHistoryItemType(item.getType());
        LocalDate postedDate = Optional.ofNullable(item.getPostedDetails())
            .map(LegacyMinorCreditorHistoryPostedDetails::getPostedDate)
            .orElse(null);

        return new MinorCreditorHistoryItem(
            sourceType,
            sequence,
            postedDate == null ? null : postedDate.atStartOfDay(),
            new MinorCreditorHistoryItemHistory()
                .postedDetails(toPostedDetails(item.getPostedDetails()))
                .type(sourceType.responseType())
                .details(toDetails(sourceType, item.getDetails()))
                .amount(sourceType == MinorCreditorHistoryItemType.FINANCIAL ? item.getAmount() : null)
        );
    }

    private PostedDetailsCommon toPostedDetails(LegacyMinorCreditorHistoryPostedDetails postedDetails) {
        if (postedDetails == null) {
            return null;
        }

        return new PostedDetailsCommon()
            .postedDate(postedDetails.getPostedDate())
            .postedBy(postedDetails.getPostedBy())
            .postedByName(postedDetails.getPostedByName());
    }

    private MinorCreditorHistoryItemHistoryDetails toDetails(
        MinorCreditorHistoryItemType sourceType,
        LegacyMinorCreditorHistoryDetails details) {

        if (details == null) {
            return null;
        }

        return switch (sourceType) {
            case AMENDMENT -> new AmendmentTypeCommon()
                .attributeName(details.getAttributeName())
                .oldValue(details.getOldValue())
                .newValue(details.getNewValue());
            case FINANCIAL -> new CreditorTransactionDetailsHistory()
                .transactionType(toCreditorTransactionType(details.getTransactionType()))
                .paymentReference(details.getPaymentReference())
                .status(toCreditorTransactionStatus(details.getStatus()))
                .statusDate(details.getStatusDate())
                .associatedRecordType(details.getAssociatedRecordType())
                .associatedRecordId(details.getAssociatedRecordId())
                .accountNumber(details.getAccountNumber())
                .defendantAccountNumber(details.getDefendantAccountNumber())
                .defendantAccountId(details.getDefendantAccountId());
            case NOTE -> new NoteDetailsHistory().noteText(details.getNoteText());
        };
    }

    private CreditorTransactionTypeReferenceCommon toCreditorTransactionType(
        LegacyCreditorTransactionTypeReference transactionType) {

        if (transactionType == null || transactionType.getTransactionType() == null) {
            return null;
        }

        return new CreditorTransactionTypeReferenceCommon()
            .transactionType(CreditorTransactionTypeReferenceCommon.TransactionTypeEnum.fromValue(
                transactionType.getTransactionType()))
            .transactionTypeDisplayName(transactionType.getTransactionType());
    }

    private CreditorTransactionStatusReferenceCommon toCreditorTransactionStatus(
        LegacyCreditorTransactionStatusReference status) {

        if (status == null || status.getCreditorTransactionStatus() == null) {
            return null;
        }

        return new CreditorTransactionStatusReferenceCommon()
            .creditorTransactionStatus(
                CreditorTransactionStatusReferenceCommon.CreditorTransactionStatusEnum.fromValue(
                    status.getCreditorTransactionStatus()))
            .creditorTransactionStatusDisplayName(status.getCreditorTransactionStatus());
    }

    private MinorCreditorHistoryItemType toHistoryItemType(String legacyType) {
        return switch (legacyType) {
            case "Amendment" -> MinorCreditorHistoryItemType.AMENDMENT;
            case "Financial" -> MinorCreditorHistoryItemType.FINANCIAL;
            case "Note" -> MinorCreditorHistoryItemType.NOTE;
            default -> throw new IllegalArgumentException("Unknown minor creditor history item type: " + legacyType);
        };
    }

    private List<String> toLegacyItemTypes(MinorCreditorHistoryFilters filters) {
        if (filters == null || filters.itemTypes().size() == MinorCreditorHistoryItemType.values().length) {
            return null;
        }

        return Arrays.stream(MinorCreditorHistoryItemType.values())
            .filter(filters::includes)
            .map(this::toLegacyItemType)
            .toList();
    }

    private String toLegacyItemType(MinorCreditorHistoryItemType itemType) {
        return switch (itemType) {
            case AMENDMENT -> "Amendment";
            case FINANCIAL -> "Financial";
            case NOTE -> "Note";
        };
    }
}
