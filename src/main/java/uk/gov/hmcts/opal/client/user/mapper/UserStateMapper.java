package uk.gov.hmcts.opal.client.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.client.user.dto.BusinessUnitUserDto;
import uk.gov.hmcts.opal.client.user.dto.PermissionDto;
import uk.gov.hmcts.opal.client.user.dto.UserStateDto;

@Mapper(componentModel = "spring")
public interface UserStateMapper {


    @Mapping(source = "username", target = "userName")
    @Mapping(source = "businessUnitUsers", target = "businessUnitUser")
    UserState toUserState(UserStateDto userStateDto);


    BusinessUnitUser toBusinessUnitUser(BusinessUnitUserDto businessUnitUserDto);

    Permission toPermission(PermissionDto permissionDto);


}
