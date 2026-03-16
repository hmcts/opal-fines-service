package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Slf4j(topic = "opal.OpalDefendantsReadIntegrationTest")
class OpalDefendantsReadIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    @Test
    @DisplayName("OPAL: Get header summary for non-existent ID returns 404")
    void getHeaderSummary_Opal_NotFound() throws Exception {
        authoriseAllPermissions();

        ResultActions ra =
            mockMvc.perform(get(URL_BASE + "/500/header-summary").header("authorization", "Bearer some_value"));

        String body = ra.andReturn().getResponse().getContentAsString();
        log.info(":getHeaderSummary_Opal_NotFound: Response body:\n{}", ToJsonString.toPrettyJson(body));

        ra.andExpect(status().isNotFound()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account Party - Happy Path [@PO-1588]")
    void opalGetDefendantAccountParty_Happy() throws Exception {
        ResultActions actions = mockMvc.perform(
            get("/defendant-accounts/77/defendant-account-parties/77").header("Authorization", "Bearer test-token"));

        log.info("Opal happy path response:\n" + actions.andReturn().getResponse().getContentAsString());

        actions.andExpect(status().isOk()).andExpect(header().string("ETag", matchesPattern("\"\\d+\"")))
            .andExpectAll(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"),
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
                jsonPath("$.defendant_account_party.party_details.individual_details.national_insurance_number").value(
                    "A11111A"),
                jsonPath("$.defendant_account_party.party_details.individual_details.individual_aliases[0].alias_id")
                    .value("7701"),
                jsonPath("$.defendant_account_party.party_details.individual_details.individual_aliases[0]"
                    + ".sequence_number").value(1),
                jsonPath("$.defendant_account_party.party_details.individual_details.individual_aliases[0].surname")
                    .value("Smith"),
                jsonPath("$.defendant_account_party.party_details.individual_details.individual_aliases[0].forenames")
                    .value("Annie"),
                jsonPath("$.defendant_account_party.party_details.individual_details.individual_aliases[1].alias_id")
                    .value("7702"),
                jsonPath("$.defendant_account_party.party_details.individual_details.individual_aliases[1]"
                    + ".sequence_number").value(2),
                jsonPath("$.defendant_account_party.party_details.individual_details.individual_aliases[1].surname")
                    .value("Johnson"),
                jsonPath("$.defendant_account_party.party_details.individual_details.individual_aliases[1].forenames")
                    .value("Anne"),
                jsonPath("$.defendant_account_party.party_details.individual_details.individual_aliases[2].alias_id")
                    .value("7703"),
                jsonPath("$.defendant_account_party.party_details.individual_details.individual_aliases[2]"
                    + ".sequence_number").value(3),
                jsonPath("$.defendant_account_party.party_details.individual_details.individual_aliases[2].surname")
                    .value("Williams"),
                jsonPath("$.defendant_account_party.party_details.individual_details.individual_aliases[2].forenames")
                    .value("Ana"),
                jsonPath("$.defendant_account_party.address.address_line_1").value("Lumber House"),
                jsonPath("$.defendant_account_party.address.address_line_2").value("77 Gordon Road"),
                jsonPath("$.defendant_account_party.address.address_line_3").value("Maidstone, Kent"),
                jsonPath("$.defendant_account_party.address.address_line_4").value(is(nullValue())),
                jsonPath("$.defendant_account_party.address.address_line_5").value(is(nullValue())),
                jsonPath("$.defendant_account_party.address.postcode").value("MA4 1AL"),
                jsonPath("$.defendant_account_party.contact_details.primary_email_address").value(is(nullValue())),
                jsonPath("$.defendant_account_party.contact_details.secondary_email_address").value(is(nullValue())),
                jsonPath("$.defendant_account_party.contact_details.mobile_telephone_number").value(is(nullValue())),
                jsonPath("$.defendant_account_party.contact_details.home_telephone_number").value(is(nullValue())),
                jsonPath("$.defendant_account_party.contact_details.work_telephone_number").value(is(nullValue())),
                jsonPath("$.defendant_account_party.vehicle_details.vehicle_make_and_model").value("Toyota Prius"),
                jsonPath("$.defendant_account_party.vehicle_details.vehicle_registration").value("AB77CDE"),
                jsonPath("$.defendant_account_party.employer_details.employer_name").value("Tesco Ltd"),
                jsonPath("$.defendant_account_party.employer_details.employer_reference").value("EMPREF77"),
                jsonPath("$.defendant_account_party.employer_details.employer_email_address").value(
                    "employer77@company.com"),
                jsonPath("$.defendant_account_party.employer_details.employer_telephone_number").value("02079997777"),
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
                jsonPath("$.defendant_account_party.language_preferences.document_language_preference.language_code")
                    .value("EN"),
                jsonPath("$.defendant_account_party.language_preferences.document_language_preference"
                    + ".language_display_name").value("English only"),
                jsonPath("$.defendant_account_party.language_preferences.hearing_language_preference.language_code")
                    .value("EN"),
                jsonPath("$.defendant_account_party.language_preferences.hearing_language_preference"
                    + ".language_display_name").value("English only"));
        String body = actions.andReturn().getResponse().getContentAsString();
        jsonSchemaValidationService.validateOrError(body, DEFENDANT_PARTY_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account Party - Organisation Only [@PO-1588]")
    void opalGetDefendantAccountParty_Organisation() throws Exception {
        ResultActions actions = mockMvc.perform(
            get("/defendant-accounts/555/defendant-account-parties/555").header("Authorization", "Bearer test-token"));

        log.info("Organisation response:\n" + actions.andReturn().getResponse().getContentAsString());

        actions.andExpect(status().isOk())
            .andExpectAll(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"),
                jsonPath("$.defendant_account_party.is_debtor").value(true),
                jsonPath("$.defendant_account_party.party_details.party_id").value("555"),
                jsonPath("$.defendant_account_party.party_details.organisation_flag").value(true),
                jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_name").value(
                    "TechCorp Solutions Ltd"),
                jsonPath("$.defendant_account_party.party_details.individual_details").doesNotExist(),
                jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_aliases",
                    hasSize(2)),
                jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_aliases[0]"
                    + ".alias_id").value("5551"),
                jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_aliases[0]"
                    + ".sequence_number").value(1),
                jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_aliases[0]"
                    + ".organisation_name").value("TechCorp Ltd"),
                jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_aliases[1]"
                    + ".alias_id").value("5552"),
                jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_aliases[1]"
                    + ".sequence_number").value(2),
                jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_aliases[1]"
                    + ".organisation_name").value("TC Solutions Limited"),
                jsonPath("$.defendant_account_party.address.address_line_1").value("Business Park"),
                jsonPath("$.defendant_account_party.address.address_line_2").value("42 Innovation Drive"),
                jsonPath("$.defendant_account_party.address.address_line_3").value("Tech District"),
                jsonPath("$.defendant_account_party.address.address_line_4").value("Birmingham"),
                jsonPath("$.defendant_account_party.address.address_line_5").value(is(nullValue())),
                jsonPath("$.defendant_account_party.address.postcode").value("B15 3TG"),
                jsonPath("$.defendant_account_party.contact_details.primary_email_address").value(is(nullValue())),
                jsonPath("$.defendant_account_party.contact_details.secondary_email_address").value(is(nullValue())),
                jsonPath("$.defendant_account_party.contact_details.mobile_telephone_number").value(is(nullValue())),
                jsonPath("$.defendant_account_party.contact_details.home_telephone_number").value(is(nullValue())),
                jsonPath("$.defendant_account_party.contact_details.work_telephone_number").value(is(nullValue())),
                jsonPath("$.defendant_account_party.vehicle_details.vehicle_make_and_model").value(is(nullValue())),
                jsonPath("$.defendant_account_party.vehicle_details.vehicle_registration").value(is(nullValue())),
                jsonPath("$.defendant_account_party.employer_details.employer_name").value(is(nullValue())),
                jsonPath("$.defendant_account_party.employer_details.employer_reference").value(is(nullValue())),
                jsonPath("$.defendant_account_party.employer_details.employer_email_address").value(is(nullValue())),
                jsonPath("$.defendant_account_party.employer_details.employer_telephone_number").value(is(nullValue())),
                jsonPath("$.defendant_account_party.employer_details.employer_address.address_line_1").value(""),
                jsonPath("$.defendant_account_party.employer_details.employer_address.address_line_2").value(
                    is(nullValue())),
                jsonPath("$.defendant_account_party.employer_details.employer_address.address_line_3").value(
                    is(nullValue())),
                jsonPath("$.defendant_account_party.employer_details.employer_address.address_line_4").value(
                    is(nullValue())),
                jsonPath("$.defendant_account_party.employer_details.employer_address.address_line_5").value(
                    is(nullValue())),
                jsonPath("$.defendant_account_party.employer_details.employer_address.postcode").value(is(nullValue())),
                jsonPath("$.defendant_account_party.language_preferences.document_language_preference.language_code")
                    .value(is(nullValue())),
                jsonPath("$.defendant_account_party.language_preferences.document_language_preference"
                    + ".language_display_name").value(is(nullValue())),
                jsonPath("$.defendant_account_party.language_preferences.hearing_language_preference.language_code")
                    .value(is(nullValue())),
                jsonPath("$.defendant_account_party.language_preferences.hearing_language_preference"
                    + ".language_display_name").value(is(nullValue())));
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account Party - Null/Optional Fields [@PO-1588]")
    void opalGetDefendantAccountParty_NullFields() throws Exception {
        ResultActions actions = mockMvc.perform(
            get("/defendant-accounts/88/defendant-account-parties/88").header("Authorization", "Bearer test-token"));
        log.info("Null fields response:\n" + actions.andReturn().getResponse().getContentAsString());
        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.surname").doesNotExist())
            .andExpect(jsonPath("$.defendant_account_party.address.address_line_1").doesNotExist());
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an individual")
    void opalGetAtAGlance_Individual() throws Exception {
        authoriseAllPermissions();

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/77/at-a-glance").header("authorization", "Bearer some_value"));

        String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
        log.info(":testGetAtAGlance: Party is an individual. etag header: \n{}", headers);
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetAtAGlance: Party is an individual. Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("etag", matchesPattern("\"\\d+\"")))
            .andExpect(jsonPath("$.defendant_account_id").value("77"))
            .andExpect(jsonPath("$.account_number").value("177A"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant"))
            .andExpect(jsonPath("$.is_youth").exists())
            .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
            .andExpect(jsonPath("$.party_details.organisation_details").doesNotExist())
            .andExpect(jsonPath("$.party_details.individual_details.age").value(expectedAge()))
            .andExpect(jsonPath("$.address").exists())
            .andExpect(jsonPath("$.payment_terms").exists())
            .andExpect(jsonPath("$.enforcement_status").exists())
            .andExpect(jsonPath("$.enforcement_status.last_enforcement_action.last_enforcement_action_id").value("10"))
            .andExpect(jsonPath("$.enforcement_status.last_enforcement_action.last_enforcement_action_title").value(
                nullValue()))
            .andExpect(jsonPath("$.comments_and_notes").exists());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_GLANCE_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an individual (Parent/Guardian)")
    void opalGetAtAGlance_Individual_ParentGuardian() throws Exception {
        authoriseAllPermissions();

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/10004/at-a-glance").header("authorization", "Bearer some_value"));

        String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
        log.info(":testGetAtAGlance: Party is an individual (Parent/Guardian). etag header: \n" + headers);
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetAtAGlance: Party is an individual (Parent/Guardian). Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("etag", "\"1\""))
            .andExpect(jsonPath("$.defendant_account_id").value("10004"))
            .andExpect(jsonPath("$.account_number").value("10004A"))
            .andExpect(jsonPath("$.debtor_type").value("Parent/Guardian"))
            .andExpect(jsonPath("$.is_youth").exists())
            .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
            .andExpect(jsonPath("$.party_details.organisation_details").doesNotExist())
            .andExpect(jsonPath("$.party_details.individual_details.age").value(expectedAge()))
            .andExpect(jsonPath("$.address").exists())
            .andExpect(jsonPath("$.payment_terms").exists())
            .andExpect(jsonPath("$.enforcement_status").exists())
            .andExpect(jsonPath("$.comments_and_notes").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_GLANCE_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an organisation")
    void opalGetAtAGlance_Organisation() throws Exception {
        authoriseAllPermissions();

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/10001/at-a-glance").header("authorization", "Bearer some_value"));

        String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
        log.info(":testGetAtAGlance: Party is an organisation. etag header: \n" + headers);
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetAtAGlance: Party is an organisation. Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("etag", "\"1\""))
            .andExpect(jsonPath("$.defendant_account_id").value("10001"))
            .andExpect(jsonPath("$.account_number").value("10001A"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant"))
            .andExpect(jsonPath("$.is_youth").value(false))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.party_details.individual_details").doesNotExist())
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"))
            .andExpect(jsonPath("$.address").exists())
            .andExpect(jsonPath("$.language_preferences").exists())
            .andExpect(jsonPath("$.language_preferences.hearing_language_preference.language_display_name").value(
                "English only"))
            .andExpect(jsonPath("$.language_preferences.document_language_preference.language_display_name").value(
                "English only"))
            .andExpect(jsonPath("$.payment_terms").exists())
            .andExpect(jsonPath("$.enforcement_status").exists())
            .andExpect(jsonPath("$.enforcement_status.collection_order_made").exists())
            .andExpect(jsonPath("$.comments_and_notes").exists());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_GLANCE_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an organisation. \n"
        + "No language preferences set (as these are optional) \n"
        + "No account comments or notes set (as these are optional)")
    void opalGetAtAGlance_Organisation_NoLanguagePrefs() throws Exception {
        authoriseAllPermissions();

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/10002/at-a-glance").header("authorization", "Bearer some_value"));

        String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
        log.info(":testGetAtAGlance: Party is an organisation. etag header: \n" + headers);
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetAtAGlance: Party is an organisation. Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("etag", "\"1\""))
            .andExpect(jsonPath("$.defendant_account_id").value("10002"))
            .andExpect(jsonPath("$.account_number").value("10002A"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant"))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.party_details.individual_details").doesNotExist())
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"))
            .andExpect(jsonPath("$.language_preferences").doesNotExist())
            .andExpect(jsonPath("$.comments_and_notes").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_GLANCE_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an organisation. "
        + "One language preference not set (as this is optional)")
    void opalGetAtAGlance_Organisation_NoHearingLanguagePref() throws Exception {
        authoriseAllPermissions();

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/10003/at-a-glance").header("authorization", "Bearer some_value"));

        String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
        log.info(":testGetAtAGlance: Party is an organisation. etag header: \n" + headers);
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetAtAGlance: Party is an organisation. Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("etag", "\"1\""))
            .andExpect(jsonPath("$.defendant_account_id").value("10003"))
            .andExpect(jsonPath("$.account_number").value("10003A"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant"))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.party_details.individual_details").doesNotExist())
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"))
            .andExpect(jsonPath("$.language_preferences.document_language_preference.language_display_name").value(
                "English only"))
            .andExpect(jsonPath("$.language_preferences.hearing_language_preference").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_GLANCE_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - 401 Unauthorized \n"
        + "when no auth header provided \n")
    void opalGetAtAGlance_missingAuthHeader_returns401() throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized")).when(userStateService)
            .checkForAuthorisedUser(any());

        mockMvc.perform(get(URL_BASE + "/10003/at-a-glance").accept(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(status().isUnauthorized()).andExpect(content().string(""));
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - 403 Forbidden\nNo auth header provided \n")
    void opalGetAtAGlance_authenticatedWithoutPermission_returns403() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Forbidden")).when(
            userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(get(URL_BASE + "/10003/at-a-glance").accept(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(status().isForbidden()).andExpect(content().string(""));
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance - Verify aliases array organisation [@PO-2312]")
    void testGetAtAGlance_VerifyAliasesArray_Organisation() throws Exception {
        authoriseAllPermissions();

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/10001/at-a-glance").header("authorization", "Bearer some_value"));

        String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
        log.info(":testGetAtAGlance: Verify aliases array. etag header: \n{}", headers);
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetAtAGlance: Verify aliases array. Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("etag", "\"1\""))
            .andExpect(jsonPath("$.defendant_account_id").value("10001"))
            .andExpect(jsonPath("$.account_number").value("10001A"))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.party_details.organisation_details").exists())
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases").isArray())
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases").isNotEmpty())
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases.length()").value(3))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[0].alias_id")
                .value("100011"))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[0].sequence_number")
                .value(1))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[0].organisation_name")
                .value("AliasOrg"))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[1].alias_id")
                .value("100012"))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[1].sequence_number")
                .value(2))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[1].organisation_name")
                .value("SecondAliasOrg"))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[2].alias_id")
                .value("100013"))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[2].sequence_number")
                .value(3))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[2].organisation_name")
                .value("ThirdAliasOrg"))
            .andExpect(jsonPath("$.party_details.individual_details").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_GLANCE_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance - Verify aliases array individual [@PO-2312]")
    void testGetAtAGlance_VerifyAliasesArray_Individual() throws Exception {
        authoriseAllPermissions();

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/77/at-a-glance").header("authorization", "Bearer some_value"));

        String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
        log.info(":testGetAtAGlance: Verify individual aliases array. etag header: \n{}", headers);
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetAtAGlance: Verify individual aliases array. Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value("77"))
            .andExpect(jsonPath("$.account_number").value("177A"))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
            .andExpect(jsonPath("$.party_details.individual_details").exists())
            .andExpect(jsonPath("$.party_details.individual_details.surname").value("Graham"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases").isArray())
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases").isNotEmpty())
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases.length()").value(3))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[0].alias_id").value("7701"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[0].sequence_number").value(1))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[0].forenames").value("Annie"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[0].surname").value("Smith"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[1].alias_id").value("7702"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[1].sequence_number").value(2))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[1].forenames").value("Anne"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[1].surname").value("Johnson"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[2].alias_id").value("7703"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[2].sequence_number").value(3))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[2].forenames").value("Ana"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[2].surname").value("Williams"))
            .andExpect(jsonPath("$.party_details.organisation_details").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_GLANCE_RESPONSE_SCHEMA);
    }
}
