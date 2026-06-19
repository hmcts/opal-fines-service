package uk.gov.hmcts.opal.steps.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import uk.gov.hmcts.opal.context.ScenarioContextHolder;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.utils.TestHttpClient;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.gov.hmcts.opal.config.Constants.REPORT_INSTANCES_URI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReportInstanceStepDef extends BaseStepDef {

    private static final Logger log =
            LoggerFactory.getLogger(ReportInstanceStepDef.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String MINIMAL_CREATE_REQUEST = """
        {
          "report_id": "test-report-id",
          "business_unit_ids": [73]
        }
        """;

    private TestHttpResponse latestRawReportInstanceResponse;


    /**
     * Calls the report-instances create endpoint using the requested authentication state.
     *
     * @param authenticationState whether the request should be sent with no token or an invalid
     *                            token.
     */
    @When("I call POST on the report instances api with {string}")
    public void callPostOnTheReportInstancesApiWithAuthenticationState(String authenticationState) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "*/*");
        headers.put("Content-Type", "application/json");

        switch (authenticationState) {
            case "no token" -> {
                // leave Authorization absent
            }
            case "invalid token" -> headers.put("Authorization", "Bearer invalidToken");
            default -> throw new IllegalArgumentException(
                "Unknown authentication state for report instance request: " + authenticationState
            );
        }

        latestRawReportInstanceResponse = TestHttpClient.request(
            "POST",
            getTestUrl() + REPORT_INSTANCES_URI,
            headers,
            MINIMAL_CREATE_REQUEST
        );
        ScenarioContextHolder.current().setLatestHttpResponse(latestRawReportInstanceResponse);
    }

    /**
     * Creates a report-instance request for the supplied report id using the current scenario user.
     *
     * @param reportId report id to send in the create request.
     */
    @When("I create a report instance with report id {string}")
    public void createReportInstanceWithReportId(String reportId) {
        String requestBody = """
            {
              "report_id": "%s",
              "business_unit_ids": [73]
            }
            """.formatted(reportId);

        latestRawReportInstanceResponse = TestHttpClient.request(
            "POST",
            getTestUrl() + REPORT_INSTANCES_URI,
            Map.of(
                "Accept", "*/*",
                "Content-Type", "application/json",
                "Authorization", "Bearer " + uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken()
            ),
            requestBody
        );
        ScenarioContextHolder.current().setLatestHttpResponse(latestRawReportInstanceResponse);
    }

    /**
     * Creates a report-instance request for the supplied report id and two business units using
     * the current scenario user.
     *
     * @param reportId report id to send in the create request.
     * @param firstBusinessUnitId first business unit id to send in the create request.
     * @param secondBusinessUnitId second business unit id to send in the create request.
     */
    @When("I create a report instance with report id {string} for business units {int} and {int}")
    public void createReportInstanceWithReportIdForBusinessUnits(
        String reportId,
        int firstBusinessUnitId,
        int secondBusinessUnitId
    ) {
        String requestBody = """
            {
              "report_id": "%s",
              "business_unit_ids": [%d, %d]
            }
            """.formatted(reportId, firstBusinessUnitId, secondBusinessUnitId);

        latestRawReportInstanceResponse = TestHttpClient.request(
            "POST",
            getTestUrl() + REPORT_INSTANCES_URI,
            Map.of(
                "Accept", "*/*",
                "Content-Type", "application/json",
                "Authorization", "Bearer " + uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken()
            ),
            requestBody
        );
        ScenarioContextHolder.current().setLatestHttpResponse(latestRawReportInstanceResponse);
    }

    /**
     * Creates a report-instance request for the supplied report id and one business unit using the
     * current scenario user.
     *
     * @param reportId report id to send in the create request.
     * @param businessUnitId business unit id to send in the create request.
     */
    @When("I create a report instance with report id {string} for business unit {int}")
    public void createReportInstanceWithReportIdForBusinessUnit(String reportId, int businessUnitId) {
        String requestBody = """
            {
              "report_id": "%s",
              "business_unit_ids": [%d]
            }
            """.formatted(reportId, businessUnitId);

        latestRawReportInstanceResponse = TestHttpClient.request(
            "POST",
            getTestUrl() + REPORT_INSTANCES_URI,
            Map.of(
                "Accept", "*/*",
                "Content-Type", "application/json",
                "Authorization", "Bearer " + uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken()
            ),
            requestBody
        );
        log.info("Base URL: {}", getTestUrl());
        log.info("Report ID: {}", reportId);
        log.info("Business Unit ID: {}", businessUnitId);
        log.info("Report instance request body: {}", requestBody);
        log.info("Report instance response status: {}", latestRawReportInstanceResponse.statusCode());
        log.info("Report instance response body: {}", latestRawReportInstanceResponse.body());
        ScenarioContextHolder.current().setLatestHttpResponse(latestRawReportInstanceResponse);
    }

    /**
     * Creates a report-instance request for the supplied report id with an amendment-date report
     * parameter using the current scenario user.
     *
     * @param reportId report id to send in the create request.
     * @param amendmentDate amendment-date value to send in report_parameters.
     */
    @When("I create a report instance with report id {string} and amendment date {string}")
    public void createReportInstanceWithReportIdAndAmendmentDate(String reportId, String amendmentDate) {
        String requestBody = """
            {
              "report_id": "%s",
              "business_unit_ids": [73],
              "report_parameters": {
                "amendment_date": "%s"
              }
            }
            """.formatted(reportId, amendmentDate);

        latestRawReportInstanceResponse = TestHttpClient.request(
            "POST",
            getTestUrl() + REPORT_INSTANCES_URI,
            Map.of(
                "Accept", "*/*",
                "Content-Type", "application/json",
                "Authorization", "Bearer " + uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken()
            ),
            requestBody
        );
        ScenarioContextHolder.current().setLatestHttpResponse(latestRawReportInstanceResponse);
    }

    /**
     * Asserts that the latest report-instance create error response is marked retriable.
     */
    @Then("the latest report instance create error response is retriable")
    public void latestReportInstanceCreateErrorResponseIsRetriable() throws Exception {
        TestHttpResponse latestHttpResponse = latestRawReportInstanceResponse;
        latestRawReportInstanceResponse = null;

        assertNotNull(latestHttpResponse, "Expected a raw HTTP response for the latest report instance request");
        assertEquals(422, latestHttpResponse.statusCode(), "Unexpected HTTP status");

        JsonNode root = OBJECT_MAPPER.readTree(latestHttpResponse.body());
        assertTrue(root.path("retriable").isBoolean(), "retriable should be a boolean");
        assertTrue(root.path("retriable").asBoolean(), "retriable should be true");
    }

    /**
     * Asserts that the create-report-instance response contains a numeric report_instance_id.
     */
    @Then("the report instance create response contains a report instance id")
    public void reportInstanceCreateResponseContainsAReportInstanceId() throws Exception {
        TestHttpResponse latestHttpResponse = latestRawReportInstanceResponse;
        latestRawReportInstanceResponse = null;

        assertNotNull(latestHttpResponse, "Expected a raw HTTP response for the latest report instance request");

        assertEquals(201, latestHttpResponse.statusCode(), "Unexpected HTTP status");
        JsonNode root = OBJECT_MAPPER.readTree(latestHttpResponse.body());
        assertTrue(root.path("report_instance_id").isIntegralNumber(),
                   "report_instance_id should be a numeric value");
    }

    /**
     * Asserts that the latest no-token report-instance create response is an unauthorised response,
     * tolerating both the local plain-text security-layer message and the deployed problem-detail
     * shape returned in some environments.
     */
    @Then("the latest report instance create response is an unauthorized response")
    public void latestReportInstanceCreateResponseIsAnUnauthorizedResponse() throws Exception {
        TestHttpResponse latestHttpResponse = latestRawReportInstanceResponse;
        latestRawReportInstanceResponse = null;

        assertNotNull(latestHttpResponse, "Expected a raw HTTP response for the latest report instance request");
        assertEquals(401, latestHttpResponse.statusCode(), "Unexpected HTTP status");

        String body = latestHttpResponse.body();
        if (body != null && body.trim().startsWith("{")) {
            validateProblemDetailResponse(body, 401);
            return;
        }

        assertTrue(
            body != null && body.contains("Full authentication is required"),
            "Unexpected no-token response body"
        );
    }

    /**
     * Asserts that the latest report-instance create error response matches the shared
     * ProblemDetail top-level contract for the expected status.
     *
     * @param expectedStatus expected HTTP status code.
     */
    @Then("latest report instance create error response matches the standard problem detail contract for status {int}")
    public void latestReportInstanceCreateErrorResponseMatchesProblemDetailContract(int expectedStatus)
        throws Exception {
        TestHttpResponse latestHttpResponse = latestRawReportInstanceResponse;
        latestRawReportInstanceResponse = null;

        assertNotNull(latestHttpResponse, "Expected a raw HTTP response for the latest report instance request");
        assertEquals(expectedStatus, latestHttpResponse.statusCode(), "Unexpected HTTP status");
        validateProblemDetailResponse(latestHttpResponse.body(), expectedStatus);
    }

    /**
     * Validates the shared ProblemDetail response shape used by report-instance error paths.
     *
     * @param body raw JSON response body.
     * @param expectedStatus expected HTTP status code in the response body.
     */
    private void validateProblemDetailResponse(String body, int expectedStatus) throws Exception {
        JsonNode root = OBJECT_MAPPER.readTree(body);

        assertTrue(root.isObject(), "Problem detail response should be a JSON object");
        assertTrue(root.path("title").isTextual(), "title should be a string");
        assertTrue(root.path("detail").isTextual(), "detail should be a string");
        assertTrue(root.path("status").isInt(), "status should be an integer");
        assertEquals(expectedStatus, root.path("status").asInt(), "Unexpected status in response body");

        if (!root.path("type").isMissingNode() && !root.path("type").isNull()) {
            assertTrue(root.path("type").isTextual(), "type should be a string when present");
        }

        if (!root.path("instance").isMissingNode() && !root.path("instance").isNull()) {
            assertTrue(root.path("instance").isTextual(), "instance should be a string when present");
        }

        if (!root.path("operation_id").isMissingNode() && !root.path("operation_id").isNull()) {
            assertTrue(root.path("operation_id").isTextual(), "operation_id should be a string when present");
        }

        if (!root.path("retriable").isMissingNode() && !root.path("retriable").isNull()) {
            assertTrue(root.path("retriable").isBoolean(), "retriable should be a boolean when present");
        }
    }
}
