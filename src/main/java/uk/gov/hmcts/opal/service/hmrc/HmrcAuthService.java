package uk.gov.hmcts.opal.service.hmrc;

import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.opal.config.cache.CacheKeys;
import uk.gov.hmcts.opal.service.hmrc.response.HMRCAuthToken;

@Service
@RequiredArgsConstructor
public class HmrcAuthService {

    private static final String GRANT_TYPE = "client_credentials";

    private final RestClient restClient;
    private final String URL = "https://todo.put.in.application.yaml"; // TODO put in config

//    @Cacheable(key = CacheKeys.HMRC_AUTH_TOKEN)
//    public HMRCAuthToken getAuthToken(String[] scopes) {
//        return getAuthToken(Set.of(scopes));
//    }

    @Cacheable(key = CacheKeys.HMRC_AUTH_TOKEN)
    public HMRCAuthToken getAuthToken(Set<String> scopes) {
        URI uri = buildUri(scopes);

        // TODO error handling
        return restClient.get()
            .uri(uri)
            .retrieve()
            .body(HMRCAuthToken.class); // TODO check can de-serialize into constructor
    }

    URI buildUri(Set<String> scopes) {

        String scopesJoined = scopes.stream().collect(Collectors.joining("+"));

        return UriComponentsBuilder.fromUriString(URL)
            .replaceQueryParam("client_secret", "") // TODO client_secret
            .replaceQueryParam("client_id", "") // TODO client_id
            .replaceQueryParam("grant_type", GRANT_TYPE)
            .replaceQueryParam("scope", scopesJoined)
            .build()
            .toUri();
    }
}
