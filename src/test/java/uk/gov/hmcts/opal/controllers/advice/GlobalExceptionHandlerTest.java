package uk.gov.hmcts.opal.controllers.advice;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.exception.SubmitterDeniedException;
import uk.gov.hmcts.opal.exception.UnprocessableException;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleJsonSchemaValidationException_returnsBadRequestProblemDetail() {
        ResponseEntity<ProblemDetail> response = globalExceptionHandler
            .handleJsonSchemaValidationException(new JsonSchemaValidationException("bad schema"));

        assertProblem(response, HttpStatus.BAD_REQUEST, "Bad Request", "json-schema-validation", false);
    }

    @Test
    void handleResourceConflict_returnsConflictProblemDetail() {
        ResourceConflictException exception = new ResourceConflictException(
            "DraftAccount",
            "123",
            "BU mismatch",
            null);

        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleResourceConflictException(exception);

        assertProblem(response, HttpStatus.CONFLICT, "Conflict", "resource-conflict", false);
        ProblemDetail problemDetail = response.getBody();
        assertEquals("DraftAccount", problemDetail.getProperties().get("resourceType"));
        assertEquals("123", problemDetail.getProperties().get("resourceId"));
        assertEquals("BU mismatch", problemDetail.getProperties().get("conflictReason"));
        assertNull(response.getHeaders().getETag());
    }

    @Test
    void handleResourceConflict_withVersioned_addsEtag() {
        ResourceConflictException exception = new ResourceConflictException(
            "DraftAccount",
            "123",
            "BU mismatch",
            () -> BigInteger.valueOf(666));

        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleResourceConflictException(exception);

        assertProblem(response, HttpStatus.CONFLICT, "Conflict", "resource-conflict", false);
        assertEquals("\"666\"", response.getHeaders().getETag());
    }

    @Test
    void handleUnprocessableException_addsReason() {
        UnprocessableException exception = new UnprocessableException("Too many results");

        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleUnprocessableException(exception);

        assertProblem(response, HttpStatus.UNPROCESSABLE_CONTENT, "Unprocessable Entity",
                      "unprocessable-entity", false);
        assertEquals("Too many results", response.getBody().getProperties().get("unprocessableReason"));
    }

    @Test
    void handleSubmitterCannotValidate_returnsForbiddenProblemDetail() {
        SubmitterDeniedException exception = new SubmitterDeniedException("Pable", "validate");

        ResponseEntity<ProblemDetail> response = globalExceptionHandler
            .handleActionDeniedForSubmitterException(exception);

        assertProblem(response, HttpStatus.FORBIDDEN, "Submitter cannot validate",
                      "submitter-cannot-validate", false);
        assertEquals("A single user cannot submit and validate the same Draft Account",
                     response.getBody().getDetail());
    }

    @Test
    void handlePaymentCardRequestAlreadyExists_keepsCurrentRetryAdvice() {
        GlobalExceptionHandler.PaymentCardRequestAlreadyExistsException exception =
            new GlobalExceptionHandler.PaymentCardRequestAlreadyExistsException("DefendantAccount", "123");

        ResponseEntity<ProblemDetail> response = globalExceptionHandler
            .handlePaymentCardRequestAlreadyExists(exception);

        assertProblem(response, HttpStatus.CONFLICT, "Conflict", "resource-conflict", true);
        assertEquals("DefendantAccount", response.getBody().getProperties().get("resourceType"));
        assertEquals("123", response.getBody().getProperties().get("resourceId"));
    }

    private static void assertProblem(ResponseEntity<ProblemDetail> response, HttpStatus status, String title,
                                      String type, boolean retriable) {
        assertEquals(status, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());
        ProblemDetail problemDetail = response.getBody();
        assertNotNull(problemDetail);
        assertEquals(status.value(), problemDetail.getStatus());
        assertEquals(title, problemDetail.getTitle());
        assertEquals("https://hmcts.gov.uk/problems/" + type, problemDetail.getType().toString());
        assertNotNull(problemDetail.getProperties().get("operation_id"));
        assertEquals(retriable, problemDetail.getProperties().get("retriable"));
    }
}
