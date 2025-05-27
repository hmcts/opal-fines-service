package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.dto.reference.CourtReferenceData;
import uk.gov.hmcts.opal.entity.court.CourtEntity;

@Mapper(componentModel = "spring")
public interface CourtMapper {

    @Mapping(target = "businessUnitId", source = "businessUnit.businessUnitId")
    CourtReferenceData toRefData(CourtEntity entity);
}
