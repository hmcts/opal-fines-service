package uk.gov.hmcts.opal.mapper.common;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.hmcts.opal.dto.common.CreditorAccountTypeReference;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;

@Mapper(componentModel = "spring")
public interface CreditorAccountTypeMapper {

    @Mapping(target = "type", source = "enumValue", qualifiedByName = "mapType")
    @Mapping(target = "displayName", source = "enumValue", qualifiedByName = "mapDisplayName")
    CreditorAccountTypeReference toDto(CreditorAccountType enumValue);

    @Named("mapType")
    default String mapType(CreditorAccountType type) {
        return type != null ? type.name() : null;
    }

    @Named("mapDisplayName")
    default String mapDisplayName(CreditorAccountType type) {
        return type != null ? type.getLabel() : null;
    }
}
