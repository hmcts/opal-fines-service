package uk.gov.hmcts.opal.common.user.client.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.common.user.authorisation.model.Permission;
import uk.gov.hmcts.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.common.user.client.dto.UserStateDto;
import uk.gov.hmcts.opal.common.user.client.dto.BusinessUnitUserDto;
import uk.gov.hmcts.opal.common.user.client.dto.PermissionDto;

@Mapper(componentModel = "spring")
public interface UserStateMapper {


    @Mapping(source = "username", target = "userName")
    @Mapping(source = "businessUnitUsers", target = "businessUnitUser")
    UserState toUserState(UserStateDto userStateDto);


    BusinessUnitUser toBusinessUnitUser(BusinessUnitUserDto businessUnitUserDto);

    Permission toPermission(PermissionDto permissionDto);


}
