package uk.gov.hmcts.opal.controllers.r1b;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1b-1-1=true")
@Sql(scripts = "classpath:db/insertData/insert_into_enforcements.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_enforcements.sql", executionPhase = AFTER_TEST_METHOD)
@Slf4j(topic = "opal.OpalDefendantEnforcementRelease1b11IntegrationTest")
class OpalDefendantEnforcementRelease1b11IntegrationTest extends DefendantEnforcementIntegrationTest {

    @Test
    @JiraStory("PO-10788")
    @JiraEpic("PO-978")
    void addEnforcement_whenRelease1b11Enabled_persistsNewResponseNames() throws Exception {
        super.postEnforcementImpl_release1b11WithNewResponseNames_persistsEnforcementDetails(log);
    }

    @Test
    @JiraStory("PO-10788")
    @JiraEpic("PO-978")
    void addEnforcement_whenRelease1b11EnabledAndPris_mapsEarliestReleaseDate() throws Exception {
        super.postEnforcementImpl_release1b11WithPris_persistsEarliestReleaseDate(log);
    }
}
