package uk.gov.hmcts.opal.steps.refdata;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
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

    @Then("the status mappings are returned with code, display name and mime type")
    public void theStatusMappingsAreReturnedWithCodeDisplayNameAndMimeType() {
        then()
            .statusCode(200)
            .body("$", hasSize(4))
            .body("code", contains("CSV", "PDF", "JSON", "XML"))
            .body("display_name", contains(
                "CSV",
                "PDF",
                "JSON",
                "XML"
            ))
            .body("mime_type", contains(
                "text/csv",
                "application/pdf",
                "application/json",
                "application/xml"));
    }

    @Then("the method of payment mappings are returned")
    public void methodOfPaymentMappingsAreReturned() {
        then()
            .statusCode(200)
            .body("$", hasSize(4))
            .body("code", contains("NC", "PO", "CQ", "CT"))
            .body("display_name", contains(
                "Notes and coins", "Postal order", "Cheque", "Credit transfer"));
    }

    @Then("the source mappings are returned")
    public void sourceMappingsAreReturned() {
        then()
            .statusCode(200)
            .body("$", hasSize(6))
            .body("code", contains("ALLPAY", "BARCLAYCARD", "BTECKOH", "DWP", "NATWEST", "OTHER"))
            .body("display_name", contains(
                "allpay", "Barclaycard", "British Telecom", "DWP/AEA", "NatWest", "Other"));
    }

    @Then("the suspense reason mappings are returned")
    public void suspenseReasonMappingsAreReturned() {
        then()
            .statusCode(200)
            .body("$", hasSize(4))
            .body("code", contains("OF", "FA", "MS", "UN"))
            .body("display_name", contains(
                "Fines overpayment", "Fine paid in advance", "Miscellaneous payment", "Unidentified Amount"));
    }

    @Then("the mappings error response lists the supported types alphabetically")
    public void mappingsErrorResponseListsSupportedTypesAlphabetically() {
        then()
            .header("Content-Type", containsString("application/problem+json"))
            .body("supported_types", contains(
                "defendant-account-status", "file-type", "method-of-payment", "source", "suspense-reason"));
    }

    @Then("the disabled mappings error response lists only Release 1B types")
    public void disabledMappingsErrorResponseListsOnlyRelease1bTypes() {
        then()
            .header("Content-Type", containsString("application/problem+json"))
            .body("supported_types", contains("defendant-account-status", "file-type"));
    }

    @Then("the release-1c-payment disabled mapping response is returned")
    public void release1cPaymentDisabledMappingResponseIsReturned() {
        then()
            .statusCode(404)
            .header("Content-Type", containsString("application/problem+json"))
            .body("type", equalTo("https://hmcts.gov.uk/problems/feature-disabled"))
            .body("title", equalTo("Feature Disabled"))
            .body("status", equalTo(404))
            .body("detail", equalTo("The requested feature is not currently available"))
            .body("retriable", equalTo(false));
    }
}
