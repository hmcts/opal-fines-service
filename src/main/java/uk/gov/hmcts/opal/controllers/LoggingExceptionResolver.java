package uk.gov.hmcts.opal.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.PropertyValueException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "opal.LoggingExceptionResolver")
public class LoggingExceptionResolver implements HandlerExceptionResolver {

    private final AccessTokenService tokenService;

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception ex) {
        if (ex instanceof DataIntegrityViolationException dive) {
            log.warn(":resolveException: DataIntegrityViolationException: ", dive.getCause());
        } else if (ex instanceof PropertyValueException pve) {
            log.warn(":resolveException: for Entity '{}', value '{}': {}",
                     pve.getEntityName(), pve.getPropertyName(), pve.getMessage());
        } else {
            log.warn(":resolveException: {}: '{}'", ex.getClass().getSimpleName(), ex.getMessage());
        }
        return null;
    }
}
