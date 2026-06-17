package uk.gov.hmcts.opal.controllers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
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
import static uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService.GET_DEFENDANT_ACCOUNT_HISTORY;

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
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHistoryRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHistoryResponse;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@DisplayName("Defendant Account History Legacy Integration Tests")
@Slf4j(topic = "opal.LegacyDefendantAccountHistoryIntegrationTest")
class LegacyDefendantAccountHistoryIntegrationTest extends AbstractLegacyDefendantsIntegrationTest {

    private static final long DEFENDANT_ACCOUNT_ID = 99000000000001L;
    private static final String HISTORY_URL = URL_BASE + "/{id}/history";

    @MockitoSpyBean
    private GatewayService gatewayService;

    @Test
    @DisplayName("PO-2647 INT.01 legacy history returns mixed items and maps them")
    @JiraStory("PO-2647")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_successReturnsMappedResponse() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        stubLegacyHistoryResponse(200, fullHistoryXml());

        ResultActions resultActions = mockMvc.perform(get(HISTORY_URL, DEFENDANT_ACCOUNT_ID)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":getDefendantAccountHistory_successReturnsMappedResponse: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"7\""))
            .andExpect(jsonPath("$.historyItems[0].type").value("Note"))
            .andExpect(jsonPath("$.historyItems[0].details.noteText").value("Legacy account note"))
            .andExpect(jsonPath("$.historyItems[1].type").value("Financial"))
            .andExpect(jsonPath("$.historyItems[1].amount").value(-25.50))
            .andExpect(jsonPath("$.historyItems[1].details.transactionType.transactionType").value("PAYMNT"))
            .andExpect(jsonPath("$.historyItems[2].type").value("Payment terms"))
            .andExpect(jsonPath("$.historyItems[2].details.payment_terms_type.payment_terms_type_code").value("I"))
            .andExpect(jsonPath("$.historyItems[3].type").value("Enforcement"))
            .andExpect(jsonPath("$.historyItems[3].details.enforcementAction").value("FSN"))
            .andExpect(jsonPath("$.historyItems[4].type").value("Amendment"))
            .andExpect(jsonPath("$.historyItems[4].details.attributeName").value("Account status"));

        ArgumentCaptor<LegacyGetDefendantAccountHistoryRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyGetDefendantAccountHistoryRequest.class);
        verify(gatewayService).postToGateway(
            eq(GET_DEFENDANT_ACCOUNT_HISTORY),
            eq(LegacyGetDefendantAccountHistoryResponse.class),
            requestCaptor.capture(),
            isNull()
        );
        assertThat(requestCaptor.getValue().getDefendantAccountId()).isEqualTo("99000000000001");
        assertThat(requestCaptor.getValue().getFromDate()).isNull();
        assertThat(requestCaptor.getValue().getToDate()).isNull();
        assertThat(requestCaptor.getValue().getItemTypes()).isEmpty();
    }

    @Test
    @DisplayName("PO-2647 INT.07-INT.09 legacy history passes filters to gateway")
    @JiraStory("PO-2647")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_filtersAreForwardedToLegacyRequest() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        stubLegacyHistoryResponse(200, filteredHistoryXml());

        mockMvc.perform(get(HISTORY_URL, DEFENDANT_ACCOUNT_ID)
                .queryParam("dateFrom", "2026-05-11")
                .queryParam("dateTo", "2026-05-12")
                .queryParam("itemTypes", "enforcement,note")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.historyItems[0].type").value("Note"))
            .andExpect(jsonPath("$.historyItems[1].type").value("Enforcement"));

        ArgumentCaptor<LegacyGetDefendantAccountHistoryRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyGetDefendantAccountHistoryRequest.class);
        verify(gatewayService).postToGateway(
            eq(GET_DEFENDANT_ACCOUNT_HISTORY),
            eq(LegacyGetDefendantAccountHistoryResponse.class),
            requestCaptor.capture(),
            isNull()
        );
        assertThat(requestCaptor.getValue().getFromDate()).hasToString("2026-05-11");
        assertThat(requestCaptor.getValue().getToDate()).hasToString("2026-05-12");
        assertThat(requestCaptor.getValue().getItemTypes()).containsExactly("Enforcement", "Note");
    }

    @Test
    @DisplayName("PO-2647 INT.10 legacy history not found returns 404")
    @JiraStory("PO-2647")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_notFoundReturns404() throws Exception {
        userStateStub.setupWithNoPermissions();
        userStateStub.addPermissions((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        stubLegacyHistoryResponse(404, "<error><message>Defendant account history not found</message></error>");

        mockMvc.perform(get(HISTORY_URL, 99999999999999L)
                .accept(MediaType.APPLICATION_JSON)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("PO-2647 INT.10 legacy history without permission returns 403")
    @JiraStory("PO-2647")
    @JiraEpic("PO-812")
    void getDefendantAccountHistory_withoutPermissionReturns403() throws Exception {
        userStateStub.setupWithNoPermissions();

        mockMvc.perform(get(HISTORY_URL, DEFENDANT_ACCOUNT_ID)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(gatewayService);
    }

    private void stubLegacyHistoryResponse(int status, String body) {
        stubFor(post(urlPathEqualTo("/opal"))
            .withQueryParam("actionType", equalTo(GET_DEFENDANT_ACCOUNT_HISTORY))
            .willReturn(aResponse()
                .withStatus(status)
                .withHeader("Content-Type", MediaType.APPLICATION_XML_VALUE)
                .withBody(body)));
    }

    private String fullHistoryXml() {
        return """
            <response>
              <version>7</version>
              <historyItems>
                <historyItems_element>
                  <postedDetails>
                    <posted_date>2026-05-12T13:00:00</posted_date>
                    <posted_by>legacy-note</posted_by>
                    <posted_by_name>Legacy Note User</posted_by_name>
                  </postedDetails>
                  <type>Note</type>
                  <details>
                    <noteText>Legacy account note</noteText>
                  </details>
                </historyItems_element>
                <historyItems_element>
                  <postedDetails>
                    <posted_date>2026-05-12T12:00:00</posted_date>
                    <posted_by>legacy-fin</posted_by>
                    <posted_by_name>Legacy Financial User</posted_by_name>
                  </postedDetails>
                  <type>Financial</type>
                  <details>
                    <transactionType>
                      <transactionType>PAYMNT</transactionType>
                      <transactionTypeDisplayName>Payment</transactionTypeDisplayName>
                    </transactionType>
                    <paymentMethod>
                      <paymentMethod>NC</paymentMethod>
                      <paymentMethodDisplayName>National cheque</paymentMethodDisplayName>
                    </paymentMethod>
                    <paymentReference>PAY123</paymentReference>
                    <additionalInformation>Legacy payment</additionalInformation>
                    <status>
                      <defendantTransactionStatus>P</defendantTransactionStatus>
                      <defendantTransactionStatusDisplayName>Partially-reversed</defendantTransactionStatusDisplayName>
                    </status>
                    <statusDate>2026-05-12T12:05:00</statusDate>
                    <associatedRecordType>defendant_accounts</associatedRecordType>
                    <associatedRecordId>99000000000001</associatedRecordId>
                    <accountNumber>ACC-99000000000001</accountNumber>
                    <sendingCourt>Legacy Court</sendingCourt>
                  </details>
                  <amount>-25.50</amount>
                </historyItems_element>
                <historyItems_element>
                  <postedDetails>
                    <posted_date>2026-05-12T09:30:00</posted_date>
                    <posted_by>legacy-pt</posted_by>
                    <posted_by_name>Legacy Terms User</posted_by_name>
                  </postedDetails>
                  <type>Payment terms</type>
                  <details>
                    <daysInDefault>7</daysInDefault>
                    <date_days_in_default_imposed>2026-05-12</date_days_in_default_imposed>
                    <reason_for_extension>Legacy extension</reason_for_extension>
                    <payment_terms_type>
                      <payment_terms_type_code>I</payment_terms_type_code>
                    </payment_terms_type>
                    <effective_date>2026-05-20</effective_date>
                    <instalment_period>
                      <installment_period_code>M</installment_period_code>
                    </instalment_period>
                    <lump_sum_amount>100.00</lump_sum_amount>
                    <instalment_amount>25.00</instalment_amount>
                  </details>
                </historyItems_element>
                <historyItems_element>
                  <postedDetails>
                    <posted_date>2026-05-11T15:00:00</posted_date>
                    <posted_by>legacy-enf</posted_by>
                    <posted_by_name>Legacy Enforcement User</posted_by_name>
                  </postedDetails>
                  <type>Enforcement</type>
                  <details>
                    <enforcementAction>FSN</enforcementAction>
                    <daysInDefault>14</daysInDefault>
                    <warrantNumber>WR123</warrantNumber>
                    <caseNumber>CASE-1</caseNumber>
                    <reason>Test enforcement</reason>
                  </details>
                </historyItems_element>
                <historyItems_element>
                  <postedDetails>
                    <posted_date>2026-05-10T11:00:00</posted_date>
                    <posted_by>legacy-amend</posted_by>
                    <posted_by_name>Legacy Amend User</posted_by_name>
                  </postedDetails>
                  <type>Amendment</type>
                  <details>
                    <attributeName>Account status</attributeName>
                    <oldValue>Old</oldValue>
                    <newValue>New</newValue>
                  </details>
                </historyItems_element>
              </historyItems>
            </response>
            """;
    }

    private String filteredHistoryXml() {
        return """
            <response>
              <version>7</version>
              <historyItems>
                <historyItems_element>
                  <postedDetails>
                    <posted_date>2026-05-12T13:00:00</posted_date>
                    <posted_by>legacy-note</posted_by>
                    <posted_by_name>Legacy Note User</posted_by_name>
                  </postedDetails>
                  <type>Note</type>
                  <details>
                    <noteText>Legacy account note</noteText>
                  </details>
                </historyItems_element>
                <historyItems_element>
                  <postedDetails>
                    <posted_date>2026-05-11T15:00:00</posted_date>
                    <posted_by>legacy-enf</posted_by>
                    <posted_by_name>Legacy Enforcement User</posted_by_name>
                  </postedDetails>
                  <type>Enforcement</type>
                  <details>
                    <enforcementAction>FSN</enforcementAction>
                    <daysInDefault>14</daysInDefault>
                    <reason>Test enforcement</reason>
                  </details>
                </historyItems_element>
              </historyItems>
            </response>
            """;
    }
}
