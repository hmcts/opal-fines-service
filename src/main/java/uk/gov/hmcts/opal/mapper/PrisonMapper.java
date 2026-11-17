package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.generated.model.PrisonsPrison;
import uk.gov.hmcts.opal.generated.model.PrisonsPrisonSummary;

@Mapper(componentModel = "spring")
public interface PrisonMapper {

    PrisonsPrison toDto(PrisonEntity entity);

    PrisonsPrisonSummary toSummary(PrisonEntity entity);
}
