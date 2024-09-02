package uk.gov.hmcts.opal.authentication.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class CustomAuthenticationExceptions implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {


        // Set the response status code
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Set the content type of the response
        response.setContentType("application/json");

        // Write the custom message to the response body
        try (PrintWriter writer = response.getWriter()) {
            writer.write("{\"error\": \"Unauthorized\", \"message\":"
                             + " \"Unauthorized: request could not be authorized\"}");
        }
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // Set the response status code
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // Set the content type of the response
        response.setContentType("application/json");

        // Write the custom message to the response body
        try (PrintWriter writer = response.getWriter()) {
            writer.write("{\"error\": \"Forbidden\", \"message\": "
                             + "\"Forbidden: access is forbidden for this user\"}");
        }
    }
}

