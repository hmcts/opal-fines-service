package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"integration", "legacy"})
@Slf4j(topic = "opal.LegacyDefendantAccountsIntegrationTest")
class LegacyDefendantAccountsIntegrationTest extends DefendantAccountsControllerIntegrationTest {

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
    void testGetHeaderSummary_500Error() throws Exception {
        super.getHeaderSummary_Legacy_500(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testSearchDefendantAccounts() throws Exception {
        super.testPostDefendantAccountsSearch(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testSearchDefendantAccount_NoAccountsFound() throws Exception {
        super.testPostDefendantAccountsSearch_WhenNoDefendantAccountsFound(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetDefendantAccountsPaymentTerms_Success() throws Exception {
        super.testLegacyGetPaymentTerms(log);
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
    void testGetDefendantAccountsAtAGlance_Success() throws Exception {
        super.testLegacyGetDefendantAtAGlance(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetDefendantAccountParty_Success() throws Exception {
        super.legacyGetDefendantAccountParty_Happy(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetDefendantAccountParty_Organisation() throws Exception {
        super.legacyGetDefendantAccountParty_Organisation(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetDefendantAccountParty_500Error() throws Exception {
        super.legacyGetDefendantAccountParty_500Error(log);
    }
}
