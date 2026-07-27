package uk.gov.hmcts.opal.service.hmrc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.cache.CacheNames;
import uk.gov.hmcts.opal.service.hmrc.creds.HMRCAuthTokenCreds;
import uk.gov.hmcts.opal.service.hmrc.response.HMRCAuthToken;

@Service
@Slf4j(topic = "opal.HmrcAuthService")
public class HmrcAuthService {

    private final RestClient restClient;
    private final HMRCAuthTokenCreds creds;
    private final String url;

    public HmrcAuthService(RestClient restClient,
        HMRCAuthTokenCreds creds,
        @Value("${hmrc.auth.url}") String url) {

        this.restClient = restClient;
        this.creds = creds;
        this.url = url;
    }

    @Cacheable(CacheNames.HMRC_AUTH_SERVICE)
    public HMRCAuthToken getAuthToken() {

        return restClient.post()
            .uri(url)
            .body(creds)
            .retrieve()
            .body(HMRCAuthToken.class);
    }
}
