package uk.gov.hmcts.opal.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;

import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class LoggingExceptionResolverTest {

    @Mock
    private AccessTokenService tokenService;

    @InjectMocks
    private LoggingExceptionResolver loggingExceptionResolver;

    @Test
    void testGetTill_Success() {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        AccessDeniedException ex = new AccessDeniedException("Not Allowed");

        // Act
        ModelAndView modelAndView = loggingExceptionResolver.resolveException(request, response, null, ex);

        // Assert
        assertNull(modelAndView);
    }
}
