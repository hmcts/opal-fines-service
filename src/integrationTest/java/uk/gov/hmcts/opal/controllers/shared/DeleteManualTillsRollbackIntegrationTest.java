package uk.gov.hmcts.opal.controllers.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles("integration")
@DisplayName("Testing Support Manual Till Delete Rollback Integration Tests")
@Sql(
    scripts = "classpath:db/insertData/insert_into_manual_till_rollback_test.sql",
    executionPhase = BEFORE_TEST_METHOD
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_manual_till_rollback_test.sql",
    executionPhase = AFTER_TEST_METHOD
)
class DeleteManualTillsRollbackIntegrationTest extends AbstractIntegrationTest {

    private static final String URL = "/testing-support/tills";
    private static final long TILL_ID = 99000000836400L;

    @MockitoSpyBean
    private TillRepository tillRepository;

    @MockitoSpyBean
    private PaymentInRepository paymentInRepository;

    @Test
    @DisplayName("Rolls back linked payment deletion when till deletion fails")
    @JiraStory("PO-10810")
    @JiraEpic("PO-2468")
    void shouldRollbackPaymentDeletionWhenTillDeletionFails() throws Exception {
        assertThat(tillRepository.findById(TILL_ID)).isPresent();
        assertThat(paymentInRepository.findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(TILL_ID))
            .hasSize(1);

        doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Simulated till delete failure"))
            .when(tillRepository)
            .deleteAllById(any());

        mockMvc.perform(delete(URL).queryParam("ids", String.valueOf(TILL_ID)))
            .andExpect(status().isInternalServerError());

        verify(paymentInRepository).deleteByTillEntity_TillIdIn(List.of(TILL_ID));
        verify(tillRepository).deleteAllById(List.of(TILL_ID));
        assertThat(tillRepository.findById(TILL_ID)).isPresent();
        assertThat(paymentInRepository.findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(TILL_ID))
            .hasSize(1);
    }
}
