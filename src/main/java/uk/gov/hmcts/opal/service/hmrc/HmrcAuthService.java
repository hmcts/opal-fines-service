package uk.gov.hmcts.opal.service.hmrc;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.opal.config.cache.CacheKeys;
import uk.gov.hmcts.opal.service.hmrc.response.HMRCAuthToken;

@Service
public class HmrcAuthService {

    private static final String GRANT_TYPE = "client_credentials";

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final String url;

    public HmrcAuthService(RestClient restClient,
        @Value("opal.hmrc.auth.client-id") String clientId,
        @Value("opal.hmrc.auth.client-secret") String clientSecret,
        @Value("opal.hmrc.auth.scope") String scope,
        @Value("opal.hmrc.auth.url") String url) {

        this.restClient = restClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.url = url;
    }

    @Cacheable(key = CacheKeys.HMRC_AUTH_TOKEN)
    public HMRCAuthToken getAuthToken() {
        URI uri = buildUri();

        // TODO error handling
        return restClient.get()
            .uri(uri)
            .retrieve()
            .body(HMRCAuthToken.class); // TODO check can de-serialize into constructor
    }

    URI buildUri() {
        return UriComponentsBuilder.fromUriString(url)
            .replaceQueryParam("client_id", clientId)
            .replaceQueryParam("client_secret", clientSecret)
            .replaceQueryParam("grant_type", GRANT_TYPE)
            .replaceQueryParam("scope", scope)
            .build()
            .toUri();
    }
}
