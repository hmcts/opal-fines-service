package uk.gov.hmcts.opal.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.rest.SerenityRest;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class LoggingSteps extends BaseStepDef {

    private static final Logger log = LoggerFactory.getLogger(LoggingSteps.class);
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

    // Map business_identifier -> expected individuals[].type (entity type)
    private static final Map<String, String> BUSINESS_TO_ENTITY_TYPE = Map.of(
        "Submit Draft Account - Defendant", "DRAFT_ACCOUNT",
        "Submit Draft Account - Parent or Guardian", "DRAFT_ACCOUNT",
        "Submit Draft Account - Minor Creditor", "DRAFT_ACCOUNT"
    );

    /**
     * Canonical DataTable-driven step:
     * Provide rows:
     *  | created_by_id | created_by_type | business_identifier  | expected_count |
     * expected_count optional, defaults to 1.
     */
    @Then("the logging service contains these PDPO logs:")
    public void loggingServiceContainsThesePdpoLogs(DataTable table) throws Exception {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        List<PdpoExpectation> expectations = rows.stream()
            .map(r -> new PdpoExpectation(
                r.get("created_by_id"),
                r.get("created_by_type"),
                r.get("business_identifier"),
                r.containsKey("expected_count") ? Integer.parseInt(r.get("expected_count")) : 1
            ))
            .collect(Collectors.toList());

        searchAllExpectations(expectations);
    }

    // Core routine: poll the search endpoint separately per expectation until all are satisfied or timeout.
    private void searchAllExpectations(List<PdpoExpectation> expectations) throws Exception {
        int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
        Instant deadline = Instant.now().plusSeconds(timeoutSeconds);
        String url = getLoggingTestUrl() + SEARCH_PATH;
        Optional<String> optionalBearer = Optional.ofNullable(System.getenv("OPAL_LOGGING_SERVICE_BEARER"));

        Map<PdpoExpectation, Integer> remaining = new LinkedHashMap<>();
        expectations.forEach(e -> remaining.put(e, e.expectedCount));

        Throwable lastException = null;
        String lastResponseBody = null;

        log.info("Polling logging service {} for {} expectations up to {}s",
                 url, expectations.size(), timeoutSeconds);

        while (Instant.now().isBefore(deadline) && !remaining.isEmpty()) {
            for (Iterator<Map.Entry<PdpoExpectation, Integer>> it = remaining.entrySet().iterator();
                 it.hasNext(); ) {

                Map.Entry<PdpoExpectation, Integer> entry = it.next();
                PdpoExpectation exp = entry.getKey();
                int need = entry.getValue();

                try {
                    Map<String, Object> payload = new HashMap<>();
                    Map<String, Object> createdBy = new HashMap<>();
                    createdBy.put("id", exp.createdById);
                    createdBy.put("type", exp.createdByType);
                    payload.put("created_by", createdBy);
                    payload.put("business_identifier", exp.businessIdentifier);

                    String createdDraftId = getCreatedDraftAccountId();
                    if (createdDraftId != null
                        && !createdDraftId.isBlank()
                        && !createdDraftId.startsWith("<")) {

                        payload.put("individual_id", createdDraftId);
                    }

                    // debug: log payload
                    log.debug("PDPO search payload: {}", MAPPER.writeValueAsString(payload));

                    var given = SerenityRest.given().contentType("application/json")
                        .body(MAPPER.writeValueAsString(payload));
                    optionalBearer.ifPresent(b -> given.header("Authorization", "Bearer " + b));

                    Response response = given.when().post(url);
                    int status = response.getStatusCode();
                    String body = response.getBody() == null ? "" : response.getBody().asString();
                    lastResponseBody = body;
                    log.info("PDPO search returned status={} body={}", status, body);

                    // if empty and we have a draft id, try alternate nested key once
                    if (status == 200
                        && (body == null || body.isBlank() || body.equals("[]"))
                        && createdDraftId != null) {

                        Map<String,Object> altPayload = new HashMap<>(payload);
                        altPayload.remove("individual_id");
                        altPayload.put("individuals.id", createdDraftId);
                        log.debug("Retrying with alternative payload: {}",
                                  MAPPER.writeValueAsString(altPayload));
                        Response altResp = SerenityRest.given().contentType("application/json")
                            .body(MAPPER.writeValueAsString(altPayload))
                            .when().post(url);
                        log.info("Alt search returned status={} body={}", altResp.getStatusCode(),
                                 altResp.getBody().asString());
                        if (altResp.getStatusCode() == 200
                            && !altResp.getBody().asString().isBlank()
                            && !altResp.getBody().asString().equals("[]")) {

                            body = altResp.getBody().asString();
                        }
                    }

                    if (status == 200 && body != null && !body.isBlank() && !body.equals("[]")) {
                        JsonNode root = MAPPER.readTree(body);
                        JsonNode entries = root.isArray() ? root : root.path("entries");

                        int foundCount = 0;
                        if (entries.isArray()) {
                            String expectedDraftId = getCreatedDraftAccountId();
                            for (JsonNode rec : entries) {
                                if (!matchesCreatedByAndBusiness(rec, exp.createdById,
                                                                 exp.createdByType, exp.businessIdentifier)) {

                                    continue;
                                }

                                // require record to reference the created draft id (if we have one)
                                if (expectedDraftId != null) {
                                    boolean referencesExpectedDraft = false;
                                    JsonNode individuals = rec.path("individuals");
                                    if (individuals.isArray()) {
                                        for (JsonNode ind : individuals) {
                                            String indId = ind.path("id").asText(null);
                                            if (expectedDraftId.equals(indId)) {
                                                referencesExpectedDraft = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (!referencesExpectedDraft) {
                                        log.debug("Record {} matches business/created_by but not draft id; skipping",
                                                  rec.path("pdpo_log_id").asText());
                                        continue;
                                    }
                                }

                                try {
                                    validateRecord(rec, exp.businessIdentifier);
                                    foundCount++;
                                } catch (AssertionError ae) {
                                    lastException = ae;
                                    log.warn("Found record for {} but assertion failed: {}",
                                             exp.businessIdentifier, ae.getMessage());
                                }
                            }
                        }

                        if (foundCount >= need) {
                            it.remove();
                            log.info("Expectation satisfied for '{}' (found {}). Remaining expectations: {}",
                                     exp.businessIdentifier, foundCount, remaining.size());
                        } else {
                            entry.setValue(need - foundCount);
                            log.debug("Expectation '{}' still needs {} more (found {})",
                                      exp.businessIdentifier, need - foundCount, foundCount);
                        }
                    } else {
                        lastException = new RuntimeException("status=" + status + " body=" + body);
                    }

                } catch (Exception e) {
                    lastException = e;
                    log.warn("Exception querying logging service for {}: {}", exp.businessIdentifier, e.toString());
                }
            }

            if (remaining.isEmpty()) {
                log.info("All PDPO expectations satisfied.");
                return;
            }

            //noinspection BusyWait
            Thread.sleep(DEFAULT_POLL_MILLIS);
        }

        String outstanding = remaining.keySet().stream()
            .map(e -> e.businessIdentifier + "(need=" + remaining.get(e) + ")")
            .collect(Collectors.joining(","));
        String err = String.format(
            "Did not find all logging entries within %d seconds. Outstanding: %s. Last response body: %s",
            DEFAULT_TIMEOUT_SECONDS, outstanding, lastResponseBody
        );
        if (lastException != null) {
            err += ". Last exception: " + lastException.getMessage();
        }
        fail(err);
    }

    // Validate a matched record's shape and semantics (created_at check intentionally omitted)
    private void validateRecord(JsonNode rec, String businessIdentifier) {
        // category must be Collection
        assertEquals("Collection", rec.path("category").asText(null), "category must be Collection");

        // recipient must be null or missing
        assertTrue(rec.path("recipient").isMissingNode() || rec.path("recipient").isNull(),
                   "recipient must be null or missing");

        // individuals[] must be present; test-support uses "id" and "type"
        JsonNode individuals = rec.path("individuals");
        assertTrue(individuals.isArray() && !individuals.isEmpty(), "individuals must be a non-empty array");

        // expected entity type for individuals (usually DRAFT_ACCOUNT)
        String expectedEntityType = BUSINESS_TO_ENTITY_TYPE.getOrDefault(businessIdentifier, "DRAFT_ACCOUNT");

        boolean entityTypeFound = false;
        for (JsonNode ind : individuals) {
            String indType = ind.path("type").asText(null);
            if (expectedEntityType.equals(indType)) {
                entityTypeFound = true;
                break;
            }
        }

        assertTrue(entityTypeFound, "individuals did not contain expected entity type=" + expectedEntityType);

        // business_identifier must match
        assertEquals(businessIdentifier, rec.path("business_identifier").asText(null),
                     "business_identifier should match expected");
    }

    private boolean matchesCreatedByAndBusiness(JsonNode rec, String createdById, String createdByType,
                                                String businessIdentifier) {

        JsonNode cb = rec.path("created_by");
        String id = cb.path("id").asText(null);
        String type = cb.path("type").asText(null);
        String bi = rec.path("business_identifier").asText(null);
        return createdById.equals(id) && createdByType.equals(type) && businessIdentifier.equals(bi);
    }

    // Serenity-backed retrieval of session variables (safe)
    private String getCreatedDraftAccountId() {
        try {
            Object v = Serenity.sessionVariableCalled("CREATED_DRAFT_ACCOUNT_ID");
            return v == null ? null : String.valueOf(v);
        } catch (Exception e) {
            log.debug("Error retrieving CREATED_DRAFT_ACCOUNT_ID from Serenity session: {}", e.getMessage());
            return null;
        }
    }

    // Expectation helper
    private static class PdpoExpectation {
        final String createdById;
        final String createdByType;
        final String businessIdentifier;
        final int expectedCount;

        PdpoExpectation(String createdById, String createdByType, String businessIdentifier, int expectedCount) {
            this.createdById = createdById;
            this.createdByType = createdByType;
            this.businessIdentifier = businessIdentifier;
            this.expectedCount = expectedCount;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PdpoExpectation)) {
                return false;
            }
            PdpoExpectation other = (PdpoExpectation) o;
            return Objects.equals(createdById, other.createdById)
                && Objects.equals(createdByType, other.createdByType)
                && Objects.equals(businessIdentifier, other.businessIdentifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(createdById, createdByType, businessIdentifier);
        }
    }
}
