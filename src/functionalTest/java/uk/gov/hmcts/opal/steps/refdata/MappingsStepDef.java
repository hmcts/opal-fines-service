package uk.gov.hmcts.opal.steps.refdata;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.opal.config.Constants.MAPPINGS_URI;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import uk.gov.hmcts.opal.steps.CommonMethods;

/**
 * Defines Cucumber steps for mappings reference-data requests and assertions.
 */
public class MappingsStepDef {

    private final CommonMethods methods = new CommonMethods();

    /**
     * Retrieves mappings for the supplied mapping type.
     *
     * @param type mapping type to request.
     */
    @When("I make a request to the mappings api for type {string}")
    public void makeARequestToTheMappingsApiForType(String type) {
        methods.getRequest(MAPPINGS_URI + "/" + type);
    }

    /**
     * Retrieves mappings without supplying the required mapping type path segment.
     */
    @When("I make a request to the mappings api without a type")
    public void makeARequestToTheMappingsApiWithoutAType() {
        methods.getRequest(MAPPINGS_URI);
    }

    /**
     * Asserts that the defendant account status mappings match the deployed contract and values.
     */
    @Then("the defendant account status mappings are returned")
    public void defendantAccountStatusMappingsAreReturned() {
        then()
            .statusCode(200)
            .body("$", hasSize(6))
            .body("code", contains("CS", "L", "TA", "TO", "TS", "WO"))
            .body("display_name", contains(
                "Account consolidated",
                "Live",
                "TFO acknowledged",
                "TFO to be acknowledged",
                "TFO to NI/Scotland to be acknowledged",
                "Account written off"
            ));
    }
}
