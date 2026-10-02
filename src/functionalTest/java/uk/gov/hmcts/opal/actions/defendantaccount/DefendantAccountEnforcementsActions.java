package uk.gov.hmcts.opal.actions.defendantaccount;

import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.assertions.CommonResponseAssertions;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.opal.utils.JsonObjectUtils.addIntObjectIfPresent;
import static uk.gov.hmcts.opal.utils.JsonObjectUtils.addLongObjectIfPresent;

/**
 * Encapsulates reusable calls and session-state handling for defendant-account enforcement
 * scenarios.
 */
public class DefendantAccountEnforcementsActions extends BaseStepDef {
    private final CommonResponseAssertions responseAssertions = new CommonResponseAssertions();

    /**
     * Extracts the created defendant-account ID from the supplied response and stores it in the
     * typed scenario context for later enforcement steps.
     *
     * @param response response expected to contain the published defendant-account identifier.
     */
    public void storeCreatedDefendantAccountId(Response response) {
        Object accountId = publishedAccountIdFrom(response);
        assertNotNull(accountId, "Expected published draft account response to contain account_id");

        String defendantAccountId = String.valueOf(accountId);
        scenarioContext().setCreatedDefendantAccountId(defendantAccountId);
        if (!isLegacyMode()) {
            scenarioContext().addCreatedDefendantAccountId(defendantAccountId);
        }
    }

    /**
     * Retrieves the enforcement status for the defendant account created during the current
     * scenario and remembers the returned ETag for later update requests.
     */
    public Response getCreatedDefendantAccountEnforcementStatus() {
        Response response = authorisedJsonRequest()
            .when()
            .get(getTestUrl() + "/defendant-accounts/" + createdDefendantAccountIdOrFail() + "/enforcement-status");

        scenarioContext().setDefendantAccountEtag(response.getHeader("ETag"));
        return response;
    }

    /**
     * Patches the enforcement override for the defendant account created during the current
     * scenario.
     *
     * @param data table-driven values used to build the override payload and request headers.
     * @throws JSONException if the JSON request body cannot be assembled.
     */
    public Response patchCreatedDefendantAccountEnforcementOverride(Map<String, String> data) throws JSONException {
        JSONObject enforcementOverride = new JSONObject()
            .put("enforcement_override_result", new JSONObject()
                .put("enforcement_override_result_id", data.get("enforcement_override_result_id")));

        addLongObjectIfPresent(enforcementOverride, data, "enforcer_id", "enforcer");
        addIntObjectIfPresent(enforcementOverride, data, "lja_id", "lja");

        JSONObject requestBody = new JSONObject().put("enforcement_override", enforcementOverride);

        String ifMatch = data.get("If-Match");
        if (ifMatch == null || ifMatch.isBlank()) {
            ifMatch = scenarioContext().getDefendantAccountEtag();
        }

        return authorisedJsonRequest()
            .header("Business-Unit-Id", data.get("business_unit_id"))
            .header("If-Match", ifMatch)
            .body(requestBody.toString())
            .when()
            .patch(getTestUrl() + "/defendant-accounts/" + createdDefendantAccountIdOrFail());
    }

    /**
     * Returns the created defendant-account ID stored for the current scenario, failing when it
     * has not yet been captured.
     *
     * @return created defendant-account ID recorded in the typed scenario context.
     */
    private String createdDefendantAccountIdOrFail() {
        return scenarioContext().getCreatedDefendantAccountIdOrFail();
    }

    private Object publishedAccountIdFrom(Response response) {
        Object accountId = response.jsonPath().get("account_id");
        return accountId != null ? accountId : response.jsonPath().get("defendant_account_id");
    }

    private boolean isLegacyMode() {
        Response response = authorisedJsonRequest()
            .when()
            .get(getTestUrl() + "/testing-support/is-legacy-mode");
        responseAssertions.assertStatus(response, 200);
        return Boolean.parseBoolean(response.asString());
    }
}
