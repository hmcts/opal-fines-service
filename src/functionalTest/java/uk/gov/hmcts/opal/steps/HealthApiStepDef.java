package uk.gov.hmcts.opal.steps;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import net.serenitybdd.rest.SerenityRest;
import org.hamcrest.Matchers;

public class HealthApiStepDef extends BaseStepDef {

    @Then("I check the health of the fines api")
    public void checkHealthOfFinesApi() {
        System.out.println("Test URL: " + getTestUrl());
        SerenityRest.when().get(getTestUrl() + "/health");
        SerenityRest.then().assertThat().statusCode(200).and().body("status", Matchers.is("UP"));
    }

    @And("this test is todo")
    public void thisStepIsTodo() {
        //TODO step is todo
        throw new PendingException();
    }
}
