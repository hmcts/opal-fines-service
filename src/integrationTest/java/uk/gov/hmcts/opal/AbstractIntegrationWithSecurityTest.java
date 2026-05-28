package uk.gov.hmcts.opal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static java.lang.String.format;
import static uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService.USER_STATE_CACHE_PREFIX;

import com.github.tomakehurst.wiremock.client.WireMock;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(profiles = {"integration-with-spring-security"}, inheritProfiles = false)
@DisplayName("JWT Controller Integration Tests")
public class AbstractIntegrationWithSecurityTest extends AbstractIntegrationTest {

    protected static final String TEST_USER_SUBJECT = "GfsHbIMt49WjQ";
    protected static final String TEST_USER_STATE_CACHE_KEY = USER_STATE_CACHE_PREFIX + TEST_USER_SUBJECT;

    protected static String validToken;
    protected static String expiredToken;
    private static String jwkResponse;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // We must stub the WireMock endpoints only once so this code must be in a static block.
    // Otherwise the access token signatures can get out of step with the keystore in WireMock.
    @BeforeAll
    static void beforeAll(@Autowired SecurityItProperties securityItProperties) throws JOSEException {
        URI jwksUri = URI.create(securityItProperties.getWireMockJwksUri());
        String jwksPath = jwksUri.getPath();
        int wireMockPort = jwksUri.getPort();

        WireMock.configureFor("localhost", wireMockPort);

        if (validToken == null) {
            RSAKey rsaKey = new RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(new Algorithm("RS256"))
                .keyID(securityItProperties.getKey())
                .generate();

            var rsaPublicJWK = rsaKey.toPublicJWK();
            jwkResponse = format("{\"keys\": [%s]}", rsaPublicJWK.toJSONString());

            RSASSASigner signer = new RSASSASigner(rsaKey);

            validToken = validToken(rsaKey, signer, securityItProperties.getIssuerUri());
            expiredToken = expiredToken(rsaKey, signer, securityItProperties.getIssuerUri());
        }

        stubFor(get(jwksPath)
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(jwkResponse)));
    }

    @BeforeEach
    void clearCachedUserState() {
        redisTemplate.delete(TEST_USER_STATE_CACHE_KEY);
    }

    protected static String validToken(RSAKey rsaKey, RSASSASigner signer, String issuerUri) throws JOSEException {
        var claimsSet = new JWTClaimsSet.Builder()
            .expirationTime(new Date(new Date().getTime() + 60 * 10000))
            .issuer(issuerUri)
            .subject(TEST_USER_SUBJECT)
            .claim("name", "Pablo")
            .build();
        return generateToken(claimsSet, rsaKey, signer);
    }

    protected static String expiredToken(RSAKey rsaKey, RSASSASigner signer, String issuerUri) throws JOSEException {
        var claimsSet = new JWTClaimsSet.Builder()
            .expirationTime(new Date(new Date().getTime() - 60 * 1000))
            .issuer(issuerUri)
            .build();
        return generateToken(claimsSet, rsaKey, signer);
    }

    protected static String generateToken(JWTClaimsSet claimsSet, RSAKey rsaKey, RSASSASigner signer)
        throws JOSEException {
        var signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(rsaKey.getKeyID())
            .build(), claimsSet);
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

}
