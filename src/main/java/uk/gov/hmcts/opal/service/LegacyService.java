package uk.gov.hmcts.opal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.opal.dto.ToJsonString;

public abstract class LegacyService {

    public static final String ACTION_TYPE = "actionType";
    final String gatewayUrl;

    final RestTemplate restTemplate;

    protected LegacyService(String gatewayUrl, RestTemplate restTemplate) {
        this.gatewayUrl = gatewayUrl;
        this.restTemplate = restTemplate;
    }

    protected abstract Logger getLog();

    public <T> T extractResponse(ResponseEntity<String> responseEntity, Class<T> clzz) {
        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
            String rawJson = responseEntity.getBody();
            getLog().info("extractResponse: Raw JSON response: {}", rawJson);

            try {
                ObjectMapper objectMapper = ToJsonString.getObjectMapper();
                JsonNode root = objectMapper.readTree(rawJson);

                return objectMapper.treeToValue(root, clzz);

            } catch (Exception e) {
                getLog().error("extractResponse: Error deserializing response: {}", e.getMessage(), e);
            }
        } else {
            getLog().warn("extractResponse: Received non-2xx response: {}", responseEntity.getStatusCode());
        }
        return null;
    }

    public <T> T getFromGateway(String actionType, Class<T> responseType, Object... uriVariables) {
        getLog().info("getFromGateway: GET from Gateway: {}",  gatewayUrl);

        // Create a UriComponentsBuilder and add parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("")
            .queryParam(ACTION_TYPE, actionType);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(gatewayUrl
                   + builder.toUriString(), String.class, uriVariables);

        return extractResponse(responseEntity, responseType);

    }

    public <T> T postToGateway(String actionType, Class<T> responseType, Object request) {
        getLog().info("postToGateway: POST to Gateway: {}", gatewayUrl);

        // Create a UriComponentsBuilder and add parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("")
            .queryParam(ACTION_TYPE, actionType);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
            gatewayUrl + builder.toUriString(), request, String.class);

        return extractResponse(responseEntity, responseType);
    }

}
