package uk.gov.hmcts.opal.controllers.r1b;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;

@ActiveProfiles({"integration", "legacy"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@DisplayName("Legacy Defendant Account Language Integration Tests")
class LegacyDefendantAccountLanguageIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/defendant-accounts";
    private static final long DEFENDANT_ACCOUNT_ID = 1060000002355L;

    @Test
    @JiraEpic("PO-812")
    @DisplayName("Legacy header summary maps Welsh business unit flag from gateway")
    void getHeaderSummary_mapsWelshSpeakingTrueToY() throws Exception {
        stubFor(post(urlPathEqualTo("/opal"))
            .withQueryParam("actionType", equalTo(LegacyDefendantAccountService.GET_HEADER_SUMMARY))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_XML_VALUE)
                .withBody(headerSummaryXml())));

        performGet("/header-summary")
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.business_unit_summary.business_unit_id").value(106))
            .andExpect(jsonPath("$.business_unit_summary.business_unit_name").value("North Wales"))
            .andExpect(jsonPath("$.business_unit_summary.welsh_speaking").value("Y"));
    }

    @Test
    @JiraEpic("PO-812")
    @DisplayName("Legacy at-a-glance maps document and hearing language codes from gateway")
    void getAtAGlance_mapsLanguagePreferences() throws Exception {
        stubFor(post(urlPathEqualTo("/opal"))
            .withQueryParam("actionType", equalTo(LegacyDefendantAccountService.GET_DEFENDANT_AT_A_GLANCE))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_XML_VALUE)
                .withBody(atAGlanceXml())));

        performGet("/at-a-glance")
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.language_preferences.document_language_preference.language_code")
                .value("CY"))
            .andExpect(jsonPath("$.language_preferences.document_language_preference.language_display_name")
                .value("Welsh and English"))
            .andExpect(jsonPath("$.language_preferences.hearing_language_preference.language_code")
                .value("CY"))
            .andExpect(jsonPath("$.language_preferences.hearing_language_preference.language_display_name")
                .value("Welsh and English"));

        verify(1,
            postRequestedFor(urlPathEqualTo("/opal"))
                .withQueryParam("actionType", equalTo(LegacyDefendantAccountService.GET_DEFENDANT_AT_A_GLANCE))
                .withRequestBody(matchingJsonPath(
                    "$.defendant_account_id", equalTo(String.valueOf(DEFENDANT_ACCOUNT_ID)))));
    }

    private ResultActions performGet(String path) throws Exception {
        return mockMvc.perform(get(URL_BASE + "/{id}" + path, DEFENDANT_ACCOUNT_ID)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header("authorization", userStateStub.getBearerToken())
            .accept(MediaType.APPLICATION_JSON));
    }

    private String headerSummaryXml() {
        return """
            <response>
              <version>1</version>
              <defendant_account_id>%s</defendant_account_id>
              <account_number>26000005K</account_number>
              <account_type>Fine</account_type>
              <business_unit_summary>
                <business_unit_name>North Wales</business_unit_name>
                <business_unit_id>106</business_unit_id>
                <business_unit_code>0094</business_unit_code>
                <welsh_speaking>true</welsh_speaking>
              </business_unit_summary>
            </response>
            """.formatted(DEFENDANT_ACCOUNT_ID);
    }

    private String atAGlanceXml() {
        return """
            <response>
              <version>1</version>
              <defendant_account_id>%s</defendant_account_id>
              <account_number>26000005K</account_number>
              <debtor_type>Defendant</debtor_type>
              <is_youth>false</is_youth>
              <language_preferences>
                <document_language_preference>
                  <language_code>CY</language_code>
                </document_language_preference>
                <hearing_language_preference>
                  <language_code>CY</language_code>
                </hearing_language_preference>
              </language_preferences>
            </response>
            """.formatted(DEFENDANT_ACCOUNT_ID);
    }
}
