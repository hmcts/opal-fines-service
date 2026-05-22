package uk.gov.hmcts.opal.steps.offences;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Defines Cucumber steps for offence-search response assertions.
 */
public class SearchOffencesResponseStepDef extends BaseStepDef {
    //static Logger log = LoggerFactory.getLogger(SearchOffencesResponseStepDef.class.getName());

    /**
     * Asserts that the offence-search request returned the expected HTTP status code.
     *
     * @param statusCode expected HTTP status code.
     */
    @Then("The offence search response returns {int}")
    public void draftAccountResponse(int statusCode) {
        then().assertThat()
                .statusCode(statusCode);
    }

    /**
     * Asserts that every returned offence has a CJS code starting with the requested prefix.
     *
     * @param cjsCode CJS code prefix expected in the response.
     */
    @Then("the response contains results with a cjs code starting with {string}")
    public void offenceResponseCjsStartsWith(String cjsCode) {

        List<String> cjsCodes = SerenityRest.then().extract().jsonPath().getList("searchData.cjs_code");
        //log.info("CJS Codes: {}", cjsCodes);

        cjsCodes.forEach(code -> {
            //log.info("Checking CJS Code: {}", code);
            //log.info("CJS Code starts with: {}", cjsCode);
            assertTrue(code.startsWith(cjsCode));
        });
    }

    /**
     * Asserts that every returned offence contains the expected field values.
     *
     * @param expectedData Cucumber table containing the expected response values.
     */
    @Then("the offences in the response contain the following data")
    public void offenceResponseContainsData(DataTable expectedData) {
        Map<String, String> expectedDataMap = expectedData.asMap(String.class, String.class);
        int count = SerenityRest.then().extract().jsonPath().getInt("count");

        for (int i = 0; i < count; i++) {
            for (String key : expectedDataMap.keySet()) {
                String actual = SerenityRest.then().extract().path("searchData[" + i + "]." + key);
                assertTrue(actual.contains(expectedDataMap.get(key)
                ), "Values are not equal: " + key + " - " + actual + " - " + expectedDataMap.get(key));
            }
        }
    }

    /**
     * Asserts that the response contains all expected CJS codes.
     *
     * @param expectedCodesTable Cucumber table containing expected CJS codes.
     */
    @Then("the response contains the following cjs codes")
    public void responseContainsCjsCodes(DataTable expectedCodesTable) {

        List<String> expectedCodes = expectedCodesTable.asList();

        List<String> actualCodes = SerenityRest.then()
            .extract()
            .jsonPath()
            .getList("searchData.cjs_code");

        System.out.println("Actual CJS Codes: " + actualCodes);

        List<String> missingCodes = new ArrayList<>();

        for (String expectedCode : expectedCodes) {

            if (!actualCodes.contains(expectedCode)) {
                missingCodes.add(expectedCode);
            }
        }

        assertTrue(
            missingCodes.isEmpty(),
            "Missing expected CJS codes: " + missingCodes
        );
    }

    /**
     * Asserts that every returned offence is active for the supplied date.
     *
     * @param activeDate active date to compare against the returned offence dates.
     */
    @Then("the offences in the response are before {string} only")
    public void offenceResponseBeforeDate(String activeDate) {
        // Format the active date string to a LocalDateTime object
        OffsetDateTime parsedActiveDate = OffsetDateTime.parse(activeDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        // Extract the list of dates from the response
        List<String> usedFromDates = SerenityRest.then().extract().jsonPath().getList("searchData.date_used_from");
        List<String> usedToDates = SerenityRest.then().extract().jsonPath().getList("searchData.date_used_to");

        // Iterate through each date in the response
        for (String dateFromResponse : usedFromDates) {
            // Parse the date from the response to a LocalDateTime object
            OffsetDateTime parsedDateFromResponse = OffsetDateTime.parse(dateFromResponse);
            // Assert that the date from the response is before the active date
            assertTrue(parsedDateFromResponse.isBefore(parsedActiveDate),
                "Response date is not before Active date: "
                            + "\n Date from response: " + parsedDateFromResponse
                            + "\n Active Date: " + parsedActiveDate);
        }
        for (String dateFromResponse : usedToDates) {
            // Parse the date from the response to a LocalDateTime object
            OffsetDateTime parsedDateFromResponse = OffsetDateTime.parse(dateFromResponse);
            // Assert that the active date is before the used to date from the response
            assertTrue(parsedActiveDate.isBefore(parsedDateFromResponse), "Active date is not before Response date: "
                + "\n Date from response: " + parsedDateFromResponse
                            + "\n Active Date: " + parsedActiveDate);
        }
    }

    /**
     * Asserts that the response contains the expected number of offence records.
     *
     * @param count expected number of matching records.
     */
    @Then("there are {int} offences in the response")
    public void offenceResponseContainsCount(int count) {
        int responseCount = then().extract().jsonPath().getInt("count");
        int cjsCodes = SerenityRest.then().extract().jsonPath().getList("searchData.cjs_code").size();
        assertEquals(responseCount, count, "Expected count: " + count + " Actual count: " + responseCount);
        assertEquals(cjsCodes, count, "Expected count: " + count + " Actual count: " + cjsCodes);
    }
}
