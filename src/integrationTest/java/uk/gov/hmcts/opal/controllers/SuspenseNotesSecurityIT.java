package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@DisplayName("Suspense note security")
class SuspenseNotesSecurityIT extends AbstractIntegrationWithSecurityTest {

    private static final String REQUEST = """
        {
          "activity_note": {
            "record_type": "suspense_items",
            "record_id": "99000000104031",
            "note_type": "AA",
            "note_text": "Suspense item note"
          }
        }
        """;

    @Test
    @JiraStory("PO-10403")
    @JiraEpic("PO-9048")
    void rejectsRequestWithoutAccessToken() throws Exception {
        assertUnauthorized(null);
    }

    @Test
    @JiraStory("PO-10403")
    @JiraEpic("PO-9048")
    void rejectsRequestWithInvalidAccessToken() throws Exception {
        assertUnauthorized("Bearer invalid-token");
    }

    private void assertUnauthorized(String authorization) throws Exception {
        MockHttpServletRequestBuilder request = post("/notes")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.IF_MATCH, "\"3\"")
            .header("Business-Unit-Id", 10403)
            .content(REQUEST);

        if (authorization != null) {
            request.header(HttpHeaders.AUTHORIZATION, authorization);
        }

        mockMvc.perform(request)
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(401));
    }
}
