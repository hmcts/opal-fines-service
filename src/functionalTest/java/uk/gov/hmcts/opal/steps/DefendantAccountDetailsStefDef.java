package uk.gov.hmcts.opal.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.Assert.assertEquals;

public class DefendantAccountDetailsStefDef extends BaseStepDef {

    @When("I make a request to the defendant account details api with")
    public void getDefendantAccountDetailsByID(DataTable id) {
        Map<String, String> idToSend = id.asMap(String.class, String.class);
        SerenityRest.given()
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + "/api/defendant-account/details?defendantAccountId=" + idToSend.get("defendantID"));
    }


    @Then("the response from the defendant account details api is")
    public void assertDefendantAccountDetailsResponseMatches(DataTable fields) {
        Map<String, String> response = fields.asMap(String.class, String.class);
        int rows = response.size();

        System.out.println("Rows: " + rows);

        then().assertThat()
            .statusCode(200);

        for (String key : response.keySet()) {
            String apiResponseValue = then().extract().body().jsonPath().getString(key);
            assertEquals("Values are not equal : ", apiResponseValue, response.get(key));
        }

    }

    @Then("the response from the defendant account details api is empty")
    public void responseFromTheDefendantAccountDetailsApiIsInvalid() {
        then().assertThat()
            .statusCode(403);
    }
}