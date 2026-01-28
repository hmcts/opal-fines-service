package uk.gov.hmcts.opal.steps.draftaccount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.utils.DraftAccountUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsBlankString.blankOrNullString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class DraftAccountPostSteps extends BaseStepDef {
    private static final Logger log = LoggerFactory.getLogger(DraftAccountPostSteps.class);

    @When("I create a draft account with the following details")
    public void postDraftAccount(DataTable accountData) throws JSONException, IOException {
        Map<String, String> dataToPost = accountData.asMap(String.class, String.class);
        JSONObject postBody = new JSONObject();

        addLongToJsonObject(postBody, dataToPost, "business_unit_id");
        addAllToJsonObject(postBody, dataToPost, "submitted_by", "submitted_by_name", "account_type");
        addToJsonObjectOrNull(postBody, dataToPost, "account_status");

        String accountFilePath = "build/resources/functionalTest/features/opalMode/manualAccountCreation/"
            + dataToPost.get(
            "account");
        String account = new String(Files.readAllBytes(Paths.get(accountFilePath)));
        JSONObject accountObject = new JSONObject(account);

        accountObject.put("originator_id", accountObject.getLong("originator_id"));
        accountObject.put("enforcement_court_id", accountObject.getLong("enforcement_court_id"));

        JSONArray offences = accountObject.getJSONArray("offences");
        for (int i = 0; i < offences.length(); i++) {
            JSONObject offence = offences.getJSONObject(i);
            offence.put("offence_id", offence.getLong("offence_id"));
        }

        String timelineFilePath = "build/resources/functionalTest/features/opalMode/manualAccountCreation"
            + "/draftAccounts/timelineJson/default.json";
        String timeline = new String(Files.readAllBytes(Paths.get(timelineFilePath)));
        JSONArray timelineArray = new JSONArray(timeline);

        postBody.put("account", accountObject);
        postBody.put("timeline_data", timelineArray);

        SerenityRest
            .given().log().all()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .body(postBody.toString())
            .when()
            .post(getTestUrl() + DRAFT_ACCOUNTS_URI);
    }

    @Then("I store the created draft account ID")
    public void storeCreatedDraftAccountId() {
        var resp = SerenityRest.lastResponse();

        log.info("CREATE status={}", resp.getStatusCode());
        log.info("CREATE headers={}", resp.getHeaders());
        log.info("CREATE body={}", resp.asString());

        String id = null;

        try {
            Object idObj = resp.jsonPath().get("draft_account_id");
            if (idObj != null) {
                id = String.valueOf(idObj);
            }
        } catch (Exception ignored) {
            // No JSON or no field; fall through to Location
        }

        // 2) Fallback to Location header’s last path segment
        if (id == null || id.isBlank()) {
            String location = resp.getHeader("Location");
            if (location != null && !location.isBlank()) {
                id = location.substring(location.lastIndexOf('/') + 1);
            }
        }

        // 3) Fail fast if still missing to avoid /null deletes later
        assertThat(
            "Create must include draft_account_id in body or a Location header with the ID",
            id,
            not(blankOrNullString())
        );

        DraftAccountUtils.addDraftAccountId(id);
        log.info("Stored draft account id={}", id);
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
            String expected = expectedData.get(key);
            String actual = then().extract().body().jsonPath().getString(key);
            assertEquals(expected, actual, "Values are not equal for field '" + key + "'");
        }
    }

    @Then("The draft account response returns {int}")
    public void draftAccountResponse(int statusCode) {
        then().assertThat()
            .statusCode(statusCode);
    }

    @When("I attempt to create a draft account with an invalid token using created by ID {string}")
    public void postDraftAccountWithInvalidToken(String CreatedBy) throws JSONException {
        JSONObject postBody = new JSONObject();

        postBody.put("business_unit_id", "77");
        postBody.put("account", "{\"account_create_request\":{\"defendant\":{},\"account\":{}}}");
        postBody.put("account_type", "Fine");
        postBody.put("account_status", "");
        postBody.put("submitted_by", CreatedBy);
        postBody.put("timeline_data", JSONObject.NULL);

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
