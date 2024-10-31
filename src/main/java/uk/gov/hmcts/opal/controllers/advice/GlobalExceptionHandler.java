package uk.gov.hmcts.opal.controllers.advice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.QueryTimeoutException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.PropertyValueException;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.exception.OpalApiException;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.launchdarkly.FeatureDisabledException;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

import static uk.gov.hmcts.opal.authentication.service.AccessTokenService.AUTH_HEADER;
import static uk.gov.hmcts.opal.util.HttpUtil.extractPreferredUsername;

@Slf4j(topic = "GlobalExceptionHandler")
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    public static final String ERROR = "error";

    public static final String MESSAGE = "message";

    public static final String DB_UNAVAILABLE_MESSAGE = "Opal Fines Database is currently unavailable";


    private final AccessTokenService tokenService;

    @ExceptionHandler(FeatureDisabledException.class)
    public ResponseEntity<String> handleFeatureDisabledException(FeatureDisabledException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .contentType(MediaType.APPLICATION_JSON).body(ex.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<String> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON).body(ex.getMessage());
    }

    @ExceptionHandler({PermissionNotAllowedException.class, AccessDeniedException.class})
    public ResponseEntity<String> handlePermissionNotAllowedException(Exception ex,
                                                                      HttpServletRequest request) {
        String authorization = request.getHeader(AUTH_HEADER);
        String preferredName = extractPreferredUsername(authorization, tokenService);
        String message = String.format("{\"error\": \"Forbidden\", \"message\" : \"For user %s, %s\"}", preferredName,
                                       ex.getMessage());
        log.error(":handlePermissionNotAllowedException: {}", message);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body(message);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMediaTypeNotAcceptableException(
        HttpMediaTypeNotAcceptableException ex) {

        log.error(":handleHttpMediaTypeNotAcceptableException: {}", ex.getMessage());
        log.error(":handleHttpMediaTypeNotAcceptableException:", ex.getBody().getDetail());

        Map<String, String> body = new LinkedHashMap<>();
        body.put(ERROR, "Not Acceptable");
        body.put(MESSAGE, ex.getMessage() + ", " + ex.getBody().getDetail());

        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException ex) {

        log.error(":handleMethodArgumentTypeMismatchException: {}", ex.getMessage());
        log.error(":handleMethodArgumentTypeMismatchException:", ex.getCause());

        Map<String, String> body = new LinkedHashMap<>();
        body.put(ERROR, "Not Acceptable");
        body.put(MESSAGE, ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handlePropertyValueException(PropertyValueException pve) {
        log.error(":handlePropertyValueException: {}", pve.getMessage());
        Map<String, String> body = new LinkedHashMap<>();
        body.put(ERROR, pve.getMessage());
        body.put("entity", pve.getEntityName());
        body.put("property", pve.getPropertyName());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleHttpMediaTypeNotSupportedException(
        HttpMediaTypeNotSupportedException ex) {

        log.error(":handleHttpMediaTypeNotSupportedException: {}", ex.getMessage());
        Map<String, String> body = new LinkedHashMap<>();
        body.put(ERROR, "Unsupported Media Type");
        body.put(MESSAGE, "The Content-Type is not supported. Please use application/json");

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException ex) {

        log.error(":handleHttpMessageNotReadableException: {}", ex.getMessage());
        Map<String, String> body = new LinkedHashMap<>();
        body.put(ERROR, "Bad Request");
        body.put(MESSAGE, "The request body could not be read. It may be missing or invalid JSON.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleInvalidDataAccessApiUsageException(
        InvalidDataAccessApiUsageException idaaue) {

        log.error(":handleInvalidDataAccessApiUsageException: {}", idaaue.getMessage());
        log.error(":handleInvalidDataAccessApiUsageException:", idaaue);

        Map<String, String> body = new LinkedHashMap<>();

        body.put(ERROR, idaaue.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleInvalidDataAccessResourceUsageException(
        InvalidDataAccessResourceUsageException idarue) {

        log.error(":handleInvalidDataAccessApiUsageException: {}", idarue.getMessage());
        log.error(":handleInvalidDataAccessApiUsageException:", idarue.getRootCause());

        Map<String, String> body = new LinkedHashMap<>();
        body.put(ERROR, idarue.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(
        EntityNotFoundException entityNotFoundException) {

        log.error(":handleEntityNotFoundException: {}", entityNotFoundException.getMessage());
        log.error(":handleEntityNotFoundException:", entityNotFoundException.getCause());

        Map<String, String> body = new LinkedHashMap<>();
        body.put(ERROR, "Entity Not Found");
        body.put(MESSAGE, entityNotFoundException.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler(OpalApiException.class)
    public ResponseEntity<Map<String, String>> handleOpalApiException(
        OpalApiException opalApiException) {

        log.error(":handleOpalApiException: {}", opalApiException.getMessage());
        log.error(":handleOpalApiException:", opalApiException.getCause());

        Map<String, String> body = new LinkedHashMap<>();
        body.put(ERROR, opalApiException.getError().getHttpStatus().getReasonPhrase());
        body.put(MESSAGE, opalApiException.getMessage());
        return ResponseEntity.status(opalApiException.getError().getHttpStatus())
            .contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler({ServletException.class, TransactionSystemException.class, PersistenceException.class})
    public ResponseEntity<Map<String, String>> handleServletExceptions(Exception ex) {

        if (ex instanceof QueryTimeoutException) {
            log.error(":handleQueryTimeoutException: {}", ex.getMessage());

            Map<String, String> body = new LinkedHashMap<>();
            body.put(ERROR, "Request Timeout");
            body.put(MESSAGE, "The request did not receive a response from the database within the timeout period");
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).contentType(MediaType.APPLICATION_JSON).body(body);
        }

        if (ex instanceof NoResourceFoundException) {
            log.error(":handleNoResourceFoundException: {}", ((NoResourceFoundException) ex).getBody().getDetail());

            Map<String, String> body = new LinkedHashMap<>();
            body.put(ERROR, "Not Found");
            body.put(MESSAGE, ((NoResourceFoundException) ex).getBody().getDetail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(body);
        }


        Map<String, String> body = new LinkedHashMap<>();
        body.put(ERROR, "Internal Server Error");
        body.put(MESSAGE, "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handlePsqlException(
        PSQLException psqlException) {

        log.error(":handlePSQLException: {}", psqlException.getMessage());
        log.error(":handlePSQLException:", psqlException.getCause());

        if (psqlException.getCause() instanceof ConnectException || psqlException.getCause()
            instanceof UnknownHostException) {
            Map<String, String> body = new LinkedHashMap<>();
            body.put(ERROR, "Service Unavailable");
            body.put(MESSAGE, DB_UNAVAILABLE_MESSAGE);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON).body(body);
        }

        Map<String, String> body = new LinkedHashMap<>();
        body.put(ERROR, "Internal Server Error");
        body.put(MESSAGE, psqlException.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleDataAccessResourceFailureException(
        DataAccessResourceFailureException dataAccessResourceFailureException) {

        log.error(":handleDataAccessResourceFailureException: {}", dataAccessResourceFailureException.getMessage());
        log.error(":handleDataAccessResourceFailureException:", dataAccessResourceFailureException.getCause());

        Map<String, String> body = new LinkedHashMap<>();
        body.put(ERROR, "Service Unavailable");
        body.put(MESSAGE, DB_UNAVAILABLE_MESSAGE);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler(JsonSchemaValidationException.class)
    public ResponseEntity<Map<String, String>> handleJsonSchemaValidationException(JsonSchemaValidationException e) {
        log.error(":handleJsonSchemaValidationException: {}", e.getMessage());

        Map<String, String> body = new LinkedHashMap<>();
        body.put(ERROR, "Bad Request");
        body.put(MESSAGE, "JSON Schema Validation Error: " + e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error(":handleIllegalArgumentException: {}", e.getMessage());

        Map<String, String> body = new LinkedHashMap<>();
        body.put(ERROR, "Bad Request");
        body.put(MESSAGE, e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<Map<String, String>> handleResourceConflictException(ResourceConflictException e) {
        log.error(":handleResourceConflictException: {}", e.getMessage());

        Map<String, String> body = new LinkedHashMap<>();
        body.put(ERROR, "Conflict");
        body.put("resourceType", e.getResourceType());
        body.put("conflictReason", e.getConflictReason());

        return ResponseEntity.status(HttpStatus.CONFLICT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);

    }


}
