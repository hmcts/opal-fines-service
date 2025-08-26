package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

@ActiveProfiles({"integration", "opal"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Slf4j(topic = "opal.OpalDefendantAccountsIntegrationTest")
class OpalDefendantAccountsIntegrationTest extends DefendantAccountsControllerIntegrationTest {

    @Disabled("Legacy has been implemented. But Opal still to be completed - see ticket PO-985")
    @Test
    void testGetHeaderSummary() throws Exception {
        super.getHeaderSummaryImpl(log);
    }

    @Disabled("Legacy has been implemented. But Opal still to be completed - see ticket PO-985")
    @Test
    void testGetHeaderSummary_500Error() throws Exception {
        super.getHeaderSummaryImpl(log);
    }
}
