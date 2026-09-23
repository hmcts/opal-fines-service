package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = {
        CreditorAccountType.class,
        uk.gov.hmcts.opal.generated.model.CreditorAccountTypeReference.DisplayNameEnum.class
    }
)
public interface CreditorAccountTypeReferenceMapper {

    @Mapping(source = "accountType", target = "type")
    @Mapping(
        target = "displayName",
        expression = "java(DisplayNameEnum.fromValue(CreditorAccountType.getDisplayName(legacy.getAccountType())))"
    )
    uk.gov.hmcts.opal.generated.model.CreditorAccountTypeReference toOpal(
        uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference legacy
    );
}
