package uk.gov.hmcts.opal.controllers.advice;

import static uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService.AUTH_HEADER;
import static uk.gov.hmcts.opal.util.HttpUtil.extractPreferredUsername;
import static uk.gov.hmcts.opal.util.VersionUtils.createETag;

import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.QueryTimeoutException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.LazyInitializationException;
import org.hibernate.PropertyValueException;
import org.postgresql.util.PSQLException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uk.gov.hmcts.opal.common.user.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.common.exception.OpalApiException;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.util.LogUtil;
import uk.gov.hmcts.opal.util.Versioned;

@Slf4j(topic = "opal.GlobalExceptionHandler")
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    public static final String DB_UNAVAILABLE_MESSAGE = "Opal Fines Database is currently unavailable";
    public static final String UNKNOWN = "'Unknown'";

    private static final Set<Integer> RETRIABLE_HTTP = Set.of(429, 502, 503, 504);

    private final AccessTokenService tokenService;

    @ExceptionHandler(FeatureDisabledException.class)
    public ResponseEntity<ProblemDetail> handleFeatureDisabledException(FeatureDisabledException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.METHOD_NOT_ALLOWED,
            "Feature Disabled",
            "The requested feature is not currently available",
            "feature-disabled",
            false,
            ex
        );
        return responseWithProblemDetail(HttpStatus.METHOD_NOT_ALLOWED, problemDetail);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ProblemDetail> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Missing Required Header",
            "A required request header is missing",
            "missing-header",
            false,
            ex
        );
        return responseWithProblemDetail(HttpStatus.BAD_REQUEST, problemDetail);
    }

    @ExceptionHandler({PermissionNotAllowedException.class, AccessDeniedException.class})
    public ResponseEntity<ProblemDetail> handlePermissionNotAllowedException(Exception ex,
                                                                             HttpServletRequest request) {
        String authorization = request.getHeader(AUTH_HEADER);
        String preferredName = extractUsername(authorization);
        String internalMessage = String.format("For user %s, %s", preferredName, ex.getMessage());
        log.error("Permission denied: {}", internalMessage);

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.FORBIDDEN,
            "Forbidden",
            "You do not have permission to access this resource",
            "forbidden",
            false,
            ex
        );

        return responseWithProblemDetail(HttpStatus.FORBIDDEN, problemDetail);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMediaTypeNotAcceptableException(
        HttpMediaTypeNotAcceptableException ex) {

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.NOT_ACCEPTABLE,
            "Not Acceptable",
            "The requested media type cannot be produced by the server",
            "not-acceptable",
            false,
            ex
        );

        return responseWithProblemDetail(HttpStatus.NOT_ACCEPTABLE, problemDetail);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException ex) {

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.NOT_ACCEPTABLE,
            "Not Acceptable",
            "Invalid parameter value format",
            "type-mismatch",
            false,
            ex
        );

        return responseWithProblemDetail(HttpStatus.NOT_ACCEPTABLE, problemDetail);
    }

    @ExceptionHandler(PropertyValueException.class)
    public ResponseEntity<ProblemDetail> handlePropertyValueException(PropertyValueException pve) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Property Value Error",
            "Invalid or missing value for a required property",
            "property-value-error",
            false,
            pve
        );

        problemDetail.setProperty("entity", pve.getEntityName());
        problemDetail.setProperty("property", pve.getPropertyName());

        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleHttpMediaTypeNotSupportedException(
        HttpMediaTypeNotSupportedException ex) {

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Unsupported Media Type",
            "The Content-Type is not supported. Please use application/json",
            "unsupported-media-type",
            false,
            ex
        );

        return responseWithProblemDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, problemDetail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException ex) {

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            "The request body could not be read. It may be missing or invalid JSON.",
            "message-not-readable",
            false,
            ex
        );

        return responseWithProblemDetail(HttpStatus.BAD_REQUEST, problemDetail);
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDataAccessApiUsageException(
        InvalidDataAccessApiUsageException idaaue) {

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "A problem occurred while accessing data",
            "invalid-data-access",
            false,
            idaaue
        );

        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDataAccessResourceUsageException(
        InvalidDataAccessResourceUsageException idarue) {

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "A problem occurred with the requested data resource",
            "invalid-resource-usage",
            false,
            idarue
        );

        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEntityNotFoundException(
        EntityNotFoundException entityNotFoundException) {

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.NOT_FOUND,
            "Entity Not Found",
            "The requested entity could not be found",
            "entity-not-found",
            false,
            entityNotFoundException
        );

        return responseWithProblemDetail(HttpStatus.NOT_FOUND, problemDetail);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ProblemDetail> handleNoSuchElementException(
        NoSuchElementException noSuchElementException) {

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.NOT_FOUND,
            "No Value Present",
            "The requested element does not exist",
            "no-such-element",
            false,
            noSuchElementException
        );

        return responseWithProblemDetail(HttpStatus.NOT_FOUND, problemDetail);
    }

    @ExceptionHandler(OpalApiException.class)
    public ResponseEntity<ProblemDetail> handleOpalApiException(
        OpalApiException opalApiException) {

        HttpStatus status = opalApiException.getError().getHttpStatus();

        ProblemDetail problemDetail = createProblemDetail(
            status,
            status.getReasonPhrase(),
            "An error occurred while processing your request",
            "opal-api-error",
            false, // unless your internal error model marks it retriable
            opalApiException
        );

        return responseWithProblemDetail(status, problemDetail);
    }

    @ExceptionHandler({ServletException.class, TransactionSystemException.class, PersistenceException.class})
    public ResponseEntity<ProblemDetail> handleServletExceptions(Exception ex) {

        if (ex instanceof QueryTimeoutException) {
            ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.REQUEST_TIMEOUT,
                "Request Timeout",
                "The request did not receive a response from the database within the timeout period",
                "query-timeout",
                true, // retriable
                ex
            );
            return responseWithProblemDetail(HttpStatus.REQUEST_TIMEOUT, problemDetail);
        }

        if (ex instanceof NoResourceFoundException nrfe) {
            ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Not Found",
                "The requested resource could not be found",
                "resource-not-found",
                false,
                nrfe
            );
            return responseWithProblemDetail(HttpStatus.NOT_FOUND, problemDetail);
        }

        if (ex instanceof TransactionSystemException tse) {
            Throwable root = NestedExceptionUtils.getMostSpecificCause(tse);
            boolean retriable = isTransientSqlState(psqlState(root));
            ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Transaction Error",
                "A transaction error occurred while processing your request",
                "transaction-error",
                retriable,
                tse
            );
            return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
        }

        // ServletException / PersistenceException default
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred while processing your request",
            "servlet-error",
            false,
            ex
        );
        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(PSQLException.class)
    public ResponseEntity<ProblemDetail> handlePsqlException(PSQLException psqlException) {
        if (psqlException.getCause() instanceof ConnectException
            || psqlException.getCause() instanceof UnknownHostException) {

            ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Service Unavailable",
                DB_UNAVAILABLE_MESSAGE,
                "database-unavailable",
                true, // retriable on connectivity/DNS errors
                psqlException
            );
            return responseWithProblemDetail(HttpStatus.SERVICE_UNAVAILABLE, problemDetail);
        }

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "A database error occurred while processing your request",
            "database-error",
            false, // all other PSQLException -> not retriable (per list)
            psqlException
        );
        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ProblemDetail> handleDataAccessResourceFailureException(
        DataAccessResourceFailureException e) {

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Service Unavailable",
            DB_UNAVAILABLE_MESSAGE,
            "database-unavailable",
            true, // retriable
            e
        );
        return responseWithProblemDetail(HttpStatus.SERVICE_UNAVAILABLE, problemDetail);
    }

    @ExceptionHandler(LazyInitializationException.class)
    public ResponseEntity<ProblemDetail> handleLazyInitializationException(
        LazyInitializationException e) {

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "A data access error occurred.",
            "lazy-initialization",
            false,
            e
        );
        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ProblemDetail> handleJpaSystemException(JpaSystemException e) {
        Throwable root = NestedExceptionUtils.getMostSpecificCause(e);
        boolean retriable = isTransientSqlState(psqlState(root));

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "A persistence error occurred while processing your request",
            "jpa-system-error",
            retriable,
            e
        );
        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ProblemDetail> handleHttpServerErrorException(HttpServerErrorException e) {
        int upstream = e.getStatusCode().value();
        boolean retriable = RETRIABLE_HTTP.contains(upstream);

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Downstream Server Error",
            e.getMessage(),
            "http-server-error",
            retriable,
            e
        );

        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail); // response status = 500
    }

    @ExceptionHandler(JsonSchemaValidationException.class)
    public ResponseEntity<ProblemDetail> handleJsonSchemaValidationException(JsonSchemaValidationException e) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            "The request does not conform to the required JSON schema",
            "json-schema-validation",
            false,
            e
        );
        return responseWithProblemDetail(HttpStatus.BAD_REQUEST, problemDetail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException e) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            "Invalid arguments were provided in the request",
            "illegal-argument",
            false,
            e
        );
        return responseWithProblemDetail(HttpStatus.BAD_REQUEST, problemDetail);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleObjectOptimisticLockingFailureException(
        ObjectOptimisticLockingFailureException e) {

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.CONFLICT,
            "Conflict",
            Optional.ofNullable(e.getMessage()).orElse("Conflict updating record. Please try again."),
            "optimistic-locking",
            false,
            e
        );
        problemDetail.setProperty("resourceType", e.getPersistentClassName());
        problemDetail.setProperty("resourceId",
                                  Optional.ofNullable(e.getIdentifier()).map(Object::toString).orElse(""));
        return responseWithProblemDetail(HttpStatus.CONFLICT, problemDetail);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ProblemDetail> handleResourceConflictException(ResourceConflictException e) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.CONFLICT,
            "Conflict",
            "A conflict occurred with the requested resource",
            "resource-conflict",
            false,
            e
        );
        problemDetail.setProperty("resourceType", e.getResourceType());
        problemDetail.setProperty("resourceId", e.getResourceId());
        problemDetail.setProperty("conflictReason", e.getConflictReason());
        return responseWithProblemDetail(HttpStatus.CONFLICT, problemDetail, e.getVersioned());
    }

    @ExceptionHandler(UnprocessableException.class)
    public ResponseEntity<ProblemDetail> handleUnprocessableException(UnprocessableException e) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "TOO_MANY_RESOURCES_FOUND",
            "The request could not be processed due to processing rules",
            "unprocessable",
            false,
            e
        );
        problemDetail.setProperty("unprocessableReason", e.getDetailedReason());
        return responseWithProblemDetail(HttpStatus.UNPROCESSABLE_ENTITY, problemDetail);
    }

    @ExceptionHandler(FeignException.Unauthorized.class)
    public ResponseEntity<ProblemDetail> handleFeignExceptionUnauthorized(FeignException.Unauthorized e) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.UNAUTHORIZED,
            "Not Authorised for Connection",
            e.getMessage(),
            "unauthorized",
            false,
            e
        );
        return responseWithProblemDetail(HttpStatus.valueOf(e.status()), problemDetail);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ProblemDetail> handleFeignException(FeignException e) {
        HttpStatus status = HttpStatus.valueOf(e.status());
        boolean retriable = RETRIABLE_HTTP.contains(status.value());

        ProblemDetail problemDetail = createProblemDetail(
            status,
            "Downstream Service Error",
            "Problem with connecting to a dependant service: " + e.getMessage(),
            "internal-server-error",
            retriable,
            e
        );
        return responseWithProblemDetail(status, problemDetail);
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail,
                                              String typeUri, boolean retry, Throwable exception) {
        String opalOperationId = LogUtil.getOrCreateOpalOperationId();
        log.error("Error ID {}:", opalOperationId, exception);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create("https://hmcts.gov.uk/problems/" + typeUri));
        problemDetail.setInstance(URI.create("https://hmcts.gov.uk/problems/instance/" + opalOperationId));
        problemDetail.setProperty("operation_id", opalOperationId);
        problemDetail.setProperty("retriable", retry);
        return problemDetail;
    }

    private ResponseEntity<ProblemDetail> responseWithProblemDetail(HttpStatus status, ProblemDetail problemDetail) {
        return responseWithProblemDetail(status, problemDetail, null);
    }

    private ResponseEntity<ProblemDetail> responseWithProblemDetail(HttpStatus status, ProblemDetail problemDetail,
        Versioned versioned) {
        BodyBuilder builder = ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON);
        Optional.ofNullable(versioned).ifPresent(v -> builder.eTag(createETag(v)));
        return builder.body(problemDetail);
    }

    private String extractUsername(String authorization) {
        try {
            return extractPreferredUsername(authorization, tokenService);
        } catch (Exception e) {
            return UNKNOWN;
        }
    }

    // ----- helpers -----

    private static String psqlState(Throwable t) {
        if (t instanceof PSQLException p) {
            return p.getSQLState();
        }

        Throwable cause = t == null ? null : t.getCause();

        if (cause instanceof PSQLException p) {
            return p.getSQLState();
        }

        return null;
    }

    private static boolean isTransientSqlState(String state) {
        if (state == null) {
            return false;
        }

        return state.equals("40001")   // serialization_failure
            || state.equals("40P01")   // deadlock_detected
            || state.equals("55P03");  // lock_not_available
    }

}
