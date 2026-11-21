package uk.gov.hmcts.opal.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.OpalProsecutorEntity;
import uk.gov.hmcts.opal.generated.model.ProsecutorsObject;

@Mapper(componentModel = "spring")
public interface OpalProsecutorMapper {

    @Mapping(target = "prosecutorId", ignore = true)
    ProsecutorsObject toProsecutor(OpalProsecutorEntity entity);

    List<ProsecutorsObject> toProsecutors(List<OpalProsecutorEntity> entities);
}
