package uk.gov.hmcts.opal.steps.defendantaccount;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import java.util.List;
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

    /**
     * Recognises a known environment dataset by imposition ID, then checks all its expected fields.
     *
     * @param fields expected JSON paths and values for preprod and JCDE.
     */
    @Then("the legacy defendant account impositions contain the expected data for the target environment")
    public void assertLegacyImpositionsForTargetEnvironment(DataTable fields) {
        List<Map<String, String>> rows = fields.asMaps(String.class, String.class);
        Map<String, String> expectedIds = rows.stream()
            .filter(row -> "imposition_id".equals(row.get("field")))
            .findFirst().orElseThrow();
        JsonPath response = then().extract().body().jsonPath();
        List<Long> actualIds = response.getList("impositions.imposition_id", Long.class);
        // Choose one complete known dataset rather than allowing field-by-field alternatives.
        List<String> matchingEnvironments = expectedIds.entrySet().stream()
            .filter(entry -> !"field".equals(entry.getKey()))
            .filter(entry -> actualIds.contains(Long.parseLong(entry.getValue())))
            .map(Map.Entry::getKey)
            .toList();
        assertEquals(1, matchingEnvironments.size(),
            "Expected exactly one known Legacy impositions dataset, but found " + matchingEnvironments
                + " for returned imposition IDs " + actualIds);

        String environment = matchingEnvironments.getFirst();
        String impositionId = expectedIds.get(environment);
        long expectedImpositionId = Long.parseLong(impositionId);
        String impositionPath = "impositions.find { it.imposition_id == " + expectedImpositionId + " }";

        for (Map<String, String> row : rows) {
            String field = row.get("field");
            assertEquals(row.get(environment), response.getString(impositionPath + "." + field),
                "Unexpected " + field + " for imposition " + impositionId + " in " + environment);
        }
    }
}
