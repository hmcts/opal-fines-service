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
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.opal.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.exception.OpalApiException;
import uk.gov.hmcts.opal.launchdarkly.FeatureDisabledException;

import java.util.Map;

import static uk.gov.hmcts.opal.authentication.service.AccessTokenService.AUTH_HEADER;
import static uk.gov.hmcts.opal.util.HttpUtil.extractPreferredUsername;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    public static final String ERROR_MESSAGE = "errorMessage";

    private final AccessTokenService tokenService;

    @ExceptionHandler(FeatureDisabledException.class)
    public ResponseEntity<String> handleFeatureDisabledException(FeatureDisabledException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ex.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<String> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler({PermissionNotAllowedException.class, AccessDeniedException.class})
    public ResponseEntity<String> handlePermissionNotAllowedException(Exception ex,
                                                                      HttpServletRequest request) {
        String authorization = request.getHeader(AUTH_HEADER);
        String preferredName = extractPreferredUsername(authorization, tokenService);
        String message = String.format("For user %s, %s", preferredName, ex.getMessage());
        log.warn(message);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMediaTypeNotAcceptableException(
        HttpMediaTypeNotAcceptableException ex) {

        log.error(":handleHttpMediaTypeNotAcceptableException: {}", ex.getMessage());
        log.error(":handleHttpMediaTypeNotAcceptableException:", ex.getCause());

        Map<String, String> body = Map.of(
            "error", "Not Acceptable",
            "message", "The server cannot produce a response matching the request Accept header"
        );
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handlePropertyValueException(PropertyValueException pve) {
        log.error(":handlePropertyValueException: {}", pve.getMessage());
        Map<String, String> body = Map.of(
            ERROR_MESSAGE, pve.getMessage(),
            "entity", pve.getEntityName(),
            "property", pve.getPropertyName()
        );
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException hmnre) {

        log.error(":handleHttpMessageNotReadableException: {}", hmnre.getMessage());
        Map<String, String> body = Map.of(
            ERROR_MESSAGE, hmnre.getMessage()
        );
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleInvalidDataAccessApiUsageException(
        InvalidDataAccessApiUsageException idaaue) {

        log.error(":handleInvalidDataAccessApiUsageException: {}", idaaue.getMessage());
        Map<String, String> body = Map.of(
            ERROR_MESSAGE, idaaue.getMessage()
        );
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleInvalidDataAccessResourceUsageException(
        InvalidDataAccessResourceUsageException idarue) {

        log.error(":handleInvalidDataAccessApiUsageException: {}", idarue.getMessage());
        log.error(":handleInvalidDataAccessApiUsageException:", idarue.getRootCause());

        Map<String, String> body = Map.of(
            ERROR_MESSAGE, idarue.getMessage()
        );
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(
        EntityNotFoundException entityNotFoundException) {

        log.error(":handleEntityNotFoundException: {}", entityNotFoundException.getMessage());
        log.error(":handleEntityNotFoundException:", entityNotFoundException.getCause());

        Map<String, String> body = Map.of(
            ERROR_MESSAGE, entityNotFoundException.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(OpalApiException.class)
    public ResponseEntity<Map<String, String>> handleOpalApiException(
        OpalApiException opalApiException) {

        log.error(":handleOpalApiException: {}", opalApiException.getMessage());
        log.error(":handleOpalApiException:", opalApiException.getCause());

        Map<String, String> body = Map.of(
            "error", opalApiException.getError().getHttpStatus().getReasonPhrase(),
            "message", opalApiException.getMessage()
        );
        return ResponseEntity.status(opalApiException.getError().getHttpStatus()).body(body);
    }

    @ExceptionHandler({ServletException.class, TransactionSystemException.class, PersistenceException.class})
    public ResponseEntity<Map<String, String>> handleDatabaseExceptions(Exception ex) {

        if (ex instanceof QueryTimeoutException) {
            log.error(":handleQueryTimeoutException: {}", ex.getMessage());

            Map<String, String> body = Map.of(
                "error", "Request Timeout",
                "message", "The request did not receive a response from the database within the timeout period"
            );
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(body);
        }

        // If it's not a QueryTimeoutException, return a generic internal server error
        Map<String, String> body = Map.of(
            "error", "Internal Server Error",
            "message", "An unexpected error occurred"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handlePsqlException(
        PSQLException psqlException) {

        log.error(":handlePSQLException: {}", psqlException.getMessage());
        log.error(":handlePSQLException:", psqlException.getCause());

        if (psqlException.getCause() instanceof java.net.ConnectException) {
            Map<String, String> body = Map.of(
                "error", "Service Unavailable", "message",
                "Opal Fines Database is currently unavailable"
            );
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
        }

        Map<String, String> body = Map.of(
            "error", "Internal Server Error", "message", psqlException.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleDataAccessResourceFailureException(
        DataAccessResourceFailureException dataAccessResourceFailureException) {

        log.error(":handleDataAccessResourceFailureException: {}", dataAccessResourceFailureException.getMessage());
        log.error(":handleDataAccessResourceFailureException:", dataAccessResourceFailureException.getCause());

        Map<String, String> body = Map.of(
            "error", "Service Unavailable", "message", "Opal Fines Database is currently unavailable"
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}
