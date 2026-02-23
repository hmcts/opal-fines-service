package uk.gov.hmcts.opal.mapper.common;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
    OrganisationDetailsMapper.class,
    IndividualDetailsMapper.class
})
public interface PartyMapper {

    PartyDetails toDto(LegacyPartyDetails legacy);
}