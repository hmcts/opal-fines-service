package uk.gov.hmcts.opal.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.controllers.advice.OpalGlobalExceptionHandler;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggleAspect;
import uk.gov.hmcts.opal.common.user.authentication.exception.CommonGlobalExceptionHandler;
import uk.gov.hmcts.opal.mapper.OutstandingAutoPaymentMapperImpl;
import uk.gov.hmcts.opal.repository.OutstandingAutoPaymentRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.OutstandingAutoPaymentService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1c-payment=true"
})
@WebMvcTest(
    controllers = OutstandingAutoPaymentController.class,
    excludeFilters = @Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {CommonGlobalExceptionHandler.class, FeatureToggleAspect.class}
    )
)
@Import({
    OutstandingAutoPaymentController.class,
    OutstandingAutoPaymentService.class,
    OutstandingAutoPaymentMapperImpl.class,
    JsonSchemaValidationService.class,
    OpalGlobalExceptionHandler.class
})
@DisplayName("Outstanding Auto Payment Error Integration Tests")
class OutstandingAutoPaymentErrorIntegrationTest extends AbstractWebMvcIntegrationTest {

    private static final String URL = "/business-units/outstanding-auto-payment-count";

    @MockitoBean
    private UserStateService userStateService;

    @MockitoBean
    private OutstandingAutoPaymentRepository repository;

    @Test
    @DisplayName("PO-2470 INT.11 - Uses common error handling for data access failures")
    @JiraStory("PO-2470")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-2470")
    void usesCommonErrorHandlingForDataAccessFailures() throws Exception {
        List<Short> businessUnitIds = List.of((short) 2470);
        when(userStateService.getBusinessUnitIdsFor(
            FinesPermission.PROCESS_AND_ALLOCATE_PAYMENTS)).thenReturn(businessUnitIds);
        when(repository.findByBusinessUnitIdInOrderByBusinessUnitNameAsc(businessUnitIds))
            .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        mockMvc.perform(get(URL)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(true));
    }
}
