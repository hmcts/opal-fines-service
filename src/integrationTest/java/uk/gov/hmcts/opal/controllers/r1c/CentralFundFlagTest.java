package uk.gov.hmcts.opal.controllers.r1c;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.common.user.authentication.exception.CommonGlobalExceptionHandler;
import uk.gov.hmcts.opal.controllers.AbstractWebMvcIntegrationTest;
import uk.gov.hmcts.opal.controllers.FeatureToggleWebMvcTestConfiguration;
import uk.gov.hmcts.opal.controllers.MajorCreditorApiController;
import uk.gov.hmcts.opal.service.CentralFundService;
import uk.gov.hmcts.opal.service.MajorCreditorAccountService;
import uk.gov.hmcts.opal.service.opal.MajorCreditorService;
import uk.gov.hmcts.opal.mapper.MajorCreditorMapper;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=false"
})
@Slf4j(topic = "opal.CentralFundFlagTest")
@DisplayName("Central Fund Controller Feature Flag Integration Tests")
@Tag("ExtendedTest")
@WebMvcTest(controllers = MajorCreditorApiController.class,
    excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CommonGlobalExceptionHandler.class))
@Import({FeatureToggleWebMvcTestConfiguration.class, MajorCreditorApiController.class})
class CentralFundFlagTest extends AbstractWebMvcIntegrationTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    @MockitoBean
    private CentralFundService centralFundService;

    @MockitoBean
    private MajorCreditorAccountService majorCreditorAccountService;

    @MockitoBean
    private MajorCreditorService majorCreditorService;

    @MockitoBean
    private MajorCreditorMapper majorCreditorMapper;

    @Test
    @DisplayName("PO-2320: GET central fund returns 404 when release-1b is disabled")
    @JiraStory("PO-2320")
    @JiraEpic("PO-1286")
    @JiraTestKey("PO-7564")
    void getCentralFund_whenFeatureDisabled_returnsNotFound() throws Exception {
        mockMvc.perform(get("/central-funds/73").header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"));
    }
}
