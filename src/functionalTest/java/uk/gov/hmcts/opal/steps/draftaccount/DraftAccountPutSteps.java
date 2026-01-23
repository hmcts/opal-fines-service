package uk.gov.hmcts.opal.steps.draftaccount;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.utils.DraftAccountUtils;
import uk.gov.hmcts.opal.utils.RequestSupport;

public class DraftAccountPutSteps extends BaseStepDef {

    @When("I update the draft account that was just created with the following details")
    public void updateCreatedDraftAccount(DataTable data) throws JSONException, IOException {
        Map<String, String> dataToPost = data.asMap(String.class, String.class);

        assertEquals(
            1,
            DraftAccountUtils.getAllDraftAccountIds().size(),
            "There should be only one draft account but found multiple: "
                + DraftAccountUtils.getAllDraftAccountIds()
        );

        JSONObject postBody = new JSONObject();

        addIntToJsonObject(postBody, dataToPost, "business_unit_id");
        addToJsonObjectOrNull(postBody, dataToPost, "submitted_by");
        addToJsonObject(postBody, dataToPost, "submitted_by_name");
        addToJsonObject(postBody, dataToPost, "account_type");
        addToJsonObjectOrNull(postBody, dataToPost, "account_status");

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

        String draftAccountId = DraftAccountUtils.getAllDraftAccountIds().get(0);

        RequestSupport.responseProcessor(
            SerenityRest
                .given()
                .spec(RequestSupport.putRequestSpec(DRAFT_ACCOUNTS_URI + "/" + draftAccountId, postBody.toString())
                    .build())
                .header(createQuotedLongHeader("If-Match", dataToPost))
                .when()
                .put()
                .then()
        );
    }

    @Then("I see the created at time hasn't changed")
    public void checkCreatedAtTime() {
        Instant apiCreatedAt = Instant.parse(then().extract().body().jsonPath().getString("created_at"));

        Instant createdTime = Instant.parse(DraftAccountUtils.getDraftAccountCreatedAtTime());

        String createdAtTime = String.valueOf(createdTime.truncatedTo(java.time.temporal.ChronoUnit.MILLIS));
        String createdAt = String.valueOf(apiCreatedAt.truncatedTo(java.time.temporal.ChronoUnit.MILLIS));

        assertEquals(createdAtTime, createdAt, "Created at time has changed");

        Serenity.recordReportData().withTitle("Times").andContents(
            "Created at time: " + createdAtTime + "\nResponse created at time: " + createdAt);
    }

    @Then("I see the account status date is now after the initial account status date")
    public void checkAccountStatusDate() {
        Instant apiAccountStatusDate = Instant.parse(then().extract()
            .body().jsonPath().getString("account_status_date"));
        Instant initialAccountStatusDate = Instant.parse(DraftAccountUtils.getInitialAccountStatusDate());

        String accountStatusDate = String.valueOf(initialAccountStatusDate
            .truncatedTo(java.time.temporal.ChronoUnit.MILLIS));
        String accountStatus = String.valueOf(apiAccountStatusDate
            .truncatedTo(java.time.temporal.ChronoUnit.MILLIS));

        Serenity.recordReportData().withTitle("Times").andContents(
            "Initial account status date: " + accountStatusDate
                + "\nResponse account status date: " + accountStatus);

        assertTrue(
            apiAccountStatusDate.isAfter(initialAccountStatusDate),
            "Account status date is not after the initial account status date"
        );
    }

    @Then("I see the account status date hasn't changed")
    public void checkAccountStatusDateNotChanged() {
        Instant apiAccountStatusDate = Instant.parse(then().extract()
            .body().jsonPath().getString("account_status_date"));
        Instant initialAccountStatusDate = Instant.parse(DraftAccountUtils.getInitialAccountStatusDate());

        String accountStatusDate = String.valueOf(initialAccountStatusDate
            .truncatedTo(java.time.temporal.ChronoUnit.MILLIS));
        String accountStatus = String.valueOf(apiAccountStatusDate
            .truncatedTo(java.time.temporal.ChronoUnit.MILLIS));

        Serenity.recordReportData().withTitle("Times").andContents(
            "Initial account status date: " + accountStatusDate
                + "\nResponse account status date: " + accountStatus);

        assertEquals(
            accountStatusDate,
            accountStatus,
            "Account status date has changed"
        );
    }

    @When("I attempt to put a draft account with an invalid token")
    public void putADraftAccountWithAnInvalidToken() {
        RequestSupport.responseProcessor(
            SerenityRest
                .given()
                .spec(RequestSupport.putRequestSpec(DRAFT_ACCOUNTS_URI, "{}")
                    .build())
                .header("Authorization", "Bearer invalidToken")
                .when()
                .put()
                .then()
        );
    }

    @When("I attempt to put a draft account with an invalid request payload")
    public void putADraftAccountWithAnInvalidRequestPayload(DataTable data) throws JSONException, IOException {
        Map<String, String> dataToPost = data.asMap(String.class, String.class);

        assertEquals(
            1,
            DraftAccountUtils.getAllDraftAccountIds().size(),
            "There should be only one draft account but found multiple: "
                + DraftAccountUtils.getAllDraftAccountIds()
        );

        JSONObject postBody = new JSONObject();

        addLongToJsonObject(postBody, dataToPost, "business_unit_id");
        addToJsonObjectOrNull(postBody, dataToPost, "submitted_by");
        addToJsonObject(postBody, dataToPost, "submitted_by_name");
        addToJsonObject(postBody, dataToPost, "account_type");
        addToJsonObjectOrNull(postBody, dataToPost, "account_status");

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

        String draftAccountId = DraftAccountUtils.getAllDraftAccountIds().get(0);

        RequestSupport.responseProcessor(
            SerenityRest
                .given()
                .spec(RequestSupport.putRequestSpec(DRAFT_ACCOUNTS_URI + "/" + draftAccountId, postBody.toString())
                    .build())
                .when()
                .put()
                .then()
        );
    }

    @When("I attempt to put a draft account with resource not found")
    public void putADraftAccountWithResourceNotFound(DataTable data) throws IOException, JSONException {
        Map<String, String> dataToPost = data.asMap(String.class, String.class);
        assertEquals(
            1,
            DraftAccountUtils.getAllDraftAccountIds().size(),
            "There should be only one draft account but found multiple: "
                + DraftAccountUtils.getAllDraftAccountIds()
        );

        JSONObject postBody = new JSONObject();

        addLongToJsonObject(postBody, dataToPost, "business_unit_id");
        addToJsonObjectOrNull(postBody, dataToPost, "submitted_by");
        addToJsonObject(postBody, dataToPost, "submitted_by_name");
        addToJsonObject(postBody, dataToPost, "account_type");
        addToJsonObjectOrNull(postBody, dataToPost, "account_status");

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

        RequestSupport.responseProcessor(
            SerenityRest
                .given()
                .spec(RequestSupport.putRequestSpec(DRAFT_ACCOUNTS_URI + "/999999", postBody.toString())
                    .build())
                .header(createQuotedLongHeader("If-Match", dataToPost))
                .when()
                .put()
                .then()
        );
    }

    @When("I attempt to put a draft account with unsupported content type for response")
    public void putADraftAccountWithUnsupportedContentType(DataTable data) throws IOException, JSONException {
        Map<String, String> dataToPost = data.asMap(String.class, String.class);

        assertEquals(
            1,
            DraftAccountUtils.getAllDraftAccountIds().size(),
            "There should be only one draft account but found multiple: "
                + DraftAccountUtils.getAllDraftAccountIds()
        );

        JSONObject postBody = new JSONObject();

        addLongToJsonObject(postBody, dataToPost, "business_unit_id");
        addToJsonObjectOrNull(postBody, dataToPost, "submitted_by");
        addToJsonObject(postBody, dataToPost, "submitted_by_name");
        addToJsonObject(postBody, dataToPost, "account_type");
        addToJsonObjectOrNull(postBody, dataToPost, "account_status");

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
        String draftAccountId = DraftAccountUtils.getAllDraftAccountIds().get(0);
        RequestSupport.responseProcessor(
            SerenityRest
                .given()
                .spec(RequestSupport.putRequestSpec(DRAFT_ACCOUNTS_URI + "/" + draftAccountId, postBody.toString())
                    .build())
                .accept("text/html")
                .when()
                .put()
                .then()
        );
    }

    @When("I attempt to put a draft account with unsupported media type for request")
    public void putADraftAccountWithUnsupportedMediaTypeForRequest(DataTable data) throws IOException, JSONException {
        Map<String, String> dataToPost = data.asMap(String.class, String.class);

        assertEquals(
            1,
            DraftAccountUtils.getAllDraftAccountIds().size(),
            "There should be only one draft account but found multiple: "
                + DraftAccountUtils.getAllDraftAccountIds()
        );

        JSONObject postBody = new JSONObject();

        addLongToJsonObject(postBody, dataToPost, "business_unit_id");
        addToJsonObjectOrNull(postBody, dataToPost, "submitted_by");
        addToJsonObject(postBody, dataToPost, "submitted_by_name");
        addToJsonObject(postBody, dataToPost, "account_type");
        addToJsonObjectOrNull(postBody, dataToPost, "account_status");

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

        String draftAccountId = DraftAccountUtils.getAllDraftAccountIds().get(0);
        RequestSupport.responseProcessor(
            SerenityRest
                .given()
                .spec(RequestSupport.putRequestSpec(DRAFT_ACCOUNTS_URI + "/" + draftAccountId, postBody.toString())
                    .build())
                .accept("text/plain")
                .when()
                .put()
                .then()
        );
    }

    @When("I put the draft account trying to provoke an internal server error")
    public void putDraftAccountToProvokeAnInternalServerError() {
        RequestSupport.responseProcessor(
            SerenityRest
                .given()
                .spec(RequestSupport.putRequestSpec(DRAFT_ACCOUNTS_URI + "?business_unit=%20", "{}")
                    .build())
                .when()
                .put()
                .then()
        );
    }
}
