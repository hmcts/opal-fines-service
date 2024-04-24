package uk.gov.hmcts.opal.authorisation.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import static uk.gov.hmcts.opal.authorisation.aspect.AuthorizationAspectService.AUTHORIZATION;
import static uk.gov.hmcts.opal.util.PermissionUtil.checkAnyRoleHasPermission;

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
        String authHeaderValue = authorizationAspectService.getRequestHeaderValue(args);
        String bearerToken = authorizationAspectService.getAuthorization(authHeaderValue)
            .orElseThrow(() -> new MissingRequestHeaderException(AUTHORIZATION));
        UserState userState = userStateService.getUserStateUsingAuthToken(bearerToken);
        if (checkAnyRoleHasPermission(userState, authorizedAnyRoleHasPermission.value())) {
            return joinPoint.proceed();
        }
        throw new PermissionNotAllowedException(authorizedAnyRoleHasPermission.value());
    }
}

