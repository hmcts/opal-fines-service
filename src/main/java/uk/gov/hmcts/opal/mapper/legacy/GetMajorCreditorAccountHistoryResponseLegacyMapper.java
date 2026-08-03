package uk.gov.hmcts.opal.mapper.legacy;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse.LegacyCreditorTransactionStatusReference;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse.LegacyCreditorTransactionTypeReference;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse.LegacyMajorCreditorHistoryDetails;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHistoryLegacyResponse.LegacyMajorCreditorHistoryItem;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.dto.response.GetMajorCreditorHistoryResponse;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionDetailsHistory;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.GetMajorCreditorHistory200Response;
import uk.gov.hmcts.opal.generated.model.MajorCreditorHistoryItemHistory;
import uk.gov.hmcts.opal.generated.model.MajorCreditorHistoryItemHistoryDetails;
import uk.gov.hmcts.opal.generated.model.NoteDetailsHistory;
import uk.gov.hmcts.opal.generated.model.PostedDetailsCommon;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GetMajorCreditorAccountHistoryResponseLegacyMapper {

    default GetMajorCreditorHistoryResponse toOpal(GetMajorCreditorAccountHistoryLegacyResponse legacy) {
        return GetMajorCreditorHistoryResponse.builder()
            .payload(toPayload(legacy))
            .version(toVersion(legacy))
            .build();
    }

    default GetMajorCreditorHistory200Response toPayload(GetMajorCreditorAccountHistoryLegacyResponse legacy) {
        return new GetMajorCreditorHistory200Response()
            .historyItems(Optional.ofNullable(legacy)
                .map(GetMajorCreditorAccountHistoryLegacyResponse::getHistoryItems)
                .orElse(List.of()).stream()
                .sorted(legacyHistoryItemComparator())
                .map(this::toHistoryItem)
                .toList());
    }

    @Mapping(target = "postedDetails", source = "postedDetails")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "details", expression = "java(mapDetails(item.getType(), item.getDetails()))")
    MajorCreditorHistoryItemHistory toHistoryItem(LegacyMajorCreditorHistoryItem item);

    CreditorTransactionDetailsHistory toDetails(LegacyMajorCreditorHistoryDetails details);

    PostedDetailsCommon toPostedDetails(LegacyPostedDetails postedDetails);

    CreditorTransactionTypeReferenceCommon toTransactionTypeReference(
        LegacyCreditorTransactionTypeReference legacy);

    CreditorTransactionStatusReferenceCommon toTransactionStatusReference(
        LegacyCreditorTransactionStatusReference legacy);

    default BigInteger toVersion(GetMajorCreditorAccountHistoryLegacyResponse legacy) {
        return Optional.ofNullable(legacy)
            .map(GetMajorCreditorAccountHistoryLegacyResponse::getVersion)
            .map(BigInteger::valueOf)
            .orElse(BigInteger.ONE);
    }

    default MajorCreditorHistoryItemHistoryDetails mapDetails(
        String type,
        LegacyMajorCreditorHistoryDetails details
    ) {
        if (type == null || details == null) {
            return null;
        }

        HistoryItemType historyItemType = HistoryItemType.fromValue(type);
        return switch (historyItemType) {
            case FINANCIAL -> toDetails(details);
            case NOTE -> new NoteDetailsHistory().noteText(details.getNoteText());
            default -> throw new IllegalArgumentException("Unsupported major creditor history item type: " + type);
        };
    }

    default MajorCreditorHistoryItemHistory.TypeEnum toHistoryItemType(String type) {
        return type == null ? null : MajorCreditorHistoryItemHistory.TypeEnum.fromValue(type);
    }

    default CreditorTransactionTypeReferenceCommon.TransactionTypeEnum toTransactionTypeEnum(String transactionType) {
        return transactionType == null ? null
            : CreditorTransactionTypeReferenceCommon.TransactionTypeEnum.fromValue(transactionType);
    }

    default CreditorTransactionStatusReferenceCommon.CreditorTransactionStatusEnum toTransactionStatusEnum(
        String status) {
        return status == null ? null
            : CreditorTransactionStatusReferenceCommon.CreditorTransactionStatusEnum.fromValue(status);
    }

    default LocalDate toLocalDate(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toLocalDate();
    }

    default Comparator<LegacyMajorCreditorHistoryItem> legacyHistoryItemComparator() {
        return Comparator.comparing(
                GetMajorCreditorAccountHistoryResponseLegacyMapper::postedDate,
                Comparator.nullsLast(Comparator.reverseOrder())
            )
            .thenComparing(LegacyMajorCreditorHistoryItem::getType, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(
                GetMajorCreditorAccountHistoryResponseLegacyMapper::paymentReference,
                Comparator.nullsLast(Comparator.naturalOrder())
            )
            .thenComparing(
                GetMajorCreditorAccountHistoryResponseLegacyMapper::associatedRecordId,
                Comparator.nullsLast(Comparator.naturalOrder())
            );
    }

    private static LocalDateTime postedDate(LegacyMajorCreditorHistoryItem item) {
        return item == null || item.getPostedDetails() == null ? null : item.getPostedDetails().getPostedDate();
    }

    private static String paymentReference(LegacyMajorCreditorHistoryItem item) {
        return item == null || item.getDetails() == null ? null : item.getDetails().getPaymentReference();
    }

    private static String associatedRecordId(LegacyMajorCreditorHistoryItem item) {
        return item == null || item.getDetails() == null ? null : item.getDetails().getAssociatedRecordId();
    }
}
