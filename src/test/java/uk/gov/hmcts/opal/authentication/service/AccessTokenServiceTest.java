package uk.gov.hmcts.opal.authentication.service;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.exception.OpalApiException;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessTokenServiceTest {

    @Mock
    private TokenValidator tokenValidator;

    @InjectMocks
    private AccessTokenService accessTokenService;

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
