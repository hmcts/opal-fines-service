package uk.gov.hmcts.opal.controllers.defendant;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.dto.ToJsonString;

@DisplayName("Integration tests for /defendant-accounts/{id}/at-a-glance")
@Slf4j
public class DefendantAccountAtAGlanceIntegrationTest {

    protected static final String URL_BASE = "/defendant-accounts";

    protected final String getAtAGlanceResponseSchemaLocation() {
        return SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountAtAGlanceResponse.json";
    }

    @Nested
    @DisplayName("Legacy Tests")
    @ActiveProfiles({"integration", "legacy"})
    @AutoConfigureMockMvc
    public class Legacy extends AbstractIntegrationTest {

        @DisplayName("Get Defendant Accounts At A Glance - Happy Path [@PO-1564]")
        @Test
        void testLegacyGetDefendantAtAGlance() throws Exception {

            when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

            ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/77/at-a-glance")
                .header("authorization", "Bearer some_value"));

            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":testGetPaymentTerms: Response body:\n" + ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_id").value("DEF-ACC-00012345"))
                .andExpect(jsonPath("$.account_number").value("ACCT-9876543210"))
                .andExpect(jsonPath("$.debtor_type").value("Defendant"))
                .andExpect(jsonPath("$.is_youth").value(false))

                // party_details
                .andExpect(jsonPath("$.party_details.party_id").value(nullValue()))
                .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
                .andExpect(jsonPath("$.party_details.organisation_details").value(nullValue()))

                // party_details.individual_details
                .andExpect(jsonPath("$.party_details.individual_details.title").value("Mr"))
                .andExpect(jsonPath("$.party_details.individual_details.surname").value("Rivers"))
                .andExpect(jsonPath("$.party_details.individual_details.date_of_birth").value("1989-05-23"))
                // age is a STRING in your JSON:
                .andExpect(jsonPath("$.party_details.individual_details.age").value("36"))
                .andExpect(jsonPath("$.party_details.individual_details.national_insurance_number").value("QQ123456C"))
                // aliases: array with one empty object {}
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases.length()").value(1))
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[0]").isMap())
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[0].*").isEmpty())

                // address
                .andExpect(jsonPath("$.address.address_line_1").value("10 Example Street"))
                .andExpect(jsonPath("$.address.address_line_2").value("Flat 2B"))
                .andExpect(jsonPath("$.address.address_line_3").value("Sample District"))
                .andExpect(jsonPath("$.address.address_line_4").value("Sampletown"))
                .andExpect(jsonPath("$.address.address_line_5").value("Exampleshire"))
                .andExpect(jsonPath("$.address.postcode").value("AB1 2CD"))

                // language_preferences (all null)
                .andExpect(jsonPath("$.language_preferences.document_language_preference.language_code")
                    .value(nullValue()))
                .andExpect(jsonPath("$.language_preferences.document_language_preference.language_display_name")
                    .value(nullValue()))
                .andExpect(jsonPath("$.language_preferences.hearing_language_preference.language_code")
                    .value(nullValue()))
                .andExpect(jsonPath("$.language_preferences.hearing_language_preference.language_display_name")
                    .value(nullValue()))

                // payment_terms
                .andExpect(jsonPath("$.payment_terms.payment_terms_type.payment_terms_type_code")
                    .value("P"))
                .andExpect(jsonPath("$.payment_terms.payment_terms_type.payment_terms_type_display_name")
                    .value("Paid"))
                .andExpect(jsonPath("$.payment_terms.effective_date").value("2025-10-01"))
                .andExpect(jsonPath("$.payment_terms.instalment_period")
                    .value(nullValue()))
                .andExpect(jsonPath("$.payment_terms.lump_sum_amount").value(0.00))
                .andExpect(jsonPath("$.payment_terms.instalment_amount").value(50.00))

                // enforcement_status
                .andExpect(jsonPath("$.enforcement_status.last_enforcement_action.last_enforcement_action_id")
                    .value(nullValue()))
                .andExpect(jsonPath("$.enforcement_status.last_enforcement_action.last_enforcement_action_title")
                    .value(nullValue()))
                .andExpect(jsonPath("$.enforcement_status.collection_order_made").value(false))
                .andExpect(jsonPath("$.enforcement_status.default_days_in_jail").value(0))
                // enforcement_override object with nested nulls
                .andExpect(jsonPath("$.enforcement_status.enforcement_override.enforcement_override_result")
                    .value(nullValue()))
                .andExpect(jsonPath("$.enforcement_status.enforcement_override.enforcer.enforcer_id")
                    .value(nullValue()))
                .andExpect(jsonPath("$.enforcement_status.enforcement_override.enforcer.enforcer_name")
                    .value(nullValue()))
                .andExpect(jsonPath("$.enforcement_status.enforcement_override.lja.lja_id")
                    .value(nullValue()))
                .andExpect(jsonPath("$.enforcement_status.enforcement_override.lja.lja_name")
                    .value(nullValue()))
                .andExpect(jsonPath("$.enforcement_status.last_movement_date").value("2025-09-30"))

                // comments_and_notes
                .andExpect(jsonPath("$.comments_and_notes.account_comment")
                    .value("Account imported from legacy system on 2025-09-01."))
                .andExpect(jsonPath("$.comments_and_notes.free_text_note_1")
                    .value("Customer agreed to monthly instalments."))
                .andExpect(jsonPath("$.comments_and_notes.free_text_note_2")
                    .value("Preferred contact: letter."))
                .andExpect(jsonPath("$.comments_and_notes.free_text_note_3")
                    .value("Next review due after three payments."));
        }

        @DisplayName("Get Defendant Accounts At A Glance - 500 Error [@PO-1564]")
        @Test
        void getDefendantAccountAtAGlance_500Error() throws Exception {

            when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

            ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/500/at-a-glance")
                .header("authorization", "Bearer some_value"));

            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":testGetHeaderSummary: Response body:\n" + ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
        }
    }

    @Nested
    @DisplayName("Opal Tests")
    @ActiveProfiles({"integration", "opal"})
    @Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
    @Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
    public class Opal extends AbstractIntegrationTest {

        @DisplayName("Get Defendant Account At A Glance - "
            + "Verify aliases array organisation [@PO-2312]")
        @Test
        void testGetAtAGlance_VerifyAliasesArray_Organisation() throws Exception {
            ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/10001/at-a-glance")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
            log.info(":testGetAtAGlance: Verify aliases array. etag header: \n{}", headers);
            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":testGetAtAGlance: Verify aliases array. Response body:\n{}",
                ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("etag", "\"1\""))
                .andExpect(jsonPath("$.defendant_account_id").value("10001"))
                .andExpect(jsonPath("$.account_number").value("10001A"))
                .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
                .andExpect(jsonPath("$.party_details.organisation_details").exists())
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"))
                // Verify that the organisation_aliases array exists and contains the expected aliases
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases")
                    .isArray())
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases")
                    .isNotEmpty())
                // Verify the array has exactly 3 aliases
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases.length()")
                    .value(3))
                // Verify the first alias details
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[0].alias_id")
                    .value("100011"))
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[0].sequence_number")
                    .value(1))
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[0].organisation_name")
                    .value("AliasOrg"))
                // Verify the second alias details
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[1].alias_id")
                    .value("100012"))
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[1].sequence_number")
                    .value(2))
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[1].organisation_name")
                    .value("SecondAliasOrg"))
                // Verify the third alias details
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[2].alias_id")
                    .value("100013"))
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[2].sequence_number")
                    .value(3))
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[2].organisation_name")
                    .value("ThirdAliasOrg"))
                .andExpect(jsonPath("$.party_details.individual_details").doesNotExist());

            validateJsonSchema(body, getAtAGlanceResponseSchemaLocation());
        }

        @DisplayName("Get Defendant Account At A Glance - "
            + "Verify aliases array individual [@PO-2312]")
        @Test
        void testGetAtAGlance_VerifyAliasesArray_Individual() throws Exception {
            ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/77/at-a-glance")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
            log.info(":testGetAtAGlance: Verify individual aliases array. etag header: \n{}", headers);
            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":testGetAtAGlance: Verify individual aliases array. Response body:\n{}",
                ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defendant_account_id").value("77"))
                .andExpect(jsonPath("$.account_number").value("177A"))
                .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
                .andExpect(jsonPath("$.party_details.individual_details").exists())
                .andExpect(jsonPath("$.party_details.individual_details.surname").value("Graham"))
                // Verify that the individual_aliases array exists and contains the expected aliases
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases")
                    .isArray())
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases")
                    .isNotEmpty())
                // Verify the array has exactly 3 aliases
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases.length()")
                    .value(3))
                // Verify the first alias details
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[0].alias_id")
                    .value("7701"))
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[0].sequence_number")
                    .value(1))
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[0].forenames")
                    .value("Annie"))
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[0].surname")
                    .value("Smith"))
                // Verify the second alias details
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[1].alias_id")
                    .value("7702"))
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[1].sequence_number")
                    .value(2))
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[1].forenames")
                    .value("Anne"))
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[1].surname")
                    .value("Johnson"))
                // Verify the third alias details
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[2].alias_id")
                    .value("7703"))
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[2].sequence_number")
                    .value(3))
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[2].forenames")
                    .value("Ana"))
                .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[2].surname")
                    .value("Williams"))
                .andExpect(jsonPath("$.party_details.organisation_details").doesNotExist());

            validateJsonSchema(body, getAtAGlanceResponseSchemaLocation());
        }

        @DisplayName("Get Defendant Account At A Glance [@PO-1564] - 403 Forbidden\n"
            + "No auth header provided \n")
        @Test
        void opalGetAtAGlance_authenticatedWithoutPermission_returns403() throws Exception {
            doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Forbidden"))
                .when(userStateService).checkForAuthorisedUser(any());

            mockMvc.perform(get(URL_BASE + "/10003/at-a-glance")
                    .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""));
        }

        @DisplayName("Get Defendant Account At A Glance [@PO-1564] - 401 Unauthorized \n"
            + "when no auth header provided \n")
        @Test
        void opalGetAtAGlance_missingAuthHeader_returns401() throws Exception {
            doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
                .when(userStateService).checkForAuthorisedUser(any());

            mockMvc.perform(get(URL_BASE + "/10003/at-a-glance")
                    .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
        }

        @DisplayName("Get Defendant Account At A Glance [@PO-1564] - Party is an organisation. "
            + "One language preference not set (as this is optional)")
        @Test
        public void opalGetAtAGlance_Organisation_NoHearingLanguagePref() throws Exception {
            ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/10003/at-a-glance")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
            log.info(":testGetAtAGlance: Party is an organisation. etag header: \n" + headers);
            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":testGetAtAGlance: Party is an organisation. Response body:\n" + ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // verify the header contains an ETag value
                .andExpect(header().string("etag", "\"1\""))
                .andExpect(jsonPath("$.defendant_account_id").value("10003"))
                .andExpect(jsonPath("$.account_number").value("10003A"))
                .andExpect(jsonPath("$.debtor_type").value("Defendant"))
                .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
                .andExpect(jsonPath("$.party_details.individual_details").doesNotExist())
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_name")
                    .value("Kings Arms"))
                .andExpect(jsonPath("$.language_preferences.document_language_preference.language_display_name")
                    .value("English only"))
                // verify hearing_language_preference node is null (optional)
                .andExpect(jsonPath("$.language_preferences.hearing_language_preference").doesNotExist());

            validateJsonSchema(body, getAtAGlanceResponseSchemaLocation());
        }

        @DisplayName("Get Defendant Account At A Glance [@PO-1564] - Party is an organisation. \n"
            + "No language preferences set (as these are optional) \n"
            + "No account comments or notes set (as these are optional)")
        @Test
        public void opalGetAtAGlance_Organisation_NoLanguagePrefs() throws Exception {
            ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/10002/at-a-glance")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
            log.info(":testGetAtAGlance: Party is an organisation. etag header: \n" + headers);
            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":testGetAtAGlance: Party is an organisation. Response body:\n" + ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // verify the header contains an ETag value
                .andExpect(header().string("etag", "\"1\""))
                .andExpect(jsonPath("$.defendant_account_id").value("10002"))
                .andExpect(jsonPath("$.account_number").value("10002A"))
                .andExpect(jsonPath("$.debtor_type").value("Defendant"))
                .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
                .andExpect(jsonPath("$.party_details.individual_details").doesNotExist())
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_name")
                    .value("Kings Arms"))
                // verify language preferences node is null
                .andExpect(jsonPath("$.language_preferences").doesNotExist())
                // verify comments_and_notes node is absent (no data included for these optional fields)
                .andExpect(jsonPath("$.comments_and_notes").doesNotExist());

            validateJsonSchema(body, getAtAGlanceResponseSchemaLocation());
        }

        @DisplayName("Get Defendant Account At A Glance [@PO-1564] - Party is an organisation")
        @Test
        public void opalGetAtAGlance_Organisation() throws Exception {
            ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/10001/at-a-glance")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
            log.info(":testGetAtAGlance: Party is an organisation. etag header: \n" + headers);
            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":testGetAtAGlance: Party is an organisation. Response body:\n" + ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // verify the header contains an ETag value
                .andExpect(header().string("etag", "\"1\""))
                .andExpect(jsonPath("$.defendant_account_id").value("10001"))
                .andExpect(jsonPath("$.account_number").value("10001A"))
                .andExpect(jsonPath("$.debtor_type").value("Defendant"))
                .andExpect(jsonPath("$.is_youth").value(false))
                .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
                .andExpect(jsonPath("$.party_details.individual_details").doesNotExist())
                .andExpect(jsonPath("$.party_details.organisation_details.organisation_name")
                    .value("Kings Arms"))
                .andExpect(jsonPath("$.address").exists())
                .andExpect(jsonPath("$.language_preferences").exists())
                // verify both language preferences are populated
                .andExpect(jsonPath("$.language_preferences.hearing_language_preference.language_display_name")
                    .value("English only"))
                .andExpect(jsonPath("$.language_preferences.document_language_preference.language_display_name")
                    .value("English only"))
                .andExpect(jsonPath("$.payment_terms").exists())
                .andExpect(jsonPath("$.enforcement_status").exists())
                .andExpect(jsonPath("$.enforcement_status.collection_order_made").exists())
                // verify comments_and_notes node is present (test data included for these optional fields)
                .andExpect(jsonPath("$.comments_and_notes").exists());

            validateJsonSchema(body, getAtAGlanceResponseSchemaLocation());
        }

        @DisplayName("Get Defendant Account At A Glance [@PO-1564] - Party is an individual (Parent/Guardian)")
        @Test
        public void opalGetAtAGlance_Individual_ParentGuardian() throws Exception {
            ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/10004/at-a-glance")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
            log.info(":testGetAtAGlance: Party is an individual (Parent/Guardian). etag header: \n" + headers);
            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":testGetAtAGlance: Party is an individual (Parent/Guardian). Response body:\n"
                + ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // verify the header contains an ETag value
                .andExpect(header().string("etag", "\"1\""))
                .andExpect(jsonPath("$.defendant_account_id").value("10004"))
                .andExpect(jsonPath("$.account_number").value("10004A"))
                .andExpect(jsonPath("$.debtor_type").value("Parent/Guardian"))
                .andExpect(jsonPath("$.is_youth").exists())
                .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
                .andExpect(jsonPath("$.party_details.organisation_details").doesNotExist())
                .andExpect(jsonPath("$.party_details.individual_details.age").value("45"))
                .andExpect(jsonPath("$.address").exists())
                .andExpect(jsonPath("$.payment_terms").exists())
                .andExpect(jsonPath("$.enforcement_status").exists())
                // verify comments_and_notes node is not present (no test data added as these are optional)
                .andExpect(jsonPath("$.comments_and_notes").doesNotExist());
            validateJsonSchema(body, getAtAGlanceResponseSchemaLocation());
        }

        @DisplayName("Get Defendant Account At A Glance [@PO-1564] - Party is an individual")
        @Test
        public void opalGetAtAGlance_Individual() throws Exception {
            ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/77/at-a-glance")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions()));

            String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
            log.info(":testGetAtAGlance: Party is an individual. etag header: \n{}", headers);
            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info(":testGetAtAGlance: Party is an individual. Response body:\n{}", ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // verify the header contains an ETag value
                .andExpect(header().string("etag", matchesPattern("\"\\d+\"")))
                .andExpect(jsonPath("$.defendant_account_id").value("77"))
                .andExpect(jsonPath("$.account_number").value("177A"))
                .andExpect(jsonPath("$.debtor_type").value("Defendant"))
                .andExpect(jsonPath("$.is_youth").exists())
                .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
                .andExpect(jsonPath("$.party_details.organisation_details").doesNotExist())
                .andExpect(jsonPath("$.party_details.individual_details.age").value("45"))
                .andExpect(jsonPath("$.address").exists())
                .andExpect(jsonPath("$.payment_terms").exists())
                .andExpect(jsonPath("$.enforcement_status").exists())
                .andExpect(
                    jsonPath("$.enforcement_status.last_enforcement_action.last_enforcement_action_id").value("10"))
                .andExpect(jsonPath("$.enforcement_status.last_enforcement_action.last_enforcement_action_title").value(
                    IsNull.nullValue()))
                .andExpect(jsonPath("$.comments_and_notes").exists());

            validateJsonSchema(body, getAtAGlanceResponseSchemaLocation());
        }
    }
}
