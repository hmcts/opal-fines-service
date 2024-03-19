package uk.gov.hmcts.opal.authentication.controller;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.hmcts.opal.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authentication.service.AuthenticationService;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.authorisation.service.AuthorisationService;

import java.net.URI;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports"})
class AuthenticationInternalUserControllerTest {

    private static final URI DUMMY_AUTHORIZATION_URI = URI.create("https://www.example.com/authorization?param=value");
    private static final URI DUMMY_LOGOUT_URI = URI.create("https://www.example.com/logout?param=value");
    private static final String DUMMY_CODE = "code";
    private static final String DUMMY_TOKEN = "token";

    @InjectMocks
    private AuthenticationInternalUserController controller;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private AccessTokenService accessTokenService;

    @Test
    void loginAndRefreshShouldReturnLoginPageAsRedirectWhenAuthHeaderIsNotSet() {
        when(authenticationService.loginOrRefresh(null, null))
            .thenReturn(DUMMY_AUTHORIZATION_URI);

        ModelAndView modelAndView = controller.loginOrRefresh(null, null);

        assertNotNull(modelAndView);
        assertEquals("redirect:https://www.example.com/authorization?param=value", modelAndView.getViewName());
    }

    @Nested
    class HandleOauthCode {
        @Test
        void handleOauthCodeFromAzureWhenCodeIsReturnedWithAccessTokenAndUserState() throws JOSEException {
            when(authenticationService.handleOauthCode(anyString()))
                .thenReturn(createDummyAccessToken("test.user@example.com"));

            SecurityToken securityToken = controller.handleOauthCode(DUMMY_CODE);
            assertNotNull(securityToken);
            assertNotNull(securityToken.getAccessToken());

            verify(authenticationService).handleOauthCode(DUMMY_CODE);
        }

        @Test
        void handleOauthCodeFromAzureWhenCodeIsReturnedWithAccessTokenAndNoUserState() throws JOSEException {
            when(authenticationService.handleOauthCode(anyString()))
                .thenReturn(createDummyAccessToken("test.missing@example.com"));

            SecurityToken securityToken = controller.handleOauthCode(DUMMY_CODE);
            assertNotNull(securityToken);
            assertNotNull(securityToken.getAccessToken());

            verify(authenticationService).handleOauthCode(DUMMY_CODE);
        }

        @Test
        void handleOauthCodeFromAzureWhenCodeIsReturnedWithoutClaim() throws JOSEException {
            when(authenticationService.handleOauthCode(anyString()))
                .thenReturn(createDummyAccessToken("test.missing@example.com"));

            SecurityToken securityToken = controller.handleOauthCode(DUMMY_CODE);
            assertNotNull(securityToken);
            assertNotNull(securityToken.getAccessToken());

            verify(authenticationService).handleOauthCode(DUMMY_CODE);
        }

        @Test
        void handleOauthCode_ReturnsSecurityToken_WhenValidCodeProvided() {
            // Arrange
            String code = "validCode";
            String accessToken = "accessToken";
            String userEmail = "test@example.com";
            UserState userState = UserState.builder().userId(234L).userName("some name").build();

            when(authenticationService.handleOauthCode(code)).thenReturn(accessToken);
            when(accessTokenService.extractPreferredUsername(accessToken)).thenReturn(userEmail);
            when(authorisationService.getAuthorisation(userEmail)).thenReturn(userState);

            // Act
            SecurityToken result = controller.handleOauthCode(code);

            // Assert
            assertNotNull(result);
            assertEquals(accessToken, result.getAccessToken());
            assertEquals(userState, result.getUserState());

            // Verify interactions
            verify(authenticationService).handleOauthCode(code);
            verify(accessTokenService).extractPreferredUsername(accessToken);
            verify(authorisationService).getAuthorisation(userEmail);
        }

        @Test
        void handleOauthCode_ReturnsSecurityTokenWithoutUserState_WhenUserEmailNotPresent() {
            // Arrange
            String code = "validCode";
            String accessToken = "accessToken";

            when(authenticationService.handleOauthCode(code)).thenReturn(accessToken);
            when(accessTokenService.extractPreferredUsername(accessToken)).thenReturn(null);

            // Act
            SecurityToken result = controller.handleOauthCode(code);

            // Assert
            assertNotNull(result);
            assertEquals(accessToken, result.getAccessToken());
            assertNull(result.getUserState());

            // Verify interactions
            verify(authenticationService).handleOauthCode(code);
            verify(accessTokenService).extractPreferredUsername(accessToken);
            verifyNoInteractions(authorisationService);
        }

    }

    @Test
    void logoutShouldReturnLogoutPageUriAsRedirectWhenTokenExistsInSession() {
        when(authenticationService.logout(DUMMY_TOKEN, null))
            .thenReturn(DUMMY_LOGOUT_URI);

        ModelAndView modelAndView = controller.logout("Bearer " + DUMMY_TOKEN, null);

        assertNotNull(modelAndView);
        assertEquals("redirect:https://www.example.com/logout?param=value", modelAndView.getViewName());
    }

    @Test
    void resetPasswordShouldReturnResetPageAsRedirect() {
        when(authenticationService.resetPassword(any()))
            .thenReturn(DUMMY_AUTHORIZATION_URI);

        ModelAndView modelAndView = controller.resetPassword(null);

        assertNotNull(modelAndView);
        assertEquals("redirect:https://www.example.com/authorization?param=value", modelAndView.getViewName());
    }


    @SuppressWarnings("PMD.UseUnderscoresInNumericLiterals")
    private String createDummyAccessToken(String emails) throws JOSEException {
        RSAKey rsaKey = new RSAKeyGenerator(2048)
            .keyID("123")
            .generate();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .claim("ver", "1.0")
            .issuer(String.format("https://<tenant-name>.b2clogin.com/%s/v2.0/", UUID.randomUUID()))
            .subject(UUID.randomUUID().toString())
            .audience(UUID.randomUUID().toString())
            .expirationTime(new Date(1690973493))
            .claim("nonce", "defaultNonce")
            .issueTime(new Date(1690969893))
            .claim("auth_time", new Date(1690969893))
            .claim("preferred_username", emails)
            .claim("name", "Test User")
            .claim("given_name", "Test")
            .claim("family_name", "User")
            .claim("tfp", "policy_name")
            .claim("nbf", new Date(1690969893))
            .build();

        SignedJWT signedJwt = new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
            claimsSet
        );

        signedJwt.sign(new RSASSASigner(rsaKey));

        return signedJwt.serialize();
    }
}
