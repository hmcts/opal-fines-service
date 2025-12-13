package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import io.restassured.specification.RequestSpecification;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.utils.DraftAccountUtils;
import uk.gov.hmcts.opal.utils.RequestSupport;

public class DraftAccountPatchSteps extends BaseStepDef {

    // ───────────────── helpers ─────────────────

    /**
     * Return the most recently created draft-account ID.
     *
     * <p>Uses {@link java.util.List#getLast()} to retrieve the last element of the list.
     * Throws an {@link IllegalStateException} if there are no recorded IDs.</p>
     *
     * @return the last created draft-account ID as a {@code String}
     */
    private String lastCreatedIdOrFail() {
        final List<String> all = DraftAccountUtils.getAllDraftAccountIds();
        if (all == null || all.isEmpty()) {
            throw new IllegalStateException("No recorded draft account IDs");
        }
        return all.getLast();
    }


    /**
     * Resolve the value to send in the {@code If-Match} header.
     *
     * <p>Rules (in order):</p>
     * <ul>
     *   <li>{@code "$etag:<name>"} → use a remembered ETag stored under
     *       {@code etag:<name>} in the Serenity session (frozen).</li>
     *   <li>Bare digits (e.g., {@code 0}, {@code 12}) → send as strong quoted ETag
     *       (e.g., {@code "\"0\""}, {@code "\"12\""}).</li>
     *   <li>Already strong quoted (starts/ends with {@code "}) → keep as-is.</li>
     *   <li>Starts with {@code W/} → keep weak tag as provided.</li>
     *   <li>Anything else (non-blank) → wrap in quotes to make a strong ETag.</li>
     *   <li>{@code null} or blank → return {@code null} (omit header).</li>
     * </ul>
     *
     * @param raw the raw table value for {@code If-Match} (may be {@code null})
     * @return the header value to send (e.g., {@code "\"123\""}, {@code W/"abc"}), or {@code null} to omit
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
            String stored = Serenity.sessionVariableCalled("etag:" + key);
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
     * Build the PATCH JSON body from the DataTable map.
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

    // ───────────────── steps ─────────────────

    @When("I patch the draft account with the following details")
    public void patchDraftAccount(DataTable data) throws JSONException {
        final Map<String, String> m = data.asMap(String.class, String.class);
        final JSONObject patchBody = buildPatchBody(m);

        final String ifMatch = resolveIfMatch(m.get("If-Match"));
        final String id = lastCreatedIdOrFail();

        RequestSpecification spec = SerenityRest
            .given()
            .spec(RequestSupport.patchRequestSpec("/draft-accounts/" + id, patchBody.toString()).build());
        if (ifMatch != null && !ifMatch.isBlank()) {
            spec = spec.header("If-Match", ifMatch);
        }
        RequestSupport.responseProcessor(spec.when().patch().then());
    }

    @When("I patch the {string} draft account with the following details")
    public void patchDraftAccount(String draftAccountId, DataTable data) throws JSONException {
        final Map<String, String> m = data.asMap(String.class, String.class);
        final JSONObject patchBody = buildPatchBody(m);

        final String ifMatch = resolveIfMatch(m.get("If-Match"));

        RequestSpecification spec = SerenityRest
            .given()
            .spec(RequestSupport.patchRequestSpec("/draft-accounts/" + draftAccountId, patchBody.toString()).build());
        if (ifMatch != null && !ifMatch.isBlank()) {
            spec = spec.header("If-Match", ifMatch);
        }
        RequestSupport.responseProcessor(spec.when().patch().then());
    }

    @When("I attempt to patch a draft account with an unsupported content type")
    public void patchDraftAccountWithUnsupportedContentType() {
        RequestSupport.responseProcessor(
            SerenityRest
                .given()
                .spec(RequestSupport.patchRequestSpec("/draft-accounts/1", "").build())
                .accept("text/plain")
                .when()
                .patch()
                .then()
        );
    }

    @When("I attempt to patch a draft account with an unsupported media type")
    public void patchDraftAccountWithInvalidMediaType() {
        RequestSupport.responseProcessor(
            SerenityRest
                .given()
                .urlEncodingEnabled(false)
                .spec(RequestSupport.patchRequestSpec("/draft-accounts/1", "").build())
                .contentType("application/xml")
                .when()
                .patch()
                .then()
        );
    }

    @When("I patch the draft account trying to provoke an internal server error")
    public void patchDraftAccountInternalServerError() {
        RequestSupport.responseProcessor(
            SerenityRest
                .given()
                .spec(RequestSupport.patchRequestSpec("/draft-accounts/%20", "").build())
                .urlEncodingEnabled(false)
                .when()
                .patch()
                .then()
        );
    }
}
