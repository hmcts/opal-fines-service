package uk.gov.hmcts.opal.controllers;

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
import uk.gov.hmcts.opal.authentication.model.SecurityToken;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.client.user.UserClient;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.TestingSupportControllerTest")
class TestingSupportControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String TEST_TOKEN = "testToken";
    private static final UserState USER_STATE = UserState.builder()
        .userName("name")
        .userId(123L)
        .businessUnitUser(Set.of(BusinessUnitUser.builder()
                          .businessUnitId((short) 123)
                          .businessUnitUserId("BU123")
                          .permissions(Set.of(
                              Permission.builder()
                                  .permissionId(1L)
                                  .permissionName("Notes")
                                  .build()))
                          .build()))
        .build();

    @MockitoBean
    private DynamicConfigService dynamicConfigService;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @MockitoBean
    private UserClient userClient;

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
    void testGetToken() throws Exception {
        SecurityToken securityToken = SecurityToken.builder()
            .accessToken(TEST_TOKEN)
            .userState(USER_STATE)
            .build();

        when(userClient.getTestUserToken()).thenReturn(securityToken);

        mockMvc.perform(get("/testing-support/token/test-user"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.access_token").value("testToken"))
            .andExpect(jsonPath("$.user_state.user_name").value("name"))
            .andExpect(jsonPath("$.user_state.user_id").value("123"))
            .andExpect(jsonPath("$.user_state.business_unit_user[0].business_unit_id")
                           .value("123"))
            .andExpect(jsonPath("$.user_state.business_unit_user[0].business_unit_user_id")
                           .value("BU123"))
            .andExpect(jsonPath("$.user_state.business_unit_user[0].permissions[0].permission_id")
                           .value("1"))
            .andExpect(
                jsonPath("$.user_state.business_unit_user[0].permissions[0].permission_name")
                           .value("Notes"));

    }

    @Test
    void testGetTokenForUser() throws Exception {
        SecurityToken securityToken = SecurityToken.builder()
            .accessToken(TEST_TOKEN)
            .userState(USER_STATE)
            .build();

        when(userClient.getTestUserToken(any())).thenReturn(securityToken);

        mockMvc.perform(get("/testing-support/token/user")
                            .header("X-User-Email", "test@example.com"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.access_token").value("testToken"))
            .andExpect(jsonPath("$.user_state.user_name").value("name"))
            .andExpect(jsonPath("$.user_state.user_id").value("123"))
            .andExpect(jsonPath("$.user_state.business_unit_user[0].business_unit_id")
                           .value("123"))
            .andExpect(jsonPath("$.user_state.business_unit_user[0].business_unit_user_id")
                           .value("BU123"))
            .andExpect(jsonPath("$.user_state.business_unit_user[0].permissions[0].permission_id")
                           .value("1"))
            .andExpect(
                jsonPath("$.user_state.business_unit_user[0].permissions[0].permission_name")
                           .value("Notes"));
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
    void testGetTokenForUserFailure() throws Exception {
        SecurityToken securityToken = SecurityToken.builder()
            .accessToken(TEST_TOKEN)
            .userState(USER_STATE)
            .build();

        when(userClient.getTestUserToken(any())).thenReturn(securityToken);

        mockMvc.perform(get("/testing-support/token/user")
                            .header("X-User-Email", "test@example.com"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.access_token").value("testToken"))
            .andExpect(jsonPath("$.user_state.user_name").value("name"))
            .andExpect(jsonPath("$.user_state.user_id").value("123"))
            .andExpect(jsonPath("$.user_state.business_unit_user[0].business_unit_id")
                           .value("123"))
            .andExpect(jsonPath("$.user_state.business_unit_user[0].business_unit_user_id")
                           .value("BU123"))
            .andExpect(jsonPath("$.user_state.business_unit_user[0].permissions[0].permission_id")
                           .value("1"))
            .andExpect(
                jsonPath("$.user_state.business_unit_user[0].permissions[0].permission_name")
                           .value("Notes"));
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
        log.info(":shouldDeleteDefendantAccountAndAssociatedData: Response body:\n" + ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isNoContent());

        // Post-check that all related data is gone
        assertThat(count("defendant_accounts", "defendant_account_id = 1001")).isZero();
        assertThat(count("defendant_account_parties", "defendant_account_id = 1001")).isZero();
        assertThat(count("payment_terms", "defendant_account_id = 1001")).isZero();
        assertThat(count("defendant_transactions", "defendant_account_id = 1001")).isZero();
        assertThat(count("impositions", "defendant_account_id = 1001")).isZero();
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
