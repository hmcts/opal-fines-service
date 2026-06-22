package uk.gov.hmcts.opal.steps.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import uk.gov.hmcts.opal.context.ScenarioContextHolder;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;
import uk.gov.hmcts.opal.utils.TestHttpClient;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.gov.hmcts.opal.config.Constants.REPORT_INSTANCES_URI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    private static final String SEEDED_REPORT_INSTANCES_QUERY =
        "?report_id=fp_register&business_units=77&from_date=2026-05-10&to_date=2026-05-30&user_id=12345678";
    private static final String FORBIDDEN_REPORT_INSTANCES_QUERY =
        "?report_id=fp_register&business_units=999&from_date=2026-05-10&to_date=2026-05-30&user_id=12345678";
    private static final Set<String> VALID_SUPPORTED_FILE_TYPES = Set.of("CSV", "PDF", "XML");

    private TestHttpResponse latestRawReportInstanceResponse;
    private TestHttpResponse latestRawGetReportInstanceResponse;


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
     * Calls the get-report-instance endpoint using the requested authentication state.
     *
     * @param instanceId report instance id to request.
     * @param authenticationState whether the request should be sent with no token or an invalid
     *                            token.
     */
    @When("I call GET on the report instance api for id {int} with {string}")
    public void callGetOnTheReportInstanceApiWithAuthenticationState(int instanceId, String authenticationState) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "*/*");

        switch (authenticationState) {
            case "no token" -> {
                // leave Authorization absent
            }
            case "invalid token" -> headers.put("Authorization", "Bearer invalidToken");
            default -> throw new IllegalArgumentException(
                "Unknown authentication state for get report instance request: " + authenticationState
            );
        }

        latestRawGetReportInstanceResponse = TestHttpClient.request(
            "GET",
            getTestUrl() + REPORT_INSTANCES_URI + "/" + instanceId,
            headers,
            null
        );
        ScenarioContextHolder.current().setLatestHttpResponse(latestRawGetReportInstanceResponse);
    }

    /**
     * Requests the supplied report instance id using the current scenario user.
     *
     * @param instanceId report instance id to request.
     */
    @When("I request report instance with id {int}")
    public void requestReportInstanceWithId(int instanceId) {
        latestRawGetReportInstanceResponse = TestHttpClient.request(
            "GET",
            getTestUrl() + REPORT_INSTANCES_URI + "/" + instanceId,
            Map.of(
                "Accept", "*/*",
                "Authorization", "Bearer " + uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken()
            ),
            null
        );
        ScenarioContextHolder.current().setLatestHttpResponse(latestRawGetReportInstanceResponse);
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
     * Requests report instances using the supplied GET query filters.
     *
     * @param filtersData Cucumber table containing filter query parameter names and values.
     */
    @When("I request report instances with the following filters")
    public void requestReportInstancesWithFilters(DataTable filtersData) {
        Map<String, String> filters = filtersData.asMap(String.class, String.class);
        String queryString = filters.entrySet().stream()
            .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
            .map(entry -> encodeQueryParameter(entry.getKey()) + "=" + encodeQueryParameter(entry.getValue()))
            .collect(Collectors.joining("&"));
        String requestUrl = getTestUrl() + REPORT_INSTANCES_URI + "?" + queryString;

        latestRawReportInstanceResponse = TestHttpClient.get(
            requestUrl,
            Map.of(
                "Accept", "application/json",
                "Authorization", "Bearer " + BearerTokenStepDef.getToken()
            )
        );
        ScenarioContextHolder.current().setLatestHttpResponse(latestRawReportInstanceResponse);
    }

    /**
     * Asserts that the get-report-instance response contains the expected field values.
     *
     * @param data Cucumber table containing the expected values for the assertion.
     */
    @Then("the report instance response contains")
    public void reportInstanceResponseContains(io.cucumber.datatable.DataTable data) throws Exception {
        TestHttpResponse latestHttpResponse = latestRawGetReportInstanceResponse;
        latestRawGetReportInstanceResponse = null;

        assertNotNull(latestHttpResponse, "Expected a raw HTTP response for the latest get report instance request");
        assertEquals(200, latestHttpResponse.statusCode(), "Unexpected HTTP status");

        JsonNode root = OBJECT_MAPPER.readTree(latestHttpResponse.body());
        Map<String, String> expected = data.asMap(String.class, String.class);

        for (Map.Entry<String, String> entry : expected.entrySet()) {
            String fieldName = entry.getKey();
            String expectedValue = entry.getValue();
            JsonNode node = root.path(fieldName);

            assertFalse(node.isMissingNode(), "Missing field in response: " + fieldName);
            assertEquals(expectedValue, node.asText(), "Unexpected value for field: " + fieldName);
        }
    }

    /**
     * Sends a report-instances request containing business-unit data the current user cannot
     * access.
     */
    @When("send request to api with data which has lack of permissions")
    public void sendRequestToApiWithDataWhichHasLackOfPermissions() {
        retrieveForbiddenReportInstancesForCurrentUser();
    }

    /**
     * Attempts to retrieve report instances without an Authorization header.
     */
    @And("I attempt to retrieve the report instances without a token")
    public void attemptToRetrieveReportInstancesWithoutToken() {
        retrieveReportInstances(Map.of("Accept", "application/json"));
    }

    /**
     * Attempts to retrieve report instances with an invalid bearer token.
     */
    @And("I attempt to retrieve the report instances with an invalid token")
    public void attemptToRetrieveReportInstancesWithInvalidToken() {
        retrieveReportInstances(
            Map.of(
                "Accept", "application/json",
                "Authorization", "Bearer invalidToken"
            )
        );
    }

    /**
     * Asserts that the latest report-instance list response returned 200 OK.
     */
    @Then("the report instances response status is 200 OK")
    public void reportInstancesResponseStatusIsOk() {
        TestHttpResponse latestHttpResponse = requireLatestReportInstanceResponse();

        assertEquals(200, latestHttpResponse.statusCode(), "Unexpected HTTP status");
    }

    /**
     * Asserts that each returned report instance matches the requested filters.
     *
     * @param filtersData Cucumber table containing the filters that should match every row.
     */
    @Then("only report instances matching the following filters are returned")
    public void onlyReportInstancesMatchingFiltersAreReturned(DataTable filtersData) throws Exception {
        JsonNode root = readReportInstanceListResponse();
        Map<String, String> filters = filtersData.asMap(String.class, String.class);

        assertFalse(root.isEmpty(), "Expected at least one report instance");
        for (JsonNode reportInstance : root) {
            assertReportInstanceMatchesFilters(reportInstance, filters);
        }
    }

    /**
     * Asserts that the report-instance list response contains a row with the supplied values.
     *
     * @param data Cucumber table containing expected JSON paths and values.
     */
    @Then("the report instances response contains the following data")
    public void reportInstancesResponseContainsTheFollowingData(DataTable data) throws Exception {
        JsonNode root = readReportInstanceListResponse();
        Map<String, String> expectedData = data.asMap(String.class, String.class);

        JsonNode matchingReportInstance = findReportInstanceContaining(root, expectedData);
        assertNotNull(matchingReportInstance, "No report instance matched the expected response data");
    }

    /**
     * Asserts that no matching report instances returns HTTP 200 and an empty JSON array.
     */
    @Then("200 for no matching report instances with an empty array.")
    public void noMatchingReportInstancesReturnsEmptyArray() throws Exception {
        JsonNode root = readReportInstanceListResponse();

        assertEquals(0, root.size(), "Expected no report instances to be returned");
    }

    /**
     * Asserts that a READY report instance with valid supported file types is downloadable.
     */
    @And("instance with status READY and valid supported file types is marked downloadable")
    public void instanceWithStatusReadyAndValidSupportedFileTypesIsMarkedDownloadable() throws Exception {
        JsonNode root = readReportInstanceListResponse();

        for (JsonNode reportInstance : root) {
            if (isReadyWithValidSupportedFileTypes(reportInstance)) {
                JsonNode isDownloadable = reportInstance.path("is_downloadable");
                assertTrue(isDownloadable.isBoolean(), "is_downloadable should be a boolean");
                assertTrue(isDownloadable.asBoolean(),
                           "READY report instance with supported file types should be downloadable");
                return;
            }
        }

        fail("Expected a READY report instance with valid supported file types");
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
     * Asserts that the latest no-token get-report-instance response is an unauthorised response,
     * tolerating both the local plain-text security-layer message and the deployed problem-detail
     * shape returned in some environments.
     */
    @Then("the latest get report instance response is an unauthorized response")
    public void latestGetReportInstanceResponseIsAnUnauthorizedResponse() throws Exception {
        TestHttpResponse latestHttpResponse = latestRawGetReportInstanceResponse;
        latestRawGetReportInstanceResponse = null;

        assertNotNull(latestHttpResponse, "Expected a raw HTTP response for the latest get report instance request");
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
     * Asserts that the latest get-report-instance error response matches the shared ProblemDetail
     * top-level contract for the expected status.
     *
     * @param expectedStatus expected HTTP status code.
     */
    @Then("the latest get report instance error response matches the standard problem detail contract for status {int}")
    public void latestGetReportInstanceErrorResponseMatchesProblemDetailContract(int expectedStatus)
        throws Exception {
        TestHttpResponse latestHttpResponse = latestRawGetReportInstanceResponse;
        latestRawGetReportInstanceResponse = null;

        assertNotNull(latestHttpResponse, "Expected a raw HTTP response for the latest get report instance request");
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

    private TestHttpResponse requireLatestReportInstanceResponse() {
        assertNotNull(latestRawReportInstanceResponse,
                      "Expected a raw HTTP response for the latest report instance request");
        return latestRawReportInstanceResponse;
    }

    private void retrieveReportInstances(Map<String, String> headers) {
        latestRawReportInstanceResponse = TestHttpClient.get(
            getTestUrl() + REPORT_INSTANCES_URI + SEEDED_REPORT_INSTANCES_QUERY,
            headers
        );
        ScenarioContextHolder.current().setLatestHttpResponse(latestRawReportInstanceResponse);
    }

    private void retrieveForbiddenReportInstancesForCurrentUser() {
        authorisedJsonRequest()
            .when()
            .get(getTestUrl() + REPORT_INSTANCES_URI + FORBIDDEN_REPORT_INSTANCES_QUERY);
    }

    private JsonNode readReportInstanceListResponse() throws Exception {
        TestHttpResponse latestHttpResponse = requireLatestReportInstanceResponse();

        assertEquals(200, latestHttpResponse.statusCode(), "Unexpected HTTP status");
        JsonNode root = OBJECT_MAPPER.readTree(latestHttpResponse.body());
        assertTrue(root.isArray(), "Report instances response should be a JSON array");
        return root;
    }

    private boolean isReadyWithValidSupportedFileTypes(JsonNode reportInstance) {
        if (!"READY".equals(reportInstance.path("status").path("code").asText())) {
            return false;
        }

        JsonNode supportedFileTypes = reportInstance.path("supported_file_types");
        assertTrue(supportedFileTypes.isArray(), "supported_file_types should be an array");

        if (supportedFileTypes.isEmpty()) {
            return false;
        }

        for (JsonNode supportedFileType : supportedFileTypes) {
            assertTrue(supportedFileType.isTextual(), "supported_file_types entries should be strings");
            assertTrue(
                VALID_SUPPORTED_FILE_TYPES.contains(supportedFileType.asText()),
                "supported_file_types contains an unsupported value: " + supportedFileType.asText()
            );
        }

        return true;
    }

    private void assertReportInstanceMatchesFilters(JsonNode reportInstance, Map<String, String> filters) {
        if (filters.containsKey("report_id")) {
            assertEquals(filters.get("report_id"), getRequiredText(reportInstance, "report_id"),
                         "Unexpected report_id");
        }

        if (filters.containsKey("user_id")) {
            assertEquals(filters.get("user_id"), getRequiredText(reportInstance, "requested_by.user_id"),
                         "Unexpected requested_by.user_id");
        }

        if (filters.containsKey("business_units")) {
            for (String businessUnitId : filters.get("business_units").split(",")) {
                assertBusinessUnitsContain(reportInstance, businessUnitId.trim());
            }
        }

        if (filters.containsKey("from_date")) {
            LocalDate generatedDate = getGeneratedDate(reportInstance);
            LocalDate fromDate = LocalDate.parse(filters.get("from_date"));
            assertFalse(generatedDate.isBefore(fromDate), "generated_at is before from_date");
        }

        if (filters.containsKey("to_date")) {
            LocalDate generatedDate = getGeneratedDate(reportInstance);
            LocalDate toDate = LocalDate.parse(filters.get("to_date"));
            assertFalse(generatedDate.isAfter(toDate), "generated_at is after to_date");
        }
    }

    private void assertBusinessUnitsContain(JsonNode reportInstance, String expectedBusinessUnitId) {
        JsonNode businessUnits = readPath(reportInstance, "business_units");
        assertTrue(businessUnits.isArray(), "business_units should be a JSON array");

        boolean businessUnitFound = false;
        for (JsonNode businessUnit : businessUnits) {
            if (expectedBusinessUnitId.equals(businessUnit.path("business_unit_id").asText())) {
                businessUnitFound = true;
                break;
            }
        }

        assertTrue(businessUnitFound, "Expected business_units to contain " + expectedBusinessUnitId);
    }

    private LocalDate getGeneratedDate(JsonNode reportInstance) {
        String generatedAt = getRequiredText(reportInstance, "generated_at");

        assertTrue(generatedAt.length() >= 10, "generated_at should include a date");
        return LocalDate.parse(generatedAt.substring(0, 10));
    }

    private JsonNode findReportInstanceContaining(JsonNode root, Map<String, String> expectedData) {
        for (JsonNode reportInstance : root) {
            boolean reportInstanceMatches = true;

            for (Map.Entry<String, String> entry : expectedData.entrySet()) {
                JsonNode actualNode = readPath(reportInstance, entry.getKey());
                if (actualNode.isMissingNode() || actualNode.isNull()
                    || !entry.getValue().equals(actualNode.asText())) {
                    reportInstanceMatches = false;
                    break;
                }
            }

            if (reportInstanceMatches) {
                return reportInstance;
            }
        }

        return null;
    }

    private String getRequiredText(JsonNode root, String path) {
        JsonNode node = readPath(root, path);

        assertTrue(!node.isMissingNode() && !node.isNull(), "Missing response field: " + path);
        return node.asText();
    }

    private JsonNode readPath(JsonNode root, String path) {
        JsonNode current = root;

        for (String segment : path.split("\\.")) {
            current = readPathSegment(current, segment);
            if (current.isMissingNode() || current.isNull()) {
                return current;
            }
        }

        return current;
    }

    private JsonNode readPathSegment(JsonNode root, String segment) {
        int firstArrayIndex = segment.indexOf('[');
        if (firstArrayIndex == -1) {
            return root.path(segment);
        }

        String fieldName = segment.substring(0, firstArrayIndex);
        JsonNode current = fieldName.isBlank() ? root : root.path(fieldName);
        int arrayIndexStart = firstArrayIndex;

        while (arrayIndexStart != -1) {
            int arrayIndexEnd = segment.indexOf(']', arrayIndexStart);
            assertTrue(arrayIndexEnd > arrayIndexStart + 1, "Invalid JSON path segment: " + segment);

            int arrayIndex = Integer.parseInt(segment.substring(arrayIndexStart + 1, arrayIndexEnd));
            current = current.path(arrayIndex);
            arrayIndexStart = segment.indexOf('[', arrayIndexEnd + 1);
        }

        return current;
    }

    private String encodeQueryParameter(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}
