package uk.gov.hmcts.opal.authorisation.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.authentication.aspect.UserStateAspectService;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUserPermissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;

import static uk.gov.hmcts.opal.util.PermissionUtil.checkAnyRoleHasPermission;
import static uk.gov.hmcts.opal.util.PermissionUtil.checkRoleHasPermission;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizationAspect {

    private final UserStateAspectService userStateAspectService;
    private final AuthorizationAspectService authorizationAspectService;

    @Around("execution(* *(*)) && @annotation(authorizedAnyRoleHasPermission)")
    public Object checkAuthorization(ProceedingJoinPoint joinPoint,
                                     AuthorizedAnyRoleHasPermission authorizedAnyRoleHasPermission
    ) throws Throwable {

        UserState userState = userStateAspectService.getUserState(joinPoint);
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
        UserState userState = userStateAspectService.getUserState(joinPoint);

        BusinessUnitUserPermissions role = authorizationAspectService.getRole(args, userState);
        if (checkRoleHasPermission(role, authorizedRoleHasPermission.value())) {
            return joinPoint.proceed();
        }
        throw new PermissionNotAllowedException(authorizedRoleHasPermission.value(), role);
    }
}
