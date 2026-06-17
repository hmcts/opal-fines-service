package uk.gov.hmcts.opal.mapper.legacy;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.CourtReferenceDto;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.history.AmendmentDetails;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryDetails;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.history.DefendantTransactionDetails;
import uk.gov.hmcts.opal.dto.history.DefendantTransactionStatusReference;
import uk.gov.hmcts.opal.dto.history.DefendantTransactionTypeReference;
import uk.gov.hmcts.opal.dto.history.EnforcementDetails;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.dto.history.NoteDetails;
import uk.gov.hmcts.opal.dto.history.PaymentMethodReference;
import uk.gov.hmcts.opal.dto.history.PaymentTermsDetails;
import uk.gov.hmcts.opal.dto.history.WriteOffTypeReference;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.dto.legacy.common.CourtReference;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LegacyDefendantAccountHistoryResponseMapper {

    default DefendantAccountHistoryResponse toOpal(LegacyGetDefendantAccountHistoryResponse legacy) {
        if (legacy == null) {
            return DefendantAccountHistoryResponse.builder()
                .version(BigInteger.ONE)
                .historyItems(List.of())
                .build();
        }

        return DefendantAccountHistoryResponse.builder()
            .version(Optional.ofNullable(legacy.getVersion()).map(BigInteger::valueOf).orElse(BigInteger.ONE))
            .historyItems(Optional.ofNullable(legacy.getHistoryItems()).orElse(List.of()).stream()
                .map(this::toHistoryItem)
                .toList())
            .build();
    }

    @Mapping(target = "postedDetails", source = "postedDetails")
    @Mapping(target = "type",
        expression = "java(uk.gov.hmcts.opal.dto.history.HistoryItemType.fromValue(item.getType()))")
    @Mapping(target = "details", expression = "java(mapDetails(item.getType(), item.getDetails()))")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "eventDateTime",
        expression = "java(item.getPostedDetails() == null ? null : item.getPostedDetails().getPostedDate())")
    DefendantAccountHistoryItem toHistoryItem(
        LegacyGetDefendantAccountHistoryResponse.LegacyDefendantAccountHistoryItem item);

    PostedDetails toPostedDetails(LegacyPostedDetails postedDetails);

    default DefendantAccountHistoryDetails mapDetails(
        String typeValue,
        LegacyGetDefendantAccountHistoryResponse.LegacyDefendantAccountHistoryDetails details
    ) {
        if (details == null || typeValue == null) {
            return null;
        }

        HistoryItemType type = HistoryItemType.fromValue(typeValue);

        return switch (type) {
            case AMENDMENT -> AmendmentDetails.builder()
                .attributeName(details.getAttributeName())
                .oldValue(details.getOldValue())
                .newValue(details.getNewValue())
                .build();
            case ENFORCEMENT -> EnforcementDetails.builder()
                .enforcementAction(details.getEnforcementAction())
                .daysInDefault(details.getDaysInDefault())
                .warrantNumber(details.getWarrantNumber())
                .hearingDate(details.getHearingDate())
                .hearingCourt(toCourtReferenceDto(details.getHearingCourt()))
                .caseNumber(details.getCaseNumber())
                .reason(details.getReason())
                .earliestDateOfRelease(details.getEarliestDateOfRelease())
                .build();
            case NOTE -> NoteDetails.builder()
                .noteText(details.getNoteText())
                .build();
            case PAYMENT_TERMS -> PaymentTermsDetails.builder()
                .daysInDefault(details.getDaysInDefault())
                .dateDaysInDefaultImposed(details.getDateDaysInDefaultImposed())
                .reasonForExtension(details.getReasonForExtension())
                .paymentTermsType(details.getPaymentTermsType() == null ? null
                    : PaymentTermsType.fromCode(details.getPaymentTermsType().getPaymentTermsTypeCode().name()))
                .effectiveDate(details.getEffectiveDate())
                .instalmentPeriod(details.getInstalmentPeriod() == null ? null
                    : InstalmentPeriod.fromCode(details.getInstalmentPeriod().getInstalmentPeriodCode().name()))
                .lumpSumAmount(details.getLumpSumAmount())
                .instalmentAmount(details.getInstalmentAmount())
                .build();
            case FINANCIAL -> DefendantTransactionDetails.builder()
                .transactionType(toTransactionTypeReference(details.getTransactionType()))
                .paymentMethod(toPaymentMethodReference(details.getPaymentMethod()))
                .paymentReference(details.getPaymentReference())
                .additionalInformation(details.getAdditionalInformation())
                .writeOff(toWriteOffTypeReference(details.getWriteOff()))
                .status(toTransactionStatusReference(details.getStatus()))
                .statusDate(details.getStatusDate())
                .associatedRecordType(details.getAssociatedRecordType())
                .associatedRecordId(details.getAssociatedRecordId())
                .accountNumber(details.getAccountNumber())
                .sendingCourt(details.getSendingCourt())
                .impositionDate(details.getImpositionDate())
                .impositionCode(details.getImpositionCode())
                .amountImposed(details.getAmountImposed())
                .build();
        };
    }

    default CourtReferenceDto toCourtReferenceDto(CourtReference courtReference) {
        if (courtReference == null) {
            return null;
        }

        return CourtReferenceDto.builder()
            .courtId(courtReference.getCourtId() == null ? null : Math.toIntExact(courtReference.getCourtId()))
            .courtName(courtReference.getCourtName())
            .build();
    }

    default DefendantTransactionTypeReference toTransactionTypeReference(
        LegacyGetDefendantAccountHistoryResponse.LegacyHistoryTypeReference legacy
    ) {
        if (legacy == null || legacy.getTransactionType() == null) {
            return null;
        }

        return DefendantTransactionTypeReference.builder()
            .transactionType(legacy.getTransactionType())
            .transactionTypeDisplayName(legacy.getTransactionTypeDisplayName())
            .build();
    }

    default PaymentMethodReference toPaymentMethodReference(
        LegacyGetDefendantAccountHistoryResponse.LegacyHistoryTypeReference legacy
    ) {
        if (legacy == null || legacy.getPaymentMethod() == null) {
            return null;
        }

        return PaymentMethodReference.builder()
            .paymentMethod(legacy.getPaymentMethod())
            .paymentMethodDisplayName(legacy.getPaymentMethodDisplayName())
            .build();
    }

    default WriteOffTypeReference toWriteOffTypeReference(
        LegacyGetDefendantAccountHistoryResponse.LegacyHistoryTypeReference legacy
    ) {
        if (legacy == null || legacy.getWriteOffType() == null) {
            return null;
        }

        return WriteOffTypeReference.builder()
            .writeOffType(legacy.getWriteOffType())
            .writeOffTypeDisplayName(legacy.getWriteOffTypeDisplayName())
            .build();
    }

    default DefendantTransactionStatusReference toTransactionStatusReference(
        LegacyGetDefendantAccountHistoryResponse.LegacyHistoryTypeReference legacy
    ) {
        if (legacy == null || legacy.getDefendantTransactionStatus() == null) {
            return null;
        }

        return DefendantTransactionStatusReference.builder()
            .defendantTransactionStatus(legacy.getDefendantTransactionStatus())
            .defendantTransactionStatusDisplayName(legacy.getDefendantTransactionStatusDisplayName())
            .build();
    }
}
