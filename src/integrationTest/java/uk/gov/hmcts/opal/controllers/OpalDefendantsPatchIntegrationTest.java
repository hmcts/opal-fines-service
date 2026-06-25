package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@Slf4j(topic = "opal.OpalDefendantsPatchIntegrationTest")
@TestPropertySource(properties = {
    "launchdarkly.default-flag-values.release-1b=true"
})
class OpalDefendantsPatchIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Happy Path [@PO-1565]")
    @JiraStory("PO-1565")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6027")
    void opalUpdateDefendantAccount_Happy() throws Exception {
        Integer currentVersion = versionFor(77L);
        HttpHeaders headers = authorisedHeaders("some_value", "78", "\"" + currentVersion + "\"");

        String requestJson = """
            {
              "comment_and_notes": {
                "account_comment": "hello"
              }
            }
            """;

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/77")
                .headers(headers)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()).contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));

        String body = a.andReturn().getResponse().getContentAsString();
        String etag = a.andReturn().getResponse().getHeader("ETag");

        log.info(":opal_updateDefendantAccount_Happy body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":opal_updateDefendantAccount_Happy ETag: {}", etag);

        a.andExpect(status().isOk()).andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        assertNotNull(etag, "ETag must be present");
        assertTrue(etag.matches("^\"\\d+\"$"), "ETag should be a quoted number");
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - If-Match Mismatch [@PO-1565]")
    @JiraStory("PO-1565")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6030")
    void patch_conflict_whenIfMatchDoesNotMatch() throws Exception {
        HttpHeaders headers = authorisedHeaders("good_token", "78", "\"999\"");

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/77")
                .headers(headers)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()).contentType(MediaType.APPLICATION_JSON)
                .content(commentAndNotesPayload("no change")));

        String body = a.andReturn().getResponse().getContentAsString();
        log.info(":patch_conflict_whenIfMatchDoesNotMatch response body:\n{}", body);

        a.andExpect(status().isConflict()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/optimistic-locking"))
            .andExpect(jsonPath("$.title").value("Conflict")).andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Missing If-Match [@PO-1565]")
    @JiraStory("PO-1565")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6025")
    void patch_badRequest_whenIfMatchMissing() throws Exception {
        HttpHeaders headers = authorisedHeaders("good_token", "78", null);

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/77")
                .headers(headers)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()).contentType(MediaType.APPLICATION_JSON)
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
    @JiraStory("PO-1565")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6029")
    void patch_forbidden_whenUserLacksAccountMaintenance() throws Exception {
        userStateStub.setupWithNoPermissions();
        HttpHeaders headers = authorisedHeaders("token_without_perm", "78", "\"0\"");

        mockMvc.perform(
                patch(URL_BASE + "/77")
                    .headers(headers)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor()).contentType(MediaType.APPLICATION_JSON)
                    .content(commentAndNotesPayload("hello")))
            .andExpect(status().isForbidden()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Not Found (account not in BU) [@PO-1565]")
    @JiraStory("PO-1565")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6033")
    void patch_notFound_whenAccountNotInHeaderBU() throws Exception {
        HttpHeaders headers = authorisedHeaders("good_token", "99", "\"0\"");

        ResultActions result = mockMvc.perform(
            patch(URL_BASE + "/77")
                .headers(headers)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()).contentType(MediaType.APPLICATION_JSON)
                .content(commentAndNotesPayload("hello")));

        log.info(":patch_notFound_whenAccountNotInHeaderBU response:\n{}",
            result.andReturn().getResponse().getContentAsString());

        result.andExpect(status().isNotFound()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Wrong Business Unit [@PO-1565]")
    @JiraStory("PO-1565")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6035")
    void patch_badRequest_whenMultipleGroupsProvided() throws Exception {
        HttpHeaders headers = authorisedHeaders("good_token", "78", "\"0\"");

        mockMvc.perform(patch(URL_BASE + "/77")
            .headers(headers)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor()).contentType(MediaType.APPLICATION_JSON)
            .content("""
                  {
                    "comment_and_notes":{"account_comment":"x"},
                    "collection_order":{"collection_order_flag":true}
                  }
                """)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Schema violation (multiple groups) [@PO-1565]")
    @JiraStory("PO-1565")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6037")
    void patch_badRequest_whenTypesInvalid() throws Exception {
        HttpHeaders headers = authorisedHeaders("good_token", "78", "\"0\"");

        mockMvc.perform(patch(URL_BASE + "/77")
                .headers(headers)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()).contentType(MediaType.APPLICATION_JSON)
                .content("""
                      {
                        "comment_and_notes": {
                          "account_comment": "Account reviewed and updated per latest case information."
                        },
                        "enforcement_override": {
                          "enforcement_override_result": {
                          "enforcement_override_result_id": "FWEC"
                          }
                        }
                      }
                    """)).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/illegal-argument"));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Update Enforcement Court [@PO-1565]")
    @JiraStory("PO-1565")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6026")
    void patch_updatesEnforcementCourt_andValidatesResponseSchema() throws Exception {
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
            patch(URL_BASE + "/77")
                .headers(headers)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()).contentType(MediaType.APPLICATION_JSON)
                .content(body));

        String resp = a.andReturn().getResponse().getContentAsString();
        log.info("enforcement_court update resp:\n{}", resp);

        a.andExpect(status().isOk()).andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(77))
            .andExpect(jsonPath("$.enforcement_court.court_id").value(100));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Update Enforcement Court With Long Court Id [@PO-3667]")
    @JiraStory("PO-3667")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6032")
    void patch_updatesEnforcementCourt_withLongCourtId() throws Exception {
        Integer currentVersion = versionFor(77L);
        HttpHeaders headers = authorisedHeaders("good_token", "78", "\"" + currentVersion + "\"");

        String body = """
            {
              "enforcement_court": {
                "court_id": 780000000185
              }
            }
            """;

        mockMvc.perform(
                patch(URL_BASE + "/77")
                    .headers(headers)
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor()).contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isOk())
            .andExpect(header().exists("ETag"))
            .andExpect(jsonPath("$.enforcement_court.court_id").value(780000000185L));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Update Collection Order [@PO-1565]")
    @JiraStory("PO-1565")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6022")
    void patch_updatesCollectionOrder() throws Exception {
        Integer currentVersion = versionFor(77L);
        HttpHeaders headers = authorisedHeaders("good_token", "78", "\"" + currentVersion + "\"");

        mockMvc.perform(patch(URL_BASE + "/77")
            .headers(headers)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor()).contentType(MediaType.APPLICATION_JSON)
            .content("""
                  {"collection_order":{"collection_order_flag":true,"collection_order_date":"2025-01-01"}}
                """)).andExpect(status().isOk()).andExpect(header().exists("ETag"));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Update Collection Order With Missing Date [@PO-3667]")
    @JiraStory("PO-3667")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6023")
    void patch_updatesCollectionOrder_whenDateMissing() throws Exception {
        Integer currentVersion = versionFor(77L);
        HttpHeaders headers = authorisedHeaders("good_token", "78", "\"" + currentVersion + "\"");

        mockMvc.perform(patch(URL_BASE + "/77")
            .headers(headers)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor()).contentType(MediaType.APPLICATION_JSON)
            .content("""
                  {"collection_order":{"collection_order_flag":true}}
                """)).andExpect(status().isOk()).andExpect(header().exists("ETag"));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Update Enforcement Override [@PO-1565]")
    @JiraStory("PO-1565")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6024")
    void patch_updatesEnforcementOverride() throws Exception {
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
            patch(URL_BASE + "/77")
                .headers(headers)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()).contentType(MediaType.APPLICATION_JSON)
                .content(body));

        String resp = a.andReturn().getResponse().getContentAsString();
        log.info("enforcement_override update resp:\n{}", ToJsonString.toPrettyJson(resp));

        a.andExpect(status().isOk()).andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(77))
            .andExpect(jsonPath("$.enforcement_override.enforcement_override_result.enforcement_override_result_id")
                .value("FWEC"))
            .andExpect(jsonPath("$.enforcement_override.enforcer.enforcer_id").value(21))
            .andExpect(jsonPath("$.enforcement_override.lja.lja_id").value(240));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Clear Enforcement Override Enforcer/LJA [@PO-3640]")
    @JiraStory("PO-3640")
    @JiraEpic("PO-1675")
    @JiraTestKey("PO-6034")
    void patch_clearsEnforcementOverrideEnforcerAndLja_whenOmitted() throws Exception {
        Integer clearVersion = versionFor(77L);
        HttpHeaders clearHeaders = authorisedHeaders(userStateStub.getBearerToken(), "78", "\"" + clearVersion + "\"");

        ResultActions clearAction = mockMvc.perform(
            patch(URL_BASE + "/77")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(clearHeaders).contentType(MediaType.APPLICATION_JSON).content("""
                      {
                        "enforcement_override": {
                          "enforcement_override_result": { "enforcement_override_result_id": "FWEC" }
                        }
                      }
                    """));

        String clearResponse = clearAction.andReturn().getResponse().getContentAsString();
        log.info("enforcement_override clear resp:\n{}", ToJsonString.toPrettyJson(clearResponse));

        clearAction.andExpect(status().isOk())
            .andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(77))
            .andExpect(jsonPath("$.enforcement_override.enforcement_override_result.enforcement_override_result_id")
                .value("FWEC"))
            .andExpect(jsonPath("$.enforcement_override.enforcer").value(nullValue()))
            .andExpect(jsonPath("$.enforcement_override.lja").value(nullValue()));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Remove Enforcement Override [@PO-1854]")
    @JiraStory("PO-1854")
    @JiraEpic("PO-1675")
    @JiraTestKey("PO-6028")
    void patch_clearsEnforcementOverride_whenResultEnforcerAndLjaAreNull() throws Exception {
        Integer clearVersion = versionFor(77L);
        HttpHeaders clearHeaders = authorisedHeaders(userStateStub.getBearerToken(), "78", "\"" + clearVersion + "\"");

        ResultActions clearAction = mockMvc.perform(
            patch(URL_BASE + "/77")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(clearHeaders)
                .contentType(MediaType.APPLICATION_JSON).content("""
                      {
                        "enforcement_override": {
                          "enforcement_override_result": null,
                          "enforcer": null,
                          "lja": null
                        }
                      }
                    """));

        String clearResponse = clearAction.andReturn().getResponse().getContentAsString();
        log.info("enforcement_override remove resp:\n{}", ToJsonString.toPrettyJson(clearResponse));

        clearAction.andExpect(status().isOk())
            .andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(77))
            .andExpect(jsonPath("$.enforcement_override").value(nullValue()));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - ETag present & Response Schema OK [@PO-1565]")
    @JiraStory("PO-1565")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6031")
    void patch_returnsETag_andResponseConformsToSchema() throws Exception {
        Integer currentVersion = versionFor(77L);
        HttpHeaders headers = authorisedHeaders("good_token", "78", "\"" + currentVersion + "\"");

        ResultActions result = mockMvc.perform(
            patch(URL_BASE + "/77")
                .headers(headers)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor()).contentType(MediaType.APPLICATION_JSON)
                .content(commentAndNotesPayload("etag test")));

        String body = result.andReturn().getResponse().getContentAsString();
        String etag = result.andReturn().getResponse().getHeader("ETag");

        log.info(":patch_returnsETag_andResponseConformsToSchema body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":patch_returnsETag_andResponseConformsToSchema ETag: {}", etag);

        result.andExpect(status().isOk()).andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        assertNotNull(etag, "ETag must be present");
        assertTrue(etag.matches("^\"\\d+\"$"), "ETag should be a quoted number");
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Missing required headers [@PO-2281]")
    @JiraStory("PO-2281")
    @JiraEpic("PO-812")
    @JiraTestKey("PO-6036")
    void patch_badRequest_whenMissingRequiredHeader() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        mockMvc.perform(patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON).content("")
            ).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/missing-header"))
            .andExpect(jsonPath("$.title").value("Missing Required Header"))
            .andExpect(jsonPath("$.detail").value("Required request header \"Business-Unit-Id\" is missing"))
            .andExpect(jsonPath("$.instance").isNotEmpty())
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.retriable").value(false));
    }
}
