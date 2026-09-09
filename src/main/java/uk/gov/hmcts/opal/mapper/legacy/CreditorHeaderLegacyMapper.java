package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountHeaderSummaryLegacyResponse.CreditorHeaderLegacy;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountHeaderSummaryResponseCreditor;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = CreditorAccountTypeReferenceMapper.class
)
public interface CreditorHeaderLegacyMapper {

    MinorCreditorAccountHeaderSummaryResponseCreditor toOpal(CreditorHeaderLegacy legacy);
}
