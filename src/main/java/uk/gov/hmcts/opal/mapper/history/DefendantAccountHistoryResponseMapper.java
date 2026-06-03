package uk.gov.hmcts.opal.mapper.history;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.CourtReferenceDto;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.history.AmendmentDetails;
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
import uk.gov.hmcts.opal.generated.model.AmendmentTypeCommon;
import uk.gov.hmcts.opal.generated.model.CourtReferenceCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountHistoryItemHistory;
import uk.gov.hmcts.opal.generated.model.DefendantAccountHistoryItemHistoryDetails;
import uk.gov.hmcts.opal.generated.model.DefendantTransactionDetailsHistory;
import uk.gov.hmcts.opal.generated.model.DefendantTransactionStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.DefendantTransactionTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementDetailsHistory;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHistory200Response;
import uk.gov.hmcts.opal.generated.model.InstalmentPeriodCommon;
import uk.gov.hmcts.opal.generated.model.NoteDetailsHistory;
import uk.gov.hmcts.opal.generated.model.PaymentMethodReferenceCommon;
import uk.gov.hmcts.opal.generated.model.PaymentTermsCommon;
import uk.gov.hmcts.opal.generated.model.PaymentTermsTypeCommon;
import uk.gov.hmcts.opal.generated.model.PostedDetailsCommon;
import uk.gov.hmcts.opal.generated.model.WriteOffTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.DefendantTransactionStatusReferenceCommon.DefendantTransactionStatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DefendantAccountHistoryResponseMapper {

    @Mapping(target = "historyItems", source = "historyItems")
    GetDefendantAccountHistory200Response toGeneratedResponse(DefendantAccountHistoryResponse response);

    @Mapping(target = "postedDetails", source = "postedDetails")
    @Mapping(target = "type", expression = "java(mapType(item.getType()))")
    @Mapping(target = "details", expression = "java(mapDetails(item.getDetails()))")
    DefendantAccountHistoryItemHistory toGeneratedHistoryItem(DefendantAccountHistoryItem item);

    @Mapping(target = "postedDate", source = "postedDate", qualifiedByName = "toLocalDate")
    PostedDetailsCommon toGeneratedPostedDetails(PostedDetails postedDetails);

    AmendmentTypeCommon toGeneratedAmendmentType(AmendmentDetails details);

    EnforcementDetailsHistory toGeneratedEnforcementDetails(EnforcementDetails details);

    NoteDetailsHistory toGeneratedNoteDetails(NoteDetails details);

    PaymentTermsCommon toGeneratedPaymentTermsDetails(PaymentTermsDetails details);

    DefendantTransactionDetailsHistory toGeneratedDefendantTransactionDetails(DefendantTransactionDetails details);

    CourtReferenceCommon toGeneratedCourtReference(CourtReferenceDto courtReference);

    PaymentTermsTypeCommon toGeneratedPaymentTermsType(PaymentTermsType paymentTermsType);

    InstalmentPeriodCommon toGeneratedInstalmentPeriod(InstalmentPeriod instalmentPeriod);

    DefendantTransactionTypeReferenceCommon toGeneratedTransactionType(
        DefendantTransactionTypeReference transactionType);

    PaymentMethodReferenceCommon toGeneratedPaymentMethod(PaymentMethodReference paymentMethod);

    WriteOffTypeReferenceCommon toGeneratedWriteOffType(WriteOffTypeReference writeOffType);

    default DefendantTransactionStatusReferenceCommon toGeneratedTransactionStatus(
        DefendantTransactionStatusReference status) {

        if (status == null || status.getDefendantTransactionStatus() == null) {
            return null;
        }

        String statusCode = status.getDefendantTransactionStatus();
        DefendantTransactionStatusEnum generatedStatus = switch (statusCode) {
            case "P" -> DefendantTransactionStatusEnum.PND;
            case "C" -> DefendantTransactionStatusEnum.COMP;
            default -> DefendantTransactionStatusEnum.CAN;
        };

        String displayName = switch (generatedStatus) {
            case PND -> "Pending";
            case COMP -> "Complete";
            case CAN -> "Cancelled";
        };

        return DefendantTransactionStatusReferenceCommon.builder()
            .defendantTransactionStatus(generatedStatus)
            .defendantTransactionStatusDisplayName(displayName)
            .build();
    }

    default DefendantAccountHistoryItemHistory.TypeEnum mapType(HistoryItemType type) {
        return type == null ? null : DefendantAccountHistoryItemHistory.TypeEnum.fromValue(type.getResponseValue());
    }

    @Named("toLocalDate")
    default LocalDate toLocalDate(LocalDateTime value) {
        return value == null ? null : value.toLocalDate();
    }

    default DefendantAccountHistoryItemHistoryDetails mapDetails(Object details) {
        if (details == null) {
            return null;
        }
        if (details instanceof AmendmentDetails amendmentDetails) {
            return toGeneratedAmendmentType(amendmentDetails);
        }
        if (details instanceof EnforcementDetails enforcementDetails) {
            return toGeneratedEnforcementDetails(enforcementDetails);
        }
        if (details instanceof NoteDetails noteDetails) {
            return toGeneratedNoteDetails(noteDetails);
        }
        if (details instanceof PaymentTermsDetails paymentTermsDetails) {
            return toGeneratedPaymentTermsDetails(paymentTermsDetails);
        }
        if (details instanceof DefendantTransactionDetails defendantTransactionDetails) {
            return toGeneratedDefendantTransactionDetails(defendantTransactionDetails);
        }
        throw new IllegalArgumentException("Unsupported defendant account history details type: "
            + details.getClass().getName());
    }
}
