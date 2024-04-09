package uk.gov.hmcts.opal.authorisation.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
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

    @Before("@annotation(uk.gov.hmcts.opal.authorisation.aspect.AuthorizedPermission) ")
    public void checkAuthorization(String authHeaderValue, AuthorizedPermission authorizedPermission) {
        String bearerToken = getAuthorization(authHeaderValue)
            .orElseThrow(() -> new MissingRequestHeaderException(AUTHORIZATION));
        UserState userState = userStateService.getUserStateUsingServletRequest(bearerToken);
        checkPermission(userState, authorizedPermission.value());
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

    private void checkPermission(UserState userState, Permissions permission) {
        checkAnyRoleHasPermission(userState, permission);
    }
}

