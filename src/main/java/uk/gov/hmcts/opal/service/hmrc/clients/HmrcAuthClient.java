package uk.gov.hmcts.opal.service.hmrc.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.opal.service.hmrc.HmrcAuthCreds;
import uk.gov.hmcts.opal.service.hmrc.response.HmrcAuthToken;

@FeignClient(name = "hmrcAuthClient", url = "${hmrc.auth.url}")
public interface HmrcAuthClient {

    @PostMapping
    HmrcAuthToken getAuthToken(HmrcAuthCreds creds);
}
