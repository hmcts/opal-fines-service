package uk.gov.hmcts.opal.authentication.service;

import java.net.URI;

public interface AuthenticationService {

    URI loginOrRefresh(String accessToken, String redirectUri);

    String handleOauthCode(String code);

    URI logout(String accessToken, String redirectUri);

    URI resetPassword(String redirectUri);

}
