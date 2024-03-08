package uk.gov.hmcts.opal.authorisation.service;

import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.opal.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorisationService {

    private final UserService userService;

    public UserState getAuthorisation(String username) {
        return userService.getUserStateByUsername(username);
    }
}
