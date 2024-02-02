package uk.gov.hmcts.opal.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.opal.steps.BearerTokenStefDef.getToken;

public class DefendantAccountAddNoteStepDef extends BaseStepDef {
    @When("I make a request to the defendant account add notes api with")
    public void postToDefAccountAddNotesApi(DataTable data) throws JSONException {
        Map<String, String> dataToPost = data.asMap(String.class, String.class);

        JSONObject postBody = new JSONObject();
        for (String key : dataToPost.keySet()) {
            postBody.put(key, dataToPost.get(key));
        }
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .body(postBody.toString())
            .when()
            .post(getTestUrl() + "/api/defendant-account/addNote");
    }

    @Then("the add notes response contains")
    public void assertAddNotesResponse(DataTable data) {
        Map<String, String> response = data.asMap(String.class, String.class);
        then().assertThat()
            .statusCode(201);

        for (String key : response.keySet()) {
            String apiResponseValue = then().extract().body().jsonPath().getString(key);
            assertEquals("Values are not equal : ", apiResponseValue, response.get(key));
        }
    }
}
