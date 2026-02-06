package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allFinesPermissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

/**
 * Common tests for both Opal and Legacy modes, to ensure 100% compatibility.
 */

abstract class CommonDefendantsIntegrationTest01 extends AbstractIntegrationTest {

    static final String URL_BASE = "/defendant-accounts";
    static final String DEFENDANT_PAYMENT_TERMS_RESPONSE_SCHEMA = SchemaPaths.DEFENDANT_ACCOUNT
        + "/getDefendantAccountPaymentTermsResponse.json";
    static final String DEFENDANT_HEADER_SUMMARY_RESPONSE_SCHEMA = SchemaPaths.DEFENDANT_ACCOUNT
        + "/getDefendantAccountHeaderSummaryResponse.json";

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

    @BeforeEach
    void setupUserState() {
        Mockito.when(userState.anyBusinessUnitUserHasPermission(Mockito.any())).thenReturn(true);
        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.any())).thenReturn(userState);
    }

    @DisplayName("Get header summary for individual defendant account [@PO-2287]")
    void getHeaderSummary_Individual(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/77/header-summary").header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary_Individual: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value("77"))
            .andExpect(jsonPath("$.account_number").value("177A"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant")).andExpect(jsonPath("$.is_youth").value(false))
            .andExpect(jsonPath("$.fixed_penalty_ticket_number").value("888"))
            .andExpect(jsonPath("$.business_unit_summary.business_unit_id").value("78"))
            .andExpect(jsonPath("$.payment_state_summary.imposed_amount").value(700.58))
            .andExpect(jsonPath("$.payment_state_summary.paid_amount").value(200.00))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
            .andExpect(jsonPath("$.party_details.individual_details.forenames").value("Anna"))
            .andExpect(jsonPath("$.party_details.individual_details.surname").value("Graham"))
            .andExpect(jsonPath("$.party_details.organisation_details").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_HEADER_SUMMARY_RESPONSE_SCHEMA);
    }

    @DisplayName("Get header summary for organisation defendant account [@PO-2287]")
    void getHeaderSummary_Organisation(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/10001/header-summary").header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary_Organisation: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value("10001"))
            .andExpect(jsonPath("$.account_number").value("10001A"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant")).andExpect(jsonPath("$.is_youth").value(false))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"))
            .andExpect(jsonPath("$.party_details.individual_details").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_HEADER_SUMMARY_RESPONSE_SCHEMA);
    }

    @DisplayName("PO-2297: header-summary (individual) returns correct defendant_party_id from "
        + "defendantAccountPartyId bug fix validation")
    void testGetHeaderSummary_Individual_UsesDefendantAccountPartyId(Logger log) throws Exception {
        // Arrange
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Act
        ResultActions resultActions =
            mockMvc.perform(get("/defendant-accounts/77/header-summary").header("authorization", "Bearer some_value"));

        // Assert
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info("PO-2297 Individual header summary response:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party_id").value("77"))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
            .andExpect(jsonPath("$.party_details.individual_details.forenames").value("Anna"))
            .andExpect(jsonPath("$.party_details.individual_details.surname").value("Graham"));

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_HEADER_SUMMARY_RESPONSE_SCHEMA);
    }

    @DisplayName("PO-2297: header-summary (organisation) returns correct defendant_party_id from"
        + " defendantAccountPartyId — bug fix validation")
    void testGetHeaderSummary_Organisation_UsesDefendantAccountPartyId(Logger log) throws Exception {
        // Arrange
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Act
        ResultActions resultActions = mockMvc.perform(
            get("/defendant-accounts/10001/header-summary").header("authorization", "Bearer some_value"));

        // Assert
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info("PO-2297 Organisation header summary response:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party_id").value("10001"))
            .andExpect(jsonPath("$.party_details.party_id").value("10001"))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"));

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_HEADER_SUMMARY_RESPONSE_SCHEMA);
    }

    void testGetHeaderSummary_ThrowsNotFound(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(
            get("/defendant-accounts/999777/header-summary").header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary_ThrowsNotFound: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound());
    }

    void testGetPaymentTerms(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Make the 'date_last_amended' deterministic for acct 77
        jdbcTemplate.update(
            "UPDATE defendant_accounts SET last_changed_date = '2024-01-03 00:00:00' WHERE defendant_account_id = 77");

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/77/payment-terms/latest").header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetPaymentTerms: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.payment_terms.days_in_default").value(120))
            .andExpect(jsonPath("$.payment_terms.date_days_in_default_imposed").isEmpty())
            .andExpect(jsonPath("$.payment_terms.reason_for_extension").isEmpty())
            .andExpect(jsonPath("$.payment_terms.payment_terms_type.payment_terms_type_code").value("B"))
            .andExpect(jsonPath("$.payment_terms.effective_date").value("2025-10-12"))
            .andExpect(jsonPath("$.payment_terms.instalment_period.instalment_period_code").value("W"))
            .andExpect(jsonPath("$.payment_terms.lump_sum_amount").isEmpty())
            .andExpect(jsonPath("$.payment_terms.instalment_amount").isEmpty())

            .andExpect(jsonPath("$.payment_terms.posted_details.posted_date").value("2023-11-03T16:05:10"))
            .andExpect(jsonPath("$.payment_terms.posted_details.posted_by").value("01000000A"))
            .andExpect(jsonPath("$.payment_terms.posted_details.posted_by_name").isEmpty())

            .andExpect(jsonPath("$.payment_card_last_requested").value("2024-01-01"))
            .andExpect(jsonPath("$.payment_terms.extension").value(false))
            .andExpect(jsonPath("$.last_enforcement").value("10"));

        jsonSchemaValidationService.validateOrError(body, DEFENDANT_PAYMENT_TERMS_RESPONSE_SCHEMA);
    }

    void testGetPaymentTermsLatest_NoPaymentTermFoundForId(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/79/payment-terms/latest").header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetPaymentTerms: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound()) // 404 HTTP status
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.title").value("Entity Not Found")).andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"));

    }

    void getDefendantAccountPaymentTerms_500Error(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/500/payment-terms/latest").header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }


    void getDefendantAccountAtAGlance_500Error(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/500/at-a-glance").header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @DisplayName("PO-2119 / Problem JSON contains retriable field")
    void testEntityNotFoundExceptionContainsRetriable(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions =
            mockMvc.perform(get(URL_BASE + "/12345/header-summary").header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testRetriableIncludedInProblemDetail: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.title").value("Entity Not Found")).andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"))
            .andExpect(jsonPath("$.retriable").value(false));

    }

    @DisplayName("PO-2119 / Problem JSON contains retriable field")
    void testWrongMediaTypeContainsRetriableField(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(
            post("/defendant-accounts/search").header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_ATOM_XML).content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [],
                      "reference_number": {
                        "account_number": "177A",
                        "prosecutor_case_reference": null,
                        "organisation": false
                      },
                      "defendant": null
                    }
                    """));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testRetriableIncludedInProblemDetail: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isUnsupportedMediaType())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/unsupported-media-type"))
            .andExpect(jsonPath("$.title").value("Unsupported Media Type")).andExpect(jsonPath("$.status").value(415))
            .andExpect(jsonPath("$.detail").value("The Content-Type is not supported. Please use application/json"))
            .andExpect(jsonPath("$.retriable").value(false));

    }

    @DisplayName("PO-2119 / Problem JSON contains retriable for invalid request body")
    void testInvalidBodyContainsRetriable(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        var resultActions = mockMvc.perform(
            post("/defendant-accounts/search").header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("{ invalid json"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info("Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @DisplayName("Get enforcement status for individual defendant account [@PO-1696]")
    void testGetEnforcementStatus(Logger log, boolean isLegacy) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/78/enforcement-status")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":getEnforcementStatus: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(header().string("ETag", "\"20\""))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(ignoreForLegacy(jsonPath("$.defendant_account_type").value("adult"), isLegacy))
            .andExpect(jsonPath("$.employer_flag").value(true))
            .andExpect(jsonPath("$.is_hmrc_check_eligible").value(false))
            .andExpect(jsonPath("$.enforcement_overview.days_in_default").value(101))
            .andExpect(jsonPath("$.enforcement_overview.collection_order.collection_order_flag").value(true))
            .andExpect(jsonPath("$.enforcement_overview.collection_order.collection_order_date").value("2024-02-18"))
            .andExpect(jsonPath("$.enforcement_overview.enforcement_court.court_id").value(780000000185L))
            .andExpect(jsonPath("$.enforcement_overview.enforcement_court.court_code").value(17))
            .andExpect(jsonPath("$.enforcement_overview.enforcement_court.court_name").value("CH17"))
            .andExpect(jsonPath("$.enforcement_override.enforcement_override_result.enforcement_override_result_id")
                .value("FWEC"))
            .andExpect(ignoreForLegacy(jsonPath("$.enforcement_override.enforcement_override_result"
                    + ".enforcement_override_result_name")
                .value("WITNESS EXPENSES - CENTRAL FUNDS"), isLegacy))
            .andExpect(jsonPath("$.enforcement_override.enforcer.enforcer_id").value(780000000021L))
            .andExpect(jsonPath("$.enforcement_override.enforcer.enforcer_name").value("North East Enforcement"))
            .andExpect(jsonPath("$.enforcement_override.lja.lja_id").value(240))
            .andExpect(jsonPath("$.enforcement_override.lja.lja_name").value("Tyne & Wear LJA"))
            .andExpect(jsonPath("$.last_enforcement_action.reason").value("Late Payment"))
            .andExpect(jsonPath("$.last_enforcement_action.warrant_number").value("Warrent007"))
            .andExpect(jsonPath("$.last_enforcement_action.date_added").value("2025-02-13T10:05:10"))
            .andExpect(jsonPath("$.last_enforcement_action.enforcement_action.result_id").value("MPSO"))
            .andExpect(jsonPath("$.last_enforcement_action.enforcement_action.result_title")
                .value("Money Payment Supervision Order"))
            .andExpect(jsonPath("$.last_enforcement_action.enforcer.enforcer_id").value("21"))
            .andExpect(jsonPath("$.last_enforcement_action.enforcer.enforcer_name").value("North East Enforcement"))
            .andExpect(jsonPath("$.last_enforcement_action.result_responses[0].parameter_name").value("reason"))
            .andExpect(jsonPath("$.last_enforcement_action.result_responses[0].response").value("Evasion of Prison"))
            .andExpect(ignoreForLegacy(jsonPath("$.last_enforcement_action.result_responses[1].parameter_name")
                    .value("supervisor"), isLegacy))
            .andExpect(ignoreForLegacy(jsonPath("$.last_enforcement_action.result_responses[1].response")
                .value("Mordred"), isLegacy))
            .andExpect(ignoreForLegacy(jsonPath("$.last_enforcement_action.result_responses[2].parameter_name")
                .value("prisondetention"), isLegacy))
            .andExpect(ignoreForLegacy(jsonPath("$.last_enforcement_action.result_responses[2].response")
                .doesNotExist(), isLegacy))
            .andExpect(jsonPath("$.account_status_reference.account_status_code").value("L"))
            .andExpect(jsonPath("$.account_status_reference.account_status_display_name").value("Live"))
            .andExpect(ignoreForLegacy(jsonPath("$.next_enforcement_action_data").value("All"), isLegacy));
    }

    @DisplayName("Get enforcement status - missing auth header returns 401")
    void testGetEnforcementStatus_missingAuthHeader_returns401(Logger log, boolean isLegacy) throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Missing token"))
            .when(userStateService).checkForAuthorisedUser(null);

        mockMvc.perform(get(URL_BASE + "/78/enforcement-status"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isUnauthorized());
    }

    @DisplayName("Get enforcement status - forbidden without permission")
    void testGetEnforcementStatus_forbidden(Logger log, boolean isLegacy) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);
        when(userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
            .thenReturn(false);

        mockMvc.perform(get(URL_BASE + "/78/enforcement-status").header("authorization", "Bearer some_value"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isForbidden());
    }

    @DisplayName("Get enforcement status - resource not found")
    void testGetEnforcementStatus_notFound(Logger log, boolean isLegacy) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultMatcher statusMatcher = isLegacy ? status().is5xxServerError() : status().isNotFound();

        mockMvc.perform(get(URL_BASE + "/999999/enforcement-status").header("authorization", "Bearer some_value"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(statusMatcher);
    }

    @DisplayName("Get enforcement status - timeout")
    void testGetEnforcementStatus_timeout(Logger log, boolean isLegacy) throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.REQUEST_TIMEOUT, "Timeout"));

        mockMvc.perform(get(URL_BASE + "/78/enforcement-status").header("authorization", "Bearer some_value"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isRequestTimeout());
    }

    @DisplayName("Get enforcement status - service unavailable")
    void testGetEnforcementStatus_serviceUnavailable(Logger log, boolean isLegacy) throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenThrow(new ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Gateway down"));

        mockMvc.perform(get(URL_BASE + "/78/enforcement-status")
                .header("authorization", "Bearer some_value"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isServiceUnavailable());
    }

    @DisplayName("Get enforcement status - server error")
    void testGetEnforcementStatus_serverError(Logger log, boolean isLegacy) throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Boom"));

        mockMvc.perform(get(URL_BASE + "/78/enforcement-status").header("authorization", "Bearer some_value"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isInternalServerError());
    }

    static ResultMatcher ignoreForLegacy(ResultMatcher matcher, boolean legacy) {
        return (legacy) ? (result) -> { } : matcher;
    }

}
