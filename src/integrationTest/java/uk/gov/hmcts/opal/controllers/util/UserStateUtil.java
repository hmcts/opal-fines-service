package uk.gov.hmcts.opal.controllers.util;

import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUserPermissions;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UserStateUtil {

    public static final UserState noPermissionsUser() {
        return UserState.builder()
            .userId(999L)
            .userName("no-permissions@users.com")
            .build();
    }

    public static final UserState permissionUser(Short buid, Permissions... permissions) {
        return UserState.builder()
            .userId(1L)
            .userName("normal@users.com")
            .businessUnitUserPermissions(Set.of(permissions(buid, permissionsFor(permissions))))
            .build();
    }

    public static final UserState permissionUser(Short buid, Permission... permissions) {
        return UserState.builder()
            .userId(1L)
            .userName("normal@users.com")
            .businessUnitUserPermissions(Set.of(permissions(buid, permissions)))
            .build();
    }

    public static final UserState permissionUser(Set<BusinessUnitUserPermissions> permissions) {
        return UserState.builder()
            .userId(1L)
            .userName("normal@users.com")
            .businessUnitUserPermissions(permissions)
            .build();
    }

    public static final BusinessUnitUserPermissions permissions(Short buid, Permission... permissions) {
        return permissions(buid, new HashSet<>(Arrays.asList(permissions)));
    }

    public static final BusinessUnitUserPermissions permissions(Short buid, Set<Permission> permissions) {
        return BusinessUnitUserPermissions.builder()
            .businessUnitUserId("USER01")
            .businessUnitId(buid)
            .permissions(permissions)
            .build();
    }

    public static final Set<Permission> permissionsFor(Permissions... permissions) {
        return Arrays.stream(permissions).map(p -> new Permission(p.id, p.description)).collect(Collectors.toSet());
    }

    public static final Permission permissionFor(Permissions p) {
        return new Permission(p.id, p.description);
    }

}
