package uk.gov.hmcts.opal.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.opal.interceptor.AcceptHeaderInterceptor;
import uk.gov.hmcts.opal.spring.interceptor.ContentDigestValidatorInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ContentDigestValidatorInterceptor contentDigestValidatorInterceptor;
    private final AcceptHeaderInterceptor acceptHeaderInterceptor;

    @Value("${opal.content-digest.request.enforce:true}")
    private boolean enforceContentDigest;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(acceptHeaderInterceptor);
        if (enforceContentDigest) {
            registry.addInterceptor(contentDigestValidatorInterceptor);
        }
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        ApplicationConversionService.configure(registry);
    }
}
