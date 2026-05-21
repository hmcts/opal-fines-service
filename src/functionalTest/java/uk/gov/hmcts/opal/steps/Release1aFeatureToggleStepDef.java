package uk.gov.hmcts.opal.steps;

import static net.serenitybdd.rest.SerenityRest.lastResponse;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountRequestFactory;

/**
 * Defines feature-toggle request steps for the fines-service release-1a gated endpoints.
 */
public class Release1aFeatureToggleStepDef extends BaseStepDef {

    private static final String COURTS_URI = "/courts";
    private static final String COURTS_SEARCH_URI = "/courts/search";
    private static final String BUSINESS_UNITS_URI = "/business-units";
    private static final String LJA_URI = "/local-justice-areas";
    private static final String OFFENCES_URI = "/offences";
    private static final String OFFENCES_SEARCH_URI = "/offences/search";
    private static final String MAJOR_CREDITORS_URI = "/major-creditors";
    private static final String PROSECUTORS_URI = "/prosecutors";
    private static final String PLACEHOLDER_DRAFT_ACCOUNT_ID = "999999";
    private static final String PLACEHOLDER_ENTITY_ID = "1";
    private static final String DEFAULT_BUSINESS_UNIT_ID = "77";
    private static final String DEFAULT_SUBMITTED_BY = "BUUID";
    private static final String DEFAULT_SUBMITTED_BY_NAME = "Laura Clerk";
    private static final String DEFAULT_ACCOUNT_TYPE = "Fine";
    private static final String DEFAULT_IF_MATCH = "\"0\"";

    private final DraftAccountRequestFactory requestFactory = new DraftAccountRequestFactory();

    /**
     * Calls the named release-1a gated endpoint using a valid representative request for that
     * route so the feature-toggle response can be asserted independently of business validation.
     *
     * @param endpointName user-facing name of the endpoint to call.
     * @throws IOException if a draft-account fixture cannot be loaded for the request body.
     * @throws JSONException if a JSON request body cannot be created.
     */
    @When("I call the release 1a gated endpoint {string}")
    public void callRelease1aGatedEndpoint(String endpointName) throws IOException, JSONException {
        switch (endpointName) {
            case "Get Courts" -> callGet(COURTS_URI + "?business_unit=77");
            case "Search Courts" -> callPost(COURTS_SEARCH_URI, new JSONObject().put("businessUnitId", "99"));
            case "Get Business Units" -> callGet(BUSINESS_UNITS_URI + "?q=Area");
            case "Get Business Unit" -> callGet(BUSINESS_UNITS_URI + "/" + PLACEHOLDER_ENTITY_ID);
            case "Get Local Justice Areas" -> callGet(LJA_URI + "?lja_type=LJA");
            case "Get Offences" -> callGet(OFFENCES_URI + "?cjs_code=CW96023");
            case "Get Offence" -> callGet(OFFENCES_URI + "/30000");
            case "Search Offences" -> callPost(
                OFFENCES_SEARCH_URI,
                new JSONObject()
                    .put("cjs_code", "IC01001")
                    .put("max_results", 10)
            );
            case "Get Major Creditors" -> callGet(MAJOR_CREDITORS_URI + "?q=AAAA");
            case "Get Prosecutors" -> callGet(PROSECUTORS_URI + "?q=AA");
            case "Add Draft Account" -> callPost(DRAFT_ACCOUNTS_URI, buildDraftAccountCreateRequest());
            case "Get Draft Accounts" -> callGet(DRAFT_ACCOUNTS_URI);
            case "Get Draft Account" -> callGet(DRAFT_ACCOUNTS_URI + "/" + PLACEHOLDER_DRAFT_ACCOUNT_ID);
            case "Replace Draft Account" -> callDraftAccountReplace(buildDraftAccountReplaceRequest());
            case "Update Draft Account" -> callDraftAccountPatch(buildDraftAccountUpdateRequest());
            default -> throw new IllegalArgumentException("Unknown release-1a gated endpoint: " + endpointName);
        }
    }

    @Then("the response reports that the feature is disabled")
    public void theResponseReportsThatTheFeatureIsDisabled() {
        lastResponse().then()
            .statusCode(405)
            .body("title", equalTo("Feature Disabled"))
            .body("detail", equalTo("The requested feature is not currently available"))
            .body("type", equalTo("https://hmcts.gov.uk/problems/feature-disabled"))
            .body("retriable", equalTo(false))
            .body("instance", notNullValue())
            .body("operation_id", notNullValue());
    }

    /**
     * Executes an authorised GET request for the supplied path.
     *
     * @param path path and query string to request from the fines service.
     */
    private void callGet(String path) {
        authorisedJsonRequest()
            .when()
            .get(getTestUrl() + path);
    }

    /**
     * Executes an authorised POST request with a JSON request body.
     *
     * @param path path to request from the fines service.
     * @param requestBody JSON request body to submit.
     */
    private void callPost(String path, JSONObject requestBody) {
        authorisedJsonRequest()
            .body(requestBody.toString())
            .when()
            .post(getTestUrl() + path);
    }

    /**
     * Executes an authorised PUT request against the placeholder draft-account identifier used by
     * the release-1a gate scenarios.
     *
     * @param requestBody JSON request body to submit.
     */
    private void callDraftAccountReplace(JSONObject requestBody) {
        authorisedJsonRequest()
            .header("If-Match", DEFAULT_IF_MATCH)
            .body(requestBody.toString())
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + PLACEHOLDER_DRAFT_ACCOUNT_ID);
    }

    /**
     * Executes an authorised PATCH request against the placeholder draft-account identifier used by
     * the release-1a gate scenarios.
     *
     * @param requestBody JSON request body to submit.
     */
    private void callDraftAccountPatch(JSONObject requestBody) {
        authorisedJsonRequest()
            .header("If-Match", DEFAULT_IF_MATCH)
            .body(requestBody.toString())
            .when()
            .patch(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + PLACEHOLDER_DRAFT_ACCOUNT_ID);
    }

    /**
     * Builds a valid draft-account create request body for the release-1a gate scenarios.
     *
     * @return create request body for the draft-account API.
     * @throws IOException if the shared account fixture cannot be loaded.
     * @throws JSONException if the JSON payload cannot be assembled.
     */
    private JSONObject buildDraftAccountCreateRequest() throws IOException, JSONException {
        return requestFactory.buildDefaultCreateRequestBody(DEFAULT_BUSINESS_UNIT_ID, DEFAULT_SUBMITTED_BY);
    }

    /**
     * Builds a valid draft-account replace request body for the release-1a gate scenarios.
     *
     * @return replace request body for the draft-account API.
     * @throws IOException if the shared account fixture cannot be loaded.
     * @throws JSONException if the JSON payload cannot be assembled.
     */
    private JSONObject buildDraftAccountReplaceRequest() throws IOException, JSONException {
        Map<String, String> replacementData = new LinkedHashMap<>();
        replacementData.put("business_unit_id", DEFAULT_BUSINESS_UNIT_ID);
        replacementData.put("submitted_by", DEFAULT_SUBMITTED_BY);
        replacementData.put("submitted_by_name", DEFAULT_SUBMITTED_BY_NAME);
        replacementData.put("account_type", DEFAULT_ACCOUNT_TYPE);
        replacementData.put("account_status", "");
        replacementData.put("account", DraftAccountRequestFactory.DEFAULT_ACCOUNT_PATH);

        return requestFactory.buildReplaceRequestBody(
            replacementData,
            DraftAccountRequestFactory.BusinessUnitIdMode.LONG
        );
    }

    /**
     * Builds a valid draft-account patch request body for the release-1a gate scenarios.
     *
     * @return patch request body for the draft-account API.
     */
    private JSONObject buildDraftAccountUpdateRequest() throws JSONException {
        return new JSONObject()
            .put("account_status", "Publishing Pending")
            .put("business_unit_id", 77)
            .put("validated_by", DEFAULT_SUBMITTED_BY);
    }


}
