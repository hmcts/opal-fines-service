package uk.gov.hmcts.opal.controllers.advice;

import static uk.gov.hmcts.opal.util.VersionUtils.createETag;

import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.common.exceptions.standard.UnauthorizedException;
import uk.gov.hmcts.opal.common.controllers.advice.OpalProblemDetailFactory;
import uk.gov.hmcts.opal.exception.DefendantAccountNotFoundException;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.exception.MissingReportServiceException;
import uk.gov.hmcts.opal.exception.MissingStoredReportContentException;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.exception.RequiredPermissionException;
import uk.gov.hmcts.opal.exception.SchemaConfigurationException;
import uk.gov.hmcts.opal.exception.SubmitterDeniedException;
import uk.gov.hmcts.opal.exception.UnsupportedMappingTypeException;
import uk.gov.hmcts.opal.exception.UnsupportedContentTypeException;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.util.Versioned;

@Slf4j(topic = "opal.GlobalExceptionHandler")
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(RequiredPermissionException.class)
    public ResponseEntity<ProblemDetail> handleRequiredPermissionException(RequiredPermissionException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.FORBIDDEN,
            "Forbidden",
            "User requires permission: " + ex.getPermission().getDescription(),
            "forbidden",
            false,
            ex
        );

        return responseWithProblemDetail(HttpStatus.FORBIDDEN, problemDetail);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetail> handleUnauthorizedException(UnauthorizedException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            "Missing or invalid access token",
            "unauthorized",
            false,
            ex
        );

        return responseWithProblemDetail(HttpStatus.UNAUTHORIZED, problemDetail);
    }

    @ExceptionHandler(UnprocessableException.class)
    public ResponseEntity<ProblemDetail> handleUnprocessableException(UnprocessableException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.UNPROCESSABLE_CONTENT,
            "Unprocessable Content",
            ex.getDetailedReason(),
            "unprocessable-entity",
            ex.isRetriable(),
            ex
        );

        problemDetail.setProperty("unprocessableReason", ex.getDetailedReason());

        return responseWithProblemDetail(HttpStatus.UNPROCESSABLE_CONTENT, problemDetail);
    }

    @ExceptionHandler(UnsupportedContentTypeException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedContentTypeException(UnsupportedContentTypeException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.UNPROCESSABLE_CONTENT,
            "Report Content Type Not Supported",
            ex.getMessage(),
            "unsupported-report-content-type",
            false,
            ex
        );

        return responseWithProblemDetail(HttpStatus.UNPROCESSABLE_CONTENT, problemDetail);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEntityNotFoundException(EntityNotFoundException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.NOT_FOUND,
            "Entity Not Found",
            "The requested entity could not be found",
            "entity-not-found",
            false,
            ex
        );

        return responseWithProblemDetail(HttpStatus.NOT_FOUND, problemDetail);
    }

    @ExceptionHandler(DefendantAccountNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleDefendantAccountNotFoundException(
        DefendantAccountNotFoundException ex) {

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.NOT_FOUND,
            "Defendant Account Not Found",
            "Defendant account not found with id: " + ex.getDefendantAccountId(),
            "entity-not-found",
            false,
            ex
        );

        return responseWithProblemDetail(HttpStatus.NOT_FOUND, problemDetail);
    }

    @ExceptionHandler(MissingStoredReportContentException.class)
    public ResponseEntity<ProblemDetail> handleMissingStoredReportContentException(
        MissingStoredReportContentException ex) {

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Missing Data In Storage Account",
            ex.getMessage(),
            "missing-report-data-in-storage-account",
            false,
            ex
        );

        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(MissingReportServiceException.class)
    public ResponseEntity<ProblemDetail> handleMissingReportServiceException(MissingReportServiceException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Missing Report Service",
            ex.getMessage(),
            "missing-report-service",
            false,
            ex
        );

        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(UnsupportedMappingTypeException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedMappingTypeException(UnsupportedMappingTypeException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Unsupported Mapping Type",
            ex.getMessage(),
            "unsupported-mapping-type",
            false,
            ex
        );
        problemDetail.setProperty("mapping_type", ex.getMappingType());
        problemDetail.setProperty("supported_types", ex.getSupportedTypes());

        return responseWithProblemDetail(HttpStatus.BAD_REQUEST, problemDetail);
    }
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ProblemDetail> handleHttpClientErrorException(HttpClientErrorException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        ProblemDetail problemDetail = createProblemDetail(
            status,
            status.getReasonPhrase(),
            Optional.ofNullable(ex.getStatusText()).filter(text -> !text.isBlank()).orElse(ex.getMessage()),
            "http-client-error",
            false,
            ex
        );

        return responseWithProblemDetail(status, problemDetail);
    }

    @ExceptionHandler(JsonSchemaValidationException.class)
    public ResponseEntity<ProblemDetail> handleJsonSchemaValidationException(JsonSchemaValidationException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            "The request does not conform to the required JSON schema",
            "json-schema-validation",
            false,
            ex
        );
        return responseWithProblemDetail(HttpStatus.BAD_REQUEST, problemDetail);
    }

    @ExceptionHandler(SchemaConfigurationException.class)
    public ResponseEntity<ProblemDetail> handleSchemaConfigurationException(SchemaConfigurationException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            ex.getMessage(),
            "internal-server-error",
            false,
            ex
        );
        return responseWithProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail);
    }

    @ExceptionHandler(InvalidReferenceValidationException.class)
    public ResponseEntity<ProblemDetail> handleInvalidReferenceValidationException(
        InvalidReferenceValidationException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            ex.getMessage(),
            "invalid-reference-validation",
            false,
            ex
        );
        return responseWithProblemDetail(HttpStatus.BAD_REQUEST, problemDetail);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ProblemDetail> handleResourceConflictException(ResourceConflictException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.CONFLICT,
            "Conflict",
            "A conflict occurred with the requested resource",
            "resource-conflict",
            false,
            ex
        );
        problemDetail.setProperty("resourceType", ex.getResourceType());
        problemDetail.setProperty("resourceId", ex.getResourceId());
        problemDetail.setProperty("conflictReason", ex.getConflictReason());
        return responseWithProblemDetail(HttpStatus.CONFLICT, problemDetail, ex.getVersioned());
    }

    @ExceptionHandler(SubmitterDeniedException.class)
    public ResponseEntity<ProblemDetail> handleActionDeniedForSubmitterException(SubmitterDeniedException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.FORBIDDEN,
            "Submitter cannot " + ex.getUpdateType(),
            "A single user cannot submit and " + ex.getUpdateType() + " the same Draft Account",
            "submitter-cannot-" + ex.getUpdateType(),
            false,
            ex
        );
        return responseWithProblemDetail(HttpStatus.FORBIDDEN, problemDetail);
    }

    @ExceptionHandler(PaymentCardRequestAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handlePaymentCardRequestAlreadyExists(
        PaymentCardRequestAlreadyExistsException ex) {

        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.CONFLICT,
            "Conflict",
            ex.getMessage(),
            "resource-conflict",
            true,
            ex
        );

        problemDetail.setProperty("resourceType", ex.getResourceType());
        problemDetail.setProperty("resourceId", ex.getResourceId());

        return responseWithProblemDetail(HttpStatus.CONFLICT, problemDetail, ex.getVersioned());
    }

    @ExceptionHandler(FeignException.Unauthorized.class)
    public ResponseEntity<ProblemDetail> handleFeignExceptionUnauthorized(FeignException.Unauthorized ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.UNAUTHORIZED,
            "Not Authorised for Connection",
            ex.getMessage(),
            "unauthorized",
            false,
            ex
        );
        return responseWithProblemDetail(HttpStatus.UNAUTHORIZED, problemDetail);
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail,
                                              String typeUri, boolean retry, Throwable exception) {
        return OpalProblemDetailFactory.createProblemDetail(status, title, detail, typeUri, retry, exception, log);
    }

    private ResponseEntity<ProblemDetail> responseWithProblemDetail(HttpStatus status, ProblemDetail problemDetail) {
        return responseWithProblemDetail(status, problemDetail, null);
    }

    private ResponseEntity<ProblemDetail> responseWithProblemDetail(HttpStatus status, ProblemDetail problemDetail,
                                                                    Versioned versioned) {
        BodyBuilder builder = ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON);
        Optional.ofNullable(versioned).ifPresent(value -> builder.eTag(createETag(value)));
        return builder.body(problemDetail);
    }

    @Getter
    public static class PaymentCardRequestAlreadyExistsException extends RuntimeException {

        private final String resourceType;
        private final String resourceId;
        private final Versioned versioned;

        public PaymentCardRequestAlreadyExistsException(String resourceType, String resourceId, Versioned versioned) {
            super("A payment card request already exists for this account.");
            this.resourceType = resourceType;
            this.resourceId = resourceId;
            this.versioned = versioned;
        }

        public PaymentCardRequestAlreadyExistsException(String resourceType, String resourceId) {
            this(resourceType, resourceId, null);
        }
    }
}
