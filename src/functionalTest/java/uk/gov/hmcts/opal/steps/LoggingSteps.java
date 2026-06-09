package uk.gov.hmcts.opal.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Defines Cucumber steps and helper logic for PDPO logging assertions.
 */
public class LoggingSteps extends BaseStepDef {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SEARCH_PATH = "/test-support/search";

    private static final int DEFAULT_TIMEOUT_SECONDS =
        Optional.ofNullable(System.getenv("LOG_SEARCH_TIMEOUT_SECONDS"))
            .map(Integer::parseInt)
            .orElse(60);

    private static final int DEFAULT_POLL_MILLIS =
        Optional.ofNullable(System.getenv("LOG_SEARCH_POLL_MILLIS"))
            .map(Integer::parseInt)
            .orElse(1000);

    private JsonNode latestPdpoSearchResponse;

    /**
     * Stores an invalid bearer token in the Serenity session for the current scenario.
     */
    @When("I set an invalid token manually")
    public void setInvalidTokenManually() {
        BearerTokenStepDef.setTokenOverride("invalid-token");
    }

    /**
     * Searches the logging-service test-support API until at least one PDPO log exists for the
     * created draft account identifier.
     */
    @Then("the logging service emits PDPO logs for the created draft account id")
    public void loggingServiceEmitsPdpoLogsForTheCreatedDraftAccountId() {
        latestPdpoSearchResponse = waitForPdpoLogs(Map.of(
            "individualId",
            scenarioContext().getLastDraftAccountIdOrFail()
        ));
    }

    /**
     * Verifies that the last PDPO search response does not expose the supplied JSON field names.
     *
     * @param fieldNames field names that must be absent from the emitted PDPO logs.
     */
    @Then("the emitted PDPO logs do not contain these field names")
    public void emittedPdpoLogsDoNotContainTheseFieldNames(DataTable fieldNames) {
        JsonNode response = requireLatestPdpoSearchResponse();
        for (String fieldName : fieldNames.asList(String.class)) {
            assertFalse(
                containsFieldName(response, fieldName),
                () -> "Expected emitted PDPO logs not to contain field name '" + fieldName + "' but found: "
                    + response
            );
        }
    }

    /**
     * Verifies that the last PDPO search response does not expose the supplied scalar values.
     *
     * @param values values that must be absent from the emitted PDPO logs.
     */
    @Then("the emitted PDPO logs do not contain these values")
    public void emittedPdpoLogsDoNotContainTheseValues(DataTable values) {
        JsonNode response = requireLatestPdpoSearchResponse();
        for (String value : values.asList(String.class)) {
            assertFalse(
                containsScalarValue(response, value),
                () -> "Expected emitted PDPO logs not to contain value '" + value + "' but found: " + response
            );
        }
    }

    /**
     * Polls the logging-service test-support search endpoint until matching PDPO logs are available.
     *
     * @param criteria search criteria to submit to the logging-service test-support API.
     * @return the first non-empty PDPO log search response.
     */
    private JsonNode waitForPdpoLogs(Map<String, String> criteria) {
        long timeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECONDS);
        long deadline = System.nanoTime() + timeoutNanos;
        Response lastResponse = null;

        while (System.nanoTime() < deadline) {
            lastResponse = SerenityRest.given()
                .accept("*/*")
                .contentType("application/json")
                .body(writeJson(criteria))
                .when()
                .post(getLoggingTestUrl() + SEARCH_PATH);

            if (lastResponse.statusCode() != 200) {
                fail("Expected PDPO log search to return 200 but was " + lastResponse.statusCode()
                    + " with body: " + lastResponse.asString());
            }

            JsonNode responseBody = readJson(lastResponse.asString());
            if (responseBody.isArray() && !responseBody.isEmpty()) {
                return responseBody;
            }

            sleepBeforeRetry();
        }

        String lastBody = lastResponse == null ? "<no response>" : lastResponse.asString();
        fail("Timed out waiting for PDPO logs matching " + criteria + ". Last response body: " + lastBody);
        return null;
    }

    /**
     * Returns the most recent PDPO log search response, failing the scenario if no search has run.
     *
     * @return the latest PDPO log search response stored for the scenario.
     */
    private JsonNode requireLatestPdpoSearchResponse() {
        assertNotNull(
            latestPdpoSearchResponse,
            "No PDPO log response is available. Search for emitted PDPO logs before asserting contents."
        );
        return latestPdpoSearchResponse;
    }

    /**
     * Serialises a Java value into JSON for the logging-service test-support request body.
     *
     * @param value value to serialise.
     * @return JSON representation of the supplied value.
     */
    private static String writeJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialise PDPO log search payload", e);
        }
    }

    /**
     * Parses a logging-service test-support response body into a JSON tree.
     *
     * @param value raw JSON response body.
     * @return parsed JSON tree.
     */
    private static JsonNode readJson(String value) {
        try {
            return MAPPER.readTree(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse PDPO log search response", e);
        }
    }

    /**
     * Pauses between polling attempts, preserving interrupt status if the wait is interrupted.
     */
    private static void sleepBeforeRetry() {
        try {
            Thread.sleep(DEFAULT_POLL_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for PDPO logs", e);
        }
    }

    /**
     * Recursively checks whether a JSON tree contains the supplied field name.
     *
     * @param node JSON tree or subtree to inspect.
     * @param expectedFieldName field name that must be found.
     * @return {@code true} if the field name exists anywhere in the tree.
     */
    private static boolean containsFieldName(JsonNode node, String expectedFieldName) {
        if (node == null) {
            return false;
        }

        if (node.isObject()) {
            for (var field : node.properties()) {
                if (expectedFieldName.equals(field.getKey()) || containsFieldName(field.getValue(), expectedFieldName)) {
                    return true;
                }
            }
            return false;
        }

        if (node.isArray()) {
            for (JsonNode child : node) {
                if (containsFieldName(child, expectedFieldName)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Recursively checks whether a JSON tree contains the supplied scalar value.
     *
     * @param node JSON tree or subtree to inspect.
     * @param expectedValue scalar value that must be found.
     * @return {@code true} if the scalar value exists anywhere in the tree.
     */
    private static boolean containsScalarValue(JsonNode node, String expectedValue) {
        if (node == null) {
            return false;
        }

        if (node.isValueNode()) {
            return expectedValue.equals(node.asText());
        }

        for (JsonNode child : node) {
            if (containsScalarValue(child, expectedValue)) {
                return true;
            }
        }

        return false;
    }
}
