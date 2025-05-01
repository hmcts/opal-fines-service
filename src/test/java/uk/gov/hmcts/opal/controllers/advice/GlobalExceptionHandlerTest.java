package uk.gov.hmcts.opal.controllers.advice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.QueryTimeoutException;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.LazyInitializationException;
import org.hibernate.PropertyValueException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uk.gov.hmcts.opal.authentication.exception.AuthenticationError;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.exception.OpalApiException;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.launchdarkly.FeatureDisabledException;

import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.URI;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleFeatureDisabledException(exception);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), problemDetail.getStatus());
        assertEquals("Feature Disabled", problemDetail.getTitle());
        assertEquals("Feature is disabled", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/feature-disabled"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandleMissingRequestHeaderException() {
        MissingRequestHeaderException exception = new MissingRequestHeaderException("TYPE");
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleMissingRequestHeaderException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Missing Required Header", problemDetail.getTitle());
        assertEquals("Missing request header named: TYPE", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/missing-header"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void handlePermissionNotAllowedException_ShouldReturnForbiddenResponse() {
        // Arrange
        PermissionNotAllowedException ex = new PermissionNotAllowedException(Permissions.ACCOUNT_ENQUIRY);
        HttpServletRequest request = new MockHttpServletRequest();

        // Act
        ResponseEntity<ProblemDetail> response =
            globalExceptionHandler.handlePermissionNotAllowedException(ex, request);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.FORBIDDEN.value(), problemDetail.getStatus());
        assertEquals("Forbidden", problemDetail.getTitle());
        assertTrue(problemDetail.getDetail().contains("permission(s) are not enabled for the user"));
        assertEquals(URI.create("https://hmcts.gov.uk/problems/forbidden"), problemDetail.getType());

        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());
    }

    @Test
    void testHandleHttpMediaTypeNotAcceptableException() {
        HttpMediaTypeNotAcceptableException exception = new HttpMediaTypeNotAcceptableException("Not acceptable");
        ResponseEntity<ProblemDetail> response = globalExceptionHandler
            .handleHttpMediaTypeNotAcceptableException(exception);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), problemDetail.getStatus());
        assertEquals("Not Acceptable", problemDetail.getTitle());
        assertEquals("Not acceptable, Could not parse Accept header.", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/not-acceptable"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandleMethodArgumentTypeMismatchException() throws NoSuchMethodException {
        // Simulate a value that caused the type mismatch
        Object invalidValue = "invalidInt";
        Class<?> requiredType = Integer.class;
        String parameterName = "testParam";
        Throwable cause = new NumberFormatException("For input string: \"invalidInt\"");
        Method method = GlobalExceptionHandlerTest.class.getMethod("sampleMethod", Integer.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);

        // Initialize MethodArgumentTypeMismatchException
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
            invalidValue,
            requiredType,
            parameterName,
            methodParameter,
            cause
        );

        ResponseEntity<ProblemDetail> response = globalExceptionHandler
            .handleMethodArgumentTypeMismatchException(exception);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), problemDetail.getStatus());
        assertEquals("Not Acceptable", problemDetail.getTitle());
        assertTrue(problemDetail.getDetail().contains("Failed to convert value of type"));
        assertEquals(URI.create("https://hmcts.gov.uk/problems/type-mismatch"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandlePropertyValueException() {
        PropertyValueException exception = new PropertyValueException("Property value exception", "entity",
                                                                      "property");
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handlePropertyValueException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("Property Value Error", problemDetail.getTitle());
        assertTrue(problemDetail.getDetail().contains("Property value exception"));
        assertEquals(URI.create("https://hmcts.gov.uk/problems/property-value-error"), problemDetail.getType());
        assertEquals("entity", problemDetail.getProperties().get("entity"));
        assertEquals("property", problemDetail.getProperties().get("property"));

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandleHttpMediaTypeNotSupportedException() {
        HttpMediaTypeNotSupportedException exception =
            new HttpMediaTypeNotSupportedException("Unsupported media type");
        ResponseEntity<ProblemDetail> response = globalExceptionHandler
            .handleHttpMediaTypeNotSupportedException(exception);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), problemDetail.getStatus());
        assertEquals("Unsupported Media Type", problemDetail.getTitle());
        assertEquals("The Content-Type is not supported. Please use application/json", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/unsupported-media-type"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandleHttpMessageNotReadableException() {
        HttpInputMessage msg = Mockito.mock(HttpInputMessage.class);
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Cannot read message", msg);
        ResponseEntity<ProblemDetail> response = globalExceptionHandler
            .handleHttpMessageNotReadableException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Bad Request", problemDetail.getTitle());
        assertEquals("The request body could not be read. It may be missing or invalid JSON.",
                     problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/message-not-readable"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandleInvalidDataAccessApiUsageException() {
        InvalidDataAccessApiUsageException exception =
            new InvalidDataAccessApiUsageException("Invalid API usage", new Throwable("Root cause"));
        ResponseEntity<ProblemDetail> response = globalExceptionHandler
            .handleInvalidDataAccessApiUsageException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("Internal Server Error", problemDetail.getTitle());
        assertEquals("Invalid API usage", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/invalid-data-access"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void handleInvalidDataAccessResourceUsageException_ShouldReturnInternalServerError() {
        InvalidDataAccessResourceUsageException exception =
            new InvalidDataAccessResourceUsageException("Invalid resource usage");
        ResponseEntity<ProblemDetail> response = globalExceptionHandler
            .handleInvalidDataAccessResourceUsageException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("Internal Server Error", problemDetail.getTitle());
        assertEquals("Invalid resource usage", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/invalid-resource-usage"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandleEntityNotFoundException() {
        EntityNotFoundException exception = new EntityNotFoundException("Entity not found");
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleEntityNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Entity Not Found", problemDetail.getTitle());
        assertEquals("Entity not found", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/entity-not-found"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandleNoSuchElementException() {
        NoSuchElementException exception = new NoSuchElementException("No such element");
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleNoSuchElementException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("No Value Present", problemDetail.getTitle());
        assertEquals("No such element", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/no-such-element"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void handleOpalApiException_ReturnsInternalServerError() {
        OpalApiException ex = new OpalApiException(
            AuthenticationError.FAILED_TO_OBTAIN_AUTHENTICATION_CONFIG);
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleOpalApiException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("Internal Server Error", problemDetail.getTitle());
        assertEquals("Failed to find authentication configuration", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/opal-api-error"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandleServletExceptions_queryTimeout() {
        QueryTimeoutException exception = new QueryTimeoutException("Query timeout", null, null);
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleServletExceptions(exception);

        assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.REQUEST_TIMEOUT.value(), problemDetail.getStatus());
        assertEquals("Request Timeout", problemDetail.getTitle());
        assertEquals("The request did not receive a response from the database within the timeout period",
                     problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/query-timeout"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void handleServletExceptions_OtherDatabaseException_ShouldReturnInternalServerError() {
        PersistenceException exception = new PersistenceException("Persistence exception");
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleServletExceptions(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("Internal Server Error", problemDetail.getTitle());
        assertEquals("An unexpected error occurred. Persistence exception", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/servlet-error"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void handleServletExceptions_ResourceNotFound_ShouldReturnNotFoundError() {
        NoResourceFoundException exception = new NoResourceFoundException(HttpMethod.GET, "path");
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleServletExceptions(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Not Found", problemDetail.getTitle());
        assertEquals("No static resource path.", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/resource-not-found"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandlePsqlException_serviceUnavailable() {
        PSQLException exception = new PSQLException("PSQL Exception",
                                                    PSQLState.CONNECTION_FAILURE,
                                                    new ConnectException("Connection refused"));
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handlePsqlException(exception);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), problemDetail.getStatus());
        assertEquals("Service Unavailable", problemDetail.getTitle());
        assertEquals("Opal Fines Database is currently unavailable", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/database-unavailable"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void handlePsqlException_WithOtherCause_ShouldReturnInternalServerError() {
        PSQLException exception = new PSQLException("PSQL Exception", PSQLState.UNEXPECTED_ERROR,
                                                    new Throwable("Unexpected error"));
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handlePsqlException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("Internal Server Error", problemDetail.getTitle());
        assertEquals("PSQL Exception", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/database-error"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandleDataAccessResourceFailureException() {
        DataAccessResourceFailureException exception =
            new DataAccessResourceFailureException("Data access resource failure");
        ResponseEntity<ProblemDetail> response = globalExceptionHandler
            .handleDataAccessResourceFailureException(exception);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), problemDetail.getStatus());
        assertEquals("Service Unavailable", problemDetail.getTitle());
        assertEquals("Opal Fines Database is currently unavailable", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/database-unavailable"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandleLazyInitializationException() {
        LazyInitializationException exception =
            new LazyInitializationException("Could not access Lazy Loaded Entity");
        ResponseEntity<ProblemDetail> response = globalExceptionHandler
            .handleLazyInitializationException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("Internal Server Error", problemDetail.getTitle());
        assertEquals("Lazy Entity Initialisation Exception. Expired DB Session?", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/lazy-initialization"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandleJpaSystemException() {
        JpaSystemException jse = new JpaSystemException(new RuntimeException("Problem with JPA"));
        ResponseEntity<ProblemDetail> response = globalExceptionHandler
            .handleJpaSystemException(jse);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("Internal Server Error", problemDetail.getTitle());
        assertEquals("Unknown Entity Persistence Error. Expired DB Session?", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/jpa-system-error"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandleJsonSchemaValidationException() {
        JsonSchemaValidationException exception =
            new JsonSchemaValidationException("JSON Schema Validation failed");
        ResponseEntity<ProblemDetail> response = globalExceptionHandler
            .handleJsonSchemaValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Bad Request", problemDetail.getTitle());
        assertEquals("JSON Schema Validation Error: JSON Schema Validation failed", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/json-schema-validation"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception =
            new IllegalArgumentException("Cannot include both A and B parameters");
        ResponseEntity<ProblemDetail> response = globalExceptionHandler
            .handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Bad Request", problemDetail.getTitle());
        assertEquals("Cannot include both A and B parameters", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/illegal-argument"), problemDetail.getType());

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandleObjectOptimisticLockingFailureException() {
        ObjectOptimisticLockingFailureException e = new ObjectOptimisticLockingFailureException(
            DraftAccountEntity.class, "123");
        ResponseEntity<ProblemDetail> response =
            globalExceptionHandler.handleObjectOptimisticLockingFailureException(e);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Conflict", problemDetail.getTitle());
        assertTrue(problemDetail.getDetail().contains("Object of class ["));
        assertEquals(URI.create("https://hmcts.gov.uk/problems/optimistic-locking"), problemDetail.getType());
        assertEquals(DraftAccountEntity.class.getName(), problemDetail.getProperties().get("resourceType"));
        assertEquals("123", problemDetail.getProperties().get("resourceId"));

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void testHandleResourceConflictException() {
        ResourceConflictException e = new ResourceConflictException("DraftAccount", "123","BusinessUnits mismatch");
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleResourceConflictException(e);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problemDetail = response.getBody();

        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Conflict", problemDetail.getTitle());
        assertEquals("BusinessUnits mismatch", problemDetail.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/resource-conflict"), problemDetail.getType());
        assertEquals("DraftAccount", problemDetail.getProperties().get("resourceType"));
        assertEquals("123", problemDetail.getProperties().get("resourceId"));

        assertTrue(response.getHeaders().getContentType().toString()
                       .contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    public static void sampleMethod(Integer testParam) {
        // Sample method to simulate the method parameter
    }
}
