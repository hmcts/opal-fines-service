package uk.gov.hmcts.opal.controllers.defendant;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.defendant.BaseDefendantAccountsIntegrationTest.commentAndNotesPayload;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.dto.ToJsonString;

@DisplayName("Integration tests for PATCH /defendant-accounts/{id}")
@Slf4j
public class DefendantAccountUpdateIntegrationTest {

    protected static final String URL_BASE = "/defendant-accounts";

    // =====================================
    // OPAL TESTS
    // =====================================
    @Nested
    @DisplayName("Opal Tests")
    @ActiveProfiles({"integration", "opal"})
    @AutoConfigureMockMvc
    @Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
    @Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
    public class Opal extends AbstractIntegrationTest {

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Test
        @DisplayName("PATCH Update Defendant Account - Happy Path [@PO-1565]")
        public void opalUpdateDefendantAccount_Happy() throws Exception {
            Integer currentVersion = jdbcTemplate.queryForObject(
                "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 77L
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions());
            headers.add("Business-Unit-Id", "78");
            headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

            String requestJson = commentAndNotesPayload("hello");

            ResultActions result = mockMvc.perform(
                patch(URL_BASE + "/77")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            );

            String body = result.andReturn().getResponse().getContentAsString();
            String etag = result.andReturn().getResponse().getHeader("ETag");

            log.info(":opalUpdateDefendantAccount_Happy body:\n{}", ToJsonString.toPrettyJson(body));
            log.info(":opalUpdateDefendantAccount_Happy ETag: {}", etag);

            result.andExpect(status().isOk())
                .andExpect(header().exists("ETag"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            assertNotNull(etag, "ETag must be present");
            assertTrue(etag.matches("^\"\\d+\"$"), "ETag should be a quoted number");

            validateJsonSchema(body, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
        }

        @Test
        @DisplayName("PATCH Update Defendant Account - If-Match Mismatch [@PO-1565]")
        public void patch_conflict_whenIfMatchDoesNotMatch() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions());
            headers.add("Business-Unit-Id", "78");
            headers.add(HttpHeaders.IF_MATCH, "\"999\"");

            String requestJson = commentAndNotesPayload("no change");

            mockMvc.perform(
                    patch(URL_BASE + "/77")
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                ).andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/optimistic-locking"))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("PATCH Update Defendant Account - Missing If-Match [@PO-1565]")
        public void patch_conflict_whenIfMatchMissing() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions());
            headers.add("Business-Unit-Id", "78");

            String requestJson = commentAndNotesPayload("hello");

            mockMvc.perform(
                    patch(URL_BASE + "/77")
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)

                ).andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type", org.hamcrest.Matchers.anyOf(
                    is("https://hmcts.gov.uk/problems/resource-conflict"),
                    is("https://hmcts.gov.uk/problems/optimistic-locking")
                )));
        }

        @Test
        @DisplayName("PATCH Update Defendant Account - Not Found (account not in BU) [@PO-1565]")
        public void patch_notFound_whenAccountNotInHeaderBU() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions());
            headers.add("Business-Unit-Id", "99");
            headers.add(HttpHeaders.IF_MATCH, "\"0\"");

            String requestJson = commentAndNotesPayload("hello");

            mockMvc.perform(
                    patch(URL_BASE + "/77")
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
        }

        @Test
        @DisplayName("PATCH Update Defendant Account - Schema violation [@PO-1565]")
        public void patch_badRequest_whenTypesInvalid() throws Exception {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions());
            headers.add("Business-Unit-Id", "78");
            headers.add(HttpHeaders.IF_MATCH, "\"0\"");

            mockMvc.perform(
                    patch(URL_BASE + "/77")
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"comment_and_notes":{"free_text_note_1": 123}}
                    """)
                ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/json-schema-validation"));
        }

        @Test
        @DisplayName("PATCH Update Defendant Account - Update Enforcement Court [@PO-1565]")
        public void patch_updatesEnforcementCourt_andValidatesResponseSchema() throws Exception {
            Integer currentVersion = jdbcTemplate.queryForObject(
                "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 77L
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions());
            headers.add("Business-Unit-Id", "78");
            headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

            String body = """
                {"enforcement_court":{"court_id":100,"court_name":"Central Magistrates"}}
            """;

            ResultActions result = mockMvc.perform(
                patch(URL_BASE + "/77")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            );

            String resp = result.andReturn().getResponse().getContentAsString();
            log.info("enforcement_court update resp:\n{}", resp);

            result.andExpect(status().isOk())
                .andExpect(header().exists("ETag"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(77))
                .andExpect(jsonPath("$.enforcement_court.court_id").value(100))
                .andExpect(jsonPath("$.enforcement_court.court_name").value("Central Magistrates"));

            validateJsonSchema(resp, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
        }

        @Test
        @DisplayName("PATCH Update Defendant Account - Update Enforcement Override [@PO-1565]")
        public void patch_updatesEnforcementOverride() throws Exception {
            Integer currentVersion = jdbcTemplate.queryForObject(
                "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 77L
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions());
            headers.add("Business-Unit-Id", "78");
            headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

            String body = """
                {
                  "enforcement_override": {
                    "enforcement_override_result": {
                      "enforcement_override_result_id": "FWEC",
                      "enforcement_override_result_title": "Further Warrant Execution Cancelled"
                    },
                    "enforcer": {
                      "enforcer_id": 21,
                      "enforcer_name": "North East Enforcement"
                    },
                    "lja": {
                      "lja_id": 240,
                      "lja_name": "Tyne & Wear LJA"
                    }
                  }
                }
            """;

            ResultActions result = mockMvc.perform(
                patch(URL_BASE + "/77")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            );

            String resp = result.andReturn().getResponse().getContentAsString();
            log.info("enforcement_override update resp:\n{}", ToJsonString.toPrettyJson(resp));

            result.andExpect(status().isOk())
                .andExpect(header().exists("ETag"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            validateJsonSchema(resp, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
        }

        @Test
        @DisplayName("PATCH Update Defendant Account - ETag present & Response Schema OK [@PO-1565]")
        public void patch_returnsETag_andResponseConformsToSchema() throws Exception {
            Integer currentVersion = jdbcTemplate.queryForObject(
                "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 77L
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions());
            headers.add("Business-Unit-Id", "78");
            headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

            String requestJson = commentAndNotesPayload("etag test");

            ResultActions result = mockMvc.perform(
                patch(URL_BASE + "/77")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            );

            String body = result.andReturn().getResponse().getContentAsString();
            String etag = result.andReturn().getResponse().getHeader("ETag");

            log.info(":patch_returnsETag_andResponseConformsToSchema body:\n{}", ToJsonString.toPrettyJson(body));
            log.info(":patch_returnsETag_andResponseConformsToSchema ETag: {}", etag);

            result.andExpect(status().isOk())
                .andExpect(header().exists("ETag"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            assertNotNull(etag, "ETag must be present");
            assertTrue(etag.matches("^\"\\d+\"$"), "ETag should be a quoted number");

            validateJsonSchema(body, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
        }
    }
}
