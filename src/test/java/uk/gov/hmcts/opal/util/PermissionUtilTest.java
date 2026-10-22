package uk.gov.hmcts.opal.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.common.util.SecurityUtil;


class PermissionUtilTest {

    @Test
    void testCheckPermissionInBusinessUnit_permissionGranted() {
        FinesPermission permission = FinesPermission.MANAGE_ALTERNATE_PAYMENT_REFERENCES;
        Short businessUnitId = 50;
        OpalJwtAuthenticationToken token = mock(OpalJwtAuthenticationToken.class);
        when(token.hasPermissionInBusinessUnit(permission, businessUnitId)).thenReturn(true);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser).thenReturn(token);

            assertDoesNotThrow(() -> PermissionUtil.checkPermissionInBusinessUnit(permission, businessUnitId));

            verify(token).hasPermissionInBusinessUnit(permission, businessUnitId);
        }
    }

    @Test
    void testCheckPermissionInBusinessUnit_permissionDenied() {
        FinesPermission permission = FinesPermission.MANAGE_ALTERNATE_PAYMENT_REFERENCES;
        Short businessUnitId = 50;
        OpalJwtAuthenticationToken token = mock(OpalJwtAuthenticationToken.class);
        when(token.hasPermissionInBusinessUnit(permission, businessUnitId)).thenReturn(false);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getOpalJwtAuthenticationTokenForCurrentUser).thenReturn(token);

            AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> PermissionUtil.checkPermissionInBusinessUnit(permission, businessUnitId));

            assertEquals(
                "User does not have the required permission: Manage Alternate Payment References in business unit: 50",
                exception.getMessage());
            verify(token).hasPermissionInBusinessUnit(permission, businessUnitId);
        }
    }

    @Test
    void findsRequiredBusinessUnitUserInDomain() {
        BusinessUnitUser user = createBusinessUnitUser(Collections.emptySet());
        DomainBusinessUnitUsers domainUsers = new DomainBusinessUnitUsers(List.of(user));

        assertSame(user, PermissionUtil.getRequiredBusinessUnitUser(domainUsers, (short) 50));
    }

    @Test
    void rejectsBusinessUnitOutsideDomainMembership() {
        DomainBusinessUnitUsers domainUsers = new DomainBusinessUnitUsers(
            List.of(createBusinessUnitUser(Collections.emptySet())));

        assertThrows(AccessDeniedException.class,
            () -> PermissionUtil.getRequiredBusinessUnitUser(domainUsers, (short) 78));
    }

    @Test
    void testViewCreditorBacsPermissionDescriptor() {
        assertEquals(11L, FinesPermission.VIEW_CREDITOR_BACS.getId());
        assertEquals("View Creditor BACS", FinesPermission.VIEW_CREDITOR_BACS.getDescription());
    }

    @Test
    void testCheckBusinessUnitUserHasPermission_success() {
        BusinessUnitUser businessUnitUser = createBusinessUnitUser(createSingleFinesPermission(2L));
        FinesPermission permission = FinesPermission.ACCOUNT_ENQUIRY_NOTES;
        assertTrue(PermissionUtil.checkBusinessUnitUserHasPermission(businessUnitUser, permission));
    }

    @Test
    void testCheckBusinessUnitUserHasPermission_fail1() {
        BusinessUnitUser businessUnitUser = createBusinessUnitUser(Collections.emptySet());
        FinesPermission permission = FinesPermission.ACCOUNT_ENQUIRY;
        AccessDeniedException ade = assertThrows(
            AccessDeniedException.class,
            () -> PermissionUtil.checkBusinessUnitUserHasPermission(businessUnitUser, permission));
        assertEquals("User does not have the required permission: Account Enquiry", ade.getMessage());
    }

    @Test
    void testCheckBusinessUnitUserHasPermission_fail2() {
        BusinessUnitUser businessUnitUser = createBusinessUnitUser(createSingleFinesPermission(2L));
        FinesPermission permission = FinesPermission.ACCOUNT_ENQUIRY;
        AccessDeniedException ade = assertThrows(
            AccessDeniedException.class,
            () -> PermissionUtil.checkBusinessUnitUserHasPermission(businessUnitUser, permission));
        assertEquals("User does not have the required permission: Account Enquiry", ade.getMessage());
    }

    @Test
    void testCheckAnyBusinessUnitUserHasPermission_success() {
        UserState userState = createUserState(Set.of(createBusinessUnitUser(createSingleFinesPermission(2L))));
        FinesPermission permission = FinesPermission.ACCOUNT_ENQUIRY_NOTES;
        assertTrue(PermissionUtil.checkAnyBusinessUnitUserHasPermission(userState, permission));
    }

    @Test
    void testCheckAnyBusinessUnitUserHasPermission_fail1() {
        UserState userState = createUserState(Set.of(createBusinessUnitUser(Collections.emptySet())));
        FinesPermission permission = FinesPermission.ACCOUNT_ENQUIRY;
        AccessDeniedException ade = assertThrows(
            AccessDeniedException.class,
            () -> PermissionUtil.checkAnyBusinessUnitUserHasPermission(userState, permission));
        assertEquals("User does not have the required permission: Account Enquiry", ade.getMessage());
    }

    @Test
    void testCheckAnyBusinessUnitUserHasPermission_fail2() {
        UserState userState = createUserState(Set.of(createBusinessUnitUser(createSingleFinesPermission(50L))));
        FinesPermission permission = FinesPermission.ACCOUNT_ENQUIRY;
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
            .businessUnitId((short) 50)
            .permissions(permissions)
            .build();
    }

    private static Set<Permission> createSingleFinesPermission(long id) {
        return Set.of(createPermission(id, "any desc"));
    }

    private static Permission createPermission(long id, String desc) {
        return Permission.builder()
            .permissionId(id)
            .permissionName("Do Stuff")
            .build();
    }
}
