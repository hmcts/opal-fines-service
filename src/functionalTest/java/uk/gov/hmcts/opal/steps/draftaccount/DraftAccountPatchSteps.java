package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import io.restassured.specification.RequestSpecification;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Defines Cucumber steps for patching draft accounts.
 */
public class DraftAccountPatchSteps extends BaseStepDef {

    // ───────────────── helpers ─────────────────

    /**
     * Return the most recently created draft-account ID.
     *
     * <p>Uses {@link java.util.List#getLast()} to retrieve the last element of the list. Throws an
     * {@link IllegalStateException} if there are no recorded IDs.</p>
     *
     * @return most recently created draft-account identifier.
     */
    private String lastCreatedIdOrFail() {
        return scenarioContext().getLastDraftAccountIdOrFail();
    }


    /**
     * Resolves the value to send in the {@code If-Match} header.
     *
     * <p>Rules (in order):</p> <ul> <li>{@code "$etag:<name>"} → use a remembered ETag stored under
     * that logical name in the typed scenario context (frozen).</li> <li>Bare digits (e.g., {@code 0},
     * {@code 12}) → send as strong quoted ETag (e.g., {@code "\"0\""}, {@code "\"12\""}).</li>
     * <li>Already strong quoted (starts/ends with {@code "}) → keep as-is.</li> <li>Starts with
     * {@code W/} → keep weak tag as provided.</li> <li>Anything else (non-blank) → wrap in quotes
     * to make a strong ETag.</li> <li>{@code null} or blank → return {@code null} (omit
     * header).</li> </ul>
     *
     * @param raw raw `If-Match` value from the scenario data.
     * @return resolved If-Match header value.
     */
    private String resolveIfMatch(String raw) {
        if (raw == null) {
            return null;
        }
        final String s = raw.trim();
        if (s.isEmpty()) {
            return null;
        }

        // Use a previously remembered, named ETag (frozen): $etag:<name>
        if (s.startsWith("$etag:")) {
            String key = s.substring("$etag:".length()).trim();
            String stored = scenarioContext().getRememberedEtag(key);
            return (stored == null || stored.isBlank()) ? null : stored;
        }

        // Already strong quoted?
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            return s;
        }

        // Bare digits → "123"
        if (s.matches("^\\d+$")) {
            return "\"" + s + "\"";
        }

        // Keep weak tags if explicitly provided (useful for negative tests)
        if (s.startsWith("W/")) {
            return s;
        }

        // Default: strong quote
        return "\"" + s + "\"";
    }

    /**
     * Builds the JSON body for a draft-account patch request from the supplied scenario data.
     *
     * @param m field values to include in the patch request.
     * @return JSON body for the patch request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    private JSONObject buildPatchBody(Map<String, String> m) throws JSONException {
        final JSONObject patch = new JSONObject();

        // business_unit_id (int if present)
        if (m.containsKey("business_unit_id")) {
            final String val = m.get("business_unit_id");
            if (val != null && !val.isBlank()) {
                patch.put("business_unit_id", Integer.parseInt(val));
            }
        }

        // direct fields (nullable)
        if (m.containsKey("account_status")) {
            final String v = m.get("account_status");
            patch.put("account_status", (v == null || v.isBlank()) ? JSONObject.NULL : v);
        }
        if (m.containsKey("validated_by")) {
            final String v = m.get("validated_by");
            patch.put("validated_by", (v == null || v.isBlank()) ? JSONObject.NULL : v);
        }

        // timeline entry
        final JSONObject timelineEntry = new JSONObject();
        final String validatedBy = m.get("validated_by");
        final String accountStatus = m.get("account_status");

        timelineEntry.put(
            "username",
            (validatedBy == null || validatedBy.isBlank()) ? JSONObject.NULL : validatedBy
        );
        timelineEntry.put(
            "status",
            (accountStatus == null || accountStatus.isBlank()) ? JSONObject.NULL : accountStatus
        );
        timelineEntry.put("status_date", ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

        if (m.containsKey("reason_text")) {
            final String v = m.get("reason_text");
            timelineEntry.put("reason_text", (v == null || v.isBlank()) ? JSONObject.NULL : v);
        }

        final JSONArray timelineDataArray = new JSONArray();
        timelineDataArray.put(timelineEntry);
        patch.put("timeline_data", timelineDataArray);

        return patch;
    }

    /**
     * Builds the base request specification for a draft-account patch request.
     *
     * @param body JSON body to send in the patch request.
     * @return request specification configured for the patch request.
     */
    private RequestSpecification patchSpec(JSONObject body) {
        return authorisedJsonRequest()
            .body(body.toString());
    }

    // ───────────────── steps ─────────────────

    /**
     * Patches the most recently created draft account with the field values supplied by the
     * scenario.
     *
     * @param data Cucumber table containing the patch values for the request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I patch the draft account with the following details")
    public void patchDraftAccount(DataTable data) throws JSONException {
        final Map<String, String> m = data.asMap(String.class, String.class);
        final JSONObject patchBody = buildPatchBody(m);

        final String ifMatch = resolveIfMatch(m.get("If-Match"));
        final String id = lastCreatedIdOrFail();

        RequestSpecification spec = patchSpec(patchBody);
        if (ifMatch != null && !ifMatch.isBlank()) {
            spec = spec.header("If-Match", ifMatch);
        }

        spec.when().patch(getTestUrl() + "/draft-accounts/" + id);
    }

    /**
     * Patches the specified draft account with the field values supplied by the scenario.
     *
     * @param draftAccountId draft-account identifier to use for the request.
     * @param data Cucumber table containing the patch values for the request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I patch the {string} draft account with the following details")
    public void patchDraftAccount(String draftAccountId, DataTable data) throws JSONException {
        final Map<String, String> m = data.asMap(String.class, String.class);
        final JSONObject patchBody = buildPatchBody(m);

        final String ifMatch = resolveIfMatch(m.get("If-Match"));

        RequestSpecification spec = patchSpec(patchBody);
        if (ifMatch != null && !ifMatch.isBlank()) {
            spec = spec.header("If-Match", ifMatch);
        }

        spec.when().patch(
            getTestUrl()
                + "/draft-accounts/"
                + draftAccountId
        );
    }

    /**
     * Attempts to patch a draft-account using an unsupported content type.
     */
    @When("I attempt to patch a draft account with an unsupported content type")
    public void patchDraftAccountWithUnsupportedContentType() {
        authorisedJsonRequest()
            .accept("text/plain")
            .when()
            .patch(getTestUrl() + "/draft-accounts/" + "1");
    }

    /**
     * Attempts to patch a draft-account with an unsupported media type.
     */
    @When("I attempt to patch a draft account with an unsupported media type")
    public void patchDraftAccountWithInvalidMediaType() {
        authorisedJsonRequest()
            .accept("application/json")
            .contentType("application/xml")
            .when()
            .patch(getTestUrl() + "/draft-accounts/" + "1");
    }

    /**
     * Sends a malformed patch request to exercise the draft-account internal-server-error path.
     */
    @When("I patch the draft account trying to provoke an internal server error")
    public void patchDraftAccountInternalServerError() {
        authorisedJsonRequest()
            .urlEncodingEnabled(false)
            .when()
            .patch(getTestUrl() + "/draft-accounts/%20");
    }

    /**
     * Attempts to patch the most recently created draft account using an invalid bearer token.
     *
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I attempt to update the draft account with an invalid token")
    public void patchDraftAccountWithInvalidToken() throws JSONException {
        final String id = lastCreatedIdOrFail();

        JSONObject patchBody = new JSONObject();
        patchBody.put("account_status", "Publishing Pending");
        patchBody.put("validated_by", "invalidToken");
        patchBody.put("timeline_data", new JSONArray());

        jsonRequestWithToken("invalidToken")
            .body(patchBody.toString())
            .when()
            .patch(getTestUrl() + "/draft-accounts/" + id);
    }
}
