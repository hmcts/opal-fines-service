package uk.gov.hmcts.opal.steps;

import static net.serenitybdd.rest.SerenityRest.then;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import uk.gov.hmcts.opal.utils.RequestSupport;

public class CourtSearchStepDef extends BaseStepDef {

    @When("I make a request to get the courts")
    public void postCourtSearch() throws JSONException {
        RequestSupport.responseProcessor(
            SerenityRest
                .given()
                .spec(RequestSupport.postRequestSpec("/courts/search", "{}").build())
                .when()
                .post()
                .then()
        );
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
