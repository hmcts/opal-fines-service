package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.Then;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;

public class LoggingSteps extends BaseStepDef {

    private static final Logger log = LoggerFactory.getLogger(LoggingSteps.class);
    private static final String SEARCH_PATH = "/test-support/search";

    /**
     * Poll POST /test-support/search until: - status == 200 AND (body non-empty and contains a matching record) OR -
     * timeout expires. Request includes created_by.id and created_by.type (the logging service requires both).
     */
    @Then("the logging service contains an entry with created_by id {string}, "
        + "type {string} and business_identifier {string}")
    public void loggingServiceContainsEntry(String createdById, String createdByType, String businessIdentifier)
        throws InterruptedException, JSONException {

        JSONObject searchBody = new JSONObject()
            .put("created_by", new JSONObject().put("id", createdById).put("type", createdByType))
            .put("business_identifier", businessIdentifier);

        String url = getLoggingTestUrl() + SEARCH_PATH;

        // Timeout configurable via env var, default 60s
        int timeoutSeconds = 30;
        String envTimeout = System.getenv("LOG_SEARCH_TIMEOUT_SECONDS");
        if (envTimeout != null && !envTimeout.isBlank()) {
            try {
                timeoutSeconds = Integer.parseInt(envTimeout);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Log search timeout is not a number", e);
            }
        }

        Duration timeout = Duration.ofSeconds(timeoutSeconds);
        Duration pollInterval = Duration.ofSeconds(2); // tune if needed

        Instant deadline = Instant.now().plus(timeout);
        Exception lastException = null;
        Optional<String> optionalBearer = Optional.ofNullable(System.getenv("OPAL_LOGGING_SERVICE_BEARER"));

        log.info("Polling logging service {} for created_by.id='{}' type='{}' business_identifier='{}' up to {}s",
            url, createdById, createdByType, businessIdentifier, timeoutSeconds);

        boolean found = false;

        while (Instant.now().isBefore(deadline)) {
            try {
                var given = SerenityRest.given().contentType("application/json").body(searchBody.toString());
                optionalBearer.ifPresent(b -> given.header("Authorization", "Bearer " + b));

                Response response = given.when().post(url);

                int status = response.getStatusCode();
                String body = response.getBody() != null ? response.getBody().asString() : "";

                log.info("Logging search attempt: status={}, body={}", status, body);

                if (status == 200) {
                    // If body empty array or blank, keep polling until it has content or until timeout
                    if (body != null && !body.isBlank() && !body.equals("[]")) {

                        // parse list and assert at least one matching record exists
                        List<Map<String, Object>> results =
                            SerenityRest.then().extract().body().jsonPath().getList("$");

                        if (results != null && !results.isEmpty()) {
                            found = results.stream().anyMatch(rec -> {
                                try {
                                    Object cbObj = rec.get("created_by");
                                    if (!(cbObj instanceof Map<?, ?> cbMap)) {
                                        return false;
                                    }
                                    Object idObj = cbMap.get("id");
                                    Object typeObj = cbMap.get("type");
                                    Object biObj = rec.get("business_identifier");
                                    return createdById.equals(String.valueOf(idObj))
                                        && createdByType.equals(String.valueOf(typeObj))
                                        && businessIdentifier.equals(String.valueOf(biObj));
                                } catch (Exception e) {
                                    return false;
                                }
                            });
                        }
                        if (found) {
                            log.info("Found matching logging entry.");
                            return;
                        }
                    }
                } else {
                    // non-200: record last exception info for debugging and continue
                    lastException = new RuntimeException("status=" + status + " body=" + body);
                }

            } catch (Exception e) {
                lastException = e;
                log.warn("Exception while polling logging service: {}", e.toString());
            }

            //noinspection BusyWait
            Thread.sleep(pollInterval.toMillis());
        }

        // If we exit the loop without finding a matching record, fail with helpful info
        String err = String.format(
            "Did not find logging entry with created_by.id='%s' type='%s' "
                + "and business_identifier='%s' within %d seconds",
            createdById, createdByType, businessIdentifier, timeoutSeconds
        );
        if (lastException != null) {
            err += ". Last exception/message: " + lastException.getMessage();
        }
        fail(err);
    }
}
