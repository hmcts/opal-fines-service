package uk.gov.hmcts.opal.authentication.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authentication.client.AzureTokenClient;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthConfigurationProperties;
import uk.gov.hmcts.opal.authentication.model.AccessTokenResponse;
import uk.gov.hmcts.opal.authentication.model.AzureTokenRequest;

@Service
@RequiredArgsConstructor
public class AzureTokenService {

    private final AzureTokenClient azureTokenClient;
    private final InternalAuthConfigurationProperties configuration;
    private final ObjectMapper objectMapper;

    public String getAccessToken(String userName, String password) {

        var accessTokenResponse = azureTokenClient.getAccessToken(
            AzureTokenRequest.builder()
                .grantType("password")
                .clientId(configuration.getClientId())
                .clientSecret(configuration.getClientSecret())
                .scope(configuration.getScope())
                .username(userName)
                .password(password)
                .build()
        );

        return extractAccessToken(accessTokenResponse);
    }

    private String extractAccessToken(String responseBody) {

        try {
            return objectMapper.readValue(responseBody, AccessTokenResponse.class).getAccessToken();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}

