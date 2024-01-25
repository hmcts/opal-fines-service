package uk.gov.hmcts.opal.authentication.config;

import lombok.SneakyThrows;
import org.apache.http.client.utils.URIBuilder;
import uk.gov.hmcts.opal.authentication.util.RequestMatcher;

import java.net.URI;
import java.net.URISyntaxException;


public interface AuthenticationConfigurationPropertiesStrategy extends RequestMatcher {
    AuthConfigurationProperties getConfiguration();

    AuthProviderConfigurationProperties getProviderConfiguration();

    @SneakyThrows(URISyntaxException.class)
    default URI getLoginUri(String redirectUri) {
        return buildCommonAuthUri(getProviderConfiguration().getAuthorizationUri(), redirectUri)
            .addParameter("response_mode", getConfiguration().getResponseMode())
            .addParameter("response_type", getConfiguration().getResponseType())
            .build();
    }

    default URI getLandingPageUri() {
        return URI.create("/");
    }

    @SneakyThrows(URISyntaxException.class)
    default URI getLogoutUri(String accessToken, String redirectUriOverride) {
        var redirectUri = getConfiguration().getLogoutRedirectUri();
        if (redirectUriOverride != null) {
            redirectUri = redirectUriOverride;
        }
        return new URIBuilder(
            getProviderConfiguration().getLogoutUri())
            .addParameter("id_token_hint", accessToken)
            .addParameter("post_logout_redirect_uri", redirectUri)
            .build();
    }

    @SneakyThrows(URISyntaxException.class)
    default URI getResetPasswordUri(String redirectUri) {
        return buildCommonAuthUri(getProviderConfiguration().getResetPasswordUri(), redirectUri)
            .addParameter("response_type", "id_token")
            .build();
    }

    @SneakyThrows(URISyntaxException.class)
    private URIBuilder buildCommonAuthUri(String uri, String redirectUriOverride) {
        var redirectUri = getConfiguration().getRedirectUri();
        if (redirectUriOverride != null) {
            redirectUri = redirectUriOverride;
        }
        return new URIBuilder(uri)
            .addParameter("client_id", getConfiguration().getClientId())
            .addParameter("redirect_uri", redirectUri)
            .addParameter("scope", getConfiguration().getScope())
            .addParameter("prompt", getConfiguration().getPrompt());
    }
}
