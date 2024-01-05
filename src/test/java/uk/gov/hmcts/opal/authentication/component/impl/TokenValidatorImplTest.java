package uk.gov.hmcts.opal.authentication.component.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey.Builder;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authentication.component.TokenValidator;
import uk.gov.hmcts.opal.authentication.config.AuthConfigurationProperties;
import uk.gov.hmcts.opal.authentication.config.AuthProviderConfigurationProperties;
import uk.gov.hmcts.opal.authentication.model.JwtValidationResult;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports"})
class TokenValidatorImplTest {

    private static final String KEY_ID_VALUE = "123456";
    private static final String VALID_ISSUER_VALUE = "VALID ISSUER VALUE";
    private static final String VALID_AUDIENCE_VALUE = "VALID AUDIENCE VALUE";
    private static final String VALID_SUBJECT_VALUE = "VALID SUBJECT VALUE";
    private static final String VALID_EMAIL_VALUE = "test.user@example.com";
    private static final String EMAILS_CLAIM_NAME = "emails";

    @Mock
    private AuthConfigurationProperties authenticationConfiguration;

    @Mock
    private AuthProviderConfigurationProperties authenticationProviderConfiguration;

    private KeyPair keyPair;
    private TokenValidator tokenValidator;

    @BeforeEach
    void setUp() {
        keyPair = createKeys();

        JWKSource<SecurityContext> testJwkSource = createTestJwkSource();

        when(authenticationProviderConfiguration.getJwkSource()).thenReturn(testJwkSource);

        when(authenticationConfiguration.getIssuerUri()).thenReturn(VALID_ISSUER_VALUE);
        when(authenticationConfiguration.getClientId()).thenReturn(VALID_AUDIENCE_VALUE);

        tokenValidator = new TokenValidatorImpl();
    }

    @Test
    void validateShouldReturnPositiveResultWhenValidTokenIsPresented() {
        when(authenticationConfiguration.getClaims()).thenReturn(EMAILS_CLAIM_NAME);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
            .audience(VALID_AUDIENCE_VALUE)
            .issuer(VALID_ISSUER_VALUE)
            .expirationTime(createInstantInFuture())
            .issueTime(Date.from(Instant.now()))
            .subject(VALID_SUBJECT_VALUE)
            .claim(EMAILS_CLAIM_NAME, List.of(VALID_EMAIL_VALUE))
            .claim("name", "Test User")
            .claim("given_name", "Test")
            .claim("family_name", "User")
            .build();

        SignedJWT jwt = createSignedJwt(jwtClaimsSet);

        JwtValidationResult validationResult = tokenValidator.validate(jwt.serialize(),
                                                                       authenticationProviderConfiguration,
                                                                       authenticationConfiguration);

        assertTrue(validationResult.valid());
        assertNull(validationResult.reason());
    }

    @Test
    void validateShouldReturnNegativeResultWhenEmailsClaimIsMissing() {
        when(authenticationConfiguration.getClaims()).thenReturn(EMAILS_CLAIM_NAME);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
            .audience(VALID_AUDIENCE_VALUE)
            .issuer(VALID_ISSUER_VALUE)
            .expirationTime(createInstantInFuture())
            .issueTime(Date.from(Instant.now()))
            .subject(VALID_SUBJECT_VALUE)
            .build();

        SignedJWT jwt = createSignedJwt(jwtClaimsSet);

        JwtValidationResult validationResult = tokenValidator.validate(jwt.serialize(),
                                                                       authenticationProviderConfiguration,
                                                                       authenticationConfiguration);

        assertFalse(validationResult.valid());
        assertEquals("JWT missing required claims: [emails]", validationResult.reason());
    }

    @Test
    void validateShouldReturnNegativeResultWhenSubjectClaimIsMissing() {
        when(authenticationConfiguration.getClaims()).thenReturn(EMAILS_CLAIM_NAME);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
            .audience(VALID_AUDIENCE_VALUE)
            .issuer(VALID_ISSUER_VALUE)
            .expirationTime(createInstantInFuture())
            .issueTime(Date.from(Instant.now()))
            .claim(EMAILS_CLAIM_NAME, List.of(VALID_EMAIL_VALUE))
            .build();

        SignedJWT jwt = createSignedJwt(jwtClaimsSet);

        JwtValidationResult validationResult = tokenValidator.validate(jwt.serialize(),
                                                                       authenticationProviderConfiguration,
                                                                       authenticationConfiguration);

        assertFalse(validationResult.valid());
        assertEquals("JWT missing required claims: [sub]", validationResult.reason());
    }

    @Test
    void validateShouldReturnNegativeResultWhenIssuedAtClaimIsMissing() {
        when(authenticationConfiguration.getClaims()).thenReturn(EMAILS_CLAIM_NAME);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
            .audience(VALID_AUDIENCE_VALUE)
            .issuer(VALID_ISSUER_VALUE)
            .expirationTime(createInstantInFuture())
            .subject(VALID_SUBJECT_VALUE)
            .claim(EMAILS_CLAIM_NAME, List.of(VALID_EMAIL_VALUE))
            .build();

        SignedJWT jwt = createSignedJwt(jwtClaimsSet);

        JwtValidationResult validationResult = tokenValidator.validate(jwt.serialize(),
                                                                       authenticationProviderConfiguration,
                                                                       authenticationConfiguration);

        assertFalse(validationResult.valid());
        assertEquals("JWT missing required claims: [iat]", validationResult.reason());
    }

    @Test
    void validateShouldReturnNegativeResultWhenInvalidAudienceIsPresented() {
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
            .audience("INVALID AUDIENCE VALUE")
            .issuer(VALID_ISSUER_VALUE)
            .expirationTime(createInstantInFuture())
            .issueTime(Date.from(Instant.now()))
            .subject(VALID_SUBJECT_VALUE)
            .claim(EMAILS_CLAIM_NAME, List.of(VALID_EMAIL_VALUE))
            .build();

        SignedJWT jwt = createSignedJwt(jwtClaimsSet);

        JwtValidationResult validationResult = tokenValidator.validate(jwt.serialize(),
                                                                       authenticationProviderConfiguration,
                                                                       authenticationConfiguration);

        assertFalse(validationResult.valid());
        assertEquals("JWT audience rejected: [INVALID AUDIENCE VALUE]", validationResult.reason());
    }

    @Test
    void validateShouldReturnNegativeResultWhenInvalidIssuerIsPresented() {
        when(authenticationConfiguration.getClaims()).thenReturn(EMAILS_CLAIM_NAME);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
            .audience(VALID_AUDIENCE_VALUE)
            .issuer("INVALID ISSUER VALUE")
            .expirationTime(createInstantInFuture())
            .issueTime(Date.from(Instant.now()))
            .subject(VALID_SUBJECT_VALUE)
            .claim(EMAILS_CLAIM_NAME, List.of(VALID_EMAIL_VALUE))
            .build();

        SignedJWT jwt = createSignedJwt(jwtClaimsSet);

        JwtValidationResult validationResult = tokenValidator.validate(jwt.serialize(),
                                                                       authenticationProviderConfiguration,
                                                                       authenticationConfiguration);

        assertFalse(validationResult.valid());
        assertEquals(
            "JWT iss claim has value INVALID ISSUER VALUE, must be VALID ISSUER VALUE",
            validationResult.reason()
        );
    }

    @Test
    void validateShouldReturnNegativeResultWhenExpiredTokenIsPresented() {
        when(authenticationConfiguration.getClaims()).thenReturn(EMAILS_CLAIM_NAME);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
            .audience(VALID_AUDIENCE_VALUE)
            .issuer(VALID_ISSUER_VALUE)
            .expirationTime(createInstantInPast())
            .issueTime(Date.from(Instant.now()))
            .subject(VALID_SUBJECT_VALUE)
            .claim(EMAILS_CLAIM_NAME, List.of(VALID_EMAIL_VALUE))
            .build();

        SignedJWT jwt = createSignedJwt(jwtClaimsSet);

        JwtValidationResult validationResult = tokenValidator.validate(jwt.serialize(),
                                                                       authenticationProviderConfiguration,
                                                                       authenticationConfiguration);

        assertFalse(validationResult.valid());
        assertEquals("Expired JWT", validationResult.reason());
    }

    @Test
    void validateShouldReturnNegativeResultWhenInvalidSignatureIsPresented() {
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
            .audience(VALID_AUDIENCE_VALUE)
            .issuer(VALID_ISSUER_VALUE)
            .expirationTime(createInstantInFuture())
            .issueTime(Date.from(Instant.now()))
            .subject(VALID_SUBJECT_VALUE)
            .claim(EMAILS_CLAIM_NAME, List.of(VALID_EMAIL_VALUE))
            .build();

        SignedJWT jwt = createSignedJwt(jwtClaimsSet);

        // Now reconstruct the jwt with a mangled signature
        String jwtWithMangledSignature = new StringJoiner(".")
            .add(new String(jwt.getSigningInput()))
            .add("I AM A MANGLED SIGNATURE")
            .toString();

        JwtValidationResult validationResult = tokenValidator.validate(jwtWithMangledSignature,
                                                                       authenticationProviderConfiguration,
                                                                       authenticationConfiguration);

        assertFalse(validationResult.valid());
        assertEquals("Signed JWT rejected: Invalid signature", validationResult.reason());
    }

    @Test
    void validateShouldThrowExceptionWhenNonParsableTokenIsPresented() {
        String jwt = "I AM NOT PARSABLE AS A JWT";

        JwtValidationResult validationResult = tokenValidator.validate(jwt,
                                                                       authenticationProviderConfiguration,
                                                                       authenticationConfiguration);

        assertFalse(validationResult.valid());
        assertEquals(
            "Invalid JWT serialization: Missing dot delimiter(s)",
            validationResult.reason()
        );
    }

    @SneakyThrows(NoSuchAlgorithmException.class)
    private KeyPair createKeys() {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        return generator.generateKeyPair();
    }

    private JWKSource<SecurityContext> createTestJwkSource() {
        var rsaKey = new Builder((RSAPublicKey) keyPair.getPublic())
            .keyID(KEY_ID_VALUE)
            .keyUse(KeyUse.SIGNATURE)
            .build();

        var jwkSet = new JWKSet(rsaKey);

        return new ImmutableJWKSet<>(jwkSet);
    }

    @SneakyThrows(JOSEException.class)
    private SignedJWT createSignedJwt(JWTClaimsSet jwtClaimsSet) {
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(KEY_ID_VALUE)
            .type(JOSEObjectType.JWT)
            .build();

        SignedJWT signedJwt = new SignedJWT(jwsHeader, jwtClaimsSet);

        RSASSASigner signer = new RSASSASigner(keyPair.getPrivate());
        signedJwt.sign(signer);

        return signedJwt;
    }

    private Date createInstantInFuture() {
        return Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
    }

    private Date createInstantInPast() {
        return Date.from(Instant.now().minus(1, ChronoUnit.HOURS));
    }

}
