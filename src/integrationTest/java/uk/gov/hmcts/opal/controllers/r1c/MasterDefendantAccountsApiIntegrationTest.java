package uk.gov.hmcts.opal.controllers.r1c;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import uk.gov.hmcts.opal.controllers.r1b.AbstractOpalDefendantsIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1c-payment=true"
})
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(
    scripts = "classpath:db/insertData/insert_into_defendant_accounts_master.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_defendant_accounts_master.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@DisplayName("Defendant Account Controller get Master Integration Tests")
@Slf4j(topic = "opal.DefendantAccountMasterIntegrationTest")
public class MasterDefendantAccountsApiIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    private static final long DEFENDANT_ACCOUNT_ID_MASTER = 990001L;
    private static final long DEFENDANT_ACCOUNT_ID = 990002L;
    private static final long MISSING_ACCOUNT_ID = 999999999L;
    private static final String URL = URL_BASE + "/%d/master";

    @Test
    @JiraStory("PO-3633")
    @JiraEpic("PO-2439")
    @DisplayName("returns the master account ID and version for a defendant account with a master account")
    void getDefendantAccountMaster_whenAccountExists_returnsMasterAccountId() throws Exception {
        mockMvc.perform(get(URL.formatted(DEFENDANT_ACCOUNT_ID))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value(DEFENDANT_ACCOUNT_ID_MASTER));
    }

    @Test
    @JiraStory("PO-3633")
    @JiraEpic("PO-2439")
    @DisplayName("returns 403 when the user lacks Search and View Accounts permission")
    void getDefendantAccountMaster_whenPermissionMissing_returnsForbidden() throws Exception {
        userStateStub.setupWithNoPermissions();

        mockMvc.perform(get(URL.formatted(DEFENDANT_ACCOUNT_ID))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource"));
    }

    @Test
    @JiraStory("PO-3633")
    @JiraEpic("PO-2439")
    @DisplayName("returns 404 when the requested defendant account does not exist")
    void getDefendantAccountMaster_whenAccountMissing_returnsNotFound() throws Exception {
        mockMvc.perform(get(URL.formatted(MISSING_ACCOUNT_ID))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"));
    }
}
