package uk.gov.hmcts.opal.steps.refdata;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.http.HttpStatus;
import uk.gov.hmcts.opal.steps.CommonMethods;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static uk.gov.hmcts.opal.config.Constants.MAJOR_CREDITORS_URI;

/**
 * Defines Cucumber steps for major-creditor reference-data requests and assertions.
 */
public class MajorCreditorRefDataStepDef {
    private final CommonMethods methods = new CommonMethods();

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
     * Asserts that the response returns the seeded major-creditor record used by this scenario.
     */
    @Then("the major creditors ref data matching to result")
    public void theMajorCreditorsRefDataMatching() {
        then().assertThat().statusCode(HttpStatus.SC_OK).body("name", equalTo("LORD CHANCELLORS DEPARTMENT")).body(
            "majorCreditorId",
            equalTo(1300000000075L)
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

        assertEquals(expected.get("major_creditor_id"), then().extract().jsonPath().getString("majorCreditorId"));
        assertEquals(
            expected.get("major_creditor_code"),
            then().extract().jsonPath().getString("majorCreditorCode")
        );
        assertEquals(expected.get("name"), then().extract().jsonPath().getString("name"));
        assertEquals(expected.get("business_unit_id"), then().extract().jsonPath().getString("businessUnitId"));
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

        assertNotEquals(expected.get("major_creditor_id"), then().extract().jsonPath().getString("majorCreditorId"));
        assertNotEquals(
            expected.get("major_creditor_code"),
            then().extract().jsonPath().getString("majorCreditorCode")
        );
        assertNotEquals(expected.get("name"), then().extract().jsonPath().getString("name"));
        assertNotEquals(expected.get("business_unit_id"), then().extract().jsonPath().getString("businessUnitId"));
    }
}
