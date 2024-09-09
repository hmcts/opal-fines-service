package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;

import static net.serenitybdd.rest.SerenityRest.then;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class CourtSearchStepDef extends BaseStepDef {
    @When("I make a request to get the courts")
    public void postCourtSearch() throws JSONException {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .body("{}")
            .when()
            .post(getTestUrl() + "/courts/search");
    }

    @Then("the court search response returns 200")
    public void courtSearchResponseOk() {
        then().assertThat()
            .statusCode(200);
    }

    @Then("the court search request returns forbidden")
    public void courtSearchForbidden() {
        then().assertThat()
            .statusCode(403);
    }

    @Then("the court search response returns an Internal Server Error")
    public void courtSearchInternalServerError() {
        then().assertThat()
            .statusCode(500);
    }
}
