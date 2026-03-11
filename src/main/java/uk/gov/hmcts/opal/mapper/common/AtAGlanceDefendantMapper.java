package uk.gov.hmcts.opal.mapper.common;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.DefendantDto;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse.AtAGlanceDefendant;
import uk.gov.hmcts.opal.dto.legacy.Defendant;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AtAGlanceDefendantMapper {

    AtAGlanceDefendant toDto(LegacyGetMinorCreditorAccountAtAGlanceResponse.AtAGlanceDefendant legacy);
}