package uk.gov.hmcts.opal.steps;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import uk.gov.hmcts.opal.utils.TestHttpClient;
import uk.gov.hmcts.opal.utils.TestHttpClient.TestHttpResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

//import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class HealthApiStepDef extends BaseStepDef {

    @Then("I check the health of the fines api")
    public void checkHealthOfFinesApi() {
        System.out.println("Test URL: " + getTestUrl());
        TestHttpResponse response = TestHttpClient.get(getTestUrl() + "/health", Map.of());
        assertEquals(200, response.statusCode());
        assertEquals("UP", response.jsonPath("status"));
    }

    @And("this test is todo")
    public void thisStepIsTodo() {
        //TODO step is todo
        throw new PendingException();
    }
}
