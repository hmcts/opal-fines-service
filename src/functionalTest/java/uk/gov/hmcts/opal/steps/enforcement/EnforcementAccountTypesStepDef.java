package uk.gov.hmcts.opal.steps.enforcement;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.serenitybdd.rest.SerenityRest.given;
import static net.serenitybdd.rest.SerenityRest.lastResponse;
import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Defines Cucumber steps for the enforcement account types endpoint.
 */
public class EnforcementAccountTypesStepDef extends BaseStepDef {

    private static final String ENFORCEMENT_ACCOUNT_TYPES_PATH = "/enforcement-accounts-types/";
    private static final String ENFORCEMENT_ACCOUNT_TYPES = "enforcement_account_types";
    private static final Set<String> TOP_LEVEL_FIELDS = Set.of(ENFORCEMENT_ACCOUNT_TYPES);
    private static final Set<String> ENFORCEMENT_ACCOUNT_TYPE_FIELDS = Set.of(
        "id",
        "version",
        "minimum_balance",
        "account_type",
        "enforcement_account_type",
        "path"
    );
    private static final Set<String> EXPECTED_ENFORCEMENT_ACCOUNT_TYPES = Set.of(
        "AL",
        "AH",
        "COL",
        "COH",
        "COLL",
        "COLH",
        "YL",
        "YH"
    );

    /**
     * Requests all enforcement account types as a specific test user.
     *
     * @param user user email used to resolve a bearer token.
     */
    @When("the {string} user requests all enforcement account types")
    public void userRequestsAllEnforcementAccountTypes(String user) {
        getEnforcementAccountTypes(BearerTokenStepDef.getAccessTokenForUser(user));
    }

    /**
     * Requests all enforcement account types with an invalid bearer token.
     */
    @When("all enforcement account types are requested with an invalid token")
    public void requestAllEnforcementAccountTypesWithInvalidToken() {
        getEnforcementAccountTypes("invalid-token");
    }

    /**
     * Asserts the successful response matches the documented response shape and seeded values.
     */
    @Then("all expected enforcement account types should be returned")
    public void enforcementAccountTypesResponseIsReturnedAsDocumented() {
        then().statusCode(200);

        Response response = lastResponse();
        Map<String, Object> root = response.jsonPath().getMap("");
        assertEquals(TOP_LEVEL_FIELDS, root.keySet());

        List<Map<String, Object>> accountTypes = response.jsonPath().getList(ENFORCEMENT_ACCOUNT_TYPES);
        assertEquals(EXPECTED_ENFORCEMENT_ACCOUNT_TYPES.size(), accountTypes.size());
        assertEquals(
            EXPECTED_ENFORCEMENT_ACCOUNT_TYPES,
            accountTypes.stream()
                .map(accountType -> (String) accountType.get("enforcement_account_type"))
                .collect(Collectors.toSet())
        );

        accountTypes.forEach(this::assertEnforcementAccountType);
    }

    /**
     * Asserts the latest enforcement account types response is forbidden.
     */
    @Then("the enforcement account types request should be rejected as forbidden")
    public void enforcementAccountTypesRequestIsRejectedAsForbidden() {
        then()
            .statusCode(403)
            .body("title", equalTo("Forbidden"))
            .body("detail", equalTo("You do not have permission to access this resource"))
            .body("type", equalTo("https://hmcts.gov.uk/problems/forbidden"))
            .body("retriable", equalTo(false))
            .body("instance", notNullValue())
            .body("operation_id", notNullValue());
    }

    /**
     * Asserts the latest enforcement account types response is unauthorized.
     */
    @Then("the enforcement account types request should be rejected as unauthorized")
    public void enforcementAccountTypesRequestIsRejectedAsUnauthorized() {
        then()
            .statusCode(401)
            .body("title", equalTo("Unauthorized"))
            .body("detail", equalTo("You are not authorized to access this resource"))
            .body("type", equalTo("https://hmcts.gov.uk/problems/unauthorized"))
            .body("retriable", equalTo(false))
            .body("instance", notNullValue())
            .body("operation_id", notNullValue());
    }

    private Response getEnforcementAccountTypes(String token) {
        return given()
            .accept("*/*")
            .contentType("application/json")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .when()
            .get(getTestUrl() + ENFORCEMENT_ACCOUNT_TYPES_PATH);
    }

    private void assertEnforcementAccountType(Map<String, Object> accountType) {
        assertTrue(
            ENFORCEMENT_ACCOUNT_TYPE_FIELDS.containsAll(accountType.keySet()),
            "Response contains undocumented enforcement account type fields: " + accountType.keySet()
        );
        assertTrue(accountType.containsKey("id"), "id must be present");
        assertTrue(accountType.containsKey("version"), "version must be present");
        assertTrue(accountType.containsKey("account_type"), "account_type must be present");
        assertTrue(
            accountType.containsKey("enforcement_account_type"),
            "enforcement_account_type must be present"
        );
        assertTrue(accountType.containsKey("path"), "path must be present");
        assertTrue(((Number) accountType.get("id")).intValue() > 0, "id must be positive");
        assertEquals(1, accountType.get("version"));
        assertTrue(Set.of("COL", "A", "CO", "Y").contains(accountType.get("account_type")));
        assertTrue(Set.of("H", "L").contains(accountType.get("path")));
    }
}
