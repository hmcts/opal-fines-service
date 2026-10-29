package uk.gov.hmcts.opal.controllers.r1b;

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
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.controllers.shared.AbstractFeatureToggleIntegrationTest;
import uk.gov.hmcts.opal.common.user.authentication.exception.CommonGlobalExceptionHandler;
import uk.gov.hmcts.opal.controllers.DefendantAccountApiController;
import uk.gov.hmcts.opal.controllers.FeatureToggleWebMvcTestConfiguration;
import uk.gov.hmcts.opal.mapper.history.DefendantAccountHistoryResponseMapper;
import uk.gov.hmcts.opal.mapper.request.ReplacePartyRequestMapper;
import uk.gov.hmcts.opal.service.DefendantAccountEnforcementService;
import uk.gov.hmcts.opal.service.DefendantAccountFixedPenaltyService;
import uk.gov.hmcts.opal.service.DefendantAccountPartyService;
import uk.gov.hmcts.opal.service.DefendantAccountPaymentTermsService;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.service.ImpositionService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=false"
})
@DisplayName("Defendant Account History Feature Flag Integration Test")
@Tag("ExtendedTest")
@WebMvcTest(controllers = DefendantAccountApiController.class,
    excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CommonGlobalExceptionHandler.class))
@Import({FeatureToggleWebMvcTestConfiguration.class, DefendantAccountApiController.class})
class DefAccHistoryFlagTest extends AbstractFeatureToggleIntegrationTest {

    @MockitoBean private DefendantAccountService defendantAccountService;
    @MockitoBean private DefendantAccountEnforcementService defendantAccountEnforcementService;
    @MockitoBean private DefendantAccountHistoryResponseMapper defendantAccountHistoryResponseMapper;
    @MockitoBean private ImpositionService impositionService;
    @MockitoBean private DefendantAccountPartyService defendantAccountPartyService;
    @MockitoBean private DefendantAccountFixedPenaltyService defendantAccountFixedPenaltyService;
    @MockitoBean private DefendantAccountPaymentTermsService defendantAccountPaymentTermsService;
    @MockitoBean private ReplacePartyRequestMapper replacePartyRequestMapper;

    @Test
    @DisplayName("PO-2622: GET defendant account history returns 404 when release-1b is disabled")
    @JiraStory("PO-2622")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-7571")
    void getDefendantAccountHistory_whenRelease1bDisabled_returnsNotFound() throws Exception {
        // Arrange
        // release-1b is disabled by local test properties.

        // Act
        ResultActions result = mockMvc.perform(
            get("/defendant-accounts/262200/history")
                .header("Authorization", "Bearer test-token")
        );

        // Assert
        result.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.detail").value("The requested feature is not currently available"));
    }
}
