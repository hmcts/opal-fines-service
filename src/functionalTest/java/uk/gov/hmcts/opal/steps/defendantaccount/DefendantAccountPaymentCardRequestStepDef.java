package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;

/**
 * Defines Cucumber steps for defendant-account payment-card-request scenarios.
 */
public class DefendantAccountPaymentCardRequestStepDef extends BaseStepDef {

    private static final String DEFENDANT_ACCOUNTS_URI = "/defendant-accounts";
    private static final String PLACEHOLDER_DEFENDANT_ACCOUNT_ID = "999999";
    private static final String DEFAULT_IF_MATCH = "\"0\"";

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
