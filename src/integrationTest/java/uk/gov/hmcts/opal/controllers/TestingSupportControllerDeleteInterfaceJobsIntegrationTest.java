package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.repository.InterfaceFileRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.repository.InterfaceMessageRepository;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.TestingSupportControllerDeleteInterfaceJobsTest")
public class TestingSupportControllerDeleteInterfaceJobsIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private InterfaceFileRepository interfaceFileRepository;

    @Autowired
    private InterfaceJobRepository interfaceJobRepository;

    @Autowired
    private InterfaceMessageRepository interfaceMessageRepository;

    @Autowired
    private PaymentInRepository paymentInRepository;

    @Autowired
    private TillRepository tillRepository;

    @Sql(
        scripts = {
            "classpath:db/insertData/insert_into_interface_jobs_for_deletion_test.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
        scripts = "classpath:db/deleteData/delete_from_interface_jobs_for_deletion_test.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @Test
    @JiraStory("PO-2578")
    @JiraEpic("PO-2468")
    void shouldDeleteInterfaceJobsAndAssociatedData() throws Exception {
        long firstInterfaceJobId = 987651L;
        long secondInterfaceJobId = 987652L;
        List<Long> interfaceFileIds = List.of(987751L, 987752L);

        assertThat(interfaceJobRepository.count()).isEqualTo(2);
        assertThat(interfaceFileRepository.count()).isEqualTo(2);
        assertThat(interfaceMessageRepository.count()).isEqualTo(2);
        assertThat(tillRepository.countByInterfaceFile_InterfaceFileIdIn(interfaceFileIds)).isEqualTo(2);
        assertThat(paymentInRepository.count()).isEqualTo(2);

        ResultActions actions = mockMvc.perform(delete("/testing-support/interface-jobs")
            .queryParam("ids", "" + firstInterfaceJobId, "" + secondInterfaceJobId));

        actions.andExpect(status().isOk());

        assertThat(paymentInRepository.count()).isZero();
        assertThat(tillRepository.countByInterfaceFile_InterfaceFileIdIn(interfaceFileIds)).isZero();
        assertThat(interfaceMessageRepository.count()).isZero();
        assertThat(interfaceFileRepository.count()).isZero();
        assertThat(interfaceJobRepository.count()).isZero();
    }

    @Test
    @JiraStory("PO-2578")
    @JiraEpic("PO-2468")
    void wouldDeleteInterfaceJobsAndAssociatedDataNoneSupplied() throws Exception {
        ResultActions actions = mockMvc.perform(delete("/testing-support/interface-jobs"));

        actions.andExpect(status().isOk());
    }
}
