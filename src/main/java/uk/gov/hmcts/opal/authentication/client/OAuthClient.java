package uk.gov.hmcts.opal.authentication.client;

import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import uk.gov.hmcts.opal.authentication.config.AuthProviderConfigurationProperties;

public interface OAuthClient {
    HTTPResponse fetchAccessToken(AuthProviderConfigurationProperties providerConfigurationProperties,
                                  String redirectType, String authCode,
                                  String clientId,
                                  String authClientSecret, String scope);
}
