package uk.gov.hmcts.opal.service.legacy;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.util.XmlUtil;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j(topic = "opal.LegacyGatewayService")
@RequiredArgsConstructor
public class LegacyGatewayService implements GatewayService {

    public static final String ACTION_TYPE = "actionType";

    protected final LegacyGatewayProperties gatewayProperties;

    protected final RestClient restClient;

    @SuppressWarnings("unchecked")
    public <T> Response<T> extractResponse(ResponseEntity<String> responseEntity, Class<T> clzz, String schemaFile) {
        HttpStatusCode code = responseEntity.getStatusCode();

        if (clzz.equals(String.class)) {
            return new Response<>(code, responseEntity.getBody());
        }

        if (responseEntity.getBody() != null) {

            String rawXml = responseEntity.getBody();
            log.info("extractResponse: Raw XML response: \n{}", rawXml);

            XmlUtil.validateXmlString(schemaFile, rawXml);

            try {
                T entity = XmlUtil.unmarshalXmlString(rawXml, clzz);
                return new Response<>(code, entity);

            } catch (Exception e) {
                log.error("extractResponse: Error deserializing response: {}", e.getMessage(), e);
                return new Response<>(code, e, rawXml);
            }
        } else {
            log.warn("extractResponse: Received an empty body in the response from the Legacy Gateway.");
            return new Response<>(code, null);
        }
    }

    public <T> Response<T> postToGateway(String actionType, Class<T> responseType, Object request,
                                         String responseSchemaFile) {
        log.info("postToGateway: POST to Gateway: {}?{}={}", gatewayProperties.getUrl(), ACTION_TYPE, actionType);

        // Create a UriComponentsBuilder and add parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("")
            .queryParam(ACTION_TYPE, actionType);

        RestClient.RequestBodySpec bodySpec = restClient.post()
            .uri(gatewayProperties.getUrl() + builder.toUriString())
            .header("AUTHORIZATION", encodeBasic(gatewayProperties.getUsername(), gatewayProperties.getPassword()))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_XML)
            .body(request);

        log.info(":postToGateway: bodySpec: {}", bodySpec);

        ResponseEntity<String> responseEntity = bodySpec
            .retrieve()
            .toEntity(String.class);

        return extractResponse(responseEntity, responseType, responseSchemaFile);
    }

    /*
     * This may not be required, but a similar method was implemented in Disco+, so is left here for now.
     */
    public Response<String> postToGateway(String actionType, Object request) {
        return postToGateway(actionType, String.class, request, null);
    }

    @Async
    public <T> CompletableFuture<Response<T>> postToGatewayAsync(String actionType, Class<T> responseType,
                                                                 Object request, String responseSchemaFile) {

        log.debug("postToGatewayAsync: ASYNC POST to Gateway: {}", gatewayProperties.getUrl()
            + "?" + ACTION_TYPE + "=" + actionType);

        long start = System.currentTimeMillis();
        Response<T> response = postToGateway(actionType, responseType, request, responseSchemaFile);
        long end = System.currentTimeMillis();

        log.debug("postToGatewayAsync: ASYNC POST to Gateway response in {} seconds.", (end - start) / 1000f);

        return CompletableFuture.completedFuture(response);

    }

    @Override
    public <T> Response<T> patchToGateway(
        String actionType, Class<T> responseType, Object request, String responseSchemaFile) {
        // Legacy gateway does not support HTTP PATCH — tunnel with POST.
        log.debug("Legacy gateway does not support PATCH; tunneling PATCH '{}' via POST", actionType);
        return postToGateway(actionType, responseType, request, responseSchemaFile);
    }

    public Response<String> patchToGateway(String actionType, Object request) {
        return patchToGateway(actionType, String.class, request, null);
    }

    @Override
    @Async
    public <T> CompletableFuture<Response<T>> patchToGatewayAsync(
        String actionType, Class<T> responseType, Object request, String responseSchemaFile) {
        return CompletableFuture.completedFuture(
            patchToGateway(actionType, responseType, request, responseSchemaFile)
        );
    }

    /*
     * This may not be required, but a similar method was implemented in Disco+, so is left here for now.
     */
    public <T> Response<T> postParamsToGateway(String actionType, Class<T> responseType, Map<String,
        Object> requestParams) {

        try {
            return postToGateway(actionType, responseType,
                                 ToJsonString.getObjectMapper().writeValueAsString(requestParams), null
            );
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException(jpe);
        }
    }

    private String encodeBasic(String username, String password) {
        return "Basic " + Base64
            .getEncoder()
            .encodeToString((username + ":" + password).getBytes());
    }
}
