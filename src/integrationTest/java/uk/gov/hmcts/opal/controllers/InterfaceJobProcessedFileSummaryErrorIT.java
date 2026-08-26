package uk.gov.hmcts.opal.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import jakarta.persistence.QueryTimeoutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.repository.InterfaceJobsProcessedFileSummaryRepository;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1c-payment=true"
})
@DisplayName("Interface Job Processed File Summary Error Integration Tests")
class InterfaceJobProcessedFileSummaryErrorIT extends AbstractIntegrationTest {

    private static final long INTERFACE_JOB_ID = 123L;
    private static final String URL = "/interface-jobs/" + INTERFACE_JOB_ID + "/processed-file-summary";

    @MockitoBean
    private InterfaceJobsProcessedFileSummaryRepository summaryViewRepository;

    @Test
    @DisplayName("PO-2576 INT.10 - maps a query timeout to 408")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void mapsQueryTimeoutToRequestTimeout() throws Exception {
        when(summaryViewRepository.findAllByInterfaceJobIdOrderByInterfaceFileIdAsc(INTERFACE_JOB_ID))
            .thenThrow(new QueryTimeoutException("timeout", null, null));

        mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(header().exists("operation_id"))
            .andExpect(jsonPath("$.status").value(408));
    }

    @Test
    @DisplayName("PO-2576 INT.10 - maps database availability failure to 503")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void mapsDatabaseAvailabilityFailureToServiceUnavailable() throws Exception {
        when(summaryViewRepository.findAllByInterfaceJobIdOrderByInterfaceFileIdAsc(INTERFACE_JOB_ID))
            .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(header().exists("operation_id"))
            .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    @DisplayName("PO-2576 INT.10 - maps unexpected failures to 500")
    @JiraStory("PO-2576")
    @JiraEpic("PO-2468")
    void mapsUnexpectedFailureToInternalServerError() throws Exception {
        when(summaryViewRepository.findAllByInterfaceJobIdOrderByInterfaceFileIdAsc(INTERFACE_JOB_ID))
            .thenThrow(new ResponseStatusException(INTERNAL_SERVER_ERROR, "unexpected failure"));

        mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(header().exists("operation_id"))
            .andExpect(jsonPath("$.status").value(500));
    }

}
