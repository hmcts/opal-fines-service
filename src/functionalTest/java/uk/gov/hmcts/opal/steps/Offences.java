package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.serenitybdd.rest.SerenityRest.then;
import static uk.gov.hmcts.opal.config.Constants.OFFENCES_REF_DATA_URI;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;


public class Offences extends BaseStepDef {

    static Logger log = LoggerFactory.getLogger(Offences.class.getName());

    @When("I make a request to the offence ref data api filtering by cjs code {string}")
    public void getRequestToOffencesRefData(String filter) {
        SerenityRest
            .given()
            .accept("*/*")
            .header("Authorization", "Bearer " + getToken())
            .contentType("application/json")
            .when()
            .get(getTestUrl() + OFFENCES_REF_DATA_URI + filter);

    }

    @Then("the LJA ref data matching to result")
    @Then("the court ref data matching to result")
    @Then("the offence ref data matching to result")
    public void theRefDataMatchingToResult() {
        int totalCount = then().extract().jsonPath().getInt("count");
        int refDataList = then().extract().jsonPath().getList("refData").size();
        log.info("total count is : " + totalCount);
        log.info("Total records in the json response" + refDataList);
        then().assertThat()
            .statusCode(200)
            .body("count", Matchers.equalTo(refDataList));
    }

}
