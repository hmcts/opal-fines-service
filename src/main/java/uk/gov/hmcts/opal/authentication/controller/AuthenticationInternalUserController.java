package uk.gov.hmcts.opal.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.hmcts.opal.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authentication.service.AuthenticationService;

import java.net.URI;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/internal-user")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller - Internal Users")
public class AuthenticationInternalUserController {

    private final AuthenticationService authenticationService;
    private final AccessTokenService accessTokenService;

    @GetMapping("/login-or-refresh")
    @Operation(summary = "Handles login for the front end API calls")
    public ModelAndView loginOrRefresh(
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue,
        @RequestParam(value = "redirect_uri", required = false) String redirectUri
    ) {
        if (authHeaderValue != null) {
            log.debug("Login attempt received with token {}", authHeaderValue);
            String accessToken = authHeaderValue.replace("Bearer ", "");
            URI url = authenticationService.loginOrRefresh(accessToken, redirectUri);
            return new ModelAndView("redirect:" + url.toString());
        } else {
            log.debug("Login attempt received without any token.");
            URI loginUri = authenticationService.getLoginUri(redirectUri);
            return new ModelAndView("redirect:" + loginUri.toString());
        }
    }

    @PostMapping("/handle-oauth-code")
    @Operation(summary = "Handles Oauth code for the front end API calls")
    public SecurityToken handleOauthCode(@RequestParam("code") String code) {

        String accessToken = authenticationService.handleOauthCode(code);
        Optional<String> preferredUsernameOptional = Optional.ofNullable(
            accessTokenService.extractPreferredUsername(accessToken));
        log.debug(
            "Login successful received for user {} from Azure AD.",
            preferredUsernameOptional.orElse("unknown")
        );
        return authenticationService.getSecurityToken(accessToken);
    }

    @GetMapping("/logout")
    @Operation(summary = "Handles Logout for the front end API calls")
    public ModelAndView logout(
        @RequestHeader("Authorization") String authHeaderValue,
        @RequestParam(value = "redirect_uri", required = false) String redirectUri
    ) {
        String accessToken = authHeaderValue.replace("Bearer ", "");
        Optional<String> preferredUsernameOptional = Optional.ofNullable(
            accessTokenService.extractPreferredUsername(accessToken));
        log.debug(
            "Logout successful received for user {}",
            preferredUsernameOptional.orElse("unknown")
        );
        URI url = authenticationService.logout(accessToken, redirectUri);
        return new ModelAndView("redirect:" + url.toString());
    }

    @GetMapping("/reset-password")
    @Operation(summary = "Handles password reset for the front end API calls")
    public ModelAndView resetPassword(@RequestParam(value = "redirect_uri", required = false) String redirectUri) {
        URI url = authenticationService.resetPassword(redirectUri);
        return new ModelAndView("redirect:" + url.toString());
    }

}
