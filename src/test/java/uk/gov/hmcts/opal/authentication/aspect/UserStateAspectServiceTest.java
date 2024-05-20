package uk.gov.hmcts.opal.authentication.aspect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authorisation.aspect.AuthorizationAspectService;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.Role;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStateAspectServiceTest {

    @Mock
    private UserStateService userStateService;

    @Mock
    private AuthorizationAspectService authorizationAspectService;

    @InjectMocks
    private UserStateAspectService userStateAspectService;

    private Object[] args;

    private static final UserState USER_STATE = UserState.builder()
        .userName("name")
        .userId(123L)
        .roles(Set.of(Role.builder()
                          .businessUnitId((short) 123)
                          .businessUserId("BU123")
                          .permissions(Set.of(
                              Permission.builder()
                                  .permissionId(1L)
                                  .permissionName("Notes")
                                  .build()))
                          .build()))
        .build();

    @BeforeEach
    void setUp() {
        args = new Object[]{};
    }

    @Test
    void getUserState_shouldReturnUserStateFromArgs() {
        when(authorizationAspectService.getUserState(args)).thenReturn(Optional.of(USER_STATE));

        UserState actualUserState = userStateAspectService.getUserState(args);

        assertEquals(USER_STATE, actualUserState);
        verify(authorizationAspectService).getUserState(args);
        verifyNoMoreInteractions(authorizationAspectService, userStateService);
    }

    @Test
    void getUserState_shouldFetchUserStateUsingBearerToken() {
        String authHeaderValue = "Bearer someToken";
        String bearerToken = "someToken";
        UserState expectedUserState = USER_STATE;

        when(authorizationAspectService.getUserState(args)).thenReturn(Optional.empty());
        when(authorizationAspectService.getRequestHeaderValue(args)).thenReturn(authHeaderValue);
        when(authorizationAspectService.getAuthorization(authHeaderValue)).thenReturn(Optional.of(bearerToken));
        when(userStateService.getUserStateUsingAuthToken(bearerToken)).thenReturn(expectedUserState);

        UserState actualUserState = userStateAspectService.getUserState(args);

        assertEquals(expectedUserState, actualUserState);
        verify(authorizationAspectService).getUserState(args);
        verify(authorizationAspectService).getRequestHeaderValue(args);
        verify(authorizationAspectService).getAuthorization(authHeaderValue);
        verify(userStateService).getUserStateUsingAuthToken(bearerToken);
    }

    @Test
    void getUserStateSupplier_shouldThrowExceptionWhenAuthorizationHeaderMissing() {
        String authHeaderValue = "Bearer someToken";

        when(authorizationAspectService.getRequestHeaderValue(args)).thenReturn(authHeaderValue);
        when(authorizationAspectService.getAuthorization(authHeaderValue)).thenReturn(Optional.empty());

        Supplier<UserState> userStateSupplier = userStateAspectService.getUserStateSupplier(args);

        assertThrows(MissingRequestHeaderException.class, userStateSupplier::get);
        verify(authorizationAspectService).getRequestHeaderValue(args);
        verify(authorizationAspectService).getAuthorization(authHeaderValue);
        verifyNoInteractions(userStateService);
    }

    @Test
    void getUserStateSupplier_shouldReturnUserStateUsingBearerToken() {
        String authHeaderValue = "Bearer someToken";
        String bearerToken = "someToken";
        UserState expectedUserState = USER_STATE;

        when(authorizationAspectService.getRequestHeaderValue(args)).thenReturn(authHeaderValue);
        when(authorizationAspectService.getAuthorization(authHeaderValue)).thenReturn(Optional.of(bearerToken));
        when(userStateService.getUserStateUsingAuthToken(bearerToken)).thenReturn(expectedUserState);

        Supplier<UserState> userStateSupplier = userStateAspectService.getUserStateSupplier(args);

        UserState actualUserState = userStateSupplier.get();

        assertEquals(expectedUserState, actualUserState);
        verify(authorizationAspectService).getRequestHeaderValue(args);
        verify(authorizationAspectService).getAuthorization(authHeaderValue);
        verify(userStateService).getUserStateUsingAuthToken(bearerToken);
    }
}
