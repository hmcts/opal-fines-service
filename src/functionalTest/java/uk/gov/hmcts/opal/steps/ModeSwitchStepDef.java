package uk.gov.hmcts.opal.steps;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class ModeSwitchStepDef extends BaseStepDef {
    static Logger log = LoggerFactory.getLogger(ModeSwitchStepDef.class.getName());

    @BeforeAll
    public static void beforeAll() throws JSONException {
        String testMode = System.getProperty("test.mode");

        if (Objects.equals(testMode, "opal")) {
            JSONObject requestBody = new JSONObject();
            requestBody.put("mode", "opal");

            log.info("Test mode equals " + testMode + " setting to: opal");
            updateAppMode(requestBody);
        }
        if (Objects.equals(testMode, "legacy")) {
            JSONObject requestBody = new JSONObject();
            requestBody.put("mode", "legacy");

            log.info("Test mode equals " + testMode + " setting to: legacy");
            updateAppMode(requestBody);
        }
    }

    @AfterAll
    public static void afterAll() throws JSONException {

        JSONObject requestBody = new JSONObject();
        requestBody.put("mode", "opal");

        log.info("All tests ran setting back to: opal");
        updateAppMode(requestBody);
    }

    private static void updateAppMode(JSONObject requestBody) {
        SerenityRest.given()
            .accept("*/*")
            .contentType("application/json")
            .body(requestBody.toString())
            .when()
            .put(getTestUrl() + "testing-support/app-mode");
    }

}
