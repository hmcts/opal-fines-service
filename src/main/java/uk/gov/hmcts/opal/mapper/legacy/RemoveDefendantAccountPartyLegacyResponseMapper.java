package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.legacy.RemoveDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RemoveDefendantAccountPartyLegacyResponseMapper {

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    RemoveDefendantAccountPartyResponse toRemoveDefendantAccountPartyResponse(
        RemoveDefendantAccountPartyLegacyResponse legacyResponse
    );
}
