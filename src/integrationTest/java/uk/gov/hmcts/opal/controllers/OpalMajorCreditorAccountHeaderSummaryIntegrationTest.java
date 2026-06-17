package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@DisplayName("Major Creditor Account Header Summary Opal Integration Tests")
@Slf4j(topic = "opal.OpalMajorCreditorAccountHeaderSummaryIntegrationTest")
class OpalMajorCreditorAccountHeaderSummaryIntegrationTest extends AbstractIntegrationTest {

    private static final String URL = "/major-creditor-accounts/{id}/header-summary";

    @Test
    @DisplayName("PO-2136 Opal valid request returns mapped body and ETag")
    @JiraStory("PO-2136")
    @JiraEpic("FAE: View Major Creditor Account Summary")
    void getHeaderSummary_successReturnsMappedResponseAndEtag() throws Exception {
        ResultActions resultActions = mockMvc.perform(get(URL, 10770000000041L)
            .accept(MediaType.APPLICATION_JSON)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":getHeaderSummary_successReturnsMappedResponseAndEtag: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
            .andExpect(jsonPath("$.major_creditor.creditor_account_id").value(10770000000041L))
            .andExpect(jsonPath("$.major_creditor.account_number").value("00001235G"))
            .andExpect(jsonPath("$.major_creditor.name").value("TFL2 ATCM Testing"))
            .andExpect(jsonPath("$.major_creditor.account_reference.account_type").value("MJ"))
            .andExpect(jsonPath("$.major_creditor.account_reference.display_name").value("Major Creditor"))
            .andExpect(jsonPath("$.business_unit_details.business_unit_id").value("77"))
            .andExpect(jsonPath("$.business_unit_details.business_unit_name").value("Camberwell Green"))
            .andExpect(jsonPath("$.business_unit_details.welsh_speaking").value("N"))
            .andExpect(jsonPath("$.awaiting_payout").value(0));
    }

    @Test
    @DisplayName("PO-2136 Opal missing major creditor account returns 404")
    @JiraStory("PO-2136")
    @JiraEpic("FAE: View Major Creditor Account Summary")
    void getHeaderSummary_notFoundReturns404() throws Exception {
        mockMvc.perform(get(URL, 999999L)
                .accept(MediaType.APPLICATION_JSON)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
