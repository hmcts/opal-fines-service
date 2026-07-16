package uk.gov.hmcts.opal.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@DisplayName("Content-Digest request and response integration tests")
@TestPropertySource(properties = {
    "opal.common.content-digest.request.enforce=false",
    "opal.common.content-digest.request.auto-generate=true",
    "opal.common.content-digest.response.enforce=true"
})
class ContentDigestRequestAndResponseIntegrationTest extends AbstractContentDigestIntegrationTest {

    @Test
    @JiraStory("PO-2878")
    @JiraEpic("PO-2675")
    @JiraTestKey("PO-5800")
    void invalidHeaderWhenEnforced_returnsContentDigestProblemResponse() throws Exception {
        MvcResult result = mockMvc.perform(post(POST_ENDPOINT)
            .contentType(APPLICATION_JSON)
            .content(POST_BODY)
            .header(CONTENT_DIGEST, invalidDigest()))
            .andExpect(status().isBadRequest())
            .andReturn();

        assertContentDigestProblem(result, "Digest validation failed",
            "Body hash did not match for algorithm: sha-512");
        assertValidResponseDigest(result);
    }

    @Test
    @JiraStory("PO-2878")
    @JiraEpic("PO-2675")
    @JiraTestKey("PO-5799")
    void malformedHeaderWhenEnforced_returnsContentDigestProblemResponse() throws Exception {
        MvcResult result = mockMvc.perform(post(POST_ENDPOINT)
            .contentType(APPLICATION_JSON)
            .content(POST_BODY)
            .header(CONTENT_DIGEST, malformedDigest()))
            .andExpect(status().isBadRequest())
            .andReturn();

        assertContentDigestProblem(result, "Invalid Content-Digest header",
            "No valid digest entries found in header.");
        assertValidResponseDigest(result);
    }

    @Test
    @JiraStory("PO-2878")
    @JiraEpic("PO-2675")
    @JiraTestKey("PO-5801")
    void validHeaderWhenEnforced_returnsSuccessWithResponseContentDigest() throws Exception {
        MvcResult result = mockMvc.perform(post(POST_ENDPOINT)
            .contentType(APPLICATION_JSON)
            .content(POST_BODY)
            .header(CONTENT_DIGEST, validPostBodyDigest()))
            .andExpect(status().isOk())
            .andReturn();

        assertValidResponseDigest(result);
    }
}
