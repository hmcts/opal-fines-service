package uk.gov.hmcts.opal.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

/**
 * Defines Cucumber steps and helper logic for PDPO logging assertions.
 */
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

    /**
     * Stores an invalid bearer token in the Serenity session for the current scenario.
     */
    @When("I set an invalid token manually")
    public void setInvalidTokenManually() {
        BearerTokenStepDef.setTokenOverride("invalid-token");
    }

}
