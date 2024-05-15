package uk.gov.hmcts.opal.controllers.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.launchdarkly.FeatureDisabledException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @MockBean
    FeatureDisabledException exception;

    @MockBean
    MissingRequestHeaderException missingRequestHeaderException;

    @MockBean
    PermissionNotAllowedException permissionNotAllowedException;

    @MockBean
    AccessTokenService tokenService;

    @Autowired
    GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleFeatureDisabledException_ReturnsMethodNotAllowed() {
        // Arrange
        String errorMessage = "Feature is disabled";
        when(exception.getMessage()).thenReturn(errorMessage);

        // Act
        ResponseEntity<String> response = globalExceptionHandler.handleFeatureDisabledException(exception);

        // Assert
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void handleMissingRequestHeaderException_ReturnsBadRequest() {
        // Arrange
        String errorMessage = "Missing required header";
        when(missingRequestHeaderException.getMessage()).thenReturn(errorMessage);

        // Act
        ResponseEntity<String> response = globalExceptionHandler.handleMissingRequestHeaderException(
            missingRequestHeaderException);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void handlePermissionNotAllowedException_ShouldReturnForbiddenResponse() {
        // Arrange
        PermissionNotAllowedException ex = new PermissionNotAllowedException(Permissions.ACCOUNT_ENQUIRY);
        HttpServletRequest request = new MockHttpServletRequest();

        // Act
        ResponseEntity<String> response = globalExceptionHandler.handlePermissionNotAllowedException(ex, request);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleAccessDeniedException_ShouldReturnForbiddenResponse() {
        // Arrange
        AccessDeniedException ex = new AccessDeniedException("access denied");
        HttpServletRequest request = new MockHttpServletRequest();
        // Act
        ResponseEntity<String> response = globalExceptionHandler.handlePermissionNotAllowedException(ex, request);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
