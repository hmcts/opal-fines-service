package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.LegacyDefendantsCommonIntegrationTest")
class LegacyDefendantsCommonIntegrationTest extends AbstractCommonDefendantsIntegrationTest {

    @Test
    @JiraStory("PO-1907")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-5917")
    void testGetHeaderSummaryInd() throws Exception {
        super.getHeaderSummary_Individual(log);
    }

    @Test
    @JiraStory("PO-1907")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-5918")
    void testGetHeaderSummaryOrg() throws Exception {
        super.getHeaderSummary_Organisation(log);
    }

    @Test
    @JiraStory("PO-2086")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-5914")
    void testGetDefendantAccountsPaymentTerms_500Error() throws Exception {
        super.getDefendantAccountPaymentTerms_500Error(log);
    }

    @Test
    @JiraStory("PO-1909")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-5920")
    void testGetDefendantAccountsAtAGlance_500Error() throws Exception {
        super.getDefendantAccountAtAGlance_500Error(log);
    }

    @Test
    @JiraStory("PO-1696")
    @JiraEpic("PO-1675")
    @JiraTestKey("PO-5921")
    void testGetEnforcementStatus() throws Exception {
        super.testGetEnforcementStatus(log, true);
    }

    @Test
    @JiraStory("PO-1696")
    @JiraEpic("PO-1675")
    @JiraTestKey("PO-5923")
    void testGetEnforcementStatus_forbidden_returns403() throws Exception {
        super.testGetEnforcementStatus_forbidden();
    }
}
