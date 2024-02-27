package uk.gov.hmcts.opal.authentication.service;

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

