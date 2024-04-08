package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.config.properties.BeDeveloperConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.opal.UserStateService.DEVELOPER_PERMISSIONS;

@ExtendWith(MockitoExtension.class)
class UserStateServiceTest {

    static final String BEARER_TOKEN = "Bearer a_token_here";

    @Mock
    private AccessTokenService tokenService;

    @Mock
    private UserService userService;

    @Mock
    private UserEntitlementService userEntitlementService;

    @Mock
    private BeDeveloperConfiguration developerConfiguration;

    @InjectMocks
    private UserStateService userStateService;

    @Test
    void testGetUserState_fromUserEntitlementService() {
        // Arrange
        UserState userState = UserState.builder().userId(123L).userName("John Smith").build();
        when(userEntitlementService.getUserStateByUsername(any())).thenReturn(Optional.of(userState));

        // Act
        UserState result = userStateService.getUserStateUsingServletRequest(BEARER_TOKEN);

        // Assert
        assertNotNull(result);
        assertEquals(123L, result.getUserId());
        assertEquals("John Smith", result.getUserName());

    }

    @Test
    void testGetUserState_fromUserService() {

        // Arrange
        Optional<UserState> userState = Optional.of(UserState.builder()
                                                        .userId(123L).userName("John Smith").build());
        when(userEntitlementService.getUserStateByUsername(any())).thenReturn(Optional.empty());
        when(userService.getLimitedUserStateByUsername(any())).thenReturn((userState));

        // Act
        UserState result = userStateService.getUserStateUsingServletRequest(BEARER_TOKEN);

        // Assert
        assertNotNull(result);
        assertEquals(123L, result.getUserId());
        assertEquals("John Smith", result.getUserName());

    }

    @Test
    void testGetUserState_developerUserState() {

        // Arrange
        when(userEntitlementService.getUserStateByUsername(any())).thenReturn(Optional.empty());
        when(userService.getLimitedUserStateByUsername(any())).thenReturn((Optional.empty()));
        when(developerConfiguration.getUserRolePermissions()).thenReturn(DEVELOPER_PERMISSIONS);

        // Act
        UserState result = userStateService.getUserStateUsingServletRequest(BEARER_TOKEN);

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.getUserId());
        assertEquals("Developer_User", result.getUserName());

    }

    @Test
    void testGetUserState_failure() {

        // Arrange
        when(userEntitlementService.getUserStateByUsername(any())).thenReturn(Optional.empty());
        when(userService.getLimitedUserStateByUsername(any())).thenReturn((Optional.empty()));

        // Act
        AccessDeniedException ex = assertThrows(
            AccessDeniedException.class,
            () -> userStateService.getUserStateUsingServletRequest(BEARER_TOKEN)
        );

        // Assert
        assertNotNull(ex);
        assertEquals("No authorised user with username 'null' found", ex.getMessage());

    }

    @Test
    void testCheckForAuthorisedUser_success() {

        // Arrange
        when(userEntitlementService.getUserStateByUsername(any())).thenReturn(Optional.empty());
        when(userService.getLimitedUserStateByUsername(any())).thenReturn((Optional.empty()));
        when(developerConfiguration.getUserRolePermissions()).thenReturn(DEVELOPER_PERMISSIONS);

        // Act
        userStateService.checkForAuthorisedUser(BEARER_TOKEN);
    }

    @Test
    void testCheckForAuthorisedUser_failure() {

        // Arrange
        when(userEntitlementService.getUserStateByUsername(any())).thenReturn(Optional.empty());
        when(userService.getLimitedUserStateByUsername(any())).thenReturn((Optional.empty()));

        // Act
        AccessDeniedException ex = assertThrows(
            AccessDeniedException.class,
            () -> userStateService.checkForAuthorisedUser(BEARER_TOKEN)
        );

        // Assert
        assertNotNull(ex);
        assertEquals("No authorised user with username 'null' found", ex.getMessage());

    }
}
