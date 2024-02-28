package uk.gov.hmcts.opal.authorisation.service;

import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.UserState;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorisationService {


    public Optional<UserState> getAuthorisation(String emailAddress) {
        //TODO: populate user state from the database.
        return Optional.of(UserState.builder()
                               .userId(emailAddress)
                               .userName("some name")
                               .build());
    }
}
