package uk.gov.hmcts.opal.controllers.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.OpalNotesIntegrationTest")
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
    void testOpalNotes_NotFound() throws Exception {
        super.postNotes_IDNotFoundError(log);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nonNotesPermissions")
    @JiraStory("PO-1566")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6225")
    @JiraTestKey(value = "PO-9470", name = "CREATE_MANAGE_DRAFT_ACCOUNTS")
    @JiraTestKey(value = "PO-9471", name = "ACCOUNT_ENQUIRY_NOTES")
    @JiraTestKey(value = "PO-9472", name = "ACCOUNT_ENQUIRY")
    @JiraTestKey(value = "PO-9473", name = "COLLECTION_ORDER")
    @JiraTestKey(value = "PO-9474", name = "CHECK_VALIDATE_DRAFT_ACCOUNTS")
    @JiraTestKey(value = "PO-9475", name = "SEARCH_AND_VIEW_ACCOUNTS")
    @JiraTestKey(value = "PO-9476", name = "ACCOUNT_MAINTENANCE")
    @JiraTestKey(value = "PO-9477", name = "AMEND_PAYMENT_TERMS")
    @JiraTestKey(value = "PO-9478", name = "ENTER_ENFORCEMENT")
    @JiraTestKey(value = "PO-9479", name = "VIEW_CREDITOR_BACS")
    @JiraTestKey(value = "PO-9480", name = "CONSOLIDATE")
    @JiraTestKey(value = "PO-9481", name = "ADD_AND_REMOVE_PAYMENT_HOLD")
    @JiraTestKey(value = "PO-9482", name = "PROCESS_AND_ALLOCATE_PAYMENTS")
    @JiraTestKey(value = "PO-9483", name = "AUTO_ENFORCEMENT")
    void testOpalNotes_Forbidden(FinesPermission permission) throws Exception {
        super.postNotes_UserWithoutPermission(permission);
    }

    @Test
    @JiraStory("PO-1566")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-9469")
    void testOpalNotes_BadRequest() throws Exception {
        super.postNotes_badRequest(log);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("businessUnitAuthorizationScenarios")
    @JiraStory("PO-1566")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6228")
    @JiraTestKey(value = "PO-9484", name = "missing Business-Unit-Id header")
    @JiraTestKey(value = "PO-9485", name = "wrong business unit in header")
    @JiraTestKey(value = "PO-9486", name = "permission present in another BU")
    void testOpalNotes_BusinessUnitAuthorization(BusinessUnitAuthorizationScenario scenario) throws Exception {
        super.postNotes_BusinessUnitAuthorization(scenario);
    }
}
