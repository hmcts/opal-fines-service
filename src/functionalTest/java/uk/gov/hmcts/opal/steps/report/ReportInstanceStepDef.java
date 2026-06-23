package uk.gov.hmcts.opal.steps.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.context.ScenarioContextHolder;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;
import uk.gov.hmcts.opal.utils.TestHttpClient;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.gov.hmcts.opal.config.Constants.REPORT_INSTANCES_URI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ReportInstanceStepDef extends BaseStepDef {

    private static final Logger log =
            LoggerFactory.getLogger(ReportInstanceStepDef.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String REPORT_PERMISSION_SELECT_SQL = """
        SELECT permission
        FROM public.reports
        WHERE report_id = ?
        """;
    private static final String REPORT_PERMISSION_UPDATE_SQL = """
        UPDATE public.reports
        SET permission = ?
        WHERE report_id = ?
        """;
    private static final String REPORT_SUPPORTED_FILE_TYPES_SELECT_SQL = """
        SELECT supported_file_types::text AS supported_file_types
        FROM public.reports
        WHERE report_id = ?
        """;
    private static final String REPORT_SUPPORTED_FILE_TYPES_UPDATE_SQL = """
        UPDATE public.reports
        SET supported_file_types = ?::public.r_supported_file_type_enum[]
        WHERE report_id = ?
        """;
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
    private final Map<String, String> originalReportPermissions = new LinkedHashMap<>();
    private final Map<String, String> originalReportSupportedFileTypes = new LinkedHashMap<>();


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
     * Updates a seeded report permission for the current scenario and restores it afterwards.
     *
     * @param reportId report id to update.
     * @param permission permission enum name to set.
     */
    @And("the report {string} has permission {string} in the database")
    public void reportHasPermissionInTheDatabase(String reportId, String permission) throws SQLException {
        FinesPermission.fromString(permission);

        if (!originalReportPermissions.containsKey(reportId)) {
            originalReportPermissions.put(reportId, readReportPermission(reportId));
        }

        updateReportPermission(reportId, permission);
    }

    /**
     * Updates a seeded report's supported file types for the current scenario and restores them
     * afterwards.
     *
     * @param reportId report id to update.
     * @param supportedFileTypes comma-separated supported file type enum names.
     */
    @And("the report {string} supports file types {string} in the database")
    public void reportSupportsFileTypesInTheDatabase(String reportId, String supportedFileTypes) throws SQLException {
        if (!originalReportSupportedFileTypes.containsKey(reportId)) {
            originalReportSupportedFileTypes.put(reportId, readReportSupportedFileTypes(reportId));
        }

        updateReportSupportedFileTypes(reportId, toSupportedFileTypesArrayLiteral(supportedFileTypes));
    }

    /**
     * Restores any report permissions changed for this scenario.
     */
    @After
    public void restoreReportPermissions() throws SQLException {
        for (Map.Entry<String, String> entry : originalReportPermissions.entrySet()) {
            updateReportPermission(entry.getKey(), entry.getValue());
        }
        originalReportPermissions.clear();

        for (Map.Entry<String, String> entry : originalReportSupportedFileTypes.entrySet()) {
            updateReportSupportedFileTypes(entry.getKey(), entry.getValue());
        }
        originalReportSupportedFileTypes.clear();
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
     * Attempts to retrieve report instances using the current scenario user's bearer token.
     */
    @And("I attempt to retrieve the report instances")
    public void attemptToRetrieveReportInstances() {
        retrieveForbiddenReportInstancesForCurrentUser();
    }

    /**
     * Retrieves report instances using filters that the current user cannot access.
     */
    @And("get the report with  user lacks permission in all relevant business units")
    public void getReportWithUserLacksPermissionInAllRelevantBusinessUnits() {
        retrieveForbiddenReportInstancesForCurrentUser();
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

        assertTrue(root.size() > 0, "Expected at least one report instance");
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

    private String readReportPermission(String reportId) throws SQLException {
        try (Connection connection = openDatabaseConnection();
             PreparedStatement statement = connection.prepareStatement(REPORT_PERMISSION_SELECT_SQL)) {
            statement.setString(1, reportId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("permission");
                }
            }
        }

        throw new IllegalArgumentException("No report found with report_id: " + reportId);
    }

    private String readReportSupportedFileTypes(String reportId) throws SQLException {
        try (Connection connection = openDatabaseConnection();
             PreparedStatement statement = connection.prepareStatement(REPORT_SUPPORTED_FILE_TYPES_SELECT_SQL)) {
            statement.setString(1, reportId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("supported_file_types");
                }
            }
        }

        throw new IllegalArgumentException("No report found with report_id: " + reportId);
    }

    private void updateReportPermission(String reportId, String permission) throws SQLException {
        try (Connection connection = openDatabaseConnection();
             PreparedStatement statement = connection.prepareStatement(REPORT_PERMISSION_UPDATE_SQL)) {
            if (permission == null) {
                statement.setNull(1, Types.VARCHAR);
            } else {
                statement.setString(1, permission);
            }
            statement.setString(2, reportId);

            int updatedRows = statement.executeUpdate();
            if (updatedRows != 1) {
                throw new IllegalArgumentException("Expected to update one report row but updated " + updatedRows);
            }
        }
    }

    private void updateReportSupportedFileTypes(String reportId, String supportedFileTypes) throws SQLException {
        try (Connection connection = openDatabaseConnection();
             PreparedStatement statement = connection.prepareStatement(REPORT_SUPPORTED_FILE_TYPES_UPDATE_SQL)) {
            if (supportedFileTypes == null) {
                statement.setNull(1, Types.VARCHAR);
            } else {
                statement.setString(1, supportedFileTypes);
            }
            statement.setString(2, reportId);

            int updatedRows = statement.executeUpdate();
            if (updatedRows != 1) {
                throw new IllegalArgumentException("Expected to update one report row but updated " + updatedRows);
            }
        }
    }

    private String toSupportedFileTypesArrayLiteral(String supportedFileTypes) {
        String fileTypes = Arrays.stream(supportedFileTypes.split(","))
            .map(String::trim)
            .filter(fileType -> !fileType.isBlank())
            .peek(this::validateSupportedFileType)
            .collect(Collectors.joining(","));

        if (fileTypes.isBlank()) {
            throw new IllegalArgumentException("At least one supported file type must be supplied");
        }

        return "{" + fileTypes + "}";
    }

    private void validateSupportedFileType(String fileType) {
        if (!VALID_SUPPORTED_FILE_TYPES.contains(fileType)) {
            throw new IllegalArgumentException("Unsupported report file type: " + fileType);
        }
    }

    private Connection openDatabaseConnection() throws SQLException {
        return DriverManager.getConnection(
            getFunctionalTestDatabaseUrl(),
            getEnvironmentValue("OPAL_FUNCTIONAL_TEST_DB_USERNAME",
                                getEnvironmentValue("OPAL_FINES_DB_USERNAME",
                                                    getEnvironmentValue("FLYWAY_USER", "opal-db-user"))),
            getEnvironmentValue("OPAL_FUNCTIONAL_TEST_DB_PASSWORD",
                                getEnvironmentValue("OPAL_FINES_DB_PASSWORD",
                                                    getEnvironmentValue("FLYWAY_PASSWORD", "opal-db-password")))
        );
    }

    private String getFunctionalTestDatabaseUrl() {
        String configuredUrl = getEnvironmentValue("OPAL_FUNCTIONAL_TEST_DB_URL",
                                                   getEnvironmentValue("FLYWAY_URL", null));
        if (configuredUrl != null) {
            return configuredUrl;
        }

        String host = getEnvironmentValue("OPAL_FINES_DB_HOST", "localhost");
        String port = getEnvironmentValue("OPAL_FINES_DB_PORT", "5432");
        String databaseName = getEnvironmentValue("OPAL_FINES_DB_NAME", "opal-fines-db");
        String options = getEnvironmentValue("OPAL_FINES_DB_OPTIONS", "");

        return "jdbc:postgresql://%s:%s/%s%s".formatted(host, port, databaseName, options);
    }

    private String getEnvironmentValue(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private TestHttpResponse requireLatestReportInstanceResponse() {
        assertNotNull(latestRawReportInstanceResponse,
                      "Expected a raw HTTP response for the latest report instance request");
        return latestRawReportInstanceResponse;
    }

    private void retrieveReportInstances(Map<String, String> headers) {
        retrieveReportInstances(SEEDED_REPORT_INSTANCES_QUERY, headers);
    }

    private void retrieveForbiddenReportInstancesForCurrentUser() {
        authorisedJsonRequest()
            .when()
            .get(getTestUrl() + REPORT_INSTANCES_URI + FORBIDDEN_REPORT_INSTANCES_QUERY);
    }

    private void retrieveReportInstances(String queryString, Map<String, String> headers) {
        latestRawReportInstanceResponse = TestHttpClient.get(
            getTestUrl() + REPORT_INSTANCES_URI + queryString,
            headers
        );
        ScenarioContextHolder.current().setLatestHttpResponse(latestRawReportInstanceResponse);
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
            assertTrue(!generatedDate.isBefore(fromDate), "generated_at is before from_date");
        }

        if (filters.containsKey("to_date")) {
            LocalDate generatedDate = getGeneratedDate(reportInstance);
            LocalDate toDate = LocalDate.parse(filters.get("to_date"));
            assertTrue(!generatedDate.isAfter(toDate), "generated_at is after to_date");
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

    @And("a report instances exists with the following details")
    public void aReportInstancesExistsWithTheFollowingDetails() {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}
