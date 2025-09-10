package uk.gov.hmcts.opal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.client.user.service.UserStateClientService;
import uk.gov.hmcts.opal.config.properties.BeDeveloperConfiguration;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.service.UserStateService.DEVELOPER_PERMISSIONS;

@ExtendWith(MockitoExtension.class)
class UserStateServiceTest {

    @Mock
    private AccessTokenService tokenService;

    @Mock
    private UserStateClientService userStateClientService;

    @Mock
    private BeDeveloperConfiguration developerConfiguration;

    @InjectMocks
    private UserStateService userStateService;

    @Test
    void testCheckForAuthorisedUser_success() {
        // Arrange
        UserState state = UserStateUtil.permissionUser((short)77, Permissions.ACCOUNT_ENQUIRY);
        when(userStateClientService.getUserStateByAuthenticatedUser(any())).thenReturn(Optional.of(state));

        // Act
        UserState userState = userStateService.checkForAuthorisedUser("");

        // Assert
        assertNotNull(userState);
        assertEquals("normal@users.com", userState.getUserName());
        assertEquals(1L, userState.getUserId());
    }

    @Test
    void testCheckForAuthorisedUser_notFound() {
        // Arrange
        when(userStateClientService.getUserStateByAuthenticatedUser(any())).thenReturn(Optional.empty());
        when(tokenService.extractPreferredUsername(any())).thenReturn("Test User");

        // Act
        AccessDeniedException ade = assertThrows(AccessDeniedException.class,
                                                 () -> userStateService.checkForAuthorisedUser(""));

        // Assert
        assertNotNull(ade);
        assertEquals("No authorised user with username 'Test User' found", ade.getMessage());
    }

    @Test
    void testCheckForAuthorisedUser_devUser() {
        // Arrange
        when(userStateClientService.getUserStateByAuthenticatedUser(any())).thenReturn(Optional.empty());
        when(developerConfiguration.getUserRolePermissions()).thenReturn(DEVELOPER_PERMISSIONS);

        // Act
        UserState userState = userStateService.checkForAuthorisedUser("");

        // Assert
        assertNotNull(userState);
        assertEquals("Developer_User", userState.getUserName());
        assertEquals(0L, userState.getUserId());
    }

    @Test
    void testGetPreferredUsername() {
        // Arrange
        when(tokenService.extractPreferredUsername(any())).thenReturn("HMCTS User");

        // Act
        String name = userStateService.getPreferredUsername("");

        // Assert
        assertNotNull(name);
        assertEquals("HMCTS User", name);
    }

}
