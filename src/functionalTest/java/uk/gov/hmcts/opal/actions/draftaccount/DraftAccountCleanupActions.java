package uk.gov.hmcts.opal.actions.draftaccount;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;
import static uk.gov.hmcts.opal.utils.draftaccount.DraftAccountEtagHelper.fetchStrongEtag;

/**
 * Encapsulates draft-account deletion and cleanup behaviour that is reused by both step
 * definitions and scenario hooks.
 */
public class DraftAccountCleanupActions extends BaseStepDef {
    private static final Logger log = LoggerFactory.getLogger(DraftAccountCleanupActions.class);

    /**
     * Deletes the most recently created draft account using concurrency control.
     *
     * @param ignoreMissingResource whether cleanup should ignore resources that are already
     *                              missing.
     */
    public void deleteLastCreatedDraftAccount(boolean ignoreMissingResource) {
        String draftAccountId = lastCreatedIdOrFail();
        deleteWithConcurrency(
            draftAccountId,
            scenarioContext().getDraftAccountCreatorOrDefault(draftAccountId, BearerTokenStepDef.DEFAULT_USER),
            ignoreMissingResource
        );
    }

    /**
     * Deletes every draft account recorded for the current scenario.
     *
     * @param ignoreMissingResource whether cleanup should ignore resources that are already
     *                              missing.
     */
    public void deleteAllCreatedDraftAccounts(boolean ignoreMissingResource) {
        List<String> accounts = new ArrayList<>(scenarioContext().getDraftAccountIds());
        if (accounts.isEmpty()) {
            log.info("No draft accounts to clean up.");
            return;
        }
        log.info("Cleaning up {} draft accounts: {}", accounts.size(), accounts);
        for (String id : accounts) {
            deleteWithConcurrency(
                id,
                scenarioContext().getDraftAccountCreatorOrDefault(id, BearerTokenStepDef.DEFAULT_USER),
                ignoreMissingResource
            );
        }
        try {
            scenarioContext().clearDraftAccountIds();
        } catch (Throwable t) {
            log.debug("clearDraftAccountIds() failed/absent (ignored): {}", t.getMessage());
        }
    }

    /**
     * Asserts that the most recently created draft account can no longer be retrieved.
     */
    public void assertLastCreatedDraftAccountDeleted() {
        String draftAccountId = lastCreatedIdOrFail();
        String creatorUser = scenarioContext().getDraftAccountCreatorOrDefault(
            draftAccountId,
            BearerTokenStepDef.DEFAULT_USER
        );

        Response response = authorisedJsonRequestForUser(creatorUser)
            .accept("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);

        assertEquals(404, response.getStatusCode(), "Expected the draft account to be deleted");
    }

    /**
     * Executes a draft-account delete request with the supplied concurrency settings.
     *
     * @param id draft-account identifier to delete.
     * @param etag ETag value to send with the request.
     * @param user user whose token should be used to perform the delete.
     * @param ignoreMissingResource whether cleanup should ignore resources that are already
     *                              missing.
     * @return response returned by the delete request.
     */
    private Response deleteWithIfMatch(String id, String etag, String user, boolean ignoreMissingResource) {
        String url = getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + id + "?ignore_missing=" + ignoreMissingResource;

        RequestSpecification spec = authorisedJsonRequestForUser(user);
        if (etag != null && !etag.isBlank()) {
            spec.header("If-Match", etag);
        }

        log.info("DELETE {} as {} (If-Match={})", url, user, etag);

        Response response = spec
            .when()
            .delete(url)
            .then()
            .extract()
            .response();

        log.info("→ {} headers={} body={}", response.getStatusCode(), response.getHeaders(), response.asString());
        return response;
    }

    /**
     * Deletes a draft account using the current ETag and retries once if a concurrency conflict is
     * detected.
     *
     * @param id draft-account identifier to delete.
     * @param user user whose token should be used to perform the delete.
     * @param ignoreMissingResource whether cleanup should ignore resources that are already
     *                              missing.
     */
    private void deleteWithConcurrency(String id, String user, boolean ignoreMissingResource) {
        String etag = fetchStrongEtag(getTestUrl(), id, BearerTokenStepDef.getAccessTokenForUser(user));
        boolean ignore = ignoreMissingResource || etag == null;

        Response deleteResponse = deleteWithIfMatch(id, etag, user, ignore);
        int code = deleteResponse.getStatusCode();

        if (code == 409 && etag != null && !ignore) {
            log.info("409 on delete for {}. Refreshing ETag and retrying once.", id);
            String refreshedEtag = fetchStrongEtag(getTestUrl(), id, BearerTokenStepDef.getAccessTokenForUser(user));
            deleteResponse = deleteWithIfMatch(id, refreshedEtag, user, false);
            code = deleteResponse.getStatusCode();
        }

        if (code == 406 || code == 500) {
            log.warn("DELETE {} returned {}. Falling back to ignore_missing=true without If-Match.", id, code);
            deleteResponse = deleteWithIfMatch(id, null, user, true);
            code = deleteResponse.getStatusCode();
        }

        Response followUpGet = authorisedJsonRequestForUser(user)
            .accept("application/json")
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + id);

        String body;
        try {
            body = followUpGet.getBody() != null ? followUpGet.getBody().asString() : "";
        } catch (Exception e) {
            body = "<unreadable body: " + e.getClass().getSimpleName() + ">";
        }
        log.info("Follow-up GET {}{} → {} body={}",
                 getTestUrl(), DRAFT_ACCOUNTS_URI + "/" + id, followUpGet.getStatusCode(), body);

        if (followUpGet.getStatusCode() == 404) {
            return;
        }

        assertThat("DELETE should succeed", code, anyOf(is(200), is(204), is(404)));
    }

    /**
     * Builds an authorised JSON request using the access token for the supplied user.
     *
     * @param user user whose token should be attached to the request.
     * @return request specification configured for the supplied user.
     */
    private RequestSpecification authorisedJsonRequestForUser(String user) {
        return jsonRequestWithToken(BearerTokenStepDef.getAccessTokenForUser(user));
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
}
