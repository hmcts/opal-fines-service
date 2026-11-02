package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import uk.gov.hmcts.opal.dto.reference.ProsecutorReferenceData;
import uk.gov.hmcts.opal.entity.ProsecutorLegacyEntity;

@Mapper(componentModel = "spring")
public interface ProsecutorMapper {

    ProsecutorReferenceData toRefData(ProsecutorLegacyEntity entity);
}
