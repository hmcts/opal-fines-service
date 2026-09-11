package uk.gov.hmcts.opal.mapper.till;

import java.util.List;
import java.util.Map;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.TillsGetResponse;

@Mapper(
    componentModel = "spring",
    uses = GetTillPaymentMapper.class
)
public abstract class GetTillMapper {

    @Mapping(target = "tillNumber", source = "till.tillNumber")
    @Mapping(target = "businessUnitId", source = "till.businessUnit.businessUnitId")
    @Mapping(target = "createdBy", source = "till.ownedByName")
    @Mapping(target = "createdDate", source = "till.createdDate")
    @Mapping(target = "paymentsIn", source = "payments")
    public abstract TillsGetResponse toResponse(
        TillEntity till, List<PaymentInEntity> payments,
        @Context Map<Long, DefendantAccountEntity> defendantAccounts);
}
