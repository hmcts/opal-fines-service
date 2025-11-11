package uk.gov.hmcts.opal.controllers.defendant;

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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration tests for /defendant-accounts/{id}/defendant-account-parties/{partyId}")
@Slf4j
public class DefendantAccountPartyIntegrationTest {

    protected static final String URL_BASE = "/defendant-accounts";

    protected final String getDefendantAccountPartyResponseSchemaLocation() {
        return SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountPartyResponse.json";
    }

    @Nested
    @DisplayName("Legacy Tests")
    @ActiveProfiles({"integration", "legacy"})
    @AutoConfigureMockMvc
    public class Legacy extends AbstractIntegrationTest {

        @DisplayName("Get Defendant Account Party - Happy Path [@PO-1973]")
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
                .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type")
                    .value("Defendant"))
                .andExpect(jsonPath("$.defendant_account_party.is_debtor").value(true))
                .andExpect(jsonPath("$.defendant_account_party.party_details.party_id").value("77"))
                .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.surname")
                    .value("Graham"))
                .andExpect(jsonPath("$.defendant_account_party.address.address_line_1").value("Lumber House"))
                .andExpect(header().string("ETag", matchesPattern("\"\\d+\"")));

            validateJsonSchema(body, getDefendantAccountPartyResponseSchemaLocation());
        }

        @DisplayName(" Get Defendant Account Party - 500 Error [@PO-1973]")
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

    @Nested
    @DisplayName("Opal Tests")
    @ActiveProfiles({"integration", "opal"})
    @Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
    @Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
    public class Opal extends AbstractIntegrationTest {

        @DisplayName("Get Defendant Account Party - Happy Path [@PO-1588]")
        @Test
        public void opalGetDefendantAccountParty_Happy() throws Exception {
            ResultActions actions = mockMvc.perform(get("/defendant-accounts/77/defendant-account-parties/77")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            log.info("Opal happy path response:\n" + actions.andReturn().getResponse().getContentAsString());

            actions.andExpect(status().isOk())
                .andExpect(header().string("ETag", matchesPattern("\"\\d+\"")))
                .andExpectAll(
                    jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"),
                    jsonPath("$.defendant_account_party.is_debtor").value(true),
                    jsonPath("$.defendant_account_party.party_details.party_id").value("77"),
                    jsonPath("$.defendant_account_party.party_details.organisation_flag").value(false),
                    jsonPath("$.defendant_account_party.party_details.organisation_details").doesNotExist(),
                    jsonPath("$.defendant_account_party.party_details.individual_details.title").value("Ms"),
                    jsonPath("$.defendant_account_party.party_details.individual_details.forenames").value("Anna"),
                    jsonPath("$.defendant_account_party.party_details.individual_details.surname").value("Graham"),
                    jsonPath("$.defendant_account_party.party_details.individual_details.date_of_birth").value(
                        "1980-02-03"),
                    jsonPath("$.defendant_account_party.party_details.individual_details.age").value("33"),
                    jsonPath("$.defendant_account_party.party_details.individual_details.national_insurance_number")
                        .value("A11111A"),
                    jsonPath(
                        "$.defendant_account_party.party_details.individual_details.individual_aliases[0].alias_id")
                        .value("7701"),
                    jsonPath(
                        "$.defendant_account_party.party_details.individual_details.individual_aliases[0]"
                            + ".sequence_number")
                        .value(1),
                    jsonPath(
                        "$.defendant_account_party.party_details.individual_details.individual_aliases[0].surname")
                        .value("Smith"),
                    jsonPath(
                        "$.defendant_account_party.party_details.individual_details.individual_aliases[0].forenames")
                        .value("Annie"),
                    jsonPath(
                        "$.defendant_account_party.party_details.individual_details.individual_aliases[1].alias_id")
                        .value("7702"),
                    jsonPath(
                        "$.defendant_account_party.party_details.individual_details.individual_aliases[1]"
                            + ".sequence_number")
                        .value(2),
                    jsonPath(
                        "$.defendant_account_party.party_details.individual_details.individual_aliases[1].surname")
                        .value("Johnson"),
                    jsonPath(
                        "$.defendant_account_party.party_details.individual_details.individual_aliases[1].forenames").value(
                        "Anne"),
                    jsonPath(
                        "$.defendant_account_party.party_details.individual_details.individual_aliases[2].alias_id").value(
                        "7703"),
                    jsonPath(
                        "$.defendant_account_party.party_details.individual_details.individual_aliases[2]"
                            + ".sequence_number")
                        .value(3),
                    jsonPath(
                        "$.defendant_account_party.party_details.individual_details.individual_aliases[2].surname").value(
                        "Williams"),
                    jsonPath(
                        "$.defendant_account_party.party_details.individual_details.individual_aliases[2].forenames").value(
                        "Ana"),
                    jsonPath("$.defendant_account_party.address.address_line_1").value("Lumber House"),
                    jsonPath("$.defendant_account_party.address.address_line_2").value("77 Gordon Road"),
                    jsonPath("$.defendant_account_party.address.address_line_3").value("Maidstone, Kent"),
                    jsonPath("$.defendant_account_party.address.address_line_4").value(is(nullValue())),
                    jsonPath("$.defendant_account_party.address.address_line_5").value(is(nullValue())),
                    jsonPath("$.defendant_account_party.address.postcode").value("MA4 1AL"),
                    jsonPath("$.defendant_account_party.contact_details.primary_email_address").value(is(nullValue())),
                    jsonPath("$.defendant_account_party.contact_details.secondary_email_address").value(
                        is(nullValue())),
                    jsonPath("$.defendant_account_party.contact_details.mobile_telephone_number").value(
                        is(nullValue())),
                    jsonPath("$.defendant_account_party.contact_details.home_telephone_number").value(is(nullValue())),
                    jsonPath("$.defendant_account_party.contact_details.work_telephone_number").value(is(nullValue())),
                    jsonPath("$.defendant_account_party.vehicle_details.vehicle_make_and_model").value("Toyota Prius"),
                    jsonPath("$.defendant_account_party.vehicle_details.vehicle_registration").value("AB77CDE"),
                    jsonPath("$.defendant_account_party.employer_details.employer_name").value("Tesco Ltd"),
                    jsonPath("$.defendant_account_party.employer_details.employer_reference").value("EMPREF77"),
                    jsonPath("$.defendant_account_party.employer_details.employer_email_address").value(
                        "employer77@company.com"),
                    jsonPath("$.defendant_account_party.employer_details.employer_telephone_number").value(
                        "02079997777"),
                    jsonPath("$.defendant_account_party.employer_details.employer_address.address_line_1").value(
                        "123 Employer Road"),
                    jsonPath("$.defendant_account_party.employer_details.employer_address.address_line_2").value(
                        is(nullValue())),
                    jsonPath("$.defendant_account_party.employer_details.employer_address.address_line_3").value(
                        is(nullValue())),
                    jsonPath("$.defendant_account_party.employer_details.employer_address.address_line_4").value(
                        is(nullValue())),
                    jsonPath("$.defendant_account_party.employer_details.employer_address.address_line_5").value(
                        is(nullValue())),
                    jsonPath("$.defendant_account_party.employer_details.employer_address.postcode").value("EMP1 2AA"),
                    jsonPath(
                        "$.defendant_account_party.language_preferences.document_language_preference.language_code").value(
                        "EN"),
                    jsonPath(
                        "$.defendant_account_party.language_preferences.document_language_preference"
                            + ".language_display_name")
                        .value("English only"),
                    jsonPath(
                        "$.defendant_account_party.language_preferences.hearing_language_preference.language_code").value(
                        "EN"),
                    jsonPath(
                        "$.defendant_account_party.language_preferences.hearing_language_preference"
                            + ".language_display_name")
                        .value("English only")
                );
            String body = actions.andReturn().getResponse().getContentAsString();
            // Schema validation
            validateJsonSchema(body, getDefendantAccountPartyResponseSchemaLocation());
        }

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
                .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.surname")
                    .doesNotExist())
                .andExpect(jsonPath("$.defendant_account_party.address.address_line_1").doesNotExist());
        }


        @DisplayName("OPAL: Get Defendant Account Party - Organisation Only [@PO-1588]")
        @Test
        public void opalGetDefendantAccountParty_Organisation() throws Exception {
            ResultActions actions = mockMvc.perform(get("/defendant-accounts/555/defendant-account-parties/555")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            log.info("Organisation response:\n" + actions.andReturn().getResponse().getContentAsString());

            actions.andExpect(status().isOk())
                .andExpectAll(
                    jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"),
                    jsonPath("$.defendant_account_party.is_debtor").value(true),
                    jsonPath("$.defendant_account_party.party_details.party_id").value("555"),
                    jsonPath("$.defendant_account_party.party_details.organisation_flag").value(true),
                    jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_name")
                        .value("TechCorp Solutions Ltd"),
                    jsonPath("$.defendant_account_party.party_details.individual_details").doesNotExist(),
                    jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_aliases",
                        hasSize(2)),
                    jsonPath(
                        "$.defendant_account_party.party_details.organisation_details.organisation_aliases[0].alias_id")
                        .value("5551"),
                    jsonPath(
                        "$.defendant_account_party.party_details.organisation_details"
                            + ".organisation_aliases[0].sequence_number")
                        .value(1),
                    jsonPath(
                        "$.defendant_account_party.party_details.organisation_details"
                            + ".organisation_aliases[0].organisation_name")
                        .value("TechCorp Ltd"),
                    jsonPath(
                        "$.defendant_account_party.party_details.organisation_details.organisation_aliases[1].alias_id")
                        .value("5552"),
                    jsonPath(
                        "$.defendant_account_party.party_details.organisation_details"
                            + ".organisation_aliases[1].sequence_number")
                        .value(2),
                    jsonPath(
                        "$.defendant_account_party.party_details.organisation_details"
                            + ".organisation_aliases[1].organisation_name")
                        .value("TC Solutions Limited"),
                    jsonPath("$.defendant_account_party.address.address_line_1")
                        .value("Business Park"),
                    jsonPath("$.defendant_account_party.address.address_line_2")
                        .value("42 Innovation Drive"),
                    jsonPath("$.defendant_account_party.address.address_line_3")
                        .value("Tech District"),
                    jsonPath("$.defendant_account_party.address.address_line_4")
                        .value("Birmingham"),
                    jsonPath("$.defendant_account_party.address.address_line_5").value(is(nullValue())),
                    jsonPath("$.defendant_account_party.address.postcode").value("B15 3TG"),
                    jsonPath("$.defendant_account_party.contact_details.primary_email_address")
                        .value(is(nullValue())),
                    jsonPath("$.defendant_account_party.contact_details.secondary_email_address")
                        .value(is(nullValue())),
                    jsonPath("$.defendant_account_party.contact_details.mobile_telephone_number")
                        .value(is(nullValue())),
                    jsonPath("$.defendant_account_party.contact_details.home_telephone_number")
                        .value(is(nullValue())),
                    jsonPath("$.defendant_account_party.contact_details.work_telephone_number")
                        .value(is(nullValue())),
                    jsonPath("$.defendant_account_party.vehicle_details.vehicle_make_and_model")
                        .value(is(nullValue())),
                    jsonPath("$.defendant_account_party.vehicle_details.vehicle_registration")
                        .value(is(nullValue())),
                    jsonPath("$.defendant_account_party.employer_details.employer_name").value(is(nullValue())),
                    jsonPath("$.defendant_account_party.employer_details.employer_reference")
                        .value(is(nullValue())),
                    jsonPath("$.defendant_account_party.employer_details.employer_email_address")
                        .value(is(nullValue())),
                    jsonPath("$.defendant_account_party.employer_details.employer_telephone_number")
                        .value(is(nullValue())),
                    jsonPath("$.defendant_account_party.employer_details.employer_address.address_line_1")
                        .value(""),
                    jsonPath("$.defendant_account_party.employer_details.employer_address.address_line_2")
                        .value(is(nullValue())),
                    jsonPath("$.defendant_account_party.employer_details.employer_address.address_line_3")
                        .value(is(nullValue())),
                    jsonPath("$.defendant_account_party.employer_details.employer_address.address_line_4")
                        .value(is(nullValue())),
                    jsonPath("$.defendant_account_party.employer_details.employer_address.address_line_5")
                        .value(is(nullValue())),
                    jsonPath("$.defendant_account_party.employer_details.employer_address.postcode")
                        .value(is(nullValue())),
                    jsonPath(
                        "$.defendant_account_party.language_preferences.document_language_preference.language_code")
                        .value(is(nullValue())),
                    jsonPath(
                        "$.defendant_account_party.language_preferences.document_language_preference"
                            + ".language_display_name")
                        .value(is(nullValue())),
                    jsonPath("$.defendant_account_party.language_preferences.hearing_language_preference.language_code")
                        .value(is(nullValue())),
                    jsonPath(
                        "$.defendant_account_party.language_preferences.hearing_language_preference"
                            + ".language_display_name")
                        .value(is(nullValue()))
                );
        }
    }
}
