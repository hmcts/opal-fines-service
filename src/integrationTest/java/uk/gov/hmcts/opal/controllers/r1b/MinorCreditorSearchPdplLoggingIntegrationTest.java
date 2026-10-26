package uk.gov.hmcts.opal.controllers.r1b;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {"launchdarkly.enabled=false", "launchdarkly.default-flag-values.release-1b=true"})
@Sql(scripts = "classpath:db/insertData/insert_minor_creditor_search_pdpo.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_minor_creditor_search_pdpo.sql", executionPhase = AFTER_TEST_METHOD)
@DisplayName("Minor creditor search consultation logging in Opal mode")
class MinorCreditorSearchPdplLoggingIntegrationTest extends MinorCreditorSearchPdplTestSupport {

    @Autowired
    private DefendantAccountPartiesRepository defendantAccountPartiesRepository;

    @Test
    @JiraStory("PO-10764")
    @JiraEpic("PO-10729")
    void individualsQueueBothLogs() throws Exception {
        search("PDPO6401").andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.creditor_accounts[0].creditor_account_id").value("10764001"))
            .andExpect(jsonPath("$.creditor_accounts[0].organisation").value(false))
            .andExpect(jsonPath("$.creditor_accounts[0].defendant.organisation").value(false));

        List<PersonalDataProcessingLogDetails> logs = logs(2);
        assertLog(logs.get(0), "Search Account Results - Individual Minor Creditors",
            PdplIdentifierType.MINOR_CREDITOR, "10764001");
        assertLog(logs.get(1), "Search Account Results - Individual Defendants associated with Minor Creditors",
            PdplIdentifierType.DEFENDANT, "10764101");
    }

    @ParameterizedTest
    @JiraStory("PO-10764")
    @JiraEpic("PO-10729")
    @CsvSource({"PDPO6402, false, true, MINOR_CREDITOR, 10764002",
        "PDPO6403, true, false, DEFENDANT, 10764101"})
    void mixedResultsUseEachPartysFlag(String account, boolean creditorCompany, boolean defendantCompany,
                                      PdplIdentifierType type, String id) throws Exception {

        search(account).andExpect(status().isOk())
            .andExpect(jsonPath("$.creditor_accounts[0].organisation").value(creditorCompany))
            .andExpect(jsonPath("$.creditor_accounts[0].defendant.organisation").value(defendantCompany));

        String identifier = type == PdplIdentifierType.MINOR_CREDITOR
            ? "Search Account Results - Individual Minor Creditors"
            : "Search Account Results - Individual Defendants associated with Minor Creditors";
        assertLog(logs(1).getFirst(), identifier, type, id);
    }

    @Test
    @JiraStory("PO-10764")
    @JiraEpic("PO-10729")
    void repeatedDefendantIsLoggedOnceWithoutRemovingResults() throws Exception {
        search("PDPO64").andExpect(status().isOk()).andExpect(jsonPath("$.count").value(7));

        List<PersonalDataProcessingLogDetails> logs = logs(2);
        assertLog(logs.get(0), "Search Account Results - Individual Minor Creditors",
            PdplIdentifierType.MINOR_CREDITOR, "10764001", "10764002", "10764005", "10764007");
        assertLog(logs.get(1), "Search Account Results - Individual Defendants associated with Minor Creditors",
            PdplIdentifierType.DEFENDANT, "10764101");
    }

    @ParameterizedTest
    @JiraStory("PO-10764")
    @JiraEpic("PO-10729")
    @ValueSource(strings = {"PDPO6404", "PDPO6406"})
    void companiesDoNotQueueLogs(String account) throws Exception {
        rejectQueueCall(1, true);
        search(account).andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1));

        verifyNoInteractions(loggingService);
    }

    @Test
    @JiraStory("PO-10764")
    @JiraEpic("PO-10729")
    void emptyResultsDoNotQueueLogs() throws Exception {
        rejectQueueCall(1, true);
        search("PDPO6499").andExpect(status().isOk()).andExpect(jsonPath("$.count").value(0));

        verifyNoInteractions(loggingService);
    }

    @Test
    @JiraStory("PO-10764")
    @JiraEpic("PO-10729")
    void unassociatedIndividualQueuesOnlyCreditorLog() throws Exception {
        search("PDPO6407").andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.creditor_accounts[0].defendant.defendant_account_id").isEmpty());

        assertLog(logs(1).getFirst(), "Search Account Results - Individual Minor Creditors",
            PdplIdentifierType.MINOR_CREDITOR, "10764007");
    }

    @Test
    @JiraStory("PO-10764")
    @JiraEpic("PO-10729")
    void missingDefendantPartyWithholdsPersonalData() throws Exception {
        defendantAccountPartiesRepository.deleteById(10764101L);

        assertQueueFailure(search("PDPO6401"), 0);
    }

    @ParameterizedTest
    @JiraStory("PO-10764")
    @JiraEpic("PO-10729")
    @CsvSource({"1, false", "2, false", "1, true", "2, true"})
    void queueFailureWithholdsPersonalData(int failingCall, boolean raisesError) throws Exception {
        rejectQueueCall(failingCall, raisesError);

        assertQueueFailure(search("PDPO6401"), failingCall);
    }
}
