package uk.gov.hmcts.opal.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
     * Requests the results endpoint with one filtering parameter.
     *
     * @param filterName filtering query-parameter name.
     * @param filterValue filtering query-parameter value.
     */
    @When("I request results using filter {string} with value {string}")
    public void getResultsWithFilter(String filterName, String filterValue) {
        authorisedJsonRequest()
            .param(filterName, filterValue)
            .when()
            .get(getTestUrl() + RESULTS_URI);
    }

    /**
     * Requests the results endpoint for the supplied result identifiers and one filtering
     * parameter.
     *
     * @param resultIds comma-separated result identifiers to request.
     * @param filterName filtering query-parameter name.
     * @param filterValue filtering query-parameter value.
     */
    @When("I request results for identifiers {string} using filter {string} with value {string}")
    public void getResultsWithIdentifiersAndFilter(String resultIds, String filterName, String filterValue) {
        authorisedJsonRequest()
            .param("result_ids", resultIds)
            .param(filterName, filterValue)
            .when()
            .get(getTestUrl() + RESULTS_URI);
    }

    /**
     * Requests the single-result endpoint for the supplied result identifier.
     *
     * @param resultId result identifier to request.
     */
    @When("I request result with identifier {string}")
    public void getResultById(String resultId) {
        authorisedJsonRequest()
            .when()
            .get(getTestUrl() + RESULTS_URI + "/" + resultId);
    }

    /**
     * Requests the single-result endpoint for the supplied result identifier with Welsh result
     * parameters requested.
     *
     * @param resultId result identifier to request.
     */
    @When("I request result with identifier {string} including Welsh parameters")
    public void getResultByIdIncludingWelshParameters(String resultId) {
        authorisedJsonRequest()
            .param("include_welsh", true)
            .when()
            .get(getTestUrl() + RESULTS_URI + "/" + resultId);
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
     * Asserts that the single-result response contains the expected field values.
     *
     * @param data Cucumber table containing the expected values for the assertion.
     */
    @Then("the result response contains")
    public void resultResponseContains(DataTable data) {
        Map<String, String> expected = data.asMap(String.class, String.class);
        then().assertThat().statusCode(200);

        for (Map.Entry<String, String> entry : expected.entrySet()) {
            String actual = then().extract().body().jsonPath().getString(entry.getKey());
            assertEquals(entry.getValue(), actual, "Values are not equal");
        }
    }

    /**
     * Asserts that result_parameters contains a contiguous sequence of parameter entries.
     *
     * @param data Cucumber table containing expected parameter field values.
     */
    @Then("the result parameters contain the following entries in order")
    public void resultParametersContainEntriesInOrder(DataTable data) {
        List<Map<String, String>> expectedParameters = data.asMaps(String.class, String.class);
        then().assertThat().statusCode(200);

        String resultParameters = then().extract().body().jsonPath().getString("result_parameters");
        List<Map<String, Object>> actualParameters = JsonPath.from(resultParameters).getList("$");

        assertTrue(
            containsExpectedSequence(actualParameters, expectedParameters),
            "Expected result parameter sequence was not found"
        );
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

    private boolean containsExpectedSequence(
        List<Map<String, Object>> actualParameters,
        List<Map<String, String>> expectedParameters) {

        for (int startIndex = 0; startIndex <= actualParameters.size() - expectedParameters.size(); startIndex++) {
            if (sequenceMatches(actualParameters, expectedParameters, startIndex)) {
                return true;
            }
        }
        return false;
    }

    private boolean sequenceMatches(
        List<Map<String, Object>> actualParameters,
        List<Map<String, String>> expectedParameters,
        int startIndex) {

        for (int offset = 0; offset < expectedParameters.size(); offset++) {
            if (!parameterMatches(actualParameters.get(startIndex + offset), expectedParameters.get(offset))) {
                return false;
            }
        }
        return true;
    }

    private boolean parameterMatches(Map<String, Object> actualParameter, Map<String, String> expectedParameter) {
        for (Map.Entry<String, String> entry : expectedParameter.entrySet()) {
            if (dataExists(entry.getValue())
                && !entry.getValue().equals(String.valueOf(actualParameter.get(entry.getKey())))) {
                return false;
            }
        }
        return true;
    }

}
