package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse.CreditorHeader;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountHeaderSummaryResponse.CreditorHeaderLegacy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CreditorHeaderLegacyMapper {

    CreditorHeader toOpal(CreditorHeaderLegacy legacy);
}
