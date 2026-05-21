package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.common.CreditorAccountTypeReference;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = CreditorAccountType.class
)
public interface CreditorAccountTypeReferenceMapper {

    @Mapping(source = "accountType", target = "type")
    @Mapping(target = "displayName", expression = "java(CreditorAccountType.getDisplayName(legacy.getAccountType()))")
    CreditorAccountTypeReference toOpal(
        uk.gov.hmcts.opal.dto.legacy.common.CreditorAccountTypeReference legacy
    );
}
