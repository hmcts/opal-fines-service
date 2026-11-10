package uk.gov.hmcts.opal.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.ProsecutorEntity;
import uk.gov.hmcts.opal.generated.model.ProsecutorsObject;

@Mapper(componentModel = "spring")
public interface ProsecutorMapper {

    @Mapping(target = "prosecutorId", ignore = true)
    ProsecutorsObject toProsecutor(ProsecutorEntity entity);

    List<ProsecutorsObject> toProsecutors(List<ProsecutorEntity> entities);
}
