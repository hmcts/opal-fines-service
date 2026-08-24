package uk.gov.hmcts.opal.mapper;

import java.math.BigInteger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.mapper.request.PaymentTermsMapper;

@Mapper(componentModel = "spring", uses = PaymentTermsMapper.class, imports = BigInteger.class)
public interface DefendantAccountPaymentTermsMapper {

    @Mapping(target = "paymentTerms", source = ".")
    @Mapping(target = "paymentCardLastRequested", source = "defendantAccount.paymentCardRequestedDate")
    @Mapping(target = "lastEnforcement", source = "defendantAccount.lastEnforcement")
    @Mapping(target = "version", source = "defendantAccount.version", defaultExpression = "java((BigInteger.ONE).longValue())")
    DefendantAccountPaymentTermsResponse toResponse(PaymentTermsEntity entity);

    @Mapping(target = "paymentTerms", source = ".")
    DefendantAccountPaymentTermsResponse toResponse(LegacyGetDefendantAccountPaymentTermsResponse response);
}
