package uk.gov.hmcts.opal.authentication.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthProviderConfigurationProperties;
import uk.gov.hmcts.opal.authentication.model.AccessTokenResponse;
import uk.gov.hmcts.opal.authentication.model.AccessTokenRequest;


@Slf4j(topic = "AzureTokenClient")
@Service
@RequiredArgsConstructor
public class AzureTokenClient {

    private final RestClient restClient;
    private final InternalAuthProviderConfigurationProperties provider;

    public AccessTokenResponse getAccessToken(AccessTokenRequest request) {

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", request.getClientId());
        body.add("client_secret", request.getClientSecret());
        body.add("scope", request.getScope());
        body.add("username", request.getUsername());
        body.add("password", request.getPassword());

        log.info(":getAccessToken:");

        ResponseEntity<AccessTokenResponse> response = restClient
            .post()
            .uri(this.provider.getTokenUri())
            .body(body)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .retrieve().toEntity(AccessTokenResponse.class);

        return response.getBody();
    }

}
