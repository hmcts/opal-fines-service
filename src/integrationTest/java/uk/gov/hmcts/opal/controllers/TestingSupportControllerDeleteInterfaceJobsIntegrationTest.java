package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.ConnectException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.repository.InterfaceFileRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.repository.InterfaceMessageRepository;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.TillRepository;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.TestingSupportControllerDeleteInterfaceJobsTest")
@DisplayName("Testing Support Interface Jobs Delete Controller Integration Tests")
@Sql(scripts = "classpath:db/insertData/insert_into_interface_jobs_for_deletion_test.sql",
    executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_interface_jobs_for_deletion_test.sql",
    executionPhase = AFTER_TEST_METHOD)
public class TestingSupportControllerDeleteInterfaceJobsIntegrationTest extends AbstractIntegrationTest {

    private static final String URL = "/testing-support/interface-jobs";

    @MockitoSpyBean
    private InterfaceJobService interfaceJobService;

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

    @Test
    @DisplayName("Deletes interface jobs and associated data")
    @JiraStory("PO-2578")
    @JiraEpic("PO-2468")
    void shouldDeleteInterfaceJobsAndAssociatedData() throws Exception {
        long interfaceJobId1 = 987651L;
        long interfaceJobId2 = 987652L;
        List<Long> interfaceFileIds = List.of(987751L, 987752L);


        assertThat(interfaceJobRepository.count()).isEqualTo(2);
        assertThat(interfaceFileRepository.count()).isEqualTo(2);
        assertThat(interfaceMessageRepository.count()).isEqualTo(2);
        assertThat(tillRepository.countByInterfaceFile_InterfaceFileIdIn(interfaceFileIds)).isEqualTo(2);
        assertThat(paymentInRepository.count()).isEqualTo(2);

        ResultActions actions = mockMvc.perform(delete(URL)
            .queryParam("ids", "" + interfaceJobId1, "" + interfaceJobId2));

        actions.andExpect(status().isOk());

        assertThat(paymentInRepository.count()).isZero();
        assertThat(tillRepository.countByInterfaceFile_InterfaceFileIdIn(interfaceFileIds)).isZero();
        assertThat(interfaceMessageRepository.count()).isZero();
        assertThat(interfaceFileRepository.count()).isZero();
        assertThat(interfaceJobRepository.count()).isZero();
    }

    @Test
    @DisplayName("Deleting interface jobs without IDs returns a server error")
    @JiraStory("PO-2578")
    @JiraEpic("PO-2468")
    void deleteInterfaceJobs_whenNoIdsAreSupplied_returnsServerError() throws Exception {
        ResultActions actions = mockMvc.perform(delete(URL));

        actions.andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Deleting interface jobs with an empty IDs parameter returns not found")
    @JiraStory("PO-2578")
    @JiraEpic("PO-2468")
    void deleteInterfaceJobs_whenEmptyIdsParmaIsSupplied_returnsNotFound() throws Exception {
        ResultActions actions = mockMvc.perform(delete(URL).queryParam("ids", ""));

        actions.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deleting an interface job that does not exist returns not found")
    @JiraStory("PO-2578")
    @JiraEpic("PO-2468")
    void deleteInterfaceJobs_whenIdDoesNotExist_returnsNotFound() throws Exception {
        mockMvc.perform(delete(URL)
                .queryParam("ids", "-1"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deleting the same interface job twice returns not found on the second call")
    @JiraStory("PO-2578")
    @JiraEpic("PO-2468")
    void deleteInterfaceJobs_whenCalledTwice_returnsNotFoundOnSecondCall() throws Exception {
        mockMvc.perform(delete(URL)
                .queryParam("ids", "987651"))
            .andExpect(status().isOk());

        mockMvc.perform(delete(URL)
                .queryParam("ids", "987651"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Database unavailable when deleting interface jobs returns service unavailable")
    @JiraStory("PO-2578")
    @JiraEpic("PO-2468")
    void deleteInterfaceJobs_whenDatabaseIsUnavailable_returnsStandardisedServiceUnavailable() throws Exception {
        doAnswer(invocation -> {
            throw new PSQLException("Connection refused", PSQLState.CONNECTION_FAILURE, new ConnectException());
        }).when(interfaceJobService).deleteInterfaceJobs(anyList());

        mockMvc.perform(delete(URL)
                .queryParam("ids", "987651")
                .accept(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Service Unavailable"))
            .andExpect(jsonPath("$.detail").value("Opal database is currently unavailable"))
            .andExpect(jsonPath("$.status").value(503))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/database-unavailable"));
    }
}
