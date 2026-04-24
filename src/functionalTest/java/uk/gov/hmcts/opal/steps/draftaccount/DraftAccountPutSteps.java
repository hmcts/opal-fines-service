package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.core.Serenity;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountActions;
import uk.gov.hmcts.opal.actions.draftaccount.DraftAccountRequestFactory;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;

/**
 * Defines Cucumber steps for updating draft accounts.
 */
public class DraftAccountPutSteps extends BaseStepDef {
    private final DraftAccountActions actions = new DraftAccountActions();
    private final DraftAccountRequestFactory requestFactory = new DraftAccountRequestFactory();

    /**
     * Replaces the draft account created earlier in the scenario with the supplied values.
     *
     * @param data Cucumber table containing the replacement values for the request.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if an account fixture file cannot be read.
     */
    @When("I update the draft account that was just created with the following details")
    public void updateCreatedDraftAccount(DataTable data) throws JSONException, IOException {
        Map<String, String> dataToPost = data.asMap(String.class, String.class);
        JSONObject postBody = requestFactory.buildReplaceRequestBody(
            dataToPost,
            DraftAccountRequestFactory.BusinessUnitIdMode.INTEGER
        );
        String draftAccountId = actions.onlyCreatedDraftAccountIdOrFail();
        authorisedJsonRequest()
            .body(postBody.toString())
            .header(createQuotedLongHeader("If-Match", dataToPost))
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
    }

    /**
     * Asserts that the `created_at` timestamp is unchanged after the update.
     */
    @Then("I see the created at time hasn't changed")
    public void checkCreatedAtTime() {
        Instant apiCreatedAt = Instant.parse(then().extract().body().jsonPath().getString("created_at"));

        Instant createdTime = Instant.parse(scenarioContext().getDraftAccountCreatedAtTime());

        String createdAtTime = String.valueOf(createdTime.truncatedTo(java.time.temporal.ChronoUnit.MILLIS));
        String createdAt = String.valueOf(apiCreatedAt.truncatedTo(java.time.temporal.ChronoUnit.MILLIS));

        assertEquals(createdAtTime, createdAt, "Created at time has changed");

        Serenity.recordReportData().withTitle("Times").andContents(
            "Created at time: " + createdAtTime + "\nResponse created at time: " + createdAt);
    }

    /**
     * Asserts that the `account_status_date` moved forward after the update.
     */
    @Then("I see the account status date is now after the initial account status date")
    public void checkAccountStatusDate() {
        Instant apiAccountStatusDate = Instant.parse(then().extract()
                                                         .body().jsonPath().getString("account_status_date"));
        Instant initialAccountStatusDate = Instant.parse(scenarioContext().getInitialAccountStatusDate());

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

    /**
     * Attempts to put a draft-account using an invalid bearer token.
     */
    @When("I attempt to put a draft account with an invalid token")
    public void putADraftAccountWithAnInvalidToken() {
        jsonRequestWithToken("invalidToken")
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI);
    }

    /**
     * Attempts to update the created draft account with a deliberately invalid request payload.
     *
     * @param data Cucumber table supplying the invalid values for the request body.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     * @throws IOException if an account fixture file cannot be read.
     */
    @When("I attempt to put a draft account with an invalid request payload")
    public void putADraftAccountWithAnInvalidRequestPayload(DataTable data) throws JSONException, IOException {
        Map<String, String> dataToPost = data.asMap(String.class, String.class);
        JSONObject postBody = requestFactory.buildReplaceRequestBody(
            dataToPost,
            DraftAccountRequestFactory.BusinessUnitIdMode.LONG
        );
        String draftAccountId = actions.onlyCreatedDraftAccountIdOrFail();
        authorisedJsonRequest()
            .body(postBody.toString())
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
    }

    /**
     * Attempts to update a non-existent draft account to exercise the not-found path.
     *
     * @param data Cucumber table supplying the request values.
     * @throws IOException if an account fixture file cannot be read.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I attempt to put a draft account with resource not found")
    public void putADraftAccountWithResourceNotFound(DataTable data) throws IOException, JSONException {
        Map<String, String> dataToPost = data.asMap(String.class, String.class);
        JSONObject postBody = requestFactory.buildReplaceRequestBody(
            dataToPost,
            DraftAccountRequestFactory.BusinessUnitIdMode.LONG
        );

        authorisedJsonRequest()
            .body(postBody.toString())
            .header(createQuotedLongHeader("If-Match", dataToPost))
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + "999999");
    }

    /**
     * Attempts to update the created draft account while requesting an unsupported response
     * content type.
     *
     * @param data Cucumber table supplying the request values.
     * @throws IOException if an account fixture file cannot be read.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I attempt to put a draft account with unsupported content type for response")
    public void putADraftAccountWithUnsupportedContentType(DataTable data) throws IOException, JSONException {
        Map<String, String> dataToPost = data.asMap(String.class, String.class);
        JSONObject postBody = requestFactory.buildReplaceRequestBody(
            dataToPost,
            DraftAccountRequestFactory.BusinessUnitIdMode.LONG
        );
        String draftAccountId = actions.onlyCreatedDraftAccountIdOrFail();
        authorisedJsonRequest()
            .accept("text/html")
            .body(postBody.toString())
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
    }

    /**
     * Attempts to update the created draft account with an unsupported request media type.
     *
     * @param data Cucumber table supplying the request values.
     * @throws IOException if an account fixture file cannot be read.
     * @throws JSONException if the JSON payload cannot be created from the supplied values.
     */
    @When("I attempt to put a draft account with unsupported media type for request")
    public void putADraftAccountWithUnsupportedMediaTypeForRequest(DataTable data) throws IOException, JSONException {
        String draftAccountId = actions.onlyCreatedDraftAccountIdOrFail();
        authorisedJsonRequest()
            .accept("text/plain")
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);
    }

    /**
     * Sends a malformed PUT request to exercise the draft-account internal-server-error path.
     */
    @When("I put the draft account trying to provoke an internal server error")
    public void putDraftAccountToProvokeAnInternalServerError() {
        authorisedJsonRequest()
            .accept("application/json")
            .contentType("application/xml")
            .when()
            .put(getTestUrl() + DRAFT_ACCOUNTS_URI + "?business_unit=%20");
    }
}
