package uk.gov.hmcts.opal.steps.defendantaccount;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
import uk.gov.hmcts.opal.steps.BaseStepDef;

public class DefendantAccountImpositionsStepDef extends BaseStepDef {

    /**
     * Requests legacy impositions for the supplied defendant-account identifier.
     *
     * @param defendantAccountId defendant-account identifier to request.
     */
    @When("I request legacy defendant account impositions for defendant account {string}")
    public void requestLegacyDefendantAccountImpositions(String defendantAccountId) {
        authorisedJsonRequest()
            .when()
            .get(getTestUrl() + "/defendant-accounts/" + defendantAccountId + "/impositions");
    }

    /**
     * Asserts that the latest impositions response includes the expected ETag header.
     *
     * @param etag expected ETag header value.
     */
    @Then("the legacy defendant account impositions response has ETag {string}")
    public void assertLegacyDefendantAccountImpositionsResponseEtag(String etag) {
        assertEquals(etag, then().extract().header("ETag"));
    }

    /**
     * Asserts that the latest impositions response contains each expected JSON-path value.
     *
     * @param fields Cucumber table mapping JSON paths to expected values.
     */
    @Then("the legacy defendant account impositions response contains")
    public void assertLegacyDefendantAccountImpositionsResponseContains(DataTable fields) {
        Map<String, String> expectedFields = fields.asMap(String.class, String.class);

        expectedFields.forEach((jsonPath, expectedValue) -> {
            String actualValue = then().extract().body().jsonPath().getString(jsonPath);
            assertEquals(expectedValue, actualValue);
        });
    }
}
