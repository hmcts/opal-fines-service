package uk.gov.hmcts.opal.steps.offences;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import net.serenitybdd.rest.SerenityRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.steps.BaseStepDef;

import java.util.List;
import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchOffencesResponseStepDef extends BaseStepDef {
    static Logger log = LoggerFactory.getLogger(SearchOffencesResponseStepDef.class.getName());

    @Then("The offence search response returns {int}")
    public void draftAccountResponse(int statusCode) {
        then().assertThat()
            .statusCode(statusCode);
    }

    @Then("the response contains results with a cjs code starting with {string}")
    public void offenceResponseCjsStartsWith(String cjsCode) {

        List<String> cjsCodes = SerenityRest.then().extract().jsonPath().getList("searchData.cjs_code");
        //log.info("CJS Codes: {}", cjsCodes);

        cjsCodes.forEach(code -> {
            //log.info("Checking CJS Code: {}", code);
            //log.info("CJS Code starts with: {}", cjsCode);
            assertTrue(code.startsWith(cjsCode));
        });
    }

    @Then("the offences in the response contain the following data")
    public void offenceResponseContainsData(DataTable expectedData) {
        Map<String, String> expectedDataMap = expectedData.asMap(String.class, String.class);
        int count = SerenityRest.then().extract().jsonPath().getInt("count");

        for (int i = 0; i < count; i++) {
            for (String key : expectedDataMap.keySet()) {
                String actual = SerenityRest.then().extract().path("searchData[" + i + "]." + key);
                assertTrue(actual.contains(expectedDataMap.get(key)
                ), "Values are not equal: " + key + " - " + actual + " - " + expectedDataMap.get(key));
            }
        }
    }
    @Then("there are {int} offences in the response")
    public void offenceResponseContainsCount(int count) {
        int responseCount = then().extract().jsonPath().getInt("count");
        int cjsCodes = SerenityRest.then().extract().jsonPath().getList("searchData.cjs_code").size();
        assertEquals(responseCount, count, "Expected count: " + count + " Actual count: " + responseCount);
        assertEquals(cjsCodes, count, "Expected count: " + count + " Actual count: " + cjsCodes);
    }
}
