package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.java.en.Then;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import static net.serenitybdd.rest.SerenityRest.then;

/**
 * Defines Cucumber steps for defendant-account search scenarios.
 */
public class DefendantAccountSearchStepDef extends BaseStepDef {

    /**
     * Asserts that the latest response has the expected HTTP status code.
     *
     * @param statusCode expected HTTP status code.
     */
    @Then("the response status code is {int}")
    public void responseStatusCodeIs(int statusCode) {
        then().assertThat().statusCode(statusCode);
    }
}
