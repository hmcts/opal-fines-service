package uk.gov.hmcts.opal.controllers.shared;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.LegacyDefendantsIntegrationTest01")
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

    @Test
    @JiraStory("PO-10341")
    @JiraEpic("PO-812")
    void testPostAddNoteForLegacyOnlyAccount() throws Exception {
        super.legacyOnlyAccountAddNoteSuccess(log);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nonNotesPermissions")
    @JiraStory("PO-1566")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-5959")
    @JiraTestKey(value = "PO-9418", name = "CREATE_MANAGE_DRAFT_ACCOUNTS")
    @JiraTestKey(value = "PO-9419", name = "ACCOUNT_ENQUIRY_NOTES")
    @JiraTestKey(value = "PO-9420", name = "ACCOUNT_ENQUIRY")
    @JiraTestKey(value = "PO-9421", name = "COLLECTION_ORDER")
    @JiraTestKey(value = "PO-9422", name = "CHECK_VALIDATE_DRAFT_ACCOUNTS")
    @JiraTestKey(value = "PO-9423", name = "SEARCH_AND_VIEW_ACCOUNTS")
    @JiraTestKey(value = "PO-9424", name = "ACCOUNT_MAINTENANCE")
    @JiraTestKey(value = "PO-9425", name = "AMEND_PAYMENT_TERMS")
    @JiraTestKey(value = "PO-9426", name = "ENTER_ENFORCEMENT")
    @JiraTestKey(value = "PO-9427", name = "VIEW_CREDITOR_BACS")
    @JiraTestKey(value = "PO-9428", name = "CONSOLIDATE")
    @JiraTestKey(value = "PO-9429", name = "ADD_AND_REMOVE_PAYMENT_HOLD")
    @JiraTestKey(value = "PO-9430", name = "PROCESS_AND_ALLOCATE_PAYMENTS")
    @JiraTestKey(value = "PO-9431", name = "AUTO_ENFORCEMENT")
    void testLegacyNotes_Forbidden(FinesPermission permission) throws Exception {
        super.postNotes_UserWithoutPermission(permission);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("businessUnitAuthorizationScenarios")
    @JiraStory("PO-1566")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-5960")
    @JiraTestKey(value = "PO-9432", name = "missing Business-Unit-Id header")
    @JiraTestKey(value = "PO-9433", name = "wrong business unit in header")
    @JiraTestKey(value = "PO-9434", name = "permission present in another BU")
    void testLegacyNotes_BusinessUnitAuthorization(BusinessUnitAuthorizationScenario scenario)
        throws Exception {
        super.postNotes_BusinessUnitAuthorization(scenario);
    }

}
