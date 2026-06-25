package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@Slf4j(topic = "opal.OpalDefendantEnforcementIntegrationTest")
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1b=true"
})
public class OpalDefendantEnforcementIntegrationTest extends DefendantEnforcementIntegrationTest {

    @Test
    @JiraStory("PO-1774")
    @JiraEpic("PO-1675")
    @JiraTestKey("PO-5998")
    public void testAddEnforcement_whenGivenAllFields_addsEnforcement() throws Exception {
        super.postEnforcementImpl_fullRequest_Success(log);
    }

    @Test
    @JiraStory("PO-1774")
    @JiraEpic("PO-1675")
    @JiraTestKey("PO-5999")
    public void testAddEnforcement_whenGivenMinimumFields_addsEnforcement() throws Exception {
        super.postEnforcementImpl_minimumRequest_Success(log);
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
    @JiraTestKey("PO-8269")
    public void testAddEnforcement_whenGivenColloWithPaymentTerms_preservesLastEnforcementAndReturnsResponses()
        throws Exception {
        super.postEnforcementImpl_colloWithPaymentTerms_preservesLastEnforcementAndReturnsResponses(log);
    }
}
