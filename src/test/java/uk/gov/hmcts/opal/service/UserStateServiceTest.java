package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.client.mapper.UserStateMapper;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("java:S1874")
class UserStateServiceTest {

    @Mock
    private AccessTokenService tokenService;

    @Mock
    private UserStateMapper userStateMapper;

    @Mock
    private UserStateClientService userStateClientService;

    @InjectMocks
    private UserStateService userStateService;

    @Test
    void testCheckForAuthorisedUser_usesCurrentAuthenticatedUserStateWhenAvailable() {
        // Arrange
        final UserState expectedUserState = mock(UserState.class);
        UserStateV2 userStateV2 = mock(UserStateV2.class);
        when(userStateClientService.getUserStateByAuthenticatedUser()).thenReturn(Optional.of(userStateV2));
        when(userStateMapper.toUserState(userStateV2, Domain.FINES)).thenReturn(expectedUserState);

        // Act
        UserState userState = userStateService.checkForAuthorisedUser("");

        // Assert
        assertSame(expectedUserState, userState);
    }

    @Test
    void testGetUserStateFromSecurityContext_returnsTokenUserState() {
        // Arrange
        OpalJwtAuthenticationToken authToken = mock(OpalJwtAuthenticationToken.class);
        UserStateV2 userStateV2 = mock(UserStateV2.class);
        setAuthentication(authToken);
        when(authToken.getUserState()).thenReturn(userStateV2);

        // Act
        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();

        // Assert
        assertSame(userStateV2, userState);
    }

    @Test
    void testGetUserStateFromSecurityContext_unexpectedTokenType() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        setAuthentication(authentication);

        // Act
        AccessDeniedException ade = assertThrows(AccessDeniedException.class,
                                                 () -> userStateService.getUserStateFromSecurityContext());

        // Assert
        assertEquals("Unexpected token type", ade.getMessage());
        verifyNoInteractions(tokenService);
    }

    @Test
    void testGetUserStateFromSecurityContext_userStateMissingFromToken() {
        // Arrange
        OpalJwtAuthenticationToken authToken = mock(OpalJwtAuthenticationToken.class);
        setAuthentication(authToken);
        when(authToken.getUserState()).thenReturn(null);

        // Act
        AccessDeniedException ade = assertThrows(AccessDeniedException.class,
                                                 () -> userStateService.getUserStateFromSecurityContext());

        // Assert
        assertEquals("User state not found in token", ade.getMessage());
    }

    @Test
    void testGetPermittedBusinessUnitIds_filtersUsingFinesDomainBusinessUnitUsers() {
        // Arrange
        OpalJwtAuthenticationToken authToken = mock(OpalJwtAuthenticationToken.class);
        setAuthentication(authToken);
        when(authToken.getUserState()).thenReturn(UserStateV2.builder()
            .userId(1L)
            .username("opal-user")
            .domains(Map.of(Domain.FINES, DomainBusinessUnitUsers.builder()
                .businessUnitUsers(List.of(
                    businessUnitUser((short) 10, FinesPermission.ACCOUNT_ENQUIRY),
                    businessUnitUser((short) 20, FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)))
                .build()))
            .build());

        // Act
        List<Short> permittedBusinessUnitIds = userStateService.getPermittedBusinessUnitIds(
            List.of((short) 10, (short) 20), FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS);

        // Assert
        assertEquals(List.of((short) 20), permittedBusinessUnitIds);
        verifyNoInteractions(userStateMapper);
    }

    @Test
    void testCheckForAuthorisedUser_mapsV2StateToV1FinesDomain() {
        // Arrange
        OpalJwtAuthenticationToken authToken = mock(OpalJwtAuthenticationToken.class);
        UserStateV2 userStateV2 = mock(UserStateV2.class);
        final UserState userState = mock(UserState.class);
        setAuthentication(authToken);
        when(userStateClientService.getUserStateByAuthenticatedUser()).thenReturn(Optional.empty());
        when(authToken.getUserState()).thenReturn(userStateV2);
        when(userStateMapper.toUserState(userStateV2, Domain.FINES)).thenReturn(userState);

        // Act
        UserState result = userStateService.checkForAuthorisedUser();

        // Assert
        assertSame(userState, result);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testCheckForAuthorisedUser_fallsBackToSecurityContextWhenAuthenticatedUserStateHasUnexpectedType() {
        // Arrange
        OpalJwtAuthenticationToken authToken = mock(OpalJwtAuthenticationToken.class);
        UserStateV2 userStateV2 = mock(UserStateV2.class);
        final UserState expectedUserState = mock(UserState.class);
        setAuthentication(authToken);
        when(userStateClientService.getUserStateByAuthenticatedUser()).thenReturn((Optional) Optional.of(Map.of()));
        when(authToken.getUserState()).thenReturn(userStateV2);
        when(userStateMapper.toUserState(userStateV2, Domain.FINES)).thenReturn(expectedUserState);

        // Act
        UserState userState = userStateService.checkForAuthorisedUser();

        // Assert
        assertSame(expectedUserState, userState);
    }

    @Test
    void testGetUser_StateV1FromSecurityContext_unexpectedTokenType() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        setAuthentication(authentication);

        // Act
        AccessDeniedException ade = assertThrows(AccessDeniedException.class,
                                                 () -> userStateService.getUserStateV1FromSecurityContext());

        // Assert
        assertEquals("Unexpected token type", ade.getMessage());
        verifyNoInteractions(userStateMapper);
    }

    @Test
    void testGetUser_userStateV1FromSecurityContextStateMissingFromToken() {
        // Arrange
        OpalJwtAuthenticationToken authToken = mock(OpalJwtAuthenticationToken.class);
        setAuthentication(authToken);
        when(authToken.getUserState()).thenReturn(null);

        // Act
        AccessDeniedException ade = assertThrows(AccessDeniedException.class,
                                                 () -> userStateService.getUserStateV1FromSecurityContext());

        // Assert
        assertEquals("User state not found in token", ade.getMessage());
        verifyNoInteractions(userStateMapper);
    }

    @Test
    void testGetPreferredUsername() {
        // Arrange
        when(tokenService.extractPreferredUsername(any())).thenReturn("HMCTS User");

        // Act
        String name = userStateService.getPreferredUsername("");

        // Assert
        assertEquals("HMCTS User", name);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(Authentication authentication) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private BusinessUnitUser businessUnitUser(Short businessUnitId, FinesPermission permission) {
        return BusinessUnitUser.builder()
            .businessUnitId(businessUnitId)
            .permissions(Set.of(permission.toCommonPermission()))
            .build();
    }
}
