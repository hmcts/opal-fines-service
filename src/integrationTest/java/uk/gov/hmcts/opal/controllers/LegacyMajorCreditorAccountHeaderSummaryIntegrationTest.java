package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountService.GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetMajorCreditorAccountHeaderSummaryLegacyResponse;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@DisplayName("Major Creditor Account Header Summary Legacy Integration Tests")
@Slf4j(topic = "opal.LegacyMajorCreditorAccountHeaderSummaryIntegrationTest")
class LegacyMajorCreditorAccountHeaderSummaryIntegrationTest extends AbstractIntegrationTest {
    
    private static final String URL = "/major-creditor-accounts/{id}/header-summary";

    @MockitoSpyBean
    private GatewayService gatewayService;

    @Test
    @DisplayName("PO-2136 INT.01 to INT.04 - valid request returns mapped body and ETag")
    @JiraStory("PO-2136")
    @JiraEpic("FAE: View Major Creditor Account Summary")
    void getHeaderSummary_successReturnsMappedResponseAndEtag() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);

        ResultActions resultActions = mockMvc.perform(get(URL, 99000000000800L)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":getHeaderSummary_successReturnsMappedResponseAndEtag: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"7\""))
            .andExpect(jsonPath("$.major_creditor.creditor_account_id").value(99000000000800L))
            .andExpect(jsonPath("$.major_creditor.account_number").value("87654321"))
            .andExpect(jsonPath("$.major_creditor.name").value("Major Creditor Test Ltd"))
            .andExpect(jsonPath("$.major_creditor.account_reference.account_type").value("MJ"))
            .andExpect(jsonPath("$.major_creditor.account_reference.display_name").value("Major Creditor"))
            .andExpect(jsonPath("$.major_creditor.account_version").doesNotExist())
            .andExpect(jsonPath("$.business_unit_details.business_unit_id").value("77"))
            .andExpect(jsonPath("$.business_unit_details.business_unit_name").value("Camberwell Green"))
            .andExpect(jsonPath("$.business_unit_details.welsh_speaking").value("N"))
            .andExpect(jsonPath("$.awaiting_payout").value(123.45));

        ArgumentCaptor<GetMajorCreditorAccountHeaderSummaryLegacyRequest> requestCaptor =
            ArgumentCaptor.forClass(GetMajorCreditorAccountHeaderSummaryLegacyRequest.class);
        verify(gatewayService).postToGateway(
            eq(GET_MAJOR_CREDITOR_ACCOUNT_HEADER_SUMMARY),
            eq(GetMajorCreditorAccountHeaderSummaryLegacyResponse.class),
            requestCaptor.capture(),
            isNull()
        );
        assertThat(requestCaptor.getValue().getCreditorAccountId()).isEqualTo("99000000000800");
    }

    @Test
    @DisplayName("PO-2136 INT.05 - repeated request returns consistent body and ETag")
    @JiraStory("PO-2136")
    @JiraEpic("FAE: View Major Creditor Account Summary")
    void getHeaderSummary_repeatedRequestReturnsConsistentResponse() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);

        ResultActions first = mockMvc.perform(get(URL, 99000000000800L)
            .accept(MediaType.APPLICATION_JSON)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));
        ResultActions second = mockMvc.perform(get(URL, 99000000000800L)
            .accept(MediaType.APPLICATION_JSON)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));

        first.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, "\"7\""));
        second.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, "\"7\""));
        assertThat(second.andReturn().getResponse().getContentAsString())
            .isEqualTo(first.andReturn().getResponse().getContentAsString());
    }

    @Test
    @DisplayName("PO-2136 INT.07 - valid token without permission returns 403")
    @JiraStory("PO-2136")
    @JiraEpic("FAE: View Major Creditor Account Summary")
    void getHeaderSummary_withoutPermissionReturns403() throws Exception {
        userStateStub.setupWithNoPermissions();

        mockMvc.perform(get(URL, 99000000000800L)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(gatewayService);
    }

    @Test
    @DisplayName("PO-2136 INT.07 - permission in non-matching business unit returns 403")
    @JiraStory("PO-2136")
    @JiraEpic("FAE: View Major Creditor Account Summary")
    void getHeaderSummary_permissionInDifferentBusinessUnitReturns403() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 10, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);

        mockMvc.perform(get(URL, 99000000000800L)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("PO-2136 INT.08 - missing token returns 403 and does not invoke gateway")
    @JiraStory("PO-2136")
    @JiraEpic("FAE: View Major Creditor Account Summary")
    void getHeaderSummary_missingTokenReturns401() throws Exception {
        userStateStub.setupWithNoPermissions();

        mockMvc.perform(get(URL, 99000000000800L)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource"));

        verifyNoInteractions(gatewayService);
    }

    @Test
    @DisplayName("PO-2136 INT.09 - missing major creditor account returns 404")
    @JiraStory("PO-2136")
    @JiraEpic("FAE: View Major Creditor Account Summary")
    void getHeaderSummary_notFoundReturns404() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);

        mockMvc.perform(get(URL, 999999L)
                .accept(MediaType.APPLICATION_JSON)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
