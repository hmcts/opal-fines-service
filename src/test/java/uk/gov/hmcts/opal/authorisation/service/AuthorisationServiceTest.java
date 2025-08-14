package uk.gov.hmcts.opal.authorisation.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.UserStateService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorisationServiceTest {

    public static final String TEST_USER = "testUser";
    @Mock
    UserStateService userStateService;
    @Mock
    AccessTokenService accessTokenService;

    @InjectMocks
    private AuthorisationService authorisationService;



    @Test
    public void testGetSecurityTokenWithValidAccessToken() {
        // Mock AccessTokenService
        String accessToken = "validAccessToken";
        when(accessTokenService.extractPreferredUsername(accessToken)).thenReturn(TEST_USER);

        // Mock UserService
        UserState userState = UserState.builder().userId(234L).userName(TEST_USER).build();
        when(userStateService.checkForAuthorisedUser(accessToken)).thenReturn(userState);

        // Call the method
        SecurityToken securityToken = authorisationService.getSecurityToken(accessToken);

        // Verify the SecurityToken properties
        assertNotNull(securityToken);
        assertEquals(accessToken, securityToken.getAccessToken());
        assertEquals(userState, securityToken.getUserState());

        // Verify interactions with dependencies
        verify(accessTokenService).extractPreferredUsername(accessToken);
        verify(userStateService).checkForAuthorisedUser(accessToken);
        verifyNoMoreInteractions(accessTokenService, userStateService);
    }

    @Test
    public void testGetSecurityTokenWithInvalidAccessToken() {
        // Mock AccessTokenService
        String accessToken = "invalidAccessToken";
        when(accessTokenService.extractPreferredUsername(accessToken)).thenReturn(null);

        // Call the method
        SecurityToken securityToken = authorisationService.getSecurityToken(accessToken);

        // Verify that SecurityToken has null user state
        assertNotNull(securityToken);
        assertEquals(accessToken, securityToken.getAccessToken());
        assertNull(securityToken.getUserState());

        // Verify interactions with dependencies
        verify(accessTokenService).extractPreferredUsername(accessToken);
        verifyNoInteractions(userStateService);
    }
}
