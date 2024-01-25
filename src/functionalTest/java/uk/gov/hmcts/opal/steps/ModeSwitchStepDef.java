package uk.gov.hmcts.opal.steps;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import static net.serenitybdd.rest.SerenityRest.then;

public class ModeSwitchStepDef extends BaseStepDef {
    @BeforeAll
    public static void beforeAll() throws JSONException {
        String testMode = System.getProperty("test.mode");

         if(Objects.equals(testMode, "opal")){
             JSONObject requestBody = new JSONObject();
             requestBody.put("mode", "opal");

             System.out.println("Test mode equals "+ testMode +" setting to: opal");
             SerenityRest.given()
                 .accept("*/*")
                 .contentType("application/json")
                 .body(requestBody.toString())
                 .when()
                 .put(getTestUrl() + "/api/testing-support/app-mode");
         }
        if(Objects.equals(testMode, "legacy")){
            JSONObject requestBody = new JSONObject();
            requestBody.put("mode", "legacy");

            System.out.println("Test mode equals "+ testMode +" setting to: legacy");
            SerenityRest.given()
                .accept("*/*")
                .contentType("application/json")
                .body(requestBody.toString())
                .when()
                .put(getTestUrl() + "/api/testing-support/app-mode");
        }
         }

    @Given("this test is ran in {string} mode")
    public void runTestAgainstMode(String mode) throws JSONException {
        switch (mode) {
            case "opal":
                if (!Objects.equals(getMode(), "opal")) {
                    toggleMode(mode);
                } else {
                    System.out.println("mode is already: " + mode);
                }
                break;
            case "legacy":
                if (!Objects.equals(getMode(), "legacy")) {
                    toggleMode(mode);
                } else {
                    System.out.println("mode is already: " + mode);
                }
                break;
        }
    }

    public String getMode() {
        SerenityRest.given()
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + "/api/testing-support/app-mode");
        String mode = then().extract().jsonPath().getString("mode");
        System.out.println("The mode is :" + mode);
        return mode;
    }

    public void toggleMode(String toggleTo) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", toggleTo);

        System.out.println("Toggling mode to: " + toggleTo);
        SerenityRest.given()
            .accept("*/*")
            .contentType("application/json")
            .body(requestBody.toString())
            .when()
            .put(getTestUrl() + "/api/testing-support/app-mode");
    }

    @AfterAll
    public static void afterAll() throws JSONException {

        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "opal");

        System.out.println("All tests ran setting back to: opal");
        SerenityRest.given()
            .accept("*/*")
            .contentType("application/json")
            .body(requestBody.toString())
            .when()
            .put(getTestUrl() + "/api/testing-support/app-mode");
    }

}
