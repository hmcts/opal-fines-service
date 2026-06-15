package uk.gov.hmcts.opal.controllers;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.LegacyDefendantsPartyIntegrationTest")

class LegacyDefendantsRemovePartyIntegrationTest extends AbstractLegacyDefendantsIntegrationTest {

    private static final String DELETE_PARTY_REQUEST = """
        {
            "defendant_account_party_id": "77",
            "version": 1
        }
        """;

    private HttpHeaders partyHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"1\"");
        return headers;
    }

    @Test
    @JiraEpic("PO-1875")
    @JiraStory("PO-1941")
    @DisplayName("LEGACY: Remove Defendant Account Party - Happy Path [@PO-1941]")
    void removeDefendantAccountParty_Happy() throws Exception {
        ResultActions actions = mockMvc.perform(
            delete(URL_BASE + "/77/defendant-account-parties/77")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(partyHeaders())
                .contentType(MediaType.APPLICATION_JSON)
                .content(DELETE_PARTY_REQUEST));

        String body = actions.andReturn().getResponse().getContentAsString();
        String etag = actions.andReturn().getResponse().getHeader("ETag");

        log.info(":legacy_removeDefendantAccountParty_Happy body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":legacy_removeDefendantAccountParty_Happy ETag: {}", etag);

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party_id").value("77"));

        jsonSchemaValidationService.validateOrError(body, REMOVE_DEFENDANT_PARTY_RESPONSE_SCHEMA);
    }

    @Test
    @JiraEpic("PO-1875")
    @JiraStory("PO-1941")
    @DisplayName("LEGACY: Remove Defendant Account Party - 500 Error [@PO-1941]")
    void removeDefendantAccountParty_500Error() throws Exception {
        ResultActions actions = mockMvc.perform(
            delete(URL_BASE + "/500/defendant-account-parties/500")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(partyHeaders())
                .contentType(MediaType.APPLICATION_JSON)
                .content(DELETE_PARTY_REQUEST));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":legacy_removeDefendantAccountParty_500Error body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(header().doesNotExist("ETag"));
    }

    @Test
    @JiraEpic("PO-1875")
    @JiraStory("PO-1941")
    @DisplayName("LEGACY: Remove Defendant Account Party - Organisation Only [@PO-1941]")
    void removeDefendantAccountParty_Organisation() throws Exception {
        ResultActions actions = mockMvc.perform(
            delete(URL_BASE + "/555/defendant-account-parties/555")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(partyHeaders())
                .contentType(MediaType.APPLICATION_JSON)
                .content(DELETE_PARTY_REQUEST));

        String body = actions.andReturn().getResponse().getContentAsString();
        String etag = actions.andReturn().getResponse().getHeader("ETag");

        log.info(":legacy_removeDefendantAccountParty_Organisation body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":legacy_removeDefendantAccountParty_Organisation ETag: {}", etag);

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party_id").value("555"));

        jsonSchemaValidationService.validateOrError(body, REMOVE_DEFENDANT_PARTY_RESPONSE_SCHEMA);
    }

    @Test
    @JiraEpic("PO-1875")
    @JiraStory("PO-1941")
    @DisplayName("LEGACY: Remove Defendant Account Party - Individual Only [@PO-1941]")
    void testRemoveDefendantAccountParty_Individual() throws Exception {
        ResultActions actions = mockMvc.perform(
            delete(URL_BASE + "/666/defendant-account-parties/666")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(partyHeaders())
                .contentType(MediaType.APPLICATION_JSON)
                .content(DELETE_PARTY_REQUEST));

        String body = actions.andReturn().getResponse().getContentAsString();
        String etag = actions.andReturn().getResponse().getHeader("ETag");

        log.info(":legacy_removeDefendantAccountParty_Individual body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":legacy_removeDefendantAccountParty_Individual ETag: {}", etag);

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party_id").value("666"));

        jsonSchemaValidationService.validateOrError(body, REMOVE_DEFENDANT_PARTY_RESPONSE_SCHEMA);
    }
}
