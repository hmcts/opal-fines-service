package uk.gov.hmcts.opal.controllers.defendant;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;

@DisplayName("Integration tests for /defendant-accounts/{id}/defendant-account-parties/{partyId}")
@Slf4j
public class DefendantAccountPartyIntegrationTest {

    protected static final String URL_BASE = "/defendant-accounts";

    protected final String getDefendantAccountPartyResponseSchemaLocation() {
        return SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountPartyResponse.json";
    }

    // ========================================
    // LEGACY TESTS
    // ========================================
    @Nested
    @DisplayName("Legacy Tests")
    @ActiveProfiles({"integration", "legacy"})
    @AutoConfigureMockMvc
    public class Legacy extends Common {

        @DisplayName("LEGACY: Get Defendant Account Party - Happy Path [@PO-1973]")
        @Test
        public void legacyGetDefendantAccountParty_Happy() throws Exception {
            ResultActions actions = mockMvc.perform(
                get(URL_BASE + "/77/defendant-account-parties/77")
                    .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
            );

            String body = actions.andReturn().getResponse().getContentAsString();
            String etag = actions.andReturn().getResponse().getHeader("ETag");

            log.info(":legacyGetDefendantAccountParty_Happy body:\n{}", body);
            log.info(":legacyGetDefendantAccountParty_Happy ETag: {}", etag);

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"))
                .andExpect(jsonPath("$.defendant_account_party.is_debtor").value(true))
                .andExpect(jsonPath("$.defendant_account_party.party_details.party_id").value("77"))
                .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.surname").value("Graham"))
                .andExpect(jsonPath("$.defendant_account_party.address.address_line_1").value("Lumber House"))
                .andExpect(header().string("ETag", matchesPattern("\"\\d+\"")));

            validateJsonSchema(body, getDefendantAccountPartyResponseSchemaLocation());
        }

        @DisplayName("LEGACY: Get Defendant Account Party - 500 Error [@PO-1973]")
        @Test
        public void legacyGetDefendantAccountParty_500Error() throws Exception {
            ResultActions actions = mockMvc.perform(
                get(URL_BASE + "/500/defendant-account-parties/500")
                    .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
            );

            String body = actions.andReturn().getResponse().getContentAsString();
            log.info(":legacyGetDefendantAccountParty_500Error body:\n{}", body);

            actions.andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(header().doesNotExist("ETag"));
        }
    }

    // ========================================
    // OPAL TESTS
    // ========================================
    @Nested
    @DisplayName("Opal Tests")
    @ActiveProfiles({"integration", "opal"})
    @Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
    @Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
    public class Opal extends Common {

        @DisplayName("OPAL: Get Defendant Account Party - Happy Path [@PO-1588]")
        @Test
        public void opalGetDefendantAccountParty_Happy() throws Exception {
            ResultActions actions = mockMvc.perform(
                get(URL_BASE + "/77/defendant-account-parties/77")
                    .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
            );

            String body = actions.andReturn().getResponse().getContentAsString();
            log.info("Opal happy path response:\n{}", body);

            actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"))
                .andExpect(jsonPath("$.defendant_account_party.is_debtor").value(true))
                .andExpect(jsonPath("$.defendant_account_party.party_details.party_id").value("77"))
                .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.surname").value("Graham"))
                .andExpect(jsonPath("$.defendant_account_party.address.address_line_1").value("Lumber House"))
                .andExpect(header().string("ETag", matchesPattern("\"\\d+\"")));

            validateJsonSchema(body, getDefendantAccountPartyResponseSchemaLocation());
        }

        @DisplayName("OPAL: Get Defendant Account Party - Organisation Only [@PO-1588]")
        @Test
        public void opalGetDefendantAccountParty_Organisation() throws Exception {
            ResultActions actions = mockMvc.perform(
                get(URL_BASE + "/555/defendant-account-parties/555")
                    .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
            );

            String body = actions.andReturn().getResponse().getContentAsString();
            log.info("Organisation response:\n{}", body);

            actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.defendant_account_party.party_details.organisation_flag").value(true))
                .andExpect(jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_name")
                    .value("TechCorp Solutions Ltd"))
                .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details").doesNotExist());
        }
    }

    // ========================================
    // COMMON TESTS
    // ========================================
    public abstract class Common extends AbstractIntegrationTest {

        @DisplayName("Get Defendant Account Party - Null/Optional Fields [@PO-1588]")
        @Test
        public void getDefendantAccountParty_NullFields() throws Exception {
            ResultActions actions = mockMvc.perform(
                get(URL_BASE + "/88/defendant-account-parties/88")
                    .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
            );

            String body = actions.andReturn().getResponse().getContentAsString();
            log.info("Null fields response:\n{}", body);

            actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.surname").doesNotExist())
                .andExpect(jsonPath("$.defendant_account_party.address.address_line_1").doesNotExist());
        }
    }
}
