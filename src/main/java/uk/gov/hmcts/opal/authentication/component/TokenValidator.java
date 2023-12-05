package uk.gov.hmcts.opal.authentication.component;

import uk.gov.hmcts.opal.authentication.config.AuthConfigurationProperties;
import uk.gov.hmcts.opal.authentication.config.AuthProviderConfigurationProperties;
import uk.gov.hmcts.opal.authentication.model.JwtValidationResult;

public interface TokenValidator {

    JwtValidationResult validate(String accessToken, AuthProviderConfigurationProperties providerConfig, AuthConfigurationProperties configuration);

}
