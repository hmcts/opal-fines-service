package uk.gov.hmcts.opal.service.hmrc;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.opal.config.cache.CacheKeys;
import uk.gov.hmcts.opal.service.hmrc.response.HMRCAuthToken;

@Service
@Slf4j(topic = "opal.HmrcAuthService")
public class HmrcAuthService {

    private static final String GRANT_TYPE = "client_credentials";

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final String url;

    public HmrcAuthService(RestClient restClient,
        @Value("${hmrc.auth.client-id}") String clientId,
        @Value("${hmrc.auth.client-secret}") String clientSecret,
        @Value("${hmrc.auth.scope}") String scope,
        @Value("${hmrc.auth.url}") String url) {

        this.restClient = restClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.url = url;
    }

    @Cacheable(value = "HmrcAuthService", key = "'" + CacheKeys.HMRC_AUTH_TOKEN + "'")
    public HMRCAuthToken getAuthToken() {
        URI uri = buildUri();

        return restClient.get()
            .uri(uri)
            .retrieve()
//            .onStatus(HttpStatusCode::isError, ((req, res) -> {
//                log.error("Error requesting HMRC auth token: {} {}", res.getStatusCode(), res.getStatusText());
//            }))
            .body(HMRCAuthToken.class);
    }

    private URI buildUri() {
        return UriComponentsBuilder.fromUriString(url)
            .queryParam("client_id", clientId)
            .queryParam("client_secret", clientSecret)
            .queryParam("grant_type", GRANT_TYPE)
            .queryParam("scope", scope)
            .build()
            .toUri();
    }
}
