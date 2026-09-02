package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.generated.model.EnforcementPostedDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.InstalmentPeriodCommonStrict;
import uk.gov.hmcts.opal.generated.model.PaymentTermsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PaymentTermsTypeCommonStrict;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface LegacyPaymentTermsMapper {

    @Mapping(target = "paymentTermsType", source = "paymentTermsType", qualifiedByName = "toOpalPaymentTermsType")
    @Mapping(target = "instalmentPeriod", source = "instalmentPeriod",
        qualifiedByName = "toJsonNullableInstalmentPeriod")
    @Mapping(target = "postedDetails", source = "postedDetails", qualifiedByName = "toJsonNullablePostedDetails")
    PaymentTermsDefendantAccount toOpal(LegacyPaymentTerms legacy);

    @Named("toOpalPaymentTermsType")
    default PaymentTermsTypeCommonStrict toOpalPaymentTermsType(LegacyPaymentTermsType legacy) {
        if (legacy == null) {
            return null;
        }

        return PaymentTermsTypeCommonStrict.builder()
            .paymentTermsTypeCode(legacy.getPaymentTermsTypeCode() == null ? null
                : PaymentTermsTypeCommonStrict.PaymentTermsTypeCodeEnum.fromValue(
                    legacy.getPaymentTermsTypeCode().name()))
            .build();
    }

    @Named("toJsonNullableInstalmentPeriod")
    default JsonNullable<InstalmentPeriodCommonStrict> toJsonNullableInstalmentPeriod(LegacyInstalmentPeriod legacy) {
        return legacy == null
            ? JsonNullable.undefined()
            : JsonNullable.of(InstalmentPeriodCommonStrict.builder()
                .instalmentPeriodCode(legacy.getInstalmentPeriodCode() == null ? null
                    : InstalmentPeriodCommonStrict.InstalmentPeriodCodeEnum.fromValue(
                        legacy.getInstalmentPeriodCode().name()))
                .build());
    }

    @Named("toJsonNullablePostedDetails")
    default JsonNullable<EnforcementPostedDetailsCommonStrict> toJsonNullablePostedDetails(LegacyPostedDetails legacy) {
        return legacy == null
            ? JsonNullable.undefined()
            : JsonNullable.of(EnforcementPostedDetailsCommonStrict.builder()
                .postedDate(legacy.getPostedDate())
                .postedBy(legacy.getPostedBy())
                .postedByName(legacy.getPostedByName())
                .build());
    }
}
