package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import uk.gov.hmcts.opal.actions.defendantaccount.DefendantAccountEnforcementsActions;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Defines Cucumber steps for defendant-account payment-card-request scenarios.
 */
public class DefendantAccountPaymentCardRequestStepDef extends BaseStepDef {

    private static final String DEFENDANT_ACCOUNTS_URI = "/defendant-accounts";
    private static final String PLACEHOLDER_DEFENDANT_ACCOUNT_ID = "999999";
    private static final String DEFAULT_IF_MATCH = "\"0\"";
    private static final String BUSINESS_UNIT_ID = "77";

    private final DefendantAccountEnforcementsActions enforcementActions = new DefendantAccountEnforcementsActions();

    /**
     * Calls the payment-card-request endpoint without the required Business-Unit-Id header.
     */
    @When("I request a defendant account payment card without the Business-Unit-Id header")
    public void requestDefendantAccountPaymentCardWithoutBusinessUnitIdHeader() {
        authorisedJsonRequest()
            .header("If-Match", DEFAULT_IF_MATCH)
            .body("{}")
            .when()
            .post(getTestUrl() + DEFENDANT_ACCOUNTS_URI + "/" + PLACEHOLDER_DEFENDANT_ACCOUNT_ID
                      + "/payment-card-request");
    }

    /**
     * Calls the payment-card-request endpoint with a caller-controlled legacy header. The service
     * must derive the audit business-unit user from the authenticated user instead.
     *
     * @param suppliedBusinessUnitUserId caller-controlled value that must be ignored.
     */
    @When("I request a payment card for the created defendant account with business unit user id {string}")
    public void requestPaymentCardForCreatedDefendantAccountWithBusinessUnitUserId(String suppliedBusinessUnitUserId) {
        Response statusResponse = enforcementActions.getCreatedDefendantAccountEnforcementStatus();
        assertEquals(200, statusResponse.statusCode(), "Expected enforcement-status request to succeed");

        //noinspection UastIncorrectHttpHeaderInspection
        authorisedJsonRequest()
            .header("Business-Unit-Id", BUSINESS_UNIT_ID)
            .header("Business-Unit-User-Id", suppliedBusinessUnitUserId)
            .header("If-Match", scenarioContext().getDefendantAccountEtag())
            .body("{}")
            .when()
            .post(getTestUrl() + DEFENDANT_ACCOUNTS_URI + "/"
                      + scenarioContext().getCreatedDefendantAccountIdOrFail() + "/payment-card-request");
    }

    /**
     * Asserts that the request completed for the defendant account created in this scenario.
     */
    @Then("the payment card request succeeds for the created defendant account")
    public void paymentCardRequestSucceedsForCreatedDefendantAccount() {
        then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("defendant_account_id", equalTo(
                Long.parseLong(scenarioContext().getCreatedDefendantAccountIdOrFail())));
    }

    /**
     * Asserts that the latest payment-card-request error reports the missing required header.
     */
    @Then("the payment card request response reports the missing Business-Unit-Id header")
    public void paymentCardRequestResponseReportsMissingBusinessUnitIdHeader() {
        then()
            .log().ifValidationFails()
            .body("title", equalTo("Missing Required Header"))
            .body("type", equalTo("https://hmcts.gov.uk/problems/missing-header"))
            .body("detail", equalTo("Required request header \"Business-Unit-Id\" is missing"))
            .body("status", equalTo(400))
            .body("retriable", equalTo(false));
    }
}
