package uk.gov.hmcts.opal.mapper.legacy;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
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
                .map(this::toHistoryItem)
                .toList());
    }

    @Mapping(target = "postedDetails", source = "postedDetails")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "details", source = "details")
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
}
