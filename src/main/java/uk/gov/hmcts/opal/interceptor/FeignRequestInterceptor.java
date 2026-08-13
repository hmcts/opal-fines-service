package uk.gov.hmcts.opal.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.service.hmrc.HmrcAuthentication;

@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Autowired
    private HmrcAuthentication auth;

    @Override
    public void apply(RequestTemplate template) {
        if (auth == null) {
            return;
        }
        template.header("Authorization", "Bearer " + auth.getToken());
    }
}