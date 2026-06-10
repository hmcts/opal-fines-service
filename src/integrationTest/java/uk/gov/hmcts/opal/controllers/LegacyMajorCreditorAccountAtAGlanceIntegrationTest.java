package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noPermissionsUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;
import static uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountService.GET_MAJOR_CREDITOR_ACCOUNT_AT_A_GLANCE;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountAtAGlanceLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountAtAGlanceLegacyResponse;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@DisplayName("Major Creditor Account At A Glance Legacy Integration Tests")
@Slf4j(topic = "opal.LegacyMajorCreditorAccountAtAGlanceIntegrationTest")
class LegacyMajorCreditorAccountAtAGlanceIntegrationTest extends AbstractIntegrationTest {

    private static final String AUTH_HEADER = "Bearer some_value";
    private static final String URL = "/major-creditor-accounts/{id}/at-a-glance";

    @MockitoBean
    private UserStateService userStateService;

    @MockitoSpyBean
    private GatewayService gatewayService;

    @Test
    @DisplayName("PO-2137 INT.01 to INT.04 - valid request returns mapped body and ETag")
    @JiraStory("PO-2137")
    @JiraEpic("PO-1286")
    void getAtAGlance_successReturnsMappedResponseAndEtag() throws Exception {
        when(userStateService.checkForAuthorisedUser())
            .thenReturn(permissionUser((short) 10, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS));

        ResultActions resultActions = mockMvc.perform(get(URL, 99000000000800L)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":getAtAGlance_successReturnsMappedResponseAndEtag: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"7\""))
            .andExpect(jsonPath("$.major_creditor.creditor_account_id").value(99000000000800L))
            .andExpect(jsonPath("$.major_creditor.name").value("Major Creditor Test Ltd"))
            .andExpect(jsonPath("$.major_creditor.code").value("MC01"))
            .andExpect(jsonPath("$.major_creditor.address.line_1").value("1 Test Street"))
            .andExpect(jsonPath("$.major_creditor.address.line_2").value("London"))
            .andExpect(jsonPath("$.major_creditor.address.line_3").value("NW1"))
            .andExpect(jsonPath("$.major_creditor.address.postcode").value("NW1 1AA"))
            .andExpect(jsonPath("$.major_creditor.pay_by_bacs").value(true))
            .andExpect(jsonPath("$.major_creditor.creditor_account_version").doesNotExist());

        ArgumentCaptor<GetMajorCreditorAccountAtAGlanceLegacyRequest> requestCaptor =
            ArgumentCaptor.forClass(GetMajorCreditorAccountAtAGlanceLegacyRequest.class);
        verify(gatewayService).postToGateway(
            eq(GET_MAJOR_CREDITOR_ACCOUNT_AT_A_GLANCE),
            eq(GetMajorCreditorAccountAtAGlanceLegacyResponse.class),
            requestCaptor.capture(),
            isNull()
        );
        assertThat(requestCaptor.getValue().getCreditorAccountId()).isEqualTo("99000000000800");
    }

    @Test
    @DisplayName("PO-2137 INT.05 - repeated request returns consistent body and ETag")
    @JiraStory("PO-2137")
    @JiraEpic("PO-1286")
    void getAtAGlance_repeatedRequestReturnsConsistentResponse() throws Exception {
        when(userStateService.checkForAuthorisedUser())
            .thenReturn(permissionUser((short) 10, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS));

        ResultActions first = mockMvc.perform(get(URL, 99000000000800L)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER));
        ResultActions second = mockMvc.perform(get(URL, 99000000000800L)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER));

        first.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, "\"7\""));
        second.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, "\"7\""));
        assertThat(second.andReturn().getResponse().getContentAsString())
            .isEqualTo(first.andReturn().getResponse().getContentAsString());
    }

    @Test
    @DisplayName("PO-2137 INT.07 - valid token without permission returns 403")
    @JiraStory("PO-2137")
    @JiraEpic("PO-1286")
    void getAtAGlance_withoutPermissionReturns403() throws Exception {
        when(userStateService.checkForAuthorisedUser()).thenReturn(noPermissionsUser());

        mockMvc.perform(get(URL, 99000000000800L)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(gatewayService);
    }

    @Test
    @DisplayName("PO-2137 INT.08 - permission in different business unit still returns 200")
    @JiraStory("PO-2137")
    @JiraEpic("PO-1286")
    void getAtAGlance_permissionInDifferentBusinessUnitReturns200() throws Exception {
        when(userStateService.checkForAuthorisedUser())
            .thenReturn(permissionUser((short) 10, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS));

        mockMvc.perform(get(URL, 99000000000800L)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ETAG, "\"7\""));
    }

    @Test
    @DisplayName("PO-2137 INT.06 - missing token returns 401 and does not invoke gateway")
    @JiraStory("PO-2137")
    @JiraEpic("PO-1286")
    void getAtAGlance_missingTokenReturns401() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).checkForAuthorisedUser();

        mockMvc.perform(get(URL, 99000000000800L).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("Unauthorized"));

        verifyNoInteractions(gatewayService);
    }

    @Test
    @DisplayName("PO-2137 INT.09 - missing major creditor account returns 404")
    @JiraStory("PO-2137")
    @JiraEpic("PO-1286")
    void getAtAGlance_notFoundReturns404() throws Exception {
        when(userStateService.checkForAuthorisedUser())
            .thenReturn(permissionUser((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS));

        mockMvc.perform(get(URL, 999999L)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
