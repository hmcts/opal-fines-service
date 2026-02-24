package uk.gov.hmcts.opal.steps;


import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import static org.hamcrest.Matchers.equalTo;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.core.Serenity;
import static net.serenitybdd.rest.SerenityRest.then;
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

        methods.getRequest(BUSINESS_UNIT_REF_DATA_URI + "?q=" + filter);

    }

    @When("I make a request to the offence ref data api filtering by business unit {int}")
    public void getRequestToOffencesRefDataBusinessUnit(int businessUnitId) {
        methods.getRequest(OFFENCES_REF_DATA_URI + "?business_unit_id=" + businessUnitId);
    }

    @When("I make a request to the offence ref data api filtering by cjs code {string}")
    public void getRequestToOffencesRefDataCjsCode(String cjsCode) {
        methods.getRequest(OFFENCES_REF_DATA_URI + "?q=" + cjsCode);
    }

    @When("I make a request to the offence ref data api filtering with the offence title {string}")
    public void getRequestToOffencesRefDataWording(String filter) {
        methods.getRequest(OFFENCES_REF_DATA_URI + "?q=" + filter);
    }

    @When("I make a request to the major creditors ref data api filter by major creditor id {long}")
    public void getRequestToMajorCreditorsBy(long majorCreditorId) {
        methods.getRequest(MAJOR_CREDITORS_URI + "/" + majorCreditorId);
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
        methods.getRequest(COURTS_REF_DATA_URI + "?q=" + filter);
    }

    @When("I make a request to the court ref data api with a filter of {string} and a business unit of {int}")
    public void getRequestToCourtsRefDataWithFilterAndBU(String filter, int businessUnitId) {
        methods.getRequest(COURTS_REF_DATA_URI + "?q=" + filter + "?business_unit=" + businessUnitId);
    }

    @When("I make a request to the court ref data api with a business unit of {int}")
    public void getRequestToCourtsRefDataWithBU(int businessUnitId) {
        methods.getRequest(COURTS_REF_DATA_URI + "?business_unit=" + businessUnitId);
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
        then().assertThat().statusCode(200).body("count", equalTo(refDataList));
    }

    @Then("the response contains the correct court data when filtered by court name {string}")
    public void theResponseContainsTheCorrectCourt(String courtName) throws SQLException, JSONException {
        int totalCount = then().extract().jsonPath().getInt("count");
        JSONArray court;
        court = db.getCourtsByCourtName(courtName);
        then().assertThat().statusCode(200);
        for (int i = 0; i < totalCount; i++) {
            String courtId = then().extract().jsonPath().getString("refData.court_id[" + i + "]");
            assertEquals(courtId, court.getJSONObject(i).getString("court_id"));

            String businessUnitId = then().extract().jsonPath().getString("refData.business_unit_id[" + i + "]");
            assertEquals(businessUnitId, court.getJSONObject(i).getString("business_unit_id"));

            String courtCode = then().extract().jsonPath().getString("refData.court_code[" + i + "]");
            assertEquals(courtCode, court.getJSONObject(i).getString("court_code"));

            String name = then().extract().jsonPath().getString("refData.name[" + i + "]");
            assertEquals(name, court.getJSONObject(i).getString("court_name"));

        }
    }

    @Then("the major creditors ref data matching to result")
    public void theMajorCreditorsRefDataMatching() {
        then().assertThat().statusCode(HttpStatus.SC_OK).body("name", equalTo("LORD CHANCELLORS DEPARTMENT")).body(
            "majorCreditorId",
            equalTo(
                1300000000075L)
        );
    }

    @Then("the response contains the below major creditor data")
    public void responseContainsMajorCreditorData(DataTable data) {
        Map<String, String> expected = data.asMap(String.class, String.class);
        then().assertThat().statusCode(200);

        assertEquals(
            expected.get("majorCreditorId"),
            then().extract().jsonPath().getString("major_creditor_id")
        );
        assertEquals(
            expected.get("majorCreditorCode"),
            then().extract().jsonPath().getString("major_creditor_code")
        );
        assertEquals(expected.get("name"), then().extract().jsonPath().getString("name"));
        assertEquals(
            expected.get("business_unit_d"),
            then().extract().jsonPath().getString("business_unit.business_unit_id")
        );
    }

    @Then("the response does not contain the below major creditor data")
    public void responseDoesNotContainMajorCreditorData(DataTable data) {
        Map<String, String> expected = data.asMap(String.class, String.class);
        then().assertThat().statusCode(200);

        assertNotEquals(
            expected.get("major_creditor_id"),
            then().extract().jsonPath().getString("major_creditor_id")
        );
        assertNotEquals(
            expected.get("major_creditor_code"),
            then().extract().jsonPath().getString("major_creditor_code")
        );
        assertNotEquals(expected.get("name"), then().extract().jsonPath().getString("name"));
        assertNotEquals(
            expected.get("business_unit_id"),
            then().extract().jsonPath().getString("businessUnit.business_unit_id")
        );
    }

    @Then("the response contains the below courts data")
    public void responseContainsCourtData(DataTable data) {
        Map<String, String> expectedData = data.asMap(String.class, String.class);
        int totalCount = then().extract().jsonPath().getInt("count");
        then().assertThat().statusCode(200);
        for (int i = 0; i < totalCount; i++) {
            String actualName = then().extract().jsonPath().getString("refData.name[" + i + "]");
            String actualBuId = then().extract().jsonPath().getString("refData.business_unit_id[" + i + "]");
            String expectedName = expectedData.get("name");
            String expectedBuId = expectedData.get("business_unit_id");
            log.info("\nApi response: \n" + "   Count: " + (i + 1) + "/" + totalCount + "\n   "
                         + actualName + "\n   " + actualBuId);
            if (expectedName == null) {
                log.info("Expected name is null - Skipping check");
            } else {
                assertTrue(
                    actualName.toLowerCase().contains(expectedName.toLowerCase()),
                    "Court name does not match : " + actualName + " : " + expectedName
                );
            }
            assertEquals(expectedBuId, actualBuId);

        }
    }

    @Then("the response does not contain the below courts data")
    public void responseDoesNotContainCourtData(DataTable data) {
        Map<String, String> expectedData = data.asMap(String.class, String.class);
        int totalCount = then().extract().jsonPath().getInt("count");
        then().assertThat().statusCode(200);
        for (int i = 0; i < totalCount; i++) {
            String actualName = then().extract().jsonPath().getString("refData.name[" + i + "]");
            String actualBuId = then().extract().jsonPath().getString("refData.business_unit_id[" + i + "]");
            String expectedName = expectedData.get("name");
            String expectedBuId = expectedData.get("business_unit_id");
            log.info("\nApi response: \n" + "   Count: " + (i + 1) + "/" + totalCount + "\n   "
                         + actualName + "\n   " + actualBuId);
            if (expectedName == null) {
                log.info("Expected name is null - Skipping check");
            } else {
                assertFalse(
                    actualName.toLowerCase().contains(expectedName.toLowerCase()),
                    "Court name matches : " + actualName + " : " + expectedName
                );
            }
            assertNotEquals(expectedBuId, actualBuId);

        }
    }

    @Then("the response contains the below offence data")
    public void responseContainsOffenceData(DataTable data) {
        Map<String, String> expectedData = data.asMap(String.class, String.class);
        int totalCount = then().extract().jsonPath().getInt("count");
        then().assertThat().statusCode(200);
        for (int i = 0; i < totalCount; i++) {
            String actualOffenceTitle = then().extract().jsonPath().getString("refData._offence_title[" + i + "]");
            String actualBuId = then().extract().jsonPath().getString("refData.business_unit_id[" + i + "]");
            String expectedOffenceTitle = expectedData.get("offenceTitle");
            String expectedBuId = expectedData.get("business_unit_id");
            log.info("Expected name: " + expectedOffenceTitle + " Expected buId: " + expectedBuId);
            log.info("\nApi response: \n" + "   Count: " + (i + 1) + "/" + totalCount + "\n   "
                         + actualOffenceTitle + "\n   " + actualBuId);
            if (expectedOffenceTitle == null) {
                log.info("Expected name is null - Skipping check");
            } else {
                assertTrue(
                    actualOffenceTitle.toLowerCase().contains(expectedOffenceTitle.toLowerCase()),
                    "Court name does not match : " + actualOffenceTitle + " : " + expectedOffenceTitle
                );
            }
            if (expectedBuId.equals("null")) {
                assertNull(actualBuId);
                break;
            }
            if (expectedBuId.equals("not null")) {
                assertNotNull(actualBuId);
                break;
            } else {
                assertEquals(expectedBuId, actualBuId);
            }
        }
    }

    @Then("the response does not contain the below offence data")
    public void responseDoesNotContainOffenceData(DataTable data) {
        Map<String, String> expectedData = data.asMap(String.class, String.class);
        int totalCount = then().extract().jsonPath().getInt("count");
        then().assertThat().statusCode(200);
        for (int i = 0; i < totalCount; i++) {
            String actualOffenceTitle = then().extract().jsonPath().getString("refData.offence_title[" + i + "]");
            String actualBuId = then().extract().jsonPath().getString("refData.business_unit_id[" + i + "]");
            String expectedBuId = expectedData.get("business_unit_id");
            log.info("\nApi response: \n" + "   Count: " + (i + 1) + "/" + totalCount + "\n   "
                         + actualOffenceTitle + "\n   " + actualBuId);
            assertNotEquals(expectedBuId, actualBuId);

        }
    }

    @Then("the response contains the correct major creditor data when filtered by id {int}")
    public void theResponseContainsTheCorrectMajorCreditor(int majorCreditorId) throws SQLException, JSONException {
        JSONArray majorCreditor;
        majorCreditor = db.getMajorCredByID(String.valueOf(majorCreditorId));
        then().assertThat().statusCode(200);
        String majorCreditorFromAPI = then().extract().jsonPath().getString("major_creditor_id");
        assertEquals(majorCreditorFromAPI, majorCreditor.getJSONObject(0).getString("major_creditor_id"));

        String businessUnitId = then().extract().jsonPath().getString("businessUnit.business_unit_id");
        assertEquals(businessUnitId, majorCreditor.getJSONObject(0).getString("business_unit_id"));

        String majorCreditorCode = then().extract().jsonPath().getString("major_creditor_code");
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
        then().assertThat().statusCode(200);
        String majorCreditorFromAPI = then().extract().jsonPath().getString("major_creditor_id");
        assertNotEquals(majorCreditorFromAPI, majorCreditor.getJSONObject(0).getString("major_creditor_id"));

        String majorCreditorCode = then().extract().jsonPath().getString("major_creditor_code");
        assertNotEquals(majorCreditorCode, majorCreditor.getJSONObject(0).getString("major_creditor_code"));

        String name = then().extract().jsonPath().getString("name");
        assertNotEquals(name, majorCreditor.getJSONObject(0).getString("major_creditor_name"));

        Serenity.recordReportData().withTitle("Data from DB").andContents(majorCreditor.toString());
    }

    @When("I make a request to enforcer ref data api filtering by name {string}")
    public void getRequestToEnforcerRefDataByName(String enforcerName) {
        methods.getRequest(ENFORCERS_REF_DATA_URI + enforcerName);
    }

    @Then("the response contains the below offence data fields and values")
    public void theResponseContainsTheBelowOffenceDataFieldsAndValues(DataTable data) {
        Map<String, String> expectedData = data.asMap(String.class, String.class);
        then().assertThat().statusCode(200);

        for (String key : expectedData.keySet()) {
            String apiResponseValue = then().extract().body().jsonPath().getString("refData[0]." + key);
            if (apiResponseValue == null && expectedData.get(key).equals("null")) {
                apiResponseValue = "null";
            }
            Assert.assertEquals("Values are not equal : ", expectedData.get(key), apiResponseValue);
        }
    }

    @When("I make a request to the LJA ref data api with lja_type {string}")
    public void getRequestToLjaRefDataWithLjaType(String ljaTypeOrCsv) {
        methods.getRequest(LJA_REF_DATA_URI + "?lja_type=" + ljaTypeOrCsv);
    }

    @Then("all returned LJAs have lja_type {string}")
    public void allReturnedLjasHaveLjaType(String expectedType) {
        List<String> types = then().extract().jsonPath().getList("refData.lja_type");

        org.hamcrest.MatcherAssert.assertThat(
            "Expected at least one result", types,
            org.hamcrest.Matchers.not(org.hamcrest.Matchers.empty())
        );
        org.hamcrest.MatcherAssert.assertThat(
            types,
            org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.equalTo(expectedType))
        );
    }

    @Then("all returned LJAs have lja_type in {string}")
    public void allReturnedLjasHaveTypeIn(String allowedTypesCsv) {
        List<String> allowed = Arrays.stream(allowedTypesCsv.split(","))
            .map(String::trim)
            .toList();

        List<String> types = then().extract().jsonPath().getList("refData.lja_type");

        assertThat("Expected at least one result", types, not(empty()));

        boolean allAllowed = types.stream().allMatch(allowed::contains);
        assertThat("Found lja_type not in allowed set. Allowed=" + allowed + ", Actual=" + types,
                   allAllowed, is(true));
    }

}






