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
 * Defines deployed-environment steps for outstanding auto payment count requests.
 */
public class OutstandingAutoPaymentStepDef extends BaseStepDef {

    private static final String OUTSTANDING_AUTO_PAYMENT_COUNT_PATH =
        "/business-units/outstanding-auto-payment-count";

    /**
     * Retrieves outstanding auto payment counts for the current authenticated user.
     */
    @When("I request outstanding auto payment counts")
    public void requestOutstandingAutoPaymentCounts() {
        authorisedJsonRequest()
            .when()
            .get(getTestUrl() + OUTSTANDING_AUTO_PAYMENT_COUNT_PATH);
    }

    /**
     * Confirms a successful response uses the documented JSON media type.
     */
    @Then("the outstanding auto payment count response is successful")
    public void outstandingAutoPaymentCountResponseIsSuccessful() {
        then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * Confirms a user with no permitted business units receives the documented empty response.
     */
    @Then("the outstanding auto payment count response is empty")
    public void outstandingAutoPaymentCountResponseIsEmpty() {
        outstandingAutoPaymentCountResponseIsSuccessful();

        List<Map<String, Object>> businessUnits = then().extract().jsonPath().getList("business_units");
        assertNotNull(businessUnits, "Response must contain business_units");
        assertTrue(businessUnits.isEmpty(), "User without payment permission must receive an empty array");
    }

    /**
     * Confirms the success response contains only the fields and JSON types defined by OpenAPI.
     */
    @Then("the outstanding auto payment count response conforms to the documented schema")
    public void outstandingAutoPaymentCountResponseConformsToDocumentedSchema() {
        outstandingAutoPaymentCountResponseIsSuccessful();

        List<Map<String, Object>> businessUnits = then().extract().jsonPath().getList("business_units");
        assertNotNull(businessUnits, "Response must contain business_units");
        assertFalse(businessUnits.isEmpty(), "Seeded outstanding interface jobs must return a business unit");

        for (Map<String, Object> businessUnit : businessUnits) {
            assertEquals(
                List.of("business_unit_id", "business_unit_name", "file_count", "till_count"),
                businessUnit.keySet().stream().sorted().toList(),
                "Each business unit must contain only documented fields"
            );
            assertInstanceOf(Number.class, businessUnit.get("business_unit_id"));
            assertInstanceOf(String.class, businessUnit.get("business_unit_name"));
            assertInstanceOf(Number.class, businessUnit.get("file_count"));
            assertInstanceOf(Number.class, businessUnit.get("till_count"));
        }
    }

    /**
     * Confirms the deployed count view includes the business unit whose interface jobs were seeded.
     *
     * @param businessUnitId business unit used when creating the test interface jobs.
     */
    @Then("business unit {int} has outstanding files to process")
    public void businessUnitHasOutstandingFilesToProcess(int businessUnitId) {
        List<Map<String, Object>> businessUnits = then().extract().jsonPath().getList("business_units");
        Map<String, Object> businessUnit = businessUnits.stream()
            .filter(item -> ((Number) item.get("business_unit_id")).intValue() == businessUnitId)
            .findFirst()
            .orElseThrow(() -> new AssertionError(
                "Expected business unit %d in the response".formatted(businessUnitId)));

        assertTrue(
            ((Number) businessUnit.get("file_count")).longValue() >= 2,
            "Both seeded interface jobs must be reflected in file_count"
        );
    }
}
