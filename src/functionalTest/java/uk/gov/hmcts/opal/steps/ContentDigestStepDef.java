package uk.gov.hmcts.opal.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.opal.utils.ContentDigestUtils.contentDigestHeaderForEmptyBody;
import static uk.gov.hmcts.opal.utils.ContentDigestUtils.invalidContentDigestHeader;
import static uk.gov.hmcts.opal.utils.ContentDigestUtils.malformedContentDigestHeader;

public class ContentDigestStepDef extends BaseStepDef {

    private static final String CONTENT_DIGEST = "Content-Digest";

    @When("I make a content digest request without a Content-Digest header")
    public void getRootWithoutContentDigestHeader() {
        getRoot(Map.of("Accept", "*/*"));
    }

    @When("I make a content digest request with a valid Content-Digest header")
    public void getRootWithValidContentDigestHeader() {
        getRoot(Map.of("Accept", "*/*", CONTENT_DIGEST, contentDigestHeaderForEmptyBody()));
    }

    @When("I make a content digest request with an invalid Content-Digest header")
    public void getRootWithInvalidContentDigestHeader() {
        getRoot(Map.of("Accept", "*/*", CONTENT_DIGEST, invalidContentDigestHeader()));
    }

    @When("I make a content digest request with a malformed Content-Digest header")
    public void getRootWithMalformedContentDigestHeader() {
        getRoot(Map.of("Accept", "*/*", CONTENT_DIGEST, malformedContentDigestHeader()));
    }

    @Then("The content digest response returns {int}")
    public void contentDigestResponseReturns(int statusCode) {
        then()
            .log().ifValidationFails()
            .statusCode(statusCode);
    }

    @Then("The content digest response does not contain a Content-Digest header")
    public void responseDoesNotContainContentDigestHeader() {
        assertThat(SerenityRest.lastResponse().getHeader(CONTENT_DIGEST)).isNull();
    }

    @Then("The content digest response contains the following")
    public void responseContainsTheFollowing(DataTable data) {
        Map<String, String> expectedData = data.asMap(String.class, String.class);

        expectedData.forEach((field, expected) ->
            assertEquals(expected, SerenityRest.lastResponse().jsonPath().getString(field),
                "Unexpected response field '" + field + "'"));
    }

    private static void getRoot(Map<String, String> headers) {
        RequestSpecification request = SerenityRest.given();
        headers.forEach(request::header);
        request.when().get(getTestUrl() + "/");
    }

}
