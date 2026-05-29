package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@DisplayName("Content-Digest default configuration integration tests")
class ContentDigestIntegrationTest extends AbstractContentDigestIntegrationTest {

    @Test
    @JiraStory("PO-2878")
    @JiraEpic("PO-2675")
    @JiraTestKey("PO-5798")
    void missingHeader_returnsSuccessWithoutResponseContentDigest() throws Exception {
        mockMvc.perform(get(ROOT_ENDPOINT))
            .andExpect(status().isOk())
            .andExpect(header().doesNotExist(CONTENT_DIGEST));
    }
}
