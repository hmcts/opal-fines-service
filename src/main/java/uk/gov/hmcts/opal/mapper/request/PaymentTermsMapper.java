package uk.gov.hmcts.opal.mapper.request;

import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;

@org.mapstruct.Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentTermsMapper {
    @org.mapstruct.Mapping(source = "daysInDefault", target = "jailDays")
    @org.mapstruct.Mapping(source = "lumpSumAmount", target = "instalmentLumpSum")
    @org.mapstruct.Mapping(source = "paymentTermsType.paymentTermsTypeCode", target = "termsTypeCode")
    @org.mapstruct.Mapping(source = "instalmentPeriod.instalmentPeriodCode", target = "instalmentPeriod")
    PaymentTermsEntity toEntity(uk.gov.hmcts.opal.dto.PaymentTerms dto);
}
