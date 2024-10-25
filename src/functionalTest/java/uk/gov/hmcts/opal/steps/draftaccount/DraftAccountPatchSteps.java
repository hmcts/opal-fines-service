package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.utils.DraftAccountUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class DraftAccountPatchSteps extends BaseStepDef {
    @When("I patch the draft account with the following details")
    public void patchDraftAccount(DataTable data) throws JSONException {
        Map<String, String> dataToPatch = data.asMap(String.class, String.class);
        JSONObject patchBody = new JSONObject();

        patchBody.put("business_unit_id", dataToPatch.get("business_unit_id"));
        patchBody.put("account_status", dataToPatch.get("account_status"));
        patchBody.put("validated_by", dataToPatch.get("validated_by"));

        JSONObject timelineData = new JSONObject();
        timelineData.put("username", dataToPatch.get("validated_by"));
        timelineData.put("status", dataToPatch.get("account_status"));

        ZonedDateTime currentDateTime = ZonedDateTime.now();
        timelineData.put("status_date", currentDateTime.format(DateTimeFormatter.ISO_INSTANT));

        if (dataToPatch.containsKey("reason_text")) {
            timelineData.put("reason_text", dataToPatch.get("reason_text"));
        }
        patchBody.put("timeline_data", timelineData);

        String draftAccountId = DraftAccountUtils.getAllDraftAccountIds().getFirst();
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .body(patchBody.toString())
            .when()
            .patch(getTestUrl() + "/draft-accounts/" + draftAccountId);
    }

    @When("I patch the {string} draft account with the following details")
    public void patchDraftAccount(String draftAccountId, DataTable data) throws JSONException {
        Map<String, String> dataToPatch = data.asMap(String.class, String.class);
        JSONObject patchBody = new JSONObject();

        patchBody.put("business_unit_id", dataToPatch.get("business_unit_id"));
        patchBody.put("account_status", dataToPatch.get("account_status"));
        patchBody.put("validated_by", dataToPatch.get("validated_by"));

        JSONObject timelineData = new JSONObject();
        timelineData.put("username", dataToPatch.get("validated_by"));
        timelineData.put("status", dataToPatch.get("account_status"));

        ZonedDateTime currentDateTime = ZonedDateTime.now();
        timelineData.put("status_date", currentDateTime.format(DateTimeFormatter.ISO_INSTANT));

        if (dataToPatch.containsKey("reason_text")) {
            timelineData.put("reason_text", dataToPatch.get("reason_text"));
        }
        patchBody.put("timeline_data", timelineData);

        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .body(patchBody.toString())
            .when()
            .patch(getTestUrl() + "/draft-accounts/" + draftAccountId);
    }

    @When("I attempt to patch a draft account with an unsupported content type")
    public void patchDraftAccountWithUnsupportedContentType() {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("text/plain")
            .contentType("application/json")
            .when()
            .patch(getTestUrl() + "/draft-accounts/" + "1");
    }

    @When("I attempt to patch a draft account with an unsupported media type")
    public void patchDraftAccountWithInvalidMediaType() {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("application/json")
            .contentType("application/xml")
            .when()
            .patch(getTestUrl() + "/draft-accounts/" + "1");
    }

    @When("I patch the draft account trying to provoke an internal server error")
    public void patchDraftAccountInternalServerError() {
        SerenityRest
            .given()
            .urlEncodingEnabled(false)
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .patch(getTestUrl() + "/draft-accounts/%20");
    }
}
