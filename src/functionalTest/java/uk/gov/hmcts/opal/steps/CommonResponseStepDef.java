package uk.gov.hmcts.opal.steps;

import io.cucumber.java.en.Then;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Defines generic response assertions that can be shared across multiple functional-test API
 * areas.
 */
public class CommonResponseStepDef extends BaseStepDef {

    /**
     * Asserts that the latest API or raw-client response returned the expected HTTP status code.
     *
     * @param statusCode expected HTTP status code.
     */
    @Then("the response status code is {int}")
    public void responseStatusCodeIs(int statusCode) {
        assertLatestResponseStatus(statusCode);
    }

    /**
     * Asserts that the latest API or raw-client response returned the expected HTTP status code.
     *
     * @param statusCode expected HTTP status code.
     */
    @Then("the response status is {int}")
    public void responseStatusIs(int statusCode) {
        assertLatestResponseStatus(statusCode);
    }

    /**
     * Asserts that the latest request succeeded with HTTP 200.
     */
    @Then("the request succeeds")
    public void requestSucceeds() {
        assertLatestResponseStatus(200);
    }

    /**
     * Asserts that the latest request created a resource with HTTP 201.
     */
    @Then("the request creates a resource")
    public void requestCreatesAResource() {
        assertLatestResponseStatus(201);
    }

    /**
     * Asserts that the latest request failed with HTTP 401.
     */
    @Then("the request is rejected as unauthorized")
    public void requestIsRejectedAsUnauthorized() {
        assertLatestResponseStatus(401);
    }

    /**
     * Asserts that the latest request failed with HTTP 403.
     */
    @Then("the request is rejected as forbidden")
    public void requestIsRejectedAsForbidden() {
        assertLatestResponseStatus(403);
    }

    /**
     * Asserts that the latest request failed with the supplied HTTP status code.
     *
     * @param status expected HTTP status code.
     */
    @Then("the request is rejected with status {int}")
    public void requestIsRejectedWithStatus(int status) {
        assertLatestResponseStatus(status);
    }

    /**
     * Asserts that the latest request failed with HTTP 400.
     */
    @Then("the request is rejected as bad request")
    public void requestIsRejectedAsBadRequest() {
        assertLatestResponseStatus(400);
    }

    /**
     * Asserts that the latest request failed with HTTP 404.
     */
    @Then("the request is rejected as not found")
    public void requestIsRejectedAsNotFound() {
        assertLatestResponseStatus(404);
    }

    /**
     * Asserts that the latest request failed with HTTP 406.
     */
    @Then("the request is rejected as not acceptable")
    public void requestIsRejectedAsNotAcceptable() {
        assertLatestResponseStatus(406);
    }

    /**
     * Asserts that the latest request failed with HTTP 409.
     */
    @Then("the request is rejected as conflict")
    public void requestIsRejectedAsConflict() {
        assertLatestResponseStatus(409);
    }

    /**
     * Asserts that the latest request failed with HTTP 415.
     */
    @Then("the request is rejected as unsupported media type")
    public void requestIsRejectedAsUnsupportedMediaType() {
        assertLatestResponseStatus(415);
    }

    /**
     * Asserts that the latest request failed with HTTP 500.
     */
    @Then("the request fails with an internal server error")
    public void requestFailsWithInternalServerError() {
        assertLatestResponseStatus(500);
    }

    /**
     * Validates the status code from the latest Serenity or raw-client response.
     *
     * @param statusCode expected HTTP status code.
     */
    private void assertLatestResponseStatus(int statusCode) {
        TestHttpResponse httpResponse = scenarioContext().consumeLatestHttpResponse();
        if (httpResponse != null) {
            assertEquals(statusCode, httpResponse.statusCode(), "Unexpected HTTP status");
            return;
        }

        then()
            .log().ifValidationFails()
            .statusCode(statusCode);
    }
}
