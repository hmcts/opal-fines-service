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

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Slf4j(topic = "opal.OpalDefendantsPatchIntegrationTest")
class OpalDefendantsPatchIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Happy Path [@PO-1565]")
    void opalUpdateDefendantAccount_Happy() throws Exception {
        authoriseAllPermissions();

        Integer currentVersion = versionFor(77L);
        HttpHeaders headers = authorisedHeaders("some_value", "78", "\"" + currentVersion + "\"");

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON)
                .content(commentAndNotesPayload("hello")));

        String body = a.andReturn().getResponse().getContentAsString();
        String etag = a.andReturn().getResponse().getHeader("ETag");

        log.info(":opal_updateDefendantAccount_Happy body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":opal_updateDefendantAccount_Happy ETag: {}", etag);

        a.andExpect(status().isOk()).andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        assertNotNull(etag, "ETag must be present");
        assertTrue(etag.matches("^\"\\d+\"$"), "ETag should be a quoted number");

        jsonSchemaValidationService.validateOrError(body, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - If-Match Mismatch [@PO-1565]")
    void patch_conflict_whenIfMatchDoesNotMatch() throws Exception {
        authoriseAllPermissions();

        HttpHeaders headers = authorisedHeaders("good_token", "78", "\"999\"");

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON)
                .content(commentAndNotesPayload("no change")));

        String body = a.andReturn().getResponse().getContentAsString();
        log.info(":patch_conflict_whenIfMatchDoesNotMatch response body:\n{}", body);

        a.andExpect(status().isConflict()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/optimistic-locking"))
            .andExpect(jsonPath("$.title").value("Conflict")).andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Missing If-Match [@PO-1565]")
    void patch_conflict_whenIfMatchMissing() throws Exception {
        authoriseAllPermissions();

        HttpHeaders headers = authorisedHeaders("good_token", "78", null);

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON)
                .content(commentAndNotesPayload("hello")));

        String body = a.andReturn().getResponse().getContentAsString();
        log.info(":patch_conflict_whenIfMatchMissing body:\n{}", body);

        a.andExpect(status().isConflict()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type",
                org.hamcrest.Matchers.anyOf(is("https://hmcts.gov.uk/problems/resource-conflict"),
                    is("https://hmcts.gov.uk/problems/optimistic-locking"))));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Forbidden when user lacks permission [@PO-1565]")
    void patch_forbidden_whenUserLacksAccountMaintenance() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            UserState.builder().userId(999L).userName("no-perm-user").businessUnitUser(Collections.emptySet()).build());

        HttpHeaders headers = authorisedHeaders("token_without_perm", "78", "\"0\"");

        mockMvc.perform(
                patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON)
                    .content(commentAndNotesPayload("hello")))
            .andExpect(status().isForbidden()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Not Found (account not in BU) [@PO-1565]")
    void patch_notFound_whenAccountNotInHeaderBU() throws Exception {
        authoriseAllPermissions();

        HttpHeaders headers = authorisedHeaders("good_token", "99", "\"0\"");

        ResultActions result = mockMvc.perform(
            patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON)
                .content(commentAndNotesPayload("hello")));

        log.info(":patch_notFound_whenAccountNotInHeaderBU response:\n{}",
            result.andReturn().getResponse().getContentAsString());

        result.andExpect(status().isNotFound()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Wrong Business Unit [@PO-1565]")
    void patch_badRequest_whenMultipleGroupsProvided() throws Exception {
        authoriseAllPermissions();

        HttpHeaders headers = authorisedHeaders("good_token", "78", "\"0\"");

        mockMvc.perform(patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON).content("""
                  {
                    "comment_and_notes":{"account_comment":"x"},
                    "collection_order":{"collection_order_flag":true}
                  }
                """)).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/json-schema-validation"));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Schema violation (multiple groups) [@PO-1565]")
    void patch_badRequest_whenTypesInvalid() throws Exception {
        authoriseAllPermissions();

        HttpHeaders headers = authorisedHeaders("good_token", "78", "\"0\"");

        mockMvc.perform(patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON).content("""
                  {"comment_and_notes":{"free_text_note_1": 123}}
                """)).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/json-schema-validation"));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Update Enforcement Court [@PO-1565]")
    void patch_updatesEnforcementCourt_andValidatesResponseSchema() throws Exception {
        authoriseAllPermissions();

        Integer currentVersion = versionFor(77L);
        HttpHeaders headers = authorisedHeaders("good_token", "78", "\"" + currentVersion + "\"");

        String body = """
            {
              "enforcement_court": {
                "court_id": 100,
                "court_name": "Central Magistrates"
              }
            }
            """;

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON).content(body));

        String resp = a.andReturn().getResponse().getContentAsString();
        log.info("enforcement_court update resp:\n{}", resp);

        a.andExpect(status().isOk()).andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id").value(77))
            .andExpect(jsonPath("$.enforcement_court.court_id").value(100))
            .andExpect(jsonPath("$.enforcement_court.court_name").value("Central Magistrates"));

        jsonSchemaValidationService.validateOrError(resp, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Update Collection Order [@PO-1565]")
    void patch_updatesCollectionOrder() throws Exception {
        authoriseAllPermissions();

        Integer currentVersion = versionFor(77L);
        HttpHeaders headers = authorisedHeaders("good_token", "78", "\"" + currentVersion + "\"");

        mockMvc.perform(patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON).content("""
              {"collection_order":{"collection_order_flag":true,"collection_order_date":"2025-01-01"}}
            """)).andExpect(status().isOk()).andExpect(header().exists("ETag"));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Update Enforcement Override [@PO-1565]")
    void patch_updatesEnforcementOverride() throws Exception {
        authoriseAllPermissions();

        Integer currentVersion = versionFor(77L);
        HttpHeaders headers = authorisedHeaders("good_token", "78", "\"" + currentVersion + "\"");

        String body = """
            {
              "enforcement_override": {
                "enforcement_override_result": {
                  "enforcement_override_result_id": "FWEC",
                "enforcement_override_result_title": "WITNESS EXPENSES - CENTRAL FUNDS"
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
            patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON).content(body));

        String resp = a.andReturn().getResponse().getContentAsString();
        log.info("enforcement_override update resp:\n{}", ToJsonString.toPrettyJson(resp));

        a.andExpect(status().isOk()).andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        jsonSchemaValidationService.validateOrError(resp, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - ETag present & Response Schema OK [@PO-1565]")
    void patch_returnsETag_andResponseConformsToSchema() throws Exception {
        authoriseAllPermissions();

        Integer currentVersion = versionFor(77L);
        HttpHeaders headers = authorisedHeaders("good_token", "78", "\"" + currentVersion + "\"");

        ResultActions result = mockMvc.perform(
            patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON)
                .content(commentAndNotesPayload("etag test")));

        String body = result.andReturn().getResponse().getContentAsString();
        String etag = result.andReturn().getResponse().getHeader("ETag");

        log.info(":patch_returnsETag_andResponseConformsToSchema body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":patch_returnsETag_andResponseConformsToSchema ETag: {}", etag);

        result.andExpect(status().isOk()).andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        assertNotNull(etag, "ETag must be present");
        assertTrue(etag.matches("^\"\\d+\"$"), "ETag should be a quoted number");

        jsonSchemaValidationService.validateOrError(body, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
    }
}
