package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import uk.gov.hmcts.opal.dto.reference.CourtReferenceData;
import uk.gov.hmcts.opal.entity.court.CourtEntity;

@Mapper(componentModel = "spring")
public interface CourtMapper {
    CourtReferenceData toRefData(CourtEntity entity);
}
