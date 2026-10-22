package uk.gov.hmcts.opal.controllers.r1b;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Sql(scripts = "classpath:db/insertData/insert_non_paying_parent_guardian_for_removal.sql",
    executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_non_paying_parent_guardian_for_removal.sql",
    executionPhase = AFTER_TEST_METHOD)
@DisplayName("Remove Defendant Account Party integration tests")
class DefendantPartyRemovalPdplIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    private static final long ACCOUNT_ID = 24010L;
    private static final long DEFENDANT_ACCOUNT_PARTY_ID = 9900000999101L;
    private static final long PARENT_GUARDIAN_PARTY_ID = 9900000999102L;
    private static final String BUSINESS_UNIT_ID = "78";
    private static final String DELETE_URI = URL_BASE + "/" + ACCOUNT_ID + "/defendant-account-parties/"
        + DEFENDANT_ACCOUNT_PARTY_ID;
    private static final String REQUEST_BODY = """
        {
            "party_details": {
                "party_id": "%d"
            }
        }
        """.formatted(PARENT_GUARDIAN_PARTY_ID);

    @MockitoBean
    private LoggingService loggingService;

    @Test
    @JiraEpic("PO-10729")
    @JiraStory("PO-10766")
    @DisplayName("Successful removal queues one Parent/Guardian erasure log")
    void delete_whenPdplLogIsQueued_removesParentGuardian() throws Exception {
        int currentVersion = versionFor(ACCOUNT_ID);
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        removeParentGuardian(currentVersion).andExpect(status().isOk());

        PersonalDataProcessingLogDetails logDetails = captureLogDetails();
        assertThat(partyAssociationCountFor(ACCOUNT_ID, DEFENDANT_ACCOUNT_PARTY_ID)).isZero();
        assertThat(versionFor(ACCOUNT_ID)).isEqualTo(currentVersion + 1);
        assertThat(logDetails.getBusinessIdentifier()).isEqualTo("Remove Parent/Guardian");
        assertThat(logDetails.getCategory()).isEqualTo(PersonalDataProcessingCategory.ERASURE);
        assertThat(logDetails.getCreatedBy().getIdentifier()).isEqualTo("500000000");
        assertThat(logDetails.getCreatedBy().getType()).isEqualTo(PdplIdentifierType.OPAL_USER_ID);
        assertThat(logDetails.getIndividuals()).singleElement().satisfies(individual -> {
            assertThat(individual.getIdentifier()).isEqualTo(String.valueOf(PARENT_GUARDIAN_PARTY_ID));
            assertThat(individual.getType()).isEqualTo(PdplIdentifierType.PARENT_GUARDIAN);
        });
    }

    @Test
    @JiraEpic("PO-10729")
    @JiraStory("PO-10766")
    @DisplayName("Queue rejection returns 503 and rolls back Parent/Guardian removal")
    void delete_whenPdplQueueRejects_rollsBackRemoval() throws Exception {
        int currentVersion = versionFor(ACCOUNT_ID);
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(false);

        removeParentGuardian(currentVersion)
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/pdpl-logging-unavailable"));

        verify(loggingService).personalDataAccessLogAsync(any());
        assertThat(partyAssociationCountFor(ACCOUNT_ID, DEFENDANT_ACCOUNT_PARTY_ID)).isOne();
        assertThat(versionFor(ACCOUNT_ID)).isEqualTo(currentVersion);
    }

    private ResultActions removeParentGuardian(int currentVersion) throws Exception {
        return mockMvc.perform(delete(DELETE_URI)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .headers(buildHttpHeaders(BUSINESS_UNIT_ID, "\"" + currentVersion + "\""))
            .contentType(MediaType.APPLICATION_JSON)
            .content(REQUEST_BODY));
    }

    private PersonalDataProcessingLogDetails captureLogDetails() {
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);
        verify(loggingService).personalDataAccessLogAsync(captor.capture());
        return captor.getValue();
    }
}
