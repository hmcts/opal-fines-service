package uk.gov.hmcts.opal.steps.draftaccount; // ← change if needed

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.utils.DraftAccountUtils;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

/**
 * Draft account deletion steps with ETag/If-Match support and verified cleanup.
 */
public class DraftAccountDeleteSteps extends BaseStepDef {
    private static final Logger log = LoggerFactory.getLogger(DraftAccountDeleteSteps.class);

    // ─────────────── helpers ───────────────

    private String urlForId(String id, boolean ignoreMissingResource) {
        return getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + id
            + (ignoreMissingResource ? "?ignore_missing=true" : "");
    }

    private RequestSpecification authed() {
        return SerenityRest.given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json");
    }

    private Response getById(String id) {
        return this.authed()
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + id);
    }

    /** Strong, quoted ETag or null if not found / header absent */
    private String fetchStrongEtag(String id) {
        Response resp = this.getById(id);
        int status = resp.getStatusCode();
        if (status == 404) return null;

        String etag = resp.getHeader("ETag");
        log.info("eTag is {}", etag);
        if (etag == null) return null;

        assertThat("ETag must be quoted", etag, matchesPattern("^\".+\"$"));
        assertThat("ETag must be strong (no W/)", etag.startsWith("W/"), is(false));
        return etag;
    }

    private Response deleteWithIfMatch(String id, String etag, boolean ignoreMissingResource) {
        RequestSpecification req = this.authed();
        if (etag != null) req.header("If-Match", etag); // strong, quoted
        return req.when().delete(this.urlForId(id, ignoreMissingResource));
    }

    /** Delete with If-Match; retry once on 409; assert success; verify GET=404. */
    private void deleteWithConcurrency(String id, boolean ignoreMissingResource) {
        String etag = this.fetchStrongEtag(id);
        log.info("eTag {} ", etag);
        if (etag == null && !ignoreMissingResource) {
            log.warn("No ETag for {}. Falling back to ignore_missing=true.", id);
            ignoreMissingResource = true;
        }

        Response del = this.deleteWithIfMatch(id, etag, ignoreMissingResource);
        int code = del.getStatusCode();

        if (code == 409 && !ignoreMissingResource) {
            log.info("409 on delete for {}. Refreshing ETag and retrying once.", id);
            String fresh = this.fetchStrongEtag(id);
            del = this.deleteWithIfMatch(id, fresh, false);
            code = del.getStatusCode();
        }

        assertThat("DELETE should succeed", code, anyOf(is(200), is(204), is(404)));
        assertThat("Follow-up GET should be 404 after delete", this.getById(id).getStatusCode(), is(404));
    }

    private String lastCreatedIdOrFail() {
        List<String> all = new ArrayList<>(DraftAccountUtils.getAllDraftAccountIds());
        assertThat("No recorded draft account IDs to delete", all, is(not(empty())));
        return all.getLast();
    }

    // ─────────────── steps (instance, not static) ───────────────

    /** Uses If-Match where available; fails if resource missing. */
    @When("I delete the draft account {string} using concurrency control")
    public void deleteByIdWithIfMatch(String draftAccountId) {
        this.deleteWithConcurrency(draftAccountId, false);
    }

    /** Ignores missing *resource* (adds ?ignore_missing=true). */
    @When("I delete the draft account {string} ignoring missing resource")
    public void deleteByIdIgnoringMissingResource(String draftAccountId) {
        this.deleteWithConcurrency(draftAccountId, true);
    }

    /** Same as above, but for the last created account */
    @When("I delete the last created draft account using concurrency control")
    public void deleteLastCreatedWithIfMatch() {
        this.deleteWithConcurrency(this.lastCreatedIdOrFail(), false);
    }

    /** Last created; ignore missing resource */
    @When("I delete the last created draft account ignoring missing resource")
    public void deleteLastCreatedIgnoringMissingResource() {
        this.deleteWithConcurrency(this.lastCreatedIdOrFail(), true);
    }

    /** Bulk clean-up of any accounts recorded during the run. */
    @Then("I delete the created draft accounts")
    public void deleteAllCreatedDraftAccounts() {
        this.actualDeleteAllCreatedDraftAccounts(false);
    }

    /** Exposed so hooks can call it */
    public void actualDeleteAllCreatedDraftAccounts(boolean ignoreMissingResource) {
        List<String> accounts = new ArrayList<>(DraftAccountUtils.getAllDraftAccountIds());
        if (accounts.isEmpty()) {
            log.info("No draft accounts to clean up.");
            return;
        }
        log.info("Cleaning up {} draft accounts: {}", accounts.size(), accounts);
        for (String id : accounts) {
            this.deleteWithConcurrency(id, ignoreMissingResource);
        }
        try {
            DraftAccountUtils.clearDraftAccountIds();
        } catch (Throwable t) {
            log.debug("clearDraftAccountIds() failed/absent (ignored): {}", t.getMessage());
        }
    }
}
