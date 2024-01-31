package uk.gov.hmcts.opal.service.legacy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.util.Map;

public abstract class LegacyService {

    public static final String ACTION_TYPE = "actionType";
    final String gatewayUrl;

    final RestClient restClient;

    protected LegacyService(String gatewayUrl, RestClient restTemplate) {
        this.gatewayUrl = gatewayUrl;
        this.restClient = restTemplate;
    }

    protected abstract Logger getLog();

    public <T> T extractResponse(ResponseEntity<String> responseEntity, Class<T> clzz) {
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            if (responseEntity.getBody() != null) {

                String rawJson = responseEntity.getBody();
                getLog().info("extractResponse: Raw JSON response: {}", rawJson);

                try {
                    ObjectMapper objectMapper = ToJsonString.getObjectMapper();
                    JsonNode root = objectMapper.readTree(rawJson);

                    return objectMapper.treeToValue(root, clzz);

                } catch (Exception e) {
                    getLog().error("extractResponse: Error deserializing response: {}", e.getMessage(), e);
                    throw new LegacyGatewayResponseException(e);
                }
            } else {
                String msg = "Received an empty body in the response from the Legacy Gateway.";
                getLog().warn("extractResponse: {}", msg);
                throw new LegacyGatewayResponseException(msg);
            }
        } else {
            String msg = MessageFormatter.format(
                "Received a non-2xx response from the Legacy Gateway: {}",
                responseEntity.getStatusCode()
            ).getMessage();
            getLog().warn("extractResponse: {}", msg);
            throw new LegacyGatewayResponseException(msg);
        }
    }

    public <T> T getFromGateway(String actionType, Class<T> responseType) {
        getLog().info("getFromGateway: GET from Gateway: {}", gatewayUrl);

        // Create a UriComponentsBuilder and add parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("")
            .queryParam(ACTION_TYPE, actionType);

        ResponseEntity<String> responseEntity = restClient.get()
            .uri(gatewayUrl + builder.toUriString())
            .retrieve()
            .toEntity(String.class);

        return extractResponse(responseEntity, responseType);

    }

    public <T> T postParamsToGateway(String actionType, Class<T> responseType, Map<String, Object> requestParams) {
        try {
            return postToGateway(actionType, responseType,
                                 ToJsonString.getObjectMapper().writeValueAsString(requestParams));
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException(jpe);
        }
    }

    public <T> T postToGateway(String actionType, Class<T> responseType, Object request) {
        getLog().info("postToGateway: POST to Gateway: {}", gatewayUrl);

        // Create a UriComponentsBuilder and add parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("")
            .queryParam(ACTION_TYPE, actionType);

        ResponseEntity<String> responseEntity = restClient.post()
            .uri(gatewayUrl + builder.toUriString())
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toEntity(String.class);

        return extractResponse(responseEntity, responseType);
    }


}
