package uk.gov.hmcts.opal.versioning;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class ApiVersionValidationConfig {

    @Value("${version.default:}")
    private String defaultVersion;

    @Value("${version.require-header:false}")
    private boolean requireHeader;


    @Bean
    public ApiVersionValidationFilter apiVersionValidationFilter(RequestVersionResolver resolver,
        ApiVersionSupport apiVersionSupport, HandlerExceptionResolver handlerExceptionResolver) {
        String def = (defaultVersion == null || defaultVersion.isBlank()) ? null : defaultVersion;
        return new ApiVersionValidationFilter(resolver, def, requireHeader, apiVersionSupport,
            handlerExceptionResolver);
    }
}