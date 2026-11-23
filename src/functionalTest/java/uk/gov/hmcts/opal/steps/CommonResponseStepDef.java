package uk.gov.hmcts.opal.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Defines generic response assertions that can be shared across multiple functional-test API
 * areas.
 */
public class CommonResponseStepDef extends BaseStepDef {
    private static final Pattern FEATURE_VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    /**
     * Asserts that the latest API or raw-client response returned the expected HTTP status code.
     *
     * @param statusCode expected HTTP status code.
     */
    @Then("the response status code is {int}")
    public void responseStatusCodeIs(int statusCode) {
        assertLatestResponseStatus(statusCode);
    }

    /**
     * Checks selected fields in the latest JSON error response against feature-file expectations.
     * Supported matchers are {@code equals}, {@code contains}, and {@code present}.
     *
     * @param expectedValues table of JSON field, matcher, and expected value rows.
     */
    @Then("the error response matches the following values")
    public void errorResponseMatchesExpectedValues(DataTable expectedValues) {
        List<Map<String, String>> rows = expectedValues.asMaps(String.class, String.class);
        assertFalse(rows.isEmpty(), "Expected at least one error response assertion");
        Set<String> assertedFields = new HashSet<>();
        List<String> failures = new ArrayList<>();

        Map<String, String> expectedStatus = rows.stream()
            .filter(row -> "status".equals(row.get("field")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Expected a status row in the error response assertions"));
        assertLatestResponseStatus(Integer.parseInt(expectedStatus.get("expected")));

        for (Map<String, String> row : rows) {
            String field = row.get("field");
            String configuredMatcher = row.get("matcher");
            String matcher = configuredMatcher == null || configuredMatcher.isBlank() ? "equals" : configuredMatcher;
            String expected = resolveExpectedValue(row.getOrDefault("expected", ""));
            assertAndCollect(failures, () -> assertErrorField(field, matcher, expected));
            assertedFields.add(field);
        }

        assertDefaultErrorField(failures, assertedFields, "instance", "present", "");
        assertDefaultErrorField(failures, assertedFields, "operation_id", "present", "");
        assertDefaultErrorField(failures, assertedFields, "is_warning", "equals", "false");
        assertDefaultErrorField(failures, assertedFields, "is_retryable", "equals", "false");

        assertTrue(failures.isEmpty(), "Error response assertions failed:\n" + String.join("\n", failures));
    }

    private void assertDefaultErrorField(List<String> failures, Set<String> assertedFields, String field,
                                         String matcher, String expected) {
        if (!assertedFields.contains(field)) {
            assertAndCollect(failures, () -> assertErrorField(field, matcher, expected));
        }
    }

    private void assertAndCollect(List<String> failures, Runnable assertion) {
        try {
            assertion.run();
        } catch (AssertionError failure) {
            failures.add(failure.getMessage());
        }
    }

    private String resolveExpectedValue(String expected) {
        Matcher variableMatcher = FEATURE_VARIABLE_PATTERN.matcher(expected);
        StringBuffer resolved = new StringBuffer();
        while (variableMatcher.find()) {
            String value = scenarioContext().getFeatureVariableOrFail(variableMatcher.group(1));
            variableMatcher.appendReplacement(resolved, Matcher.quoteReplacement(value));
        }
        variableMatcher.appendTail(resolved);
        return resolved.toString();
    }

    private void assertErrorField(String field, String matcher, String expected) {
        var response = SerenityRest.lastResponse();
        Object actualValue = getErrorFieldValue(response, field);
        if (actualValue == null && "equals".equals(matcher) && "false".equals(expected)
            && ("is_warning".equals(field) || "is_retryable".equals(field))) {
            actualValue = false;
        }
        String message = "Unexpected error response field '" + field + "'";

        switch (matcher) {
            case "equals" -> assertEquals(expected, String.valueOf(actualValue), message);
            case "contains" -> {
                assertNotNull(actualValue, message + ": value was absent");
                assertTrue(String.valueOf(actualValue).contains(expected),
                    message + ": expected '" + expected + "' in '" + actualValue + "'");
            }
            case "present" -> {
                assertNotNull(actualValue, message + ": value was absent");
                assertFalse(actualValue instanceof String text && text.isBlank(),
                    message + ": value was blank");
            }
            default -> throw new IllegalArgumentException("Unsupported error response matcher: " + matcher);
        }
    }

    private Object getErrorFieldValue(Response response, String field) {
        Object value = response.jsonPath().get(field);
        if (value == null && "operation_id".equals(field)) {
            value = response.jsonPath().get("properties.operation_id");
        }
        if (value == null && "is_retryable".equals(field)) {
            value = response.jsonPath().get("retriable");
            if (value == null) {
                value = response.jsonPath().get("properties.retriable");
            }
        }
        return value;
    }

    /**
     * Asserts that the latest API or raw-client response returned the expected HTTP status code.
     *
     * @param statusCode expected HTTP status code.
     */
    @Then("the response status is {int}")
    public void responseStatusIs(int statusCode) {
        assertLatestResponseStatus(statusCode);
    }

    /**
     * Asserts that the latest request succeeded with HTTP 200.
     */
    @Then("the request succeeds")
    public void requestSucceeds() {
        assertLatestResponseStatus(200);
    }

    /**
     * Asserts that the latest request created a resource with HTTP 201.
     */
    @Then("the request creates a resource")
    public void requestCreatesAResource() {
        assertLatestResponseStatus(201);
    }

    /**
     * Asserts that the latest request failed with HTTP 401.
     */
    @Then("the request is rejected as unauthorized")
    public void requestIsRejectedAsUnauthorized() {
        assertLatestResponseStatus(401);
    }

    /**
     * Asserts that authentication failures caused by a missing or invalid token return HTTP 401.
     */
    @Then("I get 401 when token missing or invalid")
    public void tokenMissingOrInvalidReturnsUnauthorized() {
        assertLatestResponseStatus(401);
    }

    /**
     * Asserts that permission failures return HTTP 403.
     */
    @Then("I get 403 when token missing or invalid")
    public void permissionFailureReturnsForbidden() {
        assertLatestResponseStatus(403);
    }

    /**
     * Asserts that the latest request failed with HTTP 403.
     */
    @Then("the request is rejected as forbidden")
    public void requestIsRejectedAsForbidden() {
        assertLatestResponseStatus(403);
    }

    /**
     * Asserts that the latest request failed with the supplied HTTP status code.
     *
     * @param status expected HTTP status code.
     */
    @Then("the request is rejected with status {int}")
    public void requestIsRejectedWithStatus(int status) {
        assertLatestResponseStatus(status);
    }

    /**
     * Asserts that the latest request failed with HTTP 400.
     */
    @Then("the request is rejected as bad request")
    public void requestIsRejectedAsBadRequest() {
        assertLatestResponseStatus(400);
    }

    /**
     * Asserts that the latest request failed with HTTP 404.
     */
    @Then("the request is rejected as not found")
    public void requestIsRejectedAsNotFound() {
        assertLatestResponseStatus(404);
    }

    /**
     * Asserts that the latest request failed with HTTP 406.
     */
    @Then("the request is rejected as not acceptable")
    public void requestIsRejectedAsNotAcceptable() {
        assertLatestResponseStatus(406);
    }

    /**
     * Asserts that the latest request failed with HTTP 409.
     */
    @Then("the request is rejected as conflict")
    public void requestIsRejectedAsConflict() {
        assertLatestResponseStatus(409);
    }

    /**
     * Asserts that the latest request failed with HTTP 415.
     */
    @Then("the request is rejected as unsupported media type")
    public void requestIsRejectedAsUnsupportedMediaType() {
        assertLatestResponseStatus(415);
    }

    /**
     * Asserts that the latest request failed with HTTP 500.
     */
    @Then("the request fails with an internal server error")
    public void requestFailsWithInternalServerError() {
        assertLatestResponseStatus(500);
    }

    /**
     * Validates the status code from the latest Serenity or raw-client response.
     *
     * @param statusCode expected HTTP status code.
     */
    private void assertLatestResponseStatus(int statusCode) {
        TestHttpResponse httpResponse = scenarioContext().consumeLatestHttpResponse();
        if (httpResponse != null) {
            assertEquals(statusCode, httpResponse.statusCode(), "Unexpected HTTP status");
            return;
        }

        then()
            .log().ifValidationFails()
            .statusCode(statusCode);
    }
}
