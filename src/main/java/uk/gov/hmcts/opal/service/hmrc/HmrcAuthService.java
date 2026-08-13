package uk.gov.hmcts.opal.service.hmrc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.config.cache.CacheNames;
import uk.gov.hmcts.opal.service.hmrc.clients.HmrcAuthClient;
import uk.gov.hmcts.opal.service.hmrc.clients.HmrcAuthCreds;
import uk.gov.hmcts.opal.service.hmrc.clients.response.HmrcAuthToken;

@Service
@Slf4j(topic = "opal.HmrcAuthService")
@RequiredArgsConstructor
public class HmrcAuthService implements HmrcAuthentication {

    private final HmrcAuthClient client;
    private final HmrcAuthCreds credentials;

    @Override
    @Cacheable(CacheNames.HMRC_AUTH_SERVICE)
    public String getToken() {
        HmrcAuthToken token = client.getAuthToken(credentials);
        return token.getAccessToken();
    }
}
