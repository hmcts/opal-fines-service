package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.serenitybdd.rest.SerenityRest.then;
import static uk.gov.hmcts.opal.config.Constants.BUSINESS_UNIT_REF_DATA_URI;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class BusinessUnitsStepDef extends BaseStepDef {

    static Logger log = LoggerFactory.getLogger(BusinessUnitsStepDef.class.getName());

    @When("I make a request to the business unit ref data api")
    public void getRequestToBusinessUnitRefData() {
        SerenityRest
            .given()
            .accept("*/*")
            .header("Authorization", "Bearer " + getToken())
            .contentType("application/json")
            .when()
            .get(getTestUrl() + BUSINESS_UNIT_REF_DATA_URI)
            .then().log().all();

    }

    @Then("the business unit ref data matching to result")
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
