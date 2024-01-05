package uk.gov.hmcts.opal.authentication.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.hmcts.opal.authentication.config.AuthStrategySelector;
import uk.gov.hmcts.opal.authentication.controller.AuthenticationController;
import uk.gov.hmcts.opal.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.authentication.service.AuthenticationService;

import java.net.URI;
import java.text.ParseException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public abstract class AbstractUserController implements AuthenticationController {

    private final AuthenticationService authenticationService;

    protected final AuthStrategySelector locator;

    abstract Optional<String> parseEmailAddressFromAccessToken(String accessToken) throws ParseException;

    @Override
    public ModelAndView loginOrRefresh(String authHeaderValue, String redirectUri) {
        String accessToken = null;
        if (authHeaderValue != null) {
            accessToken = authHeaderValue.replace("Bearer ", "");
        }
        URI url = authenticationService.loginOrRefresh(accessToken, redirectUri);
        return new ModelAndView("redirect:" + url.toString());
    }

    @Override
    public SecurityToken handleOauthCode(String code) {
        String accessToken = authenticationService.handleOauthCode(code);
        var securityTokenBuilder = SecurityToken.builder()
            .accessToken(accessToken);

        return securityTokenBuilder.build();
    }

    @Override
    public ModelAndView logout(String authHeaderValue, String redirectUri) {
        String accessToken = authHeaderValue.replace("Bearer ", "");
        URI url = authenticationService.logout(accessToken, redirectUri);
        return new ModelAndView("redirect:" + url.toString());
    }

    @Override
    public ModelAndView resetPassword(String redirectUri) {
        URI url = authenticationService.resetPassword(redirectUri);
        return new ModelAndView("redirect:" + url.toString());
    }
}
