package uk.gov.hmcts.opal.authentication.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomAuthenticationExceptionsTest {

    private CustomAuthenticationExceptions customAuthenticationExceptions;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        customAuthenticationExceptions = new CustomAuthenticationExceptions();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void commenceShouldReturnUnauthorizedResponse() throws IOException, ServletException {
        AuthenticationException authException = mock(AuthenticationException.class);

        customAuthenticationExceptions.commence(request, response, authException);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(writer).write("{\"error\": \"Unauthorized\", \"message\": "
                                 + "\"Unauthorized: request could not be authorized\"}");
    }

    @Test
    void handleShouldReturnForbiddenResponse() throws IOException, ServletException {
        AccessDeniedException accessDeniedException = mock(AccessDeniedException.class);

        customAuthenticationExceptions.handle(request, response, accessDeniedException);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");
        verify(writer).write("{\"error\": \"Forbidden\", \"message\": "
                                 + "\"Forbidden: access is forbidden for this user\"}");
    }
}
