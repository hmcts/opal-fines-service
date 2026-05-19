package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.When;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Defines feature-toggle request steps for the fines-service release-1b gated endpoints.
 */
public class Release1bFeatureToggleStepDef extends BaseStepDef {

    private static final String DEFENDANT_ACCOUNTS_URI = "/defendant-accounts";
    private static final String MINOR_CREDITOR_ACCOUNTS_URI = "/minor-creditor-accounts";
    private static final String NOTES_URI = "/notes";
    private static final String RESULTS_URI = "/results";
    private static final String PLACEHOLDER_DEFENDANT_ACCOUNT_ID = "999999";
    private static final String PLACEHOLDER_DEFENDANT_ACCOUNT_PARTY_ID = "999999";
    private static final String PLACEHOLDER_MINOR_CREDITOR_ACCOUNT_ID = "999999";
    private static final String PLACEHOLDER_RESULT_ID = "FCOMP";
    private static final String DEFAULT_BUSINESS_UNIT_ID = "77";
    private static final String DEFAULT_IF_MATCH = "\"0\"";
    private static final String DEFAULT_BUSINESS_UNIT_USER_ID = "FEATURE_TOGGLE_TEST";

    /**
     * Calls the named release-1b gated endpoint using a valid representative request for that
     * route so the feature-toggle response can be asserted independently of resource existence or
     * downstream business rules.
     *
     * @param endpointName user-facing name of the endpoint to call.
     */
    @When("I call the release 1b gated endpoint {string}")
    public void callRelease1bGatedEndpoint(String endpointName) {
        switch (endpointName) {
            case "Search Defendant Accounts" ->
                callDefendantAccountSearch(buildDefendantAccountSearchRequest());
            case "Search Minor Creditor Accounts" ->
                callMinorCreditorSearch(buildMinorCreditorSearchRequest());
            case "Get Defendant Account Header Summary" ->
                callGet(DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID + "/header-summary");
            case "Get Defendant Account At A Glance" ->
                callGet(DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID + "/at-a-glance");
            case "Update Defendant Account" ->
                callPatch(
                    DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID,
                    defaultBusinessUnitHeaders(),
                    buildUpdateDefendantAccountRequest()
                );
            case "Add Note" ->
                callPost(
                    NOTES_URI + "/add",
                    Map.of("If-Match", DEFAULT_IF_MATCH),
                    buildAddNoteRequest()
                );
            case "Get Defendant Account Party" ->
                callGet(
                    DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID
                        + "/defendant-account-parties/" + PLACEHOLDER_DEFENDANT_ACCOUNT_PARTY_ID
                );
            case "Replace Defendant Account Party" ->
                callReplaceDefendantAccountParty(
                    defaultBusinessUnitHeaders(),
                    buildReplaceDefendantAccountPartyRequest()
                );
            case "Add Defendant Account Party" ->
                callPost(
                    DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID
                        + "/defendant-account-parties",
                    defaultBusinessUnitHeaders(),
                    buildAddDefendantAccountPartyRequest()
                );
            case "Remove Defendant Account Party" ->
                callRemoveDefendantAccountParty(
                    defaultBusinessUnitHeaders(),
                    buildRemoveDefendantAccountPartyRequest()
                );
            case "Get Defendant Account Enforcement Status" ->
                callGet(DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID + "/enforcement-status");
            case "Get Defendant Account Impositions" ->
                callGet(DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID + "/impositions");
            case "Add Defendant Account Enforcement" ->
                callPost(
                    DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID + "/enforcements",
                    defaultBusinessUnitHeaders(),
                    buildAddDefendantAccountEnforcementRequest()
                );
            case "Remove Defendant Account Enforcement Hold" ->
                callPatch(
                    DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID + "/remove-enf-hold",
                    defaultBusinessUnitHeaders(),
                    buildRemoveDefendantAccountEnforcementHoldRequest()
                );
            case "Get Defendant Account Payment Terms" ->
                callGet(DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID + "/payment-terms/latest");
            case "Get Result" ->
                callGet(RESULTS_URI + "/" + PLACEHOLDER_RESULT_ID);
            case "Add Defendant Account Payment Terms" ->
                callPost(
                    DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID + "/payment-terms",
                    defaultBusinessUnitHeaders(),
                    buildAddDefendantAccountPaymentTermsRequest()
                );
            case "Add Defendant Account Payment Card Request" ->
                callPost(
                    DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID + "/payment-card-request",
                    paymentCardRequestHeaders(),
                    jsonObject("{}")
                );
            case "Get Defendant Account Fixed Penalty" ->
                callGet(DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID + "/fixed-penalty");
            case "Get Defendant Account Impositions" ->
                callGet(DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID + "/impositions");
            case "Get Minor Creditor Account Header Summary" ->
                callGet(MINOR_CREDITOR_ACCOUNTS_URI + "/" + PLACEHOLDER_MINOR_CREDITOR_ACCOUNT_ID + "/header-summary");
            case "Get Minor Creditor Account At A Glance" ->
                callGet(MINOR_CREDITOR_ACCOUNTS_URI + "/" + PLACEHOLDER_MINOR_CREDITOR_ACCOUNT_ID + "/at-a-glance");
            case "Get Minor Creditor Account" ->
                callGet(MINOR_CREDITOR_ACCOUNTS_URI + "/" + PLACEHOLDER_MINOR_CREDITOR_ACCOUNT_ID);
            default -> throw new IllegalArgumentException("Unknown release-1b gated endpoint: " + endpointName);
        }
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
     * Executes the minor-creditor search request used by the release-1b gate scenarios.
     *
     * @param requestBody JSON request body to submit.
     */
    private void callMinorCreditorSearch(JSONObject requestBody) {
        callPost(MINOR_CREDITOR_ACCOUNTS_URI + "/search", Map.of(), requestBody);
    }

    /**
     * Executes the defendant-account search request used by the release-1b gate scenarios.
     *
     * @param requestBody JSON request body to submit.
     */
    private void callDefendantAccountSearch(JSONObject requestBody) {
        callPost(DEFENDANT_ACCOUNTS_URI + "/search", Map.of(), requestBody);
    }

    /**
     * Executes an authorised POST request with additional headers and a JSON request body.
     *
     * @param path path to request from the fines service.
     * @param headers additional headers to include on the request.
     * @param requestBody JSON request body to submit.
     */
    private void callPost(String path, Map<String, String> headers, JSONObject requestBody) {
        applyHeaders(authorisedJsonRequest(), headers)
            .body(requestBody.toString())
            .when()
            .post(getTestUrl() + path);
    }

    /**
     * Executes the replace-defendant-account-party request used by the release-1b gate scenarios.
     *
     * @param headers additional headers to include on the request.
     * @param requestBody JSON request body to submit.
     */
    private void callReplaceDefendantAccountParty(Map<String, String> headers, JSONObject requestBody) {
        applyHeaders(authorisedJsonRequest(), headers)
            .body(requestBody.toString())
            .when()
            .put(getTestUrl() + DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID
                     + "/defendant-account-parties/" + PLACEHOLDER_DEFENDANT_ACCOUNT_PARTY_ID);
    }

    /**
     * Executes an authorised PATCH request with additional headers and a JSON request body.
     *
     * @param path path to request from the fines service.
     * @param headers additional headers to include on the request.
     * @param requestBody JSON request body to submit.
     */
    private void callPatch(String path, Map<String, String> headers, JSONObject requestBody) {
        applyHeaders(authorisedJsonRequest(), headers)
            .body(requestBody.toString())
            .when()
            .patch(getTestUrl() + path);
    }

    /**
     * Executes the remove-defendant-account-party request used by the release-1b gate scenarios.
     *
     * @param headers additional headers to include on the request.
     * @param requestBody JSON request body to submit.
     */
    private void callRemoveDefendantAccountParty(Map<String, String> headers, JSONObject requestBody) {
        applyHeaders(authorisedJsonRequest(), headers)
            .body(requestBody.toString())
            .when()
            .delete(getTestUrl() + DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID
                        + "/defendant-account-parties/" + PLACEHOLDER_DEFENDANT_ACCOUNT_PARTY_ID);
    }

    /**
     * Applies the supplied headers to the shared request specification.
     *
     * @param request request specification to enrich.
     * @param headers headers to add to the request.
     * @return the same request specification after the headers have been applied.
     */
    private RequestSpecification applyHeaders(RequestSpecification request, Map<String, String> headers) {
        RequestSpecification updatedRequest = request;
        for (Map.Entry<String, String> header : headers.entrySet()) {
            updatedRequest = updatedRequest.header(header.getKey(), header.getValue());
        }
        return updatedRequest;
    }

    /**
     * Returns the standard business-unit headers used by the release-1b write endpoints.
     *
     * @return map containing the Business-Unit-Id and If-Match headers.
     */
    private Map<String, String> defaultBusinessUnitHeaders() {
        return Map.of(
            "Business-Unit-Id", DEFAULT_BUSINESS_UNIT_ID,
            "If-Match", DEFAULT_IF_MATCH
        );
    }

    /**
     * Returns the headers required for payment-card-request operations.
     *
     * @return map containing the standard business-unit headers plus Business-Unit-User-Id.
     */
    private Map<String, String> paymentCardRequestHeaders() {
        return Map.of(
            "Business-Unit-Id", DEFAULT_BUSINESS_UNIT_ID,
            "Business-Unit-User-Id", DEFAULT_BUSINESS_UNIT_USER_ID,
            "If-Match", DEFAULT_IF_MATCH
        );
    }

    /**
     * Builds a representative minor-creditor search request body.
     *
     * @return request body for POST /minor-creditor-accounts/search.
     */
    private JSONObject buildMinorCreditorSearchRequest() {
        return jsonObject("""
            {
              "business_unit_ids": [77],
              "active_accounts_only": false,
              "account_number": "12345678"
            }
            """);
    }

    /**
     * Builds a representative defendant-account search request body.
     *
     * @return request body for POST /defendant-accounts/search.
     */
    private JSONObject buildDefendantAccountSearchRequest() {
        return jsonObject("""
            {
              "active_accounts_only": false,
              "business_unit_ids": [77],
              "reference_number": {
                "organisation": false,
                "account_number": "12345678",
                "prosecutor_case_reference": null
              },
              "defendant": null
            }
            """);
    }

    /**
     * Builds a representative defendant-account update request body.
     *
     * @return request body for PATCH /defendant-accounts/{id}.
     */
    private JSONObject buildUpdateDefendantAccountRequest() {
        return jsonObject("""
            {
              "comment_and_notes": {
                "account_comment": "release-1b off-path check"
              }
            }
            """);
    }

    /**
     * Builds a representative add-note request body.
     *
     * @return request body for POST /notes/add.
     */
    private JSONObject buildAddNoteRequest() {
        return jsonObject("""
            {
              "activity_note": {
                "record_type": "defendant_accounts",
                "record_id": "77",
                "note_text": "release-1b off-path check",
                "note_type": "AA"
              }
            }
            """);
    }

    /**
     * Builds a representative replace-party request body.
     *
     * @return request body for PUT /defendant-accounts/{id}/defendant-account-parties/{partyId}.
     */
    private JSONObject buildReplaceDefendantAccountPartyRequest() {
        return jsonObject("""
            {
              "defendant_account_party_type": "Defendant",
              "is_debtor": true,
              "party_details": {
                "party_id": "999999",
                "organisation_flag": true,
                "organisation_details": {
                  "organisation_name": "Feature Toggle Test Ltd"
                }
              }
            }
            """);
    }

    /**
     * Builds a representative add-party request body that satisfies the endpoint's JSON schema.
     *
     * @return request body for POST /defendant-accounts/{id}/defendant-account-parties.
     */
    private JSONObject buildAddDefendantAccountPartyRequest() {
        return jsonObject("""
            {
              "defendant_account_party": {
                "defendant_account_party_type": "Defendant",
                "is_debtor": true,
                "party_details": {
                  "party_id": "999999",
                  "organisation_flag": true,
                  "organisation_details": {
                    "organisation_name": "Feature Toggle Test Ltd"
                  }
                },
                "address": {
                  "address_line_1": "1 Test Street",
                  "address_line_2": null,
                  "address_line_3": null,
                  "address_line_4": null,
                  "address_line_5": null,
                  "postcode": "TE1 1ST"
                },
                "contact_details": null,
                "vehicle_details": null,
                "employer_details": null,
                "language_preferences": null
              }
            }
            """);
    }

    /**
     * Builds a representative remove-party request body.
     *
     * @return request body for DELETE /defendant-accounts/{id}/defendant-account-parties/{partyId}.
     */
    private JSONObject buildRemoveDefendantAccountPartyRequest() {
        return jsonObject("""
            {
              "party_details": {
                "party_id": "206"
              }
            }
            """);
    }

    /**
     * Builds a representative add-enforcement request body.
     *
     * @return request body for POST /defendant-accounts/{id}/enforcements.
     */
    private JSONObject buildAddDefendantAccountEnforcementRequest() {
        return jsonObject("""
            {
              "result_id": "CONF",
              "enforcement_result_responses": [
                {
                  "parameter_name": "amount_due",
                  "response": "100.00"
                },
                {
                  "parameter_name": "next_payment_date",
                  "response": "2026-01-15"
                }
              ],
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-01",
                "extension": true,
                "reason_for_extension": "Financial hardship",
                "payment_terms_type": {
                  "payment_terms_type_code": "B"
                },
                "effective_date": "2025-11-15",
                "instalment_period": {
                  "instalment_period_code": "M"
                },
                "lump_sum_amount": 0.00,
                "instalment_amount": 150.00,
                "posted_details": {
                  "posted_date": "2025-11-02T10:30:00",
                  "posted_by": "System",
                  "posted_by_name": "System User"
                }
              }
            }
            """);
    }

    /**
     * Builds a representative remove-enforcement-hold request body.
     *
     * @return request body for PATCH /defendant-accounts/{id}/remove-enf-hold.
     */
    private JSONObject buildRemoveDefendantAccountEnforcementHoldRequest() {
        return jsonObject("""
            {
              "reason": "remove hold reason"
            }
            """);
    }

    /**
     * Builds a representative add-payment-terms request body.
     *
     * @return request body for POST /defendant-accounts/{id}/payment-terms.
     */
    private JSONObject buildAddDefendantAccountPaymentTermsRequest() {
        return jsonObject("""
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": {
                  "payment_terms_type_code": "B",
                  "payment_terms_type_display_name": "By date"
                },
                "instalment_period": {
                  "instalment_period_code": "W",
                  "instalment_period_display_name": "Weekly"
                },
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": true,
              "generate_payment_terms_change_letter": true
            }
            """);
    }

    /**
     * Parses a JSON string into a request body object.
     *
     * @param body JSON string to parse.
     * @return parsed JSON object.
     */
    private JSONObject jsonObject(String body) {
        try {
            return new JSONObject(body);
        } catch (JSONException exception) {
            throw new IllegalArgumentException("Invalid JSON request body", exception);
        }
    }
}
