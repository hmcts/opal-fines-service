package uk.gov.hmcts.opal.controllers.defendant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;

/**
 * Base class for all Defendant Account integration tests.
 * Contains common setup, utilities, and schema path helpers.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseDefendantAccountsIntegrationTest extends AbstractIntegrationTest {

    protected static final String URL_BASE = "/defendant-accounts";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    // ---------- Schema path helpers ----------



    // ---------- Utility methods ----------
    protected static String commentAndNotesPayload(String accountComment) {
        // Always include all required fields for schema validation
        return """
        {
          "comment_and_notes": {
            "account_comment": %s,
            "free_text_note_1": null,
            "free_text_note_2": null,
            "free_text_note_3": null
          }
        }
        """.formatted(jsonValue(accountComment));
    }

    protected static String commentAndNotesPayload(String accountComment,
        String note1,
        String note2,
        String note3) {
        return """
            {
              "comment_and_notes": {
                "account_comment": %s,
                "free_text_note_1": %s,
                "free_text_note_2": %s,
                "free_text_note_3": %s
              }
            }
            """.formatted(
            jsonValue(accountComment),
            jsonValue(note1),
            jsonValue(note2),
            jsonValue(note3)
        );
    }

    /**
     * Renders a JSON-safe string value, or `null` if input is null.
     */
    protected static String jsonValue(String s) {
        if (s == null) {
            return "null";
        }
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }

    // Example helper for building common headers (optional)
    protected HttpHeaders defaultHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.add("Business-Unit-Id", "78");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // Example reusable method for logging and returning result body
    protected String getBody(ResultActions resultActions) throws Exception {
        return resultActions.andReturn().getResponse().getContentAsString();
    }

}
