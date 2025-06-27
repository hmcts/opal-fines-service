package uk.gov.hmcts.opal.authentication.service;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authentication.client.AzureTokenClient;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthConfigurationProperties;
import uk.gov.hmcts.opal.authentication.model.AccessTokenRequest;
import uk.gov.hmcts.opal.authentication.model.AccessTokenResponse;
import uk.gov.hmcts.opal.config.properties.TestUser;
import uk.gov.hmcts.opal.exception.OpalApiException;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessTokenServiceTest {

    @Mock
    private InternalAuthConfigurationProperties configuration;

    @Mock
    private AzureTokenClient azureTokenClient;

    @Mock
    private TokenValidator tokenValidator;

    @Mock
    private TestUser testUser;

    @InjectMocks
    private AccessTokenService accessTokenService;

    @Test
    void getAccessToken_shouldReturnAccessToken() {
        // Arrange
        when(configuration.getClientId()).thenReturn("your-client-id");
        when(configuration.getClientSecret()).thenReturn("your-client-secret");
        when(configuration.getScope()).thenReturn("your-scope");

        when(azureTokenClient.getAccessToken(any(AccessTokenRequest.class))).thenReturn(
            AccessTokenResponse.builder()
                .accessToken("your-access-token")
                .build()
        );

        // Act
        AccessTokenResponse accessToken = accessTokenService.getAccessToken("testUser", "test");

        // Assert
        assertNotNull(accessToken);
        assertEquals("your-access-token", accessToken.getAccessToken());
    }

    @Test
    void getTestUserToken_shouldReturnTestUserToken() {
        // Arrange
        AccessTokenResponse expectedResponse = AccessTokenResponse.builder().accessToken("test token").build();

        when(testUser.getEmail()).thenReturn("test email");
        when(testUser.getPassword()).thenReturn("password");
        when(azureTokenClient.getAccessToken(any(AccessTokenRequest.class))).thenReturn(expectedResponse);

        // Act
        AccessTokenResponse result = accessTokenService.getTestUserToken();

        // Assert
        assertEquals(expectedResponse, result);
    }

    @Test
    void getTestUserTokenForGivenUser_shouldReturnTestUserToken() {
        // Arrange
        AccessTokenResponse expectedResponse = AccessTokenResponse.builder().accessToken("test token").build();

        when(testUser.getPassword()).thenReturn("password");
        when(azureTokenClient.getAccessToken(any(AccessTokenRequest.class))).thenReturn(expectedResponse);

        // Act
        AccessTokenResponse result = accessTokenService.getTestUserToken("test email");

        // Assert
        assertEquals(expectedResponse, result);
    }

    @Nested
    class ExtractToken {
        @Test
        void testExtractToken_ValidAuthorizationHeader_ReturnsToken() {
            // Given
            String authorizationHeader = "Bearer sampleToken123";

            // When
            String extractedToken = accessTokenService.extractToken(authorizationHeader);

            // Then
            assertEquals("sampleToken123", extractedToken);
        }
    }

    @Nested
    class ExtractUserEmail {
        @Test
        void testExtractPreferredUsername_invalidToken() throws Exception {
            // Given
            String invalidToken = "invalidToken";

            when(tokenValidator.parse(invalidToken)).thenThrow(ParseException.class);

            assertThrows(
                OpalApiException.class,
                () -> accessTokenService.extractPreferredUsername("Bearer " + invalidToken)
            );
        }


        @Test
        void testExtractPreferredUsername_validToken() throws Exception {
            String token = "validToken";
            String expectedEmail = "test@example.com";

            PlainJWT jwt = new PlainJWT(buildJwt());

            when(tokenValidator.parse(token)).thenReturn(jwt);

            // When
            String username = accessTokenService.extractPreferredUsername("Bearer " + token);

            // Then
            assertEquals(expectedEmail, username);
        }

        @Test
        void testExtractNameClaim_validToken() throws Exception {
            // Given
            PlainJWT jwt = new PlainJWT(buildJwt());
            when(tokenValidator.parse(any())).thenReturn(jwt);

            // When
            String claim = accessTokenService.extractName("Bearer encryptedToken");

            // Then
            assertEquals("opal-test", claim);
        }

        @Test
        void testExtractScpClaim_validToken() throws Exception {
            // Given
            PlainJWT jwt = new PlainJWT(buildJwt());
            when(tokenValidator.parse(any())).thenReturn(jwt);

            // When
            String claim = accessTokenService.extractScp("Bearer encryptedToken");

            // Then
            assertEquals("opalinternaluser", claim);
        }

        @Test
        void testExtractUniqueNameClaim_validToken() throws Exception {
            // Given
            PlainJWT jwt = new PlainJWT(buildJwt());
            when(tokenValidator.parse(any())).thenReturn(jwt);

            // When
            String claim = accessTokenService.extractUniqueName("Bearer encryptedToken");

            // Then
            assertEquals("opal-test@example.com", claim);
        }

        @Test
        void testExtractUpnClaim_validToken() throws Exception {
            // Given
            PlainJWT jwt = new PlainJWT(buildJwt());
            when(tokenValidator.parse(any())).thenReturn(jwt);

            // When
            String claim = accessTokenService.extractUpn("Bearer encryptedToken");

            // Then
            assertEquals("opal-test@example.com", claim);
        }

        private JWTClaimsSet buildJwt() {
            return new JWTClaimsSet.Builder()
                .issuer("example.com")
                .subject("john.doe@example.com")
                .audience("client123")
                .claim("preferred_username", "test@example.com")
                .claim("name", "opal-test")
                .claim("scp", "opalinternaluser")
                .claim("unique_name", "opal-test@example.com")
                .claim("upn", "opal-test@example.com")
                .build();
        }
    }
}
