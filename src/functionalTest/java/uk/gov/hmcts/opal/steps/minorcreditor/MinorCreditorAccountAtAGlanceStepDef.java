package uk.gov.hmcts.opal.steps.minorcreditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;

/**
 * Defines Cucumber steps for the legacy minor-creditor account at-a-glance endpoint.
 */
public class MinorCreditorAccountAtAGlanceStepDef extends BaseStepDef {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String AT_A_GLANCE_PATH = "/minor-creditor-accounts/%d/at-a-glance";
    private static final long LEGACY_MINOR_CREDITOR_ACCOUNT_ID = 99_000_000_000_802L;

    /**
     * Requests the account seeded in the legacy API stub.
     */
    @When("I request minor creditor account at a glance for the seeded legacy minor creditor account")
    public void requestSeededLegacyMinorCreditorAccountAtAGlance() {
        getAtAGlance(BearerTokenStepDef.getToken(), LEGACY_MINOR_CREDITOR_ACCOUNT_ID);
    }

    /**
     * Requests the seeded account without an Authorization header.
     */
    @When("I request minor creditor account at a glance for the seeded legacy minor creditor account without a token")
    public void requestSeededLegacyMinorCreditorAccountAtAGlanceWithoutToken() {
        getAtAGlance(null, LEGACY_MINOR_CREDITOR_ACCOUNT_ID);
    }

    /**
     * Requests the seeded account as a specified user.
     *
     * @param user user email used to resolve a bearer token.
     */
    @When("the {string} user requests minor creditor account at a glance for the seeded legacy minor creditor account")
    public void userRequestsSeededLegacyMinorCreditorAccountAtAGlance(String user) {
        getAtAGlance(BearerTokenStepDef.getAccessTokenForUser(user), LEGACY_MINOR_CREDITOR_ACCOUNT_ID);
    }

    /**
     * Requests an account identifier that should not exist.
     */
    @When("I request minor creditor account at a glance for a non-existent minor creditor account")
    public void requestNonExistentMinorCreditorAccountAtAGlance() {
        getAtAGlance(BearerTokenStepDef.getToken(), 91_000_000_000_000L);
    }

    /**
     * Asserts the successful response contains the documented top-level structures and defendant fields.
     */
    @Then("the minor creditor account at a glance response is returned as documented")
    public void minorCreditorAccountAtAGlanceResponseIsReturnedAsDocumented() {
        Response response = net.serenitybdd.rest.SerenityRest.lastResponse();
        assertEquals(200, response.statusCode(), "Unexpected HTTP status");
        assertTrue(response.contentType().contains("application/json"), "Unexpected response content type");
        assertTrue(response.header(HttpHeaders.ETAG).matches("^\"[^\"]+\"$"), "A strong ETag is required");

        JsonNode root = latestJsonBody();
        assertTrue(root.path("party").isObject(), "party should be an object");
        assertTrue(root.path("address").isObject(), "address should be an object");
        assertTrue(root.path("creditor_account_id").isIntegralNumber(), "creditor_account_id should be an integer");
        assertTrue(root.path("defendant").isObject(), "defendant should be an object");
        assertTrue(root.path("payment").isObject(), "payment should be an object");

        JsonNode defendant = root.path("defendant");
        assertTrue(defendant.path("organisation").isBoolean(), "defendant.organisation should be a boolean");
        assertEquals(
            Set.of("account_number", "account_id", "title", "forenames", "surname", "organisation",
                "organisation_name"),
            fieldNames(defendant),
            "Unexpected defendant fields"
        );
    }

    /**
     * Asserts the organisation discriminator and name added by PO-2982.
     *
     * @param organisationName expected defendant organisation name.
     */
    @Then("the minor creditor account at a glance defendant is an organisation named {string}")
    public void minorCreditorAccountAtAGlanceDefendantIsOrganisation(String organisationName) {
        JsonNode defendant = latestJsonBody().path("defendant");
        assertTrue(defendant.path("organisation").asBoolean(), "Expected an organisation defendant");
        assertEquals(organisationName, defendant.path("organisation_name").asText());
    }

    /**
     * Asserts the shared ProblemDetail response shape.
     *
     * @param expectedStatus expected HTTP status code.
     */
    @Then("the minor creditor account at a glance error response matches the standard problem detail contract for "
        + "status {int}")
    public void minorCreditorAccountAtAGlanceErrorMatchesProblemDetail(int expectedStatus) {
        Response response = net.serenitybdd.rest.SerenityRest.lastResponse();
        assertEquals(expectedStatus, response.statusCode(), "Unexpected HTTP status");

        JsonNode root = latestJsonBody();
        assertTrue(root.path("title").isTextual(), "title should be text");
        assertTrue(root.path("detail").isTextual(), "detail should be text");
        assertEquals(expectedStatus, root.path("status").asInt(), "Unexpected problem detail status");
        assertFalse(root.has("party"), "Error response should not contain account data");
        assertFalse(root.has("defendant"), "Error response should not contain defendant data");
    }

    private void getAtAGlance(String token, long accountId) {
        RequestSpecification request = net.serenitybdd.rest.SerenityRest.given()
            .accept("*/*")
            .contentType("application/json");

        if (token != null && !token.isBlank()) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        request.when().get(getTestUrl() + AT_A_GLANCE_PATH.formatted(accountId));
    }

    private JsonNode latestJsonBody() {
        return OBJECT_MAPPER.readTree(net.serenitybdd.rest.SerenityRest.lastResponse().getBody().asString());
    }

    private Set<String> fieldNames(JsonNode node) {
        Set<String> names = new java.util.HashSet<>();
        node.propertyStream().forEach(entry -> names.add(entry.getKey()));
        return names;
    }
}
