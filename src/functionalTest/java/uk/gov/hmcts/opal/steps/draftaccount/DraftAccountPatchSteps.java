package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
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

        addIntToJsonObject(patchBody, dataToPatch, "business_unit_id");
        addToJsonObjectOrNull(patchBody, dataToPatch, "account_status");
        addToJsonObjectOrNull(patchBody, dataToPatch, "validated_by");

        // Create timeline data array with one entry
        JSONObject timelineEntry = new JSONObject();

        if (dataToPatch.containsKey("validated_by")) {
            timelineEntry.put("username", dataToPatch.get("validated_by"));
        } else {
            timelineEntry.put("username", JSONObject.NULL);
        }

        if (dataToPatch.containsKey("account_status")) {
            timelineEntry.put("status", dataToPatch.get("account_status"));
        } else {
            timelineEntry.put("status", JSONObject.NULL);
        }

        timelineEntry.put("status_date", ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        addToJsonObjectOrNull(timelineEntry, dataToPatch, "reason_text");

        JSONArray timelineDataArray = new JSONArray();
        timelineDataArray.put(timelineEntry);
        patchBody.put("timeline_data", timelineDataArray);

        String draftAccountId = DraftAccountUtils.getAllDraftAccountIds().get(0);
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .body(patchBody.toString())
            .header(createQuotedLongHeader("If-Match", dataToPatch))
            .when()
            .patch(getTestUrl() + "/draft-accounts/" + draftAccountId);
    }

    @When("I patch the {string} draft account with the following details")
    public void patchDraftAccount(String draftAccountId, DataTable data) throws JSONException {
        Map<String, String> dataToPatch = data.asMap(String.class, String.class);
        JSONObject patchBody = new JSONObject();

        addIntToJsonObject(patchBody, dataToPatch, "business_unit_id");
        addToJsonObjectOrNull(patchBody, dataToPatch, "account_status");
        addToJsonObjectOrNull(patchBody, dataToPatch, "validated_by");

        // Create timeline data array with one entry
        JSONObject timelineEntry = new JSONObject();

        if (dataToPatch.containsKey("validated_by")) {
            timelineEntry.put("username", dataToPatch.get("validated_by"));
        } else {
            timelineEntry.put("username", JSONObject.NULL);
        }

        if (dataToPatch.containsKey("account_status")) {
            timelineEntry.put("status", dataToPatch.get("account_status"));
        } else {
            timelineEntry.put("status", JSONObject.NULL);
        }

        timelineEntry.put("status_date", ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        addToJsonObjectOrNull(timelineEntry, dataToPatch, "reason_text");

        JSONArray timelineDataArray = new JSONArray();
        timelineDataArray.put(timelineEntry);
        patchBody.put("timeline_data", timelineDataArray);
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .body(patchBody.toString())
            .header(createQuotedLongHeader("If-Match", dataToPatch))
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
