package uk.gov.hmcts.opal.service.hmrc.clients;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.opal.interceptor.FeignRequestInterceptor;
import uk.gov.hmcts.opal.service.hmrc.HmrcAuthentication;

/**
 * Configuration for HMRC Feign clients (Must not have @Component, @Service etc on the class itself).
 */
public class HmrcFeignClientConfiguration {

    @Bean
    public RequestInterceptor hmrcRequestInterceptor(HmrcAuthentication authentication) {
        return new FeignRequestInterceptor(authentication);
    }
}

