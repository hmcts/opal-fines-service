package uk.gov.hmcts.opal.mapper.common;

import java.util.List;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;

@Mapper(componentModel = "spring")
public interface BusinessUnitSummaryMapper {


    @Mapping(target = "welshSpeaking", expression = "java(toWelshSpeaking(entity.isWelshLanguage()))")
    BusinessUnitSummaryCommon toBusinessUnitSummaryCommon(MinorCreditorAccountHeaderEntity entity);

    @Mapping(target = "welshSpeaking", expression = "java(toWelshSpeaking(businessUnitEntity.getWelshLanguage()))")
    BusinessUnitSummaryCommon toBusinessUnitSummaryCommon(BusinessUnitEntity businessUnitEntity);

    List<BusinessUnitSummaryCommon> toBusinessUnitSummaryCommonList(List<BusinessUnitEntity> businessUnitEntities);

    @Named("toHeaderWelshSpeaking")
    default String toHeaderWelshSpeaking(Boolean welshLanguage, @Context boolean welshLanguageEnabled) {
        return toWelshSpeaking(welshLanguageEnabled && Boolean.TRUE.equals(welshLanguage));
    }

    default String toWelshSpeaking(boolean welshLanguage) {
        return welshLanguage ? "Y" : "N";
    }
}
