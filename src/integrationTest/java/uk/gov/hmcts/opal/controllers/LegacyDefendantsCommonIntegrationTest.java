package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.LegacyDefendantsCommonIntegrationTest")
class LegacyDefendantsCommonIntegrationTest extends AbstractCommonDefendantsIntegrationTest {

    @Test
    @JiraStory("PO-1907")
    @JiraEpic("PO-812")
    void testGetHeaderSummaryInd() throws Exception {
        super.getHeaderSummary_Individual(log);
    }

    @Test
    @JiraStory("PO-1907")
    @JiraEpic("PO-812")
    void testGetHeaderSummaryOrg() throws Exception {
        super.getHeaderSummary_Organisation(log);
    }

    @Test
    @JiraStory("PO-2086")
    @JiraEpic("PO-977")
    void testGetDefendantAccountsPaymentTerms_500Error() throws Exception {
        super.getDefendantAccountPaymentTerms_500Error(log);
    }

    @Test
    @JiraStory("PO-1909")
    @JiraEpic("PO-812")
    void testGetDefendantAccountsAtAGlance_500Error() throws Exception {
        super.getDefendantAccountAtAGlance_500Error(log);
    }

    @Test
    @JiraStory("PO-1696")
    @JiraEpic("PO-1675")
    void testGetEnforcementStatus() throws Exception {
        super.testGetEnforcementStatus(log, true);
    }

    @Test
    @JiraStory("PO-1696")
    @JiraEpic("PO-1675")
    void testGetEnforcementStatus_missingAuth_returns401() throws Exception {
        super.testGetEnforcementStatus_missingAuthHeader_returns401();
    }

    @Test
    @JiraStory("PO-1696")
    @JiraEpic("PO-1675")
    void testGetEnforcementStatus_forbidden_returns403() throws Exception {
        super.testGetEnforcementStatus_forbidden();
    }

    @Test
    @JiraStory("PO-1696")
    @JiraEpic("PO-1675")
    void testGetEnforcementStatus_timeout_returns408() throws Exception {
        super.testGetEnforcementStatus_timeout();
    }

    @Test
    @JiraStory("PO-1696")
    @JiraEpic("PO-1675")
    void testGetEnforcementStatus_serviceUnavailable_returns503() throws Exception {
        super.testGetEnforcementStatus_serviceUnavailable();
    }

    @Test
    @JiraStory("PO-1696")
    @JiraEpic("PO-1675")
    void testGetEnforcementStatus_serverError_returns500() throws Exception {
        super.testGetEnforcementStatus_serverError();
    }

}
