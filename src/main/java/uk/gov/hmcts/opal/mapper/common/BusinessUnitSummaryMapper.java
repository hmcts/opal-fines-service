package uk.gov.hmcts.opal.mapper.common;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;

@Mapper(componentModel = "spring")
public interface BusinessUnitSummaryMapper {

    @Mapping(target = "businessUnitId", source = "entity.businessUnitId")
    @Mapping(target = "welshSpeaking", expression = "java(toWelshSpeaking(entity.isWelshLanguage()))")
    BusinessUnitSummary toBusinessUnitSummary(MinorCreditorAccountHeaderEntity entity);

    @Mapping(target = "welshSpeaking", expression = "java(toWelshSpeaking(businessUnitEntity.getWelshLanguage()))")
    BusinessUnitSummaryCommon toBusinessUnitSummaryCommon(BusinessUnitEntity businessUnitEntity);

    List<BusinessUnitSummaryCommon> toBusinessUnitSummaryCommonList(List<BusinessUnitEntity> businessUnitEntities);

    default String toWelshSpeaking(boolean welshLanguage) {
        return welshLanguage ? "Y" : "N";
    }
}
