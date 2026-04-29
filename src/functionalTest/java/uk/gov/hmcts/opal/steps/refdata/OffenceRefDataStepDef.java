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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.opal.config.Constants.OFFENCES_REF_DATA_URI;

/**
 * Defines Cucumber steps for offence reference-data requests and assertions.
 */
public class OffenceRefDataStepDef {
    private static final Logger log = LoggerFactory.getLogger(OffenceRefDataStepDef.class);

    private final CommonMethods methods = new CommonMethods();

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
            log.info("Expected name: {} Expected buId: {}", expectedOffenceTitle, expectedBuId);
            log.info("\nApi response: \n   Count: {}/{}\n   {}\n   {}", i + 1, totalCount, actualOffenceTitle,
                     actualBuId);
            if (expectedOffenceTitle == null) {
                log.info("Expected name is null - Skipping check");
            } else {
                assertTrue(
                    actualOffenceTitle.toLowerCase().contains(expectedOffenceTitle.toLowerCase()),
                    "Court name does not match : " + actualOffenceTitle + " : " + expectedOffenceTitle
                );
            }
            if ("null".equals(expectedBuId)) {
                assertNull(actualBuId);
                break;
            }
            if ("not null".equals(expectedBuId)) {
                assertNotNull(actualBuId);
                break;
            }
            assertEquals(expectedBuId, actualBuId);
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
            log.info("\nApi response: \n   Count: {}/{}\n   {}\n   {}", i + 1, totalCount, actualOffenceTitle,
                     actualBuId);
            assertNotEquals(expectedBuId, actualBuId);
        }
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
            if (apiResponseValue == null && "null".equals(expectedData.get(key))) {
                apiResponseValue = "null";
            }
            assertEquals(expectedData.get(key), apiResponseValue, "Values are not equal : ");
        }
    }
}
