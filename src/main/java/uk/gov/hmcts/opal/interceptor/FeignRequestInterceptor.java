package uk.gov.hmcts.opal.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.AllArgsConstructor;
import uk.gov.hmcts.opal.service.hmrc.HmrcAuthentication;

@AllArgsConstructor
public class FeignRequestInterceptor implements RequestInterceptor {

    private HmrcAuthentication auth;

    @Override
    public void apply(RequestTemplate template) {
        //Only add the Authorization header to requests that are not for the token endpoint
        if (template.url().equalsIgnoreCase("/oauth/token")) {
            return;
        }
        template.header("Authorization", "Bearer " + auth.getToken());
    }
}