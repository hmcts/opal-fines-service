package uk.gov.hmcts.opal.steps;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.hamcrest.Matchers;

//import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

@Slf4j
public class HealthApiStepDef extends BaseStepDef {

    @Then("I check the health of the fines api")
    public void checkHealthOfFinesApi() {
        log.info("Test URL: {}", getTestUrl());
        SerenityRest.given()
            //.header("Authorization", "Bearer " + getToken())
            .when()
            .get(getTestUrl() + "/health");
        SerenityRest.then().assertThat().statusCode(200).and().body("status", Matchers.is("UP"));
    }

    @And("this test is todo")
    public void thisStepIsTodo() {
        //TODO step is todo
        throw new PendingException();
    }
}
