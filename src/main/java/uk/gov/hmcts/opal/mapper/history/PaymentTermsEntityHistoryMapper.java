package uk.gov.hmcts.opal.mapper.history;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.mapper.request.PaymentTermsMapper;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = PaymentTermsMapper.class
)
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
    PaymentTerms toPaymentTermsDetails(PaymentTermsEntity entity);
}
