package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.legacy.RemoveDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyResponseDefendantAccount;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RemoveDefendantAccountPartyLegacyResponseMapper {

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    RemoveDefendantAccountPartyResponseDefendantAccount toRemoveDefendantAccountPartyResponse(
        RemoveDefendantAccountPartyLegacyResponse legacyResponse
    );
}
