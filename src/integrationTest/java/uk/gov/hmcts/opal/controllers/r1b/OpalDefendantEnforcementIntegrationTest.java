package uk.gov.hmcts.opal.controllers.r1b;

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
    @JiraTestKey("PO-9443")
    public void testAddEnforcement_withFullRequestAndBlockedAccountControls_returns422AndRollsBack() throws Exception {
        super.postEnforcementImpl_fullRequest_blockedByAccountControls(log);
    }

    @Test
    @JiraStory("PO-5757")
    @JiraEpic("PO-2990")
    @JiraTestKey("PO-9444")
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
    @JiraStory("PO-5757")
    @JiraEpic("PO-2990")
    public void testAddEnforcement_whenPaymentTermsOmitted_passesValidation() throws Exception {
        super.postEnforcementImpl_whenPaymentTermsIsOmitted_passesValidation();
    }

    @Test
    @JiraStory("PO-5757")
    @JiraEpic("PO-2990")
    public void testAddEnforcement_whenPaymentTermsExplicitlyNull_passesValidation() throws Exception {
        super.postEnforcementImpl_whenPaymentTermsIsExplicitlyNull_passesValidation();
    }

    @Test
    @JiraStory("PO-5757")
    @JiraEpic("PO-2990")
    public void testAddEnforcement_whenDaysInDefaultOmitted_returnsBadRequest() throws Exception {
        super.postEnforcementImpl_whenDaysInDefaultIsOmitted_returnsBadRequest();
    }

    @Test
    @JiraStory("PO-5757")
    @JiraEpic("PO-2990")
    public void testAddEnforcement_whenDaysInDefaultExplicitlyNull_passesValidation() throws Exception {
        super.postEnforcementImpl_whenDaysInDefaultIsExplicitlyNull_passesValidation();
    }

    @Test
    @JiraStory("PO-7193")
    @JiraEpic("PO-1675")
    @JiraTestKey("PO-8269")
    @Sql(scripts = "classpath:db/insertData/insert_into_collo_with_payment_terms.sql",
        executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/deleteData/delete_from_collo_with_payment_terms.sql",
        executionPhase = AFTER_TEST_METHOD)
    public void testAddEnforcement_whenGivenColloWithPaymentTerms_preservesLastEnforcementAndReturnsResponses()
        throws Exception {
        super.postEnforcementImpl_colloWithPaymentTerms_preservesLastEnforcementAndReturnsResponses(log);
    }
}
