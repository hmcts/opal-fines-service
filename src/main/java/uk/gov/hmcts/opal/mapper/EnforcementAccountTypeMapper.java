package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountTypeEntity;
import uk.gov.hmcts.opal.generated.model.EnforcementAccountTypeCommon;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EnforcementAccountTypeMapper {
    @Mapping(target = "id", source = "enforcementAccountTypeId")
    @Mapping(target = "path", source = "accountTypePath")
    @Mapping(target = "version", source = "versionNumber")
    EnforcementAccountTypeCommon toDto(EnforcementAccountTypeEntity entity);

    default List<EnforcementAccountTypeCommon> toDtos(Collection<EnforcementAccountTypeEntity> entities) {
        return entities.stream()
            .map(this::toDto)
            .toList();
    }
}
