package uk.gov.hmcts.opal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static java.lang.String.format;

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
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(profiles = {"integration-with-spring-security"}, inheritProfiles = false)
@DisplayName("JWT Controller Integration Tests")
public class AbstractIntegrationWithSecurityTest extends AbstractIntegrationTest {

    protected static String validToken;
    protected static String expiredToken;

    //We must stub the WireMock endpoints only once so this code must be in a static block.
    //Otherwise the access token signatures can get out of step with the keystore in WireMock.
    @BeforeAll
    static void beforeAll(@Autowired SecurityItProperties securityItProperties) throws JOSEException {
        URI jwksUri = URI.create(securityItProperties.getWireMockJwksUri());
        String jwksPath = jwksUri.getPath();
        int wireMockPort = jwksUri.getPort();

        WireMock.configureFor("localhost", wireMockPort);

        RSAKey rsaKey = new RSAKeyGenerator(2048)
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(new Algorithm("RS256"))
            .keyID(securityItProperties.getKey())
            .generate();

        var rsaPublicJWK = rsaKey.toPublicJWK();
        var jwkResponse = format("{\"keys\": [%s]}", rsaPublicJWK.toJSONString());

        stubFor(get(jwksPath)
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(jwkResponse)));

        stubFor(get("/opal/v2/users/0/state")
            .willReturn(aResponse()
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(V2_USER_STATE)));

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

    protected static  String expiredToken(RSAKey rsaKey, RSASSASigner signer, String issuerUri) throws JOSEException {
        var claimsSet = new JWTClaimsSet.Builder()
            .expirationTime(new Date(new Date().getTime() - 60 * 1000))
            .issuer(issuerUri)
            .build();
        return generateToken(claimsSet, rsaKey, signer);
    }

    protected static String generateToken(JWTClaimsSet claimsSet, RSAKey rsaKey, RSASSASigner signer) throws JOSEException {
        var signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(rsaKey.getKeyID())
            .build(), claimsSet);
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    public static final String V2_USER_STATE =
        """
        {
          "user_id" : 500000000,
          "username" : "opal-test@HMCTS.NET",
          "name" : "Pablo",
          "status" : "ACTIVE",
          "version" : 0,
          "cache_name" : "USER_STATE_k9LpT2xVqR8m",
          "domains" : {
            "fines" : {
              "business_unit_users" : [ {
                "business_unit_user_id" : "L065JG",
                "business_unit_id" : 70,
                "permissions" : [ {
                  "permission_id" : 1,
                  "permission_name" : "Create and Manage Draft Accounts"
                }, {
                  "permission_id" : 3,
                  "permission_name" : "Account Enquiry"
                }, {
                  "permission_id" : 4,
                  "permission_name" : "Collection Order"
                }, {
                  "permission_id" : 5,
                  "permission_name" : "Check and Validate Draft Accounts"
                }, {
                  "permission_id" : 6,
                  "permission_name" : "Search and View Accounts"
                } ]
              }, {
                "business_unit_user_id" : "L066JG",
                "business_unit_id" : 68,
                "permissions" : [ ]
              }, {
                "business_unit_user_id" : "L067JG",
                "business_unit_id" : 73,
                "permissions" : [ ]
              }, {
                "business_unit_user_id" : "L073JG",
                "business_unit_id" : 71,
                "permissions" : [ ]
              }, {
                "business_unit_user_id" : "L077JG",
                "business_unit_id" : 67,
                "permissions" : [ ]
              }, {
                "business_unit_user_id" : "L078JG",
                "business_unit_id" : 69,
                "permissions" : [ ]
              }, {
                "business_unit_user_id" : "L080JG",
                "business_unit_id" : 61,
                "permissions" : [ ]
              } ]
            }
          }
        }""";
}
