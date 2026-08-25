package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.paymentterms.TermsTypeCode;
import uk.gov.hmcts.opal.generated.model.DefendantAccountInstalmentPeriodCommonStrict;
import uk.gov.hmcts.opal.generated.model.DefendantAccountInstalmentPeriodCommonStrict.InstalmentPeriodCodeEnum;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPaymentTermsCommonStrict;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPaymentTermsTypeCommonStrict;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPaymentTermsTypeCommonStrict.PaymentTermsTypeCodeEnum;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPostedDetailsCommonStrict;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DefendantAccountPaymentTermsMapper {

    @Mapping(target = "paymentTerms", source = ".")
    @Mapping(target = "paymentCardLastRequested", source = "defendantAccount.paymentCardRequestedDate")
    @Mapping(target = "lastEnforcement", source = "defendantAccount.lastEnforcement")
    @Mapping(target = "version", source = "defendantAccount.version", defaultValue = "1L")
    DefendantAccountPaymentTermsResponse toResponse(PaymentTermsEntity entity);

    @Mapping(target = "daysInDefault", source = "jailDays")
    @Mapping(target = "dateDaysInDefaultImposed", source = "defendantAccount.suspendedCommittalDate")
    @Mapping(target = "paymentTermsType", source = ".")
    @Mapping(target = "instalmentPeriod", source = ".")
    @Mapping(target = "lumpSumAmount", source = "instalmentLumpSum")
    @Mapping(target = "postedDetails", source = ".")
    DefendantAccountPaymentTermsCommonStrict toPaymentTerms(PaymentTermsEntity entity);

    @Mapping(target = "version", defaultValue = "1L")
    DefendantAccountPaymentTermsResponse legacyToResponse(LegacyGetDefendantAccountPaymentTermsResponse response);

    DefendantAccountPaymentTermsCommonStrict legacyToPaymentTerms(LegacyPaymentTerms paymentTerms);

    default DefendantAccountPaymentTermsTypeCommonStrict toPaymentTermsType(PaymentTermsEntity entity) {
        TermsTypeCode typeCode = entity.getTermsTypeCode();
        return DefendantAccountPaymentTermsTypeCommonStrict.builder().paymentTermsTypeCode(
            PaymentTermsTypeCodeEnum.fromValue(typeCode.getCode())).build();
    }

    default DefendantAccountInstalmentPeriodCommonStrict toInstalmentPeriod(PaymentTermsEntity entity) {
        return DefendantAccountInstalmentPeriodCommonStrict.builder().instalmentPeriodCode(
            InstalmentPeriodCodeEnum.fromValue(entity.getInstalmentPeriod().getCode())).build();
    }

    default DefendantAccountPostedDetailsCommonStrict toPostedDetails(PaymentTermsEntity entity) {
        return DefendantAccountPostedDetailsCommonStrict.builder()
            .postedDate(entity.getPostedDate())
            .postedBy(entity.getPostedBy())
            .postedByName(entity.getPostedByUsername())
            .build();
    }

    default DefendantAccountInstalmentPeriodCommonStrict legacyToInstalmentPeriod(LegacyInstalmentPeriod period) {
        if (period == null) {
            return null;
        }
        return DefendantAccountInstalmentPeriodCommonStrict.builder().instalmentPeriodCode(
            InstalmentPeriodCodeEnum.fromValue(period.getInstalmentPeriodCode().name())).build();
    }

    default DefendantAccountPostedDetailsCommonStrict legacyToPostedDetails(LegacyPostedDetails postedDetails) {
        if (postedDetails == null) {
            return null;
        }
        return DefendantAccountPostedDetailsCommonStrict.builder()
            .postedDate(postedDetails.getPostedDate())
            .postedBy(postedDetails.getPostedBy())
            .postedByName(postedDetails.getPostedByName())
            .build();
    }

    default <T> JsonNullable<T> toJsonNullable(T value) {
        return JsonNullable.of(value);
    }
}
