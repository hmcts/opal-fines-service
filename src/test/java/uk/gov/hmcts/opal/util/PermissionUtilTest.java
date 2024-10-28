package uk.gov.hmcts.opal.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PermissionUtilTest {

    @Test
    void testCheckBusinessUnitUserHasPermission_success() {
        BusinessUnitUser businessUnitUser = createBusinessUnitUser(createSinglePermissions(41L));
        Permissions permission = Permissions.ACCOUNT_ENQUIRY_NOTES;
        assertTrue(PermissionUtil.checkBusinessUnitUserHasPermission(businessUnitUser, permission));
    }

    @Test
    void testCheckBusinessUnitUserHasPermission_fail1() {
        BusinessUnitUser businessUnitUser = createBusinessUnitUser(Collections.emptySet());
        Permissions permission = Permissions.ACCOUNT_ENQUIRY;
        AccessDeniedException ade = assertThrows(
            AccessDeniedException.class,
            () -> PermissionUtil.checkBusinessUnitUserHasPermission(businessUnitUser, permission));
        assertEquals("User does not have the required permission: Account Enquiry", ade.getMessage());
    }

    @Test
    void testCheckBusinessUnitUserHasPermission_fail2() {
        BusinessUnitUser businessUnitUser = createBusinessUnitUser(createSinglePermissions(41L));
        Permissions permission = Permissions.ACCOUNT_ENQUIRY;
        AccessDeniedException ade = assertThrows(
            AccessDeniedException.class,
            () -> PermissionUtil.checkBusinessUnitUserHasPermission(businessUnitUser, permission));
        assertEquals("User does not have the required permission: Account Enquiry", ade.getMessage());
    }

    @Test
    void testCheckAnyBusinessUnitUserHasPermission_success() {
        UserState userState = createUserState(Set.of(createBusinessUnitUser(createSinglePermissions(41L))));
        Permissions permission = Permissions.ACCOUNT_ENQUIRY_NOTES;
        assertTrue(PermissionUtil.checkAnyBusinessUnitUserHasPermission(userState, permission));
    }

    @Test
    void testCheckAnyBusinessUnitUserHasPermission_fail1() {
        UserState userState = createUserState(Set.of(createBusinessUnitUser(Collections.emptySet())));
        Permissions permission = Permissions.ACCOUNT_ENQUIRY;
        AccessDeniedException ade = assertThrows(
            AccessDeniedException.class,
            () -> PermissionUtil.checkAnyBusinessUnitUserHasPermission(userState, permission));
        assertEquals("User does not have the required permission: Account Enquiry", ade.getMessage());
    }

    @Test
    void testCheckAnyBusinessUnitUserHasPermission_fail2() {
        UserState userState = createUserState(Set.of(createBusinessUnitUser(createSinglePermissions(50L))));
        Permissions permission = Permissions.ACCOUNT_ENQUIRY;
        AccessDeniedException ade = assertThrows(
            AccessDeniedException.class,
            () -> PermissionUtil.checkAnyBusinessUnitUserHasPermission(userState, permission));
        assertEquals("User does not have the required permission: Account Enquiry", ade.getMessage());
    }

    private static UserState createUserState(Set<BusinessUnitUser> businessUnitUser) {
        return UserState.builder()
            .userId(345L)
            .userName("John Smith")
            .businessUnitUser(businessUnitUser)
            .build();
    }

    private static BusinessUnitUser createBusinessUnitUser(Set<Permission> permissions) {
        return BusinessUnitUser.builder()
            .businessUnitUserId("JK0320")
            .businessUnitId((short)50)
            .permissions(permissions)
            .build();
    }

    private static Set<Permission> createSinglePermissions(long id) {
        return Set.of(createPermission(id, "any desc"));
    }

    private static Permission createPermission(long id, String desc) {
        return Permission.builder()
            .permissionId(id)
            .permissionName("Do Stuff")
            .build();
    }
}
