package uk.gov.hmcts.opal.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.opal.config.Constants.RESULTS_URI;

/**
 * Defines Cucumber steps for the results API.
 */
public class GetResultsStepDef extends BaseStepDef {
    /**
     * Requests the results endpoint for the supplied result identifiers.
     *
     * @param resultIds comma-separated result identifiers to request.
     */
    @When("I make a request to get the results {string}")
    public void getResults(String resultIds) {
        authorisedJsonRequest()
            .param("result_ids", resultIds)
            .when()
            .get(getTestUrl() + RESULTS_URI);
    }

    /**
     * Asserts that the results response contains the expected number of records.
     *
     * @param count expected number of matching records.
     */
    @Then("The results response contains {int} results")
    public void resultsResponseContainsCount(int count) {
        then().assertThat()
            .statusCode(200).body("count", equalTo(count));
    }

    /**
     * Asserts that the results response contains the following result.
     *
     * @param data Cucumber table containing the expected values for the assertion.
     */
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
     * Asserts that the latest API or raw-client response returned the expected HTTP status code.
     *
     * @param status expected HTTP status code.
     */
    @Then("the response status is {int}")
    public void theResponseStatusIs(int status) {
        TestHttpResponse httpResponse = scenarioContext().consumeLatestHttpResponse();
        if (httpResponse != null) {
            assertEquals(status, httpResponse.statusCode(), "Unexpected HTTP status");
            return;
        }
        then()
            .log().ifValidationFails()  // shows payload on failure
            .statusCode(status);
    }
}
