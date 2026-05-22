package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.LegacyDefendantsEnforcementIntegrationTest")
class LegacyDefendantsEnforcementIntegrationTest extends AbstractLegacyDefendantsIntegrationTest {

    private static final String ENFORCEMENT_REQUEST = """
        {
          "result_id": "CONF",
          "enforcement_result_responses": [
            {
              "parameter_name": "amount_due",
              "response": "100.00"
            },
            {
              "parameter_name": "next_payment_date",
              "response": "2026-01-15"
            }
          ],
          "payment_terms": {
            "days_in_default": 30,
            "date_days_in_default_imposed": "2025-11-01",
            "extension": true,
            "reason_for_extension": "Financial hardship",
            "payment_terms_type": {
              "payment_terms_type_code": "B"
            },
            "effective_date": "2025-11-15",
            "instalment_period": {
              "instalment_period_code": "M"
            },
            "lump_sum_amount": 0.00,
            "instalment_amount": 150.00,
            "posted_details": {
              "posted_date": "2025-11-02T10:30:00",
              "posted_by": "System",
              "posted_by_name": "System User"
            }
          }
        }
        """;

    private static final String REMOVE_ENFORCEMENT_REQUEST = """
        {
          "reason": "remove hold reason"
        }
        """;

    private static final String BUSINESS_UNIT_ID = "78";
    private static final String IF_MATCH_VERSION = "1";

    private HttpHeaders enforcementHeaders(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "1");
        return headers;
    }

    @Test
    @DisplayName("LEGACY: POST Add Enforcement - success")
    @JiraStory("PO-1918")
    @JiraEpic("PO-1675")
    void testPostAddEnforcement_Success() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        var res = mockMvc.perform(
            post("/defendant-accounts/72/enforcements")
                .headers(enforcementHeaders("good_token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ENFORCEMENT_REQUEST)
        );

        res.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.enforcement_id").value("72"))
            .andExpect(jsonPath("$.defendant_account_id").value("72"))
            .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    @DisplayName("LEGACY: POST Add Enforcement - backend 500")
    @JiraStory("PO-1918")
    @JiraEpic("PO-1675")
    void testPostAddEnforcement_500Error() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        var res = mockMvc.perform(
            post("/defendant-accounts/500/enforcements")
                .headers(enforcementHeaders("good_token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ENFORCEMENT_REQUEST)
        );

        res.andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("LEGACY: POST Add Enforcement - forbidden without ENTER_ENFORCEMENT")
    @JiraStory("PO-1918")
    @JiraEpic("PO-1675")
    void testPostAddEnforcement_403Forbidden() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            UserState.builder()
                .userId(999L)
                .userName("no-permission-user")
                .businessUnitUser(Collections.emptySet())
                .build()
        );

        ResultActions res = mockMvc.perform(
            post("/defendant-accounts/72/enforcements")
                .headers(enforcementHeaders("good_token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ENFORCEMENT_REQUEST)
        );

        String responseBody = res.andReturn().getResponse().getContentAsString();
        log.info(":legacyPostAddEnforcement_403Forbidden response:\n{}", ToJsonString.toPrettyJson(responseBody));

        res.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @DisplayName("LEGACY: POST Add Enforcement - unauthorized token rejected")
    @JiraStory("PO-1918")
    @JiraEpic("PO-1675")
    void testPostAddEnforcement_401Unauthorized() throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(
                post("/defendant-accounts/72/enforcements")
                    .headers(enforcementHeaders("bad_token"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ENFORCEMENT_REQUEST)
            )
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""));
    }

    private HttpHeaders removeEnforcementHeaders(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        headers.add("Business-Unit-Id", BUSINESS_UNIT_ID);
        headers.add(HttpHeaders.IF_MATCH, IF_MATCH_VERSION);
        return headers;
    }

    @Test
    @DisplayName("LEGACY: PATCH Remove Enforcement Hold - success")
    @JiraStory("PO-1919")
    @JiraEpic("PO-1675")
    void testPatchRemoveEnforcementHold_Success() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .patch("/defendant-accounts/72/remove-enf-hold")
                    .headers(removeEnforcementHeaders("good_token"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REMOVE_ENFORCEMENT_REQUEST)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("LEGACY: PATCH Remove Enforcement Hold - forbidden without ENTER_ENFORCEMENT")
    @JiraStory("PO-1919")
    @JiraEpic("PO-1675")
    void testPatchRemoveEnforcementHold_403Forbidden() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            UserState.builder()
                .userId(999L)
                .userName("no-permission-user")
                .businessUnitUser(Collections.emptySet())
                .build()
        );

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .patch("/defendant-accounts/72/remove-enf-hold")
                    .headers(removeEnforcementHeaders("good_token"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REMOVE_ENFORCEMENT_REQUEST)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("LEGACY: PATCH Remove Enforcement Hold - unauthorized token rejected")
    @JiraStory("PO-1919")
    @JiraEpic("PO-1675")
    void testPatchRemoveEnforcementHold_401Unauthorized() throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .patch("/defendant-accounts/72/remove-enf-hold")
                    .headers(removeEnforcementHeaders("bad_token"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REMOVE_ENFORCEMENT_REQUEST)
            )
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""));
    }

    @Test
    @DisplayName("LEGACY: PATCH Remove Enforcement Hold - missing If-Match rejected")
    @JiraStory("PO-1919")
    @JiraEpic("PO-1675")
    void testPatchRemoveEnforcementHold_missingIfMatch() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", BUSINESS_UNIT_ID);

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .patch("/defendant-accounts/72/remove-enf-hold")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REMOVE_ENFORCEMENT_REQUEST)
            )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("LEGACY: PATCH Remove Enforcement Hold - invalid If-Match rejected")
    @JiraStory("PO-1919")
    @JiraEpic("PO-1675")
    void testPatchRemoveEnforcementHold_invalidIfMatch() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", BUSINESS_UNIT_ID);
        headers.add(HttpHeaders.IF_MATCH, "not-a-version");

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .patch("/defendant-accounts/72/remove-enf-hold")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(REMOVE_ENFORCEMENT_REQUEST)
            )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
