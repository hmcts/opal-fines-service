package uk.gov.hmcts.opal.versioning;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class ApiVersioningConfig {

    @Value("${version.default:}")
    private String defaultVersionFromProps;

    @Value("${version.header-name:API-Version}")
    private String headerName;

    @Bean
    public RequestVersionResolver requestVersionResolver() {
        return new HeaderRequestVersionResolver(headerName);
    }

    @Bean
    public WebMvcRegistrations webMvcRegistrations(RequestVersionResolver resolver) {

        final String defaultVersion = (defaultVersionFromProps == null || defaultVersionFromProps.isBlank())
            ? null : defaultVersionFromProps;

        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                ApiVersionRequestMappingHandlerMapping mapping =
                    new ApiVersionRequestMappingHandlerMapping(resolver, defaultVersion);

                mapping.setOrder(0);
                return mapping;
            }
        };
    }
}
