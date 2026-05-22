package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"integration", "legacy"})
@Slf4j(topic = "opal.LegacyDefendantsIntegrationTest01")
public class LegacyMinorCreditorIntegrationTest extends MinorCreditorControllerIntegrationTest {

    @Test
    void testPostSearchMinorCreditorSuccess() throws Exception {
        super.postSearchMinorCreditorImpl_Success(log);
    }

    @Test
    void testPostSearchMinorCreditor_500Error() throws Exception {
        super.legacyPostSearchMinorCreditorImpl_500Error(log);
    }

    @Test
    void testGetMinorCreditorAtAGlanceSuccess() throws Exception {
        super.getMinorCreditorAtAGlanceImpl_Success(log);
    }

    @Test
    void testGetMinorCreditorAtAGlance_500Error() throws Exception {
        super.legacyGetMinorCreditorAtAGlanceImpl_500Error(log);
    }

    @Test
    void testGetMinorCreditorHeaderSummarySuccess() throws Exception {
        super.getHeaderSummaryImpl_Success(log);
    }

    @Test
    void testGetMinorCreditorHeaderSummary_500Error() throws Exception {
        super.legacyGetMinorCreditorHeaderSummaryImpl_500Error(log);
    }

    @Test
    void testGetMinorCreditorAccountSuccess() throws Exception {
        super.getMinorCreditorAccountImpl_Success(log);
    }

    @Test
    void testGetMinorCreditorAccountFiltersBacsWithoutPermission() throws Exception {
        super.getMinorCreditorAccountImpl_filtersBacsDetailsWithoutPermission(log);
    }

    @Test
    void testGetMinorCreditorAccountMissingAuthHeaderReturns401() throws Exception {
        super.getMinorCreditorAccount_missingAuthHeader_returns401();
    }

    @Test
    void testGetMinorCreditorAccountAuthenticatedWithoutPermissionReturns403() throws Exception {
        super.getMinorCreditorAccount_authenticatedWithoutPermission_returns403();
    }

    @Test
    void testGetMinorCreditorAccount_500Error() throws Exception {
        super.legacyGetMinorCreditorAccountImpl_500Error(log);
    }
}
