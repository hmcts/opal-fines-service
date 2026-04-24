package uk.gov.hmcts.opal.steps.defendantaccount;

import io.cucumber.java.en.Then;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.gov.hmcts.opal.config.Constants.DRAFT_ACCOUNTS_URI;
import uk.gov.hmcts.opal.steps.BaseStepDef;

/**
 * Defines Cucumber steps for deleting defendant accounts created during scenarios.
 */
public class DefendantAccountDeleteStep extends BaseStepDef {
    private static final Logger log = LoggerFactory.getLogger(DefendantAccountDeleteStep.class.getName());

    /**
     * Deletes the defendant accounts linked to the draft accounts created in the current scenario.
     */
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

    /**
     * Deletes the supplied defendant account by identifier.
     *
     * @param defendantAccountId defendant-account identifier to use for the request.
     */
    private void deleteDefendantAccount(Long defendantAccountId) {
        authorisedJsonRequest()
            .when()
            .delete(getTestUrl() + "/testing-support/defendant-accounts/" + defendantAccountId);
    }


    /**
     * Collects defendant-account identifiers from the draft accounts created in the current
     * scenario.
     *
     * @return defendant-account identifiers linked to the created draft accounts.
     */
    private List<Long> getDefendantAccountIdsFromDraftAccounts() {
        List<String> draftAccounts = scenarioContext().getDraftAccountIds();
        if (draftAccounts == null || draftAccounts.isEmpty()) {
            return Collections.emptyList();
        }

        return draftAccounts.stream()
            .map(id -> getDefendantAccountIdFromDraftAccount(id))
            .filter(id -> id != null && id > 0)
            .collect(Collectors.toList());
    }

    /**
     * Reads the defendant-account identifier linked to the supplied draft account.
     *
     * @param draftAccountId draft-account identifier to use for the request.
     * @return defendant-account identifier linked to the supplied draft account, or null when none
     *         can be resolved.
     */
    private Long getDefendantAccountIdFromDraftAccount(String draftAccountId) {
        try {
            var response = authorisedJsonRequest()
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
