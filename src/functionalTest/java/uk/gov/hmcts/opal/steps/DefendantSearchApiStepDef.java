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
import static uk.gov.hmcts.opal.steps.BearerTokenStefDef.getToken;

public class DefendantSearchApiStepDef extends BaseStepDef {

    @When("I make a call to the defendant search API using the parameters")
    public void postToDefendantSearchAPI(DataTable searchCriteria) throws JSONException {
        Map<String, String> dataToPost = searchCriteria.asMap(String.class, String.class);

        JSONObject requestBody = new JSONObject();

        requestBody.put("forename", dataToPost.get("forename") != null ? dataToPost.get("forename") : "");
        requestBody.put("surname", dataToPost.get("surname") != null ? dataToPost.get("surname") : "");
        requestBody.put("initials", dataToPost.get("initials") != null ? dataToPost.get("initials") : "");

        JSONObject dateOfBirth = new JSONObject();
        dateOfBirth.put("dayOfMonth", dataToPost.get("dayOfMonth") != null ? dataToPost.get("dayOfMonth") : "");
        dateOfBirth.put("monthOfYear", dataToPost.get("monthOfYear") != null ? dataToPost.get("monthOfYear") : "");
        dateOfBirth.put("year", dataToPost.get("year") != null ? dataToPost.get("year") : "");
        requestBody.put("dateOfBirth", dateOfBirth);

        requestBody.put(
            "addressLineOne",
            dataToPost.get("addressLineOne") != null ? dataToPost.get("addressLineOne") : ""
        );

        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .body(requestBody.toString())
            .when()
            .post(getTestUrl() + "/api/defendant-account/search");

    }

    @Then("there is one result returned matching")
    public void thereIsOneResultReturnedMatching(DataTable expectedData) {
        Map<String, String> expectedResult = expectedData.asMap(String.class, String.class);

        then().assertThat()
            .statusCode(200)
            .body("totalCount", Matchers.equalTo(1))
            .body("searchResults.name[0]", Matchers.equalTo(expectedResult.get("name")))
            .body("searchResults.dateOfBirth[0]", Matchers.equalTo(expectedResult.get("dateOfBirth")))
            .body("searchResults.addressLine1[0]", Matchers.equalTo(expectedResult.get("addressLine1")));
    }

    @Then("there are no results returned")
    public void thereAreNoResultsReturned() {
        then().assertThat()
            .statusCode(200)
            .body("totalCount", Matchers.equalTo(0));
    }

    @Then("the returned results match")
    public void theReturnedResultsMatch(DataTable expectedData) {
        Map<String, String> expectedResult = expectedData.asMap(String.class, String.class);
        then().assertThat()
            .statusCode(200);

        int totalCount = then().extract().jsonPath().getInt("totalCount");
        System.out.println("total count is : " + totalCount);

        int index = 0;

        while (index < totalCount) {
            if (expectedResult.get("name") != null) {
                then().assertThat()
                    .body("searchResults.name[" + index + "]", Matchers.containsString(expectedResult.get("name")));
            }
            if (expectedResult.get("dateOfBirth") != null) {
                then().assertThat()
                    .body(
                        "searchResults.dateOfBirth[" + index + "]",
                        Matchers.containsString(expectedResult.get("dateOfBirth"))
                );
            }
            if (expectedResult.get("addressLine1") != null) {
                then().assertThat()
                    .body(
                        "searchResults.addressLine1[" + index + "]",
                        Matchers.containsString(expectedResult.get("addressLine1"))
                );
            }
            index++;
        }
    }
}
