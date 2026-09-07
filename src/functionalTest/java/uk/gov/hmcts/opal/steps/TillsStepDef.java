package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Defines deployed-environment steps for the till summary endpoint.
 */
public class TillsStepDef extends BaseStepDef {

    private static final String TILLS_PATH = "/tills";
    private static final List<String> DOCUMENTED_TILL_FIELDS = List.of(
        "amount",
        "business_unit_name",
        "date_processed",
        "errors",
        "file_name",
        "processed_by",
        "source",
        "till_number");

    /**
     * Retrieves tills scoped to one business unit for the current authenticated user.
     *
     * @param businessUnitId business unit to request
     */
    @When("I request tills for business unit {int}")
    public void requestTillsForBusinessUnit(int businessUnitId) {
        authorisedJsonRequest()
            .queryParam("business_unit_ids", businessUnitId)
            .when()
            .get(getTestUrl() + TILLS_PATH);
    }

    /**
     * Retrieves auto-payment tills from the deployed till-summary view.
     */
    @When("I request auto-payment tills")
    public void requestAutoPaymentTills() {
        authorisedJsonRequest()
            .queryParam("auto_payments", true)
            .when()
            .get(getTestUrl() + TILLS_PATH);
    }

    /**
     * Retrieves a combination of filters that cannot match a till in the deployed environment.
     *
     * @param businessUnitId a valid int16 business unit ID reserved here as unmatched test input
     */
    @When("I request allocated auto-payment tills for unmatched business unit {int}")
    public void requestUnmatchedAllocatedAutoPaymentTills(int businessUnitId) {
        authorisedJsonRequest()
            .queryParam("business_unit_ids", businessUnitId)
            .queryParam("statuses", "ALLOCATED")
            .queryParam("auto_payments", true)
            .when()
            .get(getTestUrl() + TILLS_PATH);
    }

    /**
     * Confirms row-level permission filtering or unmatched criteria return the documented empty
     * success contract rather than a permission failure.
     */
    @Then("the tills response is an empty successful response")
    public void tillsResponseIsEmptyAndSuccessful() {
        assertSuccessfulResponse();

        List<Map<String, Object>> tills = then().extract().jsonPath().getList("tills");
        assertNotNull(tills, "Response must contain tills");
        assertTrue(tills.isEmpty(), "Expected no tills for the requested permissions and filters");
    }

    /**
     * Confirms auto-payment results expose only the OpenAPI fields, including the original-file
     * details required by the auto-payment view.
     */
    @Then("the auto-payment tills response contains documented till and original file details")
    public void autoPaymentTillsResponseContainsDocumentedDetails() {
        assertSuccessfulResponse();

        List<Map<String, Object>> tills = then().extract().jsonPath().getList("tills");
        assertNotNull(tills, "Response must contain tills");
        assertFalse(tills.isEmpty(), "The QA environment must provide an auto-payment till fixture");

        for (Map<String, Object> till : tills) {
            assertEquals(DOCUMENTED_TILL_FIELDS, till.keySet().stream().sorted().toList(),
                "Each till must contain only documented fields");
            assertInstanceOf(Number.class, till.get("till_number"));
            assertInstanceOf(Number.class, till.get("errors"));
            assertInstanceOf(Number.class, till.get("amount"));
            assertInstanceOf(String.class, till.get("business_unit_name"));
            assertInstanceOf(String.class, till.get("processed_by"));
            assertInstanceOf(String.class, till.get("date_processed"));
            assertInstanceOf(String.class, till.get("file_name"));
            assertInstanceOf(String.class, till.get("source"));
            assertFalse(((String) till.get("file_name")).isBlank(), "file_name must be populated");
            assertFalse(((String) till.get("source")).isBlank(), "source must be populated");
        }
    }

    private void assertSuccessfulResponse() {
        then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }
}
