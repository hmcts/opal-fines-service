package uk.gov.hmcts.opal.steps.majorcreditor;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.steps.BearerTokenStepDef;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static net.serenitybdd.rest.SerenityRest.given;
import static net.serenitybdd.rest.SerenityRest.then;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Defines Cucumber steps for the major-creditor account header-summary endpoint.
 */
public class MajorCreditorAccountHeaderSummaryStepDef extends BaseStepDef {

    private static final String HEADER_SUMMARY_PATH = "/major-creditor-accounts/%d/header-summary";
    private static final Set<String> TOP_LEVEL_FIELDS = Set.of(
        "major_creditor",
        "business_unit_details",
        "awaiting_payout"
    );
    private static final Set<String> MAJOR_CREDITOR_FIELDS = Set.of(
        "creditor_account_id",
        "account_number",
        "name",
        "account_reference"
    );
    private static final Set<String> ACCOUNT_REFERENCE_FIELDS = Set.of(
        "account_type",
        "display_name"
    );
    private static final Set<String> BUSINESS_UNIT_FIELDS = Set.of(
        "business_unit_id",
        "business_unit_name",
        "welsh_speaking"
    );

    private Response firstResponse;
    private Response secondResponse;

    /**
     * Requests the header summary using the current scenario user's bearer token.
     *
     * @param accountId major-creditor account identifier to request.
     */
    @When("I request the major creditor account header summary for account {long}")
    public void requestMajorCreditorAccountHeaderSummary(long accountId) {
        getHeaderSummary(BearerTokenStepDef.getToken(), accountId);
    }

    /**
     * Requests the header summary as a specific test user.
     *
     * @param user user email used to resolve a bearer token.
     * @param accountId major-creditor account identifier to request.
     */
    @When("the {string} user requests the major creditor account header summary for account {long}")
    public void userRequestsMajorCreditorAccountHeaderSummary(String user, long accountId) {
        getHeaderSummary(BearerTokenStepDef.getAccessTokenForUser(user), accountId);
    }

    /**
     * Requests the header summary twice using the current scenario user's bearer token.
     *
     * @param accountId major-creditor account identifier to request.
     */
    @When("I request the major creditor account header summary for account {long} twice")
    public void requestMajorCreditorAccountHeaderSummaryTwice(long accountId) {
        firstResponse = getHeaderSummary(BearerTokenStepDef.getToken(), accountId);
        secondResponse = getHeaderSummary(BearerTokenStepDef.getToken(), accountId);
    }

    /**
     * Requests the header summary without an Authorization header.
     *
     * @param accountId major-creditor account identifier to request.
     */
    @When("I request the major creditor account header summary for account {long} without a token")
    public void requestMajorCreditorAccountHeaderSummaryWithoutToken(long accountId) {
        given()
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + HEADER_SUMMARY_PATH.formatted(accountId));
    }

    /**
     * Requests the header summary with an invalid bearer token.
     *
     * @param accountId major-creditor account identifier to request.
     */
    @When("I request the major creditor account header summary for account {long} with an invalid token")
    public void requestMajorCreditorAccountHeaderSummaryWithInvalidToken(long accountId) {
        getHeaderSummary("invalid-token", accountId);
    }

    /**
     * Asserts the successful response matches the documented API contract.
     */
    @Then("the major creditor account header summary response is returned as documented")
    public void majorCreditorAccountHeaderSummaryResponseIsReturnedAsDocumented() {
        assertHeaderSummaryResponseIsReturnedAsDocumented();
    }

    /**
     * Asserts the request returned HTTP 200.
     */
    @Then("the major creditor account header summary request succeeds")
    public void majorCreditorAccountHeaderSummaryRequestSucceeds() {
        then().statusCode(200);
    }

    /**
     * Asserts the response body contains the expected seeded data and mapped types.
     */
    @Then("the major creditor account header summary matches the documented contract")
    public void majorCreditorAccountHeaderSummaryMatchesDocumentedContract() {
        then()
            .body("major_creditor.creditor_account_id", equalTo(10770000000041L))
            .body("major_creditor.account_number", equalTo("00001235G"))
            .body("major_creditor.name", equalTo("TFL2 ATCM Testing"))
            .body("major_creditor.account_reference.account_type", equalTo("MJ"))
            .body("major_creditor.account_reference.display_name", equalTo("Major Creditor"))
            .body("business_unit_details.business_unit_id", equalTo("77"))
            .body("business_unit_details.business_unit_name", equalTo("Camberwell Green"))
            .body("business_unit_details.welsh_speaking", equalTo("N"));

        Number awaitingPayout = then().extract().jsonPath().get("awaiting_payout");
        assertThat(new BigDecimal(awaitingPayout.toString()), comparesEqualTo(BigDecimal.ZERO));
    }

    /**
     * Asserts the response body has no undocumented top-level or nested fields.
     */
    @Then("the major creditor account header summary response contains only documented fields")
    public void majorCreditorAccountHeaderSummaryResponseContainsOnlyDocumentedFields() {
        Map<String, Object> response = then().extract().jsonPath().getMap("");
        assertEquals(TOP_LEVEL_FIELDS, response.keySet());

        Map<String, Object> majorCreditor = then().extract().jsonPath().getMap("major_creditor");
        assertEquals(MAJOR_CREDITOR_FIELDS, majorCreditor.keySet());

        Map<String, Object> accountReference = then().extract().jsonPath()
            .getMap("major_creditor.account_reference");
        assertEquals(ACCOUNT_REFERENCE_FIELDS, accountReference.keySet());

        Map<String, Object> businessUnitDetails = then().extract().jsonPath()
            .getMap("business_unit_details");
        assertEquals(BUSINESS_UNIT_FIELDS, businessUnitDetails.keySet());
    }

    /**
     * Asserts the response ETag matches the supplied value.
     *
     * @param expectedVersion expected account version in the ETag header.
     */
    @Then("the major creditor account header summary ETag is {string}")
    public void majorCreditorAccountHeaderSummaryEtagIs(String expectedVersion) {
        then().header(HttpHeaders.ETAG, "\"" + expectedVersion + "\"");
    }

    /**
     * Asserts repeated responses for the same account are stable.
     */
    @Then("the repeated major creditor account header summary responses are identical")
    public void repeatedMajorCreditorAccountHeaderSummaryResponsesAreIdentical() {
        assertEquals(200, firstResponse.statusCode());
        assertEquals(200, secondResponse.statusCode());
        assertEquals(firstResponse.getBody().asString(), secondResponse.getBody().asString());
        assertEquals(firstResponse.getHeader(HttpHeaders.ETAG), secondResponse.getHeader(HttpHeaders.ETAG));
    }

    private Response getHeaderSummary(String token, long accountId) {
        return given()
            .accept("*/*")
            .contentType("application/json")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .when()
            .get(getTestUrl() + HEADER_SUMMARY_PATH.formatted(accountId));
    }

    private void assertHeaderSummaryResponseIsReturnedAsDocumented() {
        majorCreditorAccountHeaderSummaryRequestSucceeds();
        majorCreditorAccountHeaderSummaryMatchesDocumentedContract();
        majorCreditorAccountHeaderSummaryResponseContainsOnlyDocumentedFields();
        majorCreditorAccountHeaderSummaryEtagIs("1");
    }
}
