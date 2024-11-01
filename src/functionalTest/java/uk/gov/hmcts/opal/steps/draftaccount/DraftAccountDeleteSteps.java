package uk.gov.hmcts.opal.steps.draftaccount;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.opal.steps.BaseStepDef;
import uk.gov.hmcts.opal.utils.DraftAccountUtils;

import java.util.ArrayList;

import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;

public class DraftAccountDeleteSteps extends BaseStepDef {
    static Logger log = LoggerFactory.getLogger(DraftAccountDeleteSteps.class.getName());

    @When("I delete the draft account {string}")
    public static void deleteDraftAccount(String draftAccountId) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .delete(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId + "?ignore_missing=true");
    }

    @Then("I delete the created draft accounts")
    public static void deleteAllCreatedDraftAccounts() {
        ArrayList<String> accounts = DraftAccountUtils.getAllDraftAccountIds();
        for (String account : accounts) {
            log.info("Deleting draft account: {}", account);
            deleteDraftAccount(account);
        }
    }

}
