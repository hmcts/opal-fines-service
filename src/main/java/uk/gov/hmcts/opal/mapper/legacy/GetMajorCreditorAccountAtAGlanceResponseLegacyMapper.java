package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

    @Mapping(target = "line1", source = "line1")
    @Mapping(target = "line2", source = "line2")
    @Mapping(target = "line3", source = "line3")
    GetMajorCreditorAccountAtAGlanceResponse.Address toOpal(MajorCreditorAddressLegacy legacy);
}
