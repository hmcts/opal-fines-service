package uk.gov.hmcts.opal.steps;

import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.hmcts.opal.utils.TimeUtils.secondsAgo;

/**
 * Defines Cucumber steps and helper logic for PDPO logging assertions.
 */
public class LoggingSteps extends BaseStepDef {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SEARCH_PATH = "/test-support/search";
    private static final String PDPO_CREATED_BY_ID = "500000000";
    private static final String PDPO_CREATED_BY_ID_UNAUTHORISED = "500000001";
    private static final String PDPO_CREATED_BY_TYPE = "OPAL_USER_ID";
    private static final String PDPO_BUSINESS_IDENTIFIER = "Submit Draft Account - Parent or Guardian";
    private static final String INDIVIDUALS_TYPE = "DRAFT_ACCOUNT";

    private static final int DEFAULT_TIMEOUT_SECONDS =
        Optional.ofNullable(System.getenv("LOG_SEARCH_TIMEOUT_SECONDS"))
            .map(Integer::parseInt)
            .orElse(60);

    private static final int DEFAULT_POLL_MILLIS =
        Optional.ofNullable(System.getenv("LOG_SEARCH_POLL_MILLIS"))
            .map(Integer::parseInt)
            .orElse(1000);

    private static final int DEFAULT_CREATED_AFTER_SECONDS =
        Optional.ofNullable(System.getenv("LOG_SEARCH_CREATED_AFTER_SECONDS"))
            .map(Integer::parseInt)
            .orElse(60);

    private JsonNode latestPdpoSearchResponse;

    private static final Logger log = LoggerFactory.getLogger(LoggingSteps.class);

    /**
     * Stores an invalid bearer token in the Serenity session for the current scenario.
     */
    @When("I set an invalid token manually")
    public void setInvalidTokenManually() {
        BearerTokenStepDef.setTokenOverride("invalid-token");
    }

    /**
     * Searches the logging-service test-support API until at least one PDPO log exists for the created draft account
     * identifier.
     */
    @Then("the logging service emits PDPO logs for the created draft account id")
    public void loggingServiceEmitsPdpoLogsForTheCreatedDraftAccountId() {
        latestPdpoSearchResponse = waitForPdpoLogs(Map.of(
            "created_by", Map.of(
                "id", PDPO_CREATED_BY_ID,
                "type", PDPO_CREATED_BY_TYPE),
            "individual_identifier", scenarioContext().getLastDraftAccountIdOrFail(),
            "individual_type", INDIVIDUALS_TYPE,
            "created_after", secondsAgo(DEFAULT_CREATED_AFTER_SECONDS).toString()
        ));
    }

    @Then("the logging service does not emit any PDPO logs for the created draft account id")
    public void loggingServiceDoesNotEmitAnyPdpoLogsForTheCreatedDraftAccountId() {
        latestPdpoSearchResponse = assertNoPdpoLogs(Map.of(
            "created_after", secondsAgo(DEFAULT_CREATED_AFTER_SECONDS).toString(),
            "created_by", Map.of(
                "id", PDPO_CREATED_BY_ID_UNAUTHORISED,
                "type", PDPO_CREATED_BY_TYPE)
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

        log.info("RESPONSE: {}", response);

        for (String fieldName : fieldNames.asList(String.class)) {
            assertFalse(
                containsFieldName(response, fieldName),
                () -> "Expected emitted PDPO logs not to contain field name '" + fieldName + "' but found: "
                    + response
            );
        }
    }

    /**
     * Verifies that the last PDPO search response contains the supplied JSON field names.
     *
     * @param fieldNames field names that must be absent from the emitted PDPO logs.
     */
    @Then("the emitted PDPO logs do contain these field names")
    public void emittedPdpoLogsDoContainTheseFieldNames(DataTable fieldNames) {
        JsonNode response = requireLatestPdpoSearchResponse();

        log.info("RESPONSE: {}", response);

        for (String fieldName : fieldNames.asList(String.class)) {
            assertTrue(
                containsFieldName(response, fieldName),
                () -> "Expected emitted PDPO logs to contain field name '" + fieldName + "' but found: "
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
     * Verifies that the past PDPO search response does contain the supplied values.
     *
     * @param values values that must be present in the emitted PDPO logs.
     */
    @Then("the emitted PDPO logs do contain these values")
    public void emittedPdpoLogsDoContainTheseValues(DataTable values) {
        JsonNode response = requireLatestPdpoSearchResponse();
        for (String value : values.asList(String.class)) {
            String expectedValue = resolveExpectedValue(value);
            assertTrue(
                containsScalarValue(response, expectedValue),
                () -> "Expected emitted PDPO logs to contain value '" + expectedValue + "' but did not find: "
                    + response
            );
        }
    }

    /**
     * Verifies that selected scalar values occur the expected number of times in the last PDPO search response.
     *
     * @param expectedValueCounts values and their expected occurrence counts.
     */
    @Then("the emitted PDPO logs contain these values exactly this many times")
    public void emittedPdpoLogsContainTheseValuesExactlyThisManyTimes(DataTable expectedValueCounts) {
        JsonNode response = requireLatestPdpoSearchResponse();

        for (Map<String, String> row : expectedValueCounts.asMaps(String.class, String.class)) {
            String expectedValue = resolveExpectedValue(row.get("Value"));
            int expectedCount = Integer.parseInt(row.get("Count"));
            int actualCount = countScalarValues(response, expectedValue);

            assertEquals(
                expectedCount,
                actualCount,
                () -> "Expected emitted PDPO logs to contain value '" + expectedValue + "' " + expectedCount
                    + " time(s) but found " + actualCount + ": " + response
            );
        }
    }

    private String resolveExpectedValue(String value) {
        if (!Objects.equals(value, "CREATED_DRAFT_ACCOUNT_ID")) {
            return value;
        }

        String id = scenarioContext().getLastDraftAccountIdOrFail();
        if (id.isBlank()) {
            fail("No draft account ID found in scenario context");
        }
        return id;
    }

    /**
     * Polls the logging-service test-support search endpoint until matching PDPO logs are available.
     *
     * @param criteria search criteria to submit to the logging-service test-support API.
     * @return the first non-empty PDPO log search response.
     */
    private JsonNode waitForPdpoLogs(Object criteria) {
        long timeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECONDS);
        long deadline = System.nanoTime() + timeoutNanos;
        Response lastResponse = null;

        log.info("Criteria: {}", criteria);

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
     * Searches the logging-service test-support API once and fails if matching PDPO logs are found.
     *
     * @param criteria search criteria to submit to the logging-service test-support API.
     * @return the empty PDPO log search response.
     */
    private JsonNode assertNoPdpoLogs(Object criteria) {
        log.info("Criteria: {}", criteria);

        Response response = SerenityRest.given()
            .accept("*/*")
            .contentType("application/json")
            .body(writeJson(criteria))
            .when()
            .post(getLoggingTestUrl() + SEARCH_PATH);

        if (response.statusCode() != 200) {
            fail("Expected PDPO log search to return 200 but was " + response.statusCode()
                + " with body: " + response.asString());
        }

        JsonNode responseBody = readJson(response.asString());
        assertTrue(
            responseBody.isArray(),
            "Expected PDPO log search to return an array but found: " + responseBody
        );
        if (!responseBody.isEmpty()) {
            fail("Expected no PDPO logs matching " + criteria + " but found: " + responseBody);
        }

        return responseBody;
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
     * @param node              JSON tree or subtree to inspect.
     * @param expectedFieldName field name that must be found.
     * @return {@code true} if the field name exists anywhere in the tree.
     */
    private static boolean containsFieldName(JsonNode node, String expectedFieldName) {
        if (node == null) {
            return false;
        }

        if (node.isObject()) {
            for (var field : node.properties()) {
                boolean fieldNameMatches = expectedFieldName.equals(field.getKey());
                boolean childFieldNameMatches = containsFieldName(field.getValue(), expectedFieldName);
                if (fieldNameMatches || childFieldNameMatches) {
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
     * @param node          JSON tree or subtree to inspect.
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

    /**
     * Recursively counts exact scalar-value matches in a JSON tree.
     *
     * @param node          JSON tree or subtree to inspect.
     * @param expectedValue scalar value to count.
     * @return number of matching scalar values.
     */
    private static int countScalarValues(JsonNode node, String expectedValue) {
        if (node == null) {
            return 0;
        }

        if (node.isValueNode()) {
            return expectedValue.equals(node.asText()) ? 1 : 0;
        }

        int count = 0;
        for (JsonNode child : node) {
            count += countScalarValues(child, expectedValue);
        }
        return count;
    }
}
