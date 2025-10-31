package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.TestingSupportControllerTest")
class TestingSupportControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private DynamicConfigService dynamicConfigService;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @MockitoBean
    private UserStateClientService userStateClientService;

    @Test
    void testGetAppMode() throws Exception {
        AppMode appMode = AppMode.builder().mode("test").build();

        when(dynamicConfigService.getAppMode()).thenReturn(appMode);

        mockMvc.perform(get("/testing-support/app-mode"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.mode").value("test"));
    }

    @Test
    void testIsFeatureEnabled() throws Exception {
        when(featureToggleService.isFeatureEnabled(anyString())).thenReturn(true);

        mockMvc.perform(get("/testing-support/launchdarkly/bool/testFeature"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isBoolean());
    }

    @Test
    void testGetFeatureValue() throws Exception {
        String featureValue = "testValue";
        when(featureToggleService.getFeatureValue(anyString())).thenReturn(featureValue);

        mockMvc.perform(get("/testing-support/launchdarkly/string/testFeature"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(featureValue));
    }

    @Test
    void testParseToken() throws Exception {
        String token = "Bearer testToken";

        when(accessTokenService.extractPreferredUsername(token)).thenReturn("testUser");

        mockMvc.perform(get("/testing-support/token/parse")
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("testUser"));
    }

    @Test
    void testGetUserState() throws Exception {
        UserState userState = UserStateUtil.permissionUser((short) 5, FinesPermission.ACCOUNT_ENQUIRY);
        when(userStateClientService.getUserState(1L)).thenReturn(Optional.of(userState));

        mockMvc.perform(get("/testing-support/user-client/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.user_name").value(userState.getUserName()))
            .andExpect(jsonPath("$.user_id").value(userState.getUserId()));
    }

    @Test
    void testGetUserStateNotFound() throws Exception {
        when(userStateClientService.getUserState(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/testing-support/user-client/999"))
            .andExpect(status().isNotFound());
    }

    @Sql(
        scripts = "classpath:db/insertData/insert_into_defendants_for_deletion_test.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Test
    void shouldDeleteDefendantAccountAndAssociatedData() throws Exception {
        // Pre-check that data exists
        assertThat(count(
            "defendant_accounts",
            "defendant_account_id = 1001"
        )).isEqualTo(1);
        assertThat(count(
            "defendant_account_parties",
            "defendant_account_id = 1001"
        )).isGreaterThan(0);
        assertThat(count(
            "payment_terms",
            "defendant_account_id = 1001"
        )).isGreaterThan(0);
        assertThat(count(
            "defendant_transactions",
            "defendant_account_id = 1001")
        ).isGreaterThan(0);
        assertThat(count(
            "impositions",
            "defendant_account_id = 1001")
        ).isGreaterThan(0);
        assertThat(count(
            "notes",
            "associated_record_id = '1001'"
        )).isGreaterThan(0);
        assertThat(count(
            "allocations",
            "imposition_id IN (SELECT imposition_id FROM impositions WHERE defendant_account_id = 1001)")
        ).isGreaterThan(0);
        assertThat(count(
            "cheques",
            "defendant_transaction_id "
                + "IN (SELECT defendant_transaction_id FROM defendant_transactions WHERE defendant_account_id = 1001)")
        ).isGreaterThan(0);

        // When: call the deletion endpoint
        ResultActions actions = mockMvc.perform(delete("/testing-support/defendant-accounts/1001"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":shouldDeleteDefendantAccountAndAssociatedData: Response body:\n"
                 + ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isNoContent());

        // Post-check that all related data is gone
        assertThat(count("defendant_accounts", "defendant_account_id = 1001")).isZero();
        assertThat(count("defendant_account_parties", "defendant_account_id = 1001")).isZero();
        assertThat(count("payment_terms", "defendant_account_id = 1001")).isZero();
        assertThat(count("defendant_transactions", "defendant_account_id = 1001")).isZero();
        assertThat(count("impositions", "defendant_account_id = 1001")).isZero();
        assertThat(count("notes", "associated_record_id = '1001'")).isZero();

        assertThat(count(
            "allocations",
            "imposition_id IN (SELECT imposition_id FROM impositions WHERE defendant_account_id = 1001)"))
            .isZero();
        assertThat(count(
            "cheques",
            "defendant_transaction_id "
                + "IN (SELECT defendant_transaction_id FROM defendant_transactions WHERE defendant_account_id = 1001)"))
            .isZero();
    }

    private int count(String table, String whereClause) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) "
                                               + "FROM " + table + " WHERE " + whereClause, Integer.class);
    }
}
