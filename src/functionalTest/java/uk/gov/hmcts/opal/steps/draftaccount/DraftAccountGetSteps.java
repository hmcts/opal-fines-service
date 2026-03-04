package uk.gov.hmcts.opal.steps.draftaccount;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import static net.serenitybdd.rest.SerenityRest.then;
import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;
import uk.gov.hmcts.opal.utils.DraftAccountUtils;
import net.serenitybdd.core.Serenity;
import static org.junit.jupiter.api.Assertions.fail;
import io.restassured.response.Response;

public class DraftAccountGetSteps extends BaseStepDef {
    @When("I get the draft account {string}")
    public void getDraftAccount(String draftAccountId) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
    }


    @When("I get the draft account trying to provoke an internal server error")
    public void getDraftAccountInternalServerError() {
        SerenityRest
            .given()
            .urlEncodingEnabled(false)
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "/%20");
    }

    @When("I attempt to get a draft account with an invalid token")
    public void getDraftAccountWithInvalidToken() {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + "invalidToken")
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "/1234");
    }

    @When("I attempt to get a draft account with an unsupported content type")
    public void getDraftAccountWithUnsupportedContentType() {
        assertEquals(
            1,
            DraftAccountUtils.getAllDraftAccountIds().size(),
            "There should be only one draft account but found multiple: "
                + DraftAccountUtils.getAllDraftAccountIds()
        );
        String draftAccountId = DraftAccountUtils.getAllDraftAccountIds().getFirst();
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("text/plain")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
    }

    @When("I get the single created draft account and the response contains")
    public void getSingleDraftAccount(DataTable data) {
        assertEquals(
            1,
            DraftAccountUtils.getAllDraftAccountIds().size(),
            "There should be only one draft account but found multiple: "
                + DraftAccountUtils.getAllDraftAccountIds()
        );
        String draftAccountId = DraftAccountUtils.getAllDraftAccountIds().getFirst();
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);

        Map<String, String> expectedData = data.asMap(String.class, String.class);

        for (String key : expectedData.keySet()) {
            String expected = expectedData.get(key);
            String actual = then().extract().body().jsonPath().getString(key);
            assertEquals(expected, actual, "Values are not equal for field '" + key + "'");
        }
    }

    @When("I get the single created draft account")
    public void getTheSingleCreatedDraftAccount() {
        Object idObj = Serenity.sessionVariableCalled("CREATED_DRAFT_ACCOUNT_ID");
        String draftId = idObj == null ? null : String.valueOf(idObj);

        if (draftId == null || draftId.isBlank() || "null".equalsIgnoreCase(draftId)) {
            String msg = "CREATED_DRAFT_ACCOUNT_ID is missing or null BEFORE GET. "
                + "Check create step stored it and that subsequent steps didn't clear it.";
            fail(msg);
        }

        // pick up bearer token if set in session
        Object tokenObj = Serenity.sessionVariableCalled("BEARER_TOKEN");
        String token = tokenObj == null ? null : String.valueOf(tokenObj);

        var given = SerenityRest.given().contentType("application/json");
        if (token != null && !token.isBlank()) {
            given.header("Authorization", "Bearer " + token);
        }

        String base = System.getenv().getOrDefault("OPAL_BASE_URL", "http://localhost:4550");
        Response resp = given.when().get(base + "/draft-accounts/" + draftId);

        Serenity.setSessionVariable("LATEST_RESPONSE").to(resp);
    }

    @When("I get the draft accounts filtering on the Business unit {string} then the response contains")
    public void getDraftAccountsFilteringOnBU(String filter, DataTable data) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "?business_unit=" + filter);

        Map<String, String> expectedData = data.asMap(String.class, String.class);
        String count = then().extract().body().jsonPath().getString("count");
        for (String key : expectedData.keySet()) {
            for (int i = 0; i < Integer.parseInt(count); i++) {
                String apiResponseValue = then().extract().body().jsonPath().getString("summaries[" + i + "]."
                                                                                           + key);
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
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "?status=" + filter);

        Map<String, String> expectedData = data.asMap(String.class, String.class);


        String count = then().extract().body().jsonPath().getString("count");
        for (String key : expectedData.keySet()) {
            for (int i = 0; i < Integer.parseInt(count); i++) {
                String apiResponseValue = then().extract().body().jsonPath().getString("summaries[" + i + "]."
                                                                                           + key);
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
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "?submitted_by=" + filter);

        Map<String, String> expectedData = data.asMap(String.class, String.class);

        String count = then().extract().body().jsonPath().getString("count");
        for (String key : expectedData.keySet()) {
            for (int i = 0; i < Integer.parseInt(count); i++) {
                String apiResponseValue = then().extract().body().jsonPath().getString("summaries[" + i + "]."
                                                                                           + key);
                assertEquals(expectedData.get(key), apiResponseValue, "Values are not equal : ");
            }
        }
    }

    @When("I get the draft accounts filtering on Not Submitted by {string} then the response contains")
    public void getDraftAccountsFilteringOnNotSubmittedBy(String filter, DataTable data) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "?not_submitted_by=" + filter);

        Map<String, String> expectedData = data.asMap(String.class, String.class);

        String count = then().extract().body().jsonPath().getString("count");
        for (String key : expectedData.keySet()) {
            for (int i = 0; i < Integer.parseInt(count); i++) {
                String apiResponseValue = then().extract().body().jsonPath().getString("summaries[" + i + "]."
                                                                                           + key);
                assertTrue(
                    String.valueOf(expectedData.get(key))
                        .equalsIgnoreCase(String.valueOf(apiResponseValue)),
                    "Value " + apiResponseValue
                        + " is not equal to expected (case-insensitive): '"
                        + expectedData.get(key) + "'"
                );

            }
        }
    }

    @When("I get the draft accounts filtering on Submitted by {string} and Not Submitted by {string}")
    public void getDraftAccountsFilterOnSubByAndNotSubBy(String submittedByFilter, String notSubmittedByFilter) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "?submitted_by=" + submittedByFilter + "&not_submitted_by="
                     + notSubmittedByFilter);
    }

    @When("I get the draft accounts filtering on the Status {string} and Submitted by {string} "
        + "then the response contains")
    public void getDraftAccountsFilteringOnStatusesAndSubmittedBy(String statusFilter, String submittedByFilter,
                                                                  DataTable data) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "?status=" + statusFilter + "&submitted_by="
                     + submittedByFilter);

        Map<String, String> expectedData = data.asMap(String.class, String.class);

        String count = then().extract().body().jsonPath().getString("count");
        for (String key : expectedData.keySet()) {
            for (int i = 0; i < Integer.parseInt(count); i++) {
                String apiResponseValue = then().extract().body().jsonPath().getString("summaries[" + i + "]."
                                                                                           + key);
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

    @When("I attempt to get draft accounts with an invalid token")
    public void getDraftAccountsWithAnInvalidToken() {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + "invalidToken")
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI);
    }

    @When("I attempt to get draft accounts with an unsupported content type")
    public void getDraftAccountsWithAnUnsupportedContentType() {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("text/plain")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI);
    }

    @When("I get the draft accounts trying to provoke an internal server error")
    public void getDraftAccountsToProvokeInternalServerError() {
        SerenityRest
            .given()
            .urlEncodingEnabled(false)
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "?submitted_by=me&not_submitted_by=you");
    }

    @Then("I get all the draft accounts and the response contains")
    public void getDraftAccountsAndTheResponseContains(DataTable data) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI);

        Map<String, String> expectedData = data.asMap(String.class, String.class);
        String count = then().extract().body().jsonPath().getString("count");
        for (String key : expectedData.keySet()) {
            for (int i = 0; i < Integer.parseInt(count); i++) {
                String apiResponseValue = then().extract().body().jsonPath().getString("summaries[" + i + "]."
                                                                                           + key);
                assertEquals(expectedData.get(key), apiResponseValue, "Values are not equal : ");
            }
        }
    }


    @When("I get no draft accounts related to business unit {string} then the response contains")
    public void getNoDraftAccountsRelatedToBusinessUnitThenTheResponseContains(String filter, DataTable data) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "?business_unit=" + filter);

        Map<String, String> expectedData = data.asMap(String.class, String.class);


        String count = then().extract().body().jsonPath().getString("count");
        if (Integer.parseInt(count) == 0) {

            for (String key : expectedData.keySet()) {
                for (int i = 0; i < Integer.parseInt(count); i++) {
                    String apiResponseValue = then().extract().body().jsonPath().getString("summaries[" + i + "]."
                                                                                               + key);
                    assertEquals(expectedData.get(key), apiResponseValue, "Values are not equal : ");
                }
            }
        }
    }

    @Then("I get the draft accounts filtering on the Business unit {string}")
    public void getTheDraftAccountsFilteringOnTheBusinessUnit(String filter) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "?business_unit=" + filter);

    }

    @When("I get the draft accounts")
    public void getDraftAccounts() {
        // pick up bearer token if set in session, otherwise fall back to getToken()
        Object tokenObj = Serenity.sessionVariableCalled("BEARER_TOKEN");
        String token = tokenObj == null ? getToken() : String.valueOf(tokenObj);

        var given = SerenityRest.given().contentType("application/json").accept("*/*");

        if (token != null && !token.isBlank()) {
            given.header("Authorization", "Bearer " + token);
        }

        // support optional custom header set by tests (e.g. x-user-ip)
        Object hdrName = Serenity.sessionVariableCalled("CUSTOM_HEADER_NAME");
        Object hdrValue = Serenity.sessionVariableCalled("CUSTOM_HEADER_VALUE");
        if (hdrName != null && hdrValue != null) {
            given.header(String.valueOf(hdrName), String.valueOf(hdrValue));
            // clear after use to avoid leaking across scenarios
            Serenity.setSessionVariable("CUSTOM_HEADER_NAME").to(null);
            Serenity.setSessionVariable("CUSTOM_HEADER_VALUE").to(null);
        }

        Response resp = given.when().get(getTestUrl() + DRAFT_ACCOUNTS_URI);
        // store latest response for later assertions / steps
        Serenity.setSessionVariable("LATEST_RESPONSE").to(resp);
    }
}
