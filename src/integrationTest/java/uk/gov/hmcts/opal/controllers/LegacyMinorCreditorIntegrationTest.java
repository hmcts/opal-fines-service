package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"integration", "legacy"})
@Slf4j(topic = "opal.LegacyDefendantAccountsIntegrationTest")
public class LegacyMinorCreditorIntegrationTest extends MinorCreditorControllerIntegrationTest {

    @Disabled
    @DisplayName("Disabled pending fix — see defect ticket PO-2418")
    @Test
    void testPostSearchMinorCreditorSuccess() throws Exception {
        super.postSearchMinorCreditorImpl_Success(log);
    }

    @Test
    void testPostSearchMinorCreditor_500Error() throws Exception {
        super.legacyPostSearchMinorCreditorImpl_500Error(log);
    }

}
