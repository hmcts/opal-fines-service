package uk.gov.hmcts.opal.authentication.service;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthConfigurationProperties;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;


@Service
@RequiredArgsConstructor
public class AzureDummyTokenService {

    private final InternalAuthConfigurationProperties configuration;

    public String generateAzureJwtToken(String audience) throws Exception {
        // Generate an RSA key pair (for demonstration purposes, in production, you should load from a secure location)
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        // Prepare JWT with claims set
        JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder()
            .issuer(this.configuration.getIssuerUri())
            .audience(audience)
            .subject(this.configuration.getClientId())
            .issueTime(new Date())
            .expirationTime(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
            .jwtID(java.util.UUID.randomUUID().toString())
            .build();

        // Create JWS header with the specified algorithm (RS256 for Azure)
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(this.configuration.getClientId())
            .build();

        SignedJWT signedJWT = new SignedJWT(header, jwtClaims);

        JWSSigner signer = new RSASSASigner(privateKey);

        signedJWT.sign(signer);

        return signedJWT.serialize();
    }
}
