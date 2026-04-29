package uk.gov.hmcts.opal.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;

/**
 * Defines Cucumber steps for the defendant search API.
 */
public class DefendantSearchApiStepDef extends BaseStepDef {

    /**
     * Searches for defendant accounts using the supplied name, address, and date-of-birth
     * criteria.
     *
     * @param searchCriteria Cucumber table containing the search criteria for the request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I make a call to the defendant search API using the parameters")
    public void postToDefendantSearchAPI(DataTable searchCriteria) throws JSONException {
        Map<String, String> dataToPost = searchCriteria.asMap(String.class, String.class);

        JSONObject requestBody = addToNewJsonObject(dataToPost, "forename", "surname", "address_line");
        JSONObject dateOfBirth = addToNewJsonObject(dataToPost, "day_of_month", "month_of_year", "year");
        requestBody.put("date_of_birth", dateOfBirth);

        authorisedJsonRequest()
            .body(requestBody.toString())
            .when()
            .post(getTestUrl() + "/defendant-accounts/search");

    }

    /**
     * Asserts that exactly one defendant search result is returned and matches the expected data.
     *
     * @param expectedData Cucumber table containing the expected response values.
     */
    @Then("there is one result returned matching")
    public void thereIsOneResultReturnedMatching(DataTable expectedData) {
        Map<String, String> expectedResult = expectedData.asMap(String.class, String.class);

        then().assertThat()
            .statusCode(200)
            .body("total_count", Matchers.equalTo(1))
            .body("search_results.name[0]", Matchers.equalTo(expectedResult.get("name")))
            .body("search_results.date_of_birth[0]", Matchers.equalTo(expectedResult.get("dateOfBirth")))
            .body("search_results.address_line_1[0]", Matchers.equalTo(expectedResult.get("addressLine1")));
    }

}
