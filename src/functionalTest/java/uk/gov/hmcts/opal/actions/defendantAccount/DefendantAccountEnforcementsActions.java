package uk.gov.hmcts.opal.actions.defendantAccount;

import net.serenitybdd.core.Serenity;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;
import static uk.gov.hmcts.opal.utils.JsonObjectUtils.addIntObjectIfPresent;
import static uk.gov.hmcts.opal.utils.JsonObjectUtils.addLongObjectIfPresent;

public class DefendantAccountEnforcementsActions extends BaseStepDef {

    private static final String CREATED_DEFENDANT_ACCOUNT_ID = "CREATED_DEFENDANT_ACCOUNT_ID";
    private static final String DEFENDANT_ACCOUNT_ETAG = "DEFENDANT_ACCOUNT_ETAG";

    public void storeCreatedDefendantAccountIdFromLastResponse() {
        Object accountId = SerenityRest.lastResponse().jsonPath().get("account_id");
        assertNotNull(accountId, "Expected published draft account response to contain account_id");
        Serenity.setSessionVariable(CREATED_DEFENDANT_ACCOUNT_ID).to(String.valueOf(accountId));
    }

    public void getCreatedDefendantAccountEnforcementStatus() {
        var response = SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + "/defendant-accounts/" + createdDefendantAccountIdOrFail() + "/enforcement-status");

        Serenity.setSessionVariable(DEFENDANT_ACCOUNT_ETAG).to(response.getHeader("ETag"));
    }

    public void patchCreatedDefendantAccountEnforcementOverride(Map<String, String> data) throws JSONException {
        JSONObject enforcementOverride = new JSONObject()
            .put("enforcement_override_result", new JSONObject()
                .put("enforcement_override_result_id", data.get("enforcement_override_result_id")));

        addLongObjectIfPresent(enforcementOverride, data, "enforcer_id", "enforcer");
        addIntObjectIfPresent(enforcementOverride, data, "lja_id", "lja");

        JSONObject requestBody = new JSONObject().put("enforcement_override", enforcementOverride);

        String ifMatch = data.get("If-Match");
        if (ifMatch == null || ifMatch.isBlank()) {
            ifMatch = Serenity.sessionVariableCalled(DEFENDANT_ACCOUNT_ETAG);
        }

        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .header("Business-Unit-Id", data.get("business_unit_id"))
            .header("If-Match", ifMatch)
            .accept("*/*")
            .contentType("application/json")
            .body(requestBody.toString())
            .when()
            .patch(getTestUrl() + "/defendant-accounts/" + createdDefendantAccountIdOrFail());
    }

    private String createdDefendantAccountIdOrFail() {
        String defendantAccountId = Serenity.sessionVariableCalled(CREATED_DEFENDANT_ACCOUNT_ID);
        assertNotNull(defendantAccountId, "No created defendant account ID found in session");
        assertFalse(defendantAccountId.isBlank(), "Created defendant account ID is blank");
        return defendantAccountId;
    }
}
