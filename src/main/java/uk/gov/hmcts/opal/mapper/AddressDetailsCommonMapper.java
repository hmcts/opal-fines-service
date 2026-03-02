package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;

@Mapper(componentModel = "spring")
public interface AddressDetailsCommonMapper {

    AddressDetailsCommon toAddressDetailsCommon(PartyEntity party);
}
