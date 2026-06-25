package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.OpalDefendantsCommonIntegrationTest")
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1b=true"
})
class OpalDefendantsCommonIntegrationTest extends AbstractCommonDefendantsIntegrationTest {

    @Test
    @JiraStory("PO-2287")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6005")
    void testGetHeaderSummaryInd() throws Exception {
        super.getHeaderSummary_Individual(log);
    }

    @Test
    @JiraStory("PO-2287")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6006")
    void testGetHeaderSummaryOrg() throws Exception {
        super.getHeaderSummary_Organisation(log);
    }

    @Test
    @JiraStory("PO-2297")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6004")
    void testGetHeaderSummary_PO2297_Individual_UsesDefendantAccountPartyId() throws Exception {
        super.testGetHeaderSummary_Individual_UsesDefendantAccountPartyId(log);
    }

    @Test
    @JiraStory("PO-2297")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6015")
    void testGetHeaderSummary_PO2297_Organisation_UsesDefendantAccountPartyId() throws Exception {
        super.testGetHeaderSummary_Organisation_UsesDefendantAccountPartyId(log);
    }


    @Test
    @JiraStory("PO-2287")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6013")
    void get_header_summary_throws_not_found() throws Exception {
        super.testGetHeaderSummary_ThrowsNotFound(log);
    }

    @Test
    @JiraStory("PO-1716")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-6011")
    void testGetPaymentTermsLatest_Success() throws Exception {
        super.testGetPaymentTerms(log);
    }

    @Test
    @JiraStory("PO-1716")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-6009")
    void testGetPaymentTermsLatest_NoPaymentTermFoundForId() throws Exception {
        super.testGetPaymentTermsLatest_NoPaymentTermFoundForId(log);
    }

    @Test
    @JiraStory("PO-2119")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6000")
    void opal_exceptionContainsRetriableField() throws Exception {
        super.testEntityNotFoundExceptionContainsRetriable(log);
    }

    @Test
    @JiraStory("PO-2119")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6014")
    void opal_wrongMediaTypeContainsRetriableField() throws Exception {
        super.testWrongMediaTypeContainsRetriableField(log);
    }

    @Test
    @JiraStory("PO-2119")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6003")
    void testInvalidBodyContainsRetriable() throws Exception {
        super.testInvalidBodyContainsRetriable(log);
    }

    @Test
    @JiraStory("PO-1696")
    @JiraEpic("PO-1675")
    @JiraTestKey("PO-6010")
    void testGetEnforcementStatus() throws Exception {
        super.testGetEnforcementStatus(log, false);
    }

    @Test
    @JiraStory("PO-1696")
    @JiraEpic("PO-1675")
    @JiraTestKey("PO-6016")
    void testGetEnforcementStatus_forbidden_returns403() throws Exception {
        super.testGetEnforcementStatus_forbidden();
    }

    @Test
    @JiraStory("PO-1696")
    @JiraEpic("PO-1675")
    @JiraTestKey("PO-6007")
    void testGetEnforcementStatus_notFound_returns404() throws Exception {
        super.testGetEnforcementStatus_notFound();
    }
}
