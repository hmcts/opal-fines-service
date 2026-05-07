package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse.Financials;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHeaderSummaryResponse.FinancialsLegacy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FinancialsLegacyMapper {

    Financials toOpal(FinancialsLegacy legacy);
}
