package uk.gov.hmcts.opal.authorisation.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.opal.authentication.aspect.AccessTokenParam;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteDto;
import uk.gov.hmcts.opal.dto.NoteDto;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.hmcts.opal.util.PermissionUtil.getRequiredBusinessUnitUser;

@Slf4j
@Component
public class AuthorizationAspectService {

    public static final String AUTHORIZATION = "Authorization";

    public Optional<String> getAccessTokenParam(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Parameter[] parameters = signature.getMethod().getParameters();

            return Arrays.stream(parameters)
                .filter(param -> Arrays.stream(param.getAnnotations()).anyMatch(anno -> anno.annotationType().equals(
                    AccessTokenParam.class)))
                .map(param -> args[Arrays.asList(parameters).indexOf(param)])
                .filter(arg -> arg instanceof String)
                .map(String.class::cast)
                .findFirst();
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
        return Optional.empty();
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

    public BusinessUnitUser getBusinessUnitUser(Object[] args, UserState userState) {
        for (Object arg : args) {
            if (arg instanceof BusinessUnitUser) {
                return (BusinessUnitUser) arg;
            } else if (arg instanceof AddNoteDto addNoteDto) {
                return getRequiredBusinessUnitUser(userState, addNoteDto.getBusinessUnitId());
            } else if (arg instanceof NoteDto noteDto) {
                return getRequiredBusinessUnitUser(userState, noteDto.getBusinessUnitId());
            }
        }
        throw new BusinessUnitUserNotFoundException(format(
            "Can't infer the role for user %s. "
                + "Annotated method needs to have arguments of types"
                + " (BusinessUnitUser, AddNoteDto, NoteDto).",
            userState.getUserName()
        ));
    }

    public Optional<UserState> getUserState(Object[] args) {
        return getArgument(args, UserState.class);
    }

    public <T> Optional<T> getArgument(Object[] args, Class<T> clazz) {
        return Arrays.stream(args)
            .filter(clazz::isInstance)
            .map(clazz::cast)
            .findFirst();
    }
}
