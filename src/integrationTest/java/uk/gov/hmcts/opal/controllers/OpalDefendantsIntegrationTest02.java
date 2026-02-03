package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@ActiveProfiles({"integration", "opal"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.OpalDefendantsIntegrationTest02")
class OpalDefendantsIntegrationTest02 extends AbstractIntegrationTest {

    static final String URL_BASE = "/defendant-accounts";
    static final String DEFENDANT_GLANCE_RESPONSE_SCHEMA = SchemaPaths.DEFENDANT_ACCOUNT
        + "/getDefendantAccountAtAGlanceResponse.json";
    static final String DEFENDANT_PARTY_RESPONSE_SCHEMA = SchemaPaths.DEFENDANT_ACCOUNT
        + "/getDefendantAccountPartyResponse.json";
    static final String DEFENDANT_FIXED_PENALTY_RESPONSE_SCHEMA = SchemaPaths.DEFENDANT_ACCOUNT
        + "/getDefendantAccountFixedPenaltyResponse.json";
    static final LocalDate ACCOUNT_77_BIRTH_DATE = LocalDate.of(1980, 2, 3);
    
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

    static String commentAndNotesPayload(String accountComment) {
        return commentAndNotesPayload(accountComment, null, null, null);
    }

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

    @Test
    @DisplayName("OPAL: Add Payment Terms – Happy Path [@PO-1718]")
    void test_Opal_AddPaymentTerms_Happy() throws Exception {
        // Ensure user has required permissions (pattern used across tests)
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(allPermissionsUser());

        // Pull the current version from DB to satisfy optimistic locking
        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class,
            77L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("some_value");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String requestJson = """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": { "payment_terms_type_code": "B",
                "payment_terms_type_display_name": "By date"},
                "instalment_period": { "instalment_period_code": "W" ,
                "instalment_period_display_name": "Weekly"},
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": true,
              "generate_payment_terms_change_letter": true
            }
            """;

        ResultActions result = mockMvc.perform(
            post("/defendant-accounts/77/payment-terms")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        ).andDo(MockMvcResultHandlers.print());

        String body = result.andReturn().getResponse().getContentAsString();
        String etag = result.andReturn().getResponse().getHeader(HttpHeaders.ETAG);

        log.info(":opalAddPaymentTerms_Happy response body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":opalAddPaymentTerms_Happy ETag: {}", etag);

        // Basic assertions: OK + JSON + expected fields
        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.payment_terms.days_in_default").value(30));
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Unauthorized when missing auth header [@PO-1718]")
    void test_Opal_AddPaymentTerms_Unauthorized() throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).checkForAuthorisedUser(any());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        String requestJson = """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": {
                  "payment_terms_type_code": "B",
                  "payment_terms_type_display_name": "By date"
                },
                "instalment_period": {
                  "instalment_period_code": "W",
                  "instalment_period_display_name": "Weekly"
                },
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": false,
              "generate_payment_terms_change_letter": false
            }
            """;

        mockMvc.perform(
                post("/defendant-accounts/77/payment-terms")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""));
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Forbidden when user lacks permission [@PO-1718]")
    void test_Opal_AddPaymentTerms_Forbidden() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            UserState.builder()
                .userId(999L)
                .userName("no-permission-user")
                .businessUnitUser(java.util.Collections.emptySet())
                .build()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token_without_permission");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        String requestJson = """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": {
                  "payment_terms_type_code": "B",
                  "payment_terms_type_display_name": "By date"
                },
                "instalment_period": {
                  "instalment_period_code": "W",
                  "instalment_period_display_name": "Weekly"
                },
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": false,
              "generate_payment_terms_change_letter": false
            }
            """;

        mockMvc.perform(
                post("/defendant-accounts/77/payment-terms")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Not Found when account not in header BU [@PO-1718]")
    void test_Opal_AddPaymentTerms_NotFound_WrongBU() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            UserStateUtil.permissionUser((short) 99, FinesPermission.AMEND_PAYMENT_TERMS)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("some_value");
        headers.add("Business-Unit-Id", "99");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        String requestJson = """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": {
                  "payment_terms_type_code": "B",
                  "payment_terms_type_display_name": "By date"
                },
                "instalment_period": {
                  "instalment_period_code": "W",
                  "instalment_period_display_name": "Weekly"
                },
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": false,
              "generate_payment_terms_change_letter": false
            }
            """;

        mockMvc.perform(
                post("/defendant-accounts/77/payment-terms")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Conflict when If-Match does not match [@PO-1718]")
    void test_Opal_AddPaymentTerms_IfMatchConflict() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"9999\"");

        String requestJson = """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": {
                  "payment_terms_type_code": "B",
                  "payment_terms_type_display_name": "By date"
                },
                "instalment_period": {
                  "instalment_period_code": "W",
                  "instalment_period_display_name": "Weekly"
                },
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": false,
              "generate_payment_terms_change_letter": false
            }
            """;

        mockMvc.perform(
                post("/defendant-accounts/77/payment-terms")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/optimistic-locking"));
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Conflict when If-Match missing [@PO-1718]")
    void test_Opal_AddPaymentTerms_IfMatchMissing() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");

        String requestJson = """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": {
                  "payment_terms_type_code": "B",
                  "payment_terms_type_display_name": "By date"
                },
                "instalment_period": {
                  "instalment_period_code": "W",
                  "instalment_period_display_name": "Weekly"
                },
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": false,
              "generate_payment_terms_change_letter": false
            }
            """;

        ResultActions a = mockMvc.perform(
            post("/defendant-accounts/77/payment-terms")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        String body = a.andReturn().getResponse().getContentAsString();
        log.info(":addPaymentTerms_IfMatchMissing body:\n{}", body);

        a.andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type",
                org.hamcrest.Matchers.anyOf(is("https://hmcts.gov.uk/problems/resource-conflict"),
                    is("https://hmcts.gov.uk/problems/optimistic-locking"))));
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Schema validation error [@PO-1718]")
    void test_Opal_AddPaymentTerms_SchemaValidation() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        mockMvc.perform(
                post("/defendant-accounts/77/payment-terms")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"request_payment_card\": true}")
            )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/json-schema-validation"));
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Timeout [@PO-1718]")
    void test_Opal_AddPaymentTerms_Timeout() throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.REQUEST_TIMEOUT, "Timeout"));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("some_value");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        String requestJson = """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": {
                  "payment_terms_type_code": "B",
                  "payment_terms_type_display_name": "By date"
                },
                "instalment_period": {
                  "instalment_period_code": "W",
                  "instalment_period_display_name": "Weekly"
                },
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": false,
              "generate_payment_terms_change_letter": false
            }
            """;

        mockMvc.perform(
                post("/defendant-accounts/77/payment-terms")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isRequestTimeout());
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Service unavailable [@PO-1718]")
    void test_Opal_AddPaymentTerms_ServiceUnavailable() throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenThrow(new ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Gateway down"));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("some_value");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        String requestJson = """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": {
                  "payment_terms_type_code": "B",
                  "payment_terms_type_display_name": "By date"
                },
                "instalment_period": {
                  "instalment_period_code": "W",
                  "instalment_period_display_name": "Weekly"
                },
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": false,
              "generate_payment_terms_change_letter": false
            }
            """;

        mockMvc.perform(
                post("/defendant-accounts/77/payment-terms")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Server error [@PO-1718]")
    void test_Opal_AddPaymentTerms_ServerError() throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenThrow(new ResponseStatusException(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Boom"));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("some_value");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        String requestJson = """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": {
                  "payment_terms_type_code": "B",
                  "payment_terms_type_display_name": "By date"
                },
                "instalment_period": {
                  "instalment_period_code": "W",
                  "instalment_period_display_name": "Weekly"
                },
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": false,
              "generate_payment_terms_change_letter": false
            }
            """;

        mockMvc.perform(
                post("/defendant-accounts/77/payment-terms")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("OPAL: Get header summary for non-existent ID returns 404")
    void getHeaderSummary_Opal_NotFound() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

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
    public void opalGetDefendantAccountParty_Happy() throws Exception {

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
                    "A11111A"), jsonPath(
                    "$.defendant_account_party.party_details.individual_details.individual_aliases[0].alias_id").value(
                    "7701"), jsonPath(
                    "$.defendant_account_party.party_details.individual_details"
                        + ".individual_aliases[0].sequence_number").value(
                    1), jsonPath(
                    "$.defendant_account_party.party_details.individual_details.individual_aliases[0].surname").value(
                    "Smith"), jsonPath(
                    "$.defendant_account_party.party_details.individual_details.individual_aliases[0].forenames").value(
                    "Annie"), jsonPath(
                    "$.defendant_account_party.party_details.individual_details.individual_aliases[1].alias_id").value(
                    "7702"), jsonPath(
                    "$.defendant_account_party.party_details.individual_details.individual_aliases[1]"
                        + ".sequence_number").value(
                    2), jsonPath(
                    "$.defendant_account_party.party_details.individual_details.individual_aliases[1].surname").value(
                    "Johnson"), jsonPath(
                    "$.defendant_account_party.party_details.individual_details.individual_aliases[1].forenames").value(
                    "Anne"), jsonPath(
                    "$.defendant_account_party.party_details.individual_details.individual_aliases[2].alias_id").value(
                    "7703"), jsonPath(
                    "$.defendant_account_party.party_details.individual_details"
                        + ".individual_aliases[2].sequence_number").value(
                    3), jsonPath(
                    "$.defendant_account_party.party_details.individual_details.individual_aliases[2].surname").value(
                    "Williams"), jsonPath(
                    "$.defendant_account_party.party_details.individual_details.individual_aliases[2].forenames").value(
                    "Ana"), jsonPath("$.defendant_account_party.address.address_line_1").value("Lumber House"),
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
                jsonPath(
                    "$.defendant_account_party.language_preferences.document_language_preference.language_code").value(
                    "EN"), jsonPath(
                    "$.defendant_account_party.language_preferences.document_language_preference"
                        + ".language_display_name").value(
                    "English only"), jsonPath(
                    "$.defendant_account_party.language_preferences.hearing_language_preference.language_code").value(
                    "EN"), jsonPath(
                    "$.defendant_account_party.language_preferences.hearing_language_preference"
                        + ".language_display_name").value(
                    "English only"));
        String body = actions.andReturn().getResponse().getContentAsString();
        // Schema validation
        jsonSchemaValidationService.validateOrError(body, DEFENDANT_PARTY_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account Party - Organisation Only [@PO-1588]")
    public void opalGetDefendantAccountParty_Organisation() throws Exception {
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
                    hasSize(2)), jsonPath(
                    "$.defendant_account_party.party_details.organisation_details"
                        + ".organisation_aliases[0].alias_id").value(
                    "5551"), jsonPath("$.defendant_account_party.party_details.organisation_details"
                    + ".organisation_aliases[0].sequence_number").value(1), jsonPath(
                    "$.defendant_account_party.party_details.organisation_details"
                        + ".organisation_aliases[0].organisation_name").value("TechCorp Ltd"), jsonPath(
                    "$.defendant_account_party.party_details.organisation_details"
                        + ".organisation_aliases[1].alias_id").value(
                    "5552"), jsonPath("$.defendant_account_party.party_details.organisation_details"
                    + ".organisation_aliases[1].sequence_number").value(2), jsonPath(
                    "$.defendant_account_party.party_details.organisation_details"
                        + ".organisation_aliases[1].organisation_name").value("TC Solutions Limited"),
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
                jsonPath(
                    "$.defendant_account_party.language_preferences.document_language_preference.language_code").value(
                    is(nullValue())), jsonPath(
                    "$.defendant_account_party.language_preferences.document_language_preference"
                        + ".language_display_name").value(
                    is(nullValue())), jsonPath(
                    "$.defendant_account_party.language_preferences.hearing_language_preference.language_code").value(
                    is(nullValue())), jsonPath(
                    "$.defendant_account_party.language_preferences.hearing_language_preference"
                        + ".language_display_name").value(
                    is(nullValue())));
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account Party - Null/Optional Fields [@PO-1588]")
    public void opalGetDefendantAccountParty_NullFields() throws Exception {
        ResultActions actions = mockMvc.perform(
            get("/defendant-accounts/88/defendant-account-parties/88").header("Authorization", "Bearer test-token"));
        log.info("Null fields response:\n" + actions.andReturn().getResponse().getContentAsString());
        actions.andExpect(status(
            ).isOk())
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.surname").doesNotExist())
            .andExpect(jsonPath("$.defendant_account_party.address.address_line_1").doesNotExist());
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an individual")
    public void opalGetAtAGlance_Individual() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        String expectedAge = String.valueOf(Period.between(ACCOUNT_77_BIRTH_DATE, LocalDate.now()).getYears());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/77/at-a-glance").header("authorization", "Bearer some_value"));

        String expectedAge = String.valueOf(Period.between(LocalDate.of(1980, 2, 3), LocalDate.now()).getYears());

        String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
        log.info(":testGetAtAGlance: Party is an individual. etag header: \n{}", headers);
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetAtAGlance: Party is an individual. Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // verify the header contains an ETag value
            .andExpect(header().string("etag", matchesPattern("\"\\d+\"")))
            .andExpect(jsonPath("$.defendant_account_id").value("77"))
            .andExpect(jsonPath("$.account_number").value("177A"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant")).andExpect(jsonPath("$.is_youth").exists())
            .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
            .andExpect(jsonPath("$.party_details.organisation_details").doesNotExist())
            .andExpect(jsonPath("$.party_details.individual_details.age").value(expectedAge))
            .andExpect(jsonPath("$.address").exists()).andExpect(jsonPath("$.payment_terms").exists())
            .andExpect(jsonPath("$.enforcement_status").exists())
            .andExpect(jsonPath("$.enforcement_status.last_enforcement_action.last_enforcement_action_id").value("10"))
            .andExpect(jsonPath("$.enforcement_status.last_enforcement_action.last_enforcement_action_title").value(
                IsNull.nullValue())).andExpect(jsonPath("$.comments_and_notes").exists());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_GLANCE_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an individual (Parent/Guardian)")
    public void opalGetAtAGlance_Individual_ParentGuardian() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        String expectedAge = String.valueOf(Period.between(ACCOUNT_77_BIRTH_DATE, LocalDate.now()).getYears());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/10004/at-a-glance").header("authorization", "Bearer some_value"));

        String expectedAge = String.valueOf(Period.between(LocalDate.of(1980, 2, 3), LocalDate.now()).getYears());

        String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
        log.info(":testGetAtAGlance: Party is an individual (Parent/Guardian). etag header: \n" + headers);
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testGetAtAGlance: Party is an individual (Parent/Guardian). Response body:\n" + ToJsonString.toPrettyJson(
                body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // verify the header contains an ETag value
            .andExpect(header().string("etag", "\"1\"")).andExpect(jsonPath("$.defendant_account_id").value("10004"))
            .andExpect(jsonPath("$.account_number").value("10004A"))
            .andExpect(jsonPath("$.debtor_type").value("Parent/Guardian")).andExpect(jsonPath("$.is_youth").exists())
            .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
            .andExpect(jsonPath("$.party_details.organisation_details").doesNotExist())
            .andExpect(jsonPath("$.party_details.individual_details.age").value(expectedAge))
            .andExpect(jsonPath("$.address").exists()).andExpect(jsonPath("$.payment_terms").exists())
            .andExpect(jsonPath("$.enforcement_status").exists())
            // verify comments_and_notes node is not present (no test data added as these are optional)
            .andExpect(jsonPath("$.comments_and_notes").doesNotExist());
        ;

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_GLANCE_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an organisation")
    public void opalGetAtAGlance_Organisation() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/10001/at-a-glance").header("authorization", "Bearer some_value"));

        String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
        log.info(":testGetAtAGlance: Party is an organisation. etag header: \n" + headers);
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetAtAGlance: Party is an organisation. Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // verify the header contains an ETag value
            .andExpect(header().string("etag", "\"1\"")).andExpect(jsonPath("$.defendant_account_id").value("10001"))
            .andExpect(jsonPath("$.account_number").value("10001A"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant")).andExpect(jsonPath("$.is_youth").value(false))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.party_details.individual_details").doesNotExist())
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"))
            .andExpect(jsonPath("$.address").exists()).andExpect(jsonPath("$.language_preferences").exists())
            // verify both language preferences are populated
            .andExpect(jsonPath("$.language_preferences.hearing_language_preference.language_display_name").value(
                "English only")).andExpect(
                jsonPath("$.language_preferences.document_language_preference.language_display_name")
                    .value("English only"))
            .andExpect(jsonPath("$.payment_terms").exists()).andExpect(jsonPath("$.enforcement_status").exists())
            .andExpect(jsonPath("$.enforcement_status.collection_order_made").exists())
            // verify comments_and_notes node is present (test data included for these optional fields)
            .andExpect(jsonPath("$.comments_and_notes").exists());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_GLANCE_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an organisation. \n"
        + "No language preferences set (as these are optional) \n"
        + "No account comments or notes set (as these are optional)")
    public void opalGetAtAGlance_Organisation_NoLanguagePrefs() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/10002/at-a-glance").header("authorization", "Bearer some_value"));

        String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
        log.info(":testGetAtAGlance: Party is an organisation. etag header: \n" + headers);
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetAtAGlance: Party is an organisation. Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // verify the header contains an ETag value
            .andExpect(header().string("etag", "\"1\"")).andExpect(jsonPath("$.defendant_account_id").value("10002"))
            .andExpect(jsonPath("$.account_number").value("10002A"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant"))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.party_details.individual_details").doesNotExist())
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"))
            // verify language preferences node is null
            .andExpect(jsonPath("$.language_preferences").doesNotExist())
            // verify comments_and_notes node is absent (no data included for these optional fields)
            .andExpect(jsonPath("$.comments_and_notes").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_GLANCE_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an organisation. "
        + "One language preference not set (as this is optional)")
    public void opalGetAtAGlance_Organisation_NoHearingLanguagePref() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/10003/at-a-glance").header("authorization", "Bearer some_value"));

        String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
        log.info(":testGetAtAGlance: Party is an organisation. etag header: \n" + headers);
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetAtAGlance: Party is an organisation. Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // verify the header contains an ETag value
            .andExpect(header().string("etag", "\"1\"")).andExpect(jsonPath("$.defendant_account_id").value("10003"))
            .andExpect(jsonPath("$.account_number").value("10003A"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant"))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.party_details.individual_details").doesNotExist())
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"))
            .andExpect(jsonPath("$.language_preferences.document_language_preference.language_display_name").value(
                "English only"))
            // verify hearing_language_preference node is null (optional)
            .andExpect(jsonPath("$.language_preferences.hearing_language_preference").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_GLANCE_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName(
        "OPAL: Get Defendant Account At A Glance [@PO-1564] - 401 Unauthorized \n" + "when no auth header provided \n")
    void opalGetAtAGlance_missingAuthHeader_returns401() throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized")).when(userStateService)
            .checkForAuthorisedUser(any());

        mockMvc.perform(get(URL_BASE + "/10003/at-a-glance").accept(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(status().isUnauthorized()).andExpect(content().string(""));
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - 403 Forbidden\n" + "No auth header provided \n")
    void opalGetAtAGlance_authenticatedWithoutPermission_returns403() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Forbidden")).when(
            userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(get(URL_BASE + "/10003/at-a-glance").accept(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(status().isForbidden()).andExpect(content().string(""));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Happy Path [@PO-1565]")
    void opalUpdateDefendantAccount_Happy() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Read the current version to avoid optimistic locking conflicts
        Integer currentVersion =
            jdbcTemplate.queryForObject("SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 77L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("some_value");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\""); // use actual version

        String requestJson = commentAndNotesPayload("hello");

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON).content(requestJson));

        String body = a.andReturn().getResponse().getContentAsString();
        String etag = a.andReturn().getResponse().getHeader("ETag");

        log.info(":opal_updateDefendantAccount_Happy body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":opal_updateDefendantAccount_Happy ETag: {}", etag);

        a.andExpect(status().isOk()).andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // ETag should be a quoted integer, e.g. "1"
        assertNotNull(etag, "ETag must be present");
        assertTrue(etag.matches("^\"\\d+\"$"), "ETag should be a quoted number");

        // Validate response JSON against schema
        jsonSchemaValidationService.validateOrError(body, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
    }


    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - If-Match Mismatch [@PO-1565]")
    void patch_conflict_whenIfMatchDoesNotMatch() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"999\"");

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
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        // Intentionally DO NOT add If-Match

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON)
                .content(commentAndNotesPayload("hello")));

        String body = a.andReturn().getResponse().getContentAsString();
        log.info(":patch_conflict_whenIfMatchMissing body:\n{}", body);

        a.andExpect(status().isConflict()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            // Depending on VersionUtils, this may be resource-conflict or optimistic-locking.
            .andExpect(jsonPath("$.type",
                org.hamcrest.Matchers.anyOf(is("https://hmcts.gov.uk/problems/resource-conflict"),
                    is("https://hmcts.gov.uk/problems/optimistic-locking"))));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Forbidden when user lacks permission [@PO-1565]")
    void patch_forbidden_whenUserLacksAccountMaintenance() throws Exception {
        // user without ACCOUNT_MAINTENANCE
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            UserState.builder().userId(999L).userName("no-perm-user").businessUnitUser(java.util.Collections.emptySet())
                .build());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token_without_perm");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        String body = commentAndNotesPayload("hello");

        mockMvc.perform(patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isForbidden()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Not Found (account not in BU) [@PO-1565]")
    void patch_notFound_whenAccountNotInHeaderBU() throws Exception {
        // User is authenticated and has perms, but BU header doesn't match the DA's BU → 404
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "99");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        String requestJson = commentAndNotesPayload("hello");

        var result = mockMvc.perform(
            patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON).content(requestJson));

        log.info(":patch_notFound_whenAccountNotInHeaderBU response:\n{}",
            result.andReturn().getResponse().getContentAsString());

        result.andExpect(status().isNotFound()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Wrong Business Unit [@PO-1565]")
    void patch_badRequest_whenMultipleGroupsProvided() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

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
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        mockMvc.perform(patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON).content("""
                  {"comment_and_notes":{"free_text_note_1": 123}}
                """)).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/json-schema-validation"));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Update Enforcement Court [@PO-1565]")
    void patch_updatesEnforcementCourt_andValidatesResponseSchema() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // 🔑 Get the current version number from DB for account 77
        Integer currentVersion =
            jdbcTemplate.queryForObject("SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 77L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String body = """
            {
              "enforcement_court": {
                "court_id": 100,
                "court_name": "Central Magistrates"
              }
            }
            """;

        var a = mockMvc.perform(
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
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Use the live version from DB to avoid 409 conflicts
        Integer currentVersion =
            jdbcTemplate.queryForObject("SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 77L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        mockMvc.perform(patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON).content("""
              {"collection_order":{"collection_order_flag":true,"collection_order_date":"2025-01-01"}}
            """)).andExpect(status().isOk()).andExpect(header().exists("ETag"));
    }

    @Test
    @DisplayName("OPAL: PATCH Update Defendant Account - Update Enforcement Override [@PO-1565]")
    void patch_updatesEnforcementOverride() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Use the live version from DB to avoid 409 conflicts
        Integer currentVersion =
            jdbcTemplate.queryForObject("SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 77L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

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
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Pull the current version from DB to satisfy optimistic locking
        Integer currentVersion =
            jdbcTemplate.queryForObject("SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 77L);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String requestJson = commentAndNotesPayload("etag test");

        ResultActions result = mockMvc.perform(
            patch(URL_BASE + "/77").headers(headers).contentType(MediaType.APPLICATION_JSON).content(requestJson));

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


    @Test
    @DisplayName("OPAL: Get Defendant Account Fixed Penalty [@PO-1819]")
    void testGetDefendantAccountFixedPenalty() throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(
            get("/defendant-accounts/77/fixed-penalty")
                .header("authorization", "Bearer some_value")
        );

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDefendantAccountFixedPenalty: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("etag", matchesPattern("\"\\d+\"")))
            .andExpect(jsonPath("$.vehicle_fixed_penalty_flag").value(true))
            .andExpect(jsonPath("$.fixed_penalty_ticket_details.issuing_authority")
                .value("Kingston-upon-Thames Mags Court"))
            .andExpect(jsonPath("$.fixed_penalty_ticket_details.ticket_number").value("888"))
            .andExpect(jsonPath("$.fixed_penalty_ticket_details.place_of_offence").value("London"))
            .andExpect(jsonPath("$.vehicle_fixed_penalty_details.vehicle_registration_number").value("AB12CDE"))
            .andExpect(jsonPath("$.vehicle_fixed_penalty_details.vehicle_drivers_license").value("DOE1234567"))
            .andExpect(jsonPath("$.vehicle_fixed_penalty_details.notice_number").value("PN98765"))
            .andExpect(jsonPath("$.vehicle_fixed_penalty_details.date_notice_issued").exists());

        // Schema validation
        jsonSchemaValidationService.validateOrError(body, DEFENDANT_FIXED_PENALTY_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account Fixed Penalty - 404 when not found [@PO-1819]")
    void testGetDefendantAccountFixedPenalty_NotFound() throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(
            get("/defendant-accounts/99999/fixed-penalty")
                .header("authorization", "Bearer some_value")
        );

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDefendantAccountFixedPenalty_NotFound: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"))
            .andExpect(header().doesNotExist("ETag"));
    }


    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance - " + "Verify aliases array organisation [@PO-2312]")
    void testGetAtAGlance_VerifyAliasesArray_Organisation() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/10001/at-a-glance").header("authorization", "Bearer some_value"));

        String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
        log.info(":testGetAtAGlance: Verify aliases array. etag header: \n{}", headers);
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetAtAGlance: Verify aliases array. Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("etag", "\"1\"")).andExpect(jsonPath("$.defendant_account_id").value("10001"))
            .andExpect(jsonPath("$.account_number").value("10001A"))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.party_details.organisation_details").exists())
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"))
            // Verify that the organisation_aliases array exists and contains the expected aliases
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases").isArray())
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases").isNotEmpty())
            // Verify the array has exactly 3 aliases
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases.length()").value(3))
            // Verify the first alias details
            .andExpect(
                jsonPath("$.party_details.organisation_details.organisation_aliases[0].alias_id").value("100011"))
            .andExpect(
                jsonPath("$.party_details.organisation_details.organisation_aliases[0].sequence_number").value(1))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[0].organisation_name").value(
                "AliasOrg"))
            // Verify the second alias details
            .andExpect(
                jsonPath("$.party_details.organisation_details.organisation_aliases[1].alias_id").value("100012"))
            .andExpect(
                jsonPath("$.party_details.organisation_details.organisation_aliases[1].sequence_number").value(2))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[1].organisation_name").value(
                "SecondAliasOrg"))
            // Verify the third alias details
            .andExpect(
                jsonPath("$.party_details.organisation_details.organisation_aliases[2].alias_id").value("100013"))
            .andExpect(
                jsonPath("$.party_details.organisation_details.organisation_aliases[2].sequence_number").value(3))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_aliases[2].organisation_name").value(
                "ThirdAliasOrg")).andExpect(jsonPath("$.party_details.individual_details").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_GLANCE_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("OPAL: Get Defendant Account At A Glance - " + "Verify aliases array individual [@PO-2312]")
    void testGetAtAGlance_VerifyAliasesArray_Individual() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

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
            // Verify that the individual_aliases array exists and contains the expected aliases
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases").isArray())
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases").isNotEmpty())
            // Verify the array has exactly 3 aliases
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases.length()").value(3))
            // Verify the first alias details
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[0].alias_id").value("7701"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[0].sequence_number").value(1))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[0].forenames").value("Annie"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[0].surname").value("Smith"))
            // Verify the second alias details
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[1].alias_id").value("7702"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[1].sequence_number").value(2))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[1].forenames").value("Anne"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[1].surname").value("Johnson"))
            // Verify the third alias details
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[2].alias_id").value("7703"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[2].sequence_number").value(3))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[2].forenames").value("Ana"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases[2].surname").value("Williams"))
            .andExpect(jsonPath("$.party_details.organisation_details").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_GLANCE_RESPONSE_SCHEMA);
    }

    @Test
    @DisplayName("OPAL: PUT Replace DAP – Not Found (account not in BU)")
    void put_notFound_whenAccountNotInHeaderBU() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Use the known test account we seed below: 20010 in BU=78
        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class,
            20010L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "99");                 // wrong BU on purpose
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String body = """
            {
                "defendant_account_party_type": "Defendant",
                "is_debtor": true,
                "party_details": {
                "party_id": "20010",
                "organisation_flag": true,
                "organisation_details": { "organisation_name": "PutCo Ltd" }
                }
            }
            """;

        var res = mockMvc.perform(
            put("/defendant-accounts/20010/defendant-account-parties/20010")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        log.info("PUT DAP wrong BU resp:\n{}", res.andReturn().getResponse().getContentAsString());

        res.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
    }

    @Test
    @DisplayName("OPAL: PUT Replace DAP – Happy path (updates party + debtor + bumps version)")
    void put_happyPath_updates_andReturnsResponse() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Get current version so we can set If-Match and then assert ETag bumped
        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class,
            20010L
        );

        String etag = "\"" + currentVersion + "\"";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, etag);

        String body = """
            {
                "defendant_account_party_type": "Defendant",
                "is_debtor": true,
                "party_details": {
                 "party_id": "20010",
                 "organisation_flag": true,
                  "organisation_details": { "organisation_name": "PutCo Ltd" }
                },
                "address": {
                "address_line_1": "100 Put Street",
                "postcode": "PU1 1TT"
                },
                    "contact_details": {
                    "primary_email_address": "putco@example.com",
                    "work_telephone_number": "0207000000"
                },
                    "vehicle_details": {
                    "vehicle_make_and_model": "VW Golf",
                    "vehicle_registration": "PU70ABC"
                },
                    "employer_details": {
                    "employer_name": "Put Employer",
                    "employer_address": {
                        "address_line_1": "1 Employer Way",
                        "postcode": "EM1 1AA"
                    }
                },
                    "language_preferences": {
                    "document_language_preference": { "language_code": "EN" },
                    "hearing_language_preference":  { "language_code": "CY" }
                 }
            }
            """;

        var call = mockMvc.perform(
            put("/defendant-accounts/20010/defendant-account-parties/20010")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        String resp = call.andReturn().getResponse().getContentAsString();
        log.info("PUT DAP happy path resp:\n{}", resp);

        String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

        call.andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // response wrapper shape
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));


    }

    @Test
    @DisplayName("OPAL: PUT Replace DAP – DAP not found on account")
    void put_notFound_whenDapMissing() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class,
            20010L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

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
            put("/defendant-accounts/20010/defendant-account-parties/99999")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        log.info("PUT DAP missing DAP resp:\n{}", res.andReturn().getResponse().getContentAsString());

        res.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
    }

    @Test
    @DisplayName("OPAL: Add Payment Card Request – Happy Path [@PO-1719]")
    void opalAddPaymentCardRequest_Happy() throws Exception {

        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(allPermissionsUser());

        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class,
            901L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("some_value");
        headers.add("Business-Unit-Id", "78");
        headers.add("Business-Unit-User-Id", "TEST_USER_123");
        headers.add("If-Match", "\"" + currentVersion + "\"");

        ResultActions result = mockMvc.perform(
            post("/defendant-accounts/901/payment-card-request")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        );

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":opalAddPaymentCardRequest_Happy body:\n{}", ToJsonString.toPrettyJson(body));

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value(901));
    }

    @Test
    @DisplayName("OPAL: Add Payment Card Request – Not Found when account not in header BU [@PO-1719]")
    void opalAddPaymentCardRequest_NotFound_WrongBU() throws Exception {

        // User authenticated with all required permissions
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(allPermissionsUser());

        // Get correct version so request passes optimistic locking
        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class,
            88L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("some_value");
        headers.add("Business-Unit-Id", "99"); // Wrong BU → should trigger 404
        headers.add("If-Match", "\"" + currentVersion + "\"");

        ResultActions result = mockMvc.perform(
            post("/defendant-accounts/88/payment-card-request")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        );

        String body = result.andReturn().getResponse().getContentAsString();
        log.info(":opalAddPaymentCardRequest_NotFound_WrongBU body:\n{}", body);

        result.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("OPAL: Add Payment Card Request – Forbidden when user lacks permission [@PO-1719]")
    void opalAddPaymentCardRequest_Forbidden_NoPermission() throws Exception {

        // User with NO AMEND_PAYMENT_TERMS permission
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(
                UserState.builder()
                    .userId(123L)
                    .userName("no-permission")
                    .businessUnitUser(java.util.Collections.emptySet()) // no BU permissions
                    .build()
            );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token_without_permission");
        headers.add("Business-Unit-Id", "78");
        headers.add("If-Match", "\"0\"");

        mockMvc.perform(
                post("/defendant-accounts/88/payment-card-request")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("OPAL: Add Payment Card Request – Unauthorized when missing auth header [@PO-1719]")
    void opalAddPaymentCardRequest_Unauthorized() throws Exception {

        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(
                post("/defendant-accounts/88/payment-card-request")
                    .header("Business-Unit-Id", "78")
                    .header("If-Match", "\"0\"")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""));
    }

    @Test
    @DisplayName("OPAL: Add Payment Card Request – Conflict when If-Match does not match [@PO-1719]")
    void opalAddPaymentCardRequest_IfMatchConflict() throws Exception {

        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("some_value");
        headers.add("Business-Unit-Id", "78");
        headers.add("If-Match", "\"9999\""); // Wrong version

        mockMvc.perform(
                post("/defendant-accounts/88/payment-card-request")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/optimistic-locking"));
    }

    @Test
    @DisplayName("OPAL: Add Payment Card Request – Conflict when PCR already exists [@PO-1719]")
    void opalAddPaymentCardRequest_AlreadyExists() throws Exception {

        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(allPermissionsUser());

        when(accessTokenService.extractName(any()))
            .thenReturn("TEST_USER_DISPLAY_NAME");   // <-- REQUIRED FIX

        // ---- FIRST CALL ----
        Integer version1 = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class,
            88L
        );
        log.info("INITIAL VERSION = {}", version1);

        HttpHeaders headers1 = new HttpHeaders();
        headers1.setBearerAuth("some_value");
        headers1.add("Business-Unit-Id", "78");
        headers1.add("Business-Unit-User-Id", "TEST_USER_123");
        headers1.add("If-Match", "\"" + version1 + "\"");

        mockMvc.perform(
            post("/defendant-accounts/88/payment-card-request")
                .headers(headers1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        ).andExpect(status().isOk());

        // ---- SECOND CALL ----
        Integer version2 = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class,
            88L
        );

        HttpHeaders headers2 = new HttpHeaders();
        headers2.setBearerAuth("some_value");
        headers2.add("Business-Unit-Id", "78");
        headers2.add("Business-Unit-User-Id", "TEST_USER_123");
        headers2.add("If-Match", "\"" + version2 + "\"");

        ResultActions result = mockMvc.perform(
            post("/defendant-accounts/88/payment-card-request")
                .headers(headers2)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        );

        result.andExpect(status().isConflict())
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/resource-conflict"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail")
                .value("A payment card request already exists for this account."))
            .andExpect(jsonPath("$.resourceType").value("DefendantAccountEntity"))
            .andExpect(jsonPath("$.resourceId").value("88"))
            .andExpect(jsonPath("$.retriable").value(true))
            .andExpect(jsonPath("$.conflictReason").doesNotExist());
    }


    @Test
    @DisplayName("OPAL: PUT Replace DAP – Individual aliases upsert/trim on isolated IDs (22004)")
    void put_individual_aliases_upsert_and_trim() throws Exception {
        // Authorise
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Get current version → ETag
        Integer currentVersion =
            jdbcTemplate.queryForObject("SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 22004L);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, etag);

        // Request: individual with aliases:
        //  - Update existing (id=2200401, seq=1) to Jane Doe
        //  - Add NEW alias (no id) as J. Smith with seq=2
        //  - Omit any others → service should trim extras
        String body = """
            {
              "defendant_account_party_type": "Defendant",
              "is_debtor": false,
              "party_details": {
                "party_id": "22004",
                "organisation_flag": false,
                "individual_details": {
                  "forenames": "MainForenames",
                  "surname": "MainSurname",
                  "individual_aliases": [
                    { "alias_id": "2200401", "sequence_number": 1, "forenames": "Jane", "surname": "Doe" },
                    { "alias_id": "","sequence_number": 2, "forenames": "J.", "surname": "Smith" }
                  ]
                }
              }
            }
            """;

        var call = mockMvc.perform(put("/defendant-accounts/22004/defendant-account-parties/22004").headers(headers)
            .contentType(MediaType.APPLICATION_JSON).content(body));

        String resp = call.andReturn().getResponse().getContentAsString();
        log.info("PUT DAP individual aliases (22004) resp:\n{}", resp);

        String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";

        call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

        // Verify DB aliases now: should be two rows, seq 1..2; id 2200401 updated + one new row
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT alias_id, sequence_number, forenames, surname, organisation_name "
                + "FROM aliases WHERE party_id = ? ORDER BY sequence_number", 22004L);

        // Exactly 2 aliases
        assertEquals(2, rows.size());

        // Row 1 (updated)
        assertEquals(1, ((Number) rows.get(0).get("sequence_number")).intValue());
        assertEquals(2200401L, ((Number) rows.get(0).get("alias_id")).longValue());
        assertEquals("Jane", rows.get(0).get("forenames"));
        assertEquals("Doe", rows.get(0).get("surname"));
        assertNull(rows.get(0).get("organisation_name"));

        // Row 2 (newly created, id != 2200401)
        assertEquals(2, ((Number) rows.get(1).get("sequence_number")).intValue());
        assertNotNull(rows.get(1).get("alias_id"));
        assertNotEquals(2200401L, ((Number) rows.get(1).get("alias_id")).longValue());
        assertEquals("J.", rows.get(1).get("forenames"));
        assertEquals("Smith", rows.get(1).get("surname"));
        assertNull(rows.get(1).get("organisation_name"));

        Integer updatedVersion =
            jdbcTemplate.queryForObject("SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 22004L);

        assertEquals(currentVersion + 1, updatedVersion);
    }

    @Test
    @Sql(scripts = "classpath:db/insertData/update_into_parties.sql", executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("OPAL: PUT Replace DAP – Organisation aliases upsert (update + insert) and trim (delete missing)")
    void put_org_aliases_upsert_and_trim() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // 1) ETag for If-Match
        Integer currentVersion =
            jdbcTemplate.queryForObject("SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 20010L);
        assertNotNull(currentVersion);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, etag);

        String body = """
            {
              "defendant_account_party_type": "Defendant",
              "is_debtor": true,
              "party_details": {
                "party_id": "20010",
                "organisation_flag": true,
                "organisation_details": {
                  "organisation_name": "PutCo Ltd",
                  "organisation_aliases": [
                    { "alias_id": 200101, "sequence_number": 2, "organisation_name": "PutCo Alias One (updated)" },
                    { "sequence_number": 3, "organisation_name": "PutCo Alias Three (new)" }
                  ]
                }
              },
              "address": { "address_line_1": "100 Put Street", "postcode": "PU1 1TT" },
              "contact_details": { "primary_email_address": "putco@example.com",
              "work_telephone_number": "0207000000" },
              "vehicle_details": { "vehicle_make_and_model": "VW Golf", "vehicle_registration": "PU70ABC" },
              "employer_details": {
                "employer_name": "Put Employer",
                "employer_address": { "address_line_1": "1 Employer Way", "postcode": "EM1 1AA" }
              },
              "language_preferences": {
                "document_language_preference": { "language_code": "EN" },
                "hearing_language_preference":  { "language_code": "CY" }
              }
            }
            """;

        // 3) Call API
        var call = mockMvc.perform(put("/defendant-accounts/20010/defendant-account-parties/20010").headers(headers)
            .contentType(MediaType.APPLICATION_JSON).content(body));

        String resp = call.andReturn().getResponse().getContentAsString();
        log.info("PUT DAP (org aliases upsert/trim) resp:\n{}", resp);

        // 4) Assert ETag bump + basic shape
        String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";
        call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

        // 5) Verify DB: only updated (200101) + new remain; 200102 must be gone; order by sequence_number
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT alias_id, sequence_number, organisation_name, surname, forenames "
                + "FROM aliases WHERE party_id = ? ORDER BY sequence_number ASC", 20010L);

        assertEquals(2, rows.size(), "Expected exactly 2 aliases after upsert+trim");

        // Row #1: updated 200101
        {
            Map<String, Object> r0 = rows.get(0);
            assertEquals(2, ((Number) r0.get("sequence_number")).intValue());
            assertEquals(200101L, ((Number) r0.get("alias_id")).longValue());
            assertEquals("PutCo Alias One (updated)", r0.get("organisation_name"));
            assertNull(r0.get("surname"));
            assertNull(r0.get("forenames"));
        }

        // Row #2: new alias (db-generated id, not 200102)
        {
            Map<String, Object> r1 = rows.get(1);
            assertEquals(3, ((Number) r1.get("sequence_number")).intValue());
            Long newId = ((Number) r1.get("alias_id")).longValue();
            assertNotNull(newId);
            assertNotEquals(200102L, newId, "Omitted alias 200102 must have been deleted");
            assertEquals("PutCo Alias Three (new)", r1.get("organisation_name"));
            assertNull(r1.get("surname"));
            assertNull(r1.get("forenames"));
        }

        // Safety: verify 200102 deleted
        Integer stillThere =
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM aliases WHERE party_id = ? AND alias_id = 200102",
                Integer.class, 20010L);

        assertEquals(0, stillThere, "Alias 200102 should be trimmed");

        Integer updatedVersion =
            jdbcTemplate.queryForObject("SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 20010L);

        assertEquals(currentVersion + 1, updatedVersion);
    }

    @Test
    @Sql(scripts = "classpath:db/insertData/update_into_parties.sql", executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("OPAL: PUT Replace DAP – is_debtor = false -> clear debtor fields but do not delete row")
    void put_replace_dap_isDebtorFalse_clearsDebtorFieldsButKeepsRow() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // 1) ETag for If-Match
        Integer currentVersion =
            jdbcTemplate.queryForObject("SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 20010L);
        assertNotNull(currentVersion);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, etag);

        String body = """
                {
                "defendant_account_party_type": "Defendant",
                "is_debtor": false,
                "party_details": {
                "party_id": "20010",
                    "organisation_flag": true,
                    "organisation_details": {
                    "organisation_name": "PutCo Ltd",
                        "organisation_aliases": [
                    { "alias_id": 200101, "sequence_number": 2, "organisation_name": "PutCo Alias One (updated)" },
                    { "sequence_number": 3, "organisation_name": "PutCo Alias Three (new)" }
                      ]
                }
            },
                "address": { "address_line_1": "100 Put Street", "postcode": "PU1 1TT" },
                "contact_details": {},
                "vehicle_details": {},
                "employer_details": {},
                "language_preferences": {}
            }
            """;

        // 3) Call API
        ResultActions call = mockMvc.perform(
            put("/defendant-accounts/20010/defendant-account-parties/20010")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        String resp = call.andReturn().getResponse().getContentAsString();
        log.info("PUT DAP (is_debtor=false) resp:\n{}", ToJsonString.toPrettyJson(resp));

        // 4) Assert ETag bump + basic shape
        String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";
        call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"));

        // 5) Verify DB: debtor_detail row still exists but key fields cleared (vehicle, employer, language)
        Map<String, Object> row = jdbcTemplate.queryForMap(
            "SELECT party_id, vehicle_make, vehicle_registration, employer_name, document_language "
                + "FROM debtor_detail WHERE party_id = ?",
            20010L);

        assertEquals(20010L, ((Number) row.get("party_id")).longValue());
        assertNull(row.get("vehicle_make"), "vehicle_make should be cleared to null");
        assertNull(row.get("vehicle_registration"), "vehicle_registration should be cleared to null");
        assertNull(row.get("employer_name"), "employer_name should be cleared to null");
        assertNull(row.get("document_language"), "document_language should be cleared to null");

        // Version bump verification
        Integer updatedVersion =
            jdbcTemplate.queryForObject("SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 20010L);
        assertEquals(currentVersion + 1, updatedVersion);
    }

    @Test
    @Sql(scripts = "classpath:db/insertData/update_into_parties.sql", executionPhase = BEFORE_TEST_METHOD)
    @DisplayName("OPAL: PUT Replace DAP – is_debtor = true -> upsert debtor details (create/update)")
    void put_replace_dap_isDebtorTrue_upsertsDebtorDetails() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // 1) ETag for If-Match
        Integer currentVersion =
            jdbcTemplate.queryForObject("SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 20010L);
        assertNotNull(currentVersion);
        String etag = "\"" + currentVersion + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, etag);

        String body = """
                {
                "defendant_account_party_type": "Defendant",
                "is_debtor": true,
                "party_details": {
                "party_id": "20010",
                    "organisation_flag": true,
                    "organisation_details": {
                    "organisation_name": "PutCo Ltd",
                        "organisation_aliases": [
                    { "alias_id": 200101, "sequence_number": 2, "organisation_name": "PutCo Alias One (updated)" },
                    { "sequence_number": 3, "organisation_name": "PutCo Alias Three (new)" }
                      ]
                }
            },
                "address": { "address_line_1": "100 Put Street", "postcode": "PU1 1TT" },
                "contact_details": { "primary_email_address": "putco@example.com",
                "work_telephone_number": "0207000000" },
                "vehicle_details": { "vehicle_make_and_model": "VW Golf", "vehicle_registration": "PU70ABC" },
                "employer_details": {
                "employer_name": "Put Employer",
                    "employer_address": { "address_line_1": "1 Employer Way", "postcode": "EM1 1AA" }
            },
                "language_preferences": {
                "document_language_preference": { "language_code": "EN" },
                "hearing_language_preference":  { "language_code": "CY" }
            }
            }
            """;

        // 3) Call API
        var call = mockMvc.perform(put("/defendant-accounts/20010/defendant-account-parties/20010").headers(headers)
            .contentType(MediaType.APPLICATION_JSON).content(body));

        String resp = call.andReturn().getResponse().getContentAsString();
        log.info("PUT DAP (is_debtor=true) resp:\n{}", resp);

        // 4) Assert ETag bump + basic shape
        String expectedNextEtag = "\"" + (currentVersion + 1) + "\"";
        call.andExpect(status().isOk()).andExpect(header().string(HttpHeaders.ETAG, expectedNextEtag))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type")
                .value("Defendant"));

        // 5) Verify DB: debtor_detail row updated/inserted with vehicle & employer & document_language
        Map<String, Object> row = jdbcTemplate.queryForMap(
            "SELECT party_id, vehicle_make, vehicle_registration, "
                + "employer_name, employer_address_line_1, employee_reference, employer_telephone, "
                + "employer_email, document_language FROM debtor_detail WHERE party_id = ?",
            20010L);

        assertEquals(20010L, ((Number) row.get("party_id")).longValue());
        assertEquals("VW Golf", row.get("vehicle_make"));
        assertEquals("PU70ABC", row.get("vehicle_registration"));
        assertEquals("Put Employer", row.get("employer_name"));
        assertEquals("1 Employer Way", row.get("employer_address_line_1"));
        assertEquals("EN", row.get("document_language"));

        // Version bump verification
        Integer updatedVersion =
            jdbcTemplate.queryForObject("SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
                Integer.class, 20010L);
        assertEquals(currentVersion + 1, updatedVersion);
    }


}
