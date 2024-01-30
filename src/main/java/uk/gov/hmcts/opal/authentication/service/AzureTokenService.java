package uk.gov.hmcts.opal.authentication.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authentication.client.AzureTokenClient;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthConfigurationProperties;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthProviderConfigurationProperties;
import uk.gov.hmcts.opal.authentication.model.AccessTokenResponse;

@Service
@RequiredArgsConstructor
public class AzureTokenService {

    private final AzureTokenClient azureTokenClient;
    private final InternalAuthConfigurationProperties configuration;
    private final InternalAuthProviderConfigurationProperties provider;
    private final ObjectMapper objectMapper;

    @Value("${azuread.clientId}")
    private String clientId;

    @Value("${azuread.clientSecret}")
    private String clientSecret;

    @Value("${azuread.username}")
    private String username;

    @Value("${azuread.password}")
    private String password;

    @Value("${azuread.resource}")
    private String resource;

    public String getAccessToken() {
        // Assuming the "password" grant type
        String accessTokenResponse = azureTokenClient.getAccessToken(
            "password",
            clientId,
            clientSecret,
            "api://" + clientId + "/opalinternaluser",
            username,
            password
        );

        // Extract the access token from the response (you may need a JSON library for this)
        String accessToken = extractAccessToken(accessTokenResponse);

        return accessToken;
    }

    private String extractAccessToken(String responseBody) {

        try {
            return objectMapper.readValue(responseBody, AccessTokenResponse.class).getAccessToken();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}

