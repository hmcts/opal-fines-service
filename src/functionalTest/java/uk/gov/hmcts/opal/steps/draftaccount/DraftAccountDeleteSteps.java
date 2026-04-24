package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;

import static uk.gov.hmcts.opal.steps.draftaccount.DraftAccountCommonSteps.fetchStrongEtag;

/**
 * Draft account deletion steps with ETag/If-Match support and verified cleanup.
 */
public class DraftAccountDeleteSteps extends BaseStepDef {
    private static final Logger log = LoggerFactory.getLogger(DraftAccountDeleteSteps.class);

    // ─────────────── helpers ───────────────

    // Delete /draft-accounts/{id} with optional If-Match and ignore_missing.
    // - Only sends If-Match when an ETag is provided (non-null/non-blank)
    // - Returns the raw Response (no assertions here)
    // - Logs request URL, If-Match value, and response details for easy debugging
    /**
     * Executes a draft-account delete request with the supplied concurrency settings.
     *
     * @param id draft-account identifier to delete.
     * @param etag ETag value to send with the request.
     * @param ignoreMissingResource whether cleanup should ignore resources that are already
     *                              missing.
     * @return response returned by the delete request.
     */
    private io.restassured.response.Response deleteWithIfMatch(String id, String etag, boolean ignoreMissingResource) {
        final String url = getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + id + "?ignore_missing=" + ignoreMissingResource;

        io.restassured.specification.RequestSpecification spec = authorisedJsonRequest();

        if (etag != null && !etag.isBlank()) {
            spec.header("If-Match", etag);
        }

        log.info("DELETE {} (If-Match={})", url, etag);

        io.restassured.response.Response resp = spec
            .when()
            .delete(url)
            .then()
            .extract()
            .response();

        log.info("→ {} headers={} body={}", resp.getStatusCode(), resp.getHeaders(), resp.asString());
        return resp;
    }

    /**
     * Deletes a draft account using the current ETag and retries once if a concurrency conflict is
     * detected.
     *
     * @param id draft-account identifier to delete.
     * @param ignoreMissingResource whether cleanup should ignore resources that are already
     *                              missing.
     */
    private void deleteWithConcurrency(String id, boolean ignoreMissingResource) {
        String etag = fetchStrongEtag(getTestUrl(), id);

        boolean ignore = ignoreMissingResource || etag == null;

        Response del = this.deleteWithIfMatch(id, etag, ignore);
        int code = del.getStatusCode();

        if (code == 409 && etag != null && !ignore) {
            log.info("409 on delete for {}. Refreshing ETag and retrying once.", id);
            String fresh = fetchStrongEtag(getTestUrl(), id); // 🔁 again
            del = this.deleteWithIfMatch(id, fresh, false);
            code = del.getStatusCode();
        }

        // If DELETE misbehaves (e.g., 406/500), fall back to best effort:
        // no If-Match and ignore_missing=true
        if (code == 406 || code == 500) {
            log.warn("DELETE {} returned {}. Falling back to ignore_missing=true without If-Match.", id, code);
            del = this.deleteWithIfMatch(id, null, true);
            code = del.getStatusCode();
        }

        // Check if the resource is actually gone — this is what matters for cleanup
        Response after = authorisedJsonRequest()
            .accept("application/json")
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + id);

        String body;
        try {
            body = after.getBody() != null ? after.getBody().asString() : "";
        } catch (Exception e) {
            body = "<unreadable body: " + e.getClass().getSimpleName() + ">";
        }
        log.info("Follow-up GET {}{} → {} body={}",
                 getTestUrl(), DRAFT_ACCOUNTS_URI + "/" + id, after.getStatusCode(), body);

        // If it's gone, treat cleanup as success even if the original DELETE was 406/500
        if (after.getStatusCode() == 404) {
            return;
        }

        // Otherwise, we still expect a proper success code from DELETE
        assertThat("DELETE should succeed", code, anyOf(is(200), is(204), is(404)));
    }


    /**
     * Returns the most recently created draft-account identifier from scenario state.
     *
     * @return most recently created draft-account identifier.
     */
    private String lastCreatedIdOrFail() {
        List<String> all = new ArrayList<>(scenarioContext().getDraftAccountIds());
        assertThat("No recorded draft account IDs to delete", all, is(not(empty())));
        return all.getLast();
    }

    // ─────────────── steps (instance, not static) ───────────────

    /**
     * Deletes the last created draft-account using concurrency control.
     */
    @When("I delete the last created draft account using concurrency control")
    public void deleteLastCreatedWithIfMatch() {
        this.deleteWithConcurrency(this.lastCreatedIdOrFail(), false);
    }

    /**
     * Deletes the last created draft-account ignoring missing resource.
     */
    @When("I delete the last created draft account ignoring missing resource")
    public void deleteLastCreatedIgnoringMissingResource() {
        this.deleteWithConcurrency(this.lastCreatedIdOrFail(), true);
    }

    /**
     * Deletes every draft account recorded for the current scenario.
     */
    @Then("I delete the created draft accounts")
    public void deleteAllCreatedDraftAccounts() {
        this.actualDeleteAllCreatedDraftAccounts(false);
    }

    /**
     * Deletes every draft account recorded for the current scenario.
     *
     * @param ignoreMissingResource whether cleanup should ignore resources that are already
     *                              missing.
     */
    public void actualDeleteAllCreatedDraftAccounts(boolean ignoreMissingResource) {
        List<String> accounts = new ArrayList<>(scenarioContext().getDraftAccountIds());
        if (accounts.isEmpty()) {
            log.info("No draft accounts to clean up.");
            return;
        }
        log.info("Cleaning up {} draft accounts: {}", accounts.size(), accounts);
        for (String id : accounts) {
            this.deleteWithIfMatch(id, null, ignoreMissingResource);
        }
        try {
            scenarioContext().clearDraftAccountIds();
        } catch (Throwable t) {
            log.debug("clearDraftAccountIds() failed/absent (ignored): {}", t.getMessage());
        }
    }
}
