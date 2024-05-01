package uk.gov.hmcts.opal.authorisation.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authorisation.model.Role;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.function.Supplier;

import static uk.gov.hmcts.opal.authorisation.aspect.AuthorizationAspectService.AUTHORIZATION;
import static uk.gov.hmcts.opal.util.PermissionUtil.checkAnyRoleHasPermission;
import static uk.gov.hmcts.opal.util.PermissionUtil.checkRoleHasPermission;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizationAspect {

    private final UserStateService userStateService;
    private final AuthorizationAspectService authorizationAspectService;

    @Around("execution(* *(*)) && @annotation(authorizedAnyRoleHasPermission)")
    public Object checkAuthorization(ProceedingJoinPoint joinPoint,
                                     AuthorizedAnyRoleHasPermission authorizedAnyRoleHasPermission
    ) throws Throwable {

        Object[] args = joinPoint.getArgs();
        UserState userState = getUserState(args);
        if (checkAnyRoleHasPermission(userState, authorizedAnyRoleHasPermission.value())) {
            return joinPoint.proceed();
        }
        throw new PermissionNotAllowedException(authorizedAnyRoleHasPermission.value());
    }

    @Around("execution(* *(*)) && @annotation(authorizedRoleHasPermission)")
    public Object checkAuthorization(ProceedingJoinPoint joinPoint,
                                     AuthorizedRoleHasPermission authorizedRoleHasPermission
    ) throws Throwable {

        Object[] args = joinPoint.getArgs();
        UserState userState = getUserState(args);

        Role role = authorizationAspectService.getRole(args, userState);
        if (checkRoleHasPermission(role, authorizedRoleHasPermission.value())) {
            return joinPoint.proceed();
        }
        throw new PermissionNotAllowedException(authorizedRoleHasPermission.value(), role);
    }

    /**
     * This will infer the UserState from the arguments of the annotated method if present.
     * If no argument of USerState then it will fetch based on the bearer token of the current user.
     *
     * @param args arguments of the annotated method
     * @return UserState object for the user
     */
    private UserState getUserState(Object[] args) {
        return authorizationAspectService.getUserState(args)
            .orElseGet(getUserStateSupplier(args));
    }

    private Supplier<UserState> getUserStateSupplier(Object[] args) {
        return () -> {
            String authHeaderValue = authorizationAspectService.getRequestHeaderValue(args);
            String bearerToken = authorizationAspectService.getAuthorization(authHeaderValue)
                .orElseThrow(() -> new MissingRequestHeaderException(AUTHORIZATION));
            return userStateService.getUserStateUsingAuthToken(bearerToken);
        };
    }
}

