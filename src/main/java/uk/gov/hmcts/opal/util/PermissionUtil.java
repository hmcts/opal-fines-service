package uk.gov.hmcts.opal.util;

import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.Role;
import uk.gov.hmcts.opal.authorisation.model.UserState;

public class PermissionUtil {


    public static Role getRequiredRole(UserState userState, short businessUnitId) {
        return userState.getRoleForBusinessUnit(businessUnitId).orElseThrow(() -> new
            AccessDeniedException("User does not have an assigned role in business unit: " + businessUnitId));
    }

    public static boolean checkRoleHasPermission(Role role, Permissions permission) {
        if (role.doesNotHavePermission(permission)) {
            throw new AccessDeniedException("User does not have the required permission: " + permission.description);
        }
        return true;
    }

    public static boolean checkAnyRoleHasPermission(UserState userState, Permissions permission) {
        if (userState.noRoleHasPermission(permission)) {
            throw new AccessDeniedException("User does not have the required permission: " + permission.description);
        }
        return true;
    }

}
