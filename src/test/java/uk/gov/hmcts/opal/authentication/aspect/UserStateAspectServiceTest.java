package uk.gov.hmcts.opal.authentication.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authorisation.aspect.AuthorizationAspectService;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private ProceedingJoinPoint joinPoint;

    private Object[] args;

    private static final UserState USER_STATE = UserState.builder()
        .userName("name")
        .userId(123L)
        .businessUnitUser(Set.of(BusinessUnitUser.builder()
                          .businessUnitId((short) 123)
                          .businessUnitUserId("BU123")
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

    @Nested class GetUserState {

        @Test
        void getUserState_shouldReturnUserStateFromArgs() {
            when(joinPoint.getArgs()).thenReturn(args);
            when(authorizationAspectService.getUserState(args)).thenReturn(Optional.of(USER_STATE));

            UserState actualUserState = userStateAspectService.getUserState(joinPoint);

            assertEquals(USER_STATE, actualUserState);
            verify(authorizationAspectService).getUserState(args);
            verifyNoMoreInteractions(authorizationAspectService, userStateService);
        }

        @Test
        void getUserState_shouldFetchUserStateUsingBearerToken() {
            String authHeaderValue = "Bearer someToken";
            String bearerToken = "someToken";
            UserState expectedUserState = USER_STATE;

            when(joinPoint.getArgs()).thenReturn(args);
            when(authorizationAspectService.getUserState(args)).thenReturn(Optional.empty());
            when(authorizationAspectService.getAccessTokenParam(any())).thenReturn(Optional.of(authHeaderValue));
            when(authorizationAspectService.getAuthorization(authHeaderValue)).thenReturn(Optional.of(bearerToken));
            when(userStateService.checkForAuthorisedUser(bearerToken)).thenReturn(expectedUserState);

            UserState actualUserState = userStateAspectService.getUserState(joinPoint);

            assertEquals(expectedUserState, actualUserState);
            verify(authorizationAspectService).getUserState(args);
            verify(authorizationAspectService).getAccessTokenParam(joinPoint);
            verify(authorizationAspectService).getAuthorization(authHeaderValue);
            verify(userStateService).checkForAuthorisedUser(bearerToken);
        }

    }

    @Nested class GetUserStateSupplier {
        @Test
        void getUserStateSupplier_shouldThrowExceptionWhenAuthorizationHeaderMissing() {
            String authHeaderValue = "Bearer someToken";

            when(authorizationAspectService.getAccessTokenParam(any())).thenReturn(Optional.of(authHeaderValue));
            when(authorizationAspectService.getAuthorization(authHeaderValue)).thenReturn(Optional.empty());

            Supplier<UserState> userStateSupplier = userStateAspectService.getUserStateSupplier(joinPoint);

            assertThrows(MissingRequestHeaderException.class, userStateSupplier::get);
            verify(authorizationAspectService).getAccessTokenParam(joinPoint);
            verify(authorizationAspectService).getAuthorization(authHeaderValue);
            verifyNoInteractions(userStateService);
        }

        @Test
        void getUserStateSupplier_shouldReturnUserStateUsingBearerToken() {
            String authHeaderValue = "Bearer someToken";
            String bearerToken = "someToken";
            UserState expectedUserState = USER_STATE;

            when(authorizationAspectService.getAccessTokenParam(joinPoint)).thenReturn(Optional.of(authHeaderValue));
            when(authorizationAspectService.getAuthorization(authHeaderValue)).thenReturn(Optional.of(bearerToken));
            when(userStateService.checkForAuthorisedUser(bearerToken)).thenReturn(expectedUserState);

            Supplier<UserState> userStateSupplier = userStateAspectService.getUserStateSupplier(joinPoint);

            UserState actualUserState = userStateSupplier.get();

            assertEquals(expectedUserState, actualUserState);
            verify(authorizationAspectService).getAccessTokenParam(joinPoint);
            verify(authorizationAspectService).getAuthorization(authHeaderValue);
            verify(userStateService).checkForAuthorisedUser(bearerToken);
        }
    }

}
