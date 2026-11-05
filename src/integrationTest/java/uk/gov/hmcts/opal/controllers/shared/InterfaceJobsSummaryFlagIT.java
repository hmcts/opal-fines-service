package uk.gov.hmcts.opal.controllers.shared;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.common.user.authentication.exception.CommonGlobalExceptionHandler;
import uk.gov.hmcts.opal.controllers.AbstractWebMvcIntegrationTest;
import uk.gov.hmcts.opal.controllers.FeatureToggleWebMvcTestConfiguration;
import uk.gov.hmcts.opal.controllers.InterfaceJobsApiController;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.InterfaceJobProcessedFileSummaryService;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1c-payment=false"
})
@DisplayName("Interface Jobs Summary Flag Integration Tests")
@Tag("ExtendedTest")
@WebMvcTest(controllers = InterfaceJobsApiController.class,
    excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CommonGlobalExceptionHandler.class))
@Import({FeatureToggleWebMvcTestConfiguration.class, InterfaceJobsApiController.class})
class InterfaceJobsSummaryFlagIT extends AbstractWebMvcIntegrationTest {

    private static final String URL = "/interface-jobs/summary";

    @MockitoBean
    private DynamicConfigService dynamicConfigService;

    @MockitoBean
    private InterfaceJobService interfaceJobService;

    @MockitoBean
    private InterfaceJobProcessedFileSummaryService processedFileSummaryService;

    @Test
    @DisplayName("PO-2574 INT.11 - Handles disabled feature")
    @JiraStory("PO-2574")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-9395")
    void handlesDisabledFeature() throws Exception {
        mockMvc.perform(get(URL)
                .queryParam("business_unit_ids", "2574"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"));
    }
}
