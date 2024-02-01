package uk.gov.hmcts.opal.authentication.service;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthConfigurationProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AzureDummyTokenServiceTest {

    @Mock
    private InternalAuthConfigurationProperties config;

    @InjectMocks
    private AzureDummyTokenService jwtService;

    @Test
    void testGenerateAzureJwtToken() throws Exception {
        String audience = "myapp";
        String issuer = "https://issuer.example.com";
        String clientId = "abc123";

        when(config.getIssuerUri()).thenReturn(issuer);
        when(config.getClientId()).thenReturn(clientId);

        String token = jwtService.generateAzureJwtToken(audience);

        // Verify JWT structure
        SignedJWT parsedToken = SignedJWT.parse(token);
        JWTClaimsSet claims = parsedToken.getJWTClaimsSet();

        assertEquals(issuer, claims.getIssuer());
        assertEquals(audience, claims.getAudience().get(0));
        assertEquals(clientId, claims.getSubject());
    }

}
