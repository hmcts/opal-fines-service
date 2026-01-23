package uk.gov.hmcts.opal.steps.defendantaccount;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.hamcrest.Matchers;
import uk.gov.hmcts.opal.steps.BaseStepDef;


@Slf4j
public class DefendantAccountDetailsStefDef extends BaseStepDef {

    @When("I make a request to the defendant account details api with")
    public void getDefendantAccountDetailsByID(DataTable id) {
        Map<String, String> idToSend = id.asMap(String.class, String.class);
        SerenityRest.given()
            .accept("*/*")
            .header("Authorization", "Bearer " + getToken())
            .contentType("application/json")
            .when()
            .get(getTestUrl() + "/defendant-accounts/" + idToSend.get("defendantID") + "/header-summary");
    }

    @Then("the response from the defendant account details api is")
    public void assertDefendantAccountDetailsResponseMatches(DataTable fields) {
        Map<String, String> response = fields.asMap(String.class, String.class);
        int rows = response.size();

        log.info("Rows: {}", rows);

        then().assertThat()
            .statusCode(200);

        for (String key : response.keySet()) {
            String apiResponseValue = then().extract().body().jsonPath().getString(key);
            assertEquals("Values are not equal : ", apiResponseValue, response.get(key));
        }

    }

    @Then("the response from the defendant account details api is unauthorised")
    public void assertDefendantAccountDetailsResponseUnauthorised() {
        then().assertThat()
            .statusCode(401);
    }

    @Then("the response from the defendant account search api is unauthorised")
    public void assertDefendantAccountSearchResponseUnauthorised() {
        then().assertThat()
            .statusCode(401);
    }

    @Then("the response from the defendant account details api is forbidden")
    public void assertDefendantAccountDetailsResponseForbidden() {
        then().assertThat()
            .statusCode(403);
    }

    @Then("the response from the defendant account search api is forbidden")
    public void assertDefendantAccountSearchResponseForbidden() {
        then().assertThat()
            .statusCode(403);
    }

    @Then("the response from the defendant account details api is empty")
    public void responseFromTheDefendantAccountDetailsApiIsInvalid() {
        then().assertThat()
            .statusCode(Matchers.not(200));
    }
}
