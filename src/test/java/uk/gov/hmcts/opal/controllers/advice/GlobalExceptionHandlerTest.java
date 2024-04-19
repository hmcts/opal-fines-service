package uk.gov.hmcts.opal.controllers.advice;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.launchdarkly.FeatureDisabledException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Mock
    FeatureDisabledException exception;

    @Mock
    MissingRequestHeaderException missingRequestHeaderException;

    @Mock
    PermissionNotAllowedException permissionNotAllowedException;

    @InjectMocks
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
    void handlePermissionNotAllowedException_ReturnsBadRequest() {
        // Arrange
        String errorMessage = "permission is not allowed for user";
        when(permissionNotAllowedException.getMessage()).thenReturn(errorMessage);

        // Act
        ResponseEntity<String> response = globalExceptionHandler.handlePermissionNotAllowedException(
            permissionNotAllowedException);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }
}
