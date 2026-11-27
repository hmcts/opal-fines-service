package uk.gov.hmcts.opal.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import uk.gov.hmcts.opal.entity.OpalLocalJusticeAreaEntity;
import uk.gov.hmcts.opal.generated.model.LocalJusticeArea;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreaSummary;

@Mapper(componentModel = "spring")
public interface OpalLocalJusticeAreaMapper {

    LocalJusticeArea toLocalJusticeArea(OpalLocalJusticeAreaEntity entity);

    LocalJusticeAreaSummary toLocalJusticeAreaSummary(OpalLocalJusticeAreaEntity entity);

    List<LocalJusticeAreaSummary> toLocalJusticeAreaSummaries(List<OpalLocalJusticeAreaEntity> entities);
}
