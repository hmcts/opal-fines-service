package uk.gov.hmcts.opal.controllers.r1b;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@DisplayName("OPAL Defendant Account Party PDPO Logging Integration Tests")
class OpalDefendantPartyPdplLoggingIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    private static final long DEFENDANT_ACCOUNT_ID = 77L;
    private static final long DEFENDANT_PARTY_ID = 77L;
    private static final long PARENT_GUARDIAN_ACCOUNT_ID = 10004L;
    private static final long PARENT_GUARDIAN_PARTY_ID = 10004L;

    @MockitoBean
    private LoggingService loggingService;

    @BeforeEach
    void authoriseUserAndAcceptPdpoLogs() {
        authorise((short) 78, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);
    }

    @Test
    @DisplayName("PDPO: reading a Defendant party queues a consultation log")
    @JiraStory("PO-10755")
    @JiraEpic("PO-10729")
    void getDefendantAccountParty_defendant_queuesConsultationLog() throws Exception {
        getParty(DEFENDANT_ACCOUNT_ID, DEFENDANT_PARTY_ID)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

        assertPdpoLog("View Defendant", "77", PdplIdentifierType.DEFENDANT_ACCOUNT);
    }

    @Test
    @DisplayName("PDPO: reading a Parent/Guardian party queues a consultation log")
    @JiraStory("PO-10755")
    @JiraEpic("PO-10729")
    void getDefendantAccountParty_parentGuardian_queuesConsultationLog() throws Exception {
        getParty(PARENT_GUARDIAN_ACCOUNT_ID, PARENT_GUARDIAN_PARTY_ID)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type")
                .value("Parent/Guardian"));

        assertPdpoLog("View Parent/Guardian", "10004", PdplIdentifierType.PARENT_GUARDIAN);
    }

    @Test
    @DisplayName("PDPO: queue rejection prevents Defendant party data from being returned")
    @JiraStory("PO-10755")
    @JiraEpic("PO-10729")
    void getDefendantAccountParty_queueReturnsFalse_returnsServiceUnavailable() throws Exception {
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);

        getParty(DEFENDANT_ACCOUNT_ID, DEFENDANT_PARTY_ID)
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/pdpl-logging-unavailable"))
            .andExpect(jsonPath("$.detail").value("The personal data logging queue is currently unavailable"))
            .andExpect(jsonPath("$.retriable").value(true))
            .andExpect(jsonPath("$.defendant_account_party").doesNotExist());
    }

    @Test
    @DisplayName("PDPO: a missing party does not queue a consultation log")
    @JiraStory("PO-10755")
    @JiraEpic("PO-10729")
    void getDefendantAccountParty_notFound_doesNotQueueLog() throws Exception {
        getParty(DEFENDANT_ACCOUNT_ID, 999999L)
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verify(loggingService, never()).personalDataAccessLogAsync(any());
    }

    private ResultActions getParty(long defendantAccountId, long defendantAccountPartyId) throws Exception {

        return mockMvc.perform(get(URL_BASE + "/" + defendantAccountId + "/defendant-account-parties/"
                + defendantAccountPartyId)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header("Authorization", userStateStub.getBearerToken()));
    }

    private void assertPdpoLog(String businessIdentifier, String partyId, PdplIdentifierType partyType) {
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService).personalDataAccessLogAsync(captor.capture());
        PersonalDataProcessingLogDetails details = captor.getValue();

        assertAll(
            () -> assertEquals(businessIdentifier, details.getBusinessIdentifier()),
            () -> assertEquals(PersonalDataProcessingCategory.CONSULTATION, details.getCategory()),
            () -> assertEquals("500000000", details.getCreatedBy().getIdentifier()),
            () -> assertEquals(PdplIdentifierType.OPAL_USER_ID, details.getCreatedBy().getType()),
            () -> assertEquals(List.of(partyId), details.getIndividuals().stream()
                .map(ParticipantIdentifier::getIdentifier).toList()),
            () -> assertEquals(List.of(partyType), details.getIndividuals().stream()
                .map(ParticipantIdentifier::getType).toList()),
            () -> assertNull(details.getRecipient())
        );
    }
}
