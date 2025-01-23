package uk.gov.hmcts.opal.service.legacy;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.util.XmlUtil;

import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public abstract class LegacyService {

    public static final String ACTION_TYPE = "actionType";

    protected final LegacyGatewayProperties legacyGateway;

    protected final RestClient restClient;

    protected abstract Logger getLog();

    @SuppressWarnings("unchecked")
    public <T> T extractResponse(ResponseEntity<String> responseEntity, Class<T> clzz) {
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            if (responseEntity.getBody() != null) {

                String rawXml = responseEntity.getBody();
                getLog().info("extractResponse: Raw XML response: {}", rawXml);

                try {
                    XmlUtil xmlUtil = new XmlUtil();
                    return (T) xmlUtil.unmarshalXmlString(rawXml, clzz);

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
        getLog().info("getFromGateway: GET from Gateway: {}", legacyGateway.getUrl());

        // Create a UriComponentsBuilder and add parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("")
            .queryParam(ACTION_TYPE, actionType);

        ResponseEntity<String> responseEntity = restClient.get()
            .uri(legacyGateway.getUrl() + builder.toUriString())
            .header("AUTHORIZATION", encodeBasic(legacyGateway.getUsername(), legacyGateway.getPassword()))
            .retrieve()
            .toEntity(String.class);

        return extractResponse(responseEntity, responseType);

    }

    public <T> T postParamsToGateway(String actionType, Class<T> responseType, Map<String, Object> requestParams) {
        try {
            return postToGateway(actionType, responseType,
                                 ToJsonString.getObjectMapper().writeValueAsString(requestParams)
            );
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException(jpe);
        }
    }

    public <T> T postToGateway(String actionType, Class<T> responseType, Object request) {
        getLog().debug("postToGateway: POST to Gateway: {}", legacyGateway.getUrl()
            + "?" + ACTION_TYPE + "=" + actionType);

        // Create a UriComponentsBuilder and add parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("")
            .queryParam(ACTION_TYPE, actionType);

        ResponseEntity<String> responseEntity = restClient.post()
            .uri(legacyGateway.getUrl() + builder.toUriString())
            .header("AUTHORIZATION", encodeBasic(legacyGateway.getUsername(), legacyGateway.getPassword()))
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toEntity(String.class);

        return extractResponse(responseEntity, responseType);
    }

    private void pingUrl(String url) {
        getLog().info("Pinging URL: {}", url);
        try {
            ResponseEntity<String> response = restClient.get()
                .uri(url)
                .retrieve()
                .toEntity(String.class);
            getLog().info("Ping response: {}", response.getStatusCode());
        } catch (Exception e) {
            getLog().error("Ping failed for URL: {}", url, e);
        }
    }

    public ResponseEntity<String> postToGatewayRawResponse(String actionType, Object request) {
        String legacyGatewayUrl = legacyGateway.getUrl();
        String googleUrl = "https://www.google.com";
        pingUrl(googleUrl);
        pingUrl(legacyGatewayUrl);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("")
            .queryParam(ACTION_TYPE, actionType);
        String fullUrl = legacyGatewayUrl + builder.toUriString();
        getLog().debug("postToGateway: POST to Gateway URL: {}", fullUrl);

        String authorizationHeader = encodeBasic(legacyGateway.getUsername(), legacyGateway.getPassword());
        getLog().debug("postToGateway: Headers: AUTHORIZATION={}, Content-Type={}", authorizationHeader,
                       MediaType.APPLICATION_JSON);

        return restClient.post()
            .uri(fullUrl)
            .header("AUTHORIZATION", authorizationHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toEntity(String.class);
    }

    private String encodeBasic(String username, String password) {
        return "Basic " + Base64
            .getEncoder()
            .encodeToString((username + ":" + password).getBytes());
    }

}
