package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.utils.DraftAccountUtils;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNT_URI;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class DraftAccountGetSteps extends BaseStepDef {
    @When("I get the draft account {string}")
    public void getDraftAccount(String draftAccountId) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNT_URI + "/" + draftAccountId);
    }

    @When("I get the single created draft account and the response contains")
    public void getSingleDraftAccount(DataTable data) {
        assertEquals(
            1,
            DraftAccountUtils.getAllDraftAccountIds().size(),
            "There should be only one draft account but found multiple: " + DraftAccountUtils.getAllDraftAccountIds()
        );
        String draftAccountId = DraftAccountUtils.getAllDraftAccountIds().getFirst();
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNT_URI + "/" + draftAccountId);

        Map<String, String> expectedData = data.asMap(String.class, String.class);

        for (String key : expectedData.keySet()) {
            String apiResponseValue = then().extract().body().jsonPath().getString(key);
            assertEquals(expectedData.get(key), apiResponseValue, "Values are not equal : ");
        }
    }

    @When("I get the draft accounts filtering on the Business unit {string} then the response contains")
    public void getDraftAccountsFilteringOnBU(String filter, DataTable data) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNT_URI + "?business_unit=" + filter);

        Map<String, String> expectedData = data.asMap(String.class, String.class);
        String count = then().extract().body().jsonPath().getString("count");
        for (String key : expectedData.keySet()) {
            for(int i = 0; i < Integer.parseInt(count); i++) {
                String apiResponseValue = then().extract().body().jsonPath().getString("summaries[" + i + "]." + key);
                assertEquals(expectedData.get(key), apiResponseValue, "Values are not equal : ");
            }
        }
    }

    @When("I get the draft accounts filtering on the Status {string} then the response contains")
    public void getDraftAccountsFilteringOnStatuses(String filter, DataTable data) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNT_URI + "?status=" + filter);

        Map<String, String> expectedData = data.asMap(String.class, String.class);


        String count = then().extract().body().jsonPath().getString("count");
        for (String key : expectedData.keySet()) {
            for(int i = 0; i < Integer.parseInt(count); i++) {
                String apiResponseValue = then().extract().body().jsonPath().getString("summaries[" + i + "]." + key);
                assertEquals(expectedData.get(key), apiResponseValue, "Values are not equal : ");
            }
        }
    }

    @When("I get the draft accounts filtering on Submitted by {string} then the response contains")
    public void getDraftAccountsFilteringOnSubmittedBy(String filter, DataTable data) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNT_URI + "?submitted_by=" + filter);

        Map<String, String> expectedData = data.asMap(String.class, String.class);

        String count = then().extract().body().jsonPath().getString("count");
        for (String key : expectedData.keySet()) {
            for(int i = 0; i < Integer.parseInt(count); i++) {
                String apiResponseValue = then().extract().body().jsonPath().getString("summaries[" + i + "]." + key);
                assertEquals(expectedData.get(key), apiResponseValue, "Values are not equal : ");
            }
        }
    }
    @When("I get the draft accounts filtering on the Status {string} and Submitted by {string} then the response contains")
    public void getDraftAccountsFilteringOnStatusesAndSubmittedBy(String statusFilter, String submittedByFilter, DataTable data) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNT_URI + "?status=" + statusFilter + "&submitted_by=" + submittedByFilter);

        Map<String, String> expectedData = data.asMap(String.class, String.class);

        String count = then().extract().body().jsonPath().getString("count");
        for (String key : expectedData.keySet()) {
            for(int i = 0; i < Integer.parseInt(count); i++) {
                String apiResponseValue = then().extract().body().jsonPath().getString("summaries[" + i + "]." + key);
                assertEquals(expectedData.get(key), apiResponseValue, "Values are not equal : ");
            }
        }
    }

    @And("The draft account filtered response does not contain accounts in the {string} business unit")
    public void draftAccountFilteredResponseDoesNotContainAccountsInBusinessUnit(String filter) {
        String count = then().extract().body().jsonPath().getString("count");
        for (int i = 0; i < Integer.parseInt(count); i++) {
            String buID = then().extract().body().jsonPath().getString("summaries[" + i + "].business_unit_id");
            assertNotEquals(filter, buID, "should not contain " + filter);
        }
    }
    @And("The draft account filtered response does not contain accounts with status {string}")
    public void draftAccountFilteredResponseDoesNotContainAccountsInStatus(String filter) {
        String count = then().extract().body().jsonPath().getString("count");
        for (int i = 0; i < Integer.parseInt(count); i++) {
            String status = then().extract().body().jsonPath().getString("summaries[" + i + "].account_status");
            assertNotEquals(filter, status, "should not contain " + filter);
        }
    }
    @And("The draft account filtered response does not contain accounts submitted by {string}")
    public void draftAccountFilteredResponseDoesNotContainAccountsSubmittedBy(String filter) {
        String count = then().extract().body().jsonPath().getString("count");
        for (int i = 0; i < Integer.parseInt(count); i++) {
            String submittedBy = then().extract().body().jsonPath().getString("summaries[" + i + "].submitted_by");
            assertNotEquals(filter, submittedBy, "should not contain " + filter);
        }
    }
}
