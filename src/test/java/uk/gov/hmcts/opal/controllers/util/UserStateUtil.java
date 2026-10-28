package uk.gov.hmcts.opal.controllers.util;

import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUserV2;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;
import uk.gov.hmcts.opal.common.user.authorisation.model.PermissionV2;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UserStateUtil {

    public static UserStateV2 noFinesPermissionUser() {
        return UserStateV2.builder()
            .userId(999L)
            .username("no-permissions@users.com")
            .name("No Permissions User")
            .domains(Map.of(Domain.FINES, domainBusinessUnitUsers(List.of())))
            .build();
    }

    public static UserStateV2 noPermissionsUser() {
        return noFinesPermissionUser();
    }

    public static UserStateV2 allFinesPermissionUser() {
        return new UserStateV2.DeveloperUserState();
    }

    public static UserStateV2 allPermissionsUser() {
        return allFinesPermissionUser();
    }

    public static UserStateV2 permissionUser(Short buid, FinesPermission... permissions) {
        return UserStateV2.builder()
            .userId(1L)
            .username("normal@users.com")
            .name("Normal User")
            .domains(Map.of(Domain.FINES,
                domainBusinessUnitUsers(List.of(permissions(buid, permissionsFor(permissions))))))
            .build();
    }

    public static UserStateV2 permissionUser(Short buid, PermissionV2... permissions) {
        return UserStateV2.builder()
            .userId(1L)
            .username("normal@users.com")
            .name("Normal User")
            .domains(Map.of(Domain.FINES, domainBusinessUnitUsers(List.of(permissions(buid, permissions)))))
            .build();
    }

    public static UserStateV2 permissionUser(Set<BusinessUnitUserV2> permissions) {
        return UserStateV2.builder()
            .userId(1L)
            .username("normal@users.com")
            .name("Normal User")
            .domains(Map.of(Domain.FINES, domainBusinessUnitUsers(List.copyOf(permissions))))
            .build();
    }

    public static BusinessUnitUserV2 permissions(Short buid, PermissionV2... permissions) {
        return permissions(buid, new HashSet<>(Arrays.asList(permissions)));
    }

    public static BusinessUnitUserV2 permissions(Short buid, Set<PermissionV2> permissions) {
        return BusinessUnitUserV2.builder()
            .businessUnitUserId("USER01")
            .businessUnitId(buid)
            .permissions(permissions)
            .build();
    }

    public static Set<PermissionV2> permissionsFor(FinesPermission... permissions) {
        return Arrays.stream(permissions)
            .map(UserStateUtil::permissionFor)
            .collect(Collectors.toSet());
    }

    public static PermissionV2 permissionFor(FinesPermission permission) {
        return permission.toCommonPermission();
    }

    private static DomainBusinessUnitUsers domainBusinessUnitUsers(List<BusinessUnitUserV2> businessUnitUsers) {
        return DomainBusinessUnitUsers.builder().businessUnitUsers(businessUnitUsers).build();
    }

}
