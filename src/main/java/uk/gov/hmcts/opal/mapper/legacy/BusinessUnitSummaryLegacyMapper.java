package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BusinessUnitSummaryLegacyMapper {

    BusinessUnitSummaryCommon toBusinessUnitSummaryCommon(
        uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary businessUnitSummary);
}
