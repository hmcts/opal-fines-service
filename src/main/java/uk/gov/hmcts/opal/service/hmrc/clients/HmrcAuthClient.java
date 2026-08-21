package uk.gov.hmcts.opal.service.hmrc.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.opal.service.hmrc.clients.response.HmrcAuthToken;

@FeignClient(name = "hmrcAuthClient",
    url = "${opal.hmrc.url}",
    configuration = HmrcFeignClientConfiguration.class
)

public interface HmrcAuthClient {

    @PostMapping("/oauth/token")
    HmrcAuthToken getAuthToken(HmrcAuthCreds creds);
}
