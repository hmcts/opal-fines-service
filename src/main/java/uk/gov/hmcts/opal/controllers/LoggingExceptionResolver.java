package uk.gov.hmcts.opal.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;

import static uk.gov.hmcts.opal.authentication.service.AccessTokenService.AUTH_HEADER;
import static uk.gov.hmcts.opal.util.HttpUtil.extractPreferredUsername;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "LoggingExceptionResolver")
public class LoggingExceptionResolver implements HandlerExceptionResolver {

    private final AccessTokenService tokenService;

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception exception) {
        if (exception instanceof AccessDeniedException ade) {
            String authorization = request.getHeader(AUTH_HEADER);
            String preferredName = extractPreferredUsername(authorization, tokenService);
            log.warn(":resolveException: For user '{}', {}", preferredName, ade.getMessage());
        }
        return null;
    }
}
