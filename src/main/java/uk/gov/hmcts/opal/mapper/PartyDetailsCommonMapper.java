package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;

@Mapper(
    componentModel = "spring",
    uses = {IndividualDetailsCommonMapper.class, OrganisationDetailsCommonMapper.class}
)
public interface PartyDetailsCommonMapper {

    @Mapping(target = "partyId", expression = "java(toPartyIdString(party.getPartyId()))")
    @Mapping(target = "organisationFlag", source = "organisation")
    @Mapping(
        target = "individualDetails",
        source = "party",
        conditionExpression = "java(!party.isOrganisation())"
    )
    @Mapping(
        target = "organisationDetails",
        source = "party",
        conditionExpression = "java(party.isOrganisation())"
    )
    PartyDetailsCommon toPartyDetailsCommon(PartyEntity party);

    default String toPartyIdString(Long partyId) {
        return partyId == null ? null : String.valueOf(partyId);
    }
}
