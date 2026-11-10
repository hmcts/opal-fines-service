package uk.gov.hmcts.opal.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.ProsecutorLegacyEntity;
import uk.gov.hmcts.opal.generated.model.ProsecutorsObject;

@Mapper(componentModel = "spring")
public interface ProsecutorLegacyMapper {

    @Mapping(target = "prosecutorCode", ignore = true)
    ProsecutorsObject toProsecutor(ProsecutorLegacyEntity entity);

    List<ProsecutorsObject> toProsecutors(List<ProsecutorLegacyEntity> entities);
}
