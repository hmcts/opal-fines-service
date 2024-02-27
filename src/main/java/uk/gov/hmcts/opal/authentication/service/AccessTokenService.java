package uk.gov.hmcts.opal.authentication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authentication.client.AzureTokenClient;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthConfigurationProperties;
import uk.gov.hmcts.opal.authentication.model.AccessTokenRequest;
import uk.gov.hmcts.opal.authentication.model.AccessTokenResponse;
import uk.gov.hmcts.opal.config.properties.TestUser;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

    private final InternalAuthConfigurationProperties configuration;
    private final AzureTokenClient azureTokenClient;
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

}

