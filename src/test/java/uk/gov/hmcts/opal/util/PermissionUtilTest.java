package uk.gov.hmcts.opal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUserV2;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;
import uk.gov.hmcts.opal.common.user.authorisation.model.PermissionV2;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;

class PermissionUtilTest {

    @Test
    void findsRequiredBusinessUnitUserInDomain() {
        BusinessUnitUserV2 user = createBusinessUnitUser(Collections.emptySet());
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
        assertEquals("VIEW_CREDITOR_BACS", FinesPermission.VIEW_CREDITOR_BACS.getPermissionCode());
        assertEquals("View Creditor BACS", FinesPermission.VIEW_CREDITOR_BACS.getPermissionName());
    }

    @Test
    void testCheckBusinessUnitUserHasPermission_success() {
        BusinessUnitUserV2 businessUnitUser = createBusinessUnitUser(
            createSingleFinesPermission(FinesPermission.ACCOUNT_ENQUIRY_NOTES.getPermissionCode()));
        FinesPermission permission = FinesPermission.ACCOUNT_ENQUIRY_NOTES;
        assertTrue(PermissionUtil.checkBusinessUnitUserHasPermission(businessUnitUser, permission));
    }

    @Test
    void testCheckBusinessUnitUserHasPermission_fail1() {
        BusinessUnitUserV2 businessUnitUser = createBusinessUnitUser(Collections.emptySet());
        FinesPermission permission = FinesPermission.ACCOUNT_ENQUIRY;
        AccessDeniedException ade = assertThrows(
            AccessDeniedException.class,
            () -> PermissionUtil.checkBusinessUnitUserHasPermission(businessUnitUser, permission));
        assertEquals("User does not have the required permission: Account Enquiry", ade.getMessage());
    }

    @Test
    void testCheckBusinessUnitUserHasPermission_fail2() {
        BusinessUnitUserV2 businessUnitUser = createBusinessUnitUser(
            createSingleFinesPermission(FinesPermission.ACCOUNT_ENQUIRY_NOTES.getPermissionCode()));
        FinesPermission permission = FinesPermission.ACCOUNT_ENQUIRY;
        AccessDeniedException ade = assertThrows(
            AccessDeniedException.class,
            () -> PermissionUtil.checkBusinessUnitUserHasPermission(businessUnitUser, permission));
        assertEquals("User does not have the required permission: Account Enquiry", ade.getMessage());
    }

    @Test
    void testCheckAnyBusinessUnitUserHasPermission_success() {
        UserStateV2 userState = createUserState(Set.of(createBusinessUnitUser(
            createSingleFinesPermission(FinesPermission.ACCOUNT_ENQUIRY_NOTES.getPermissionCode()))));
        FinesPermission permission = FinesPermission.ACCOUNT_ENQUIRY_NOTES;
        assertTrue(PermissionUtil.checkAnyBusinessUnitUserHasPermission(userState, permission));
    }

    @Test
    void testCheckAnyBusinessUnitUserHasPermission_fail1() {
        UserStateV2 userState = createUserState(Set.of(createBusinessUnitUser(Collections.emptySet())));
        FinesPermission permission = FinesPermission.ACCOUNT_ENQUIRY;
        AccessDeniedException ade = assertThrows(
            AccessDeniedException.class,
            () -> PermissionUtil.checkAnyBusinessUnitUserHasPermission(userState, permission));
        assertEquals("User does not have the required permission: Account Enquiry", ade.getMessage());
    }

    @Test
    void testCheckAnyBusinessUnitUserHasPermission_fail2() {
        UserStateV2 userState =
            createUserState(Set.of(createBusinessUnitUser(
                createSingleFinesPermission(FinesPermission.AMEND_PAYMENT_TERMS.getPermissionCode()))));
        FinesPermission permission = FinesPermission.ACCOUNT_ENQUIRY;
        AccessDeniedException ade = assertThrows(
            AccessDeniedException.class,
            () -> PermissionUtil.checkAnyBusinessUnitUserHasPermission(userState, permission));
        assertEquals("User does not have the required permission: Account Enquiry", ade.getMessage());
    }

    private static UserStateV2 createUserState(Set<BusinessUnitUserV2> businessUnitUser) {
        return UserStateV2.builder()
            .userId(345L)
            .username("John Smith")
            .domains(Map.of(Domain.FINES, DomainBusinessUnitUsers.builder()
                .businessUnitUsers(List.copyOf(businessUnitUser))
                .build()))
            .build();
    }

    private static BusinessUnitUserV2 createBusinessUnitUser(Set<PermissionV2> permissions) {
        return BusinessUnitUserV2.builder()
            .businessUnitUserId("JK0320")
            .businessUnitId((short)50)
            .permissions(permissions)
            .build();
    }

    private static Set<PermissionV2> createSingleFinesPermission(String code) {
        return Set.of(PermissionV2.fromPermissionCode(code));
    }

    private static PermissionV2 createPermission(String name) {
        return PermissionV2.fromPermissionName(name);
    }
}
