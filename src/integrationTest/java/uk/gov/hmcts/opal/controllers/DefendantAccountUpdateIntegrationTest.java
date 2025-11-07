package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Component
@DisplayName("Integration tests for PATCH /defendant-accounts/{id}")
public class DefendantAccountUpdateIntegrationTest extends BaseDefendantAccountsIntegrationTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @DisplayName("OPAL: PATCH Update Defendant Account - Happy Path [@PO-1565]")
    public void opalUpdateDefendantAccount_Happy(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Read the current version to avoid optimistic locking conflicts
        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class, 77L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("some_value");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String requestJson = commentAndNotesPayload("hello");

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        String body = a.andReturn().getResponse().getContentAsString();
        String etag = a.andReturn().getResponse().getHeader("ETag");

        log.info(":opal_updateDefendantAccount_Happy body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":opal_updateDefendantAccount_Happy ETag: {}", etag);

        a.andExpect(status().isOk())
            .andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        assertNotNull(etag, "ETag must be present");
        assertTrue(etag.matches("^\"\\d+\"$"), "ETag should be a quoted number");

        jsonSchemaValidationService.validateOrError(body, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - If-Match Mismatch [@PO-1565]")
    public void patch_conflict_whenIfMatchDoesNotMatch(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"999\"");

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentAndNotesPayload("no change"))
        );

        String body = a.andReturn().getResponse().getContentAsString();
        log.info(":patch_conflict_whenIfMatchDoesNotMatch response body:\n{}", body);

        a.andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/optimistic-locking"))
            .andExpect(jsonPath("$.title").value("Conflict"))
            .andExpect(jsonPath("$.status").value(409));
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - Missing If-Match [@PO-1565]")
    public void patch_conflict_whenIfMatchMissing(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        // Intentionally DO NOT add If-Match

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentAndNotesPayload("hello"))
        );

        String body = a.andReturn().getResponse().getContentAsString();
        log.info(":patch_conflict_whenIfMatchMissing body:\n{}", body);

        a.andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type", org.hamcrest.Matchers.anyOf(
                is("https://hmcts.gov.uk/problems/resource-conflict"),
                is("https://hmcts.gov.uk/problems/optimistic-locking")
            )));
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - Forbidden when user lacks permission [@PO-1565]")
    public void patch_forbidden_whenUserLacksAccountMaintenance(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            UserState.builder()
                .userId(999L)
                .userName("no-perm-user")
                .businessUnitUser(java.util.Collections.emptySet())
                .build()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token_without_perm");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        String body = commentAndNotesPayload("hello");

        mockMvc.perform(patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - Not Found (account not in BU) [@PO-1565]")
    public void patch_notFound_whenAccountNotInHeaderBU(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "99");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        String requestJson = commentAndNotesPayload("hello");

        var result = mockMvc.perform(
            patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        log.info(":patch_notFound_whenAccountNotInHeaderBU response:\n{}",
            result.andReturn().getResponse().getContentAsString());

        result.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - Wrong Business Unit [@PO-1565]")
    public void patch_badRequest_whenMultipleGroupsProvided(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        mockMvc.perform(patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                      {
                        "comment_and_notes":{"account_comment":"x"},
                        "collection_order":{"collection_order_flag":true}
                      }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/json-schema-validation"));
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - Schema violation (multiple groups) [@PO-1565]")
    public void patch_badRequest_whenTypesInvalid(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        mockMvc.perform(patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                      {"comment_and_notes":{"free_text_note_1": 123}}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/json-schema-validation"));
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - Update Enforcement Court [@PO-1565]")
    public void patch_updatesEnforcementCourt_andValidatesResponseSchema(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class, 77L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String body = """
            {
              "enforcement_court": {
                "court_id": 100,
                "court_name": "Central Magistrates"
              }
            }
            """;

        var a = mockMvc.perform(
            patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        String resp = a.andReturn().getResponse().getContentAsString();
        log.info("enforcement_court update resp:\n{}", resp);

        a.andExpect(status().isOk())
            .andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(77))
            .andExpect(jsonPath("$.enforcement_court.court_id").value(100))
            .andExpect(jsonPath("$.enforcement_court.court_name").value("Central Magistrates"));

        jsonSchemaValidationService.validateOrError(resp, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - Update Collection Order [@PO-1565]")
    public void patch_updatesCollectionOrder(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class, 77L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        mockMvc.perform(patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                      {"collection_order":{"collection_order_flag":true,"collection_order_date":"2025-01-01"}}
                    """))
            .andExpect(status().isOk())
            .andExpect(header().exists("ETag"));
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - Update Enforcement Override [@PO-1565]")
    public void patch_updatesEnforcementOverride(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class, 77L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
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

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        String resp = a.andReturn().getResponse().getContentAsString();
        log.info("enforcement_override update resp:\n{}", ToJsonString.toPrettyJson(resp));

        a.andExpect(status().isOk())
            .andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        jsonSchemaValidationService.validateOrError(resp, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - ETag present & Response Schema OK [@PO-1565]")
    public void patch_returnsETag_andResponseConformsToSchema(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class, 77L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
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

        jsonSchemaValidationService.validateOrError(body, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
    }
}
