package uk.gov.hmcts.opal.mapper.common;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;

@Mapper(componentModel = "spring")
public interface CreditorAccountTypeMapper {

    @Mapping(target = "type", source = "enumValue", qualifiedByName = "mapType")
    @Mapping(target = "displayName", expression = "java(toGeneratedDisplayName(enumValue))")
    uk.gov.hmcts.opal.generated.model.CreditorAccountTypeReference toGeneratedDto(CreditorAccountType enumValue);

    @Named("mapType")
    default String mapType(CreditorAccountType type) {
        return type != null ? type.name() : null;
    }

    @Named("mapDisplayName")
    default String mapDisplayName(CreditorAccountType type) {
        return type != null ? type.getLabel() : null;
    }

    default uk.gov.hmcts.opal.generated.model.CreditorAccountTypeReference.DisplayNameEnum toGeneratedDisplayName(
        CreditorAccountType type
    ) {
        String displayName = mapDisplayName(type);
        return displayName == null
            ? null
            : uk.gov.hmcts.opal.generated.model.CreditorAccountTypeReference.DisplayNameEnum.fromValue(displayName);
    }
}
