package uk.gov.hmcts.opal.steps;


import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.core.Serenity;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static uk.gov.hmcts.opal.config.Constants.BUSINESS_UNIT_REF_DATA_URI;
import static uk.gov.hmcts.opal.config.Constants.COURTS_REF_DATA_URI;
import static uk.gov.hmcts.opal.config.Constants.ENFORCERS_REF_DATA_URI;
import static uk.gov.hmcts.opal.config.Constants.LJA_REF_DATA_URI;
import static uk.gov.hmcts.opal.config.Constants.MAJOR_CREDITORS_URI;
import static uk.gov.hmcts.opal.config.Constants.OFFENCES_REF_DATA_URI;


public class RefDataStepDef extends BaseStepDef {

    static Logger log = LoggerFactory.getLogger(RefDataStepDef.class.getName());
    CommonMethods methods = new CommonMethods();
    DatabaseStepDef db = new DatabaseStepDef();

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

    @When("I make a request to the court ref data api with a filter of {string}")
    public void getRequestToCourtsRefDataWithFilter(String filter) {
        methods.getRequest(COURTS_REF_DATA_URI + filter);
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

    @Then("the response contains the correct court data when filtered by court name {string}")
    public void theResponseContainsTheCorrectCourt(String courtName) throws SQLException, JSONException {
        int totalCount = then().extract().jsonPath().getInt("count");
        JSONArray court;
        court = db.getCourtsByCourtName(courtName);
        then().assertThat()
            .statusCode(200);
        for (int i = 0; i < totalCount; i++) {
            String courtId = then().extract().jsonPath().getString("refData.courtId[" + i + "]");
            assertEquals(courtId, court.getJSONObject(i).getString("court_id"));

            String businessUnitId = then().extract().jsonPath().getString("refData.businessUnitId[" + i + "]");
            assertEquals(businessUnitId, court.getJSONObject(i).getString("business_unit_id"));

            String courtCode = then().extract().jsonPath().getString("refData.courtCode[" + i + "]");
            assertEquals(courtCode, court.getJSONObject(i).getString("court_code"));

            String name = then().extract().jsonPath().getString("refData.name[" + i + "]");
            assertEquals(name, court.getJSONObject(i).getString("court_name"));

        }
    }

    @Then("the major creditors ref data matching to result")
    public void theMajorCreditorsRefDataMatching() {
        then().assertThat()
            .statusCode(HttpStatus.SC_OK)
            .body("name", equalTo("CHESTERFIELD BOROUGH COUNCIL"))
            .body("majorCreditorId", equalTo(15));
    }

    @Then("the response contains the correct major creditor data when filtered by id {int}")
    public void theResponseContainsTheCorrectMajorCreditor(int majorCreditorId) throws SQLException, JSONException {
        JSONArray majorCreditor;
        majorCreditor = db.getMajorCredByID(String.valueOf(majorCreditorId));
        then().assertThat()
            .statusCode(200);
        String majorCreditorFromAPI = then().extract().jsonPath().getString("majorCreditorId");
        assertEquals(majorCreditorFromAPI, majorCreditor.getJSONObject(0).getString("major_creditor_id"));

        String businessUnitId = then().extract().jsonPath().getString("businessUnit.businessUnitId");
        assertEquals(businessUnitId, majorCreditor.getJSONObject(0).getString("business_unit_id"));

        String majorCreditorCode = then().extract().jsonPath().getString("majorCreditorCode");
        assertEquals(majorCreditorCode, majorCreditor.getJSONObject(0).getString("major_creditor_code"));

        String name = then().extract().jsonPath().getString("name");
        assertEquals(name, majorCreditor.getJSONObject(0).getString("major_creditor_name"));

        Serenity.recordReportData().withTitle("Data from DB").andContents(majorCreditor.toString());
    }

    @Then("the response does not contain the major creditor data for {int}")
    public void theResponseDoesNotContainTheCorrectMajorCreditor(int majorCreditorId)
        throws SQLException, JSONException {
        JSONArray majorCreditor;
        majorCreditor = db.getMajorCredByID(String.valueOf(majorCreditorId));
        then().assertThat()
            .statusCode(200);
        String majorCreditorFromAPI = then().extract().jsonPath().getString("majorCreditorId");
        assertNotEquals(majorCreditorFromAPI, majorCreditor.getJSONObject(0).getString("major_creditor_id"));

        String majorCreditorCode = then().extract().jsonPath().getString("majorCreditorCode");
        assertNotEquals(majorCreditorCode, majorCreditor.getJSONObject(0).getString("major_creditor_code"));

        String name = then().extract().jsonPath().getString("name");
        assertNotEquals(name, majorCreditor.getJSONObject(0).getString("major_creditor_name"));

        Serenity.recordReportData().withTitle("Data from DB").andContents(majorCreditor.toString());
    }

    @When("I make a request to enforcer ref data api filtering by name {string}")
    public void getRequestToEnforcerRefDataByName(String enforcerName) {
        methods.getRequest(ENFORCERS_REF_DATA_URI + enforcerName);
    }

}
