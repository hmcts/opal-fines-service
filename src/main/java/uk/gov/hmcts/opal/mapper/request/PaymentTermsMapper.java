package uk.gov.hmcts.opal.mapper.request;

import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;

@org.mapstruct.Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentTermsMapper {
    @org.mapstruct.Mapping(source = "daysInDefault", target = "jailDays")
    @org.mapstruct.Mapping(source = "lumpSumAmount", target = "instalmentLumpSum")
    @org.mapstruct.Mapping(source = "paymentTermsType.paymentTermsTypeCode", target = "termsTypeCode")
    @org.mapstruct.Mapping(source = "instalmentPeriod.instalmentPeriodCode", target = "instalmentPeriod")
    @org.mapstruct.Mapping(source = "postedDetails.postedBy", target = "postedBy")
    @org.mapstruct.Mapping(source = "postedDetails.postedByName", target = "postedByUsername")
    PaymentTermsEntity toEntity(uk.gov.hmcts.opal.dto.PaymentTerms dto);

    @org.mapstruct.Mapping(source = "instalmentPeriod", target = "instalmentPeriod")
    PaymentTerms toDto(PaymentTermsEntity savedPaymentTerms);

    // MapStruct will use this to convert a String code -> InstalmentPeriod
    default InstalmentPeriod map(String code) {
        if (code == null) {
            return null;
        }

        return InstalmentPeriod.fromCode(code);
    }

    // MapStruct will use this to convert InstalmentPeriod -> String code
    default String map(InstalmentPeriod period) {
        return period == null ? null : period.getInstalmentPeriodDisplayName();
    }
}
