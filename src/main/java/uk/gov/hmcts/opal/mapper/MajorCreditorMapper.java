package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.dto.reference.MajorCreditorReferenceData;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorEntity;

@Mapper(componentModel = "spring")
public interface MajorCreditorMapper {

    @Mapping(source = "creditorAccountEntity.creditorAccountId", target = "creditorAccountId")
    @Mapping(source = "creditorAccountEntity.accountNumber", target = "accountNumber") // if you fixed the typo
    @Mapping(source = "creditorAccountEntity.creditorAccountType", target = "creditorAccountType")
    @Mapping(source = "creditorAccountEntity.prosecutionService", target = "prosecutionService")
    @Mapping(source = "creditorAccountEntity.minorCreditorPartyId", target = "minorCreditorPartyId")
    @Mapping(source = "creditorAccountEntity.fromSuspense", target = "fromSuspense")
    @Mapping(source = "creditorAccountEntity.holdPayout", target = "holdPayout")
    @Mapping(source = "creditorAccountEntity.lastChangedDate", target = "lastChangedDate")
    MajorCreditorReferenceData toRefData(MajorCreditorEntity entity);

}
