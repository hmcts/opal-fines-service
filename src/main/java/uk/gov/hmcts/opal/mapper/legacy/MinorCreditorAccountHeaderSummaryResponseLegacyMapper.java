package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountHeaderSummaryResponse;

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
public interface MinorCreditorAccountHeaderSummaryResponseLegacyMapper {

    @Mapping(source = "partyDetails", target = "party")
    MinorCreditorAccountHeaderSummaryResponse toOpal(GetMinorCreditorAccountHeaderSummaryLegacyResponse legacy);

}
