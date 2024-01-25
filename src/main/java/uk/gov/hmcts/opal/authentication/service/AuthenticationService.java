package uk.gov.hmcts.opal.authentication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authentication.config.AuthStrategySelector;
import uk.gov.hmcts.opal.authentication.config.AuthenticationConfigurationPropertiesStrategy;
import uk.gov.hmcts.opal.authentication.dao.AzureDao;
import uk.gov.hmcts.opal.authentication.exception.AuthenticationError;
import uk.gov.hmcts.opal.authentication.exception.AzureDaoException;
import uk.gov.hmcts.opal.authentication.model.OAuthProviderRawResponse;
import uk.gov.hmcts.opal.exception.OpalApiException;

import java.net.URI;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final TokenValidator tokenValidator;
    private final AzureDao azureDao;

    private final AuthStrategySelector locator;

    public URI loginOrRefresh(String accessToken, String redirectUri) {
        log.debug("Initiated login or refresh flow with access token {}", accessToken);

        AuthenticationConfigurationPropertiesStrategy configStrategy = locator.locateAuthenticationConfiguration();
        if (accessToken == null) {
            return configStrategy.getLoginUri(redirectUri);
        }

        var validationResult = tokenValidator.validate(
            accessToken,
            configStrategy.getProviderConfiguration(),
            configStrategy.getConfiguration()
        );

        if (!validationResult.valid()) {
            return configStrategy.getLoginUri(redirectUri);
        }

        return configStrategy.getLandingPageUri();
    }

    public String handleOauthCode(String code) {
        AuthenticationConfigurationPropertiesStrategy configStrategy = locator.locateAuthenticationConfiguration();

        log.debug("Presented authorization code {}", code);

        OAuthProviderRawResponse tokenResponse;
        try {
            tokenResponse = azureDao.fetchAccessToken(
                code,
                configStrategy.getProviderConfiguration(),
                configStrategy.getConfiguration()
            );
        } catch (AzureDaoException e) {
            throw new OpalApiException(AuthenticationError.FAILED_TO_OBTAIN_ACCESS_TOKEN, e);
        }
        var accessToken = Objects.nonNull(tokenResponse.getIdToken())
            ? tokenResponse.getIdToken()
            : tokenResponse.getAccessToken();

        var validationResult = tokenValidator.validate(
            accessToken,
            configStrategy.getProviderConfiguration(),
            configStrategy.getConfiguration()
        );
        if (!validationResult.valid()) {
            log.error("Invalid reason: " + validationResult.reason());
            throw new OpalApiException(AuthenticationError.FAILED_TO_VALIDATE_ACCESS_TOKEN);
        }

        return accessToken;
    }

    public URI logout(String accessToken, String redirectUri) {
        AuthenticationConfigurationPropertiesStrategy configStrategy = locator.locateAuthenticationConfiguration();

        log.debug("Initiated logout flow with access token {} and redirectUri {}", accessToken, redirectUri);
        return configStrategy.getLogoutUri(accessToken, redirectUri);
    }

    public URI resetPassword(String redirectUri) {
        AuthenticationConfigurationPropertiesStrategy configStrategy = locator.locateAuthenticationConfiguration();

        log.debug("Requesting password reset, with redirectUri {}", redirectUri);
        return configStrategy.getResetPasswordUri(redirectUri);
    }
}
