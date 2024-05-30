package uk.gov.hmcts.opal.steps;


import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.apache.http.HttpStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.opal.config.Constants.BUSINESS_UNIT_REF_DATA_URI;
import static uk.gov.hmcts.opal.config.Constants.OFFENCES_REF_DATA_URI;
import static uk.gov.hmcts.opal.config.Constants.MAJOR_CREDITORS_URI;
import static uk.gov.hmcts.opal.config.Constants.LJA_REF_DATA_URI;
import static uk.gov.hmcts.opal.config.Constants.COURTS_REF_DATA_URI;
import static uk.gov.hmcts.opal.config.Constants.ENFORCERS_REF_DATA_URI;


public class RefDataStepDef extends BaseStepDef {

    static Logger log = LoggerFactory.getLogger(RefDataStepDef.class.getName());
    CommonMethods methods = new CommonMethods();

    @When("I make a request to the business unit ref data api filtering by business unit type {string}")
    public void getRequestToBusinessUnitRefData(String filter) {

        methods.getRequest(BUSINESS_UNIT_REF_DATA_URI + filter);

    }

    @When("I make a request to the offence ref data api filtering by cjs code {string}")
    public void getRequestToOffencesRefData(String filter) {
        methods.getRequest(OFFENCES_REF_DATA_URI + filter);
    }

    @When("I make a request to the major creditors ref data api filter by major creditor id {int}")
    public void getRequestToMajorCreditorsBy(int majorCreditorId) {
        methods.getRequest(MAJOR_CREDITORS_URI + majorCreditorId);
    }

    @When("I make a request to the LJA ref data api with")
    public void getRequestToLJARefData() {
        methods.getRequest(LJA_REF_DATA_URI);
    }

    @When("I make a request to the court ref data api with")
    public void getRequestToCourtsRefData() {
        methods.getRequest(COURTS_REF_DATA_URI);
    }

    @Then("the LJA ref data matching to result")
    @Then("the court ref data matching to result")
    @Then("the offence ref data matching to result")
    @Then("the enforcer ref data matching to result")
    @Then("the business unit ref data matching to result")
    public void theRefDataMatchingToResult() {
        int totalCount = then().extract().jsonPath().getInt("count");
        int refDataList = then().extract().jsonPath().getList("refData").size();
        log.info("total count is : " + totalCount);
        log.info("Total records in the json response" + refDataList);
        then().assertThat()
            .statusCode(200)
            .body("count", equalTo(refDataList));
    }

    @Then("the major creditors ref data matching to result")
    public void theMajorCreditorsRefDataMatching() {
        then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .body("name",equalTo("CHESTERFIELD BOROUGH COUNCIL"))
            .body("majorCreditorId",equalTo(15));
    }

    @When("I make a request to enforcer ref data api filtering by name {string}")
    public void getRequestToEnforcerRefDataByName(String enforcerName) {
        methods.getRequest(ENFORCERS_REF_DATA_URI + enforcerName);
    }

}
