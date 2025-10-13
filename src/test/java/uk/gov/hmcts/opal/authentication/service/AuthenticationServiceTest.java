package uk.gov.hmcts.opal.authentication.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.authentication.aspect.LogAuditDetail;
import uk.gov.hmcts.opal.authentication.config.AuthStrategySelector;
import uk.gov.hmcts.opal.authentication.config.AuthenticationConfigurationPropertiesStrategy;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthConfigurationProperties;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthConfigurationPropertiesStrategy;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthProviderConfigurationProperties;
import uk.gov.hmcts.opal.authentication.dao.AzureDao;
import uk.gov.hmcts.opal.authentication.exception.AzureDaoException;
import uk.gov.hmcts.opal.authentication.model.JwtValidationResult;
import uk.gov.hmcts.opal.authentication.model.OAuthProviderRawResponse;
import uk.gov.hmcts.opal.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.authorisation.model.LogActions;
import uk.gov.hmcts.opal.authorisation.service.AuthorisationService;
import uk.gov.hmcts.opal.exception.OpalApiException;

import java.lang.reflect.Method;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final URI DUMMY_AUTH_URI = URI.create("DUMMY_AUTH_URI");
    private static final URI DUMMY_LOGOUT_URI = URI.create("DUMMY_LOGOUT_URI");
    private static final URI DUMMY_LANDING_PAGE_URI = URI.create("DUMMY_LANDING_PAGE_URI");
    private static final String DUMMY_CODE = "DUMMY CODE";
    private static final String DUMMY_ID_TOKEN = "DUMMY ID TOKEN";

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private TokenValidator tokenValidator;

    @Mock
    private AzureDao azureDao;

    @Mock
    private AuthStrategySelector uriProvider;

    @Mock
    private InternalAuthConfigurationProperties internalAuthConfigurationProperties;

    @Mock
    private AuthorisationService authorisationService;

    @Test
    void loginOrRefreshShouldReturnAuthUriWhenNoAuthHeaderExists() {

        AuthenticationConfigurationPropertiesStrategy authStrategyMock = Mockito.mock(
            AuthenticationConfigurationPropertiesStrategy.class);
        when(uriProvider.locateAuthenticationConfiguration()).thenReturn(authStrategyMock);

        when(authStrategyMock.getLoginUri(null))
            .thenReturn(DUMMY_AUTH_URI);

        URI uri = authenticationService.loginOrRefresh(null, null);

        assertEquals(DUMMY_AUTH_URI, uri);
    }

    @Test
    void loginOrRefreshShouldReturnAuthUriWhenInvalidAccessTokenExists() {

        AuthenticationConfigurationPropertiesStrategy authStrategyMock = Mockito.mock(
            AuthenticationConfigurationPropertiesStrategy.class);
        when(uriProvider.locateAuthenticationConfiguration()).thenReturn(authStrategyMock);

        when(authStrategyMock.getLoginUri(null))
            .thenReturn(DUMMY_AUTH_URI);
        when(tokenValidator.validate(
            DUMMY_ID_TOKEN,
            authStrategyMock.getProviderConfiguration(),
            authStrategyMock.getConfiguration()
        ))
            .thenReturn(new JwtValidationResult(false, "Invalid token"));

        URI uri = authenticationService.loginOrRefresh(DUMMY_ID_TOKEN, null);

        assertEquals(DUMMY_AUTH_URI, uri);
    }

    @Test
    void loginOrRefreshShouldReturnLandingPageUriWhenValidAccessTokenExists() {

        AuthenticationConfigurationPropertiesStrategy authStrategyMock = Mockito.mock(
            AuthenticationConfigurationPropertiesStrategy.class);
        when(uriProvider.locateAuthenticationConfiguration()).thenReturn(authStrategyMock);

        when(authStrategyMock.getLandingPageUri())
            .thenReturn(DUMMY_LANDING_PAGE_URI);
        when(tokenValidator.validate(
            DUMMY_ID_TOKEN,
            authStrategyMock.getProviderConfiguration(),
            authStrategyMock.getConfiguration()
        ))
            .thenReturn(new JwtValidationResult(true, null));

        URI uri = authenticationService.loginOrRefresh(DUMMY_ID_TOKEN, null);

        assertEquals(DUMMY_LANDING_PAGE_URI, uri);
    }

    @Test
    void handleOauthCodeShouldReturnLandingPageUriWhenTokenIsObtainedAndValid() throws AzureDaoException {
        when(azureDao.fetchAccessToken(anyString(), notNull(), notNull()))
            .thenReturn(new OAuthProviderRawResponse(null, 0, DUMMY_ID_TOKEN, 0));
        when(tokenValidator.validate(anyString(), notNull(), notNull()))
            .thenReturn(new JwtValidationResult(true, null));
        when(uriProvider.locateAuthenticationConfiguration()).thenReturn(
            new InternalAuthConfigurationPropertiesStrategy(
                internalAuthConfigurationProperties,
                new InternalAuthProviderConfigurationProperties()
            ));
        String token = authenticationService.handleOauthCode(DUMMY_CODE);

        assertEquals(DUMMY_ID_TOKEN, token);
    }

    @Test
    void handleOauthCodeShouldThrowExceptionWhenFetchAccessTokenThrowsException() throws AzureDaoException {
        when(azureDao.fetchAccessToken(anyString(), notNull(), notNull()))
            .thenThrow(AzureDaoException.class);
        when(uriProvider.locateAuthenticationConfiguration())
            .thenReturn(new InternalAuthConfigurationPropertiesStrategy(
                internalAuthConfigurationProperties,
                new InternalAuthProviderConfigurationProperties()
            ));

        OpalApiException exception = assertThrows(
            OpalApiException.class,
            () -> authenticationService.handleOauthCode(DUMMY_CODE)
        );

        assertEquals("100", exception.getError().getErrorTypeNumeric());
    }

    @Test
    void handleOauthCodeShouldThrowExceptionWhenValidationFails() throws AzureDaoException {
        when(azureDao.fetchAccessToken(anyString(), notNull(), notNull()))
            .thenReturn(new OAuthProviderRawResponse(null, 0, DUMMY_ID_TOKEN, 0));
        when(tokenValidator.validate(anyString(), notNull(), notNull()))
            .thenReturn(new JwtValidationResult(false, "validation failure reason"));
        when(uriProvider.locateAuthenticationConfiguration()).thenReturn(
            new InternalAuthConfigurationPropertiesStrategy(
                internalAuthConfigurationProperties,
                new InternalAuthProviderConfigurationProperties()
            ));

        OpalApiException exception = assertThrows(
            OpalApiException.class,
            () -> authenticationService.handleOauthCode(DUMMY_CODE)
        );

        assertEquals("101", exception.getError().getErrorTypeNumeric());
    }

    @Test
    void logoutShouldReturnLogoutPageUriWhenSessionExists() {

        AuthenticationConfigurationPropertiesStrategy authStrategyMock = Mockito.mock(
            AuthenticationConfigurationPropertiesStrategy.class);

        when(uriProvider.locateAuthenticationConfiguration()).thenReturn(authStrategyMock);

        when(authStrategyMock.getLogoutUri(anyString(), any()))
            .thenReturn(DUMMY_LOGOUT_URI);

        URI uri = authenticationService.logout(DUMMY_ID_TOKEN, null);

        assertEquals(DUMMY_LOGOUT_URI, uri);
    }

    @Test
    void resetPasswordShouldReturnResetPasswordUri() {

        AuthenticationConfigurationPropertiesStrategy authStrategyMock = Mockito.mock(
            AuthenticationConfigurationPropertiesStrategy.class);
        when(uriProvider.locateAuthenticationConfiguration()).thenReturn(authStrategyMock);

        when(authStrategyMock.getResetPasswordUri(null))
            .thenReturn(DUMMY_AUTH_URI);

        URI uri = authenticationService.resetPassword(null);

        assertEquals(DUMMY_AUTH_URI, uri);
    }

    @Test
    void testGetSecurityToken() {
        String accessToken = "testAccessToken";
        SecurityToken expectedToken = SecurityToken.builder()
            .accessToken(accessToken)
            .build();

        when(authorisationService.getSecurityToken(accessToken)).thenReturn(expectedToken);

        SecurityToken result = authenticationService.getSecurityToken(accessToken);

        assertEquals(expectedToken, result);
        verify(authorisationService, times(1)).getSecurityToken(accessToken);
    }

    @Nested
    class LogAuditDetailTests {

        @Test
        @SneakyThrows
        void loginOrRefreshHasLogAuditDetail() {

            Method method = AuthenticationService.class.getMethod("loginOrRefresh", String.class, String.class);
            LogAuditDetail annotation = method.getAnnotation(LogAuditDetail.class);

            boolean isAnnotated = method.isAnnotationPresent(LogAuditDetail.class);
            assertTrue(isAnnotated, "loginOrRefresh should be annotated with @LogAuditDetail");
            assertEquals(LogActions.LOG_IN, annotation.action(), "The value property should be 'LOG_IN'");
        }

        @Test
        @SneakyThrows
        void logoutHasLogAuditDetail() {

            Method method = AuthenticationService.class.getMethod("logout", String.class, String.class);
            LogAuditDetail annotation = method.getAnnotation(LogAuditDetail.class);

            boolean isAnnotated = method.isAnnotationPresent(LogAuditDetail.class);
            assertTrue(isAnnotated, "logout should be annotated with @LogAuditDetail");
            assertEquals(LogActions.LOG_OUT, annotation.action(), "The value property should be 'LOG_OUT'");
        }

        @Test
        @SneakyThrows
        void getSecurityTokenHasLogAuditDetail() {

            Method method = AuthenticationService.class.getMethod("getSecurityToken", String.class);
            LogAuditDetail annotation = method.getAnnotation(LogAuditDetail.class);

            boolean isAnnotated = method.isAnnotationPresent(LogAuditDetail.class);
            assertTrue(isAnnotated, "getSecurityToken should be annotated with @LogAuditDetail");
            assertEquals(LogActions.LOG_IN, annotation.action(), "The value property should be 'LOG_IN'");
        }
    }

}
