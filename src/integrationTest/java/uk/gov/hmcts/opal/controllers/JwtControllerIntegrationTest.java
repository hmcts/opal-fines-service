package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
                .header(AUTHORIZATION, "Bearer " + validToken))
            .andExpect(status().isOk());
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
