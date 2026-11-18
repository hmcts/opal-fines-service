package uk.gov.hmcts.opal.controllers.r1a;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

/**
 * Verifies rejected draft-list requests never reach the PDPO publisher. Functional scenario
 * PO-5661 checks the deployed API returns 401; its former absence check could pick up valid
 * list consultations containing the same draft ID from other requests on a shared environment.
 * The logging search API does not provide a request-correlation filter.
 */
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1a=true",
    "launchdarkly.default-flag-values.is-legacy-mode=false"
})
@DisplayName("Draft account listing security and PDPO logging")
class DraftAccountListSecurityIT extends AbstractIntegrationWithSecurityTest {

    @MockitoBean
    private LoggingService loggingService;

    @Test
    @JiraStory("PO-2361")
    @JiraEpic("PO-2355")
    @DisplayName("PO-5661 - Invalid token rejects draft listing without publishing PDPO logs")
    void invalidTokenDoesNotPublishPdpoLogs() throws Exception {
        assertRejectedWithoutPdpoLogs("invalidToken");
    }

    @Test
    @JiraStory("PO-2361")
    @JiraEpic("PO-2355")
    @DisplayName("Expired token rejects draft listing without publishing PDPO logs")
    void expiredTokenDoesNotPublishPdpoLogs() throws Exception {
        assertRejectedWithoutPdpoLogs(expiredToken);
    }

    private void assertRejectedWithoutPdpoLogs(String token) throws Exception {
        mockMvc.perform(get("/draft-accounts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(loggingService);
    }
}
