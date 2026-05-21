package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.SEARCH_AND_VIEW_ACCOUNTS;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleService;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.UserStateService;

@ActiveProfiles({"integration"})
@Sql(scripts = "classpath:db/insertData/insert_into_central_funds.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_central_funds.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.CentralFundControllerIntegrationTest")
@DisplayName("Central Fund Controller Integration Tests")
class CentralFundControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/central-funds";
    private static final String AUTH_HEADER = "Bearer test-token";

    @MockitoBean
    private UserStateService userStateService;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        when(featureToggleService.isFeatureEnabled("release-1b")).thenReturn(true);
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(UserStateUtil.permissionUser((short) 73, SEARCH_AND_VIEW_ACCOUNTS));
    }

    @Test
    @DisplayName("PO-2320: GET central fund returns 200 with payload and ETag")
    void getCentralFund_returnsPayloadWithEtag() throws Exception {
        ResultActions actions = mockMvc.perform(get(URL_BASE + "/73")
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":getCentralFund_returnsPayloadWithEtag: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
            .andExpect(jsonPath("$.major_creditor.creditor_account_id").value(73))
            .andExpect(jsonPath("$.major_creditor.account_number").value("00002000J"))
            .andExpect(jsonPath("$.major_creditor.name").value("West London Central Fund"))
            .andExpect(jsonPath("$.business_unit_details.business_unit_id").value("73"))
            .andExpect(jsonPath("$.business_unit_details.business_unit_name").value("West London"))
            .andExpect(jsonPath("$.business_unit_details.welsh_speaking").value("N"));
    }

    @Test
    @DisplayName("PO-2320: GET central fund returns 405 when release-1b is disabled")
    void getCentralFund_whenFeatureDisabled_returnsMethodNotAllowed() throws Exception {
        when(featureToggleService.isFeatureEnabled("release-1b")).thenReturn(false);

        mockMvc.perform(get(URL_BASE + "/73").header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Feature Disabled"))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/feature-disabled"));
    }

    @Test
    @DisplayName("PO-2320: GET central fund returns 404 when central fund does not exist")
    void getCentralFund_whenCentralFundDoesNotExist_returnsNotFound() throws Exception {
        mockMvc.perform(get(URL_BASE + "/999").header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
    }

    @Test
    @DisplayName("PO-2320: GET central fund returns 403 when user lacks Search and View Accounts")
    void getCentralFund_whenUserLacksPermission_returnsForbidden() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.noPermissionsUser());

        mockMvc.perform(get(URL_BASE + "/73").header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"));
    }
}
