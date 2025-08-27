package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"integration", "legacy"})
@Slf4j(topic = "opal.LegacyDefendantAccountsIntegrationTest")
class LegacyDefendantAccountsIntegrationTest extends DefendantAccountsControllerIntegrationTest {

    @Test
    void testGetHeaderSummary() throws Exception {
        super.getHeaderSummaryImpl(log);
    }

    @Test
    void testGetHeaderSummary_500Error() throws Exception {
        super.getHeaderSummaryImpl_500Error(log);
    }

    @Test
    void testSearchDefendantAccounts() throws Exception {
        super.testPostDefendantAccountsSearch(log);
    }

    @Test
    void testSearchDefendantAccount_NoAccountsFound() throws Exception {
        super.testPostDefendantAccountsSearch_WhenNoDefendantAccountsFound(log);
    }

    @Override
    String getHeaderSummaryResponseSchemaLocation() {
        return "legacy/getDefendantAccountHeaderSummaryLegacyResponse.json";
    }
}
