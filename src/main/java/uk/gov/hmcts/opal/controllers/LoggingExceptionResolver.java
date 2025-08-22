package uk.gov.hmcts.opal.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.PropertyValueException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "opal.LoggingExceptionResolver")
@Order(Ordered.HIGHEST_PRECEDENCE) // <--- IMPORTANT
public class LoggingExceptionResolver implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception ex) {
        try {
            // --- 401 Unauthorized (no/invalid Authorization header) ---
            if (ex instanceof AuthenticationCredentialsNotFoundException) {
                log.warn(":resolveException: AuthenticationCredentialsNotFoundException: '{}'", ex.getMessage());
                writeProblem401(request, response, ex.getMessage());
                return new ModelAndView(); // <--- tell Spring it's handled
            }

            // --- existing logging for other exceptions; let global/default handlers deal with them ---
            if (ex instanceof DataIntegrityViolationException dive) {
                log.warn(":resolveException: DataIntegrityViolationException: ", dive.getCause());
            } else if (ex instanceof PropertyValueException pve) {
                log.warn(":resolveException: for Entity '{}', value '{}': {}",
                    pve.getEntityName(), pve.getPropertyName(), pve.getMessage());
            } else {
                log.warn(":resolveException: {}: '{}'", ex.getClass().getSimpleName(), ex.getMessage());
            }
            return null;

        } catch (Exception writeErr) {
            log.error("Failed to write error response", writeErr);
            return null;
        }
    }

    private void writeProblem401(HttpServletRequest request, HttpServletResponse response, String detail)
        throws java.io.IOException {
        if (response.isCommitted()) {
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");

        String body = """
        {
          "type":"about:blank",
          "title":"Unauthorized",
          "status":401,
          "detail":"%s",
          "instance":"%s"
        }
            """.formatted(escapeJson(detail), escapeJson(request.getRequestURI()));

        response.getWriter().write(body);
        response.getWriter().flush();
    }

    private String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }

}
