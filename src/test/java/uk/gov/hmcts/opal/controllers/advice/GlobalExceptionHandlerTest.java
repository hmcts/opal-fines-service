package uk.gov.hmcts.opal.controllers.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.common.exceptions.standard.UnauthorizedException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.exception.DefendantAccountNotFoundException;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.exception.MissingMappingTypeException;
import uk.gov.hmcts.opal.exception.MissingReportServiceException;
import uk.gov.hmcts.opal.exception.MissingStoredReportContentException;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.exception.RequiredPermissionException;
import uk.gov.hmcts.opal.exception.SchemaConfigurationException;
import uk.gov.hmcts.opal.exception.SubmitterDeniedException;
import uk.gov.hmcts.opal.exception.UnsupportedMappingTypeException;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.exception.UnsupportedContentTypeException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleRequiredPermission_returnsForbiddenProblem() {
        RequiredPermissionException ex = new RequiredPermissionException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);

        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleRequiredPermissionException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());
        assertEquals("Forbidden", response.getBody().getTitle());
        assertEquals("User requires permission: Search and View Accounts", response.getBody().getDetail());
        assertEquals(false, response.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleUnauthorized_returnsUnauthorizedProblem() {
        UnauthorizedException ex = new UnauthorizedException("Unauthorised", "Bad token");

        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleUnauthorizedException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());
        assertEquals("Unauthorized", response.getBody().getTitle());
        assertEquals("Missing or invalid access token", response.getBody().getDetail());
        assertEquals(false, response.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleUnprocessable_returnsReasonAndRetryFlag() {
        UnprocessableException ex = new UnprocessableException("Too many results", true);

        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleUnprocessableException(ex);

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
        assertEquals("Unprocessable Content", response.getBody().getTitle());
        assertEquals(true, response.getBody().getProperties().get("retriable"));
        assertEquals("Too many results", response.getBody().getProperties().get("unprocessableReason"));
    }

    @Test
    void handleUnsupportedContentType_returnsUnprocessableProblem() {
        UnsupportedContentTypeException ex = new UnsupportedContentTypeException(
            "report",
            "text/plain",
            Collections.singletonList("application/pdf")
        );

        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleUnsupportedContentTypeException(ex);

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
        assertEquals("Report Content Type Not Supported", response.getBody().getTitle());
        assertEquals(false, response.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleDefendantAccountNotFound_returnsNotFoundProblem() {
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleDefendantAccountNotFoundException(
            new DefendantAccountNotFoundException(999999999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Defendant Account Not Found", response.getBody().getTitle());
        assertEquals("Defendant account not found with id: 999999999", response.getBody().getDetail());
        assertEquals(false, response.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleEntityNotFound_returnsSanitizedNotFoundProblem() {
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleEntityNotFoundException(
            new EntityNotFoundException("Defendant Account not found with id: 999999999")
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Entity Not Found", response.getBody().getTitle());
        assertEquals("The requested entity could not be found", response.getBody().getDetail());
        assertEquals(false, response.getBody().getProperties().get("retriable"));
        assertNull(response.getBody().getProperties().get("reason"));
    }

    @Test
    void handleMissingStoredReportContent_returnsInternalServerErrorProblem() {
        MissingStoredReportContentException ex = new MissingStoredReportContentException(12L, "blob/path");

        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleMissingStoredReportContentException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Missing Data In Storage Account", response.getBody().getTitle());
        assertEquals(
            "Stored report content file 'blob/path' was not found for report instance id: 12",
            response.getBody().getDetail()
        );
        assertEquals(false, response.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleMissingReportService_returnsInternalServerErrorProblem() {
        MissingReportServiceException ex = new MissingReportServiceException("REPORT_1");

        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleMissingReportServiceException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Missing Report Service", response.getBody().getTitle());
        assertEquals(
            "No report service implementation found for reportId: REPORT_1",
            response.getBody().getDetail()
        );
    }

    @Test
    void handleHttpClientError_returnsUpstreamStatus() {
        HttpClientErrorException ex = HttpClientErrorException.create(
            HttpStatusCode.valueOf(404),
            "Not Found!",
            HttpHeaders.EMPTY,
            null,
            null
        );

        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleHttpClientErrorException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not Found", response.getBody().getTitle());
        assertEquals("Not Found!", response.getBody().getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/http-client-error"), response.getBody().getType());
    }

    @Test
    void handleJsonSchemaValidation_returnsBadRequestProblem() {
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleJsonSchemaValidationException(
            new JsonSchemaValidationException("bad schema")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().getTitle());
        assertEquals("The request does not conform to the required JSON schema", response.getBody().getDetail());
    }

    @Test
    void handleSchemaConfiguration_returnsInternalServerErrorProblem() {
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleSchemaConfigurationException(
            new SchemaConfigurationException("missing config")
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().getTitle());
        assertEquals("missing config", response.getBody().getDetail());
    }

    @Test
    void handleInvalidReferenceValidation_returnsBadRequestProblem() {
        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleInvalidReferenceValidationException(
            new InvalidReferenceValidationException("bad reference data")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().getTitle());
        assertEquals("bad reference data", response.getBody().getDetail());
        assertEquals(
            URI.create("https://hmcts.gov.uk/problems/invalid-reference-validation"),
            response.getBody().getType()
        );
    }

    @Test
    void handleUnsupportedMappingType_false() {
        UnsupportedMappingTypeException ex = new UnsupportedMappingTypeException(
            "unsupported-type",
            List.of("defendant-account-status")
        );

        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleUnsupportedMappingTypeException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
        ProblemDetail pd = r.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), pd.getStatus());
        assertEquals("Unsupported Mapping Type", pd.getTitle());
        assertEquals(
            "Unsupported mapping type: unsupported-type. Supported types: defendant-account-status",
            pd.getDetail()
        );
        assertEquals("unsupported-type", pd.getProperties().get("mapping_type"));
        assertEquals(List.of("defendant-account-status"), pd.getProperties().get("supported_types"));
        assertEquals(URI.create("https://hmcts.gov.uk/problems/unsupported-mapping-type"), pd.getType());
        assertEquals(false, pd.getProperties().get("retriable"));
    }

    @Test
    void handleResourceConflict_returnsPropertiesAndEtag() {
        ResourceConflictException ex = new ResourceConflictException(
            DraftAccountEntity.class.getSimpleName(),
            123,
            "BU mismatch",
            () -> BigInteger.valueOf(666)
        );

        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleResourceConflictException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(false, response.getBody().getProperties().get("retriable"));
        assertEquals("DraftAccountEntity", response.getBody().getProperties().get("resourceType"));
        assertEquals("123", response.getBody().getProperties().get("resourceId"));
        assertEquals("BU mismatch", response.getBody().getProperties().get("conflictReason"));
        assertEquals("\"666\"", response.getHeaders().getETag());
    }

    @Test
    void handleMissingMappingType_false() {
        MissingMappingTypeException ex = new MissingMappingTypeException(List.of("defendant-account-status"));

        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleMissingMappingTypeException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
        ProblemDetail pd = r.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), pd.getStatus());
        assertEquals("Missing Mapping Type", pd.getTitle());
        assertEquals(
            "Required mapping type is missing. Supported types: defendant-account-status",
            pd.getDetail()
        );
        assertEquals(List.of("defendant-account-status"), pd.getProperties().get("supported_types"));
        assertEquals(URI.create("https://hmcts.gov.uk/problems/missing-mapping-type"), pd.getType());
        assertEquals(false, pd.getProperties().get("retriable"));
    }

    @Test
    void handleSubmitterDenied_returnsForbiddenProblem() {
        SubmitterDeniedException ex = new SubmitterDeniedException("Pable", "validate");

        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleActionDeniedForSubmitterException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Submitter cannot validate", response.getBody().getTitle());
        assertEquals(
            "A single user cannot submit and validate the same Draft Account",
            response.getBody().getDetail()
        );
        assertEquals(
            URI.create("https://hmcts.gov.uk/problems/submitter-cannot-validate"),
            response.getBody().getType()
        );
    }

    @Test
    void handlePaymentCardRequestAlreadyExists_returnsConflictProblemAndEtag() {
        GlobalExceptionHandler.PaymentCardRequestAlreadyExistsException ex =
            new GlobalExceptionHandler.PaymentCardRequestAlreadyExistsException(
                "DraftAccount",
                "123",
                () -> BigInteger.valueOf(99)
            );

        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handlePaymentCardRequestAlreadyExists(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict", response.getBody().getTitle());
        assertEquals("A payment card request already exists for this account.", response.getBody().getDetail());
        assertEquals(true, response.getBody().getProperties().get("retriable"));
        assertEquals("DraftAccount", response.getBody().getProperties().get("resourceType"));
        assertEquals("123", response.getBody().getProperties().get("resourceId"));
        assertEquals("\"99\"", response.getHeaders().getETag());
    }

    @Test
    void handlePaymentCardRequestAlreadyExists_withoutVersionedOmitsEtag() {
        GlobalExceptionHandler.PaymentCardRequestAlreadyExistsException ex =
            new GlobalExceptionHandler.PaymentCardRequestAlreadyExistsException("DraftAccount", "123");

        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handlePaymentCardRequestAlreadyExists(ex);

        assertNull(response.getHeaders().getETag());
    }

    @Test
    void handleFeignUnauthorized_returnsUnauthorizedProblem() {
        FeignException.Unauthorized ex = (FeignException.Unauthorized) buildFeignException(401, "Unauthorized");

        ResponseEntity<ProblemDetail> response = globalExceptionHandler.handleFeignExceptionUnauthorized(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Not Authorised for Connection", response.getBody().getTitle());
        assertEquals(false, response.getBody().getProperties().get("retriable"));
    }

    private static FeignException buildFeignException(int status, String reason) {
        Map<String, Collection<String>> headers = Collections.emptyMap();

        Request request = Request.create(
            Request.HttpMethod.GET,
            "/test",
            headers,
            Request.Body.empty(),
            new RequestTemplate()
        );

        Response response = Response.builder()
            .request(request)
            .status(status)
            .reason(reason)
            .headers(headers)
            .build();

        return FeignException.errorStatus("GET /test", response);
    }
}
