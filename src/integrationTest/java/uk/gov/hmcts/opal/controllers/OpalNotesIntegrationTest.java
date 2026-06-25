package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.OpalNotesIntegrationTest")
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1b=true"
})
public class OpalNotesIntegrationTest extends NotesIntegrationTest {

    @Test
    @JiraStory("PO-1566")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6226")
    void testOpalNotes_Success() throws Exception {
        super.postNotesImpl(log);
    }

    @Test
    @JiraStory("PO-1566")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6227")
    void testOpalNotes_500Error() throws Exception {
        super.postNotes_IDNotFoundError(log);
    }

    @Test
    @JiraStory("PO-1566")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6225")
    void testOpalNotes_Forbidden() throws Exception {
        super.postNotes_UserWithoutPermission(log);
    }
}
