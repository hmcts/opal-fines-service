package uk.gov.hmcts.opal.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import jakarta.persistence.QueryTimeoutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.common.controllers.advice.OpalGlobalExceptionHandler;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggleAspect;
import uk.gov.hmcts.opal.common.user.authentication.exception.CommonGlobalExceptionHandler;
import uk.gov.hmcts.opal.controllers.AbstractWebMvcIntegrationTest;
import uk.gov.hmcts.opal.mapper.InterfaceJobProcessedFileSummaryMapper;
import uk.gov.hmcts.opal.mapper.InterfaceMessageMapper;
import uk.gov.hmcts.opal.repository.InterfaceJobRepository;
import uk.gov.hmcts.opal.repository.InterfaceJobsProcessedFileSummaryRepository;
import uk.gov.hmcts.opal.repository.InterfaceMessageRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.InterfaceJobProcessedFileSummaryPdplLoggingService;
import uk.gov.hmcts.opal.service.opal.InterfaceJobProcessedFileSummaryService;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1c-payment=true"
})
@WebMvcTest(
    controllers = InterfaceJobsApiController.class,
    excludeFilters = @Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {CommonGlobalExceptionHandler.class, FeatureToggleAspect.class}
    )
)
@Import({
    InterfaceJobsApiController.class,
    InterfaceJobProcessedFileSummaryService.class,
    JsonSchemaValidationService.class,
    OpalGlobalExceptionHandler.class
})
@DisplayName("Interface Job Processed File Summary Error Integration Tests")
class InterfaceJobProcessedFileSummaryErrorIT extends AbstractWebMvcIntegrationTest {

    private static final long INTERFACE_JOB_ID = 123L;
    private static final String URL = "/interface-jobs/" + INTERFACE_JOB_ID + "/processed-file-summary";

    @MockitoBean
    private InterfaceJobsProcessedFileSummaryRepository summaryViewRepository;

    @MockitoBean
    private DynamicConfigService dynamicConfigService;

    @MockitoBean
    private InterfaceJobService interfaceJobService;

    @MockitoBean
    private InterfaceJobRepository interfaceJobRepository;

    @MockitoBean
    private InterfaceMessageRepository interfaceMessageRepository;

    @MockitoBean
    private InterfaceJobProcessedFileSummaryMapper processedFileSummaryMapper;

    @MockitoBean
    private InterfaceMessageMapper interfaceMessageMapper;

    @MockitoBean
    private UserStateService userStateService;

    @MockitoBean
    private InterfaceJobProcessedFileSummaryPdplLoggingService pdplLoggingService;

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
            .andExpect(jsonPath("$.status").value(500));
    }

}
