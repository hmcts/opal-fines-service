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

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testPutReplaceDefendantAccountParty_Success() throws Exception {
        super.legacyPutReplaceDefendantAccountParty_Success(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testPutReplaceDefendantAccountParty_Error() throws Exception {
        super.legacyPutReplaceDefendantAccountParty_500Error(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testUpdateDefendantAccount_CommentNotes_Success() throws Exception {
        super.test_Legacy_UpdateDefendantAccount_CommentsNotes_Success(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testUpdateDefendantAccount_CommentsNotes_500Error() throws Exception {
        super.test_Legacy_UpdateDefendantAccount_CommentNotes_500Error(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testUpdateDefendantAccount_CommentNotes_401Unauthorized() throws Exception {
        super.test_Legacy_UpdateDefendantAccount_CommentNotes_401Unauthorized(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testUpdateDefendantAccount_CommentNotes_403Forbidden() throws Exception {
        super.test_Legacy_UpdateDefendantAccount_CommentNotes_403Forbidden(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testUpdateDefendantAccount_CommentNotes_400BadRequest() throws Exception {
        super.test_Legacy_UpdateDefendantAccount_CommentNotes_400BadRequest(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testLegacyPostAddEnforcement_Success() throws Exception {
        super.legacyPostAddEnforcement_Success(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testLegacyPostAddEnforcement_403Forbidden() throws Exception {
        super.legacyPostAddEnforcement_403Forbidden(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testLegacyPostAddEnforcement_401Unauthorized() throws Exception {
        super.legacyPostAddEnforcement_401Unauthorized(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testLegacyPostAddEnforcement_500Error() throws Exception {
        super.legacyPostAddEnforcement_500Error(log);
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

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void legacyAddPaymentCardRequest_Success() throws Exception {
        super.legacyAddPaymentCardRequest_Happy(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void addPaymentCardRequest_Legacy_500() throws Exception {
        super.legacyAddPaymentCardRequest_500(log);
    }
}
