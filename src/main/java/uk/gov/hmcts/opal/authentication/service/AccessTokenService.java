package uk.gov.hmcts.opal.authentication.service;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authentication.client.AzureTokenClient;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthConfigurationProperties;
import uk.gov.hmcts.opal.authentication.exception.AuthenticationError;
import uk.gov.hmcts.opal.authentication.model.AccessTokenRequest;
import uk.gov.hmcts.opal.authentication.model.AccessTokenResponse;
import uk.gov.hmcts.opal.config.properties.TestUser;
import uk.gov.hmcts.opal.exception.OpalApiException;

import java.text.ParseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessTokenService {

    public static final String AUTH_HEADER = "authorization";
    public static final String PREFERRED_USERNAME_KEY = "preferred_username";
    public static final String NAME_KEY = "name";
    public static final String SCP_KEY = "scp";
    public static final String UNIQUE_NAME_KEY = "unique_name";
    public static final String UPN_NAME_KEY = "upn";
    public static final String BEARER_PREFIX = "Bearer ";

    private final InternalAuthConfigurationProperties configuration;
    private final AzureTokenClient azureTokenClient;
    private final TokenValidator tokenValidator;
    private final TestUser testUser;

    public AccessTokenResponse getTestUserToken() {
        return getAccessToken(testUser.getEmail(), testUser.getPassword());
    }

    public AccessTokenResponse getTestUserToken(String userEmail) {
        return getAccessToken(userEmail, testUser.getPassword());
    }

    public AccessTokenResponse getAccessToken(String userName, String password) {

        AccessTokenRequest tokenRequest = AccessTokenRequest.builder()
            .grantType("password")
            .clientId(configuration.getClientId())
            .clientSecret(configuration.getClientSecret())
            .scope(configuration.getScope())
            .username(userName)
            .password(password)
            .build();

        return azureTokenClient.getAccessToken(
            tokenRequest
        );
    }

    public String extractPreferredUsername(String accessToken) {
        return extractClaim(accessToken, PREFERRED_USERNAME_KEY);
    }

    public String extractName(String accessToken) {
        return extractClaim(accessToken, NAME_KEY);
    }

    public String extractScp(String accessToken) {
        return extractClaim(accessToken, SCP_KEY);
    }

    public String extractUniqueName(String accessToken) {
        return extractClaim(accessToken, UNIQUE_NAME_KEY);
    }

    public String extractUpn(String accessToken) {
        return extractClaim(accessToken, UPN_NAME_KEY);
    }

    public String extractClaim(String accessToken, String claimKey) {
        return extractClaims(accessToken).getClaim(claimKey).toString();
    }

    public JWTClaimsSet extractClaims(String accessToken) {
        try {
            String token = extractToken(accessToken);
            JWT parsedJwt = tokenValidator.parse(token);
            return parsedJwt.getJWTClaimsSet();
        } catch (ParseException e) {
            log.error(":extractClaim: Unable to extract claims from JWT Token: {}", e.getMessage());
            throw new OpalApiException(AuthenticationError.FAILED_TO_PARSE_ACCESS_TOKEN, e);
        }
    }

    public String extractToken(String accessToken) {
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            return accessToken.substring(7);
        }
        return accessToken;
    }
}
