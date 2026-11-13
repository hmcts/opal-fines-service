package uk.gov.hmcts.opal.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaLegacyEntity;
import uk.gov.hmcts.opal.generated.model.LocalJusticeArea;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreaSummary;

@Mapper(componentModel = "spring")
public interface LocalJusticeAreaLegacyMapper {

    LocalJusticeArea toLocalJusticeArea(LocalJusticeAreaLegacyEntity entity);

    LocalJusticeAreaSummary toLocalJusticeAreaSummary(LocalJusticeAreaLegacyEntity entity);

    List<LocalJusticeAreaSummary> toLocalJusticeAreaSummaries(List<LocalJusticeAreaLegacyEntity> entities);
}
