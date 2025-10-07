package uk.gov.hmcts.opal.authorisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.UserStateService;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j(topic = "AuthorisationService")
public class AuthorisationService {

    private final UserStateService userStateService;
    private final AccessTokenService accessTokenService;

    public SecurityToken getSecurityToken(String accessToken) {
        SecurityToken.SecurityTokenBuilder securityTokenBuilder = SecurityToken.builder()
            .accessToken(accessToken);
        Optional<String> preferredUsernameOptional = Optional.ofNullable(
            accessTokenService.extractPreferredUsername(accessToken));
        log.debug(":getSecurityToken: preferred user name: {}", preferredUsernameOptional);

        if (preferredUsernameOptional.isPresent()) {
            UserState userStateOptional = userStateService.checkForAuthorisedUser(accessToken);
            securityTokenBuilder.userState(userStateOptional);
        }
        return securityTokenBuilder.build();
    }
}
