package uk.gov.hmcts.opal.steps;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Defines Cucumber steps and helper logic for PDPO logging assertions.
 */
public class LoggingSteps extends BaseStepDef {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SEARCH_PATH = "/test-support/search";
    private static final String PDPO_CREATED_BY_ID = "500000000";
    private static final String PDPO_CREATED_BY_TYPE = "OPAL_USER_ID";
    private static final String PDPO_BUSINESS_IDENTIFIER = "Submit Draft Account - Parent or Guardian";
    private static final String MINOR_CREDITOR_HISTORY_BUSINESS_IDENTIFIER =
        "View Minor Creditor Account History Item";
    private static final String MINOR_CREDITOR_SUMMARY_BUSINESS_IDENTIFIER =
        "View Minor Creditor Account - Individual";
    private static final String OPAL_USER_ID = "OPAL_USER_ID";
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
        latestPdpoSearchResponse = waitForPdpoLogs(pdpoSearchCriteria(PDPO_BUSINESS_IDENTIFIER,
            scenarioContext().getLastDraftAccountIdOrFail(), PdplIdentifierType.DRAFT_ACCOUNT.name()));
    }

    /**
     * Verifies a party operation reached the logging service with the expected actor and subject.
     *
     * @param businessIdentifier TDIA operation recorded by the party endpoint.
     * @param category processing category, using the logging API's display value.
     * @param partyType scenario party role whose identifier was retained before replacement or removal.
     */
    @Then("the logging service emits {string} with category {string} for the {string} party")
    public void loggingServiceEmitsPartyOperation(String businessIdentifier, String category, String partyType) {
        String partyId = scenarioContext().getPartyIdOrFail(partyType);
        String individualType = switch (partyType) {
            case "Defendant" -> PdplIdentifierType.DEFENDANT_ACCOUNT.name();
            case "Parent/Guardian" -> PdplIdentifierType.PARENT_GUARDIAN.name();
            default -> throw new IllegalArgumentException("Unknown party type: " + partyType);
        };
        assertPdpoOperation(businessIdentifier, category, partyId, individualType);
    }

    /**
     * Verifies an update log identifies the scenario's minor-creditor account and current OPAL user.
     *
     * @param businessIdentifier operation recorded by the minor-creditor endpoint.
     * @param category processing category using the logging API's display value.
     */
    @Then("the logging service emits {string} with category {string} for the created minor creditor account")
    public void loggingServiceEmitsMinorCreditorOperation(String businessIdentifier, String category) {
        assertPdpoOperation(businessIdentifier, category, scenarioContext().getCreatedMinorCreditorAccountIdOrFail().toString(),
            PdplIdentifierType.MINOR_CREDITOR.name());
    }

    /**
     * Checks for unwanted logs throughout the asynchronous logging observation window.
     *
     * @param businessIdentifier operation that must not be recorded for the scenario's company account.
     */
    @Then("the logging service does not emit {string} for the created minor creditor account")
    public void loggingServiceDoesNotEmitMinorCreditorOperation(String businessIdentifier) {
        Map<String, Object> criteria = pdpoSearchCriteria(businessIdentifier,
            scenarioContext().getCreatedMinorCreditorAccountIdOrFail().toString(), PdplIdentifierType.MINOR_CREDITOR.name());
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECONDS);
        do {
            JsonNode response = searchPdpoLogs(criteria);
            assertTrue(response.isArray() && response.isEmpty(), "Unexpected minor-creditor PDPO logs: " + response);
            sleepBeforeRetry();
        } while (System.nanoTime() < deadline);
    }

    private void assertPdpoOperation(String businessIdentifier, String category, String individualId,
        String individualType) {
        Map<String, Object> criteria = new LinkedHashMap<>(pdpoSearchCriteria(businessIdentifier,
            individualId, individualType));
        criteria.put("category", category);
        latestPdpoSearchResponse = waitForPdpoLogs(criteria);

        for (JsonNode log : requireLatestPdpoSearchResponse()) {
            assertEquals(businessIdentifier, log.path("business_identifier").asString());
            assertEquals(category, log.path("category").asString());
            assertEquals(PDPO_CREATED_BY_ID, log.path("created_by").path("id").asString());
            assertEquals(PDPO_CREATED_BY_TYPE, log.path("created_by").path("type").asString());
            assertTrue(StreamSupport.stream(log.path("individuals").spliterator(), false)
                .anyMatch(individual -> individualId.equals(individual.path("id").asString())
                    && individualType.equals(individual.path("type").asString())),
                "Expected the log to identify " + individualType + " " + individualId);
        }
    }

    private Map<String, Object> pdpoSearchCriteria(String businessIdentifier, String individualId,
        String individualType) {
        return Map.of(
            "created_by", Map.of("id", PDPO_CREATED_BY_ID, "type", PDPO_CREATED_BY_TYPE),
            "business_identifier", businessIdentifier,
            "individual_identifier", individualId,
            "individual_type", individualType
        );
    }

    /**
     * Verifies the logging service persisted a consultation containing each represented history
     * user exactly once.
     *
     * @param expectedUsers OPAL user identifiers expected in the PDPO participant list.
     */
    @Then("the logging service contains a minor creditor history consultation for these users")
    public void loggingServiceContainsMinorCreditorHistoryConsultationForUsers(DataTable expectedUsers) {
        Set<String> expectedUserIds = Set.copyOf(expectedUsers.asList(String.class));
        assertFalse(expectedUserIds.isEmpty(), "At least one expected history user is required");

        JsonNode response = waitForPdpoLogs(Map.of(
            "business_identifier", MINOR_CREDITOR_HISTORY_BUSINESS_IDENTIFIER,
            "category", "Consultation",
            "individual_identifier", expectedUserIds.iterator().next(),
            "individual_type", OPAL_USER_ID
        ));
        JsonNode logEntry = response.get(0);
        JsonNode individuals = logEntry.path("individuals");

        assertEquals(MINOR_CREDITOR_HISTORY_BUSINESS_IDENTIFIER,
            logEntry.path("business_identifier").asText());
        assertEquals("Consultation", logEntry.path("category").asText());
        assertTrue(individuals.isArray(), "PDPO individuals should be an array");

        Set<String> actualUserIds = new java.util.LinkedHashSet<>();
        for (JsonNode individual : individuals) {
            assertEquals(OPAL_USER_ID, individual.path("type").asText());
            actualUserIds.add(individual.path("id").asText());
        }

        assertEquals(expectedUserIds, actualUserIds, "Unexpected minor creditor history participants");
        assertEquals(actualUserIds.size(), individuals.size(), "History participants must not contain duplicates");
    }

    /**
     * Verifies that an individual minor-creditor summary read emitted the required consultation.
     */
    @Then("the logging service contains an individual minor creditor summary consultation")
    public void loggingServiceContainsIndividualMinorCreditorSummaryConsultation() {
        Long accountId = scenarioContext().getCreatedMinorCreditorAccountIdOrFail();
        JsonNode response = waitForPdpoLogs(Map.of(
            "business_identifier", MINOR_CREDITOR_SUMMARY_BUSINESS_IDENTIFIER,
            "category", "Consultation",
            "individual_identifier", accountId.toString(),
            "individual_type", "MINOR_CREDITOR"
        ));
        JsonNode logEntry = response.get(0);

        assertEquals(MINOR_CREDITOR_SUMMARY_BUSINESS_IDENTIFIER,
            logEntry.path("business_identifier").asText());
        assertEquals("Consultation", logEntry.path("category").asText());
        assertTrue(logEntry.path("individuals").isArray(), "PDPO individuals should be an array");
        assertTrue(logEntry.path("individuals").valueStream().anyMatch(individual ->
                accountId.toString().equals(individual.path("id").asText())
                    && "MINOR_CREDITOR".equals(individual.path("type").asText())),
            "The created minor creditor was not present in the PDPO consultation");
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
    private JsonNode waitForPdpoLogs(Map<String, Object> criteria) {
        long timeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECONDS);
        long deadline = System.nanoTime() + timeoutNanos;
        JsonNode lastResponse = null;

        while (System.nanoTime() < deadline) {
            lastResponse = searchPdpoLogs(criteria);
            if (lastResponse.isArray() && !lastResponse.isEmpty()) {
                return lastResponse;
            }

            sleepBeforeRetry();
        }

        String lastBody = lastResponse == null ? "<no response>" : lastResponse.toString();
        fail("Timed out waiting for PDPO logs matching " + criteria + ". Last response body: " + lastBody);
        return null;
    }

    private JsonNode searchPdpoLogs(Map<String, Object> criteria) {
        Response response = SerenityRest.given()
            .accept("*/*")
            .contentType("application/json")
            .body(writeJson(criteria))
            .when()
            .post(getLoggingTestUrl() + SEARCH_PATH);
        assertEquals(200, response.statusCode(), "PDPO log search failed: " + response.asString());
        return readJson(response.asString());
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
