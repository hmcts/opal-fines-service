package uk.gov.hmcts.opal.authentication.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authorisation.aspect.AuthorizationAspectService;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.UserStateService;

import java.util.function.Supplier;

import static uk.gov.hmcts.opal.authorisation.aspect.AuthorizationAspectService.AUTHORIZATION;

@Component
@Slf4j(topic = "UserStateAspectService")
@RequiredArgsConstructor
public class UserStateAspectService {

    private final UserStateService userStateService;
    private final AuthorizationAspectService authorizationAspectService;

    /**
     * This will infer the UserState from the arguments of the annotated method if present.
     * If no argument of USerState then it will fetch based on the bearer token of the current user.
     *
     * @param joinPoint ProceedingJoinPoint
     * @return UserState object for the user
     */
    public UserState getUserState(ProceedingJoinPoint joinPoint) {
        log.debug(":getUserState:");
        return authorizationAspectService.getUserState(joinPoint.getArgs())
            .orElseGet(getUserStateSupplier(joinPoint));
    }

    public Supplier<UserState> getUserStateSupplier(ProceedingJoinPoint joinPoint) {
        return () -> {
            log.debug(":getUserStateSupplier:");
            String authHeaderValue = authorizationAspectService.getAccessTokenParam(joinPoint).orElse(null);
            String bearerToken = authorizationAspectService.getAuthorization(authHeaderValue)
                .orElseThrow(() -> new MissingRequestHeaderException(AUTHORIZATION));
            return userStateService.checkForAuthorisedUser(bearerToken);
        };
    }

}
