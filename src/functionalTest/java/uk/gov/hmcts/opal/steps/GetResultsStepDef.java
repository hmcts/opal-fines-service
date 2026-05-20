package uk.gov.hmcts.opal.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.specification.RequestSpecification;

import java.util.LinkedHashMap;
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
    @When("I request results for identifiers {string}")
    public void getResults(String resultIds) {
        performResultsRequest(resultIds, Map.of());
    }

    /**
     * Requests the results endpoint for the supplied result identifiers with the provided
     * feature-flagged filter parameters.
     *
     * @param resultIds comma-separated result identifiers to request.
     * @param data Cucumber table containing query-parameter names and values to send.
     */
    @When("I request results for identifiers {string} with the following filters")
    public void getResultsWithFilters(String resultIds, DataTable data) {
        performResultsRequest(resultIds, new LinkedHashMap<>(data.asMap(String.class, String.class)));
    }

    /**
     * Asserts that the results response contains the expected number of records.
     *
     * @param count expected number of matching records.
     */
    @Then("{int} results are returned")
    public void resultsResponseContainsCount(int count) {
        then().assertThat()
            .statusCode(200).body("count", equalTo(count));
    }

    /**
     * Asserts that the results response contains the following result.
     *
     * @param data Cucumber table containing the expected values for the assertion.
     */
    @Then("the returned results include the following result")
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
     * Executes the shared GET /results request using the supplied identifiers and optional
     * query parameters.
     *
     * @param resultIds comma-separated result identifiers to request.
     * @param queryParams additional query parameters to include on the request.
     */
    private void performResultsRequest(String resultIds, Map<String, String> queryParams) {
        RequestSpecification request = authorisedJsonRequest().param("result_ids", resultIds);

        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (dataExists(entry.getValue())) {
                request = request.param(entry.getKey(), entry.getValue());
            }
        }

        request
            .when()
            .get(getTestUrl() + RESULTS_URI);
    }

}
