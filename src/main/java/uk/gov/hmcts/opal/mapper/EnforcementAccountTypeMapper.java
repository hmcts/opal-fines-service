package uk.gov.hmcts.opal.mapper;

import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementAccountTypeEntity;
import uk.gov.hmcts.opal.generated.model.EnforcementAccountTypeCommon;

@Mapper(componentModel = "spring")
public interface EnforcementAccountTypeMapper {

    @Mapping(target = "id", source = "enforcementAccountTypeId")
    @Mapping(target = "path", source = "accountTypePath.value")
    @Mapping(target = "version", source = "versionNumber")
    @Mapping(target = "enforcementAccountType", source = "enforcementAccountType.code")
    @Mapping(target = "accountType", source = "accountType.code")
    EnforcementAccountTypeCommon toEnforcementAccountTypeCommon(EnforcementAccountTypeEntity entity);

    default List<EnforcementAccountTypeCommon> toEnforcementAccountTypeCommonList(
        Collection<EnforcementAccountTypeEntity> entities) {
        return entities.stream()
            .map(this::toEnforcementAccountTypeCommon)
            .toList();
    }
}
