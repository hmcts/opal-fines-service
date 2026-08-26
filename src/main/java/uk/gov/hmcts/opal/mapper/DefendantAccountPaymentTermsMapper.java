package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.entity.paymentterms.InstalmentPeriod;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.paymentterms.TermsTypeCode;
import uk.gov.hmcts.opal.generated.model.DefendantAccountInstalmentPeriodCommonStrict;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPaymentTermsCommonStrict;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPaymentTermsTypeCommonStrict;
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
    @Mapping(target = "paymentTermsType", source = "termsTypeCode")
    @Mapping(target = "lumpSumAmount", source = "instalmentLumpSum")
    @Mapping(target = "postedDetails", source = ".")
    DefendantAccountPaymentTermsCommonStrict toPaymentTerms(PaymentTermsEntity entity);

    @Mapping(target = "paymentTermsTypeCode", source = "code")
    DefendantAccountPaymentTermsTypeCommonStrict toPaymentTermsType(TermsTypeCode typeCode);

    @Mapping(target = "instalmentPeriodCode", source = "code")
    DefendantAccountInstalmentPeriodCommonStrict toInstalmentPeriod(InstalmentPeriod period);

    @Mapping(target = "postedByName", source = "postedByUsername")
    DefendantAccountPostedDetailsCommonStrict toPostedDetails(PaymentTermsEntity entity);

    @Mapping(target = "version", defaultValue = "1L")
    DefendantAccountPaymentTermsResponse legacyToResponse(LegacyGetDefendantAccountPaymentTermsResponse response);

    DefendantAccountPaymentTermsCommonStrict legacyToPaymentTerms(LegacyPaymentTerms paymentTerms);

    DefendantAccountInstalmentPeriodCommonStrict legacyToInstalmentPeriod(LegacyInstalmentPeriod period);

    DefendantAccountPostedDetailsCommonStrict legacyToPostedDetails(LegacyPostedDetails postedDetails);

    default <T> JsonNullable<T> toJsonNullable(T value) {
        return JsonNullable.of(value);
    }
}
