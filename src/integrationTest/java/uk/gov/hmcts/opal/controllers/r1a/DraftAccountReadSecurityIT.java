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
 * Verifies rejected draft-account reads never reach the PDPO publisher.
 * The functional tests check the deployed API returns 401. Valid list requests in shared staging
 * can produce audit records containing a draft ID from another scenario.
 */
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1a=true",
    "launchdarkly.default-flag-values.is-legacy-mode=false"
})
@DisplayName("Draft account read security and PDPO logging")
class DraftAccountReadSecurityIT extends AbstractIntegrationWithSecurityTest {

    @MockitoBean
    private LoggingService loggingService;

    @Test
    @JiraStory("PO-2361")
    @JiraEpic("PO-2355")
    @DisplayName("PO-5661 - Invalid token rejects draft listing without publishing PDPO logs")
    void invalidTokenDoesNotPublishPdpoLogs() throws Exception {
        assertRejectedWithoutPdpoLogs("/draft-accounts", "invalidToken");
    }

    @Test
    @JiraStory("PO-2361")
    @JiraEpic("PO-2355")
    @DisplayName("Expired token rejects draft listing without publishing PDPO logs")
    void expiredTokenDoesNotPublishPdpoLogs() throws Exception {
        assertRejectedWithoutPdpoLogs("/draft-accounts", expiredToken);
    }

    @Test
    @JiraStory("PO-2360")
    @JiraEpic("PO-2355")
    @DisplayName("PO-5639 - Invalid token rejects draft retrieval without publishing PDPO logs")
    void invalidTokenOnGetByIdDoesNotPublishPdpoLogs() throws Exception {
        assertRejectedWithoutPdpoLogs("/draft-accounts/1", "invalidToken");
    }

    private void assertRejectedWithoutPdpoLogs(String path, String token) throws Exception {
        mockMvc.perform(get(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(loggingService);
    }
}
