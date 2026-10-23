package uk.gov.hmcts.opal.mapper.legacy;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
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
import uk.gov.hmcts.opal.generated.model.GetMajorCreditorHistory200Response;
import uk.gov.hmcts.opal.generated.model.MajorCreditorHistoryItemHistory;
import uk.gov.hmcts.opal.generated.model.MajorCreditorHistoryItemHistoryDetails;
import uk.gov.hmcts.opal.generated.model.NoteDetailsHistory;
import uk.gov.hmcts.opal.generated.model.PostedDetailsCommon;
import uk.gov.hmcts.opal.util.DateTimeUtils;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = DateTimeUtils.class)
public interface LegacyMajorCreditorHistoryMapper {

    default GetMajorCreditorHistoryResponse toOpal(LegacyMajorCreditorHistoryResponse legacy) {
        return GetMajorCreditorHistoryResponse.builder()
            .payload(toPayload(legacy))
            .version(toVersion(legacy))
            .build();
    }

    default GetMajorCreditorHistory200Response toPayload(LegacyMajorCreditorHistoryResponse legacy) {
        return new GetMajorCreditorHistory200Response()
            .historyItems(Optional.ofNullable(legacy)
                .map(LegacyMajorCreditorHistoryResponse::getHistoryItems)
                .orElse(List.of()).stream()
                .sorted(legacyHistoryItemComparator())
                .map(this::toHistoryItem)
                .toList());
    }

    @Mapping(target = "postedDetails", source = "postedDetails")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "details", expression = "java(mapDetails(item.getType(), item.getDetails()))")
    MajorCreditorHistoryItemHistory toHistoryItem(HistoryItem item);

    @Mapping(target = "statusDate", expression = "java(DateTimeUtils.startOf(details.getStatusDate()))")
    CreditorTransactionDetailsHistory toDetails(HistoryDetails details);

    PostedDetailsCommon toPostedDetails(LegacyPostedDetails postedDetails);

    CreditorTransactionTypeReferenceCommon toTransactionTypeReference(TransactionTypeReference legacy);

    CreditorTransactionStatusReferenceCommon toTransactionStatusReference(TransactionStatusReference legacy);

    default BigInteger toVersion(LegacyMajorCreditorHistoryResponse legacy) {
        return Optional.ofNullable(legacy)
            .map(LegacyMajorCreditorHistoryResponse::getVersion)
            .orElse(BigInteger.ONE);
    }

    default MajorCreditorHistoryItemHistoryDetails mapDetails(String type, HistoryDetails details) {
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

    default Comparator<HistoryItem> legacyHistoryItemComparator() {
        return Comparator.comparing(
                LegacyMajorCreditorHistoryMapper::postedDate,
                Comparator.nullsLast(Comparator.reverseOrder())
            )
            .thenComparing(HistoryItem::getType, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(
                LegacyMajorCreditorHistoryMapper::paymentReference,
                Comparator.nullsLast(Comparator.naturalOrder())
            )
            .thenComparing(
                LegacyMajorCreditorHistoryMapper::associatedRecordId,
                Comparator.nullsLast(Comparator.naturalOrder())
            );
    }

    private static LocalDateTime postedDate(HistoryItem item) {
        return item == null || item.getPostedDetails() == null ? null : item.getPostedDetails().getPostedDate();
    }

    private static String paymentReference(HistoryItem item) {
        return item == null || item.getDetails() == null ? null : item.getDetails().getPaymentReference();
    }

    private static String associatedRecordId(HistoryItem item) {
        return item == null || item.getDetails() == null ? null : item.getDetails().getAssociatedRecordId();
    }
}
