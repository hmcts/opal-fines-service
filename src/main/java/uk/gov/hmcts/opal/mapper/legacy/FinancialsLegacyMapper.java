package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse.FinancialsLegacy;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountHeaderSummaryResponseFinancials;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FinancialsLegacyMapper {

    MinorCreditorAccountHeaderSummaryResponseFinancials toOpal(FinancialsLegacy legacy);
}
