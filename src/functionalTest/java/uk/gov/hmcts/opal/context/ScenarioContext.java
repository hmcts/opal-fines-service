package uk.gov.hmcts.opal.context;

import org.junit.jupiter.api.Assertions;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Holds the mutable state for a single functional-test scenario in a typed structure.
 */
public class ScenarioContext {

    private final List<String> draftAccountIds = new ArrayList<>();
    private final Map<String, String> draftAccountCreators = new LinkedHashMap<>();
    private final Map<String, String> rememberedEtags = new LinkedHashMap<>();
    private final Map<String, String> queuedHeaders = new LinkedHashMap<>();

    private String draftAccountCreatedAtTime = "";
    private String initialAccountStatusDate = "";
    private String currentUser = "";
    private TestHttpResponse latestHttpResponse;
    private String createdDefendantAccountId;
    private String defendantAccountEtag;

    /**
     * Resets the scenario context to a clean state.
     */
    public void reset() {
        draftAccountIds.clear();
        draftAccountCreators.clear();
        rememberedEtags.clear();
        queuedHeaders.clear();
        draftAccountCreatedAtTime = "";
        initialAccountStatusDate = "";
        currentUser = "";
        latestHttpResponse = null;
        createdDefendantAccountId = null;
        defendantAccountEtag = null;
    }

    /**
     * Records a created draft-account ID for the current scenario.
     *
     * @param id created draft-account identifier to remember.
     */
    public void addDraftAccountId(String id) {
        draftAccountIds.add(id);
    }

    /**
     * Returns every draft-account ID recorded for the current scenario.
     *
     * @return immutable snapshot of the recorded draft-account IDs.
     */
    public List<String> getDraftAccountIds() {
        return List.copyOf(draftAccountIds);
    }

    /**
     * Returns the only draft-account ID recorded for the current scenario, failing when the
     * scenario created none or more than one.
     *
     * @return the single created draft-account ID recorded for the scenario.
     */
    public String getOnlyDraftAccountIdOrFail() {
        Assertions.assertEquals(
            1,
            draftAccountIds.size(),
            "There should be only one draft account but found multiple: " + draftAccountIds
        );
        return draftAccountIds.getFirst();
    }

    /**
     * Returns the most recently created draft-account ID recorded for the current scenario.
     *
     * @return most recently created draft-account ID.
     */
    public String getLastDraftAccountIdOrFail() {
        if (draftAccountIds.isEmpty()) {
            throw new IllegalStateException("No recorded draft account IDs");
        }
        return draftAccountIds.getLast();
    }

    /**
     * Clears all remembered draft-account IDs for the current scenario.
     */
    public void clearDraftAccountIds() {
        draftAccountIds.clear();
        draftAccountCreators.clear();
    }

    /**
     * Remembers which authenticated user created a draft account in the current scenario.
     *
     * @param id created draft-account identifier.
     * @param creatorUser authenticated user who created the draft account.
     */
    public void rememberDraftAccountCreator(String id, String creatorUser) {
        draftAccountCreators.put(id, creatorUser);
    }

    /**
     * Returns the remembered creator user for the supplied draft account.
     *
     * @param id created draft-account identifier.
     * @param fallbackUser user to return when the creator was not explicitly recorded.
     * @return remembered creator user, or the supplied fallback when none has been recorded.
     */
    public String getDraftAccountCreatorOrDefault(String id, String fallbackUser) {
        return draftAccountCreators.getOrDefault(id, fallbackUser);
    }

    /**
     * Records the currently authenticated user for the scenario.
     *
     * @param currentUser authenticated user currently driving the scenario.
     */
    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * Returns the currently authenticated user for the scenario, or the supplied fallback when no
     * explicit user has been recorded yet.
     *
     * @param fallbackUser user to return when no explicit current user has been recorded.
     * @return current scenario user, or the supplied fallback when none has been recorded.
     */
    public String getCurrentUserOrDefault(String fallbackUser) {
        return currentUser == null || currentUser.isBlank() ? fallbackUser : currentUser;
    }

    /**
     * Records the `created_at` timestamp returned for the latest created draft account.
     *
     * @param createdAtTime created timestamp to remember.
     */
    public void setDraftAccountCreatedAtTime(String createdAtTime) {
        this.draftAccountCreatedAtTime = createdAtTime;
    }

    /**
     * Returns the remembered `created_at` timestamp for the current scenario.
     *
     * @return stored `created_at` timestamp, or an empty string when none has been recorded.
     */
    public String getDraftAccountCreatedAtTime() {
        return draftAccountCreatedAtTime;
    }

    /**
     * Records the initial `account_status_date` returned for the latest created draft account.
     *
     * @param initialAccountStatusDate initial account-status date to remember.
     */
    public void setInitialAccountStatusDate(String initialAccountStatusDate) {
        this.initialAccountStatusDate = initialAccountStatusDate;
    }

    /**
     * Returns the remembered initial `account_status_date` for the current scenario.
     *
     * @return stored initial account-status date, or an empty string when none has been recorded.
     */
    public String getInitialAccountStatusDate() {
        return initialAccountStatusDate;
    }

    /**
     * Stores the latest raw HTTP response produced by the low-level test client.
     *
     * @param latestHttpResponse response to remember.
     */
    public void setLatestHttpResponse(TestHttpResponse latestHttpResponse) {
        this.latestHttpResponse = latestHttpResponse;
    }

    /**
     * Returns and clears the latest remembered raw HTTP response.
     *
     * @return latest remembered raw HTTP response, or {@code null} when none has been recorded.
     */
    public TestHttpResponse consumeLatestHttpResponse() {
        TestHttpResponse response = latestHttpResponse;
        latestHttpResponse = null;
        return response;
    }

    /**
     * Returns and clears any queued custom request headers.
     *
     * @return queued request headers to apply to the next request.
     */
    public Map<String, String> consumeQueuedHeaders() {
        Map<String, String> headers = new LinkedHashMap<>(queuedHeaders);
        queuedHeaders.clear();
        return headers;
    }

    /**
     * Remembers an ETag value under a logical name for later reuse within the same scenario.
     *
     * @param name logical name used to retrieve the ETag later.
     * @param etag ETag value to remember.
     */
    public void rememberEtag(String name, String etag) {
        rememberedEtags.put(name, etag);
    }

    /**
     * Returns a previously remembered ETag for the supplied logical name.
     *
     * @param name logical name used to store the remembered ETag.
     * @return remembered ETag value, or {@code null} when no ETag has been stored under that name.
     */
    public String getRememberedEtag(String name) {
        return rememberedEtags.get(name);
    }

    /**
     * Stores the created defendant-account ID for the current scenario.
     *
     * @param createdDefendantAccountId created defendant-account identifier to remember.
     */
    public void setCreatedDefendantAccountId(String createdDefendantAccountId) {
        this.createdDefendantAccountId = createdDefendantAccountId;
    }

    /**
     * Returns the created defendant-account ID for the current scenario, failing when it has not
     * yet been recorded.
     *
     * @return created defendant-account identifier.
     */
    public String getCreatedDefendantAccountIdOrFail() {
        assertNotNull(createdDefendantAccountId, "No created defendant account ID found in scenario context");
        assertFalse(createdDefendantAccountId.isBlank(), "Created defendant account ID is blank");
        return createdDefendantAccountId;
    }

    /**
     * Stores the current defendant-account ETag for the scenario.
     *
     * @param defendantAccountEtag ETag returned by the latest defendant-account GET.
     */
    public void setDefendantAccountEtag(String defendantAccountEtag) {
        this.defendantAccountEtag = defendantAccountEtag;
    }

    /**
     * Returns the current defendant-account ETag for the scenario.
     *
     * @return remembered defendant-account ETag, or {@code null} when none has been recorded.
     */
    public String getDefendantAccountEtag() {
        return defendantAccountEtag;
    }
}
