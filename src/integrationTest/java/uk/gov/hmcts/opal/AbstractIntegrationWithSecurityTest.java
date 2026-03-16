package uk.gov.hmcts.opal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static java.lang.String.format;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.net.URI;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(profiles = {"integration-with-spring-security"}, inheritProfiles = false)
@DisplayName("JWT Controller Integration Tests")
public class AbstractIntegrationWithSecurityTest extends AbstractIntegrationTest {

    private static final WireMockServer JWKS_SERVER =
        new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    static {
        JWKS_SERVER.start();
    }

    protected SecurityItProperties securityItProperties;

    private RSAKey rsaKey;
    private RSASSASigner signer;

    public AbstractIntegrationWithSecurityTest(SecurityItProperties securityItProperties) throws JOSEException {
        this.securityItProperties = securityItProperties;

        URI jwksUri = URI.create(securityItProperties.getWireMockJwksUri());
        String jwksPath = jwksUri.getPath();

        rsaKey = new RSAKeyGenerator(2048)
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(new Algorithm("RS256"))
            .keyID(securityItProperties.getKey())
            .generate();

        var rsaPublicJWK = rsaKey.toPublicJWK();
        var jwkResponse = format("{\"keys\": [%s]}", rsaPublicJWK.toJSONString());

        JWKS_SERVER.stubFor(get(jwksPath)
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(jwkResponse)));

        signer = new RSASSASigner(rsaKey);
    }

    @DynamicPropertySource
    static void securityProperties(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.security.oauth2.client.registration.internal-azure-ad.issuer-uri",
            () -> JWKS_SERVER.baseUrl() + "/opal/"
        );
        registry.add(
            "spring.security.oauth2.client.provider.internal-azure-ad-provider.jwk-set-uri",
            () -> JWKS_SERVER.baseUrl() + "/opal/oauth2/jwks.json"
        );
    }

    protected String validToken() throws JOSEException {
        var claimsSet = new JWTClaimsSet.Builder()
            .expirationTime(new Date(new Date().getTime() + 60 * 1000))
            .issuer(securityItProperties.getIssuerUri())
            .build();
        return generateToken(claimsSet);
    }

    protected String expiredToken() throws JOSEException {
        var claimsSet = new JWTClaimsSet.Builder()
            .expirationTime(new Date(new Date().getTime() - 60 * 1000))
            .issuer(securityItProperties.getIssuerUri())
            .build();
        return generateToken(claimsSet);
    }

    protected String generateToken(JWTClaimsSet claimsSet) throws JOSEException {
        var signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(rsaKey.getKeyID())
            .build(), claimsSet);
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }
}
