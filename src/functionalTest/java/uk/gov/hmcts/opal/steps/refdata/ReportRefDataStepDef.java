package uk.gov.hmcts.opal.steps.refdata;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.opal.steps.CommonMethods;
import uk.gov.hmcts.opal.context.ScenarioContextHolder;
import uk.gov.hmcts.opal.utils.TestHttpClient;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.opal.config.Constants.REPORTS_URI;

/**
 * Defines Cucumber steps for report-definition reference-data requests and contract assertions.
 */
public class ReportRefDataStepDef {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String REPORT_ID_ENV = "OPAL_FUNCTIONAL_REPORT_ID";
    private static final String SEEDED_REPORT_ID = "operational_report_enforcement";
    private static final Set<String> EXPECTED_FIELDS = Set.of(
        "report_id",
        "report_title",
        "report_group",
        "supported_file_types",
        "audited_report",
        "report_parameters",
        "supports_multiple_business_units",
        "is_bespoke_journey",
        "shown_as_worklist",
        "retention_period",
        "permission",
        "can_manually_create"
    );
    private static final Set<String> VALID_FILE_TYPES = Set.of("CSV", "PDF", "XML");

    private final CommonMethods methods = new CommonMethods();
    private TestHttpResponse latestRawReportResponse;

    /**
     * Retrieves the configured happy-path report definition from the deployed environment.
     */
    @When("I make a request to the configured report definition api")
    public void getConfiguredReportDefinition() {
        methods.getRequest(REPORTS_URI + "/" + resolveReportId());
    }

    /**
     * Retrieves the seeded report definition used for stable auth and error-contract checks.
     */
    @When("I make a request to the seeded report definition api")
    public void getSeededReportDefinition() {
        methods.getRequest(REPORTS_URI + "/" + SEEDED_REPORT_ID);
    }

    /**
     * Retrieves a report definition by report id.
     *
     * @param reportId report id to request.
     */
    @When("get the report with report_id {string}")
    public void getReportWithReportId(String reportId) {
        methods.getRequest(REPORTS_URI + "/" + reportId);
    }

    /**
     * Calls the seeded report definition endpoint using the requested authentication state.
     *
     * @param authenticationState whether the request should be sent with no token or an invalid
     *                            token.
     */
    @When("I call GET on the seeded report definition api with {string}")
    public void callSeededReportDefinitionWithAuthenticationState(String authenticationState) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "*/*");

        switch (authenticationState) {
            case "no token" -> {
                // no-op, leave Authorization absent
            }
            case "invalid token" -> headers.put("Authorization", "Bearer invalidToken");
            default -> throw new IllegalArgumentException(
                "Unknown authentication state for report request: " + authenticationState
            );
        }

        latestRawReportResponse = TestHttpClient.request(
            "GET",
            uk.gov.hmcts.opal.steps.BaseStepDef.getTestUrl() + REPORTS_URI + "/" + SEEDED_REPORT_ID,
            headers,
            null
        );
        ScenarioContextHolder.current().setLatestHttpResponse(latestRawReportResponse);
    }

    /**
     * Asserts that the latest report-definition response matches the documented contract shape and
     * primitive types.
     */
    @Then("the report definition matches the documented contract")
    public void reportDefinitionMatchesDocumentedContract() {
        String body = then().extract().body().asString();
        JsonNode root = OBJECT_MAPPER.readTree(body);

        then().assertThat().statusCode(200);
        assertEquals(EXPECTED_FIELDS, collectFieldNames(root), "Unexpected report fields returned");
        assertEquals(resolveReportId(), root.path("report_id").asString(), "Unexpected report_id returned");

        assertTrue(root.path("report_id").isString(), "report_id should be a string");
        assertTrue(root.path("report_title").isString(), "report_title should be a string");
        assertTrue(root.path("report_group").isString(), "report_group should be a string");
        assertTrue(root.path("supported_file_types").isArray(), "supported_file_types should be an array");
        assertTrue(root.path("audited_report").isBoolean(), "audited_report should be a boolean");
        assertTrue(root.path("report_parameters").isObject(), "report_parameters should be an object");
        assertTrue(
            root.path("supports_multiple_business_units").isBoolean(),
            "supports_multiple_business_units should be a boolean"
        );
        assertTrue(root.path("is_bespoke_journey").isBoolean(), "is_bespoke_journey should be a boolean");
        assertTrue(root.path("shown_as_worklist").isBoolean(), "shown_as_worklist should be a boolean");
        assertTrue(root.path("can_manually_create").isBoolean(), "can_manually_create should be a boolean");

        validateSupportedFileTypes(root.path("supported_file_types"));
        validateNullableStringField(root.path("retention_period"), "retention_period");
        validateNullableStringField(root.path("permission"), "permission");

        if (!root.path("retention_period").isNull()) {
            assertNotNull(
                Duration.parse(root.path("retention_period").asString()),
                "retention_period should be an ISO-8601 duration"
            );
        }
    }

    /**
     * Asserts that the latest report-definition error response matches the shared ProblemDetail
     * top-level contract for the expected status.
     *
     * @param expectedStatus expected HTTP status code.
     */
    @Then("the latest report definition error response matches the standard problem detail contract for status {int}")
    public void latestReportDefinitionErrorResponseMatchesProblemDetailContract(int expectedStatus) {
        TestHttpResponse latestHttpResponse = latestRawReportResponse;
        latestRawReportResponse = null;

        String body;
        if (latestHttpResponse != null) {
            assertEquals(expectedStatus, latestHttpResponse.statusCode(), "Unexpected HTTP status");
            body = latestHttpResponse.body();
        } else {
            then().statusCode(expectedStatus);
            body = SerenityRest.lastResponse().asString();
        }

        validateProblemDetailResponse(body, expectedStatus);
    }

    /**
     * Asserts that the latest no-token report-definition response is an unauthorised response,
     * tolerating both the local plain-text security-layer message and the deployed problem-detail
     * shape returned in some environments.
     */
    @Then("the latest report definition response is an unauthorized response")
    public void latestReportDefinitionResponseIsAnUnauthorizedResponse() {
        TestHttpResponse latestHttpResponse = latestRawReportResponse;
        latestRawReportResponse = null;

        assertNotNull(latestHttpResponse, "Expected a raw HTTP response for the latest report request");
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

    private void validateProblemDetailResponse(String body, int expectedStatus) {
        JsonNode root = OBJECT_MAPPER.readTree(body);
        assertTrue(root.isObject(), "Problem detail response should be a JSON object");
        assertTrue(root.path("title").isString(), "title should be a string");
        assertTrue(root.path("detail").isString(), "detail should be a string");
        assertTrue(root.path("status").isInt(), "status should be an integer");
        assertEquals(expectedStatus, root.path("status").asInt(), "Unexpected status in response body");

        validateOptionalTextField(root.path("type"), "type");
        validateOptionalTextField(root.path("instance"), "instance");
        validateOptionalTextField(root.path("operation_id"), "operation_id");

        if (!root.path("retriable").isMissingNode() && !root.path("retriable").isNull()) {
            assertTrue(root.path("retriable").isBoolean(), "retriable should be a boolean when present");
        }
    }

    private void validateSupportedFileTypes(JsonNode supportedFileTypes) {
        for (JsonNode fileType : supportedFileTypes) {
            assertTrue(fileType.isString(), "supported_file_types entries should be strings");
            String fileTypeValue = fileType.asString();
            assertTrue(
                VALID_FILE_TYPES.contains(fileTypeValue),
                "supported_file_types contains an unsupported value: " + fileTypeValue
            );
        }
    }

    private void validateNullableStringField(JsonNode field, String fieldName) {
        assertFalse(field.isMissingNode(), fieldName + " should be present");
        if (!field.isNull()) {
            assertTrue(field.isString(), fieldName + " should be a string when present");
        }
    }

    private void validateOptionalTextField(JsonNode field, String fieldName) {
        if (!field.isMissingNode() && !field.isNull()) {
            assertTrue(field.isString(), fieldName + " should be a string when present");
        }
    }

    private Set<String> collectFieldNames(JsonNode root) {
        Set<String> fieldNames = new java.util.HashSet<>();
        root.properties().forEach(field -> fieldNames.add(field.getKey()));
        return fieldNames;
    }

    private String resolveReportId() {
        String reportId = System.getenv(REPORT_ID_ENV);
        if (reportId == null || reportId.isBlank()) {
            throw new IllegalStateException(
                "Set " + REPORT_ID_ENV
                    + " to a permissioned report_id before running the report-definition happy-path E2E test"
            );
        }
        return reportId;
    }
}
