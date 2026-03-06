package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.LegacyDefendantsIntegrationTest02")
class LegacyDefendantsIntegrationTest02 extends AbstractIntegrationTest {

    static final String URL_BASE = "/defendant-accounts";
    static final String DEFENDANT_PARTY_RESPONSE_SCHEMA = SchemaPaths.DEFENDANT_ACCOUNT
        + "/getDefendantAccountPartyResponse.json";
    
    @MockitoBean
    UserStateService userStateService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    JsonSchemaValidationService jsonSchemaValidationService;

    @MockitoBean
    private UserState userState;

    @MockitoBean
    AccessTokenService accessTokenService;

    static String commentAndNotesPayload(String accountComment, String note1, String note2, String note3) {
        return """
            {
              "comment_and_notes": {
                "account_comment": %s,
                "free_text_note_1": %s,
                "free_text_note_2": %s,
                "free_text_note_3": %s
              }
            }
            """.formatted(jsonValue(accountComment), jsonValue(note1), jsonValue(note2), jsonValue(note3));
    }

    /**
     * Renders a JSON value: quoted string if not null, otherwise JSON null.
     */
    private static String jsonValue(String s) {
        if (s == null) {
            return "null";
        }
        // basic escape for quotes; good enough for tests
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }

    @BeforeEach
    void setupUserState() {
        Mockito.when(userState.anyBusinessUnitUserHasPermission(Mockito.any())).thenReturn(true);
        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.any())).thenReturn(userState);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: PATCH Update Defendant Account - Update Comment Notes [@PO-1908]")
    void test_Legacy_UpdateDefendantAccount_CommentsNotes_Success() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Read the current version to avoid optimistic locking conflicts
        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class, 77L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("some_value");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, String.valueOf(currentVersion)); // use actual version

        // create patch request with CommentsAndNotes payload
        String requestJson = commentAndNotesPayload("patch DefAcc comment legacy test",
            "patch DefAcc note one legacy test", "patch DefAcc note two legacy test",
            "patch DefAcc note three legacy test");

        ResultActions resultActions = mockMvc.perform(
                patch(URL_BASE + "/77")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andDo(MockMvcResultHandlers.print());

        String body = resultActions.andReturn().getResponse().getContentAsString();
        String etag = resultActions.andReturn().getResponse().getHeader("ETag");
        long version = objectMapper.readTree(body).path("version").asLong();

        log.info(":legacy_UpdateDefendantAccount_CommentsNotes_Success ETag: {}", etag);

        // Verify the response
        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value("77"))
            .andExpect(jsonPath("$.comment_and_notes.account_comment")
                .value("patch DefAcc comment legacy test"))
            .andExpect(jsonPath("$.comment_and_notes.free_text_note_1")
                .value("patch DefAcc note one legacy test"))
            .andExpect(jsonPath("$.comment_and_notes.free_text_note_2")
                .value("patch DefAcc note two legacy test"))
            .andExpect(jsonPath("$.comment_and_notes.free_text_note_3")
                .value("patch DefAcc note three legacy test"))
            .andExpect(header().string("ETag", "\"" + ++currentVersion + "\""));
    }


    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: PATCH Update Defendant Account - Update Comment Notes - 500 Error [@PO-1908]")
    void test_Legacy_UpdateDefendantAccount_CommentNotes_500Error() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // create patch request with CommentsAndNotes payload
        String requestJson = commentAndNotesPayload("patch DefAcc comment legacy test",
            "patch DefAcc note one legacy test", "patch DefAcc note two legacy test",
            "patch DefAcc note three legacy test");

        ResultActions actions = mockMvc.perform(
            patch(URL_BASE + "/500")
                .header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":Legacy_UpdateDefendantAccount_CommentNotes_500Error body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(header().doesNotExist("ETag")); // no ETag on error payloads
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: PATCH Update Defendant Account - 401 Unauthorized [@PO-1908, CEP2]")
    void test_Legacy_UpdateDefendantAccount_CommentNotes_401Unauthorized() throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).checkForAuthorisedUser(any());

        String requestJson = commentAndNotesPayload("patch DefAcc comment legacy test",
            "patch DefAcc note one legacy test", "patch DefAcc note two legacy test",
            "patch DefAcc note three legacy test");

        mockMvc.perform(
                patch(URL_BASE + "/77")
                    .header("authorization", "Bearer invalid_token")
                    .header("Business-Unit-Id", "78")
                    .header(HttpHeaders.IF_MATCH, "0")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""));
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: PATCH Update Defendant Account - 403 Forbidden [@PO-1908, CEP3]")
    void test_Legacy_UpdateDefendantAccount_CommentNotes_403Forbidden() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Forbidden"))
            .when(userStateService).checkForAuthorisedUser(any());

        String requestJson = commentAndNotesPayload("patch DefAcc comment legacy test",
            "patch DefAcc note one legacy test", "patch DefAcc note two legacy test",
            "patch DefAcc note three legacy test");

        mockMvc.perform(
                patch(URL_BASE + "/77")
                    .header("authorization", "Bearer some_value")
                    .header("Business-Unit-Id", "78")
                    .header(HttpHeaders.IF_MATCH, "0")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isForbidden())
            .andExpect(content().string(""));
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: PATCH Update Defendant Account - 400 Bad Request Invalid Payload [@PO-1908, CEP1]")
    void test_Legacy_UpdateDefendantAccount_CommentNotes_400BadRequest() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Invalid payload - using integer instead of string for account_comment
        String invalidJson = """
            {
              "comment_and_notes": {
                "account_comment": 12345
              }
            }
            """;

        ResultActions actions = mockMvc.perform(
            patch(URL_BASE + "/77")
                .header("authorization", "Bearer some_value")
                .header("Business-Unit-Id", "78")
                .header(HttpHeaders.IF_MATCH, "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        );

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":Legacy_UpdateDefendantAccount_CommentNotes_400BadRequest body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type")
                .value("https://hmcts.gov.uk/problems/json-schema-validation"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.retriable").value(false));
    }


    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: Add Payment Card Request – Happy Path [@PO-2088]")
    void legacyAddPaymentCardRequest_Happy() throws Exception {

        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("some_value");
        headers.add("Business-Unit-Id", "78");
        headers.add("If-Match", "3");

        ResultActions result = mockMvc.perform(
            post("/defendant-accounts/901/payment-card-request")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
        );

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":legacyAddPaymentCardRequest_Happy body:\n{}", ToJsonString.toPrettyJson(body));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value(901));
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: Add Payment Card Request – 500 Error [@PO-2088]")
    void legacyAddPaymentCardRequest_500() throws Exception {

        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("some_value");
        headers.add("Business-Unit-Id", "78");
        headers.add("If-Match", "1");

        ResultActions result = mockMvc.perform(
            post("/defendant-accounts/555/payment-card-request")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
        );

        log.info(":legacyAddPaymentCardRequest_500 body:\n{}", result.andReturn().getResponse().getContentAsString());

        result.andExpect(status().is5xxServerError());
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: POST Add Enforcement - success")
    void legacyPostAddEnforcement_Success() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + 1 + "\"");

        String body = """
            {
              "result_id": "CONF",
              "enforcement_result_responses": [
                {
                  "parameter_name": "amount_due",
                  "response": "100.00"
                },
                {
                  "parameter_name": "next_payment_date",
                  "response": "2026-01-15"
                }
              ],
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-01",
                "extension": true,
                "reason_for_extension": "Financial hardship",
                "payment_terms_type": {
                  "payment_terms_type_code": "B"
                },
                "effective_date": "2025-11-15",
                "instalment_period": {
                  "instalment_period_code": "M"
                },
                "lump_sum_amount": 0.00,
                "instalment_amount": 150.00,
                "posted_details": {
                  "posted_date": "2025-11-02",
                  "posted_by": "System",
                  "posted_by_name": "System User"
                }
              }
            }
            """;

        var res = mockMvc.perform(
            post("/defendant-accounts/72/enforcements")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        res.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.enforcement_id").value("72"))
            .andExpect(jsonPath("$.defendant_account_id").value("72"))
            .andExpect(jsonPath("$.version").value(1));
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: POST Add Enforcement - backend 500")
    void legacyPostAddEnforcement_500Error() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + 1 + "\"");

        String body = """
            {
              "result_id": "CONF",
              "enforcement_result_responses": [
                {
                  "parameter_name": "amount_due",
                  "response": "100.00"
                },
                {
                  "parameter_name": "next_payment_date",
                  "response": "2026-01-15"
                }
              ],
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-01",
                "extension": true,
                "reason_for_extension": "Financial hardship",
                "payment_terms_type": {
                  "payment_terms_type_code": "B"
                },
                "effective_date": "2025-11-15",
                "instalment_period": {
                  "instalment_period_code": "M"
                },
                "lump_sum_amount": 0.00,
                "instalment_amount": 150.00,
                "posted_details": {
                  "posted_date": "2025-11-02",
                  "posted_by": "System",
                  "posted_by_name": "System User"
                }
              }
            }
            """;

        var res = mockMvc.perform(
            post("/defendant-accounts/500/enforcements")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        res.andExpect(status().is5xxServerError());
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: POST Add Enforcement - forbidden without ENTER_ENFORCEMENT")
    void legacyPostAddEnforcement_403Forbidden() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            UserState.builder()
                .userId(999L)
                .userName("no-permission-user")
                .businessUnitUser(java.util.Collections.emptySet())
                .build()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + 1 + "\"");

        String body = """
            {
              "result_id": "CONF",
              "enforcement_result_responses": [
                {
                  "parameter_name": "amount_due",
                  "response": "100.00"
                },
                {
                  "parameter_name": "next_payment_date",
                  "response": "2026-01-15"
                }
              ],
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-01",
                "extension": true,
                "reason_for_extension": "Financial hardship",
                "payment_terms_type": {
                  "payment_terms_type_code": "B"
                },
                "effective_date": "2025-11-15",
                "instalment_period": {
                  "instalment_period_code": "M"
                },
                "lump_sum_amount": 0.00,
                "instalment_amount": 150.00,
                "posted_details": {
                  "posted_date": "2025-11-02",
                  "posted_by": "System",
                  "posted_by_name": "System User"
                }
              }
            }
            """;

        ResultActions res = mockMvc.perform(
            post("/defendant-accounts/72/enforcements")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        String responseBody = res.andReturn().getResponse().getContentAsString();
        log.info(":legacyPostAddEnforcement_403Forbidden response:\n{}", ToJsonString.toPrettyJson(responseBody));

        res.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: POST Add Enforcement - unauthorized token rejected")
    void legacyPostAddEnforcement_401Unauthorized() throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).checkForAuthorisedUser(any());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("bad_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + 1 + "\"");

        String body = """
            {
              "result_id": "CONF",
              "enforcement_result_responses": [
                {
                  "parameter_name": "amount_due",
                  "response": "100.00"
                }
              ],
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-01",
                "extension": true,
                "reason_for_extension": "Financial hardship",
                "payment_terms_type": {
                  "payment_terms_type_code": "B"
                },
                "effective_date": "2025-11-15",
                "instalment_period": {
                  "instalment_period_code": "M"
                },
                "lump_sum_amount": 0.00,
                "instalment_amount": 150.00,
                "posted_details": {
                  "posted_date": "2025-11-02",
                  "posted_by": "System",
                  "posted_by_name": "System User"
                }
              }
            }
            """;

        mockMvc.perform(
                post("/defendant-accounts/72/enforcements")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""));
    }


    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: Get Defendant Account Party - Happy Path [@PO-1973]")
    public void legacyGetDefendantAccountParty_Happy() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(
            get(URL_BASE + "/77/defendant-account-parties/77").header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        String etag = actions.andReturn().getResponse().getHeader("ETag");
        long version = objectMapper.readTree(body).path("version").asLong();

        log.info(":legacy_getDefendantAccountParty_Happy body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":legacy_getDefendantAccountParty_Happy ETag: {}", etag);

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"))
            .andExpect(jsonPath("$.defendant_account_party.is_debtor").value(true))
            .andExpect(jsonPath("$.defendant_account_party.party_details.party_id").value("77"))
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.surname").value("Graham"))
            .andExpect(jsonPath("$.defendant_account_party.address.address_line_1").value("Lumber House"))
            // Validate that ETag header exists and is numeric (e.g. "0", "1", etc.)
            .andExpect(header().string("ETag", matchesPattern("\"\\d+\"")));

        // Schema validation
        jsonSchemaValidationService.validateOrError(body, DEFENDANT_PARTY_RESPONSE_SCHEMA);
    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: Get Defendant Account Party - Organisation Only [@PO-1973]")
    void legacyGetDefendantAccountParty_Organisation() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(
            get(URL_BASE + "/555/defendant-account-parties/555").header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        String etag = actions.andReturn().getResponse().getHeader("ETag");
        final Long version = objectMapper.readTree(body).path("version").asLong();

        log.info(":legacy_getDefendantAccountParty_Organisation body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":legacy_getDefendantAccountParty_Organisation ETag: {}", etag);

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.party_details.organisation_flag").value(true)).andExpect(
                jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_name").value(
                    "TechCorp Solutions Ltd"))
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details").doesNotExist())
            .andExpect(header().string("ETag", "\"1\""));

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_PARTY_RESPONSE_SCHEMA);
    }


    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: Get Defendant Account Party - 500 Error [@PO-1973]")
    void legacyGetDefendantAccountParty_500Error() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(
            get(URL_BASE + "/500/defendant-account-parties/500").header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":legacy_getDefendantAccountParty_500Error body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(header().doesNotExist("ETag")); // no ETag on error payloads
    }


    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: PUT Replace DAP")
    void legacyPutReplaceDefendantAccountParty_Success() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + 1 + "\"");

        String body = """
            {
                    "defendant_account_party_type": "Defendant",
                    "party_details": {
                    "party_id": "20010",
                    "organisation_flag": true,
                    "organisation_details": { "organisation_name": "StillCo" }
                 }
            }
            """;

        var res = mockMvc.perform(
            put("/defendant-accounts/77/defendant-account-parties/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        log.info("PUT DAP missing DAP resp:\n{}", res.andReturn().getResponse().getContentAsString());

        res.andExpect(status().isOk())
            .andExpect(header().string("etag", matchesPattern("\"\\d+\"")))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type")
                .value("Defendant"));

    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: PUT Replace DAP")
    void legacyPutReplaceDefendantAccountParty_500Error() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + 1 + "\"");

        String body = """
            {
                    "defendant_account_party_type": "Defendant",
                    "party_details": {
                    "party_id": "20010",
                    "organisation_flag": true,
                    "organisation_details": { "organisation_name": "StillCo" }
                 }
            }
            """;

        var res = mockMvc.perform(
            put("/defendant-accounts/500/defendant-account-parties/500")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        log.info("PUT DAP missing DAP resp:\n{}", res.andReturn().getResponse().getContentAsString());

        res.andExpect(status().is5xxServerError());

    }

    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @Test
    @DisplayName("LEGACY: Get header summary for non-existent ID returns 500")
    void getHeaderSummary_Legacy_500() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions ra =
            mockMvc.perform(get(URL_BASE + "/500/header-summary").header("authorization", "Bearer some_value"));

        String body = ra.andReturn().getResponse().getContentAsString();
        log.info(":getHeaderSummary_Legacy_500: Response body:\n{}", ToJsonString.toPrettyJson(body));

        ra.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    void testLegacyGetPaymentTerms() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/77/payment-terms/latest").header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetPaymentTerms: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))

            .andExpect(jsonPath("$.payment_terms.days_in_default").value(120))
            .andExpect(jsonPath("$.payment_terms.date_days_in_default_imposed").value("2025-10-12"))
            .andExpect(jsonPath("$.payment_terms.reason_for_extension").value(""))
            .andExpect(jsonPath("$.payment_terms.payment_terms_type.payment_terms_type_code").value("B"))
            .andExpect(jsonPath("$.payment_terms.effective_date").value("2025-10-12"))
            .andExpect(jsonPath("$.payment_terms.instalment_period.instalment_period_code").value("W"))
            .andExpect(jsonPath("$.payment_terms.lump_sum_amount").value(0.00))
            .andExpect(jsonPath("$.payment_terms.instalment_amount").value(0.00))

            .andExpect(jsonPath("$.posted_details.posted_date").value("2023-11-03"))
            .andExpect(jsonPath("$.posted_details.posted_by").value("01000000A"))
            .andExpect(jsonPath("$.posted_details.posted_by_name").value(""))

            .andExpect(jsonPath("$.payment_card_last_requested").value("2024-01-01"))
            .andExpect(jsonPath("$.date_last_amended").value("2024-01-03"))
            .andExpect(jsonPath("$.extension").value(false)).andExpect(jsonPath("$.last_enforcement").value("REM"));

    }


    void testLegacyGetDefendantAtAGlance() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/77/at-a-glance").header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetPaymentTerms: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value("DEF-ACC-00012345"))
            .andExpect(jsonPath("$.account_number").value("ACCT-9876543210"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant")).andExpect(jsonPath("$.is_youth").value(false))

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
            .andExpect(jsonPath("$.language_preferences.document_language_preference.language_code").value(nullValue()))
            .andExpect(jsonPath("$.language_preferences.document_language_preference.language_display_name").value(
                nullValue()))
            .andExpect(jsonPath("$.language_preferences.hearing_language_preference.language_code").value(nullValue()))
            .andExpect(
                jsonPath("$.language_preferences.hearing_language_preference.language_display_name").value(nullValue()))

            // payment_terms
            .andExpect(jsonPath("$.payment_terms.payment_terms_type.payment_terms_type_code").value("P"))
            .andExpect(jsonPath("$.payment_terms.payment_terms_type.payment_terms_type_display_name").value("Paid"))
            .andExpect(jsonPath("$.payment_terms.effective_date").value("2025-10-01"))
            .andExpect(jsonPath("$.payment_terms.instalment_period").value(nullValue()))
            .andExpect(jsonPath("$.payment_terms.lump_sum_amount").value(0.00))
            .andExpect(jsonPath("$.payment_terms.instalment_amount").value(50.00))

            // enforcement_status
            .andExpect(
                jsonPath("$.enforcement_status.last_enforcement_action.last_enforcement_action_id").value(nullValue()))
            .andExpect(jsonPath("$.enforcement_status.last_enforcement_action.last_enforcement_action_title").value(
                nullValue())).andExpect(jsonPath("$.enforcement_status.collection_order_made").value(false))
            .andExpect(jsonPath("$.enforcement_status.default_days_in_jail").value(0))
            // enforcement_override object with nested nulls
            .andExpect(
                jsonPath("$.enforcement_status.enforcement_override.enforcement_override_result").value(nullValue()))
            .andExpect(jsonPath("$.enforcement_status.enforcement_override.enforcer.enforcer_id").value(nullValue()))
            .andExpect(jsonPath("$.enforcement_status.enforcement_override.enforcer.enforcer_name").value(nullValue()))
            .andExpect(jsonPath("$.enforcement_status.enforcement_override.lja.lja_id").value(nullValue()))
            .andExpect(jsonPath("$.enforcement_status.enforcement_override.lja.lja_name").value(nullValue()))
            .andExpect(jsonPath("$.enforcement_status.last_movement_date").value("2025-09-30"))

            // comments_and_notes
            .andExpect(jsonPath("$.comments_and_notes.account_comment").value(
                "Account imported from legacy system on 2025-09-01.")).andExpect(
                jsonPath("$.comments_and_notes.free_text_note_1").value("Customer agreed to monthly instalments."))
            .andExpect(jsonPath("$.comments_and_notes.free_text_note_2").value("Preferred contact: letter.")).andExpect(
                jsonPath("$.comments_and_notes.free_text_note_3").value("Next review due after three payments."));

    }
}
