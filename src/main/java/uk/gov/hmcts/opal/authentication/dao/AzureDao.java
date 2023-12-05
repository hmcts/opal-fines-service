package uk.gov.hmcts.opal.authentication.dao;

import uk.gov.hmcts.opal.authentication.config.AuthConfigurationProperties;
import uk.gov.hmcts.opal.authentication.config.AuthProviderConfigurationProperties;
import uk.gov.hmcts.opal.authentication.exception.AzureDaoException;
import uk.gov.hmcts.opal.authentication.model.OAuthProviderRawResponse;

public interface AzureDao {
    OAuthProviderRawResponse fetchAccessToken(String code, AuthProviderConfigurationProperties providerConfig,
                                              AuthConfigurationProperties configuration) throws AzureDaoException;
}
