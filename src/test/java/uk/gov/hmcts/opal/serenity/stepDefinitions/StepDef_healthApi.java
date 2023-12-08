package uk.gov.hmcts.opal.serenity.stepDefinitions;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import net.serenitybdd.rest.SerenityRest;
import org.hamcrest.Matchers;


public class StepDef_healthApi {

    String testURL = System.getenv("TEST_URL");

    public String ifUrlNullSetLocal(){
        if(testURL == null){
            testURL = "http://localhost:4550";
            System.out.println("Set to: " + testURL);
        }
        else return testURL;
        return testURL;
    }

    @Then("I check the health of the fines api")
    public void checkHealthOfFinesApi(){
        System.out.println("Test URL: " + ifUrlNullSetLocal());
        SerenityRest.when().get(ifUrlNullSetLocal() + "/health");
        SerenityRest.then().assertThat().statusCode(200).and().body("status", Matchers.is("UP"));
    }

    @And("this test is todo")
    public void thisStepIsTodo() {
        //TODO step is todo
        throw new PendingException();
    }
}
