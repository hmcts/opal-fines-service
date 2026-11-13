package uk.gov.hmcts.opal.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.generated.model.LocalJusticeArea;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreaSummary;

@Mapper(componentModel = "spring")
public interface LocalJusticeAreaMapper {

    LocalJusticeArea toLocalJusticeArea(LocalJusticeAreaEntity entity);

    LocalJusticeAreaSummary toLocalJusticeAreaSummary(LocalJusticeAreaEntity entity);

    List<LocalJusticeAreaSummary> toLocalJusticeAreaSummaries(List<LocalJusticeAreaEntity> entities);
}
