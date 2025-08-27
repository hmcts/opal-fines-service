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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.authentication.aspect.UserStateAspectService;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.UserStateService;

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

    static final BusinessUnitUser BUSINESS_UNIT_USER = BusinessUnitUser.builder()
        .businessUnitId((short) 123)
        .businessUnitUserId("BU123")
        .permissions(Set.of(
            Permission.builder()
                .permissionId(3L)
                .permissionName("Account Enquiry")
                .build()))
        .build();
    static final UserState USER_STATE = UserState.builder()
        .userName("name")
        .userId(123L)
        .businessUnitUser(Set.of(BUSINESS_UNIT_USER))
        .build();

    @MockitoBean
    UserStateService userStateService;

    @MockitoBean
    AuthorizationAspectService authorizationAspectService;

    @MockitoBean
    ProceedingJoinPoint joinPoint;

    @MockitoBean
    AuthorizedAnyBusinessUnitUserHasPermission authorizedAnyBusinessUnitUserHasPermission;

    @MockitoBean
    AuthorizedBusinessUnitUserHasPermission authorizedBusinessUnitUserHasPermission;

    @Autowired
    AuthorizationAspect authorizationAspect;

    @Nested
    class AuthorizedAnyBusinessUnitUserHasPermissionAspect {

        @Test
        void checkAuthorization_WhenAuthorizationHeaderMissing_ThrowsException() {
            Object[] args = {"some argument"};
            when(joinPoint.getArgs()).thenReturn(args);
            when(authorizedAnyBusinessUnitUserHasPermission.value()).thenReturn(Permissions.ACCOUNT_ENQUIRY);

            Assertions.assertThrows(
                MissingRequestHeaderException.class,
                () -> authorizationAspect.checkAuthorization(joinPoint, authorizedAnyBusinessUnitUserHasPermission)
            );
        }

        @Test
        @SneakyThrows
        void checkAuthorization_WhenUserHasPermission_ReturnsProceededObject() {
            Object[] args = {"Bearer token"};
            when(joinPoint.getArgs()).thenReturn(args);
            when(authorizationAspectService.getUserState(args)).thenReturn(Optional.ofNullable(USER_STATE));

            when(joinPoint.proceed()).thenReturn(new Object());
            when(authorizedAnyBusinessUnitUserHasPermission.value()).thenReturn(Permissions.ACCOUNT_ENQUIRY);

            Object result = authorizationAspect.checkAuthorization(joinPoint,
                                                                   authorizedAnyBusinessUnitUserHasPermission
            );

            assertNotNull(result);
            verify(joinPoint, times(1)).proceed();
        }

        @Test
        void checkAuthorization_WhenUserDoesNotHavePermission_ThrowsException() throws Throwable {
            Object[] args = {"Bearer token"};
            when(joinPoint.getArgs()).thenReturn(args);
            when(authorizationAspectService.getUserState(args)).thenReturn(Optional.ofNullable(USER_STATE));

            when(joinPoint.proceed()).thenReturn(new Object());
            when(authorizedAnyBusinessUnitUserHasPermission.value()).thenReturn(Permissions.ACCOUNT_ENQUIRY_NOTES);

            AccessDeniedException exception = Assertions.assertThrows(
                AccessDeniedException.class,
                () -> authorizationAspect.checkAuthorization(joinPoint, authorizedAnyBusinessUnitUserHasPermission)
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
    class AuthorizedBusinessUnitUserHasPermissionAspect {

        @Test
        void checkAuthorization_WhenUserHasPermission_ReturnsProceededObject() throws Throwable {
            Object[] args = {"Bearer token"};
            when(joinPoint.getArgs()).thenReturn(args);

            when(authorizationAspectService.getUserState(args)).thenReturn(Optional.ofNullable(USER_STATE));

            when(joinPoint.proceed()).thenReturn(new Object());
            when(authorizedBusinessUnitUserHasPermission.value()).thenReturn(Permissions.ACCOUNT_ENQUIRY);
            when(authorizationAspectService.getBusinessUnitUser(any(), any()))
                .thenReturn(BUSINESS_UNIT_USER);

            Object result = authorizationAspect.checkAuthorization(joinPoint, authorizedBusinessUnitUserHasPermission);

            assertNotNull(result);
            verify(joinPoint, times(1)).proceed();
        }

        @Test
        void checkAuthorization_WhenUserDoesNotHavePermission_ThrowsException() throws Throwable {
            Object[] args = {"Bearer token"};
            when(joinPoint.getArgs()).thenReturn(args);
            when(authorizationAspectService.getUserState(args)).thenReturn(Optional.ofNullable(USER_STATE));

            when(joinPoint.proceed()).thenReturn(new Object());
            when(authorizedBusinessUnitUserHasPermission.value()).thenReturn(Permissions.ACCOUNT_ENQUIRY_NOTES);
            when(authorizationAspectService.getBusinessUnitUser(any(), any()))
                .thenReturn(BUSINESS_UNIT_USER);

            AccessDeniedException exception = Assertions.assertThrows(
                AccessDeniedException.class,
                () -> authorizationAspect.checkAuthorization(joinPoint, authorizedBusinessUnitUserHasPermission)
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
