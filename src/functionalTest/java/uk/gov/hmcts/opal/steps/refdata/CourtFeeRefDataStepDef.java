package uk.gov.hmcts.opal.steps.refdata;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static uk.gov.hmcts.opal.config.Constants.COURT_FEES_REF_DATA_URI;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.gov.hmcts.opal.steps.BaseStepDef;

/**
 * Defines Cucumber steps for court-fee reference-data requests and assertions.
 */
public class CourtFeeRefDataStepDef extends BaseStepDef {

    /**
     * Retrieves court fees for the supplied business unit.
     *
     * @param businessUnitId business-unit identifier to apply to the request.
     */
    @When("I request court fees for business unit {int}")
    public void requestCourtFeesForBusinessUnit(int businessUnitId) {
        authorisedJsonRequest()
            .queryParam("business_unit_id", businessUnitId)
            .when()
            .get(getTestUrl() + COURT_FEES_REF_DATA_URI);
    }

    /**
     * Retrieves court fees without the required business-unit query parameter.
     */
    @When("I request court fees without a business unit")
    public void requestCourtFeesWithoutBusinessUnit() {
        authorisedJsonRequest()
            .when()
            .get(getTestUrl() + COURT_FEES_REF_DATA_URI);
    }

    /**
     * Retrieves court fees using a non-numeric business-unit query parameter.
     *
     * @param businessUnitId invalid business-unit value to submit.
     */
    @When("I request court fees with business unit {string}")
    public void requestCourtFeesWithBusinessUnit(String businessUnitId) {
        authorisedJsonRequest()
            .queryParam("business_unit_id", businessUnitId)
            .when()
            .get(getTestUrl() + COURT_FEES_REF_DATA_URI);
    }

    /**
     * Asserts that the API returns court-fee records with the fields and value types in the contract.
     */
    @Then("the court fees response contains reference data")
    public void courtFeesResponseContainsReferenceData() {
        then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType("application/json");

        List<Map<String, Object>> actualFees = then().extract().jsonPath().getList("court_fees");
        Set<String> expectedFields = Set.of("court_fee_id", "court_fee_code", "description", "amount");
        assertFalse(actualFees.isEmpty(), "Expected at least one court fee for the selected business unit");
        for (Map<String, Object> courtFee : actualFees) {
            assertEquals(expectedFields, courtFee.keySet(), "Court-fee fields should match the API contract");
            assertInstanceOf(Number.class, courtFee.get("court_fee_id"), "court_fee_id should be numeric");
            assertInstanceOf(String.class, courtFee.get("court_fee_code"), "court_fee_code should be a string");
            assertInstanceOf(String.class, courtFee.get("description"), "description should be a string");
            assertInstanceOf(Number.class, courtFee.get("amount"), "amount should be numeric");
        }
    }

    /**
     * Asserts that the API returns an empty court-fee list.
     */
    @Then("the court fees response is an empty list")
    public void courtFeesResponseIsEmpty() {
        then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType("application/json")
            .body("court_fees", hasSize(0));
    }

    /**
     * Asserts that a rejected court-fee request uses the standard problem response.
     *
     * @param status expected HTTP status.
     * @param title expected problem title.
     */
    @Then("the court fees request is rejected with standard problem status {int} and title {string}")
    public void courtFeesRequestIsRejectedWithStandardProblem(int status, String title) {
        then()
            .log().ifValidationFails()
            .statusCode(status)
            .contentType(containsString("application/problem+json"))
            .body("status", equalTo(status))
            .body("title", equalTo(title));
    }

    /**
     * Asserts that a disabled Release 1C payment feature returns its standard problem detail.
     */
    @Then("the court fees feature-disabled problem response is returned")
    public void courtFeesFeatureDisabledProblemResponseIsReturned() {
        then()
            .log().ifValidationFails()
            .statusCode(404)
            .contentType(containsString("application/problem+json"))
            .body("type", equalTo("https://hmcts.gov.uk/problems/feature-disabled"))
            .body("title", equalTo("Feature Disabled"))
            .body("status", equalTo(404))
            .body("detail", equalTo("The requested feature is not currently available"))
            .body("retriable", equalTo(false));
    }

}
