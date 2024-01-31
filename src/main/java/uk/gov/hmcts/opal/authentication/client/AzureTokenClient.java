package uk.gov.hmcts.opal.authentication.client;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.opal.authentication.model.AzureTokenRequest;

@Service
public class AzureTokenClient {

    private final RestTemplate restTemplate;
    private final String tokenUrl;

    public AzureTokenClient(RestTemplate restTemplate,
                            @Value("${spring.security.oauth2.client.provider.internal-azure-ad-provider.token-uri}")
                            String tokenUrl) {
        this.restTemplate = restTemplate;
        this.tokenUrl = tokenUrl;
    }

    public String getAccessToken(AzureTokenRequest request) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", request.getClientId());
        body.add("client_secret", request.getClientSecret());
        body.add("scope", request.getScope());
        body.add("username", request.getUsername());
        body.add("password", request.getPassword());

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, httpEntity, String.class);

        return response.getBody();
    }
}

