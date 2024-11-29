package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.utils.DraftAccountUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

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

        JSONObject timelineObject;
        if (dataToPost.get("timeline_data") != null) {
            String timelineFilePath = "build/resources/functionalTest/features/opalMode/manualAccountCreation/"
                + dataToPost.get(
                "account");
            String timeline = new String(Files.readAllBytes(Paths.get(timelineFilePath)));
            timelineObject = new JSONObject(timeline);
        } else {
            String timelineFilePath = "build/resources/functionalTest/features/opalMode/manualAccountCreation"
                + "/draftAccounts/timelineJson/default.json";
            String timeline = new String(Files.readAllBytes(Paths.get(timelineFilePath)));
            timelineObject = new JSONObject(timeline);
        }
        postBody.put("account", accountObject);
        postBody.put("timeline_data", timelineObject);

        String draftAccountId = DraftAccountUtils.getAllDraftAccountIds().getFirst();
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .body(postBody.toString())
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
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

    @When("I attempt to put a draft account with an invalid token")
    public void putADraftAccountWithAnInvalidToken() {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + "invalidToken")
            .accept("*/*")
            .contentType("application/json")
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI);
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

        JSONObject timelineObject;
        if (dataToPost.get("timeline_data") != null) {
            String timelineFilePath = "build/resources/functionalTest/features/opalMode/manualAccountCreation/"
                + dataToPost.get(
                "account");
            String timeline = new String(Files.readAllBytes(Paths.get(timelineFilePath)));
            timelineObject = new JSONObject(timeline);
        } else {
            String timelineFilePath = "build/resources/functionalTest/features/opalMode/manualAccountCreation"
                + "/draftAccounts/timelineJson/default.json";
            String timeline = new String(Files.readAllBytes(Paths.get(timelineFilePath)));
            timelineObject = new JSONObject(timeline);
        }
        postBody.put("account", accountObject);
        postBody.put("timeline_data", timelineObject);

        String draftAccountId = DraftAccountUtils.getAllDraftAccountIds().getFirst();
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .body(postBody.toString())
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
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

        JSONObject timelineObject;
        if (dataToPost.get("timeline_data") != null) {
            String timelineFilePath = "build/resources/functionalTest/features/opalMode/manualAccountCreation/"
                + dataToPost.get(
                "account");
            String timeline = new String(Files.readAllBytes(Paths.get(timelineFilePath)));
            timelineObject = new JSONObject(timeline);
        } else {
            String timelineFilePath = "build/resources/functionalTest/features/opalMode/manualAccountCreation"
                + "/draftAccounts/timelineJson/default.json";
            String timeline = new String(Files.readAllBytes(Paths.get(timelineFilePath)));
            timelineObject = new JSONObject(timeline);
        }
        postBody.put("account", accountObject);
        postBody.put("timeline_data", timelineObject);

        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .body(postBody.toString())
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + "10");
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

        JSONObject timelineObject;
        if (dataToPost.get("timeline_data") != null) {
            String timelineFilePath = "build/resources/functionalTest/features/opalMode/manualAccountCreation/"
                + dataToPost.get(
                "account");
            String timeline = new String(Files.readAllBytes(Paths.get(timelineFilePath)));
            timelineObject = new JSONObject(timeline);
        } else {
            String timelineFilePath = "build/resources/functionalTest/features/opalMode/manualAccountCreation"
                + "/draftAccounts/timelineJson/default.json";
            String timeline = new String(Files.readAllBytes(Paths.get(timelineFilePath)));
            timelineObject = new JSONObject(timeline);
        }
        postBody.put("account", accountObject);
        postBody.put("timeline_data", timelineObject);
        String draftAccountId = DraftAccountUtils.getAllDraftAccountIds().getFirst();
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("text/html")
            .contentType("application/json")
            .body(postBody.toString())
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
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

        JSONObject timelineObject;
        if (dataToPost.get("timeline_data") != null) {
            String timelineFilePath = "build/resources/functionalTest/features/opalMode/manualAccountCreation/"
                + dataToPost.get(
                "account");
            String timeline = new String(Files.readAllBytes(Paths.get(timelineFilePath)));
            timelineObject = new JSONObject(timeline);
        } else {
            String timelineFilePath = "build/resources/functionalTest/features/opalMode/manualAccountCreation"
                + "/draftAccounts/timelineJson/default.json";
            String timeline = new String(Files.readAllBytes(Paths.get(timelineFilePath)));
            timelineObject = new JSONObject(timeline);
        }
        postBody.put("account", accountObject);
        postBody.put("timeline_data", timelineObject);

        String draftAccountId = DraftAccountUtils.getAllDraftAccountIds().getFirst();
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("text/plain")
            .contentType("application/json")
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
    }

    @When("I put the draft account trying to provoke an internal server error")
    public void putDraftAccountToProvokeAnInternalServerError() {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("application/json")
            .contentType("application/xml")
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI + "?business_unit=%20");
    }
}
