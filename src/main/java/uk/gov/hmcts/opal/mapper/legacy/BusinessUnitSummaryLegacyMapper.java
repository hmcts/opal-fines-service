package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BusinessUnitSummaryLegacyMapper {

    BusinessUnitSummary toOpal(uk.gov.hmcts.opal.dto.legacy.common.BusinessUnitSummary businessUnitSummary);
}
