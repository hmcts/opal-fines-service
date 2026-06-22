package uk.gov.hmcts.opal.mapper.history;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.DefendantTransactionDetails;
import uk.gov.hmcts.opal.dto.history.DefendantTransactionStatusReference;
import uk.gov.hmcts.opal.dto.history.DefendantTransactionTypeReference;
import uk.gov.hmcts.opal.dto.history.PaymentMethodReference;
import uk.gov.hmcts.opal.dto.history.WriteOffTypeReference;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.PaymentMethod;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionStatus;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionType;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionWriteOffCode;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DefendantTransactionEntityHistoryMapper {

    @Mapping(target = "postedDetails.postedDate", source = "postedDate")
    @Mapping(target = "postedDetails.postedBy", source = "postedBy")
    @Mapping(target = "postedDetails.postedByName", source = "postedByUsername")
    @Mapping(target = "type", expression = "java(uk.gov.hmcts.opal.dto.history.HistoryItemType.FINANCIAL)")
    @Mapping(target = "details", source = ".", qualifiedByName = "toTransactionDetails")
    @Mapping(target = "amount", source = "transactionAmount")
    @Mapping(target = "eventDateTime", source = "postedDate")
    @Mapping(target = "sourceId", source = "defendantTransactionId")
    DefendantAccountHistoryItem toHistoryItem(DefendantTransactionEntity entity);

    @Named("toTransactionDetails")
    @Mapping(target = "transactionType", source = "transactionType")
    @Mapping(target = "additionalInformation", source = "text")
    @Mapping(target = "writeOff", source = "writeOffCode")
    @Mapping(target = "associatedRecordType", source = "associatedRecordType")
    @Mapping(target = "amountImposed", source = "imposedAmount")
    DefendantTransactionDetails toTransactionDetails(DefendantTransactionEntity entity);

    default DefendantTransactionTypeReference map(DefendantTransactionType transactionType) {
        if (transactionType == null) {
            return null;
        }
        return DefendantTransactionTypeReference.builder()
            .transactionType(transactionType.getApiCode())
            .transactionTypeDisplayName(transactionType.getDisplayName())
            .build();
    }

    default PaymentMethodReference map(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return null;
        }
        return PaymentMethodReference.builder()
            .paymentMethod(paymentMethod.name())
            .paymentMethodDisplayName(paymentMethod.getDisplayName())
            .build();
    }

    default WriteOffTypeReference map(DefendantTransactionWriteOffCode writeOffCode) {
        if (writeOffCode == null) {
            return null;
        }
        return WriteOffTypeReference.builder()
            .writeOffType(writeOffCode.getLabel())
            .writeOffTypeDisplayName(writeOffCode.getDisplayName())
            .build();
    }

    default DefendantTransactionStatusReference map(DefendantTransactionStatus status) {
        if (status == null) {
            return null;
        }
        return DefendantTransactionStatusReference.builder()
            .defendantTransactionStatus(status.name())
            .defendantTransactionStatusDisplayName(status.getDisplayName())
            .build();
    }

    default String map(AssociatedRecordType associatedRecordType) {
        return associatedRecordType == null ? null : associatedRecordType.getLabel();
    }
}
