package uk.gov.hmcts.opal.mapper.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.paymentterms.TermsTypeCode;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentTermsMapper {

    @Mapping(source = "daysInDefault", target = "jailDays")
    @Mapping(source = "lumpSumAmount", target = "instalmentLumpSum")
    @Mapping(source = "paymentTermsType.paymentTermsTypeCode", target = "termsTypeCode")
    @Mapping(source = "instalmentPeriod.instalmentPeriodCode", target = "instalmentPeriod")
    @Mapping(source = "postedDetails.postedBy", target = "postedBy")
    @Mapping(source = "postedDetails.postedByName", target = "postedByUsername")
    PaymentTermsEntity toEntity(PaymentTerms dto);

    @Mapping(source = "termsTypeCode", target = "paymentTermsType.paymentTermsTypeCode")
    @Mapping(source = "instalmentPeriod", target = "instalmentPeriod.instalmentPeriodCode")
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
