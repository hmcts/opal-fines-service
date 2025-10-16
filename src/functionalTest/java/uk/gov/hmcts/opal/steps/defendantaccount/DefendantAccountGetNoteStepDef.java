package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class DefendantAccountGetNoteStepDef extends BaseStepDef {
    @When("I make a request to get the defendant account notes for")
    public void getDefendantAccountNotes(DataTable data) {
        Map<String, String> idToSend = data.asMap(String.class, String.class);

        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + "/defendant-accounts/notes/" + idToSend.get("defendantID"));
    }

    @Then("the response contains the following in position {string}")
    public void assertAddNotesResponse(String position, DataTable data) {
        Map<String, String> response = data.asMap(String.class, String.class);
        then().assertThat()
            .statusCode(200);

        for (String key : response.keySet()) {
            String apiResponseValue = then().extract().body().jsonPath().getString(key + "[" + position + "]");
            assertEquals("Values are not equal : ", response.get(key), apiResponseValue);
        }
    }

    @Then("the get notes request is forbidden")
    public void assertAddNotesForbidden() {
        then().assertThat()
            .statusCode(403);
    }

    @Then("the get notes request returns an Internal Server Error")
    public void assertAddNotesInternalServerError() {
        then().assertThat()
            .statusCode(500);
    }
}
