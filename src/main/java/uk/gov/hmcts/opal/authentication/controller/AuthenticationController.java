package uk.gov.hmcts.opal.authentication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.hmcts.opal.authentication.model.SecurityToken;

public interface AuthenticationController {

    @GetMapping("/login-or-refresh")
    ModelAndView loginOrRefresh(
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue,
        @RequestParam(value = "redirect_uri", required = false) String redirectUri
    );

    @PostMapping("/handle-oauth-code")
    SecurityToken handleOauthCode(@RequestParam("code") String code);

    @GetMapping("/logout")
    ModelAndView logout(
        @RequestHeader("Authorization") String authHeaderValue,
        @RequestParam(value = "redirect_uri", required = false) String redirectUri
    );

    @GetMapping("/reset-password")
    ModelAndView resetPassword(@RequestParam(value = "redirect_uri", required = false) String redirectUri);


}
