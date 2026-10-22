package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.AlternatePaymentReferenceEntity;
import uk.gov.hmcts.opal.generated.model.AlternatePaymentReferencesItem;

@Mapper(componentModel = "spring")
public interface AlternatePaymentReferenceMapper {


    @Mapping(target = "createdAt", source = "createdDatetime")
    @Mapping(target = "updatedAt", source = "updatedDatetime")
    @Mapping(target = "defendantAccountId", source = "defendantAccount.defendantAccountId")
    @Mapping(target = "businessUnitId", source = "businessUnit.businessUnitId")
    AlternatePaymentReferencesItem toAlternatePaymentReferencesItem(AlternatePaymentReferenceEntity entity);
}
