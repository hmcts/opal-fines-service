package uk.gov.hmcts.opal.steps.refdata;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.steps.CommonMethods;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.opal.config.Constants.COURTS_REF_DATA_URI;

/**
 * Defines Cucumber steps for court reference-data requests and assertions.
 */
public class CourtRefDataStepDef {
    private static final Logger log = LoggerFactory.getLogger(CourtRefDataStepDef.class);

    private final CommonMethods methods = new CommonMethods();

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
        methods.getRequest(COURTS_REF_DATA_URI + "?q=" + filter + "&business_unit=" + businessUnitId);
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
            log.info("\nApi response: \n   Count: {}/{}\n   {}\n   {}", i + 1, totalCount, actualName, actualBuId);
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
            log.info("\nApi response: \n   Count: {}/{}\n   {}\n   {}", i + 1, totalCount, actualName, actualBuId);
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

    private static void assertNotEquals(String expectedBuId, String actualBuId) {
        org.junit.jupiter.api.Assertions.assertNotEquals(expectedBuId, actualBuId);
    }
}
