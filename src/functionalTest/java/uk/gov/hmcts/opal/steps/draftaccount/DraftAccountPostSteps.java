package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.utils.DraftAccountUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class DraftAccountPostSteps extends BaseStepDef {
    @When("I create a draft account with the following details")
    public void postDraftAccount(DataTable accountData) throws JSONException, IOException {
        Map<String, String> dataToPost = accountData.asMap(String.class, String.class);
        JSONObject postBody = new JSONObject();

        postBody.put(
            "business_unit_id",
            dataToPost.get("business_unit_id") != null ? dataToPost.get("business_unit_id") : ""
        );
        postBody.put("submitted_by", dataToPost.get("submitted_by") != null ? dataToPost.get("submitted_by") : "");
        postBody.put("submitted_by_name", dataToPost.get("submitted_by_name") != null
            ? dataToPost.get("submitted_by_name") : "");
        postBody.put("account_type", dataToPost.get("account_type") != null ? dataToPost.get("account_type") : "");
        postBody.put(
            "account_status",
            dataToPost.get("account_status") != null ? dataToPost.get("account_status") : ""
        );


        String accountFilePath = "build/resources/functionalTest/features/opalMode/manualAccountCreation/"
            + dataToPost.get(
            "account");
        String account = new String(Files.readAllBytes(Paths.get(accountFilePath)));
        JSONObject accountObject = new JSONObject(account);


        String timelineFilePath = "build/resources/functionalTest/features/opalMode/manualAccountCreation"
            + "/draftAccounts/timelineJson/default.json";
        String timeline = new String(Files.readAllBytes(Paths.get(timelineFilePath)));
        JSONArray timelineArray = new JSONArray(timeline);

        postBody.put("account", accountObject);
        postBody.put("timeline_data", timelineArray);

        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .body(postBody.toString())
            .when()
            .post(getTestUrl() + DRAFT_ACCOUNTS_URI);
    }

    @Then("I store the created draft account ID")
    public void storeDraftAccountId() {
        String draftAccountId = then().extract().body().jsonPath().getString("draft_account_id");
        DraftAccountUtils.addDraftAccountId(draftAccountId);
    }

    @Then("I store the created draft account created_at time")
    public void storeDraftAccountCreatedTime() {
        String createdAt = then().extract().body().jsonPath().getString("created_at");
        DraftAccountUtils.addDraftAccountCreatedAtTime(createdAt);
    }

    @Then("I store the created draft account initial account_status_date")
    public void storeDraftAccountInitialAccountStatusDate() {
        String initialAccountStatusDate = then().extract().body().jsonPath().getString("account_status_date");
        DraftAccountUtils.addInitialAccountStatusDate(initialAccountStatusDate);
    }

    @Then("The draft account response contains the following data")
    public void draftAccountResponseContains(DataTable data) {
        Map<String, String> expectedData = data.asMap(String.class, String.class);

        for (String key : expectedData.keySet()) {
            String apiResponseValue = then().extract().body().jsonPath().getString(key);
            assertEquals(expectedData.get(key), apiResponseValue, "Values are not equal : ");
        }
    }

    @Then("The draft account response returns {int}")
    public void draftAccountResponse(int statusCode) {
        then().assertThat()
            .statusCode(statusCode);
    }

    @When("I attempt to create a draft account with an invalid token")
    public void postDraftAccountWithInvalidToken() throws JSONException {
        JSONObject postBody = new JSONObject();

        postBody.put("business_unit_id", "77");
        postBody.put("account", "{\"account_create_request\":{\"defendant\":{},\"account\":{}}}");
        postBody.put("account_type", "Fine");
        postBody.put("account_status", "");
        postBody.put("submitted_by", "BUUID");
        postBody.put("timeline_data", "");

        SerenityRest
            .given()
            .header("Authorization", "Bearer " + "invalidToken")
            .accept("*/*")
            .contentType("application/json")
            .body(postBody.toString())
            .when()
            .post(getTestUrl() + DRAFT_ACCOUNTS_URI);

    }

    @When("I attempt to create a draft account with an unsupported content type")
    public void postDraftAccountWithUnsupportedContentType() throws JSONException, IOException {
        JSONObject postBody = new JSONObject();
        String accountFilePath = "build/resources/functionalTest/features/opalMode/manualAccountCreation/"
            + "draftAccounts/accountJson/account.json";
        String account = new String(Files.readAllBytes(Paths.get(accountFilePath)));
        JSONObject accountObject = new JSONObject(account);
        postBody.put("business_unit_id", 77);
        postBody.put("account", accountObject);
        postBody.put("account_type", "Fine");
        postBody.put("account_status", "");
        postBody.put("submitted_by", "BUUID");
        postBody.put("timeline_data", new JSONObject());

        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("text/plain")
            .contentType("application/json")
            .body(postBody.toString())
            .when()
            .post(getTestUrl() + DRAFT_ACCOUNTS_URI);

    }

    @When("I attempt to create a draft account with an unsupported media type")
    public void postDraftAccountWithUnsupportedMediaType() throws JSONException, IOException {
        JSONObject postBody = new JSONObject();
        String accountFilePath = "build/resources/functionalTest/features/opalMode/manualAccountCreation/"
            + "draftAccounts/accountJson/account.json";
        String account = new String(Files.readAllBytes(Paths.get(accountFilePath)));
        JSONObject accountObject = new JSONObject(account);

        postBody.put("business_unit_id", 77);
        postBody.put("account", accountObject);
        postBody.put("account_type", "Fine");
        postBody.put("account_status", "");
        postBody.put("submitted_by", "BUUID");
        postBody.put("timeline_data", new JSONObject());

        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/xml")
            .body(postBody.toString())
            .when()
            .post(getTestUrl() + DRAFT_ACCOUNTS_URI);

    }
}
