package uk.gov.hmcts.opal.mapper.common;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressMapper {

    AddressDetails toDto(AddressDetailsLegacy legacy);

    AddressDetailsCommon toAddressDetailsCommon(PartyEntity party);
}