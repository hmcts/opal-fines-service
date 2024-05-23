package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.opal.config.Constants.MAJOR_CREDITORS_URI;

import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class MajorCreditorsStepDef extends BaseStepDef {

    static Logger log = LoggerFactory.getLogger(OffencesStepDef.class.getName());

    @When("I make a request to the major creditors ref data api filter by major creditor id {int}")
    public void getRequestToMajorCreditorsBy(int majorCreditorId) {
        log.info("THE URL----" + getTestUrl() + MAJOR_CREDITORS_URI);

        SerenityRest
            .given()
            .accept("*/*")
            .header("Authorization", "Bearer " + getToken())
            .contentType("application/json")
            .when()
            .get(getTestUrl() + MAJOR_CREDITORS_URI + majorCreditorId);

    }

    @Then("the major creditors ref data matching to result")
    public void theMajorCreditorsRefDataMatchingToResult() {
        then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .body("name",equalTo("CHESTERFIELD BOROUGH COUNCIL"))
            .body("majorCreditorId",equalTo(15));



    }
}
