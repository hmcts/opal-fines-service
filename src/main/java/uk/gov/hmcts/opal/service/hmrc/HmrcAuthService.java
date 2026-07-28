package uk.gov.hmcts.opal.service.hmrc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.cache.CacheNames;
import uk.gov.hmcts.opal.service.hmrc.response.HmrcAuthToken;

@Service
@Slf4j(topic = "opal.HmrcAuthService")
public class HmrcAuthService {

    private final RestClient restClient;
    private final HmrcAuthCreds credentials;
    private final String url;

    public HmrcAuthService(RestClient restClient, HmrcAuthCreds credentials, @Value("${hmrc.auth.url}") String url) {
        this.restClient = restClient;
        this.credentials = credentials;
        this.url = url;
    }

    @Cacheable(CacheNames.HMRC_AUTH_SERVICE)
    public HmrcAuthToken getAuthToken() {

        return restClient.post()
            .uri(url)
            .body(credentials)
            .retrieve()
            .body(HmrcAuthToken.class);
    }
}
