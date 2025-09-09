package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import lombok.extern.slf4j.Slf4j;

@ActiveProfiles({"integration", "legacy"})
@Slf4j(topic = "opal.LegacyDefendantAccountsIntegrationTest")
class LegacyDefendantAccountsIntegrationTest extends DefendantAccountsControllerIntegrationTest {

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetHeaderSummary() throws Exception {
        super.getHeaderSummaryImpl(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetHeaderSummary_500Error() throws Exception {
        super.getHeaderSummaryImpl_500Error(log);
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

    @Test
    void testGetDefendantAccountsPaymentTerms_SUCCESS() throws Exception {
        super.testGetPaymentTerms(log);
    }

    @Test
    void testGetDefendantAccountsPaymentTerms_500Error() throws Exception {
        super.getDefendantAccountPaymentTerms_500Error(log);
    }

    @Override
    String getHeaderSummaryResponseSchemaLocation() {
        return "legacy/getDefendantAccountHeaderSummaryLegacyResponse.json";
    }

    @Override
    String getPaymentTermsResponseSchemaLocation() {
        return "legacy/getDefendantAccountPaymentTermsLegacyResponse.json";
    }
}
