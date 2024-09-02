package uk.gov.hmcts.opal.controllers.advice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.PropertyValueException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import uk.gov.hmcts.opal.authentication.exception.AuthenticationError;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.exception.OpalApiException;
import uk.gov.hmcts.opal.launchdarkly.FeatureDisabledException;

import java.util.Map;

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

    @Test
    void handlePropertyValueException() {
        // Arrange
        PropertyValueException pve = new PropertyValueException("A Test Message", "DraftAccountEntity", "account");
        // Act
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handlePropertyValueException(pve);
        // Assert
        assertEquals(org.htmlunit.http.HttpStatus.IM_A_TEAPOT_418, response.getStatusCode().value());
    }

    @Test
    void handleHttpMessageNotReadableException() {
        // Arrange
        HttpInputMessage input = Mockito.mock(HttpInputMessage.class);
        HttpMessageNotReadableException hmnre = new HttpMessageNotReadableException("A Test Message", input);
        // Act
        ResponseEntity<Map<String, String>> response = globalExceptionHandler
            .handleHttpMessageNotReadableException(hmnre);
        // Assert
        assertEquals(org.htmlunit.http.HttpStatus.IM_A_TEAPOT_418, response.getStatusCode().value());
    }

    @Test
    void handleInvalidDataAccessApiUsageException() {
        // Arrange
        InvalidDataAccessApiUsageException idaaue = new InvalidDataAccessApiUsageException("A Test Message");
        // Act
        ResponseEntity<Map<String, String>> response = globalExceptionHandler
            .handleInvalidDataAccessApiUsageException(idaaue);
        // Assert
        assertEquals(org.htmlunit.http.HttpStatus.IM_A_TEAPOT_418, response.getStatusCode().value());
    }

    @Test
    void handleInvalidDataAccessResourceUsageException() {
        // Arrange
        InvalidDataAccessResourceUsageException idarue = new InvalidDataAccessResourceUsageException("A Test Message");
        // Act
        ResponseEntity<Map<String, String>> response = globalExceptionHandler
            .handleInvalidDataAccessResourceUsageException(idarue);
        // Assert
        assertEquals(org.htmlunit.http.HttpStatus.IM_A_TEAPOT_418, response.getStatusCode().value());
    }

    @Test
    void handleEntityNotFoundException_ReturnsNotFound() {
        EntityNotFoundException ex = new EntityNotFoundException("Entity not found");
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleEntityNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Entity not found", response.getBody().get(GlobalExceptionHandler.ERROR_MESSAGE));
    }

    @Test
    void handleOpalApiException_ReturnsInternalServerError() {
        OpalApiException ex = new OpalApiException(AuthenticationError.FAILED_TO_PARSE_ACCESS_TOKEN,
                                                   "Internal Server Error");
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleOpalApiException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to parse access token. Internal Server Error", response.getBody().get("message"));
    }

    @Test
    void handleHttpMediaTypeNotAcceptableException_ReturnsNotAcceptable() {
        HttpMediaTypeNotAcceptableException ex = new HttpMediaTypeNotAcceptableException("Not acceptable");
        ResponseEntity<Map<String, String>> response = globalExceptionHandler
            .handleHttpMediaTypeNotAcceptableException(ex);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
    }
}
