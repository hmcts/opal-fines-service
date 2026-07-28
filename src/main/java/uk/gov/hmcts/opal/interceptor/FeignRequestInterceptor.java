package uk.gov.hmcts.opal.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.service.hmrc.HMRCAuthentication;

@Component
@AllArgsConstructor
public class FeignRequestInterceptor implements RequestInterceptor {

    private final HMRCAuthentication auth;

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", "Bearer " + auth.getToken());
    }
}