package uk.gov.hmcts.opal.mapper.common;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
    OrganisationDetailsMapper.class,
    IndividualDetailsMapper.class
})
public interface PartyMapper {

    PartyDetails toDto(LegacyPartyDetails legacy);

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