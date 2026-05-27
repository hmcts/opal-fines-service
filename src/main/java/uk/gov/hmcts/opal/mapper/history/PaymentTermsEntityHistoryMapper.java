package uk.gov.hmcts.opal.mapper.history;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.paymentterms.TermsTypeCode;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentTermsEntityHistoryMapper {

    @Mapping(target = "postedDetails.postedDate", source = "postedDate")
    @Mapping(target = "postedDetails.postedBy", source = "postedBy")
    @Mapping(target = "postedDetails.postedByName", source = "postedByUsername")
    @Mapping(target = "type", expression = "java(uk.gov.hmcts.opal.dto.history.HistoryItemType.PAYMENT_TERMS)")
    @Mapping(target = "details", source = ".", qualifiedByName = "toPaymentTermsDetails")
    @Mapping(target = "eventDateTime", source = "postedDate")
    @Mapping(target = "sourceId", source = "paymentTermsId")
    DefendantAccountHistoryItem toHistoryItem(PaymentTermsEntity entity);

    @Named("toPaymentTermsDetails")
    @Mapping(target = "daysInDefault", source = "jailDays")
    @Mapping(target = "lumpSumAmount", source = "instalmentLumpSum")
    @Mapping(target = "paymentTermsType.paymentTermsTypeCode", source = "termsTypeCode")
    @Mapping(target = "instalmentPeriod.instalmentPeriodCode", source = "instalmentPeriod")
    @Mapping(target = "postedDetails.postedDate", source = "postedDate")
    @Mapping(target = "postedDetails.postedBy", source = "postedBy")
    @Mapping(target = "postedDetails.postedByName", source = "postedByUsername")
    PaymentTerms toPaymentTermsDetails(PaymentTermsEntity entity);

    default PaymentTermsType.PaymentTermsTypeCode map(TermsTypeCode code) {
        return code == null ? null : PaymentTermsType.PaymentTermsTypeCode.fromValue(code.getCode());
    }

    default InstalmentPeriod.InstalmentPeriodCode map(
        uk.gov.hmcts.opal.entity.paymentterms.InstalmentPeriod period
    ) {
        return period == null ? null : InstalmentPeriod.InstalmentPeriodCode.fromValue(period.getCode());
    }
}
