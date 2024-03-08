package uk.gov.hmcts.opal.service.opal;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        // Arrange
        UserState userState = UserState.builder().userId("UID_001").userName("John Smith").build();
        when(userEntitlementService.getUserStateByUsername(any())).thenReturn(Optional.of(userState));

        // Act
        UserState result = userStateService.getUserStateUsingServletRequest(request);

        // Assert
        assertNotNull(result);
        assertEquals("UID_001", result.getUserId());
        assertEquals("John Smith", result.getUserName());

    }

    @Test
    void testGetUserState_fromUserService() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        // Arrange
        Optional<UserState> userState = Optional.of(UserState.builder()
                                                        .userId("UID_001").userName("John Smith").build());
        when(userEntitlementService.getUserStateByUsername(any())).thenReturn(Optional.empty());
        when(userService.getLimitedUserStateByUsername(any())).thenReturn((userState));

        // Act
        UserState result = userStateService.getUserStateUsingServletRequest(request);

        // Assert
        assertNotNull(result);
        assertEquals("UID_001", result.getUserId());
        assertEquals("John Smith", result.getUserName());

    }

    @Test
    void testGetUserState_developerUserState() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        // Arrange
        when(userEntitlementService.getUserStateByUsername(any())).thenReturn(Optional.empty());
        when(userService.getLimitedUserStateByUsername(any())).thenReturn((Optional.empty()));
        when(developerConfiguration.getUserRolePermissions()).thenReturn(DEVELOPER_PERMISSIONS);

        // Act
        // UserState result = userStateService.getUserStateUsingServletRequest(request);
        UserState result =  userStateService.getUserStateUsingServletRequest(request);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getUserId());
        assertEquals("", result.getUserName());

    }

    @Test
    void testGetUserState_failure() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        // Arrange
        when(userEntitlementService.getUserStateByUsername(any())).thenReturn(Optional.empty());
        when(userService.getLimitedUserStateByUsername(any())).thenReturn((Optional.empty()));

        // Act
        // UserState result = userStateService.getUserStateUsingServletRequest(request);
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                                                () -> userStateService.getUserStateUsingServletRequest(request));

        // Assert
        assertNotNull(ex);
        assertEquals("No authorised user with username 'null' found", ex.getMessage());

    }
}
