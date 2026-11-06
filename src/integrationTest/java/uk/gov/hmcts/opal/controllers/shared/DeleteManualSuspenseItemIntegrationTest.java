package uk.gov.hmcts.opal.controllers.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.repository.ControlTotalRepository;
import uk.gov.hmcts.opal.repository.CourtFeeRepository;
import uk.gov.hmcts.opal.repository.CourtFeesReceivedRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.SuspenseItemRepository;
import uk.gov.hmcts.opal.repository.SuspenseTransactionRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles("integration")
@DisplayName("Testing Support Manual Suspense Item Delete Controller Integration Tests")
@Sql(scripts = "classpath:db/insertData/insert_into_manual_suspense_item_for_deletion_test.sql",
    executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_manual_suspense_item_for_deletion_test.sql",
    executionPhase = AFTER_TEST_METHOD)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class DeleteManualSuspenseItemIntegrationTest extends AbstractIntegrationTest {

    private static final String URL = "/testing-support/suspense-items/";
    private static final long SUSPENSE_ITEM_ID = 99000000836503L;
    private static final long NON_EXISTENT_SUSPENSE_ITEM_ID = 99000000836599L;
    private static final long FIRST_SUSPENSE_TRANSACTION_ID = 99000000836510L;
    private static final long SECOND_SUSPENSE_TRANSACTION_ID = 99000000836511L;
    private static final long FIRST_COURT_FEE_RECEIVED_ID = 99000000836530L;
    private static final long SECOND_COURT_FEE_RECEIVED_ID = 99000000836531L;
    private static final long FIRST_CONTROL_TOTAL_ID = 99000000836520L;
    private static final long SECOND_CONTROL_TOTAL_ID = 99000000836521L;
    private static final long NOTE_ID = 99000000836540L;

    @Autowired
    private SuspenseItemRepository suspenseItemRepository;

    @Autowired
    private SuspenseTransactionRepository suspenseTransactionRepository;

    @Autowired
    private CourtFeesReceivedRepository courtFeesReceivedRepository;

    @Autowired
    private ControlTotalRepository controlTotalRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private CourtFeeRepository courtFeeRepository;

    @Test
    @JiraStory("PO-10585")
    @JiraEpic("PO-2997")
    @DisplayName("Rejects a negative suspense item ID")
    void shouldRejectNegativeSuspenseItemId() throws Exception {
        mockMvc.perform(delete(URL + "-1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Bad Request"));

        assertThat(suspenseItemRepository.findById(SUSPENSE_ITEM_ID)).isPresent();
    }

    @Test
    @JiraStory("PO-10585")
    @JiraEpic("PO-2997")
    @DisplayName("Ignores a suspense item ID that does not exist")
    void shouldIgnoreMissingSuspenseItem() throws Exception {
        mockMvc.perform(delete(URL + NON_EXISTENT_SUSPENSE_ITEM_ID))
            .andExpect(status().isNoContent());

        assertThat(suspenseItemRepository.findById(SUSPENSE_ITEM_ID)).isPresent();
    }

    @Test
    @JiraStory("PO-10585")
    @JiraEpic("PO-2997")
    @DisplayName("Deletes a suspense item and all supported related records")
    void shouldDeleteSuspenseItemAndRelatedRecords() throws Exception {
        assertThat(suspenseItemRepository.findById(SUSPENSE_ITEM_ID)).isPresent();
        assertThat(suspenseTransactionRepository.findAllBySuspenseItem(
            suspenseItemRepository.findById(SUSPENSE_ITEM_ID).orElseThrow())).hasSize(2);
        assertThat(courtFeesReceivedRepository.findAllById(List.of(
            FIRST_COURT_FEE_RECEIVED_ID, SECOND_COURT_FEE_RECEIVED_ID))).hasSize(2);
        assertThat(controlTotalRepository.findAllById(List.of(
            FIRST_CONTROL_TOTAL_ID, SECOND_CONTROL_TOTAL_ID))).hasSize(2);
        assertThat(noteRepository.findById(NOTE_ID)).isPresent();

        mockMvc.perform(delete(URL + SUSPENSE_ITEM_ID))
            .andExpect(status().isNoContent());

        assertThat(suspenseItemRepository.findById(SUSPENSE_ITEM_ID)).isEmpty();
        assertThat(suspenseTransactionRepository.findAllById(List.of(
            FIRST_SUSPENSE_TRANSACTION_ID, SECOND_SUSPENSE_TRANSACTION_ID))).isEmpty();
        assertThat(courtFeesReceivedRepository.findAllById(List.of(
            FIRST_COURT_FEE_RECEIVED_ID, SECOND_COURT_FEE_RECEIVED_ID))).isEmpty();
        assertThat(controlTotalRepository.findAllById(List.of(
            FIRST_CONTROL_TOTAL_ID, SECOND_CONTROL_TOTAL_ID))).isEmpty();
        assertThat(noteRepository.findById(NOTE_ID)).isEmpty();

        assertThat(courtFeeRepository.findAllById(List.of(99000000836501L,99000000836502L)).size()).isEqualTo(2);
    }
}
