package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import uk.gov.hmcts.opal.dto.reference.OffenceReferenceData;
import uk.gov.hmcts.opal.dto.reference.OffenceSearchData;
import uk.gov.hmcts.opal.entity.offence.OffenceEntity;

@Mapper(componentModel = "spring")
public interface OffenceMapper {

    OffenceReferenceData toRefData(OffenceEntity entity);

    OffenceSearchData toSearchData(OffenceEntity entity);
}
