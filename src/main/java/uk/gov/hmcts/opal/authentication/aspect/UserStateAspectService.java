package uk.gov.hmcts.opal.authentication.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authorisation.aspect.AuthorizationAspectService;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.function.Supplier;

import static uk.gov.hmcts.opal.authorisation.aspect.AuthorizationAspectService.AUTHORIZATION;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserStateAspectService {

    private final UserStateService userStateService;
    private final AuthorizationAspectService authorizationAspectService;

    /**
     * This will infer the UserState from the arguments of the annotated method if present.
     * If no argument of USerState then it will fetch based on the bearer token of the current user.
     *
     * @param args arguments of the annotated method
     * @return UserState object for the user
     */
    public UserState getUserState(Object[] args) {
        return authorizationAspectService.getUserState(args)
            .orElseGet(getUserStateSupplier(args));
    }

    public Supplier<UserState> getUserStateSupplier(Object[] args) {
        return () -> {
            String authHeaderValue = authorizationAspectService.getRequestHeaderValue(args);
            String bearerToken = authorizationAspectService.getAuthorization(authHeaderValue)
                .orElseThrow(() -> new MissingRequestHeaderException(AUTHORIZATION));
            return userStateService.getUserStateUsingAuthToken(bearerToken);
        };
    }

}
