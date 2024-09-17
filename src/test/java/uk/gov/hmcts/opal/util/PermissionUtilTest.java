package uk.gov.hmcts.opal.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUserPermissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.opal.controllers.UserStateBuilder.createRole;
import static uk.gov.hmcts.opal.controllers.UserStateBuilder.createSinglePermissions;
import static uk.gov.hmcts.opal.controllers.UserStateBuilder.createUserState;

class PermissionUtilTest {

    @Test
    void testCheckRoleHasPermission_success() {
        BusinessUnitUserPermissions role = createRole(createSinglePermissions(41L));
        Permissions permission = Permissions.ACCOUNT_ENQUIRY_NOTES;
        assertTrue(PermissionUtil.checkRoleHasPermission(role, permission));
    }

    @Test
    void testCheckRoleHasPermission_fail1() {
        BusinessUnitUserPermissions role = createRole(Collections.emptySet());
        Permissions permission = Permissions.ACCOUNT_ENQUIRY;
        AccessDeniedException ade = assertThrows(
            AccessDeniedException.class,
            () -> PermissionUtil.checkRoleHasPermission(role, permission));
        assertEquals("User does not have the required permission: Account Enquiry", ade.getMessage());
    }

    @Test
    void testCheckRoleHasPermission_fail2() {
        BusinessUnitUserPermissions role = createRole(createSinglePermissions(41L));
        Permissions permission = Permissions.ACCOUNT_ENQUIRY;
        AccessDeniedException ade = assertThrows(
            AccessDeniedException.class,
            () -> PermissionUtil.checkRoleHasPermission(role, permission));
        assertEquals("User does not have the required permission: Account Enquiry", ade.getMessage());
    }

    @Test
    void testCheckAnyRoleHasPermission_success() {
        UserState userState = createUserState(Set.of(createRole(createSinglePermissions(41L))));
        Permissions permission = Permissions.ACCOUNT_ENQUIRY_NOTES;
        assertTrue(PermissionUtil.checkAnyRoleHasPermission(userState, permission));
    }

    @Test
    void testCheckAnyRoleHasPermission_fail1() {
        UserState userState = createUserState(Set.of(createRole(Collections.emptySet())));
        Permissions permission = Permissions.ACCOUNT_ENQUIRY;
        AccessDeniedException ade = assertThrows(
            AccessDeniedException.class,
            () -> PermissionUtil.checkAnyRoleHasPermission(userState, permission));
        assertEquals("User does not have the required permission: Account Enquiry", ade.getMessage());
    }

    @Test
    void testCheckAnyRoleHasPermission_fail2() {
        UserState userState = createUserState(Set.of(createRole(createSinglePermissions(50L))));
        Permissions permission = Permissions.ACCOUNT_ENQUIRY;
        AccessDeniedException ade = assertThrows(
            AccessDeniedException.class,
            () -> PermissionUtil.checkAnyRoleHasPermission(userState, permission));
        assertEquals("User does not have the required permission: Account Enquiry", ade.getMessage());
    }
}
