package uk.gov.hmcts.opal.authorisation.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.Role;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AuthorizationAspect.class)
@ExtendWith(MockitoExtension.class)
class AuthorizationAspectTest {

    static final UserState USER_STATE = UserState.builder()
        .userName("name")
        .userId(123L)
        .roles(Set.of(Role.builder()
                          .businessUnitId((short) 123)
                          .businessUserId("BU123")
                          .permissions(Set.of(
                              Permission.builder()
                                  .permissionId(54L)
                                  .permissionName("Account Enquiry")
                                  .build()))
                          .build()))
        .build();

    @MockBean
    UserStateService userStateService;

    @MockBean
    AuthorizationAspectService authorizationAspectService;

    @MockBean
    ProceedingJoinPoint joinPoint;

    @MockBean
    AuthorizedAnyRoleAnyRoleHasPermission authorizedAnyRoleAnyRoleHasPermission;

    @Autowired
    AuthorizationAspect authorizationAspect;

    @Test
    void checkAuthorization_WhenAuthorizationHeaderMissing_ThrowsException() throws Throwable {
        Object[] args = {"some argument"};
        when(joinPoint.getArgs()).thenReturn(args);
        when(authorizationAspectService.getRequestHeaderValue(args)).thenReturn(null);
        when(authorizedAnyRoleAnyRoleHasPermission.value()).thenReturn(Permissions.ACCOUNT_ENQUIRY);

        assertThrows(
            MissingRequestHeaderException.class,
            () -> authorizationAspect.checkAuthorization(joinPoint, authorizedAnyRoleAnyRoleHasPermission)
        );
    }

    @Test
    void checkAuthorization_WhenUserHasPermission_ReturnsProceededObject() throws Throwable {
        Object[] args = {"Bearer token"};
        when(joinPoint.getArgs()).thenReturn(args);
        when(authorizationAspectService.getRequestHeaderValue(args)).thenReturn("Bearer token");
        when(authorizationAspectService.getAuthorization("Bearer token"))
            .thenReturn(Optional.of("Bearer token"));
        when(userStateService.getUserStateUsingAuthToken("Bearer token")).thenReturn(USER_STATE);
        when(joinPoint.proceed()).thenReturn(new Object());
        when(authorizedAnyRoleAnyRoleHasPermission.value()).thenReturn(Permissions.ACCOUNT_ENQUIRY);

        Object result = authorizationAspect.checkAuthorization(joinPoint, authorizedAnyRoleAnyRoleHasPermission);

        assertNotNull(result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void checkAuthorization_WhenUserDoesNotHavePermission_ReturnsNull() throws Throwable {
        Object[] args = {"Bearer token"};
        when(joinPoint.getArgs()).thenReturn(args);
        when(authorizationAspectService.getRequestHeaderValue(args)).thenReturn("Bearer token");
        when(authorizationAspectService.getAuthorization("Bearer token"))
            .thenReturn(Optional.of("Bearer token"));
        when(userStateService.getUserStateUsingAuthToken("Bearer token")).thenReturn(USER_STATE);
        when(joinPoint.proceed()).thenReturn(new Object());
        when(authorizedAnyRoleAnyRoleHasPermission.value()).thenReturn(Permissions.ACCOUNT_ENQUIRY_NOTES);

        AccessDeniedException exception = assertThrows(
            AccessDeniedException.class,
            () -> authorizationAspect.checkAuthorization(joinPoint, authorizedAnyRoleAnyRoleHasPermission)
        );

        assertNotNull(exception);
        assertEquals(
            "User does not have the required permission: Account Enquiry - Account Notes",
            exception.getMessage()
        );
        verify(joinPoint, never()).proceed();
    }
}
