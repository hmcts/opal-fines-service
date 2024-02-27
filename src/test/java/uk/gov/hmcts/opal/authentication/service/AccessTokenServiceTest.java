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
        public void testExtractToken_ValidAuthorizationHeader_ReturnsToken() {
            // Given
            String authorizationHeader = "Bearer sampleToken123";

            // When
            String extractedToken = accessTokenService.extractToken(authorizationHeader);

            // Then
            assertEquals("sampleToken123", extractedToken);
        }

        @Test
        public void testExtractToken_NullAuthorizationHeader_ThrowsException() {
            assertThrows(OpalApiException.class, () -> accessTokenService.extractToken(null));
        }

        @Test
        public void testExtractToken_InvalidAuthorizationHeader_ThrowsException() {
            assertThrows(OpalApiException.class, () -> accessTokenService.extractToken("InvalidHeader"));
        }
    }

    @Nested
    class ExtractUserEmail {
        @Test
        void testExtractUserEmail_invalidToken() throws Exception {
            // Given
            String invalidToken = "invalidToken";

            when(tokenValidator.parse(invalidToken)).thenThrow(ParseException.class);

            assertThrows(
                OpalApiException.class,
                () -> accessTokenService.extractUserEmail("Bearer " + invalidToken)
            );
        }


        @Test
        void testExtractUserEmail_validToken() throws Exception {
            String token = "validToken";
            String expectedEmail = "test@example.com";

            JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder();

            claimsSetBuilder.issuer("example.com")
                .subject("john.doe@example.com")
                .audience("client123")
                .claim("preferred_username", "test@example.com");

            JWTClaimsSet claimsSet = claimsSetBuilder.build();

            PlainJWT jwt = new PlainJWT(claimsSet);

            when(tokenValidator.parse(token)).thenReturn(jwt);

            // When
            String email = accessTokenService.extractUserEmail("Bearer " + token);

            // Then
            assertEquals(expectedEmail, email);
        }
    }
}

