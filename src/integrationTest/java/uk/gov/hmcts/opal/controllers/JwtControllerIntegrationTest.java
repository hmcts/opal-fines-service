package uk.gov.hmcts.opal.controllers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static java.lang.String.format;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.opal.AbstractIntegrationTest;

@ActiveProfiles(profiles = {"integration-with-spring-security"}, inheritProfiles = false)
@Slf4j(topic = "opal.JwtControllerIntegrationTest")
@DisplayName("JWT Controller Integration Tests")
class JwtControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL = "/business-units/5";
    private static final int WIREMOCK_PORT = 4553;
    private static final String WIREMOCK_JWKS_PATH = "/opal/oauth2/jwks.json";
    private static final String ISSUER_URI = "http://localhost:" + WIREMOCK_PORT + "/opal/";
    private static final String KEY_ID = "... some random string ...";

    private static RSAKey rsaKey;
    private static RSASSASigner signer;

    @BeforeAll
    static void setUp() throws JOSEException {

        WireMock.configureFor("localhost", WIREMOCK_PORT);

        rsaKey = new RSAKeyGenerator(2048)
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(new Algorithm("RS256"))
            .keyID(KEY_ID)
            .generate();

        var rsaPublicJWK = rsaKey.toPublicJWK();
        var jwkResponse = format("{\"keys\": [%s]}", rsaPublicJWK.toJSONString());

        stubFor(get(WIREMOCK_JWKS_PATH)
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(jwkResponse)));

        signer = new RSASSASigner(rsaKey);
    }

    @Test
    @DisplayName("Testing Valid Token")
    void testValidJwtTokenGivesOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL)
                .header("authorization", "Bearer " + validToken()))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Testing Expired JWT Token")
    void testExpiredTokenGivesUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL)
                .header("authorization", "Bearer " + expiredToken()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Testing Invalid JWT Token")
    void testInvalidTokenGivesUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL)
                .header("authorization", "Bearer aa.bb.cc"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Testing Missing JWT Token")
    void testMissingTokenGivesUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL)
                .header("authorization", "Bearer "))
            .andExpect(status().isUnauthorized());
    }

    private String validToken() throws JOSEException {
        var claimsSet = new JWTClaimsSet.Builder()
            .expirationTime(new Date(new Date().getTime() + 60 * 1000))
            .issuer(ISSUER_URI)
            .build();
        return generateToken(claimsSet);
    }

    private String expiredToken() throws JOSEException {
        var claimsSet = new JWTClaimsSet.Builder()
            .expirationTime(new Date(new Date().getTime() - 60 * 1000))
            .issuer(ISSUER_URI)
            .build();
        return generateToken(claimsSet);
    }

    private String generateToken(JWTClaimsSet claimsSet) throws JOSEException {
        var signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(rsaKey.getKeyID())
            .build(), claimsSet);
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }
}
