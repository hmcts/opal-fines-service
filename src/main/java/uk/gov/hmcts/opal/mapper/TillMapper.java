package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import uk.gov.hmcts.opal.entity.TillSummaryEntity;
import uk.gov.hmcts.opal.generated.model.TillsItem;

@Mapper(componentModel = "spring")
public interface TillMapper {

    TillsItem toResponse(TillSummaryEntity entity);
}
