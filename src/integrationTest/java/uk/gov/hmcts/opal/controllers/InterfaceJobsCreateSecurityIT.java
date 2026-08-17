package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1c-payment=true"
})
@DisplayName("Interface Jobs Create Security Integration Tests")
class InterfaceJobsCreateSecurityIT extends AbstractIntegrationWithSecurityTest {

    private static final String URL = "/interface-jobs";

    @Test
    @DisplayName("PO-2577 INT.01 - Rejects create without token")
    @JiraStory("PO-2577")
    @JiraEpic("PO-304")
    @JiraTestKey("PO-10084")
    @Disabled
    void rejectsCreateWithoutToken() throws Exception {
        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("""
                         {
                           "interface_jobs": [
                             {
                               "file_name": "missing-token.dat",
                               "source": "NATWEST",
                               "records": "[{\\"account\\":\\"abc123\\"}]",
                               "business_unit_id": 2577,
                               "interface_name": "Missing Token Interface Jobs",
                               "created_datetime": "2026-07-14T10:00:00"
                             }
                           ]
                         }
                         """))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/unauthorized"))
            .andExpect(jsonPath("$.title").value("Unauthorized"))
            .andExpect(jsonPath("$.detail").value("You are not authorized to access this resource"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.properties.retriable").value(false));
    }
}
