package uk.gov.hmcts.opal.controllers.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.QueryTimeoutException;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import org.hibernate.LazyInitializationException;
import org.hibernate.PropertyValueException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import uk.gov.hmcts.opal.common.user.authentication.exception.AuthenticationError;
import uk.gov.hmcts.opal.common.user.authentication.exception.MissingRequestHeaderException;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.entity.draft.DraftAccountEntity;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.common.exception.OpalApiException;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.exception.SubmitterCannotValidateException;
import uk.gov.hmcts.opal.launchdarkly.FeatureDisabledException;

@SpringBootTest
@ContextConfiguration(classes = GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @MockitoBean FeatureDisabledException featureDisabledException;
    @MockitoBean MissingRequestHeaderException missingRequestHeaderException;
    @MockitoBean PermissionNotAllowedException permissionNotAllowedException;
    @MockitoBean AccessTokenService tokenService;

    @Autowired GlobalExceptionHandler globalExceptionHandler;

    // ---------- Simple false (non-retriable) buckets ----------

    @Test
    void handleFeatureDisabledException_false() {
        FeatureDisabledException ex = new FeatureDisabledException("off");
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleFeatureDisabledException(ex);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, r.getStatusCode());
        ProblemDetail pd = r.getBody();
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), pd.getStatus());
        assertEquals("Feature Disabled", pd.getTitle());
        assertEquals("The requested feature is not currently available", pd.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/feature-disabled"), pd.getType());
        assertEquals(false, pd.getProperties().get("retriable"));
        assertNotNull(pd.getInstance());
        assertTrue(r.getHeaders().getContentType().toString().contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void handleMissingHeader_false() {
        MissingRequestHeaderException ex = new MissingRequestHeaderException("TYPE");
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleMissingRequestHeaderException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
        ProblemDetail pd = r.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), pd.getStatus());
        assertEquals("Missing Required Header", pd.getTitle());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/missing-header"), pd.getType());
        assertEquals(false, pd.getProperties().get("retriable"));
    }

    @Test
    void handleForbidden_false() {
        PermissionNotAllowedException ex = new PermissionNotAllowedException(FinesPermission.ACCOUNT_ENQUIRY);
        HttpServletRequest req = new MockHttpServletRequest();

        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handlePermissionNotAllowedException(ex, req);
        assertEquals(HttpStatus.FORBIDDEN, r.getStatusCode());
        ProblemDetail pd = r.getBody();
        assertEquals(HttpStatus.FORBIDDEN.value(), pd.getStatus());
        assertEquals("Forbidden", pd.getTitle());
        assertEquals(false, pd.getProperties().get("retriable"));
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, r.getHeaders().getContentType());
    }

    @Test
    void handleNotAcceptable_false() {
        ResponseEntity<ProblemDetail> r =
            globalExceptionHandler.handleHttpMediaTypeNotAcceptableException(
                new HttpMediaTypeNotAcceptableException("nope"));

        assertEquals(HttpStatus.NOT_ACCEPTABLE, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleTypeMismatch_false() throws Exception {
        Object invalidValue = "x";
        Class<?> requiredType = Integer.class;
        String name = "n";
        Method m = GlobalExceptionHandlerTest.class.getMethod("sampleMethod", Integer.class);
        MethodParameter mp = new MethodParameter(m, 0);

        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
            invalidValue, requiredType, name, mp, new NumberFormatException("bad"));

        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleMethodArgumentTypeMismatchException(ex);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    @Test
    void handlePropertyValue_false() {
        PropertyValueException ex = new PropertyValueException("msg", "entity", "prop");
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handlePropertyValueException(ex);
        ProblemDetail pd = r.getBody();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        assertEquals("entity", pd.getProperties().get("entity"));
        assertEquals("prop", pd.getProperties().get("property"));
        assertEquals(false, pd.getProperties().get("retriable"));
    }

    @Test
    void handleUnsupportedMediaType_false() {
        ResponseEntity<ProblemDetail> r = globalExceptionHandler
            .handleHttpMediaTypeNotSupportedException(new HttpMediaTypeNotSupportedException("bad"));
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleMessageNotReadable_false() {
        HttpInputMessage msg = Mockito.mock(HttpInputMessage.class);
        ResponseEntity<ProblemDetail> r = globalExceptionHandler
            .handleHttpMessageNotReadableException(new HttpMessageNotReadableException("x", msg));
        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleInvalidDataAccessApiUsage_false() {
        ResponseEntity<ProblemDetail> r = globalExceptionHandler
            .handleInvalidDataAccessApiUsageException(
                new InvalidDataAccessApiUsageException("bad", new Throwable("root")));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleInvalidDataAccessResourceUsage_false() {
        ResponseEntity<ProblemDetail> r = globalExceptionHandler
            .handleInvalidDataAccessResourceUsageException(
                new InvalidDataAccessResourceUsageException("bad"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleEntityNotFound_false() {
        ResponseEntity<ProblemDetail> r = globalExceptionHandler
            .handleEntityNotFoundException(new EntityNotFoundException("nf"));
        assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleNoSuchElement_false() {
        ResponseEntity<ProblemDetail> r = globalExceptionHandler
            .handleNoSuchElementException(new NoSuchElementException("none"));
        assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleOpalApi_false() {
        OpalApiException ex = new OpalApiException(AuthenticationError.FAILED_TO_OBTAIN_AUTHENTICATION_CONFIG);
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleOpalApiException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    // ---------- Servlet/Transaction/Persistence buckets ----------

    @Test
    void handleServlet_queryTimeout_true() {
        QueryTimeoutException ex = new QueryTimeoutException("q", null, null);
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleServletExceptions(ex);

        assertEquals(HttpStatus.REQUEST_TIMEOUT, r.getStatusCode());
        assertEquals(true, r.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleServlet_noResource_false() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/x");
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleServletExceptions(ex);
        assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleServlet_txTransient_true() {
        // underlying cause -> transient PSQLState (deadlock 40P01)
        TransactionSystemException ex = new TransactionSystemException(
            "tx", new PSQLException("deadlock", PSQLState.DEADLOCK_DETECTED));
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleServletExceptions(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        assertEquals(true, r.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleServlet_txNonTransient_false() {
        // underlying cause -> non-transient PSQLState (syntax error)
        TransactionSystemException ex = new TransactionSystemException(
            "tx", new PSQLException("syntax", PSQLState.SYNTAX_ERROR));
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleServletExceptions(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleServlet_otherPersistence_false() {
        ResponseEntity<ProblemDetail> r = globalExceptionHandler
            .handleServletExceptions(new PersistenceException("oops"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    // ---------- PostgreSQL buckets ----------

    @Test
    void handlePsql_connectivity_true() throws Exception {
        PSQLException ex = new PSQLException("db down", PSQLState.CONNECTION_FAILURE,
                                             new ConnectException("refused"));
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handlePsqlException(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, r.getStatusCode());
        assertEquals(true, r.getBody().getProperties().get("retriable"));
        assertEquals(URI.create("https://hmcts.gov.uk/problems/database-unavailable"), r.getBody().getType());
    }

    @Test
    void handlePsql_other_false() {
        PSQLException ex = new PSQLException("db err", PSQLState.UNEXPECTED_ERROR, new Throwable("t"));
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handlePsqlException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
        assertEquals(URI.create("https://hmcts.gov.uk/problems/database-error"), r.getBody().getType());
    }

    // ---------- DataAccess ----------

    @Test
    void handleDataAccessResourceFailure_true() {
        DataAccessResourceFailureException ex = new DataAccessResourceFailureException("down");
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleDataAccessResourceFailureException(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, r.getStatusCode());
        assertEquals(true, r.getBody().getProperties().get("retriable"));
    }

    // ---------- Lazy & JPA ----------

    @Test
    void handleLazy_false() {
        ResponseEntity<ProblemDetail> r = globalExceptionHandler
            .handleLazyInitializationException(new LazyInitializationException("lazy"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleJpa_transient_true() {
        // most specific cause -> SERIALIZATION_FAILURE (40001) -> true
        PSQLException psql = new PSQLException("serial", PSQLState.SERIALIZATION_FAILURE);
        JpaSystemException ex = new JpaSystemException(new RuntimeException("wrap", psql));
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleJpaSystemException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        assertEquals(true, r.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleJpa_nonTransient_false() {
        JpaSystemException ex = new JpaSystemException(new RuntimeException("plain"));
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleJpaSystemException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    // ---------- HttpServerErrorException (always respond 500) ----------

    @Test
    void handleHttpServerError_forced500_retriableFalse() {
        HttpServerErrorException ex = new HttpServerErrorException(HttpStatusCode.valueOf(404), "Not Found!");
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleHttpServerErrorException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        ProblemDetail pd = r.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), pd.getStatus());
        assertEquals("Downstream Server Error", pd.getTitle());
        assertEquals("404 Not Found!", pd.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/http-server-error"), pd.getType());
        assertEquals(false, pd.getProperties().get("retriable"));
        assertNotNull(pd.getInstance());
        assertTrue(r.getHeaders().getContentType().toString().contains(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void handleHttpServerError_forced500_retriableTrueOn503() {
        HttpServerErrorException ex = new HttpServerErrorException(HttpStatusCode.valueOf(503), "Service Unavailable");
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleHttpServerErrorException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        assertEquals(true, r.getBody().getProperties().get("retriable")); // 503 -> true
    }

    // ---------- JSON schema & IllegalArgument ----------

    @Test
    void handleJsonSchema_false() {
        ResponseEntity<ProblemDetail> r = globalExceptionHandler
            .handleJsonSchemaValidationException(new JsonSchemaValidationException("bad schema"));
        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    @Test
    void handleIllegalArgument_false() {
        ResponseEntity<ProblemDetail> r = globalExceptionHandler
            .handleIllegalArgumentException(new IllegalArgumentException("bad arg"));
        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    // ---------- Conflicts ----------

    @Test
    void handleOptimisticLock_false() {
        ObjectOptimisticLockingFailureException ex =
            new ObjectOptimisticLockingFailureException(DraftAccountEntity.class, "123");
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleObjectOptimisticLockingFailureException(ex);

        assertEquals(HttpStatus.CONFLICT, r.getStatusCode());
        ProblemDetail pd = r.getBody();
        assertEquals(false, pd.getProperties().get("retriable"));
        assertEquals(DraftAccountEntity.class.getName(), pd.getProperties().get("resourceType"));
        assertEquals("123", pd.getProperties().get("resourceId"));
    }

    @Test
    void handleResourceConflict_false() {
        ResourceConflictException ex = new ResourceConflictException("DraftAccount", "123", "BU mismatch", null);
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleResourceConflictException(ex);

        assertEquals(HttpStatus.CONFLICT, r.getStatusCode());
        ProblemDetail pd = r.getBody();
        assertEquals(false, pd.getProperties().get("retriable"));
        assertEquals("DraftAccount", pd.getProperties().get("resourceType"));
        assertEquals("123", pd.getProperties().get("resourceId"));
        assertEquals("BU mismatch", pd.getProperties().get("conflictReason"));
        assertNull(r.getHeaders().getETag());
    }

    @Test
    void handleResourceConflict_withVersioned() {
        ResourceConflictException ex = new ResourceConflictException(
            "DraftAccount", "123", "BU mismatch", () -> BigInteger.valueOf(666));
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleResourceConflictException(ex);

        assertEquals(HttpStatus.CONFLICT, r.getStatusCode());
        ProblemDetail pd = r.getBody();
        assertEquals(false, pd.getProperties().get("retriable"));
        assertEquals("DraftAccount", pd.getProperties().get("resourceType"));
        assertEquals("123", pd.getProperties().get("resourceId"));
        assertEquals("BU mismatch", pd.getProperties().get("conflictReason"));
        assertEquals("\"666\"", r.getHeaders().getETag());
    }

    @Test
    void handleUnprocessableException() {
        UnprocessableException ex = new UnprocessableException("Too many results");
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleUnprocessableException(ex);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, r.getStatusCode());
        ProblemDetail pd = r.getBody();
        assertEquals(false, pd.getProperties().get("retriable"));
        assertEquals("Too many results", pd.getProperties().get("unprocessableReason"));
        assertNull(r.getHeaders().getETag());
    }

    @Test
    void handleSubmitterCannotValidate_forbidden() {
        SubmitterCannotValidateException ex =
            new SubmitterCannotValidateException("A single user cannot submit and validate the same Draft Account");
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleSubmitterCannotValidateException(ex);

        assertEquals(HttpStatus.FORBIDDEN, r.getStatusCode());
        ProblemDetail pd = r.getBody();
        assertEquals(HttpStatus.FORBIDDEN.value(), pd.getStatus());
        assertEquals("Submitter cannot validate", pd.getTitle());
        assertEquals("A single user cannot submit and validate the same Draft Account", pd.getDetail());
        assertEquals(URI.create("https://hmcts.gov.uk/problems/submitter-cannot-validate"), pd.getType());
        assertEquals(false, pd.getProperties().get("retriable"));
    }

    // ---------- FeignException (generic handler) ----------

    @Test
    void handleFeign_generic_503_retriableTrue() {
        FeignException ex = buildFeignException(503, "Service Unavailable");
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleFeignException(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, r.getStatusCode());
        assertEquals(true, r.getBody().getProperties().get("retriable"));
        assertEquals("Downstream Service Error", r.getBody().getTitle());
    }

    @Test
    void handleFeign_generic_500_retriableFalse() {
        FeignException ex = buildFeignException(500, "Boom");
        ResponseEntity<ProblemDetail> r = globalExceptionHandler.handleFeignException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        assertEquals(false, r.getBody().getProperties().get("retriable"));
    }

    // ---------- util ----------

    public static void sampleMethod(Integer testParam) {
        // no-op
    }

    private static FeignException buildFeignException(int status, String reason) {
        Map<String, Collection<String>> headers = Collections.emptyMap();

        Request request = Request.create(
            Request.HttpMethod.GET,
            "/test",
            headers,
            null,
            StandardCharsets.UTF_8,
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
