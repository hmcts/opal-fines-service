package uk.gov.hmcts.opal.authentication.service;

import com.nimbusds.jwt.JWT;
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

    public String extractUserEmail(String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            JWT parsedJwt = tokenValidator.parse(token);
            return parsedJwt.getJWTClaimsSet().getClaim("preferred_username").toString();
        } catch (ParseException e) {
            log.error("Unable to parse token: " + e.getMessage());
            throw new OpalApiException(AuthenticationError.FAILED_TO_PARSE_ACCESS_TOKEN, e);
        }
    }

    public String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        log.error("Unable to extract bearer token: ");
        throw new OpalApiException(AuthenticationError.FAILED_TO_PARSE_ACCESS_TOKEN);
    }
}

