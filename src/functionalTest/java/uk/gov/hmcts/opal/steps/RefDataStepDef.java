package uk.gov.hmcts.opal.steps;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import static org.hamcrest.Matchers.equalTo;
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
import static net.serenitybdd.rest.SerenityRest.then;
import static uk.gov.hmcts.opal.config.Constants.BUSINESS_UNIT_REF_DATA_URI;
import static uk.gov.hmcts.opal.config.Constants.COURTS_REF_DATA_URI;
import static uk.gov.hmcts.opal.config.Constants.ENFORCERS_REF_DATA_URI;
import static uk.gov.hmcts.opal.config.Constants.LJA_REF_DATA_URI;
import static uk.gov.hmcts.opal.config.Constants.MAJOR_CREDITORS_URI;
import static uk.gov.hmcts.opal.config.Constants.OFFENCES_REF_DATA_URI;


/**
 * Defines Cucumber steps for reference-data API scenarios.
 */
public class RefDataStepDef extends BaseStepDef {

    static Logger log = LoggerFactory.getLogger(RefDataStepDef.class.getName());
    CommonMethods methods = new CommonMethods();

    /**
     * Retrieves business-unit reference data filtered by business-unit type.
     *
     * @param filter business-unit type filter to apply to the request.
     */
    @When("I make a request to the business unit ref data api filtering by business unit type {string}")
    public void getRequestToBusinessUnitRefData(String filter) {

        methods.getRequest(BUSINESS_UNIT_REF_DATA_URI + "?q=" + filter);

    }

    /**
     * Retrieves business-unit reference data with the raw HTTP client to exercise the same
     * business-unit-type filter path.
     *
     * @param filter business-unit type filter to apply to the request.
     */
    @When("I make a raw request to the business unit ref data api filtering by business unit type {string}")
    public void getRawRequestToBusinessUnitRefData(String filter) {
        methods.getRequestUsingRawHttpClient(BUSINESS_UNIT_REF_DATA_URI + "?q=" + filter);
    }

    /**
     * Retrieves offence reference data for a specific business unit.
     *
     * @param businessUnitId business-unit identifier to apply to the request.
     */
    @When("I make a request to the offence ref data api filtering by business unit {int}")
    public void getRequestToOffencesRefDataBusinessUnit(int businessUnitId) {
        methods.getRequest(OFFENCES_REF_DATA_URI + "?business_unit_id=" + businessUnitId);
    }

    /**
     * Retrieves offence reference data filtered by CJS code.
     *
     * @param cjsCode CJS code to apply to the request.
     */
    @When("I make a request to the offence ref data api filtering by cjs code {string}")
    public void getRequestToOffencesRefDataCjsCode(String cjsCode) {
        methods.getRequest(OFFENCES_REF_DATA_URI + "?q=" + cjsCode);
    }

    /**
     * Retrieves offence reference data filtered by offence wording.
     *
     * @param filter offence-title filter to apply to the request.
     */
    @When("I make a request to the offence ref data api filtering with the offence title {string}")
    public void getRequestToOffencesRefDataWording(String filter) {
        methods.getRequest(OFFENCES_REF_DATA_URI + "?q=" + filter);
    }

    /**
     * Retrieves the major-creditor reference-data record for a specific identifier.
     *
     * @param majorCreditorId major-creditor identifier to request.
     */
    @When("I make a request to the major creditors ref data api filter by major creditor id {long}")
    public void getRequestToMajorCreditorsBy(long majorCreditorId) {
        methods.getRequest(MAJOR_CREDITORS_URI + "/" + majorCreditorId);
    }

    /**
     * Sends a request to the LJA reference-data API.
     */
    @When("I make a request to the LJA ref data api with")
    public void getRequestToLJARefData() {
        methods.getRequest(LJA_REF_DATA_URI);
    }

    /**
     * Retrieves LJA reference data filtered by one or more `lja_type` values.
     *
     * @param ljaTypeOrCsv single `lja_type` value or comma-separated list of allowed values.
     */
    @When("I make a request to the LJA ref data api with lja_type {string}")
    public void getRequestToLjaRefDataWithLjaType(String ljaTypeOrCsv) {
        methods.getRequest(LJA_REF_DATA_URI + "?lja_type=" + ljaTypeOrCsv);
    }

    /**
     * Retrieves court reference data filtered by court name or code.
     *
     * @param filter filter value to apply to the request.
     */
    @When("I make a request to the court ref data api with a filter of {string}")
    public void getRequestToCourtsRefDataWithFilter(String filter) {
        methods.getRequest(COURTS_REF_DATA_URI + "?q=" + filter);
    }

    /**
     * Retrieves court reference data using both a free-text filter and a business-unit filter.
     *
     * @param filter filter value to apply to the request.
     * @param businessUnitId business-unit identifier to apply to the request.
     */
    @When("I make a request to the court ref data api with a filter of {string} and a business unit of {int}")
    public void getRequestToCourtsRefDataWithFilterAndBU(String filter, int businessUnitId) {
        methods.getRequest(COURTS_REF_DATA_URI + "?q=" + filter + "?business_unit=" + businessUnitId);
    }

    /**
     * Retrieves court reference data for a specific business unit.
     *
     * @param businessUnitId business-unit identifier to apply to the request.
     */
    @When("I make a request to the court ref data api with a business unit of {int}")
    public void getRequestToCourtsRefDataWithBU(int businessUnitId) {
        methods.getRequest(COURTS_REF_DATA_URI + "?business_unit=" + businessUnitId);
    }

    /**
     * Asserts that the response count matches the number of returned reference-data records.
     */
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

    /**
     * Asserts that the response returns the seeded major-creditor record used by this scenario.
     */
    @Then("the major creditors ref data matching to result")
    public void theMajorCreditorsRefDataMatching() {
        then().assertThat().statusCode(HttpStatus.SC_OK).body("name", equalTo("LORD CHANCELLORS DEPARTMENT")).body(
            "majorCreditorId",
            equalTo(
                1300000000075L)
        );
    }

    /**
     * Asserts that the latest response contains the expected major-creditor data.
     *
     * @param data Cucumber table containing the expected values for the assertion.
     */
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

    /**
     * Asserts that the latest response does not contain the supplied major-creditor data.
     *
     * @param data Cucumber table containing the expected values for the assertion.
     */
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

    /**
     * Asserts that the latest response contains the expected court data.
     *
     * @param data Cucumber table containing the expected values for the assertion.
     */
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

    /**
     * Asserts that the latest response does not contain the supplied court data.
     *
     * @param data Cucumber table containing the expected values for the assertion.
     */
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

    /**
     * Asserts that the latest response contains the expected offence data.
     *
     * @param data Cucumber table containing the expected values for the assertion.
     */
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

    /**
     * Asserts that the latest response does not contain the supplied offence data.
     *
     * @param data Cucumber table containing the expected values for the assertion.
     */
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

    /**
     * Retrieves enforcer reference data filtered by enforcer name.
     *
     * @param enforcerName enforcer name to append to the request path.
     */
    @When("I make a request to enforcer ref data api filtering by name {string}")
    public void getRequestToEnforcerRefDataByName(String enforcerName) {
        methods.getRequest(ENFORCERS_REF_DATA_URI + enforcerName);
    }

    /**
     * Asserts that the first returned offence contains the expected field values.
     *
     * @param data Cucumber table containing the expected values for the assertion.
     */
    @Then("the response contains the below offence data fields and values")
    public void theResponseContainsTheBelowOffenceDataFieldsAndValues(DataTable data) {
        Map<String, String> expectedData = data.asMap(String.class, String.class);
        then().assertThat().statusCode(200);

        for (String key : expectedData.keySet()) {
            String apiResponseValue = then().extract().body().jsonPath().getString("refData[0]." + key);
            if (apiResponseValue == null && expectedData.get(key).equals("null")) {
                apiResponseValue = "null";
            }
            assertEquals(expectedData.get(key), apiResponseValue, "Values are not equal : ");
        }
    }

    /**
     * Asserts that every returned LJA has the expected `lja_type` value.
     *
     * @param expectedType expected `lja_type` value.
     */
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

    /**
     * Asserts that every returned LJA has an `lja_type` contained in the supplied allow-list.
     *
     * @param allowedTypesCsv comma-separated list of allowed `lja_type` values.
     */
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
