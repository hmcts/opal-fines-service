package uk.gov.hmcts.opal.config;

import feign.RequestInterceptor;
import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {

    @Bean
    public Decoder feignDecoder(ObjectProvider<FeignHttpMessageConverters> messageConverters) {
        return new ResponseEntityDecoder(new SpringDecoder(messageConverters));
    }

    @Bean
    public RequestInterceptor userServiceFmeaHeaderInterceptor(UserServiceFmeaProperties properties) {
        return requestTemplate -> {
            if (!properties.enabled()) {
                return;
            }

            String path = requestTemplate.path();
            if (path != null && path.startsWith("/v2/users/0/state")) {
                requestTemplate.header(properties.headerName(), properties.headerValue());
            }
        };
    }
}
