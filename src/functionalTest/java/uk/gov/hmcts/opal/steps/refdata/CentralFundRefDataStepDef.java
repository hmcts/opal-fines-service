package uk.gov.hmcts.opal.steps.refdata;

import static net.serenitybdd.rest.SerenityRest.lastResponse;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.opal.config.Constants.CENTRAL_FUNDS_URI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.CommonMethods;
import uk.gov.hmcts.opal.utils.TestHttpClient;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

/**
 * Defines Cucumber steps for Central Fund deployed-environment reference-data checks.
 */
public class CentralFundRefDataStepDef extends BaseStepDef {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CommonMethods methods = new CommonMethods();
    private Integer requestedBusinessUnitId;

    /**
     * Calls the Central Fund endpoint for the supplied business unit using the current scenario
     * user.
     *
     * @param businessUnitId business unit identifier to request.
     */
    @When("I make a request to the central funds api for business unit {int}")
    public void makeARequestToTheCentralFundsApiForBusinessUnit(int businessUnitId) {
        requestedBusinessUnitId = businessUnitId;
        methods.getRequest(CENTRAL_FUNDS_URI + "/" + businessUnitId);
    }

    /**
     * Asserts the deployed Central Fund response contract for the discovered Central Fund.
     *
     * @throws IOException if the response body cannot be parsed as JSON.
     */
    @Then("the central fund response matches the deployed contract")
    public void centralFundResponseMatchesTheDeployedContract() throws IOException {
        Response response = lastResponse();
        int businessUnitId = requiredRequestedBusinessUnitId();

        response.then()
            .statusCode(200)
            .body("major_creditor.creditor_account_id", notNullValue())
            .body("major_creditor.account_number", notNullValue())
            .body("major_creditor.name", notNullValue())
            .body("business_unit_details.business_unit_id", equalTo(String.valueOf(businessUnitId)))
            .body("business_unit_details.business_unit_name", notNullValue())
            .body("business_unit_details.welsh_speaking", matchesPattern("Y|N"));

        String etag = response.getHeader("ETag");
        assertNotNull(etag, "ETag header must be present");
        assertFalse(etag.startsWith("W/"), "ETag must be strong");
        assertTrue(etag.matches("^\"[0-9]+\"$"), "ETag must be a quoted numeric account version");

        JsonNode root = OBJECT_MAPPER.readTree(response.getBody().asString());
        assertEquals(Set.of("major_creditor", "business_unit_details"), fieldNames(root));
        assertEquals(Set.of("creditor_account_id", "account_number", "name"), fieldNames(root.get("major_creditor")));
        assertEquals(Set.of("business_unit_id", "business_unit_name", "welsh_speaking"),
            fieldNames(root.get("business_unit_details")));
    }

    /**
     * Asserts that the latest raw unauthorised Central Fund response matches the standard auth
     * envelope used by the deployed service.
     *
     * @param expectedStatus expected HTTP status code.
     * @throws IOException if the raw response body cannot be parsed as JSON.
     */
    @Then("the central fund authentication error response is returned with status {int}")
    public void centralFundAuthenticationErrorResponseIsReturnedWithStatus(int expectedStatus) throws IOException {
        TestHttpResponse response = scenarioContext().consumeLatestHttpResponse();
        assertNotNull(response, "Expected a raw HTTP response for the Central Fund auth scenario");
        assertEquals(expectedStatus, response.statusCode());

        JsonNode problem = OBJECT_MAPPER.readTree(response.body());
        assertEquals(expectedStatus, problem.path("status").asInt());
        assertEquals("Unauthorized", problem.path("title").asText());
        assertEquals("https://hmcts.gov.uk/problems/unauthorized", problem.path("type").asText());
        assertTrue(problem.has("retriable"));
        assertNotEquals("", problem.path("detail").asText());
        assertNotEquals("", problem.path("instance").asText());
        assertNotEquals("", problem.path("operation_id").asText());
        assertFalse(problem.has("major_creditor"));
        assertFalse(problem.has("business_unit_details"));
    }

    /**
     * Asserts that the latest Central Fund response is the standard forbidden problem response.
     */
    @Then("the central fund forbidden response is returned")
    public void centralFundForbiddenResponseIsReturned() {
        lastResponse().then()
            .statusCode(403)
            .body("title", equalTo("Forbidden"))
            .body("detail", equalTo("You do not have permission to access this resource"))
            .body("type", equalTo("https://hmcts.gov.uk/problems/forbidden"))
            .body("retriable", equalTo(false))
            .body("instance", notNullValue())
            .body("operation_id", notNullValue());
    }

    /**
     * Asserts that the latest Central Fund response is the standard not-found problem response.
     *
     * @throws IOException if the response body cannot be parsed as JSON.
     */
    @Then("the central fund not found response is returned")
    public void centralFundNotFoundResponseIsReturned() throws IOException {
        Response response = lastResponse();
        response.then()
            .statusCode(404)
            .body("title", equalTo("Entity Not Found"))
            .body("detail", equalTo("The requested entity could not be found"))
            .body("type", equalTo("https://hmcts.gov.uk/problems/entity-not-found"))
            .body("retriable", equalTo(false))
            .body("instance", notNullValue())
            .body("operation_id", notNullValue());

        JsonNode problem = OBJECT_MAPPER.readTree(response.getBody().asString());
        assertFalse(problem.has("major_creditor"));
        assertFalse(problem.has("business_unit_details"));
    }

    private int requiredRequestedBusinessUnitId() {
        assertNotNull(requestedBusinessUnitId,
            "No Central Fund business unit has been requested for this scenario. "
                + "Call the Central Fund request step before using the assertions.");
        return requestedBusinessUnitId;
    }

    private Set<String> fieldNames(JsonNode node) {
        Set<String> fields = new HashSet<>();
        node.fieldNames().forEachRemaining(fields::add);
        return fields;
    }
}
