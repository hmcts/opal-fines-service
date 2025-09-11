package uk.gov.hmcts.opal.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.opal.config.Constants.RESULTS_URI;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class GetResultsStepDef extends BaseStepDef {
    @When("I make a request to get the results {string}")
    public void getResults(String resultIds) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .param("result_ids", resultIds)
            .when()
            .get(getTestUrl() + RESULTS_URI);
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
}

