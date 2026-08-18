package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface LegacyPartyDetailsMapper {

    PartyDetails toOpal(PartyDetailsLegacy legacy);

    PartyDetailsCommon toPartyDetailsCommon(PartyDetailsLegacy legacy);
}
