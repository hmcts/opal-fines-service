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
    void testGetHeaderSummary() throws Exception {
        super.getHeaderSummaryImpl(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testGetHeaderSummary_500Error() throws Exception {
        super.getHeaderSummaryImpl_500Error(log);
    }
}
