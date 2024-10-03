package uk.gov.hmcts.opal.authorisation.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.authentication.aspect.UserStateAspectService;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUserPermissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;

import static uk.gov.hmcts.opal.util.PermissionUtil.checkAnyBusinessUnitUserHasPermission;
import static uk.gov.hmcts.opal.util.PermissionUtil.checkBusinessUnitUserHasPermission;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizationAspect {

    private final UserStateAspectService userStateAspectService;
    private final AuthorizationAspectService authorizationAspectService;

    @Around("execution(* *(*)) && @annotation(authorizedAnyBusinessUnitUserHasPermission)")
    public Object checkAuthorization(ProceedingJoinPoint joinPoint,
                            AuthorizedAnyBusinessUnitUserHasPermission authorizedAnyBusinessUnitUserHasPermission
    ) throws Throwable {

        UserState userState = userStateAspectService.getUserState(joinPoint);
        if (checkAnyBusinessUnitUserHasPermission(userState, authorizedAnyBusinessUnitUserHasPermission.value())) {
            return joinPoint.proceed();
        }
        throw new PermissionNotAllowedException(authorizedAnyBusinessUnitUserHasPermission.value());
    }

    @Around("execution(* *(*)) && @annotation(authorizedBusinessUnitUserHasPermission)")
    public Object checkAuthorization(ProceedingJoinPoint joinPoint,
                                     AuthorizedBusinessUnitUserHasPermission authorizedBusinessUnitUserHasPermission
    ) throws Throwable {

        Object[] args = joinPoint.getArgs();
        UserState userState = userStateAspectService.getUserState(joinPoint);

        BusinessUnitUserPermissions businessUnitUserPermissions = authorizationAspectService
            .getBusinessUnitUserPermissions(args, userState);
        if (checkBusinessUnitUserHasPermission(businessUnitUserPermissions,
                                               authorizedBusinessUnitUserHasPermission.value())) {
            return joinPoint.proceed();
        }
        throw new PermissionNotAllowedException(authorizedBusinessUnitUserHasPermission.value(),
                                                businessUnitUserPermissions);
    }
}
