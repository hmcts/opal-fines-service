package uk.gov.hmcts.opal.controllers.r1b;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b-1-1=true"
})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.LegacyGetEnforcementStatusR1B11IntegrationTest")
public class LegacyGetEnforcementStatusR1B11IntegrationTest extends AbstractIntegrationTest {

    @Test
    @JiraEpic("PO-978")
    @JiraStory("PO-10829")
    @DisplayName("LEGACY: next enforcement action data for MPSO is present when R1B.1.1 is enabled")
    void testGetEnforcementStatus_populatesNextEnforcementActionDataForMPSO() throws Exception {
        ResultActions result = mockMvc.perform(get("/defendant-accounts/1234/enforcement-status")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.last_enforcement_action.enforcement_action.result_id").value("MPSO"))
            .andExpect(jsonPath("$.next_enforcement_action_data").value("All"));
    }

    @Test
    @JiraEpic("PO-978")
    @JiraStory("PO-10829")
    @DisplayName("LEGACY: next enforcement action data is not populated when no last enforcement action exists")
    void testGetEnforcementStatus_hasNoLastEnforcementAction() throws Exception {
        ResultActions result = mockMvc.perform(get("/defendant-accounts/2009/enforcement-status")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.last_enforcement_action").doesNotExist())
            .andExpect(jsonPath("$.next_enforcement_action_data").doesNotExist());
    }

    @Test
    @JiraEpic("PO-978")
    @JiraStory("PO-10829")
    @DisplayName("LEGACY: Blank result ID does not populate next enforcement action data")
    void testGetEnforcementStatus_hasBlankResultId() throws Exception {
        ResultActions result = mockMvc.perform(get("/defendant-accounts/2010/enforcement-status")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.last_enforcement_action.enforcement_action.result_id").doesNotExist())
            .andExpect(jsonPath("$.next_enforcement_action_data").doesNotExist());
    }

    @Test
    @JiraEpic("PO-978")
    @JiraStory("PO-10829")
    @DisplayName("LEGACY: Unknown result ID does not populate next enforcement action data")
    void testGetEnforcementStatus_hasUnknownResultId() throws Exception {
        ResultActions result = mockMvc.perform(get("/defendant-accounts/2011/enforcement-status")
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.last_enforcement_action.enforcement_action.result_id").value("UNKNOWN"))
            .andExpect(jsonPath("$.next_enforcement_action_data").doesNotExist());
    }

}
