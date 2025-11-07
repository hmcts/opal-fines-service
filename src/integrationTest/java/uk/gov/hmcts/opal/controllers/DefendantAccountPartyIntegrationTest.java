package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.SchemaPaths;

@Component
@DisplayName("Integration tests for /defendant-accounts/{id}/defendant-account-parties/{partyId}")
public class DefendantAccountPartyIntegrationTest extends BaseDefendantAccountsIntegrationTest {

    protected final String getDefendantAccountPartyResponseSchemaLocation() {
        return SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountPartyResponse.json";
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    @DisplayName("OPAL: Get Defendant Account Party - Happy Path [@PO-1588]")
    public void opalGetDefendantAccountParty_Happy(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(get("/defendant-accounts/77/defendant-account-parties/77")
            .header("Authorization", "Bearer test-token"));

        String body = getBody(actions);
        log.info("Opal happy path response:\n" + body);

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"))
            .andExpect(jsonPath("$.defendant_account_party.is_debtor").value(true))
            .andExpect(jsonPath("$.defendant_account_party.party_details.party_id").value("77"))
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.surname").value("Graham"))
            .andExpect(jsonPath("$.defendant_account_party.address.address_line_1").value("Lumber House"))
            .andExpect(header().string("ETag", matchesPattern("\"\\d+\"")));

        String bodyStr = actions.andReturn().getResponse().getContentAsString();
        jsonSchemaValidationService.validateOrError(bodyStr, getDefendantAccountPartyResponseSchemaLocation());
    }

    @DisplayName("OPAL: Get Defendant Account Party - Organisation Only [@PO-1588]")
    public void opalGetDefendantAccountParty_Organisation(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(get("/defendant-accounts/555/defendant-account-parties/555")
            .header("Authorization", "Bearer test-token"));

        String body = getBody(actions);
        log.info("Organisation response:\n" + body);

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.defendant_account_party.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_name")
                .value("TechCorp Solutions Ltd"))
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details").doesNotExist());
    }

    @DisplayName("OPAL: Get Defendant Account Party - Null/Optional Fields [@PO-1588]")
    public void opalGetDefendantAccountParty_NullFields(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(get("/defendant-accounts/88/defendant-account-parties/88")
            .header("Authorization", "Bearer test-token"));

        String body = getBody(actions);
        log.info("Null fields response:\n" + body);

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.surname").doesNotExist())
            .andExpect(jsonPath("$.defendant_account_party.address.address_line_1").doesNotExist());
    }

    @DisplayName("LEGACY: Get Defendant Account Party - Happy Path [@PO-1973]")
    public void legacyGetDefendantAccountParty_Happy(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(
            get("/defendant-accounts/77/defendant-account-parties/77")
                .header("authorization", "Bearer some_value")
        );

        String body = actions.andReturn().getResponse().getContentAsString();
        String etag = actions.andReturn().getResponse().getHeader("ETag");

        log.info(":legacy_getDefendantAccountParty_Happy body:\n" + body);
        log.info(":legacy_getDefendantAccountParty_Happy ETag: " + etag);

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"))
            .andExpect(jsonPath("$.defendant_account_party.is_debtor").value(true))
            .andExpect(jsonPath("$.defendant_account_party.party_details.party_id").value("77"))
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.surname").value("Graham"))
            .andExpect(jsonPath("$.defendant_account_party.address.address_line_1").value("Lumber House"))
            .andExpect(header().string("ETag", matchesPattern("\"\\d+\"")));

        jsonSchemaValidationService.validateOrError(body, getDefendantAccountPartyResponseSchemaLocation());
    }

    @DisplayName("LEGACY: Get Defendant Account Party - Organisation Only [@PO-1973]")
    public void legacyGetDefendantAccountParty_Organisation(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(
            get("/defendant-accounts/555/defendant-account-parties/555")
                .header("authorization", "Bearer some_value")
        );

        String body = actions.andReturn().getResponse().getContentAsString();
        String etag = actions.andReturn().getResponse().getHeader("ETag");

        log.info(":legacy_getDefendantAccountParty_Organisation body:\n" + body);
        log.info(":legacy_getDefendantAccountParty_Organisation ETag: " + etag);

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_name")
                .value("TechCorp Solutions Ltd"))
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details").doesNotExist())
            .andExpect(header().string("ETag", "\"1\""));

        jsonSchemaValidationService.validateOrError(body, getDefendantAccountPartyResponseSchemaLocation());
    }

    @DisplayName("LEGACY: Get Defendant Account Party - 500 Error [@PO-1973]")
    public void legacyGetDefendantAccountParty_500Error(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(
            get("/defendant-accounts/500/defendant-account-parties/500")
                .header("authorization", "Bearer some_value")
        );

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":legacy_getDefendantAccountParty_500Error body:\n" + body);

        actions.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(header().doesNotExist("ETag"));
    }
}