package uk.gov.hmcts.opal.mapper.legacy;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
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
import uk.gov.hmcts.opal.generated.model.GetMajorCreditorHistory200Response;
import uk.gov.hmcts.opal.generated.model.MajorCreditorHistoryItemHistory;
import uk.gov.hmcts.opal.generated.model.MajorCreditorHistoryItemHistoryDetails;
import uk.gov.hmcts.opal.generated.model.NoteDetailsHistory;
import uk.gov.hmcts.opal.generated.model.PostedDetailsCommon;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MajorCreditorHistoryLegacyMapper {

    default GetMajorCreditorHistoryResponse toOpal(MajorCreditorHistoryLegacyResponse legacy) {
        return GetMajorCreditorHistoryResponse.builder()
            .payload(toPayload(legacy))
            .version(toVersion(legacy))
            .build();
    }

    default MajorCreditorHistoryLegacyRequest toLegacyRequest(
        Long majorCreditorAccountId,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<String> itemTypes
    ) {
        return MajorCreditorHistoryLegacyRequest.builder()
            .creditorAccountId(String.valueOf(majorCreditorAccountId))
            .fromDate(dateFrom)
            .toDate(dateTo)
            .itemTypes(toLegacyHistoryItemTypes(itemTypes))
            .build();
    }

    default GetMajorCreditorHistory200Response toPayload(MajorCreditorHistoryLegacyResponse legacy) {
        return new GetMajorCreditorHistory200Response()
            .historyItems(Optional.ofNullable(legacy)
                .map(MajorCreditorHistoryLegacyResponse::getHistoryItems)
                .orElse(List.of()).stream()
                .sorted(legacyHistoryItemComparator())
                .map(this::toHistoryItem)
                .toList());
    }

    @Mapping(target = "details", expression = "java(mapDetails(item.getType(), item.getDetails()))")
    MajorCreditorHistoryItemHistory toHistoryItem(LegacyHistoryItem item);

    CreditorTransactionDetailsHistory toDetails(LegacyHistoryDetails details);

    PostedDetailsCommon toPostedDetails(LegacyPostedDetails postedDetails);

    CreditorTransactionTypeReferenceCommon toTransactionTypeReference(
        LegacyTransactionTypeReference legacy);

    CreditorTransactionStatusReferenceCommon toTransactionStatusReference(
        LegacyTransactionStatusReference legacy);

    default BigInteger toVersion(MajorCreditorHistoryLegacyResponse legacy) {
        return Optional.ofNullable(legacy)
            .map(MajorCreditorHistoryLegacyResponse::getVersion)
            .map(BigInteger::valueOf)
            .orElse(BigInteger.ONE);
    }

    default MajorCreditorHistoryItemHistoryDetails mapDetails(
        String type,
        LegacyHistoryDetails details
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

    default List<String> toLegacyHistoryItemTypes(List<String> itemTypes) {
        List<String> legacyItemTypes = queryValues(itemTypes).stream()
            .map(HistoryItemType::fromValue)
            .map(HistoryItemType::getResponseValue)
            .toList();

        return legacyItemTypes.isEmpty() ? null : legacyItemTypes;
    }

    default List<String> queryValues(List<String> itemTypes) {
        if (itemTypes == null) {
            return List.of();
        }

        return itemTypes.stream()
            .flatMap(rawValue -> rawValue == null ? Stream.of("") : Arrays.stream(rawValue.split(",", -1)))
            .map(String::trim)
            .filter(itemType -> !itemType.isEmpty())
            .toList();
    }

    default Comparator<LegacyHistoryItem> legacyHistoryItemComparator() {
        return Comparator.comparing(
                MajorCreditorHistoryLegacyMapper::postedDate,
                Comparator.nullsLast(Comparator.reverseOrder())
            )
            .thenComparing(LegacyHistoryItem::getType, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(
                MajorCreditorHistoryLegacyMapper::paymentReference,
                Comparator.nullsLast(Comparator.naturalOrder())
            )
            .thenComparing(
                MajorCreditorHistoryLegacyMapper::associatedRecordId,
                Comparator.nullsLast(Comparator.naturalOrder())
            );
    }

    private static LocalDateTime postedDate(LegacyHistoryItem item) {
        return item == null || item.getPostedDetails() == null ? null : item.getPostedDetails().getPostedDate();
    }

    private static String paymentReference(LegacyHistoryItem item) {
        return item == null || item.getDetails() == null ? null : item.getDetails().getPaymentReference();
    }

    private static String associatedRecordId(LegacyHistoryItem item) {
        return item == null || item.getDetails() == null ? null : item.getDetails().getAssociatedRecordId();
    }
}
