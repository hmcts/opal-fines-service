package uk.gov.hmcts.opal.authentication.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.SecurityContext;
import uk.gov.hmcts.opal.authentication.exception.AuthenticationException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public interface AuthProviderConfigurationProperties {

    String getAuthorizationUri();

    String getTokenUri();

    String getJwkSetUri();

    String getLogoutUri();

    String getResetPasswordUri();

    default JWKSource<SecurityContext> getJwkSource() {
        try {
            URL jwksUrl = new URI(getJwkSetUri()).toURL();

            return JWKSourceBuilder.create(jwksUrl).build();
        } catch (MalformedURLException | URISyntaxException malformedUrlException) {
            throw new AuthenticationException("Sorry authentication jwks URL (" + getJwkSetUri() + ") is incorrect");
        }
    }
}
