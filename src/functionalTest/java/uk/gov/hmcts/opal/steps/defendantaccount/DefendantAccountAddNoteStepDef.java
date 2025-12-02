package uk.gov.hmcts.opal.steps.defendantaccount;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.Assert.assertEquals;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.utils.RequestSupport;

public class DefendantAccountAddNoteStepDef extends BaseStepDef {

    @When("I make a request to the defendant account add notes api with")
    public void postToDefAccountAddNotesApi(DataTable data) throws JSONException {
        Map<String, String> dataToPost = data.asMap(String.class, String.class);

        JSONObject postBody = new JSONObject();
        for (String key : dataToPost.keySet()) {
            postBody.put(key, dataToPost.get(key));
        }
        RequestSupport.responseProcessor(
            SerenityRest
                .given()
                .spec(RequestSupport.postRequestSpec("/defendant-accounts/addNote", postBody.toString()).build())
                .when()
                .post()
                .then()
        );
    }

    @Then("the add notes response contains")
    public void assertAddNotesResponse(DataTable data) {
        Map<String, String> response = data.asMap(String.class, String.class);
        then().assertThat()
            .statusCode(201);

        for (String key : response.keySet()) {
            String apiResponseValue = then().extract().body().jsonPath().getString(key);
            assertEquals("Values are not equal : ", response.get(key), apiResponseValue);
        }
    }

    @Then("the add notes request is forbidden")
    public void assertAddNotesForbidden() {
        then().assertThat()
            .statusCode(403);
    }

    @Then("the add notes request returns an Internal Server Error")
    public void assertAddNotesInternalServerError() {
        then().assertThat()
            .statusCode(500);
    }

    @Then("the following account note is returned in the ac details request")
    public void theFollowingAccountNoteIsReturnedInTheAcDetailsRequest(DataTable data) {
        Map<String, String> response = data.asMap(String.class, String.class);
        for (String key : response.keySet()) {
            String apiResponseValue = then().extract().body().jsonPath().getString(key);
            assertEquals("Values are not equal : ", response.get(key), apiResponseValue);
        }
    }
}
