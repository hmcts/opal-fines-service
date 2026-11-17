package uk.gov.hmcts.opal.controllers.r1a;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import uk.gov.hmcts.opal.controllers.shared.util.DraftAccountTestData;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

/**
 * Verifies that rejected draft-account creation never reaches the PDPO logging publisher.
 *
 * <p>The functional scenarios PO-5624 and PO-5640 still verify HTTP 401 against the deployed API.
 * Their former operation-wide PDPO baseline assertion could fail when unrelated requests created
 * logs on shared environments such as demo. A rejected creation has no account identifier, and
 * the logging search API has no request-correlation filter to isolate its logs.
 *
 * <p>This security integration test therefore checks both HTTP 401 and no interaction with the
 * logging publisher, preserving the no-PDPO side-effect assertion without depending on shared logs.
 */
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1a=true",
    "launchdarkly.default-flag-values.is-legacy-mode=false"
})
@DisplayName("Draft account creation security and PDPO logging")
class DraftAccountCreationSecurityIT extends AbstractIntegrationWithSecurityTest {

    @MockitoBean
    private LoggingService loggingService;

    @Test
    @JiraStory("PO-2357")
    @JiraEpic("PO-2355")
    @DisplayName("PO-2357 - Invalid token rejects draft-account creation without publishing PDPO logs")
    void invalidTokenDoesNotPublishPdpoLogs() throws Exception {
        assertRejectedWithoutPdpoLogs("invalidToken");
    }

    @Test
    @JiraStory("PO-2357")
    @JiraEpic("PO-2355")
    @DisplayName("PO-2357 - Expired token rejects draft-account creation without publishing PDPO logs")
    void expiredTokenDoesNotPublishPdpoLogs() throws Exception {
        assertRejectedWithoutPdpoLogs(expiredToken);
    }

    private void assertRejectedWithoutPdpoLogs(String token) throws Exception {
        mockMvc.perform(post("/draft-accounts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(DraftAccountTestData.Post.defaultData().toJson()))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(loggingService);
    }
}
