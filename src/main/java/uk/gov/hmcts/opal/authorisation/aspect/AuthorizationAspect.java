package uk.gov.hmcts.opal.authorisation.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.Optional;

import static uk.gov.hmcts.opal.util.PermissionUtil.checkAnyRoleHasPermission;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizationAspect {

    public static final String AUTHORIZATION = "Authorization";
    private final UserStateService userStateService;

    @Around("execution(* *(*)) && @annotation(authorizedPermission)")
    public Object checkAuthorization(ProceedingJoinPoint joinPoint,
                                     AuthorizedPermission authorizedPermission) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String authHeaderValue = getRequestHeaderValue(args);
        return check(joinPoint, authorizedPermission, authHeaderValue);
    }

    private String getRequestHeaderValue(Object[] args) {
        for (Object arg : args) {
            if (arg.getClass().isAnnotationPresent(org.springframework.web.bind.annotation.RequestHeader.class)
                && arg instanceof String) {
                return (String) arg;
            }
        }
        return null;
    }

    private Object check(ProceedingJoinPoint joinPoint,
                         AuthorizedPermission authorizedPermission,
                         String authHeaderValue) throws Throwable {
        String bearerToken = getAuthorization(authHeaderValue)
            .orElseThrow(() -> new MissingRequestHeaderException(AUTHORIZATION));
        UserState userState = userStateService.getUserStateUsingAuthToken(bearerToken);
        if (checkPermission(userState, authorizedPermission.value())) {
            return joinPoint.proceed();
        }
        return null;
    }

    private Optional<String> getAuthorization(String authHeaderValue) {
        if (authHeaderValue != null) {
            return Optional.of(authHeaderValue);
        }
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (sra != null) {
            return Optional.ofNullable(sra.getRequest().getHeader(AUTHORIZATION));
        }
        return Optional.empty();
    }

    private boolean checkPermission(UserState userState, Permissions permission) {
        return checkAnyRoleHasPermission(userState, permission);
    }
}

