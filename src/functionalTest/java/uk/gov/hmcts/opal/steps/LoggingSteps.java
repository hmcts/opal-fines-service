package uk.gov.hmcts.opal.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
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
    private static final Map<String, String> BUSINESS_TO_ENTITY_TYPE = Map.ofEntries(
        Map.entry("Submit Draft Account - Defendant", "DRAFT_ACCOUNT"),
        Map.entry("Submit Draft Account - Parent or Guardian", "DRAFT_ACCOUNT"),
        Map.entry("Submit Draft Account - Minor Creditor", "DRAFT_ACCOUNT"),
        Map.entry("Update Draft Account - Defendant", "DRAFT_ACCOUNT"),
        Map.entry("Update Draft Account - Parent or Guardian", "DRAFT_ACCOUNT"),
        Map.entry("Update Draft Account - Minor Creditor", "DRAFT_ACCOUNT"),
        Map.entry("Re-submit Draft Account - Defendant", "DRAFT_ACCOUNT"),
        Map.entry("Re-submit Draft Account - Parent or Guardian", "DRAFT_ACCOUNT"),
        Map.entry("Re-submit Draft Account - Minor Creditor", "DRAFT_ACCOUNT"),
        Map.entry("Get Draft Account - Defendant", "DRAFT_ACCOUNT"),
        Map.entry("Get Draft Account - Parent or Guardian", "DRAFT_ACCOUNT"),
        Map.entry("Get Draft Account - Minor Creditor", "DRAFT_ACCOUNT")
    );

    @Then("the logging service contains these PDPO logs:")
    public void loggingServiceContainsThesePdpoLogs(DataTable table) throws Exception {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        List<Expectation> expectations = rows.stream()
            .map(r -> new Expectation(
                r.get("created_by_id"),
                r.get("created_by_type"),
                r.get("business_identifier"),
                r.containsKey("expected_count") ? Integer.parseInt(r.get("expected_count")) : 1
            ))
            .collect(Collectors.toList());

        pollForExpectations(expectations);
    }

    @When("I set an invalid token manually")
    public void setInvalidTokenManually() {
        Serenity.setSessionVariable("BEARER_TOKEN").to("invalid-token");
    }

    @Then("no PDPO logs exist for created_by id {string}, type {string} and business_identifier {string}")
    public void noPdpoLogsExist(String createdById,
                                String createdByType,
                                String businessIdentifier) throws Exception {

        final int timeoutMillis = 5000;
        final String url = getLoggingTestUrl() + SEARCH_PATH;
        final Optional<String> optionalBearer = Optional.ofNullable(System.getenv("OPAL_LOGGING_SERVICE_BEARER"));

        Map<String, Object> payload = new HashMap<>();
        payload.put("business_identifier", businessIdentifier);

        // scope by draft id if available
        String createdDraftId = getCreatedDraftAccountId();
        if (createdDraftId != null && !createdDraftId.isBlank() && !createdDraftId.startsWith("<")) {
            payload.put("individual_id", createdDraftId);
        }

        Instant deadline = Instant.now().plusMillis(timeoutMillis);
        String lastBody = null;

        while (Instant.now().isBefore(deadline)) {
            var given = SerenityRest.given()
                .contentType("application/json")
                .body(MAPPER.writeValueAsString(payload));
            optionalBearer.ifPresent(b -> given.header("Authorization", "Bearer " + b));

            Response searchResp = given.when().post(url);
            int status = searchResp.getStatusCode();
            String body = searchResp.getBody() == null ? "" : searchResp.getBody().asString();
            lastBody = body;

            if (status == 200) {
                if (body == null || body.isBlank() || body.trim().equals("[]")) {
                    // no logs found
                    return;
                }

                JsonNode root = MAPPER.readTree(body);
                if (!root.isArray()) {
                    fail("Unexpected logging search response format: " + body);
                }

                for (JsonNode rec : root) {
                    if (matchesCreatedBy(rec, createdById, createdByType)
                        && businessIdentifier.equals(rec.path("business_identifier").asText())) {
                        fail("Unexpected PDPO log found: " + rec.toPrettyString());
                    }
                }
                // If we got here, no matching record found
                return;
            }

            //noinspection BusyWait
            Thread.sleep(DEFAULT_POLL_MILLIS);
        }

        fail("Did not confirm absence of PDPO logs within " + timeoutMillis + "ms. Last response: " + lastBody);
    }

    private void pollForExpectations(List<Expectation> expectations) throws Exception {
        int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
        Instant deadline = Instant.now().plusSeconds(timeoutSeconds);
        String url = getLoggingTestUrl() + SEARCH_PATH;
        Optional<String> optionalBearer = Optional.ofNullable(System.getenv("OPAL_LOGGING_SERVICE_BEARER"));

        Map<Expectation, Integer> remaining = new LinkedHashMap<>();
        expectations.forEach(e -> remaining.put(e, e.expectedCount));

        Throwable lastException = null;
        String lastResponseBody = null;

        log.info("Polling logging service {} for {} expectations up to {}s",
                 url, expectations.size(), timeoutSeconds);

        while (Instant.now().isBefore(deadline) && !remaining.isEmpty()) {
            for (Iterator<Map.Entry<Expectation, Integer>> it = remaining.entrySet().iterator();
                 it.hasNext(); ) {

                Map.Entry<Expectation, Integer> entry = it.next();
                Expectation exp = entry.getKey();
                int need = entry.getValue();

                try {
                    String payload = buildSearchPayload(exp);
                    log.info("Search payload for [{}]: {}", exp.businessIdentifier, payload);

                    log.debug("PDPO search payload: {}", payload);

                    var given = SerenityRest.given()
                        .contentType("application/json")
                        .body(payload);
                    optionalBearer.ifPresent(b -> given.header("Authorization", "Bearer " + b));

                    Response response = given.when().post(url);
                    int status = response.getStatusCode();
                    String body = response.getBody() == null ? "" : response.getBody().asString();
                    lastResponseBody = body;
                    log.info("PDPO search returned status={} body={}", status, body);

                    // if empty and we have a draft id, try alternate nested key once
                    String createdDraftId = getCreatedDraftAccountId();
                    if (status == 200 && (body == null || body.isBlank() || body.equals("[]"))
                        && createdDraftId != null) {

                        Map<String, Object> altPayload = new HashMap<>();

                        Map<String, Object> cb = new HashMap<>();
                        cb.put("id", exp.createdById);
                        cb.put("type", exp.createdByType);

                        altPayload.put("created_by", cb);
                        altPayload.put("business_identifier", exp.businessIdentifier);
                        altPayload.put("individuals.id", createdDraftId);
                        log.debug("Retrying with alternative payload: {}", MAPPER.writeValueAsString(altPayload));

                        var altGiven = SerenityRest.given()
                            .contentType("application/json")
                            .body(MAPPER.writeValueAsString(altPayload));
                        optionalBearer.ifPresent(b -> altGiven.header("Authorization", "Bearer " + b));

                        Response altResp = altGiven.when().post(url);
                        log.info("Alt search returned status={} body={}", altResp.getStatusCode(),
                                 altResp.getBody() == null ? "" : altResp.getBody().asString());
                        if (altResp.getStatusCode() == 200) {
                            body = altResp.getBody() == null ? "" : altResp.getBody().asString();
                            lastResponseBody = body;
                        }
                    }

                    if (status == 200 && body != null && !body.isBlank() && !body.equals("[]")) {
                        JsonNode root = MAPPER.readTree(body);
                        JsonNode entries = root.isArray() ? root : root.path("entries");

                        int foundCount = 0;
                        if (entries.isArray()) {
                            String expectedDraftId = getCreatedDraftAccountId();
                            for (JsonNode rec : entries) {
                                if (!matchesCreatedBy(rec, exp.createdById, exp.createdByType)) {
                                    continue;
                                }

                                if (!exp.businessIdentifier.equals(rec.path("business_identifier").asText(null))) {
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
                                    // lightweight validation to preserve contract checks
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

    // Validate a matched record's shape and semantics
    private void validateRecord(JsonNode rec, String businessIdentifier) {

        @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
        final Map<String, String> BUSINESS_TO_CATEGORY = Map.ofEntries(
            Map.entry("Get Draft Account - Defendant", "Consultation"),
            Map.entry("Get Draft Account - Parent or Guardian", "Consultation"),
            Map.entry("Get Draft Account - Minor Creditor", "Consultation")
        );

        String expectedCategory = BUSINESS_TO_CATEGORY.getOrDefault(businessIdentifier, "Collection");
        // category must match expected
        assertEquals(expectedCategory, rec.path("category").asText(null),"category must be " + expectedCategory);

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

    private String buildSearchPayload(Expectation exp) throws Exception {
        Map<String,Object> payload = new HashMap<>();
        Map<String,Object> cb = new HashMap<>();
        cb.put("id", exp.createdById);
        cb.put("type", exp.createdByType);
        payload.put("created_by", cb);
        payload.put("business_identifier", exp.businessIdentifier);

        String draftId = getCreatedDraftAccountId();
        if (draftId != null && !draftId.isBlank() && !draftId.startsWith("<")) {
            payload.put("individual_id", draftId);
        }

        return MAPPER.writeValueAsString(payload);
    }

    private boolean matchesCreatedBy(JsonNode rec, String expectedId, String expectedType) {
        JsonNode cb = rec.path("created_by");
        String actualId = cb.path("id").asText(null);
        String actualType = cb.path("type").asText(null);

        if (expectedType == null || actualType == null) {
            // If either is null, be conservative and require equality (or null==null)
            if (!Objects.equals(expectedType, actualType)) {
                return false;
            }
        } else {
            if (!expectedType.equalsIgnoreCase(actualType)) {
                return false;
            }
        }

        if (expectedId == null) {
            return true;
        }

        if (expectedId.equals(actualId)) {
            return true;
        }

        // Allow numeric fallback (new OPAL user id style): expected Lxxxx but actual numeric id
        if (expectedId.startsWith("L") && actualId != null && actualId.matches("\\d+")) {
            log.warn("Expected created_by id '{}' but actual was '{}'. Accepting numeric fallback.",
                     expectedId, actualId);
            return true;
        }

        return false;
    }

    private String getCreatedDraftAccountId() {
        try {
            Object v = Serenity.sessionVariableCalled("CREATED_DRAFT_ACCOUNT_ID");
            return v == null ? null : String.valueOf(v);
        } catch (Exception e) {
            log.debug("Error retrieving CREATED_DRAFT_ACCOUNT_ID from Serenity session: {}", e.getMessage());
            return null;
        }
    }

    private static class Expectation {
        final String createdById;
        final String createdByType;
        final String businessIdentifier;
        final int expectedCount;

        Expectation(String id, String type, String bi, int count) {
            this.createdById = id;
            this.createdByType = type;
            this.businessIdentifier = bi;
            this.expectedCount = count;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Expectation other)) {
                return false;
            }
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
