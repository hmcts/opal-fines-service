package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.LegacyDefendantsIntegrationTest01")
class LegacyDefendantsIntegrationTest01 extends CommonDefendantsIntegrationTest01 {

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetHeaderSummaryInd() throws Exception {
        super.getHeaderSummary_Individual(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetHeaderSummaryOrg() throws Exception {
        super.getHeaderSummary_Organisation(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetDefendantAccountsPaymentTerms_500Error() throws Exception {
        super.getDefendantAccountPaymentTerms_500Error(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetDefendantAccountsAtAGlance_500Error() throws Exception {
        super.getDefendantAccountAtAGlance_500Error(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetEnforcementStatus() throws Exception {
        super.testGetEnforcementStatus(log, true);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetEnforcementStatus_missingAuth_returns401() throws Exception {
        super.testGetEnforcementStatus_missingAuthHeader_returns401(log, true);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetEnforcementStatus_forbidden_returns403() throws Exception {
        super.testGetEnforcementStatus_forbidden(log, true);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetEnforcementStatus_timeout_returns408() throws Exception {
        super.testGetEnforcementStatus_timeout(log, true);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetEnforcementStatus_serviceUnavailable_returns503() throws Exception {
        super.testGetEnforcementStatus_serviceUnavailable(log, true);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetEnforcementStatus_serverError_returns500() throws Exception {
        super.testGetEnforcementStatus_serverError(log, true);
    }

}
