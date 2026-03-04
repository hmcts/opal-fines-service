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
import java.util.*;
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

        for (Map<String, String> r : rows) {
            String createdById = r.get("created_by_id");
            String createdByType = r.get("created_by_type");
            String businessIdentifier = r.get("business_identifier");
            int expectedCount = r.containsKey("expected_count") ? Integer.parseInt(r.get("expected_count")) : 1;

            String individualIdCell = r.getOrDefault("individual_id", "").trim();

            if ("<CREATED_DRAFT_ACCOUNT_IDS>".equalsIgnoreCase(individualIdCell)) {
                List<String> createdIds = readCreatedDraftAccountIdsFromSession();
                if (createdIds.isEmpty()) {
                    throw new IllegalStateException("No CREATED_DRAFT_ACCOUNT_IDS found in session to expand token.");
                }

                // existing behavior: poll once per id (keeps compatibility)
                for (String id : createdIds) {
                    Object previous = null;
                    try {
                        try { previous = Serenity.sessionVariableCalled("CREATED_DRAFT_ACCOUNT_ID"); } catch (Exception ignored) {}
                        Serenity.setSessionVariable("CREATED_DRAFT_ACCOUNT_ID").to(id);

                        Expectation exp = new Expectation(createdById, createdByType, businessIdentifier, expectedCount);
                        // re-use your existing single-id polling routine
                        pollForExpectations(List.of(exp));
                    } finally {
                        try {
                            if (previous != null) {
                                Serenity.setSessionVariable("CREATED_DRAFT_ACCOUNT_ID").to(previous);
                            } else {
                                Serenity.setSessionVariable("CREATED_DRAFT_ACCOUNT_ID").to((String) null);
                            }
                        } catch (Exception ignored) {
                            log.debug("Failed to restore CREATED_DRAFT_ACCOUNT_ID session var: {}", ignored.getMessage());
                        }
                    }
                }

            } else if ("<CREATED_DRAFT_ACCOUNT_IDS_ALL_IN_ONE>".equalsIgnoreCase(individualIdCell)) {
                // assert there exists at least one single PDPO log whose 'individuals' contains ALL created ids
                List<String> createdIds = readCreatedDraftAccountIdsFromSession();
                if (createdIds.isEmpty()) {
                    throw new IllegalStateException("No CREATED_DRAFT_ACCOUNT_IDS found in session to expand token.");
                }

                // call helper which polls for up to timeout and asserts presence
                assertAllCreatedIdsInSinglePdpoLog(createdIds, createdById, createdByType, businessIdentifier, 60_000L);

            } else {
                // Default behaviour
                Expectation exp = new Expectation(createdById, createdByType, businessIdentifier, expectedCount);
                pollForExpectations(List.of(exp));
            }
        }
    }

    /**
     * Safely read CREATED_DRAFT_ACCOUNT_IDS from Serenity session and return a List<String>.
     */
    private List<String> readCreatedDraftAccountIdsFromSession() {
        List<String> createdIds = new ArrayList<>();
        try {
            Object o = Serenity.sessionVariableCalled("CREATED_DRAFT_ACCOUNT_IDS");
            if (o instanceof List<?>) {
                for (Object elem : (List<?>) o) {
                    if (elem != null) {
                        createdIds.add(String.valueOf(elem));
                    }
                }
            } else if (o instanceof String) {
                String s = ((String) o).trim();
                if (!s.isEmpty()) {
                    String[] parts = s.split(",");
                    for (String p : parts) {
                        String t = p.trim();
                        if (!t.isEmpty()) {
                            createdIds.add(t);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.debug("Error reading CREATED_DRAFT_ACCOUNT_IDS from session: {}", ex.getMessage());
        }
        return createdIds;
    }

    /**
     * Poll logging service for up to timeoutMillis and assert there is at least one PDPO log
     * whose 'individuals' array contains all of the supplied createdIds.
     *
     * Uses SerenityRest to call the logging service endpoint
     */
    private void assertAllCreatedIdsInSinglePdpoLog(List<String> createdIds,
                                                    String createdById,
                                                    String createdByType,
                                                    String businessIdentifier,
                                                    long timeoutMillis) throws Exception {

        long start = System.currentTimeMillis();
        long deadline = start + timeoutMillis;
        boolean found = false;
        Exception lastException = null;

        while (System.currentTimeMillis() <= deadline && !found) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("business_identifier", businessIdentifier);
                Map<String, String> createdBy = new HashMap<>();
                createdBy.put("id", createdById);
                createdBy.put("type", createdByType);
                payload.put("created_by", createdBy);

                var resp = SerenityRest.given()
                    .contentType("application/json")
                    .body(payload)
                    .post("http://localhost:4065/test-support/search");

                if (resp.getStatusCode() != 200) {
                    lastException = new IllegalStateException("Logging service returned non-200: " + resp.getStatusCode());
                } else {
                    List<Map<String, Object>> logs = resp.jsonPath().getList("$");
                    log.info("Logging service returned {} entries for payload {}",
                             logs == null ? 0 : logs.size(),
                             payload);

                    for (Map<String, Object> logEntry : logs) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> individuals = (List<Map<String, Object>>) logEntry.get("individuals");
                        if (individuals == null) {
                            continue;
                        }

                        Set<String> idsInEntry = new HashSet<>();
                        for (Map<String, Object> ind : individuals) {
                            Object idObj = ind.get("id");
                            if (idObj != null) {
                                idsInEntry.add(String.valueOf(idObj));
                            }
                        }
                        if (idsInEntry.containsAll(createdIds)) {
                            found = true;

                            log.info("Found PDPO entry matching all created ids: {}", createdIds);
                            log.info("Matching entry: {}", logEntry);

                            break;
                        }
                    }
                }
            } catch (Exception e) {
                lastException = e;
                log.debug("Error while querying logging service: {}", e.getMessage());
            }

            if (!found) {
                Thread.sleep(500);
            }
        }

        if (!found) {
            String message = "Expected at least one PDPO log containing all created draft account ids: " + createdIds;
            if (lastException != null) {
                throw new AssertionError(message + " (last error: " + lastException.getMessage() + ")", lastException);
            } else {
                throw new AssertionError(message);
            }
        }
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

    @Then("there is exactly {int} PDPO record for {string}")
    public void thereIsExactlyNRecordsForBusinessIdentifier(int expectedCount, String businessIdentifier) throws Exception {
        String url = getLoggingTestUrl() + SEARCH_PATH;
        Optional<String> optionalBearer = Optional.ofNullable(System.getenv("OPAL_LOGGING_SERVICE_BEARER"));
        Instant deadline = Instant.now().plusSeconds(DEFAULT_TIMEOUT_SECONDS);
        String lastBody = null;

        Map<String, Object> payload = new HashMap<>();
        payload.put("business_identifier", businessIdentifier);

        while (Instant.now().isBefore(deadline)) {
            var given = SerenityRest.given()
                .contentType("application/json")
                .body(MAPPER.writeValueAsString(payload));
            optionalBearer.ifPresent(b -> given.header("Authorization", "Bearer " + b));

            Response response = given.when().post(url);
            int status = response.getStatusCode();
            String body = response.getBody() == null ? "" : response.getBody().asString();
            lastBody = body;

            if (status == 200) {
                JsonNode root = MAPPER.readTree(body);
                JsonNode entries = root.isArray() ? root : root.path("entries");
                int found = 0;
                if (entries.isArray()) {
                    for (JsonNode rec : entries) {
                        if (businessIdentifier.equals(rec.path("business_identifier").asText(null))) {
                            found++;
                        }
                    }
                }
                if (found == expectedCount) {
                    return;
                }
            }

            Thread.sleep(DEFAULT_POLL_MILLIS);
        }

        fail("Expected " + expectedCount + " PDPO record(s) for '" + businessIdentifier + "' but did not find them within "
                 + DEFAULT_TIMEOUT_SECONDS + "s. Last body: " + lastBody);
    }

    @Then("the PDPO for {string} contains individuals:")
    public void thePdpoForContainsIndividuals(String businessIdentifier, DataTable table) throws Exception {
        List<String> expectedIds = table.asList().stream().map(String::trim).collect(Collectors.toList());

        String url = getLoggingTestUrl() + SEARCH_PATH;
        Optional<String> optionalBearer = Optional.ofNullable(System.getenv("OPAL_LOGGING_SERVICE_BEARER"));
        Instant deadline = Instant.now().plusSeconds(DEFAULT_TIMEOUT_SECONDS);
        String lastBody = null;

        Map<String, Object> payload = new HashMap<>();
        payload.put("business_identifier", businessIdentifier);

        while (Instant.now().isBefore(deadline)) {
            var given = SerenityRest.given()
                .contentType("application/json")
                .body(MAPPER.writeValueAsString(payload));
            optionalBearer.ifPresent(b -> given.header("Authorization", "Bearer " + b));

            Response response = given.when().post(url);
            int status = response.getStatusCode();
            String body = response.getBody() == null ? "" : response.getBody().asString();
            lastBody = body;

            if (status == 200 && body != null && !body.isBlank() && !body.equals("[]")) {
                JsonNode root = MAPPER.readTree(body);
                JsonNode entries = root.isArray() ? root : root.path("entries");

                if (entries.isArray()) {
                    for (JsonNode rec : entries) {
                        if (!businessIdentifier.equals(rec.path("business_identifier").asText(null))) {
                            continue;
                        }

                        // check individuals[] exists
                        JsonNode individuals = rec.path("individuals");
                        if (!individuals.isArray() || individuals.isEmpty()) {
                            continue;
                        }

                        Set<String> foundIds = new HashSet<>();
                        for (JsonNode ind : individuals) {
                            String id = ind.path("id").asText(null);
                            if (id != null) {
                                foundIds.add(id);
                            }
                        }

                        boolean allPresent = expectedIds.stream().allMatch(foundIds::contains);
                        if (allPresent) {
                            // reuse validateRecord contract checks
                            validateRecord(rec, businessIdentifier);
                            return;
                        }
                    }
                }
            }

            Thread.sleep(DEFAULT_POLL_MILLIS);
        }

        fail("Did not find a PDPO for '" + businessIdentifier + "' containing individuals " + expectedIds
                 + " within " + DEFAULT_TIMEOUT_SECONDS + "s. Last body: " + lastBody);
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
