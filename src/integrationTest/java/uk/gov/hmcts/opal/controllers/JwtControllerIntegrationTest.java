package uk.gov.hmcts.opal.controllers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.gov.hmcts.opal.util.FeatureFlags;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@Slf4j(topic = "opal.JwtControllerIntegrationTest")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1a=true"
})
@DisplayName("JWT Controller Integration Tests")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    FeatureFlags.RELEASE_1A_ENABLED_PROPERTY + "=true"
})
class JwtControllerIntegrationTest extends AbstractIntegrationWithSecurityTest {

    private static final String URL = "/business-units/5";
    public static final String AUTHORIZATION = "authorization";

    @Test
    @DisplayName("Testing Valid Token")
    @JiraStory("PO-2833")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-5906")
    void testValidJwtTokenGivesOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(AUTHORIZATION, userStateStub.getAuthorizationToken()))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Testing Feature-Disabled User Service Lookup")
    @JiraStory("PO-6359")
    @JiraEpic("PO-3685")
    @JiraTestKey("PO-7581")
    void testFeatureDisabledUserStateLookupGivesServiceUnavailableProblemJson() throws Exception {
        WireMock.configureFor("localhost", 4553);
        StubMapping disabledUserStateStub = stubFor(get("/opal/v2/users/0/state")
            .withHeader("Authorization", equalTo("Bearer " + validToken))
            .atPriority(1)
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                .withBody(
                    """
                    {
                      "type":"https://hmcts.gov.uk/problems/feature-disabled",
                      "title":"Feature Disabled",
                      "status":404,
                      "detail":"The requested feature is not currently available"
                    }
                    """
                )));

        try {
            mockMvc.perform(MockMvcRequestBuilders.get(URL)
                    .header(AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type")
                    .value("https://hmcts.gov.uk/problems/downstream-service-unavailable"))
                .andExpect(jsonPath("$.title").value("Service Unavailable"))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.detail").value(
                    "Authentication was not possible because the required user-service endpoint is disabled."
                ))
                .andExpect(jsonPath("$.instance").exists())
                .andExpect(jsonPath("$.operation_id").exists())
                .andExpect(jsonPath("$.retriable").value(false));
        } finally {
            WireMock.removeStub(disabledUserStateStub);
        }
    }

    @Test
    @DisplayName("Testing Expired JWT Token")
    @JiraStory("PO-2833")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-5907")
    void testExpiredTokenGivesUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL)
                .header(AUTHORIZATION, "Bearer " + expiredToken))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Testing Invalid JWT Token")
    @JiraStory("PO-2833")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-5905")
    void testInvalidTokenGivesUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL)
                .header(AUTHORIZATION, "Bearer aa.bb.cc"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Testing Missing JWT Token")
    @JiraStory("PO-2833")
    @JiraEpic("PO-2233")
    @JiraTestKey("PO-5908")
    void testMissingTokenGivesUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL)
                .header(AUTHORIZATION, "Bearer "))
            .andExpect(status().isUnauthorized());
    }
}
