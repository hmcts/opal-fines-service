package uk.gov.hmcts.opal.steps.offences;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import java.util.Map;

import static uk.gov.hmcts.opal.config.Constants.OFFENCES_SEARCH_URI;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class SearchOffencesRequestStepDef extends BaseStepDef {
    @When("I make a request to the offence search api filtering by")
    public void postOffencesSearchRequest(DataTable filters) throws JSONException {
        Map<String, String> dataToPost = filters.asMap(String.class, String.class);
        JSONObject requestBody = new JSONObject();
        requestBody.put("cjs_code", dataToPost.get("cjs_code") != null ? dataToPost.get("cjs_code") : "");
        requestBody.put("title", dataToPost.get("title") != null ? dataToPost.get("title") : "");
        requestBody.put("act_and_section", dataToPost.get("act_and_section") != null
            ? dataToPost.get("act_and_section") : "");
        requestBody.put("active_date", dataToPost.get("active_date") != null ? dataToPost.get("active_date") : "");
        requestBody.put("max_results", dataToPost.get("max_results") != null ? dataToPost.get("max_results") : "100");

        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .body(requestBody.toString())
            .when()
            .post(getTestUrl() + OFFENCES_SEARCH_URI);
    }
}
