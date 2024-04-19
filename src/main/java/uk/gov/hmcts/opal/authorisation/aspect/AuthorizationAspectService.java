package uk.gov.hmcts.opal.authorisation.aspect;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component
public class AuthorizationAspectService {

    public static final String AUTHORIZATION = "Authorization";

    public String getRequestHeaderValue(Object[] args) {
        for (Object arg : args) {
            if (arg.getClass().isAnnotationPresent(org.springframework.web.bind.annotation.RequestHeader.class)
                && arg instanceof String) {
                return (String) arg;
            }
        }
        return null;
    }

    public Optional<String> getAuthorization(String authHeaderValue) {
        if (authHeaderValue != null) {
            return Optional.of(authHeaderValue);
        }
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (sra != null) {
            return Optional.ofNullable(sra.getRequest().getHeader(AUTHORIZATION));
        }
        return Optional.empty();
    }
}
