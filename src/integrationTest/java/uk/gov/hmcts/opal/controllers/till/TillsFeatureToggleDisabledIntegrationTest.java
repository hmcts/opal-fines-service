package uk.gov.hmcts.opal.controllers.till;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.common.user.authentication.exception.CommonGlobalExceptionHandler;
import uk.gov.hmcts.opal.controllers.AbstractWebMvcIntegrationTest;
import uk.gov.hmcts.opal.controllers.FeatureToggleWebMvcTestConfiguration;
import uk.gov.hmcts.opal.controllers.till.TillsApiController;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.till.CreateTillService;
import uk.gov.hmcts.opal.service.opal.till.TillAllocationService;
import uk.gov.hmcts.opal.service.opal.till.TillSearchService;
import uk.gov.hmcts.opal.service.opal.till.TillsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles("integration")
@TestPropertySource(properties = "launchdarkly.default-flag-values.release-1c-payment=false")
@DisplayName("Tills Feature Toggle Integration Tests")
@WebMvcTest(controllers = TillsApiController.class,
    excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CommonGlobalExceptionHandler.class))
@Import({FeatureToggleWebMvcTestConfiguration.class, TillsApiController.class})
class TillsFeatureToggleDisabledIntegrationTest extends AbstractWebMvcIntegrationTest {

    @MockitoBean
    private DynamicConfigService dynamicConfigService;

    @MockitoBean
    private TillSearchService tillSearchService;

    @MockitoBean
    private TillsService tillsService;

    @MockitoBean
    private CreateTillService createTillService;

    @MockitoBean
    private TillAllocationService tillAllocationService;

    @Test
    @JiraStory("PO-3423")
    @JiraEpic("PO-2116")
    void rejectsAllocationWhenDisabled() throws Exception {
        mockMvc.perform(post("/tills/allocate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tills\":[{\"till_id\":1,\"business_unit_id\":78}]}"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"));
    }

    @Test
    @DisplayName("PO-8362 INT.04 - Returns not found when Release 1C Payment is disabled")
    @JiraStory("PO-8362")
    @JiraStory("PO-3629")
    @JiraEpic("PO-3635")
    void rejectsDisabledFeature() throws Exception {
        mockMvc.perform(get("/tills/99000000836200"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"));
    }
}
