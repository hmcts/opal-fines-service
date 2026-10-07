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
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles("integration")
@DisplayName("Testing Support Manual Tills Delete Controller Integration Tests")
@Sql(
    scripts = "classpath:db/insertData/insert_into_manual_tills_for_deletion_test.sql",
    executionPhase = BEFORE_TEST_METHOD
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_manual_tills_for_deletion_test.sql",
    executionPhase = AFTER_TEST_METHOD
)
class DeleteManualTillsIntegrationTest extends AbstractIntegrationTest {

    private static final String URL = "/testing-support/tills";
    private static final long FIRST_TILL_ID = 99000000836300L;
    private static final long SECOND_TILL_ID = 99000000836301L;
    private static final long UNRELATED_TILL_ID = 99000000836302L;

    @Autowired
    private TillRepository tillRepository;

    @Autowired
    private PaymentInRepository paymentInRepository;

    @Test
    @DisplayName("Deletes requested manual tills and linked payments")
    @JiraStory("PO-3630")
    void shouldDeleteRequestedManualTillsAndPayments() throws Exception {
        assertThat(tillRepository.findAllById(List.of(
            FIRST_TILL_ID, SECOND_TILL_ID, UNRELATED_TILL_ID
        ))).hasSize(3);

        mockMvc.perform(delete(URL)
                .queryParam(
                    "ids",
                    String.valueOf(FIRST_TILL_ID),
                    String.valueOf(FIRST_TILL_ID),
                    String.valueOf(SECOND_TILL_ID),
                    "999999"
                ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").doesNotExist());

        assertThat(tillRepository.findAllById(List.of(FIRST_TILL_ID, SECOND_TILL_ID))).isEmpty();

        assertThat(paymentInRepository.findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(
            FIRST_TILL_ID
        )).isEmpty();

        assertThat(paymentInRepository.findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(
            SECOND_TILL_ID
        )).isEmpty();

        assertThat(tillRepository.findById(UNRELATED_TILL_ID)).isPresent();

        assertThat(paymentInRepository.findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(
            UNRELATED_TILL_ID
        )).hasSize(1);
    }

    @Test
    @DisplayName("Rejects a request without till IDs")
    @JiraStory("PO-3630")
    void shouldRejectRequestWithoutTillIds() throws Exception {
        mockMvc.perform(delete(URL))
            .andExpect(status().isBadRequest());
    }
}