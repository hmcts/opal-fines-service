package uk.gov.hmcts.opal.mapper.request;

import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.paymentterms.TermsTypeCode;

@org.mapstruct.Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentTermsMapper {
    @org.mapstruct.Mapping(source = "daysInDefault", target = "jailDays")
    @org.mapstruct.Mapping(source = "lumpSumAmount", target = "instalmentLumpSum")
    @org.mapstruct.Mapping(source = "paymentTermsType.paymentTermsTypeCode", target = "termsTypeCode")
    @org.mapstruct.Mapping(source = "instalmentPeriod.instalmentPeriodCode", target = "instalmentPeriod")
    @org.mapstruct.Mapping(source = "postedDetails.postedBy", target = "postedBy")
    @org.mapstruct.Mapping(source = "postedDetails.postedByName", target = "postedByUsername")
    PaymentTermsEntity toEntity(PaymentTerms dto);

    @org.mapstruct.Mapping(source = "termsTypeCode", target = "paymentTermsType.paymentTermsTypeCode")
    @org.mapstruct.Mapping(source = "instalmentPeriod", target = "instalmentPeriod.instalmentPeriodCode")
    PaymentTerms toDto(PaymentTermsEntity savedPaymentTerms);

    default uk.gov.hmcts.opal.entity.paymentterms.InstalmentPeriod map(
        InstalmentPeriod.InstalmentPeriodCode code
    ) {
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
}
