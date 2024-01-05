package uk.gov.hmcts.opal.authentication.component.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authentication.config.AuthConfigurationProperties;
import uk.gov.hmcts.opal.authentication.config.AuthProviderConfigurationProperties;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthConfigurationPropertiesStrategyTest {

    @Mock
    private AuthConfigurationProperties authConfiguration;

    @Mock
    private AuthProviderConfigurationProperties provider;

    @InjectMocks
    private DummyAuthStrategy authConfig;

    @Test
    void getLoginUriShouldReturnExpectedUri() {
        commonMocksForAuthorisation();
        when(provider.getAuthorizationUri()).thenReturn("AuthUrl");

        when(authConfiguration.getResponseMode()).thenReturn("ResponseMode");
        when(authConfiguration.getResponseType()).thenReturn("ResponseType");

        URI authUrl = authConfig.getLoginUri(null);

        assertEquals(
            "AuthUrl?client_id=ClientId&redirect_uri=RedirectId&scope=Scope&prompt=Prompt"
                + "&response_mode=ResponseMode&response_type=ResponseType",
            authUrl.toString()
        );
    }

    @Test
    void getLoginUriShouldReturnExpectedUriWithOverriddenRedirectUri() {

        commonMocksForAuthorisation();

        when(provider.getAuthorizationUri()).thenReturn("AuthUrl");
        when(authConfiguration.getResponseMode()).thenReturn("ResponseMode");
        when(authConfiguration.getResponseType()).thenReturn("ResponseType");

        URI authUrl = authConfig.getLoginUri("OverriddenRedirectUri");

        assertEquals(
            "AuthUrl?client_id=ClientId&redirect_uri=OverriddenRedirectUri&scope=Scope&prompt=Prompt"
                + "&response_mode=ResponseMode&response_type=ResponseType",
            authUrl.toString()
        );
    }

    @Test
    void getLandingPageUriShouldReturnExpectedUri() {
        URI landingPageUri = authConfig.getLandingPageUri();

        assertEquals("/", landingPageUri.toString());
    }

    @Test
    void getLogoutUriShouldReturnExpectedUri() {

        when(provider.getLogoutUri()).thenReturn("LogoutUrl");
        when(authConfiguration.getLogoutRedirectUri()).thenReturn("LogoutRedirectUrl");

        URI logoutUri = authConfig.getLogoutUri("DUMMY_SESSION_ID", null);

        assertEquals(
            "LogoutUrl?id_token_hint=DUMMY_SESSION_ID&post_logout_redirect_uri=LogoutRedirectUrl",
            logoutUri.toString()
        );
    }

    @Test
    void getLogoutUriShouldReturnExpectedUriWithOverriddenRedirectUri() {
        when(authConfiguration.getLogoutRedirectUri()).thenReturn("LogoutRedirectUrl");
        when(provider.getLogoutUri()).thenReturn("LogoutUrl");

        URI logoutUri = authConfig.getLogoutUri("DUMMY_SESSION_ID", "OverriddenRedirectUri");

        assertEquals(
            "LogoutUrl?id_token_hint=DUMMY_SESSION_ID&post_logout_redirect_uri=OverriddenRedirectUri",
            logoutUri.toString()
        );
    }

    @Test
    void getResetPasswordUriShouldReturnExpectedUri() {
        commonMocksForAuthorisation();

        when(provider.getResetPasswordUri()).thenReturn("ResetUrl");

        URI logoutUri = authConfig.getResetPasswordUri(null);

        assertEquals(
            "ResetUrl?client_id=ClientId&redirect_uri=RedirectId&scope=Scope&prompt=Prompt"
                + "&response_type=id_token",
            logoutUri.toString()
        );
    }

    @Test
    void getResetPasswordUriShouldReturnExpectedUriWithOverriddenRedirectUri() {
        commonMocksForAuthorisation();

        when(provider.getResetPasswordUri()).thenReturn("ResetUrl");

        URI logoutUri = authConfig.getResetPasswordUri("OverriddenRedirectUri");

        assertEquals(
            "ResetUrl?client_id=ClientId&redirect_uri=OverriddenRedirectUri&scope=Scope&prompt=Prompt"
                + "&response_type=id_token",
            logoutUri.toString()
        );
    }

    private AuthConfigurationProperties commonMocksForAuthorisation() {
        when(authConfiguration.getClientId()).thenReturn("ClientId");
        when(authConfiguration.getRedirectUri()).thenReturn("RedirectId");
        when(authConfiguration.getScope()).thenReturn("Scope");
        when(authConfiguration.getPrompt()).thenReturn("Prompt");

        return authConfiguration;
    }
}

