package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"integration", "legacy"})
@Slf4j(topic = "opal.LegacyDefendantAccountsIntegrationTest")
class LegacyDefendantAccountsIntegrationTest extends DefendantAccountsControllerIntegrationTest {

    @Disabled("See DTSPO-27066. A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetHeaderSummary() throws Exception {
        super.getHeaderSummaryImpl(log);
    }

    @Disabled("See DTSPO-27066. A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetHeaderSummary_500Error() throws Exception {
        super.getHeaderSummaryImpl_500Error(log);
    }

    @Disabled("See DTSPO-27066. A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testSearchDefendantAccounts() throws Exception {
        super.testPostDefendantAccountsSearch(log);
    }

    @Disabled("See DTSPO-27066. A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testSearchDefendantAccount_NoAccountsFound() throws Exception {
        super.testPostDefendantAccountsSearch_WhenNoDefendantAccountsFound(log);
    }
}
