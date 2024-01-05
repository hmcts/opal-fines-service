package uk.gov.hmcts.opal.authentication.controller;

import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.hmcts.opal.authentication.config.AuthStrategySelector;
import uk.gov.hmcts.opal.authentication.config.AuthenticationConfigurationPropertiesStrategy;
import uk.gov.hmcts.opal.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.authentication.service.AuthenticationService;

import java.net.URI;
import java.text.ParseException;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/internal-user")
@RequiredArgsConstructor
public class AuthenticationInternalUserController {

    private final AuthenticationService authenticationService;

    private final AuthStrategySelector locator;

    @GetMapping("/login-or-refresh")
    public ModelAndView loginOrRefresh(
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue,
        @RequestParam(value = "redirect_uri", required = false) String redirectUri
    ) {
        String accessToken = null;
        if (authHeaderValue != null) {
            accessToken = authHeaderValue.replace("Bearer ", "");
        }
        URI url = authenticationService.loginOrRefresh(accessToken, redirectUri);
        return new ModelAndView("redirect:" + url.toString());
    }

    @PostMapping("/handle-oauth-code")
    public SecurityToken handleOauthCode(@RequestParam("code") String code) {
        String accessToken = authenticationService.handleOauthCode(code);
        var securityTokenBuilder = SecurityToken.builder()
            .accessToken(accessToken);

        return securityTokenBuilder.build();
    }

    @GetMapping("/logout")
    public ModelAndView logout(
        @RequestHeader("Authorization") String authHeaderValue,
        @RequestParam(value = "redirect_uri", required = false) String redirectUri
    ) {
        String accessToken = authHeaderValue.replace("Bearer ", "");
        URI url = authenticationService.logout(accessToken, redirectUri);
        return new ModelAndView("redirect:" + url.toString());
    }

    @GetMapping("/reset-password")
    public ModelAndView resetPassword(@RequestParam(value = "redirect_uri", required = false) String redirectUri) {
        URI url = authenticationService.resetPassword(redirectUri);
        return new ModelAndView("redirect:" + url.toString());
    }

    public Optional<String> parseEmailAddressFromAccessToken(String accessToken) throws ParseException {
        AuthenticationConfigurationPropertiesStrategy configStrategy = locator.locateAuthenticationConfiguration();
        SignedJWT jwt = SignedJWT.parse(accessToken);
        final String emailAddresses = jwt.getJWTClaimsSet()
            .getStringClaim(configStrategy.getConfiguration().getClaims());
        if (emailAddresses == null || emailAddresses.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(emailAddresses);
    }

}
