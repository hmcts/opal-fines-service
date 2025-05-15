package uk.gov.hmcts.opal.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class DefendantSearchApiStepDef extends BaseStepDef {

    @When("I make a call to the defendant search API using the parameters")
    public void postToDefendantSearchAPI(DataTable searchCriteria) throws JSONException {
        Map<String, String> dataToPost = searchCriteria.asMap(String.class, String.class);

        JSONObject requestBody = addToNewJsonObject(dataToPost, "forename", "surname", "initials", "address_line");
        JSONObject dateOfBirth = addToNewJsonObject(dataToPost, "day_of_month", "month_of_year", "year");
        requestBody.put("date_of_birth", dateOfBirth);

        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .body(requestBody.toString())
            .when()
            .post(getTestUrl() + "/defendant-accounts/search");

    }

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

    @Then("there are no results returned")
    public void thereAreNoResultsReturned() {
        then().assertThat()
            .statusCode(200)
            .body("total_count", Matchers.equalTo(0));
    }

    @Then("the returned results match")
    public void theReturnedResultsMatch(DataTable expectedData) {
        Map<String, String> expectedResult = expectedData.asMap(String.class, String.class);
        then().assertThat()
            .statusCode(200);

        int totalCount = then().extract().jsonPath().getInt("total_count");
        System.out.println("total count is : " + totalCount);

        int index = 0;

        while (index < totalCount) {
            if (expectedResult.get("name") != null) {
                then().assertThat()
                    .body("search_results.name[" + index + "]", Matchers.containsString(expectedResult.get("name")));
            }
            if (expectedResult.get("date_of_birth") != null) {
                then().assertThat()
                    .body(
                        "search_results.date_of_birth[" + index + "]",
                        Matchers.containsString(expectedResult.get("date_of_birth"))
                );
            }
            if (expectedResult.get("address_line_1") != null) {
                then().assertThat()
                    .body(
                        "search_results.address_line_1[" + index + "]",
                        Matchers.containsString(expectedResult.get("addressLine1"))
                );
            }
            index++;
        }
    }
}
