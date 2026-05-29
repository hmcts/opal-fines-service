package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        LegacyPartyDetailsMapper.class,
        BusinessUnitSummaryLegacyMapper.class,
        CreditorHeaderLegacyMapper.class,
        FinancialsLegacyMapper.class
    }
)
public interface GetMinorCreditorAccountHeaderSummaryResponseLegacyMapper {

    @Mappings({
        @Mapping(source = "partyDetails", target = "party")
    })
    GetMinorCreditorAccountHeaderSummaryResponse toOpal(GetMinorCreditorAccountHeaderSummaryLegacyResponse legacy);

}
