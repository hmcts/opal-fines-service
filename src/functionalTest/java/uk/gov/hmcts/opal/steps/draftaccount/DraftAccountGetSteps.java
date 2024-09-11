package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.utils.DraftAccountUtils;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class DraftAccountGetSteps extends BaseStepDef {
    @When("I get the draft account {string}")
    public void getDraftAccount(String draftAccountId) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + "/api/draft-accounts/" + draftAccountId);
    }

    @When("I get the single created draft account and the response contains")
    public void getSingleDraftAccount(DataTable data) {
        assertEquals(
            1,
            DraftAccountUtils.getAllDraftAccountIds().size(),
            "There should be only one draft account but found multiple: " + DraftAccountUtils.getAllDraftAccountIds()
        );
        String draftAccountId = DraftAccountUtils.getAllDraftAccountIds().getFirst();
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .get(getTestUrl() + "/api/draft-accounts/" + draftAccountId);

        Map<String, String> expectedData = data.asMap(String.class, String.class);

        for (String key : expectedData.keySet()) {
            String apiResponseValue = then().extract().body().jsonPath().getString(key);
            assertEquals(expectedData.get(key), apiResponseValue, "Values are not equal : ");
        }
    }
}
