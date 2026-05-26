package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@DisplayName("Content-Digest response generation integration tests")
@TestPropertySource(properties = {
    "opal.common.content-digest.request.enforce=false",
    "opal.common.content-digest.request.auto-generate=true",
    "opal.common.content-digest.response.enforce=false"
})
class ContentDigestResponseGenerationIntegrationTest extends AbstractContentDigestIntegrationTest {

    @Test
    @JiraStory("PO-2878")
    @JiraEpic("PO-2675")
    @JiraTestKey("PO-5804")
    void missingHeader_returnsSuccessWithResponseContentDigest() throws Exception {
        MvcResult result = mockMvc.perform(get(ROOT_ENDPOINT))
            .andExpect(status().isOk())
            .andReturn();

        assertValidResponseDigest(result);
    }

    @Test
    @JiraStory("PO-2878")
    @JiraEpic("PO-2675")
    @JiraTestKey("PO-5802")
    void invalidHeader_returnsContentDigestProblemResponse() throws Exception {
        MvcResult result = mockMvc.perform(get(ROOT_ENDPOINT).header(CONTENT_DIGEST, invalidDigest()))
            .andExpect(status().isBadRequest())
            .andReturn();

        assertContentDigestProblem(result, "Digest validation failed",
            "Body hash did not match for algorithm: sha-512");
        assertValidResponseDigest(result);
    }

    @Test
    @JiraStory("PO-2878")
    @JiraEpic("PO-2675")
    @JiraTestKey("PO-5803")
    void malformedHeader_returnsContentDigestProblemResponse() throws Exception {
        MvcResult result = mockMvc.perform(get(ROOT_ENDPOINT).header(CONTENT_DIGEST, malformedDigest()))
            .andExpect(status().isBadRequest())
            .andReturn();

        assertContentDigestProblem(result, "Invalid Content-Digest header",
            "No valid digest entries found in header.");
        assertValidResponseDigest(result);
    }
}
