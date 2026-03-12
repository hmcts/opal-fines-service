package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;

@Slf4j(topic = "opal.JwtControllerIntegrationTest")
@DisplayName("JWT Controller Integration Tests")
class JwtControllerIntegrationTest extends AbstractIntegrationWithSecurityTest {

    private static final String URL = "/business-units/5";
    public static final String AUTHORIZATION = "authorization";

    @Test
    @DisplayName("Testing Valid Token")
    void testValidJwtTokenGivesOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL)
                .header(AUTHORIZATION, "Bearer " + validToken()))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Testing Expired JWT Token")
    void testExpiredTokenGivesUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL)
                .header(AUTHORIZATION, "Bearer " + expiredToken()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Testing Invalid JWT Token")
    void testInvalidTokenGivesUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL)
                .header(AUTHORIZATION, "Bearer aa.bb.cc"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Testing Missing JWT Token")
    void testMissingTokenGivesUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(URL)
                .header(AUTHORIZATION, "Bearer "))
            .andExpect(status().isUnauthorized());
    }
}
