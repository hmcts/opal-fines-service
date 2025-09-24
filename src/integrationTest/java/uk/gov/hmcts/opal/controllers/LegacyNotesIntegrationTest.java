package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"integration", "legacy"})
@Slf4j(topic = "opal.LegacyDefendantAccountsIntegrationTest")
public class LegacyNotesIntegrationTest extends NotesIntegrationTest {

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testPostAddNotesSuccess() throws Exception {
        super.LegacyTestAddNoteSuccess(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testSearchDefendantAccount_NoAccountsFound() throws Exception {
        super.LegacyTestAddNote500Error(log);
    }

}
