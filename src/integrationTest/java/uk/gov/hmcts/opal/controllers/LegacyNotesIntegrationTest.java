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
@Slf4j(topic = "opal.LegacyDefendantsIntegrationTest01")
public class LegacyNotesIntegrationTest extends NotesIntegrationTest {

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testPostAddNotesSuccess() throws Exception {
        super.legacyTestAddNoteSuccess(log);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    void testSearchDefendantAccount_NoAccountsFound() throws Exception {
        super.legacyTestAddNote500Error(log);
    }

}
