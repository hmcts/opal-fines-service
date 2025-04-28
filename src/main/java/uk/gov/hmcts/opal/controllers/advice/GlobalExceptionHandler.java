package uk.gov.hmcts.opal.controllers.advice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.QueryTimeoutException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.LazyInitializationException;
import org.hibernate.PropertyValueException;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;
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
import java.net.URI;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Optional;

import static uk.gov.hmcts.opal.authentication.service.AccessTokenService.AUTH_HEADER;
import static uk.gov.hmcts.opal.util.HttpUtil.extractPreferredUsername;

@Slf4j(topic = "opal.GlobalExceptionHandler")
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    public static final String DB_UNAVAILABLE_MESSAGE = "Opal Fines Database is currently unavailable";

    private final AccessTokenService tokenService;

    @ExceptionHandler(FeatureDisabledException.class)
    public ResponseEntity<ProblemDetail> handleFeatureDisabledException(FeatureDisabledException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.METHOD_NOT_ALLOWED,
            "Feature Disabled",
            ex.getMessage(),
            "feature-disabled"
        );
        return responseWithProblemDetail(HttpStatus.METHOD_NOT_ALLOWED, problemDetail);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ProblemDetail> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Missing Required Header",
            ex.getMessage(),
            "missing-header"
        );
        return responseWithProblemDetail(HttpStatus.BAD_REQUEST, problemDetail);
    }

    @ExceptionHandler({PermissionNotAllowedException.class, AccessDeniedException.class})
    public ResponseEntity<ProblemDetail> handlePermissionNotAllowedException(Exception ex,
                                                                             HttpServletRequest request) {
        String authorization = request.getHeader(AUTH_HEADER);
        String preferredName = extractPreferredUsername(authorization, tokenService);
        String detailMessage = String.format("For user %s, %s", preferredName, ex.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.FORBIDDEN,
            "Forbidden",
            detailMessage,
            "forbidden"
        );

        log.error(":handlePermissionNotAllowedException: {}", detailMessage);
        return responseWithProblemDetail(HttpStatus.FORBIDDEN, problemDetail);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMediaTypeNotAcceptableException(
        HttpMediaTypeNotAcceptableException ex) {

        log.error(":handleHttpMediaTypeNotAcceptableException: {}", ex.getMessage());
        log.error(":handleHttpMediaTypeNotAcceptableException:", ex.getBody().getDetail());

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.NOT_ACCEPTABLE,
            "Not Acceptable",
            ex.getMessage() + ", " + ex.getBody().getDetail(),
            "not-acceptable"
        );

        return responseWithProblemDetail(HttpStatus.NOT_ACCEPTABLE, problemDetail);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException ex) {

        log.error(":handleMethodArgumentTypeMismatchException: {}", ex.getMessage());
        log.error(":handleMethodArgumentTypeMismatchException:", ex);

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.NOT_ACCEPTABLE,
            "Not Acceptable",
            ex.getMessage(),
            "type-mismatch"
        );

        return responseWithProblemDetail(HttpStatus.NOT_ACCEPTABLE, problemDetail);
    }

    @ExceptionHandler(PropertyValueException.class)
    public ResponseEntity<ProblemDetail> handlePropertyValueException(PropertyValueException pve) {
        log.error(":handlePropertyValueException: {}", pve.getMessage());
        log.error(":handlePropertyValueException:", pve);

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Property Value Error",
            pve.getMessage(),
            "property-value-error"
        );

        // Add additional properties to the problem detail
        problemDetail.setProperty("entity", pve.getEntityName());
        problemDetail.setProperty("property", pve.getPropertyName());

        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleHttpMediaTypeNotSupportedException(
        HttpMediaTypeNotSupportedException ex) {

        log.error(":handleHttpMediaTypeNotSupportedException: {}", ex.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Unsupported Media Type",
            "The Content-Type is not supported. Please use application/json",
            "unsupported-media-type"
        );

        return responseWithProblemDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, problemDetail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException ex) {

        log.error(":handleHttpMessageNotReadableException: {}", ex.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            "The request body could not be read. It may be missing or invalid JSON.",
            "message-not-readable"
        );

        return responseWithProblemDetail(HttpStatus.BAD_REQUEST, problemDetail);
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDataAccessApiUsageException(
        InvalidDataAccessApiUsageException idaaue) {

        log.error(":handleInvalidDataAccessApiUsageException: {}", idaaue.getMessage());
        log.error(":handleInvalidDataAccessApiUsageException:", idaaue);

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            idaaue.getMessage(),
            "invalid-data-access"
        );

        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDataAccessResourceUsageException(
        InvalidDataAccessResourceUsageException idarue) {

        log.error(":handleInvalidDataAccessApiUsageException: {}", idarue.getMessage());
        log.error(":handleInvalidDataAccessApiUsageException:", idarue.getRootCause());

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            idarue.getMessage(),
            "invalid-resource-usage"
        );

        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEntityNotFoundException(
        EntityNotFoundException entityNotFoundException) {

        log.warn(":handleEntityNotFoundException: {}", entityNotFoundException.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.NOT_FOUND,
            "Entity Not Found",
            entityNotFoundException.getMessage(),
            "entity-not-found"
        );

        return responseWithProblemDetail(HttpStatus.NOT_FOUND, problemDetail);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNoSuchElementException(
        NoSuchElementException noSuchElementException) {

        log.warn(":handleNoSuchElementException: {}", noSuchElementException.getMessage());
        log.warn(":handleNoSuchElementException:", getNonNullCause(noSuchElementException));

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.NOT_FOUND,
            "No Value Present",
            noSuchElementException.getMessage(),
            "no-such-element"
        );

        return responseWithProblemDetail(HttpStatus.NOT_FOUND, problemDetail);
    }

    @ExceptionHandler(OpalApiException.class)
    public ResponseEntity<ProblemDetail> handleOpalApiException(
        OpalApiException opalApiException) {

        log.error(":handleOpalApiException: {}", opalApiException.getMessage());
        log.error(":handleOpalApiException:", getNonNullCause(opalApiException));

        HttpStatus status = opalApiException.getError().getHttpStatus();

        ProblemDetail problemDetail = createProblemDetail(
            status,
            status.getReasonPhrase(),
            opalApiException.getMessage(),
            "opal-api-error"
        );

        return responseWithProblemDetail(status, problemDetail);
    }

    @ExceptionHandler({ServletException.class, TransactionSystemException.class, PersistenceException.class})
    public ResponseEntity<ProblemDetail> handleServletExceptions(Exception ex) {

        if (ex instanceof QueryTimeoutException) {
            log.error(":handleQueryTimeoutException: {}", ex.getMessage());

            ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.REQUEST_TIMEOUT,
                "Request Timeout",
                "The request did not receive a response from the database within the timeout period",
                "query-timeout"
            );

            return responseWithProblemDetail(HttpStatus.REQUEST_TIMEOUT, problemDetail);
        }

        if (ex instanceof NoResourceFoundException nrfe) {
            log.error(":handleNoResourceFoundException: {}", nrfe.getBody().getDetail());

            ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Not Found",
                nrfe.getBody().getDetail(),
                "resource-not-found"
            );

            return responseWithProblemDetail(HttpStatus.NOT_FOUND, problemDetail);
        }

        log.error(":handleServletExceptions: {}: {}", ex.getClass(), ex.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred. " + ex.getMessage(),
            "servlet-error"
        );

        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(PSQLException.class)
    public ResponseEntity<ProblemDetail> handlePsqlException(PSQLException psqlException) {

        log.error(":handlePSQLException: {}", psqlException.getMessage());
        log.error(":handlePSQLException: ", getNonNullCause(psqlException));

        if (psqlException.getCause() instanceof ConnectException ||
            psqlException.getCause() instanceof UnknownHostException) {

            ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Service Unavailable",
                DB_UNAVAILABLE_MESSAGE,
                "database-unavailable"
            );

            return responseWithProblemDetail(HttpStatus.SERVICE_UNAVAILABLE, problemDetail);
        }

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            psqlException.getMessage(),
            "database-error"
        );

        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ProblemDetail> handleDataAccessResourceFailureException(
        DataAccessResourceFailureException dataAccessResourceFailureException) {

        log.error(":handleDataAccessResourceFailureException: {}", dataAccessResourceFailureException.getMessage());
        log.error(":handleDataAccessResourceFailureException: ",
                  getNonNullCause(dataAccessResourceFailureException));

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Service Unavailable",
            DB_UNAVAILABLE_MESSAGE,
            "database-unavailable"
        );

        return responseWithProblemDetail(HttpStatus.SERVICE_UNAVAILABLE, problemDetail);
    }

    @ExceptionHandler(LazyInitializationException.class)
    public ResponseEntity<ProblemDetail> handleLazyInitializationException(
        LazyInitializationException lazyInitializationException) {

        log.error(":handleLazyInitializationException: {}", lazyInitializationException.getMessage());
        log.error(":handleLazyInitializationException: ", getNonNullCause(lazyInitializationException));

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "Lazy Entity Initialisation Exception. Expired DB Session?",
            "lazy-initialization"
        );

        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ProblemDetail> handleJpaSystemException(JpaSystemException jpaSystemException) {

        log.error(":handleJpaSystemException: {}", jpaSystemException.getMessage());
        log.error(":handleJpaSystemException: ", getNonNullCause(jpaSystemException));

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "Unknown Entity Persistence Error. Expired DB Session?",
            "jpa-system-error"
        );

        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(JsonSchemaValidationException.class)
    public ResponseEntity<ProblemDetail> handleJsonSchemaValidationException(JsonSchemaValidationException e) {
        log.error(":handleJsonSchemaValidationException: {}", e.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            "JSON Schema Validation Error: " + e.getMessage(),
            "json-schema-validation"
        );

        return responseWithProblemDetail(HttpStatus.BAD_REQUEST, problemDetail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error(":handleIllegalArgumentException: {}", e.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            e.getMessage(),
            "illegal-argument"
        );

        return responseWithProblemDetail(HttpStatus.BAD_REQUEST, problemDetail);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleObjectOptimisticLockingFailureException(
        ObjectOptimisticLockingFailureException e) {

        log.warn(":handleObjectOptimisticLockingFailureException: {}", e.getMessage());

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.CONFLICT,
            "Conflict",
            e.getMessage(),
            "optimistic-locking"
        );

        problemDetail.setProperty("resourceType", e.getPersistentClassName());
        problemDetail.setProperty("resourceId",
                                  Optional.ofNullable(e.getIdentifier()).map(Object::toString).orElse(""));

        return responseWithProblemDetail(HttpStatus.CONFLICT, problemDetail);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ProblemDetail> handleResourceConflictException(ResourceConflictException e) {
        log.error(":handleResourceConflictException: {}", e.getMessage());
        log.error(":handleResourceConflictException: {}", getNonNullCause(e));

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.CONFLICT,
            "Conflict",
            e.getConflictReason(),
            "resource-conflict"
        );

        problemDetail.setProperty("resourceType", e.getResourceType());
        problemDetail.setProperty("resourceId", e.getResourceId());

        return responseWithProblemDetail(HttpStatus.CONFLICT, problemDetail);
    }

    private Throwable getNonNullCause(Throwable t) {
        return t.getCause() == null ? t : t.getCause();
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, String typeUri) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create("https://hmcts.gov.uk/problems/" + typeUri));
        return problemDetail;
    }

    private ResponseEntity<ProblemDetail> responseWithProblemDetail(HttpStatus status, ProblemDetail problemDetail) {
        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problemDetail);
    }
}
