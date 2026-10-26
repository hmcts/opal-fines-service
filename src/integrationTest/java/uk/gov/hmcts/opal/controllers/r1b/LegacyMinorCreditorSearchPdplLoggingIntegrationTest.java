package uk.gov.hmcts.opal.controllers.r1b;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.dto.legacy.CreditorAccount;
import uk.gov.hmcts.opal.dto.legacy.Defendant;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsResponse;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {"launchdarkly.enabled=false", "launchdarkly.default-flag-values.release-1b=true"})
@DisplayName("Minor creditor search consultation logging in legacy mode")
class LegacyMinorCreditorSearchPdplLoggingIntegrationTest extends MinorCreditorSearchPdplTestSupport {

    @Test
    @JiraStory("PO-10764")
    @JiraEpic("PO-10729")
    void individualResultsQueueUniqueParticipants() throws Exception {
        stubResults(account("101", false, "201", false), account("101", false, "201", false),
            account("102", false, "201", false));

        search("PDPO6401").andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(3))
            .andExpect(jsonPath("$.creditor_accounts.length()").value(3))
            .andExpect(jsonPath("$.creditor_accounts[0].surname").value("SearchCreditor"))
            .andExpect(jsonPath("$.creditor_accounts[0].defendant.surname").value("SearchDefendant"));

        List<PersonalDataProcessingLogDetails> logs = logs(2);
        assertLog(logs.get(0), "Search Account Results - Individual Minor Creditors",
            PdplIdentifierType.MINOR_CREDITOR, "101", "102");
        assertLog(logs.get(1), "Search Account Results - Individual Defendants associated with Minor Creditors",
            PdplIdentifierType.DEFENDANT, "201");
    }

    @ParameterizedTest
    @JiraStory("PO-10764")
    @JiraEpic("PO-10729")
    @CsvSource({"false, true, MINOR_CREDITOR, 101", "true, false, DEFENDANT, 201"})
    void mixedResultsUseEachPartysFlag(boolean creditorCompany, boolean defendantCompany,
                                      PdplIdentifierType type, String id) throws Exception {

        stubResults(account("101", creditorCompany, "201", defendantCompany));

        search("PDPO6401").andExpect(status().isOk())
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
    void companiesDoNotQueueLogs() throws Exception {
        stubResults(account("101", true, "201", true));
        rejectQueueCall(1, true);

        search("PDPO6401").andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1));

        verifyNoInteractions(loggingService);
    }

    @Test
    @JiraStory("PO-10764")
    @JiraEpic("PO-10729")
    void emptyResultsDoNotQueueLogs() throws Exception {
        stubResults();
        rejectQueueCall(1, true);

        search("PDPO6401").andExpect(status().isOk()).andExpect(jsonPath("$.count").value(0));

        verifyNoInteractions(loggingService);
    }

    @Test
    @JiraStory("PO-10764")
    @JiraEpic("PO-10729")
    void unassociatedIndividualQueuesOnlyCreditorLog() throws Exception {
        CreditorAccount creditor = account("101", false, "201", false);
        creditor.setDefendant(null);
        stubResults(creditor);

        search("PDPO6401").andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.creditor_accounts[0].defendant").isEmpty());

        assertLog(logs(1).getFirst(), "Search Account Results - Individual Minor Creditors",
            PdplIdentifierType.MINOR_CREDITOR, "101");
    }

    @Test
    @JiraStory("PO-10764")
    @JiraEpic("PO-10729")
    void missingIndividualIdentifierWithholdsPersonalData() throws Exception {
        stubResults(account(" ", false, "201", false));

        assertQueueFailure(search("PDPO6401"), 0);
    }

    @ParameterizedTest
    @JiraStory("PO-10764")
    @JiraEpic("PO-10729")
    @CsvSource({"1, false", "2, false", "1, true", "2, true"})
    void queueFailureWithholdsPersonalData(int failingCall, boolean raisesError) throws Exception {
        stubResults(account("101", false, "201", false));
        rejectQueueCall(failingCall, raisesError);

        assertQueueFailure(search("PDPO6401"), failingCall);
    }

    private void stubResults(CreditorAccount... accounts) {
        String xml = LegacyMinorCreditorSearchResultsResponse.builder().count(accounts.length)
            .creditorAccounts(Arrays.asList(accounts)).build().toXml();
        stubFor(post(urlPathEqualTo("/opal"))
            .withQueryParam("actionType", equalTo("getMinorCreditorAccount"))
            .willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_XML_VALUE).withBody(xml)));
    }

    private CreditorAccount account(String creditorId, boolean creditorCompany,
                                    String defendantId, boolean defendantCompany) {

        return CreditorAccount.builder()
            .creditorAccountId(creditorId)
            .accountNumber("PDPO6401")
            .businessUnitId("10")
            .businessUnitName("Derbyshire")
            .organisation(creditorCompany)
            .organisationName(creditorCompany ? "Search Creditor Ltd" : null)
            .firstnames(creditorCompany ? null : "Alice")
            .surname(creditorCompany ? null : "SearchCreditor")
            .defendant(Defendant.builder().defendantAccountId(defendantId).organisation(defendantCompany)
                .organisationName(defendantCompany ? "Search Defendant Ltd" : null)
                .firstnames(defendantCompany ? null : "John")
                .surname(defendantCompany ? null : "SearchDefendant").build())
            .build();
    }
}
