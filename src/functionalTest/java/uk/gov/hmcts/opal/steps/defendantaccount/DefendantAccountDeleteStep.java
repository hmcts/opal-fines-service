package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.java.en.Then;
import uk.gov.hmcts.opal.utils.DraftAccountUtils;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.serenitybdd.rest.SerenityRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;
import static uk.gov.hmcts.opal.steps.BearerTokenStepDef.getToken;
import uk.gov.hmcts.opal.steps.BaseStepDef;

public class DefendantAccountDeleteStep extends BaseStepDef {
    private static final Logger log = LoggerFactory.getLogger(DefendantAccountDeleteStep.class.getName());

    @Then("I delete the created defendant accounts")
    public void deleteCreatedDefendantAccounts() {
        List<Long> defendantAccountIds = getDefendantAccountIdsFromDraftAccounts();

        if (!defendantAccountIds.isEmpty()) {
            for (Long defendantAccountId : defendantAccountIds) {
                log.info("Deleting defendant account: {}", defendantAccountId);
                deleteDefendantAccount(defendantAccountId);
            }
        } else {
            log.info("No defendant accounts to delete");
        }
    }

    private void deleteDefendantAccount(Long defendantAccountId) {
        SerenityRest
            .given()
            .header("Authorization", "Bearer " + getToken())
            .accept("*/*")
            .contentType("application/json")
            .when()
            .delete(getTestUrl() + "/testing-support/defendant-accounts/" + defendantAccountId);
    }


    public static List<Long> getDefendantAccountIdsFromDraftAccounts() {
        List<String> draftAccounts = DraftAccountUtils.getAllDraftAccountIds();
        if (draftAccounts == null || draftAccounts.isEmpty()) {
            return Collections.emptyList();
        }

        return draftAccounts.stream()
            .map(id -> getDefendantAccountIdFromDraftAccount(id))
            .filter(id -> id != null && id > 0)
            .collect(Collectors.toList());
    }

    private static Long getDefendantAccountIdFromDraftAccount(String draftAccountId) {
        try {
            var response = SerenityRest
                .given()
                .header("Authorization", "Bearer " + getToken())
                .accept("*/*")
                .when()
                .get(getTestUrl() + DRAFT_ACCOUNTS_URI + "/" + draftAccountId);

            if (response.statusCode() == 200) {
                Object accountIdObj = response.jsonPath().get("account_id");
                if (accountIdObj != null) {
                    log.info("Defendant account ID for draft account {}: {}", draftAccountId, accountIdObj);
                    return Long.valueOf(accountIdObj.toString());
                }
            }
            log.warn("Failed to get defendant account ID from draft account {}: Status code {}",
                     draftAccountId, response.statusCode());
            return null;
        } catch (Exception e) {
            log.error("Error getting defendant account ID for draft account {}: {}",
                      draftAccountId, e.getMessage());
            return null;
        }
    }
}
