package uk.gov.hmcts.opal.authorisation.aspect;

import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.authentication.aspect.UserStateAspectService;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUserPermissions;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {AuthorizationAspect.class, UserStateAspectService.class})
@ExtendWith(MockitoExtension.class)
class AuthorizationAspectTest {

    static final BusinessUnitUserPermissions BUSINESS_UNIT_USER_PERMISSIONS = BusinessUnitUserPermissions.builder()
        .businessUnitId((short) 123)
        .businessUnitUserId("BU123")
        .permissions(Set.of(
            Permission.builder()
                .permissionId(54L)
                .permissionName("Account Enquiry")
                .build()))
        .build();
    static final UserState USER_STATE = UserState.builder()
        .userName("name")
        .userId(123L)
        .businessUnitUserPermissions(Set.of(BUSINESS_UNIT_USER_PERMISSIONS))
        .build();

    @MockBean
    UserStateService userStateService;

    @MockBean
    AuthorizationAspectService authorizationAspectService;

    @MockBean
    ProceedingJoinPoint joinPoint;

    @MockBean
    AuthorizedAnyRoleHasPermission authorizedAnyRoleHasPermission;

    @MockBean
    AuthorizedRoleHasPermission authorizedRoleHasPermission;

    @Autowired
    AuthorizationAspect authorizationAspect;

    @Nested
    class AuthorizedAnyBusinessUnitUserPermissionsHasPermissionAspect {

        @Test
        void checkAuthorization_WhenAuthorizationHeaderMissing_ThrowsException() {
            Object[] args = {"some argument"};
            when(joinPoint.getArgs()).thenReturn(args);
            when(authorizedAnyRoleHasPermission.value()).thenReturn(Permissions.ACCOUNT_ENQUIRY);

            Assertions.assertThrows(
                MissingRequestHeaderException.class,
                () -> authorizationAspect.checkAuthorization(joinPoint, authorizedAnyRoleHasPermission)
            );
        }

        @Test
        @SneakyThrows
        void checkAuthorization_WhenUserHasPermission_ReturnsProceededObject() {
            Object[] args = {"Bearer token"};
            when(joinPoint.getArgs()).thenReturn(args);
            when(authorizationAspectService.getUserState(args)).thenReturn(Optional.ofNullable(USER_STATE));

            when(joinPoint.proceed()).thenReturn(new Object());
            when(authorizedAnyRoleHasPermission.value()).thenReturn(Permissions.ACCOUNT_ENQUIRY);

            Object result = authorizationAspect.checkAuthorization(joinPoint, authorizedAnyRoleHasPermission);

            assertNotNull(result);
            verify(joinPoint, times(1)).proceed();
        }

        @Test
        void checkAuthorization_WhenUserDoesNotHavePermission_ThrowsException() throws Throwable {
            Object[] args = {"Bearer token"};
            when(joinPoint.getArgs()).thenReturn(args);
            when(authorizationAspectService.getUserState(args)).thenReturn(Optional.ofNullable(USER_STATE));

            when(joinPoint.proceed()).thenReturn(new Object());
            when(authorizedAnyRoleHasPermission.value()).thenReturn(Permissions.ACCOUNT_ENQUIRY_NOTES);

            AccessDeniedException exception = Assertions.assertThrows(
                AccessDeniedException.class,
                () -> authorizationAspect.checkAuthorization(joinPoint, authorizedAnyRoleHasPermission)
            );

            assertNotNull(exception);
            assertEquals(
                "User does not have the required permission: Account Enquiry - Account Notes",
                exception.getMessage()
            );
            verify(joinPoint, never()).proceed();
        }
    }

    @Nested
    class AuthorizedBusinessUnitUserPermissionsHasPermissionAspect {

        @Test
        void checkAuthorization_WhenUserHasPermission_ReturnsProceededObject() throws Throwable {
            Object[] args = {"Bearer token"};
            when(joinPoint.getArgs()).thenReturn(args);

            when(authorizationAspectService.getUserState(args)).thenReturn(Optional.ofNullable(USER_STATE));

            when(joinPoint.proceed()).thenReturn(new Object());
            when(authorizedRoleHasPermission.value()).thenReturn(Permissions.ACCOUNT_ENQUIRY);
            when(authorizationAspectService.getRole(any(), any())).thenReturn(BUSINESS_UNIT_USER_PERMISSIONS);

            Object result = authorizationAspect.checkAuthorization(joinPoint, authorizedRoleHasPermission);

            assertNotNull(result);
            verify(joinPoint, times(1)).proceed();
        }

        @Test
        void checkAuthorization_WhenUserDoesNotHavePermission_ThrowsException() throws Throwable {
            Object[] args = {"Bearer token"};
            when(joinPoint.getArgs()).thenReturn(args);
            when(authorizationAspectService.getUserState(args)).thenReturn(Optional.ofNullable(USER_STATE));

            when(joinPoint.proceed()).thenReturn(new Object());
            when(authorizedRoleHasPermission.value()).thenReturn(Permissions.ACCOUNT_ENQUIRY_NOTES);
            when(authorizationAspectService.getRole(any(), any())).thenReturn(BUSINESS_UNIT_USER_PERMISSIONS);

            AccessDeniedException exception = Assertions.assertThrows(
                AccessDeniedException.class,
                () -> authorizationAspect.checkAuthorization(joinPoint, authorizedRoleHasPermission)
            );

            assertNotNull(exception);
            assertEquals(
                "User does not have the required permission: Account Enquiry - Account Notes",
                exception.getMessage()
            );
            verify(joinPoint, never()).proceed();
        }
    }
}
