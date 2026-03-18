package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.LegacyDefendantsPartyIntegrationTest")
class LegacyDefendantsPartyIntegrationTest extends AbstractLegacyDefendantsIntegrationTest {

    private static final String PUT_PARTY_REQUEST = """
        {
                "defendant_account_party_type": "Defendant",
                "party_details": {
                "party_id": "20010",
                "organisation_flag": true,
                "organisation_details": { "organisation_name": "StillCo" }
             }
        }
        """;

    private HttpHeaders partyHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"1\"");
        return headers;
    }

    @Test
    @DisplayName("LEGACY: Get Defendant Account Party - Happy Path [@PO-1973]")
    void testGetDefendantAccountParty_Happy() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(
            get(URL_BASE + "/77/defendant-account-parties/77").header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        String etag = actions.andReturn().getResponse().getHeader("ETag");

        log.info(":legacy_getDefendantAccountParty_Happy body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":legacy_getDefendantAccountParty_Happy ETag: {}", etag);

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"))
            .andExpect(jsonPath("$.defendant_account_party.is_debtor").value(true))
            .andExpect(jsonPath("$.defendant_account_party.party_details.party_id").value("77"))
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.surname").value("Graham"))
            .andExpect(jsonPath("$.defendant_account_party.address.address_line_1").value("Lumber House"))
            .andExpect(header().string("ETag", matchesPattern("\"\\d+\"")));

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_PARTY_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("LEGACY: Get Defendant Account Party - Organisation Only [@PO-1973]")
    void testGetDefendantAccountParty_Organisation() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(
            get(URL_BASE + "/555/defendant-account-parties/555").header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        String etag = actions.andReturn().getResponse().getHeader("ETag");

        log.info(":legacy_getDefendantAccountParty_Organisation body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":legacy_getDefendantAccountParty_Organisation ETag: {}", etag);

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_name")
                .value("TechCorp Solutions Ltd"))
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details").doesNotExist())
            .andExpect(header().string("ETag", "\"1\""));

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_PARTY_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("LEGACY: Get Defendant Account Party - 500 Error [@PO-1973]")
    void testGetDefendantAccountParty_500Error() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(
            get(URL_BASE + "/500/defendant-account-parties/500").header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":legacy_getDefendantAccountParty_500Error body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(header().doesNotExist("ETag"));
    }

    @Test
    @DisplayName("LEGACY: PUT Replace DAP")
    void testPutReplaceDefAccParty_Success() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        var res = mockMvc.perform(
            put("/defendant-accounts/77/defendant-account-parties/77")
                .headers(partyHeaders())
                .contentType(MediaType.APPLICATION_JSON)
                .content(PUT_PARTY_REQUEST)
        );

        log.info("PUT DAP missing DAP resp:\n{}", res.andReturn().getResponse().getContentAsString());

        res.andExpect(status().isOk())
            .andExpect(header().string("etag", matchesPattern("\"\\d+\"")))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type")
                .value("Defendant"));
    }

    @Test
    @DisplayName("LEGACY: PUT Replace DAP")
    void testPutReplaceDefAccParty_500Error() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        var res = mockMvc.perform(
            put("/defendant-accounts/500/defendant-account-parties/500")
                .headers(partyHeaders())
                .contentType(MediaType.APPLICATION_JSON)
                .content(PUT_PARTY_REQUEST)
        );

        log.info("PUT DAP missing DAP resp:\n{}", res.andReturn().getResponse().getContentAsString());

        res.andExpect(status().is5xxServerError());
    }
}
