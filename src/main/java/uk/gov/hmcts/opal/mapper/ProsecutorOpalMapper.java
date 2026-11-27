package uk.gov.hmcts.opal.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.ProsecutorOpalEntity;
import uk.gov.hmcts.opal.generated.model.ProsecutorsObject;

@Mapper(componentModel = "spring")
public interface ProsecutorOpalMapper {

    @Mapping(target = "prosecutorId", ignore = true)
    ProsecutorsObject toProsecutor(ProsecutorOpalEntity entity);

    List<ProsecutorsObject> toProsecutors(List<ProsecutorOpalEntity> entities);
}
