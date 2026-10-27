package uk.gov.hmcts.opal.mapper.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.paymentterms.TermsTypeCode;
import uk.gov.hmcts.opal.generated.model.EnforcementPostedDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.GetPaymentTermsResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.InstalmentPeriodCommonStrict;
import uk.gov.hmcts.opal.generated.model.PaymentTermsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PaymentTermsTypeCommonStrict;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentTermsMapper {

    @Mapping(source = "daysInDefault", target = "jailDays")
    @Mapping(source = "lumpSumAmount", target = "instalmentLumpSum")
    @Mapping(source = "paymentTermsType.paymentTermsTypeCode", target = "termsTypeCode")
    @Mapping(source = "instalmentPeriod.instalmentPeriodCode", target = "instalmentPeriod")
    @Mapping(source = "postedDetails.postedBy", target = "postedBy")
    @Mapping(source = "postedDetails.postedByName", target = "postedByUsername")
    PaymentTermsEntity toEntity(PaymentTerms dto);

    default PaymentTermsEntity toEntity(PaymentTermsDefendantAccount source) {
        if (source == null) {
            return null;
        }

        EnforcementPostedDetailsCommonStrict postedDetails = toValue(source.getPostedDetails());
        InstalmentPeriodCommonStrict instalmentPeriod = toValue(source.getInstalmentPeriod());
        return PaymentTermsEntity.builder()
            .jailDays(toValue(source.getDaysInDefault()))
            .termsTypeCode(toTermsTypeCode(source.getPaymentTermsType() == null
                ? null : source.getPaymentTermsType().getPaymentTermsTypeCode()))
            .effectiveDate(toValue(source.getEffectiveDate()))
            .instalmentPeriod(toInstalmentPeriod(instalmentPeriod == null
                ? null : instalmentPeriod.getInstalmentPeriodCode()))
            .instalmentLumpSum(toValue(source.getLumpSumAmount()))
            .instalmentAmount(toValue(source.getInstalmentAmount()))
            .extension(source.getExtension())
            .reasonForExtension(toValue(source.getReasonForExtension()))
            .postedDate(postedDetails == null ? null : postedDetails.getPostedDate())
            .postedBy(postedDetails == null ? null : toValue(postedDetails.getPostedBy()))
            .postedByUsername(postedDetails == null ? null : toValue(postedDetails.getPostedByName()))
            .build();
    }

    @Mapping(source = "termsTypeCode", target = "paymentTermsType.paymentTermsTypeCode")
    @Mapping(source = "instalmentPeriod", target = "instalmentPeriod.instalmentPeriodCode")
    PaymentTerms toDto(PaymentTermsEntity savedPaymentTerms);

    default GetPaymentTermsResponseDefendantAccount toGeneratedResponse(
        PaymentTermsEntity entity, DefendantAccountEntity account) {
        if (entity == null) {
            return null;
        }

        PaymentTermsDefendantAccount paymentTerms = PaymentTermsDefendantAccount.builder()
            .daysInDefault(JsonNullable.of(entity.getJailDays()))
            .dateDaysInDefaultImposed(JsonNullable.of(account.getSuspendedCommittalDate()))
            .extension(entity.getExtension())
            .reasonForExtension(JsonNullable.of(entity.getReasonForExtension()))
            .paymentTermsType(PaymentTermsTypeCommonStrict.builder()
                .paymentTermsTypeCode(toGeneratedTermsTypeCode(entity.getTermsTypeCode()))
                .paymentTermsTypeDisplayName(mapDisplayName(entity.getTermsTypeCode()))
                .build())
            .effectiveDate(JsonNullable.of(entity.getEffectiveDate()))
            .instalmentPeriod(JsonNullable.of(InstalmentPeriodCommonStrict.builder()
                .instalmentPeriodCode(toGeneratedInstalmentPeriodCode(entity.getInstalmentPeriod()))
                .instalmentPeriodDisplayName(mapDisplayName(entity.getInstalmentPeriod()))
                .build()))
            .lumpSumAmount(JsonNullable.of(entity.getInstalmentLumpSum()))
            .instalmentAmount(JsonNullable.of(entity.getInstalmentAmount()))
            .postedDetails(JsonNullable.of(EnforcementPostedDetailsCommonStrict.builder()
                .postedDate(entity.getPostedDate())
                .postedBy(JsonNullable.of(entity.getPostedBy()))
                .postedByName(JsonNullable.of(entity.getPostedByUsername()))
                .build()))
            .build();

        return GetPaymentTermsResponseDefendantAccount.builder()
            .paymentTerms(paymentTerms)
            .paymentCardLastRequested(account.getPaymentCardRequestedDate())
            .lastEnforcement(account.getLastEnforcement())
            .build();
    }

    default <T> T toValue(JsonNullable<T> source) {
        return source == null ? null : source.orElse(null);
    }

    default uk.gov.hmcts.opal.entity.paymentterms.InstalmentPeriod map(InstalmentPeriod.InstalmentPeriodCode code) {
        return code == null ? null : uk.gov.hmcts.opal.entity.paymentterms.InstalmentPeriod.fromCode(code.name());
    }

    default InstalmentPeriod.InstalmentPeriodCode map(uk.gov.hmcts.opal.entity.paymentterms.InstalmentPeriod period) {
        return period == null ? null : InstalmentPeriod.InstalmentPeriodCode.fromValue(period.getCode());
    }

    default TermsTypeCode map(PaymentTermsType.PaymentTermsTypeCode code) {
        return code == null ? null : TermsTypeCode.fromCode(code.name());
    }

    default PaymentTermsType.PaymentTermsTypeCode map(TermsTypeCode code) {
        return code == null ? null : PaymentTermsType.PaymentTermsTypeCode.fromValue(code.getCode());
    }

    default TermsTypeCode toTermsTypeCode(PaymentTermsTypeCommonStrict.PaymentTermsTypeCodeEnum code) {
        return code == null ? null : TermsTypeCode.fromCode(code.getValue());
    }

    default PaymentTermsTypeCommonStrict.PaymentTermsTypeCodeEnum toGeneratedTermsTypeCode(TermsTypeCode code) {
        return code == null ? null : PaymentTermsTypeCommonStrict.PaymentTermsTypeCodeEnum.fromValue(code.getCode());
    }

    default uk.gov.hmcts.opal.entity.paymentterms.InstalmentPeriod toInstalmentPeriod(
        InstalmentPeriodCommonStrict.InstalmentPeriodCodeEnum code) {
        return code == null ? null : uk.gov.hmcts.opal.entity.paymentterms.InstalmentPeriod.fromCode(code.getValue());
    }

    default InstalmentPeriodCommonStrict.InstalmentPeriodCodeEnum toGeneratedInstalmentPeriodCode(
        uk.gov.hmcts.opal.entity.paymentterms.InstalmentPeriod period) {
        return period == null ? null
            : InstalmentPeriodCommonStrict.InstalmentPeriodCodeEnum.fromValue(period.getCode());
    }

    default PaymentTermsTypeCommonStrict.PaymentTermsTypeDisplayNameEnum mapDisplayName(TermsTypeCode code) {
        return code == null ? null
            : PaymentTermsTypeCommonStrict.PaymentTermsTypeDisplayNameEnum.fromValue(switch (code) {
                case BY_DATE -> "By date";
                case PAID -> "Paid";
                case INSTALMENTS -> "Instalments";
            });
    }

    default InstalmentPeriodCommonStrict.InstalmentPeriodDisplayNameEnum mapDisplayName(
        uk.gov.hmcts.opal.entity.paymentterms.InstalmentPeriod period) {
        return period == null ? null
            : InstalmentPeriodCommonStrict.InstalmentPeriodDisplayNameEnum.fromValue(switch (period) {
                case WEEK -> "Weekly";
                case MONTH -> "Monthly";
                case FORTNIGHT -> "Fortnightly";
            });
    }
}
