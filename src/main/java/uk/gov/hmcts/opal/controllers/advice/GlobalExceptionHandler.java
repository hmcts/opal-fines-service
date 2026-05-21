package uk.gov.hmcts.opal.controllers.advice;

import static uk.gov.hmcts.opal.util.VersionUtils.createETag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.opal.common.controllers.advice.OpalProblemDetailFactory;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.exception.SubmitterDeniedException;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.util.Versioned;

import java.util.Optional;

@Slf4j(topic = "opal.GlobalExceptionHandler")
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnprocessableException.class)
    public ResponseEntity<ProblemDetail> handleUnprocessableException(UnprocessableException ex) {
        ProblemDetail problemDetail = createProblemDetail(
            HttpStatus.UNPROCESSABLE_CONTENT,
            "Unprocessable Entity",
            "The request could not be processed",
            "unprocessable-entity",
            false,
            ex
        );

        problemDetail.setProperty("unprocessableReason", ex.getDetailedReason());

        return responseWithProblemDetail(HttpStatus.UNPROCESSABLE_CONTENT, problemDetail);
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

    @ExceptionHandler(GlobalExceptionHandler.PaymentCardRequestAlreadyExistsException.class)
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
        Optional.ofNullable(versioned).ifPresent(v -> builder.eTag(createETag(v)));
        return builder.body(problemDetail);
    }

    /**
     * Exception type used for the specific PCR-already-exists conflict so we can return a
     * tailored 409 Problem JSON.
     */
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

        public String getResourceType() {
            return resourceType;
        }

        public String getResourceId() {
            return resourceId;
        }

        public Versioned getVersioned() {
            return versioned;
        }
    }
}
