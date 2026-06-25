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

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.LegacyDefendantsIntegrationTest01")
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1b=true"
})
public class LegacyNotesIntegrationTest extends NotesIntegrationTest {

    @Test
    @JiraStory("PO-1566")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-5958")
    void testPostAddNotesSuccess() throws Exception {
        super.legacyTestAddNoteSuccess(log);
    }

    @Test
    @JiraStory("PO-1566")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-5957")
    void testSearchDefendantAccount_NoAccountsFound() throws Exception {
        super.legacyTestAddNote500Error(log);
    }

}
