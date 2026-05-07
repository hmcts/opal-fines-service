package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHeaderSummaryResponse.FinancialsLegacy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        LegacyPartyDetailsMapper.class,
        LegacyBusinessUnitSummaryMapper.class,
        CreditorHeaderLegacyMapper.class,
        FinancialsLegacy.class
    }
)
public interface LegacyGetMinorCreditorAccountHeaderSummaryResponseMapper {

    @Mappings({
        @Mapping(source = "partyDetails", target = "party")
    })
    GetMinorCreditorAccountHeaderSummaryResponse toOpal(LegacyGetMinorCreditorAccountHeaderSummaryResponse legacy);

}
