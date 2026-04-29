package uk.gov.hmcts.opal.actions.draftaccount;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import net.serenitybdd.rest.SerenityRest;
import org.hamcrest.MatcherAssert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.utils.TestHttpClient;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsBlankString.blankOrNullString;
import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;

/**
 * Encapsulates reusable Draft-Account API calls and the scenario-state updates that accompany
 * those calls.
 */
public class DraftAccountActions extends BaseStepDef {

    private static final Logger log = LoggerFactory.getLogger(DraftAccountActions.class);

    private final DraftAccountRequestFactory requestFactory = new DraftAccountRequestFactory();

    /**
     * Creates a draft account using the supplied scenario values.
     *
     * @param accountData field values used to build the draft-account create payload.
     * @return API response returned by the create request.
     * @throws JSONException if the JSON request body cannot be assembled.
     * @throws IOException if a supporting draft-account fixture cannot be loaded.
     */
    public Response createDraftAccount(Map<String, String> accountData) throws JSONException, IOException {
        JSONObject postBody = requestFactory.buildCreateRequestBody(accountData);

        return loggedAuthorisedJsonRequest()
            .body(postBody.toString())
            .when()
            .post(getTestUrl() + DRAFT_ACCOUNTS_URI);
    }

    /**
     * Creates each draft account described by the supplied rows and remembers every created ID in
     * scenario state for later steps and cleanup.
     *
     * @param accountRows one row per draft account to create.
     * @throws JSONException if any JSON request body cannot be assembled.
     * @throws IOException if a supporting draft-account fixture cannot be loaded.
     */
    public void createDraftAccountsAndStoreIds(
        List<Map<String, String>> accountRows
    ) throws JSONException, IOException {
        for (Map<String, String> row : accountRows) {
            createDraftAccount(row);
            SerenityRest.then().assertThat().statusCode(201);
            storeCreatedDraftAccountIdFromLastResponse();
        }
    }

    /**
     * Creates a draft account using the low-level HTTP client rather than SerenityRest.
     *
     * @param accountData field values used to build the draft-account create payload.
     * @throws JSONException if the JSON request body cannot be assembled.
     * @throws IOException if a supporting draft-account fixture cannot be loaded.
     */
    public void createDraftAccountUsingRawHttpClient(
        Map<String, String> accountData
    ) throws JSONException, IOException {
        JSONObject postBody = requestFactory.buildCreateRequestBody(accountData);

        scenarioContext().setLatestHttpResponse(
            TestHttpClient.request(
                "POST",
                getTestUrl() + DRAFT_ACCOUNTS_URI,
                Map.of(
                    "Accept", "*/*",
                    "Authorization", "Bearer " + uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken(),
                    "Content-Type", "application/json"
                ),
                postBody.toString()
            )
        );
    }

    /**
     * Extracts the created draft-account ID from the latest create response and stores it in both
     * typed scenario state and the shared created-ID list.
     */
    public void storeCreatedDraftAccountIdFromLastResponse() {
        storeCreatedDraftAccountId(SerenityRest.lastResponse());
    }

    /**
     * Extracts the created draft-account ID from the supplied create response and stores it in
     * scenario state for later steps and cleanup.
     *
     * @param response create response returned by the draft-account API.
     */
    public void storeCreatedDraftAccountId(Response response) {
        log.info("CREATE status={}", response.getStatusCode());
        log.info("CREATE headers={}", response.getHeaders());
        log.info("CREATE body={}", response.asString());

        String id = null;

        try {
            Object idObj = response.jsonPath().get("draft_account_id");
            if (idObj != null) {
                id = String.valueOf(idObj);
            }
        } catch (Exception ignored) {
            // No JSON or no field; fall through to Location.
        }

        if (id == null || id.isBlank()) {
            String location = response.getHeader("Location");
            if (location != null && !location.isBlank()) {
                id = location.substring(location.lastIndexOf('/') + 1);
            }
        }

        MatcherAssert.assertThat(
            "Create must include draft_account_id in body or a Location header with the ID",
            id,
            not(blankOrNullString())
        );

        scenarioContext().addDraftAccountId(id);
        String creatorUser = scenarioContext().getCurrentUserOrDefault(BearerTokenStepDef.DEFAULT_USER);
        scenarioContext().rememberDraftAccountCreator(id, creatorUser);
        log.info("Stored draft id={} for creatorUser={} in scenario context (draftAccountIds={})",
                 id, creatorUser, scenarioContext().getDraftAccountIds());
    }

    /**
     * Stores the `created_at` timestamp from the supplied draft-account response in scenario
     * state for later comparison.
     *
     * @param response response whose `created_at` field should be remembered.
     */
    public void storeDraftAccountCreatedAtTime(Response response) {
        scenarioContext().setDraftAccountCreatedAtTime(response.jsonPath().getString("created_at"));
    }

    /**
     * Stores the initial `account_status_date` from the supplied draft-account response in
     * scenario state for later comparison.
     *
     * @param response response whose `account_status_date` field should be remembered.
     */
    public void storeInitialAccountStatusDate(Response response) {
        scenarioContext().setInitialAccountStatusDate(response.jsonPath().getString("account_status_date"));
    }

    /**
     * Returns the only draft-account ID recorded for the current scenario, failing when the
     * scenario created none or more than one.
     *
     * @return the single created draft-account ID recorded for the scenario.
     */
    public String onlyCreatedDraftAccountIdOrFail() {
        return scenarioContext().getOnlyDraftAccountIdOrFail();
    }

    /**
     * Retrieves a draft account by identifier.
     *
     * @param draftAccountId draft-account identifier to request.
     * @return API response returned by the GET request.
     */
    public Response getDraftAccount(String draftAccountId) {
        return authorisedJsonRequest()
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
    }

    /**
     * Retrieves the single draft account created by the current scenario.
     *
     * @return API response returned by the GET request.
     */
    public Response getSingleCreatedDraftAccount() {
        return getDraftAccount(onlyCreatedDraftAccountIdOrFail());
    }

    /**
     * Retrieves draft-account summaries using the current scenario token.
     *
     * @param filters query parameters to apply to the list request.
     */
    public void getDraftAccounts(Map<String, String> filters) {
        executeDraftAccountsGet(filters);
    }

    /**
     * Replaces the single draft account created by the current scenario.
     *
     * @param data replacement values to send to the draft-account API.
     * @param businessUnitIdMode numeric mode to use when serialising `business_unit_id`.
     * @throws JSONException if the JSON request body cannot be assembled.
     * @throws IOException if a supporting draft-account fixture cannot be loaded.
     */
    public void replaceCreatedDraftAccount(
        Map<String, String> data,
        DraftAccountRequestFactory.BusinessUnitIdMode businessUnitIdMode
    ) throws JSONException, IOException {
        replaceDraftAccount(onlyCreatedDraftAccountIdOrFail(), data, businessUnitIdMode);
    }

    /**
     * Replaces the specified draft account.
     *
     * @param draftAccountId draft-account identifier to replace.
     * @param data replacement values to send to the draft-account API.
     * @param businessUnitIdMode numeric mode to use when serialising `business_unit_id`.
     * @throws JSONException if the JSON request body cannot be assembled.
     * @throws IOException if a supporting draft-account fixture cannot be loaded.
     */
    public void replaceDraftAccount(
        String draftAccountId,
        Map<String, String> data,
        DraftAccountRequestFactory.BusinessUnitIdMode businessUnitIdMode
    ) throws JSONException, IOException {
        JSONObject postBody = requestFactory.buildReplaceRequestBody(data, businessUnitIdMode);
        RequestSpecification request = authorisedJsonRequest().body(postBody.toString());

        if (data.containsKey("If-Match") && data.get("If-Match") != null && !data.get("If-Match").isBlank()) {
            request = request.header(createQuotedLongHeader("If-Match", data));
        }

        request.when().put(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
    }

    /**
     * Patches the single draft account created by the current scenario.
     *
     * @param data patch values to send to the draft-account API.
     * @return API response returned by the patch request.
     * @throws JSONException if the JSON request body cannot be assembled.
     */
    public Response patchCreatedDraftAccount(Map<String, String> data) throws JSONException {
        return patchDraftAccount(onlyCreatedDraftAccountIdOrFail(), data);
    }

    /**
     * Patches the specified draft account.
     *
     * @param draftAccountId draft-account identifier to patch.
     * @param data patch values to send to the draft-account API.
     * @return API response returned by the patch request.
     * @throws JSONException if the JSON request body cannot be assembled.
     */
    public Response patchDraftAccount(String draftAccountId, Map<String, String> data) throws JSONException {
        JSONObject patchBody = buildPatchRequestBody(data);
        RequestSpecification request = authorisedJsonRequest().body(patchBody.toString());

        String ifMatch = resolveIfMatch(data.get("If-Match"));
        if (ifMatch != null && !ifMatch.isBlank()) {
            request = request.header("If-Match", ifMatch);
        }

        return request.when().patch(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
    }

    /**
     * Executes the draft-account list request, applying any queued custom header overrides from
     * the typed scenario context before clearing them.
     *
     * @param filters query parameters to apply to the list request.
     */
    private void executeDraftAccountsGet(Map<String, String> filters) {
        RequestSpecification given = authorisedJsonRequest();

        scenarioContext().consumeQueuedHeaders().forEach(given::header);

        filters.forEach(given::queryParam);

        given.when().get(getTestUrl() + DRAFT_ACCOUNTS_URI);
    }

    /**
     * Resolves the value to send in the `If-Match` header for patch requests.
     *
     * @param raw raw `If-Match` value supplied by the scenario.
     * @return resolved `If-Match` header value, or `null` when the header should be omitted.
     */
    private String resolveIfMatch(String raw) {
        if (raw == null) {
            return null;
        }

        String candidate = raw.trim();
        if (candidate.isEmpty()) {
            return null;
        }

        if (candidate.startsWith("$etag:")) {
            String key = candidate.substring("$etag:".length()).trim();
            String stored = scenarioContext().getRememberedEtag(key);
            return (stored == null || stored.isBlank()) ? null : stored;
        }

        if (candidate.length() >= 2 && candidate.startsWith("\"") && candidate.endsWith("\"")) {
            return candidate;
        }

        if (candidate.matches("^\\d+$")) {
            return "\"" + candidate + "\"";
        }

        if (candidate.startsWith("W/")) {
            return candidate;
        }

        return "\"" + candidate + "\"";
    }

    /**
     * Builds the JSON body for a draft-account patch request from the supplied scenario data.
     *
     * @param data field values to include in the patch request.
     * @return JSON body for the patch request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    private JSONObject buildPatchRequestBody(Map<String, String> data) throws JSONException {
        JSONObject patch = new JSONObject();

        if (data.containsKey("business_unit_id")) {
            String businessUnitId = data.get("business_unit_id");
            if (businessUnitId != null && !businessUnitId.isBlank()) {
                patch.put("business_unit_id", Integer.parseInt(businessUnitId));
            }
        }

        if (data.containsKey("account_status")) {
            String accountStatus = data.get("account_status");
            patch.put("account_status", (accountStatus == null || accountStatus.isBlank()) ? JSONObject.NULL
                : accountStatus);
        }
        if (data.containsKey("validated_by")) {
            String validatedBy = data.get("validated_by");
            patch.put("validated_by", (validatedBy == null || validatedBy.isBlank()) ? JSONObject.NULL
                : validatedBy);
        }

        JSONObject timelineEntry = new JSONObject();
        String validatedBy = data.get("validated_by");
        String accountStatus = data.get("account_status");
        timelineEntry.put("username", (validatedBy == null || validatedBy.isBlank()) ? JSONObject.NULL : validatedBy);
        timelineEntry.put("status", (accountStatus == null || accountStatus.isBlank()) ? JSONObject.NULL
            : accountStatus);
        timelineEntry.put(
            "status_date",
            java.time.ZonedDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        );

        if (data.containsKey("reason_text")) {
            String reasonText = data.get("reason_text");
            timelineEntry.put("reason_text", (reasonText == null || reasonText.isBlank()) ? JSONObject.NULL
                : reasonText);
        }

        JSONArray timelineDataArray = new JSONArray();
        timelineDataArray.put(timelineEntry);
        patch.put("timeline_data", timelineDataArray);

        return patch;
    }
}
