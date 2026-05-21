package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        OrganisationDetailsLegacyMapper.class,
        IndividualDetailsLegacyMapper.class
    }
)
public interface PartyDetailsLegacyMapper {

    PartyDetailsCommon toOpal(PartyDetailsLegacy partyDetails);
}
