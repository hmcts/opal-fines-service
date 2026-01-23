package uk.gov.hmcts.opal.steps;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.opal.config.Constants.RESULTS_URI;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.opal.utils.RequestSupport;

public class GetResultsStepDef extends BaseStepDef {

    @When("I make a request to get the results {string}")
    public void getResults(String resultIds) {
        RequestSupport.responseProcessor(
            SerenityRest
                .given()
                .spec(RequestSupport.getRequestSpec(RESULTS_URI).build())
                .param("result_ids", resultIds)
                .when()
                .get()
                .then()
        );
    }

    @Then("The results response contains {int} results")
    public void resultsResponseContainsCount(int count) {
        then().assertThat()
            .statusCode(200).body("count", equalTo(count));
    }

    @Then("The results response contains the following result")
    public void resultsResponseContains(DataTable data) {
        Map<String, String> expected = data.asMap(String.class, String.class);
        String resultID = expected.get("result_id");
        for (String key : expected.keySet()) {
            String actual = then().extract().body().jsonPath().getString("refData.find { it.result_id == '"
                + resultID + "' }." + key);
            assertEquals(expected.get(key), actual, "Values are not equal");
        }
    }

    /**
     * Verifies the HTTP status code of the last API response.
     */
    @Then("the response status is {int}")
    public void theResponseStatusIs(int status) {
        then()
            .log().ifValidationFails()  // shows payload on failure
            .statusCode(status);
    }
}
