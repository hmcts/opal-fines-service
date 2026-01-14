package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import lombok.extern.slf4j.Slf4j;

@ActiveProfiles({"integration", "opal"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.OpalDefendantsIntegrationTest01")
class OpalDefendantsIntegrationTest01 extends CommonDefendantsIntegrationTest01 {

    @Test
    void testGetHeaderSummaryInd() throws Exception {
        super.getHeaderSummary_Individual(log);
    }

    @Test
    void testGetHeaderSummaryOrg() throws Exception {
        super.getHeaderSummary_Organisation(log);
    }

    @Test
    void testGetHeaderSummary_PO2297_Individual_UsesDefendantAccountPartyId() throws Exception {
        super.testGetHeaderSummary_Individual_UsesDefendantAccountPartyId(log);
    }

    @Test
    void testGetHeaderSummary_PO2297_Organisation_UsesDefendantAccountPartyId() throws Exception {
        super.testGetHeaderSummary_Organisation_UsesDefendantAccountPartyId(log);
    }


    @Test
    void get_header_summary_throws_not_found() throws Exception {
        super.testGetHeaderSummary_ThrowsNotFound(log);
    }

    @Test
    void testGetPaymentTermsLatest_Success() throws Exception {
        super.testGetPaymentTerms(log);
    }

    @Test
    void testGetPaymentTermsLatest_NoPaymentTermFoundForId() throws Exception {
        super.testGetPaymentTermsLatest_NoPaymentTermFoundForId(log);
    }

    @Test
    void opal_exceptionContainsRetriableField() throws Exception {
        super.testEntityNotFoundExceptionContainsRetriable(log);
    }

    @Test
    void opal_wrongMediaTypeContainsRetriableField() throws Exception {
        super.testWrongMediaTypeContainsRetriableField(log);
    }

    @Test
    void testInvalidBodyContainsRetriable() throws Exception {
        super.testInvalidBodyContainsRetriable(log);
    }

    @Test
    void testGetEnforcementStatus() throws Exception {
        super.testGetEnforcementStatus(log, false);
    }

    @Test
    void testGetEnforcementStatus_missingAuth_returns401() throws Exception {
        super.testGetEnforcementStatus_missingAuthHeader_returns401(log, false);
    }

    @Test
    void testGetEnforcementStatus_forbidden_returns403() throws Exception {
        super.testGetEnforcementStatus_forbidden(log, false);
    }

    @Test
    void testGetEnforcementStatus_notFound_returns404() throws Exception {
        super.testGetEnforcementStatus_notFound(log, false);
    }

    @Test
    void testGetEnforcementStatus_timeout_returns408() throws Exception {
        super.testGetEnforcementStatus_timeout(log, false);
    }

    @Test
    void testGetEnforcementStatus_serviceUnavailable_returns503() throws Exception {
        super.testGetEnforcementStatus_serviceUnavailable(log, false);
    }

    @Test
    void testGetEnforcementStatus_serverError_returns500() throws Exception {
        super.testGetEnforcementStatus_serverError(log, false);
    }

}
