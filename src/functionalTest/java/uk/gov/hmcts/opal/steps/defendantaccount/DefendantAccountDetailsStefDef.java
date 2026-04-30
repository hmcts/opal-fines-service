package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Defines Cucumber steps for the defendant-account details API.
 */
public class DefendantAccountDetailsStefDef extends BaseStepDef {
    private static final Logger log = LoggerFactory.getLogger(DefendantAccountDetailsStefDef.class);

    /**
     * Retrieves the header-summary details for the defendant account identified in the scenario
     * data.
     *
     * @param id Cucumber table containing the defendant account identifier.
     */
    @When("I make a request to the defendant account details api with")
    public void getDefendantAccountDetailsByID(DataTable id) {
        Map<String, String> idToSend = id.asMap(String.class, String.class);
        authorisedJsonRequest()
            .when()
            .get(getTestUrl() + "/defendant-accounts/" + idToSend.get("defendantID") + "/header-summary");
    }

    /**
     * Asserts that the defendant-account details response contains the expected field values.
     *
     * @param fields Cucumber table containing the expected values for the assertion.
     */
    @Then("the response from the defendant account details api is")
    public void assertDefendantAccountDetailsResponseMatches(DataTable fields) {
        Map<String, String> response = fields.asMap(String.class, String.class);
        int rows = response.size();

        log.info("Asserting {} fields from the defendant-account details response", rows);

        then().assertThat()
            .statusCode(200);

        for (String key : response.keySet()) {
            String apiResponseValue = then().extract().body().jsonPath().getString(key);
            assertEquals("Values are not equal : ", apiResponseValue, response.get(key));
        }

    }

}
