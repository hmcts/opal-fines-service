package uk.gov.hmcts.opal.authentication.component.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.opal.authentication.config.AuthConfigurationProperties;
import uk.gov.hmcts.opal.authentication.config.AuthProviderConfigurationProperties;
import uk.gov.hmcts.opal.authentication.config.AuthenticationConfigurationPropertiesStrategy;

@Getter
@Setter
@RequiredArgsConstructor
public class DummyAuthStrategy implements AuthenticationConfigurationPropertiesStrategy {
    private final AuthConfigurationProperties configurationProperties;
    private final AuthProviderConfigurationProperties configurationProviderProperties;

    @Override
    public AuthConfigurationProperties getConfiguration() {
        return configurationProperties;
    }

    @Override
    public AuthProviderConfigurationProperties getProviderConfiguration() {
        return configurationProviderProperties;
    }

    @Override
    public boolean doesMatch(HttpServletRequest req) {
        return false;
    }
}
