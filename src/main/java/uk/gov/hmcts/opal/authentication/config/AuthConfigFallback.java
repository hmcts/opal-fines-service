package uk.gov.hmcts.opal.authentication.config;

import jakarta.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface AuthConfigFallback {
    AuthenticationConfigurationPropertiesStrategy getFallbackStrategy(HttpServletRequest request);
}
