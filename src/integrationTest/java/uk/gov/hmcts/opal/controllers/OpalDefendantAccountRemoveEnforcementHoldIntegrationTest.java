package uk.gov.hmcts.opal.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.service.opal.ReportEntryService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@DisplayName("Remove Defendant Account Enforcement Hold Integration Tests")
class OpalDefendantAccountRemoveEnforcementHoldIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    static final String URL_BASE = "/defendant-accounts";

    private static final long ACCOUNT_WITH_ENFORCEMENT_HOLD = 99000000000002L;
    private static final short BUSINESS_UNIT_ID = 77;

    @MockitoBean
    private ReportEntryService reportEntryService;

    @Test
    @DisplayName("Remove enforcement hold returns 200 and updates the defendant account")
    @JiraStory("PO-1775")
    @JiraEpic("PO-1675")
    @JiraTestKey("PO-5996")
    void int01_removeEnforcementHold_returnsOkAndUpdatesDatabase() throws Exception {
        String ifMatch = "\"" + versionFor(ACCOUNT_WITH_ENFORCEMENT_HOLD) + "\"";

        ResultActions resultActions = mockMvc.perform(
            patch(URL_BASE + "/{defendantAccountId}/remove-enf-hold", ACCOUNT_WITH_ENFORCEMENT_HOLD)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("Business-Unit-Id", BUSINESS_UNIT_ID)
                .header("If-Match", ifMatch)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .content("""
                    {
                      "reason": "remove hold reason"
                    }
                    """)
        );
        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.defendantAccountId").value(String.valueOf(ACCOUNT_WITH_ENFORCEMENT_HOLD)));

        String lastEnforcement = jdbcTemplate.queryForObject(
            "SELECT last_enforcement FROM defendant_accounts WHERE defendant_account_id = ?",
            String.class,
            ACCOUNT_WITH_ENFORCEMENT_HOLD
        );

        org.junit.jupiter.api.Assertions.assertNull(lastEnforcement);
    }

    @Test
    @DisplayName("Remove enforcement hold returns 403 when access token is missing")
    @JiraStory("PO-1775")
    @JiraEpic("PO-1675")
    @JiraTestKey("PO-5992")
    void int02_removeEnforcementHold_returnsUnauthorized_whenAccessTokenMissing() throws Exception {
        userStateStub.setupWithNoPermissions();
        
        String ifMatch = "\"" + versionFor(ACCOUNT_WITH_ENFORCEMENT_HOLD) + "\"";

        ResultActions resultActions = mockMvc.perform(
            patch(URL_BASE + "/{defendantAccountId}/remove-enf-hold", ACCOUNT_WITH_ENFORCEMENT_HOLD)
                .header("Business-Unit-Id", BUSINESS_UNIT_ID)
                .header("If-Match", ifMatch)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_PROBLEM_JSON)
                .content("""
                    {
                      "reason": "remove hold reason"
                    }
                    """)
        );

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Remove enforcement hold returns 403 when user lacks permission")
    @JiraStory("PO-1775")
    @JiraEpic("PO-1675")
    @JiraTestKey("PO-5993")
    void int03_removeEnforcementHold_returnsForbidden_whenUserLacksPermission() throws Exception {
        userStateStub.setupWithNoPermissions();
        String ifMatch = "\"" + versionFor(ACCOUNT_WITH_ENFORCEMENT_HOLD) + "\"";

        ResultActions resultActions = mockMvc.perform(
            patch(URL_BASE + "/{defendantAccountId}/remove-enf-hold", ACCOUNT_WITH_ENFORCEMENT_HOLD)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("Business-Unit-Id", BUSINESS_UNIT_ID)
                .header("If-Match", ifMatch)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_PROBLEM_JSON)
                .content("""
                    {
                      "reason": "remove hold reason"
                    }
                    """)
        );

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("Remove enforcement hold returns 404 when defendant account is not found")
    @JiraStory("PO-1775")
    @JiraEpic("PO-1675")
    @JiraTestKey("PO-5994")
    void int04_removeEnforcementHold_returnsNotFound_whenAccountDoesNotExist() throws Exception {
        String ifMatch = "\"" + versionFor(ACCOUNT_WITH_ENFORCEMENT_HOLD) + "\"";

        long missingAccountId = 999999999L;

        ResultActions resultActions = mockMvc.perform(
            patch(URL_BASE + "/{defendantAccountId}/remove-enf-hold", missingAccountId)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("Business-Unit-Id", BUSINESS_UNIT_ID)
                .header("If-Match", ifMatch)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_PROBLEM_JSON)
                .content("""
                    {
                      "reason": "remove hold reason"
                    }
                    """)
        );

        resultActions.andExpect(status().isNotFound())
            .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @DisplayName("Remove enforcement hold returns 409 when If-Match does not match")
    @JiraStory("PO-1775")
    @JiraEpic("PO-1675")
    @JiraTestKey("PO-5995")
    void int05_removeEnforcementHold_returnsConflict_whenIfMatchDoesNotMatch() throws Exception {
        int invalidVersion = versionFor(ACCOUNT_WITH_ENFORCEMENT_HOLD) + 1;

        ResultActions resultActions = mockMvc.perform(
            patch(URL_BASE + "/{defendantAccountId}/remove-enf-hold", ACCOUNT_WITH_ENFORCEMENT_HOLD)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .header("Business-Unit-Id", BUSINESS_UNIT_ID)
                .header("If-Match", "\"" + invalidVersion + "\"")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_PROBLEM_JSON)
                .content("""
                    {
                      "reason": "remove hold reason"
                    }
                    """)
        );

        resultActions.andExpect(status().isConflict())
            .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }
}
