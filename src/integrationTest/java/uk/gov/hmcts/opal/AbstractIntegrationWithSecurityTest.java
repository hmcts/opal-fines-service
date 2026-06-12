package uk.gov.hmcts.opal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static java.lang.String.format;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.opal.support.UserStateStub;

@ActiveProfiles(profiles = {"integration-with-spring-security"}, inheritProfiles = false)
@DisplayName("JWT Controller Integration Tests")
public class AbstractIntegrationWithSecurityTest extends AbstractIntegrationTest {

    protected static String validToken;
    protected static String expiredToken;
    protected static String jwksPath;
    protected static String jwkResponse;

    @Override
    @BeforeEach
    public void beforeEach() {
        super.beforeEach();
        stubFor(get(jwksPath)
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(jwkResponse)));
    }

    @Override
    protected UserStateStub createUserStateStub() {
        return new UserStateStub(validToken);
    }

    //We must stub the WireMock endpoints only once so this code must be in a static block.
    //Otherwise the access token signatures can get out of step with the keystore in WireMock.
    @BeforeAll
    static void beforeAll(@Autowired SecurityItProperties securityItProperties) throws JOSEException {
        URI jwksUri = URI.create(securityItProperties.getWireMockJwksUri());
        jwksPath = jwksUri.getPath();

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


    protected static String validToken(RSAKey rsaKey, RSASSASigner signer, String issuerUri) throws JOSEException {
        var claimsSet = new JWTClaimsSet.Builder()
            .expirationTime(new Date(new Date().getTime() + 60 * 10000))
            .issuer(issuerUri)
            .subject("GfsHbIMt49WjQ") //500000001
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
