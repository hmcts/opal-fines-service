package uk.gov.hmcts.opal.controllers.advice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.QueryTimeoutException;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.PropertyValueException;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import uk.gov.hmcts.opal.authentication.exception.AuthenticationError;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.exception.OpalApiException;
import uk.gov.hmcts.opal.launchdarkly.FeatureDisabledException;

import java.net.ConnectException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void testHandleFeatureDisabledException() {
        FeatureDisabledException exception = new FeatureDisabledException("Feature is disabled");
        ResponseEntity<String> response = globalExceptionHandler.handleFeatureDisabledException(exception);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("Feature is disabled", response.getBody());
    }

    @Test
    void testHandleMissingRequestHeaderException() {
        MissingRequestHeaderException exception = new MissingRequestHeaderException("TYPE");
        ResponseEntity<String> response = globalExceptionHandler.handleMissingRequestHeaderException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing request header named: TYPE", response.getBody());
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
    void testHandleHttpMediaTypeNotAcceptableException() {
        HttpMediaTypeNotAcceptableException exception = new HttpMediaTypeNotAcceptableException("Not acceptable");
        ResponseEntity<Map<String, String>> response = globalExceptionHandler
            .handleHttpMediaTypeNotAcceptableException(exception);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        assertEquals("Not Acceptable", response.getBody().get("error"));
        assertEquals("Not acceptable, Could not parse Accept header.",
                     response.getBody().get("message"));
    }

    @Test
    void testHandlePropertyValueException() {
        PropertyValueException exception = new PropertyValueException("Property value exception", "entity",
                                                                      "property");
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handlePropertyValueException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Property value exception : entity.property", response.getBody().get("error"));
    }

    @Test
    void testHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Cannot read message",
                                                                                        new Throwable("Root cause"));
        ResponseEntity<Map<String, String>> response = globalExceptionHandler
            .handleHttpMessageNotReadableException(exception);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
        assertEquals("Cannot read message", response.getBody().get("error"));
        assertEquals("The request body could not be read, ensure content-type is application/json",
                     response.getBody().get("message"));
    }

    @Test
    void testHandleInvalidDataAccessApiUsageException() {
        InvalidDataAccessApiUsageException exception =
            new InvalidDataAccessApiUsageException("Invalid API usage", new Throwable("Root cause"));
        ResponseEntity<Map<String, String>> response = globalExceptionHandler
            .handleInvalidDataAccessApiUsageException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Invalid API usage", response.getBody().get("error"));
    }

    @Test
    void handleInvalidDataAccessResourceUsageException_ShouldReturnInternalServerError() {
        InvalidDataAccessResourceUsageException exception = new InvalidDataAccessResourceUsageException("Invalid resource usage");
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleInvalidDataAccessResourceUsageException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Invalid resource usage", response.getBody().get("error"));
    }

    @Test
    void testHandleEntityNotFoundException() {
        EntityNotFoundException exception = new EntityNotFoundException("Entity not found");
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleEntityNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Entity Not Found", response.getBody().get("error"));
    }

    @Test
    void handleOpalApiException_ReturnsInternalServerError() {
        OpalApiException ex = new OpalApiException(
            AuthenticationError.FAILED_TO_OBTAIN_AUTHENTICATION_CONFIG);
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleOpalApiException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to find authentication configuration", response.getBody().get("message"));
    }

    @Test
    void testHandleDatabaseExceptions_queryTimeout() {
        QueryTimeoutException exception = new QueryTimeoutException("Query timeout", null, null);
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleDatabaseExceptions(exception);

        assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());
        assertEquals("Request Timeout", response.getBody().get("error"));
        assertEquals("The request did not receive a response from the database within the timeout period",
                     response.getBody().get("message"));
    }

    @Test
    void handleDatabaseExceptions_OtherDatabaseException_ShouldReturnInternalServerError() {
        PersistenceException exception = new PersistenceException("Persistence exception");
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleDatabaseExceptions(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().get("error"));
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
    }

    @Test
    void testHandlePsqlException_serviceUnavailable() {
        PSQLException exception = new PSQLException("PSQL Exception",
                                                    PSQLState.CONNECTION_FAILURE,
                                                    new ConnectException("Connection refused"));
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handlePsqlException(exception);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Service Unavailable", response.getBody().get("error"));
        assertEquals("Opal Fines Database is currently unavailable", response.getBody().get("message"));
    }

    @Test
    void handlePsqlException_WithOtherCause_ShouldReturnInternalServerError() {
        PSQLException exception = new PSQLException("PSQL Exception", PSQLState.UNEXPECTED_ERROR, new Throwable("Unexpected error"));
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handlePsqlException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().get("error"));
        assertEquals("PSQL Exception", response.getBody().get("message"));
    }

    @Test
    void testHandleDataAccessResourceFailureException() {
        DataAccessResourceFailureException exception =
            new DataAccessResourceFailureException("Data access resource failure");
        ResponseEntity<Map<String, String>> response = globalExceptionHandler
            .handleDataAccessResourceFailureException(exception);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Service Unavailable", response.getBody().get("error"));
        assertEquals("Opal Fines Database is currently unavailable", response.getBody().get("message"));
    }
}
