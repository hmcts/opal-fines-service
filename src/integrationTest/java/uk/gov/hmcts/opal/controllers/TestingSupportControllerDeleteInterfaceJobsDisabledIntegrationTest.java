package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {"opal.testing-support-endpoints.enabled=false"})
public class TestingSupportControllerDeleteInterfaceJobsDisabledIntegrationTest extends AbstractIntegrationTest {
    private static final String URL = "/testing-support/interface-jobs";

    @Test
    @JiraStory("PO-2578")
    @JiraEpic("PO-2468")
    void shouldReturn404() throws Exception {
        mockMvc.perform(delete(URL)
            .queryParam("ids", "987651")
            .accept(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(status().isNotFound());
    }
}

