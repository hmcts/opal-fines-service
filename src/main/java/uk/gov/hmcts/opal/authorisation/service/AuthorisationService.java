package uk.gov.hmcts.opal.authorisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.opal.UserService;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j(topic = "AuthorisationService")
public class AuthorisationService {

    private final UserService userService;
    private final AccessTokenService accessTokenService;

    public UserState getAuthorisation(String username) {
        return userService.getUserStateByUsername(username);
    }

    public SecurityToken getSecurityToken(String accessToken) {
        SecurityToken.SecurityTokenBuilder securityTokenBuilder = SecurityToken.builder()
            .accessToken(accessToken);
        Optional<String> preferredUsernameOptional = Optional.ofNullable(
            accessTokenService.extractPreferredUsername(accessToken));
        log.info(":getSecurityToken: preferred user name: {}", preferredUsernameOptional);

        if (preferredUsernameOptional.isPresent()) {
            UserState userStateOptional = this.getAuthorisation(preferredUsernameOptional.get());
            securityTokenBuilder.userState(userStateOptional);
        }
        return securityTokenBuilder.build();
    }
}
