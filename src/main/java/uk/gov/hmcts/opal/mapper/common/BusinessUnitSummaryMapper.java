package uk.gov.hmcts.opal.mapper.common;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;

@Mapper(componentModel = "spring")
public interface BusinessUnitSummaryMapper {

    @Mapping(target = "businessUnitId", source = "entity.businessUnitId")
    @Mapping(target = "businessUnitCode", ignore = true)
    @Mapping(target = "welshSpeaking", expression = "java(toWelshSpeaking(entity.isWelshLanguage()))")
    BusinessUnitSummary toBusinessUnitSummary(MinorCreditorAccountHeaderEntity entity);

    default String toWelshSpeaking(boolean welshLanguage) {
        return welshLanguage ? "Y" : "N";
    }
}
