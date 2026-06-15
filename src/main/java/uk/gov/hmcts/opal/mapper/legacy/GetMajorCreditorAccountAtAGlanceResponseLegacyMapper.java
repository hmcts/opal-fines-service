package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountAtAGlanceLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountAtAGlanceLegacyResponse.MajorCreditorAddressLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountAtAGlanceLegacyResponse.MajorCreditorLegacy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface GetMajorCreditorAccountAtAGlanceResponseLegacyMapper {

    GetMajorCreditorAccountAtAGlanceResponse toOpal(GetMajorCreditorAccountAtAGlanceLegacyResponse legacy);

    GetMajorCreditorAccountAtAGlanceResponse.MajorCreditor toOpal(MajorCreditorLegacy legacy);

    GetMajorCreditorAccountAtAGlanceResponse.Address toOpal(MajorCreditorAddressLegacy legacy);
}
