package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountPartyLegacyResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DefendantAccountPartyLegacyResponseMapper {

    // Add any necessary mappings here. For now, we assume that the field names and types match
    // between the legacy response and the new response, so no explicit mappings are defined.
    @Mappings({

    })

    GetDefendantAccountPartyResponse toDefendantAccountPartyResponse(
        AddDefendantAccountPartyLegacyResponse legacyResponse
    );
}
