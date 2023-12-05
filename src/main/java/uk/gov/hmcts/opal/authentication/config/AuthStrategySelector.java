package uk.gov.hmcts.opal.authentication.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthStrategySelector {

    private final List<AuthenticationConfigurationPropertiesStrategy> configMatchers;

    public final AuthConfigFallback defaultFallback;


    public AuthenticationConfigurationPropertiesStrategy locateAuthenticationConfiguration(AuthConfigFallback fallback) {
        HttpServletRequest request =
            ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        Optional<AuthenticationConfigurationPropertiesStrategy> configuration
            = configMatchers.stream().filter(e -> e.doesMatch(request)).findFirst();

        if (fallback == null && !configuration.isPresent()) {
            return defaultFallback.getFallbackStrategy(request);
        } else if (fallback != null && !configuration.isPresent()) {
            return fallback.getFallbackStrategy(request);
        }

        return configuration.get();
    }

    public AuthenticationConfigurationPropertiesStrategy locateAuthenticationConfiguration() {
        return locateAuthenticationConfiguration(null);
    }
}
