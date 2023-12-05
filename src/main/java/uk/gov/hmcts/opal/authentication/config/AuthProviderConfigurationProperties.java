package uk.gov.hmcts.opal.authentication.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.SecurityContext;
import uk.gov.hmcts.opal.authentication.exception.AuthenticationException;

import java.net.MalformedURLException;
import java.net.URL;

public interface AuthProviderConfigurationProperties {

    String getAuthorizationUri();

    String getTokenUri();

    String getJwkSetUri();

    String getLogoutUri();

    String getResetPasswordUri();

    default JWKSource<SecurityContext> getJwkSource() {
        try {
            URL jwksUrl = new URL(getJwkSetUri());

            return JWKSourceBuilder.create(jwksUrl).build();
        } catch (MalformedURLException malformedUrlException) {
            throw new AuthenticationException("Sorry authentication jwks URL is incorrect");
        }
    }
}
