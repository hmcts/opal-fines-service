package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noPermissionsUser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("Remove Defendant Account Enforcement Hold Integration Tests")
class OpalDefendantAccountRemoveEnforcementHoldIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    static final String URL_BASE = "/defendant-accounts";

    private static final long ACCOUNT_WITH_ENFORCEMENT_HOLD = 99000000000002L;
    private static final short BUSINESS_UNIT_ID = 77;
    private static final String IF_MATCH = "\"1\"";

    @Test
    @DisplayName("Remove enforcement hold returns 200 and updates the defendant account")
    void int01_removeEnforcementHold_returnsOkAndUpdatesDatabase() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(
            patch(URL_BASE + "/{defendantAccountId}/remove-enf-hold", ACCOUNT_WITH_ENFORCEMENT_HOLD)
                .header("authorization", "Bearer some_value")
                .header("Business-Unit-Id", BUSINESS_UNIT_ID)
                .header("If-Match", IF_MATCH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .content("""
                    {
                      "reason": "remove hold reason"
                    }
                    """)
        );
        resultActions.andExpect(status().isConflict())
            .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("Remove enforcement hold returns 401 when access token is missing")
    void int02_removeEnforcementHold_returnsUnauthorized_whenAccessTokenMissing() throws Exception {
        ResultActions resultActions = mockMvc.perform(
            patch(URL_BASE + "/{defendantAccountId}/remove-enf-hold", ACCOUNT_WITH_ENFORCEMENT_HOLD)
                .header("Business-Unit-Id", BUSINESS_UNIT_ID)
                .header("If-Match", IF_MATCH)
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
    @DisplayName("Remove enforcement hold returns 403 when user lacks permission")
    void int03_removeEnforcementHold_returnsForbidden_whenUserLacksPermission() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionsUser());

        ResultActions resultActions = mockMvc.perform(
            patch(URL_BASE + "/{defendantAccountId}/remove-enf-hold", ACCOUNT_WITH_ENFORCEMENT_HOLD)
                .header("authorization", "Bearer some_value")
                .header("Business-Unit-Id", BUSINESS_UNIT_ID)
                .header("If-Match", IF_MATCH)
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
    void int04_removeEnforcementHold_returnsNotFound_whenAccountDoesNotExist() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        long missingAccountId = 999999999L;

        ResultActions resultActions = mockMvc.perform(
            patch(URL_BASE + "/{defendantAccountId}/remove-enf-hold", missingAccountId)
                .header("authorization", "Bearer some_value")
                .header("Business-Unit-Id", BUSINESS_UNIT_ID)
                .header("If-Match", IF_MATCH)
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
    void int05_removeEnforcementHold_returnsConflict_whenIfMatchDoesNotMatch() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(
            patch(URL_BASE + "/{defendantAccountId}/remove-enf-hold", ACCOUNT_WITH_ENFORCEMENT_HOLD)
                .header("authorization", "Bearer some_value")
                .header("Business-Unit-Id", BUSINESS_UNIT_ID)
                .header("If-Match", "\"999\"")
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
