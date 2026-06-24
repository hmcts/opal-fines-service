package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@Sql(scripts = "classpath:db/insertData/insert_into_enforcements.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_enforcements.sql", executionPhase = AFTER_TEST_METHOD)
@Slf4j(topic = "opal.OpalDefendantEnforcementIntegrationTest")
public class OpalDefendantEnforcementIntegrationTest extends DefendantEnforcementIntegrationTest {

    @Test
    @JiraStory("PO-5757")
    @JiraEpic("PO-2990")
    public void testAddEnforcement_withFullRequestAndBlockedAccountControls_returns422AndRollsBack() throws Exception {
        super.postEnforcementImpl_fullRequest_blockedByAccountControls(log);
    }

    @Test
    @JiraStory("PO-5757")
    @JiraEpic("PO-2990")
    public void testAddEnforcement_withMinimumRequestAndBlockedAccountControls_returns422AndRollsBack()
        throws Exception {
        super.postEnforcementImpl_minimumRequest_blockedByAccountControls(log);
    }

    @Test
    @JiraStory("PO-1774")
    @JiraEpic("PO-1675")
    @JiraTestKey("PO-5997")
    public void testAddEnforcement_whenGivenInvalidDefendant_Fails() throws Exception {
        super.postEnforcementImpl_invalidDefendant_Failure(log);
    }

    @Test
    @JiraStory("PO-7193")
    @JiraEpic("PO-1675")
    public void testAddEnforcement_whenGivenColloWithPaymentTerms_preservesLastEnforcementAndReturnsResponses()
        throws Exception {
        super.postEnforcementImpl_colloWithPaymentTerms_preservesLastEnforcementAndReturnsResponses(log);
    }
}
