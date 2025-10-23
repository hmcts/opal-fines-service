package uk.gov.hmcts.opal.controllers.util;

import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UserStateUtil {

    public static final UserState noFinesPermissionUser() {
        return UserState.builder()
            .userId(999L)
            .userName("no-permissions@users.com")
            .businessUnitUser(Collections.emptySet())
            .build();
    }

    public static UserState allFinesPermissionUser() {
        return new UserState.DeveloperUserState();
    }

    public static UserState allPermissionsUser() {
        return allFinesPermissionUser();
    }

    public static UserState noPermissionsUser() {
        return noFinesPermissionUser();
    }

    public static UserState permissionUser(Short buid, FinesPermission... permissions) {
        return UserState.builder()
            .userId(1L)
            .userName("normal@users.com")
            .businessUnitUser(Set.of(permissions(buid, permissionsFor(permissions))))
            .build();
    }

    public static UserState permissionUser(Short[] buids, FinesPermission... permissions) {
        return UserState.builder()
            .userId(1L)
            .userName("normal@users.com")
            .businessUnitUser(
                Arrays
                    .stream(buids)
                    .map(buid -> permissions(buid, permissionsFor(permissions)))
                    .collect(Collectors.toSet()))
            .build();
    }

    public static UserState permissionUser(Short buid, Permission... permissions) {
        return UserState.builder()
            .userId(1L)
            .userName("normal@users.com")
            .businessUnitUser(Set.of(permissions(buid, permissions)))
            .build();
    }

    public static UserState permissionUser(Set<BusinessUnitUser> permissions) {
        return UserState.builder()
            .userId(1L)
            .userName("normal@users.com")
            .businessUnitUser(permissions)
            .build();
    }

    public static BusinessUnitUser permissions(Short buid, Permission... permissions) {
        return permissions(buid, new HashSet<>(Arrays.asList(permissions)));
    }

    public static BusinessUnitUser permissions(Short buid, Set<Permission> permissions) {
        return BusinessUnitUser.builder()
            .businessUnitUserId("USER01")
            .businessUnitId(buid)
            .permissions(permissions)
            .build();
    }

    public static Set<Permission> permissionsFor(FinesPermission... permissions) {
        return Arrays.stream(permissions)
            .map(UserStateUtil::permissionFor)
            .collect(Collectors.toSet());
    }

    public static Permission permissionFor(FinesPermission permission) {
        return Permission.builder()
            .permissionId(permission.getId())
            .permissionName(permission.getDescription())
            .build();
    }

}
