package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleApi;
import uk.gov.hmcts.opal.common.user.authorisation.client.mapper.UserStateMapper;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.TestingSupportControllerTest")
@SuppressWarnings("java:S1874")
class TestingSupportControllerIntegrationTest extends AbstractIntegrationTest {

    // Limit JdbcTemplate use to narrow test setup or persistence-side-effect checks.
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private DynamicConfigService dynamicConfigService;

    @MockitoBean
    private FeatureToggleApi featureToggleApi;

    @MockitoBean
    private UserStateClientService userStateClientService;

    @MockitoBean
    private UserStateMapper userStateMapper;

    @Test
    @JiraStory("PO-256")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-6279")
    void testIsLegacyMode() throws Exception {
        when(dynamicConfigService.isLegacyMode()).thenReturn(true);

        mockMvc.perform(get("/testing-support/is-legacy-mode"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").value(true));
    }

    @Test
    @JiraStory("PO-256")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-6276")
    void testIsFeatureEnabled() throws Exception {
        when(featureToggleApi.isFeatureEnabled(anyString())).thenReturn(true);

        mockMvc.perform(get("/testing-support/launchdarkly/bool/testFeature"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isBoolean());
    }

    @Test
    @JiraStory("PO-256")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-6278")
    void testGetFeatureValue() throws Exception {
        String featureValue = "testValue";
        when(featureToggleApi.getFeatureValue(anyString(), anyString())).thenReturn(featureValue);

        mockMvc.perform(get("/testing-support/launchdarkly/string/testFeature"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(featureValue));
    }

    @Test
    @JiraStory("PO-256")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-6277")
    void testParseToken() throws Exception {
        mockMvc.perform(get("/testing-support/token/parse")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("Authorization", userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("opal-test@HMCTS.NET"));
    }

    @Test
    @JiraStory("PO-256")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-6282")
    void testGetUserState() throws Exception {
        UserState userState = UserStateUtil.permissionUser((short) 5, FinesPermission.ACCOUNT_ENQUIRY);
        UserStateV2 userStateV2 = mock(UserStateV2.class);
        when(userStateClientService.getUserStateByAuthenticatedUser()).thenReturn(Optional.of(userStateV2));
        when(userStateMapper.toUserState(userStateV2, Domain.FINES)).thenReturn(userState);

        mockMvc.perform(get("/testing-support/user-client/0"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.user_name").value(userState.getUserName()))
            .andExpect(jsonPath("$.user_id").value(userState.getUserId()));
    }

    @Test
    @JiraStory("PO-256")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-6280")
    void testGetUserStateNotFound() throws Exception {
        mockMvc.perform(get("/testing-support/user-client/999"))
            .andExpect(status().isNotFound());
    }

    @Sql(
        scripts = "classpath:db/insertData/insert_into_defendants_for_deletion_test.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Test
    @JiraStory("PO-1772")
    @JiraStory("PO-1777")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6281")
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
            "reports",
            "report_id = '10001'"
        )).isGreaterThan(0);
        assertThat(count(
            "report_entries",
             "associated_record_id = '1001'"
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
            "amendments",
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
        assertThat(count("reports", "report_id = '10001'")).as("Rows in Reports should not be deleted").isEqualTo(1);
        assertThat(count("report_entries", "associated_record_id = '1001'")
        ).isZero();
        assertThat(count("defendant_transactions", "defendant_account_id = 1001")).isZero();
        assertThat(count("impositions", "defendant_account_id = 1001")).isZero();
        assertThat(count("notes", "associated_record_id = '1001'")).isZero();
        assertThat(count("amendments", "associated_record_id = '1001'")).isZero();

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
