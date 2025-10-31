package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import org.hamcrest.core.IsNull;
import static org.htmlunit.util.MimeType.APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mockito;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.server.ResponseStatusException;

import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

/**
 * Common tests for both Opal and Legacy modes, to ensure 100% compatibility.
 */

abstract class DefendantAccountsControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/defendant-accounts";

    @MockitoBean
    UserStateService userStateService;

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    // Suppressed until @MockBean is replaced with new approach (Spring Boot 3.3+)
    @SuppressWarnings("removal")
    @MockBean
    private UserState userState;

    @Autowired
    JdbcTemplate jdbcTemplate;

    final String getAtAGlanceResponseSchemaLocation() {
        return SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountAtAGlanceResponse.json";
    }

    final String getPaymentTermsResponseSchemaLocation() {
        return SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountPaymentTermsResponse.json";
    }

    final String getHeaderSummaryResponseSchemaLocation() {
        return SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountHeaderSummaryResponse.json";
    }

    final String getDefendantAccountPartyResponseSchemaLocation() {
        return SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountPartyResponse.json";
    }

    final String getDefendantAccountsSearchResponseSchemaLocation() {
        return SchemaPaths.DEFENDANT_ACCOUNT + "/postDefendantAccountsSearchResponse.json";
    }

    final String getFixedPenaltyResponseSchemaLocation() {
        return SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountFixedPenaltyResponse.json";
    }

    @BeforeEach
    void setupUserState() {
        Mockito.when(userState.anyBusinessUnitUserHasPermission(Mockito.any()))
            .thenReturn(true);

        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.any()))
            .thenReturn(userState);
    }

    @DisplayName("Get header summary for individual defendant account [@PO-2287]")
    void getHeaderSummary_Individual(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/77/header-summary")
                                                          .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary_Individual: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value("77"))
            .andExpect(jsonPath("$.account_number").value("177A"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant"))
            .andExpect(jsonPath("$.is_youth").value(false))
            .andExpect(jsonPath("$.fixed_penalty_ticket_number").value("888"))
            .andExpect(jsonPath("$.business_unit_summary.business_unit_id").value("78"))
            .andExpect(jsonPath("$.payment_state_summary.imposed_amount").value(700.58))
            .andExpect(jsonPath("$.payment_state_summary.paid_amount").value(200.00))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
            .andExpect(jsonPath("$.party_details.individual_details.forenames").value("Anna"))
            .andExpect(jsonPath("$.party_details.individual_details.surname").value("Graham"))
            .andExpect(jsonPath("$.party_details.organisation_details").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, getHeaderSummaryResponseSchemaLocation());
    }

    @DisplayName("Get header summary for organisation defendant account [@PO-2287]")
    void getHeaderSummary_Organisation(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/10001/header-summary")
                                                          .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary_Organisation: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value("10001"))
            .andExpect(jsonPath("$.account_number").value("10001A"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant"))
            .andExpect(jsonPath("$.is_youth").value(false))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"))
            .andExpect(jsonPath("$.party_details.individual_details").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, getHeaderSummaryResponseSchemaLocation());
    }

    @DisplayName("OPAL: Get header summary for non-existent ID returns 404")
    void getHeaderSummary_Opal_NotFound(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions ra = mockMvc.perform(
            get(URL_BASE + "/500/header-summary")
                .header("authorization", "Bearer some_value")
        );

        String body = ra.andReturn().getResponse().getContentAsString();
        log.info(":getHeaderSummary_Opal_NotFound: Response body:\n{}", ToJsonString.toPrettyJson(body));

        ra.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.status").value(404));
    }

    @DisplayName("LEGACY: Get header summary for non-existent ID returns 500")
    void getHeaderSummary_Legacy_500(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions ra = mockMvc.perform(
            get(URL_BASE + "/500/header-summary")
                .header("authorization", "Bearer some_value")
        );

        String body = ra.andReturn().getResponse().getContentAsString();
        log.info(":getHeaderSummary_Legacy_500: Response body:\n{}", ToJsonString.toPrettyJson(body));

        ra.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }


    @DisplayName("PO-2297: header-summary (individual) returns correct defendant_party_id from "
        + "defendantAccountPartyId bug fix validation")
    void testGetHeaderSummary_Individual_UsesDefendantAccountPartyId(Logger log) throws Exception {
        // Arrange
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Act
        ResultActions resultActions = mockMvc.perform(
            get("/defendant-accounts/77/header-summary")
                .header("authorization", "Bearer some_value")
        );

        // Assert
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info("PO-2297 Individual header summary response:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party_id").value("77"))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
            .andExpect(jsonPath("$.party_details.individual_details.forenames").value("Anna"))
            .andExpect(jsonPath("$.party_details.individual_details.surname").value("Graham"));

        jsonSchemaValidationService.validateOrError(body, getHeaderSummaryResponseSchemaLocation());
    }

    @DisplayName("PO-2297: header-summary (organisation) returns correct defendant_party_id from"
        + " defendantAccountPartyId — bug fix validation")
    void testGetHeaderSummary_Organisation_UsesDefendantAccountPartyId(Logger log) throws Exception {
        // Arrange
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Act
        ResultActions resultActions = mockMvc.perform(
            get("/defendant-accounts/10001/header-summary")
                .header("authorization", "Bearer some_value")
        );

        // Assert
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info("PO-2297 Organisation header summary response:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party_id").value("10001"))
            .andExpect(jsonPath("$.party_details.party_id").value("10001"))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Kings Arms"));

        jsonSchemaValidationService.validateOrError(body, getHeaderSummaryResponseSchemaLocation());
    }

    @DisplayName("Search defendant accounts - POST with valid criteria [@PO-33, @PO-119]")
    void testPostDefendantAccountsSearch(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                   "active_accounts_only": true,
                   "business_unit_ids": [101, 102, 78],
                   "reference_number": null,
                   "defendant": {
                       "include_aliases": true,
                       "organisation": false,
                       "address_line_1": null,
                       "postcode": "AB1 2CD",
                       "organisation_name": null,
                       "exact_match_organisation_name": null,
                       "surname": "Smith",
                       "exact_match_surname": true,
                       "forenames": "John",
                       "exact_match_forenames": false,
                       "birth_date": "1985-06-15",
                       "national_insurance_number": "QQ123456C"
                       }
                }"""));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("defendant_accounts[0].defendant_account_id").value("1"))
            .andExpect(jsonPath("defendant_accounts[0].account_number").value("100A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }

    @DisplayName("Search defendant accounts - No Accounts found [@PO-33, @PO-119]")
    void testPostDefendantAccountsSearch_WhenNoDefendantAccountsFound(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                   "active_accounts_only": true,
                   "business_unit_ids": [101],
                   "reference_number": null,
                   "defendant": {
                       "include_aliases": true,
                       "organisation": false,
                       "address_line_1": null,
                       "postcode": "AB1 2CD",
                       "organisation_name": null,
                       "exact_match_organisation_name": null,
                       "surname": "ShouldNotMatchAnythingXYZ",
                       "exact_match_surname": true,
                       "forenames": "John",
                       "exact_match_forenames": false,
                       "birth_date": "1985-06-15",
                       "national_insurance_number": "QQ123456C"
                       }
                }"""));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_WhenNoDefendantAccountsFound: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0));
    }

    @DisplayName("OPAL: Search defendant accounts – POST with valid criteria (seed id=77)")
    void testPostDefendantAccountsSearch_Opal(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": true,
                    "organisation": false,
                    "address_line_1": "Lumber",
                    "postcode": "MA4 1AL",
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "surname": "Graham",
                    "exact_match_surname": true,
                    "forenames": "Anna",
                    "exact_match_forenames": true,
                    "birth_date": "1980-02-03",
                    "national_insurance_number": "A11111A"
                  }
                }"""));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }

    @DisplayName("OPAL: Search defendant accounts – POST no matches (different BU)")
    void testPostDefendantAccountsSearch_Opal_NoResults(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [101],
                      "reference_number": null,
                      "defendant": {
                        "include_aliases": true,
                        "organisation": false,
                        "organisation_name": null,
                        "exact_match_organisation_name": null,
                        "address_line_1": "Lumber House",
                        "postcode": "MA4 1AL",
                        "surname": "Graham",
                        "exact_match_surname": true,
                        "forenames": "Anna",
                        "exact_match_forenames": true,
                        "birth_date": "1980-02-03",
                        "national_insurance_number": "A11111A"
                      }
                }"""));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_NoResults: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0));
    }

    @DisplayName("OPAL: Search by exact name + BU = 1 match (seed id=77)")
    void testPostDefendantAccountsSearch_Opal_ByNameAndBU(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                         {
                            "active_accounts_only": true,
                            "business_unit_ids": [78],
                            "reference_number": null,
                            "defendant": {
                              "include_aliases": true,
                              "organisation": false,
                              "organisation_name": null,
                              "exact_match_organisation_name": null,
                              "address_line_1": "Lumber House",
                                "postcode": "MA4 1AL",
                                "surname": "Graham",
                                "exact_match_surname": true,
                                "forenames": "Anna",
                                "exact_match_forenames": true,
                                "birth_date": "1980-02-03",
                                "national_insurance_number": "A11111A"
                              }
                            }
                }"""));

        String body = actions.andReturn().getResponse()
            .getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_ByNameAndBU: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }

    @DisplayName("OPAL: Postcode match ignores spaces/hyphens (MA4 1AL vs MA41AL)")
    void testPostDefendantAccountsSearch_Opal_Postcode_IgnoresSpaces(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                        {
                             "active_accounts_only": true,
                             "business_unit_ids": [78],
                             "reference_number": null,
                                "defendant": {
                                "include_aliases": true,
                                "organisation": false,
                                "address_line_1": "Lumber House",
                                "postcode": "MA41AL",
                                "organisation_name": null,
                                "exact_match_organisation_name": null,
                                "surname": "Graham",
                                "exact_match_surname": true,
                                "forenames": "Anna",
                                "exact_match_forenames": true,
                                "birth_date": "1980-02-03",
                                "national_insurance_number": "A11111A"
                                 }
                }"""));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_Postcode_IgnoresSpaces: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"));
    }

    @DisplayName("OPAL: Account number 'starts with' (177*) — active flag respected in new search view")
    void testPostDefendantAccountsSearch_Opal_AccountNumberStartsWith(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                        {
                                  "active_accounts_only": true,
                                  "business_unit_ids": [78],
                                  "reference_number": {
                                    "account_number": "177",
                                    "prosecutor_case_reference": null,
                                    "organisation": false
                                  },
                                  "defendant": null
                                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_AccountNumberStartsWith: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.defendant_accounts[*].defendant_account_id").value(containsInAnyOrder("77", "9077")))
            .andExpect(jsonPath("$.defendant_accounts[*].account_number")
                .value(containsInAnyOrder("177A", "177B")))
             .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }


    @DisplayName("OPAL: PCR exact (090A)")
    void testPostDefendantAccountsSearch_Opal_PcrExact(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": {
                    "account_number": null,
                    "prosecutor_case_reference": "090A",
                    "organisation": false
                  },
                  "defendant": null
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_PcrExact: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }


    @DisplayName("OPAL: PCR no match -> 0 records")
    void testPostDefendantAccountsSearch_Opal_PcrNoMatch(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": {
                    "account_number": null,
                    "prosecutor_case_reference": "ZZZ999",
                    "organisation": false
                  },
                  "defendant": null
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_PcrNoMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0));
    }

    @DisplayName("OPAL: NI starts-with (A111) -> 1 record")
    void testPostDefendantAccountsSearch_Opal_NiStartsWith(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": true,
                    "organisation": false,
                    "address_line_1": "Lumber House",
                    "postcode": "MA4 1AL",
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "surname": "Graham",
                    "exact_match_surname": true,
                    "forenames": "Anna",
                    "exact_match_forenames": true,
                    "birth_date": "1980-02-03",
                    "national_insurance_number": "A111"
                  }
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_NiStartsWith: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }

    @DisplayName("OPAL: Address line 1 starts-with (\"Lumber\") -> 1 record")
    void testPostDefendantAccountsSearch_Opal_AddressStartsWith(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": true,
                    "organisation": false,
                    "address_line_1": "Lumber",
                    "postcode": "MA4 1AL",
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "surname": "Graham",
                    "exact_match_surname": true,
                    "forenames": "Anna",
                    "exact_match_forenames": true,
                    "birth_date": "1980-02-03",
                    "national_insurance_number": "A11111A"
                  }
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_AddressStartsWith: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"))
            .andExpect(jsonPath("$.defendant_accounts[0].address_line_1").value("Lumber House"))
            .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("MA4 1AL"));
    }


    @DisplayName("OPAL: DOB exact (1980-02-03) -> 1 record")
    void testPostDefendantAccountsSearch_Opal_DobExact(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": true,
                    "organisation": false,
                    "address_line_1": "Lumber House",
                    "postcode": "MA4 1AL",
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "surname": "Graham",
                    "exact_match_surname": true,
                    "forenames": "Anna",
                    "exact_match_forenames": true,
                    "birth_date": "1980-02-03",
                    "national_insurance_number": "A11111A"
                  }
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_DobExact: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_title").value("Ms"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_firstnames").value("Anna"))
            .andExpect(jsonPath("$.defendant_accounts[0].birth_date").value("1980-02-03"));
    }

    @DisplayName("OPAL: Include aliases = true still returns match on main name (no alias in DB)")
    void testPostDefendantAccountsSearch_Opal_AliasFlag_UsesMainName(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": true,
                    "organisation": false,
                    "address_line_1": "Lumber House",
                    "postcode": "MA4 1AL",
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "surname": "Graham",
                    "exact_match_surname": true,
                    "forenames": "Anna",
                    "exact_match_forenames": true,
                    "birth_date": "1980-02-03",
                    "national_insurance_number": "A11111A"
                  }
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_AliasFlag_UsesMainName: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"));
    }

    @DisplayName("OPAL: Active accounts only = false → returns both active and inactive accounts (order-agnostic)")
    void testPostDefendantAccountsSearch_Opal_ActiveAccountsOnlyFalse(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": false,
                  "business_unit_ids": [78],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": true,
                    "organisation": false,
                    "address_line_1": "Lumber House",
                    "postcode": "MA4 1AL",
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "surname": "Graham",
                    "exact_match_surname": true,
                    "forenames": "Anna",
                    "exact_match_forenames": true,
                    "birth_date": "1980-02-03",
                    "national_insurance_number": "A11111A"
                  }
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_ActiveAccountsOnlyFalse: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.defendant_accounts[*].defendant_account_id").value(
                containsInAnyOrder("77", "9077")))
            .andExpect(jsonPath("$.defendant_accounts[*].account_number")
                .value(containsInAnyOrder("177A", "177B")))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }


    @DisplayName("OPAL: Account number request includes check letter -> still matches (strips check letter)")
    void testPostDefendantAccountsSearch_Opal_AccountNumber_WithCheckLetter(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": {
                    "account_number": "177A",
                    "prosecutor_case_reference": null,
                    "organisation": false
                  },
                  "defendant": null
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_AccountNumber_WithCheckLetter: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }

    @DisplayName("OPAL: No defendant object in payload → party still resolved")
    void testPostDefendantAccountsSearch_Opal_NoDefendantObject_StillResolvesParty(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": {
                    "account_number": "177A",
                    "prosecutor_case_reference": null,
                    "organisation": false
                  },
                  "defendant": null
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_NoDefendantObject_StillResolvesParty: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].address_line_1").value("Lumber House"))
            .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("MA4 1AL"));
    }

    @DisplayName("OPAL: Search without business_unit_ids → still returns results")
    void testPostDefendantAccountsSearch_Opal_WithoutBusinessUnitFilter(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
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

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_WithoutBusinessUnitFilter: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }

    @DisplayName("OPAL: Personal party (Anna Graham) includes title, forenames, and surname")
    void testPostDefendantAccountsSearch_Opal_AnnaGraham_FullDetails(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": false,
                    "organisation": false,
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "surname": "Graham",
                    "exact_match_surname": true,
                    "forenames": "Anna",
                    "exact_match_forenames": true,
                    "address_line_1": "Lumber House",
                    "postcode": "MA4 1AL",
                    "birth_date": "1980-02-03",
                    "national_insurance_number": "A11111A"
                  }
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_AnnaGraham_FullDetails: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_firstnames").value("Anna"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_title").value("Ms"));
    }

    @DisplayName("OPAL: Organisation returns no personal fields (awaiting seeded org data)")
    void testPostDefendantAccountsSearch_Opal_OrganisationWithNoPersonalNames(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": true,
                    "organisation": true,
                    "organisation_name": "Sainsco",
                    "exact_match_organisation_name": true,
                    "address_line_1": null,
                    "postcode": null,
                    "surname": null,
                    "exact_match_surname": null,
                    "forenames": null,
                    "exact_match_forenames": null,
                    "birth_date": null,
                    "national_insurance_number": null
                  }
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_OrganisationWithNoPersonalNames: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value(333))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("Sainsco"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_title").doesNotExist())
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_firstnames").doesNotExist())
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").doesNotExist());

    }

    @DisplayName("OPAL: Alias search fallback → matches on main name when no alias exists")
    void testPostDefendantAccountsSearch_Opal_AliasFallbackToMainName(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": true,
                    "organisation": false,
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "surname": "Graham",
                    "exact_match_surname": true,
                    "forenames": "Anna",
                    "exact_match_forenames": true,
                    "address_line_1": "Lumber House",
                    "postcode": "MA4 1AL",
                    "birth_date": "1980-02-03",
                    "national_insurance_number": "A11111A"
                  }
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_AliasFallbackToMainName: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_firstnames").value("Anna"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"));
    }

    @DisplayName("OPAL: Optional fields correctly mapped or excluded when null")
    void testPostDefendantAccountsSearch_Opal_OptionalFieldsPresentAndMissing(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": false,
                    "organisation": false,
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "surname": "Graham",
                    "exact_match_surname": true,
                    "forenames": "Anna",
                    "exact_match_forenames": true,
                    "address_line_1": "Lumber House",
                    "postcode": "MA4 1AL",
                    "birth_date": "1980-02-03",
                    "national_insurance_number": "A11111A"
                  }
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_OptionalFieldsPresentAndMissing: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].unit_fine_value").doesNotExist())
            .andExpect(jsonPath("$.defendant_accounts[0].originator_name").doesNotExist())
            .andExpect(jsonPath("$.defendant_accounts[0].parent_guardian_surname").doesNotExist())
            .andExpect(jsonPath("$.defendant_accounts[0].parent_guardian_firstnames").doesNotExist());
    }

    void testGetHeaderSummary_ThrowsNotFound(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get("/defendant-accounts/999777/header-summary")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary_ThrowsNotFound: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound());
    }

    @DisplayName("OPAL: Alias fields are mapped when party personal details are null")
    void testPostDefendantAccountsSearch_Opal_AliasFieldsMapped(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": {
                    "account_number": "188A",
                    "prosecutor_case_reference": null,
                    "organisation": false
                  },
                  "defendant": null
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_AliasFieldsMapped: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].alias_number").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].organisation_name").doesNotExist())
            .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].surname").value("AliasSurname"))
            .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].forenames").value("AliasForenames"));
    }

    public void testPostDefendantAccountsSearch_Opal_BusinessUnitNullFallback(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        mockMvc.perform(post("/defendant-accounts/search")
                .header("authorization", "Bearer some_value")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [9999],
                      "reference_number": {
                        "account_number": "199A",
                        "prosecutor_case_reference": null,
                        "organisation": false
                      },
                      "defendant": null
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_name").value(""))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("9999"));
    }

    @DisplayName("OPAL: Fuzzy surname match when exact_match_surname = false")
    void testPostDefendantAccountsSearch_Opal_SurnamePartialMatch(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                       "active_accounts_only": true,
                       "business_unit_ids": [78],
                       "reference_number": null,
                       "defendant": {
                         "include_aliases": false,
                         "organisation": false,
                         "organisation_name": null,
                         "exact_match_organisation_name": null,
                         "surname": "Grah",
                         "exact_match_surname": false,
                         "forenames": "Anna",
                         "exact_match_forenames": true,
                         "address_line_1": "Lumber House",
                         "postcode": "MA4 1AL",
                         "birth_date": "1980-02-03",
                         "national_insurance_number": "A11111A"
                       }
                     }
                
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_SurnamePartialMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"));
    }

    @DisplayName("OPAL: Match on alias when both alias and main name exist")
    void testPostDefendantAccountsSearch_Opal_MatchOnAlias_WhenMainPresent(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": null,
                      "defendant": {
                        "include_aliases": true,
                        "organisation": false,
                        "surname": "AliasSurname",
                        "exact_match_surname": true,
                        "forenames": "AliasForenames",
                        "exact_match_forenames": true,
                        "address_line_1": "Alias Street",
                        "postcode": "AL1 1AS",
                        "organisation_name": null,
                        "exact_match_organisation_name": null,
                        "birth_date": "1980-01-01",
                        "national_insurance_number": "XX999999X"
                      }
                    }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_Opal_MatchOnAlias_WhenMainPresent: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].surname").value("AliasSurname"));
    }

    // AC1: Multi-parameter search tests - ALL search parameters must match

    @DisplayName("AC1: Multi-parameter search - surname + postcode (both must match) [@PO-710]")
    void testPostDefendantAccountsSearch_AC1_SurnameAndPostcode(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Search with surname "Graham" AND postcode "MA4 1AL" - should match account 77
        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": false,
                    "organisation": false,
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "address_line_1": null,
                    "surname": "Graham",
                    "exact_match_surname": true,
                    "forenames": null,
                    "exact_match_forenames": null,
                    "birth_date": null,
                    "national_insurance_number": null,
                    "postcode": "MA4 1AL"
                  }
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC1_SurnameAndPostcode: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"));
    }

    @DisplayName("AC1: Multi-parameter search - surname + wrong postcode (no matches expected) [@PO-710]")
    void testPostDefendantAccountsSearch_AC1_SurnameAndWrongPostcode(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Search with surname "Graham" AND wrong postcode - should return 0 results
        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": false,
                    "organisation": false,
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "address_line_1": null,
                    "surname": "Graham",
                    "exact_match_surname": true,
                    "forenames": null,
                    "exact_match_forenames": null,
                    "birth_date": null,
                    "national_insurance_number": null,
                    "postcode": "XX99 9XX"
                  }
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC1_SurnameAndWrongPostcode: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(0));
    }

    @DisplayName("AC1: Multi-parameter search - forenames + surname + DOB + NI (all must match) [@PO-710]")
    void testPostDefendantAccountsSearch_AC1_CompletePersonalDetails(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": false,
                    "organisation": false,
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "address_line_1": null,
                    "postcode": null,
                    "surname": "Graham",
                    "exact_match_surname": true,
                    "forenames": "Anna",
                    "exact_match_forenames": true,
                    "birth_date": "1980-02-03",
                    "national_insurance_number": "A11111A"
                  }
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC1_CompletePersonalDetails: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"));
    }

    @DisplayName("AC1: Multi-parameter search - address + NI number (both must match) [@PO-710]")
    void testPostDefendantAccountsSearch_AC1_AddressAndNI(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Search with address line 1 starting "Lumber" AND NI starting "A111" - should match account 77
        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": false,
                    "organisation": false,
                    "address_line_1": "Lumber",
                    "postcode": "MA4 1AL",
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "surname": "Graham",
                    "exact_match_surname": true,
                    "forenames": "Anna",
                    "exact_match_forenames": true,
                    "birth_date": "1980-02-03",
                    "national_insurance_number": "A111"
                  }
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC1_AddressAndNI: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"));
    }

    @DisplayName("AC1: Multi-parameter search - wrong business unit excludes otherwise matching records [@PO-710]")
    void testPostDefendantAccountsSearch_AC1_WrongBusinessUnitExcludes(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Search with correct surname but wrong business unit - should return 0 results
        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [999],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": false,
                    "organisation": false,
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "address_line_1": null,
                    "postcode": null,
                    "surname": "Graham",
                    "exact_match_surname": true,
                    "forenames": null,
                    "exact_match_forenames": null,
                    "birth_date": null,
                    "national_insurance_number": null
                  }
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC1_WrongBusinessUnitExcludes: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(0));
    }

    // AC2: Business unit filtering test

    @DisplayName("AC2: Only accounts within specified business units are returned [@PO-710]")
    void testPostDefendantAccountsSearch_AC2_BusinessUnitFiltering(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Should find accounts 77, 88, 901, 333 but filter to only return those in business unit 78
        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": true,
                    "organisation": false,
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "surname": "Graham",
                    "exact_match_surname": true,
                    "forenames": null,
                    "exact_match_forenames": null,
                    "address_line_1": null,
                    "postcode": null,
                    "birth_date": null,
                    "national_insurance_number": null
                  }
                }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC2_BusinessUnitFiltering: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"));
    }

    // AC3a: Active accounts only filtering tests

    @DisplayName("AC3a: Active accounts only filtering - false includes both active and completed accounts [@PO-710]")
    void testPostDefendantAccountsSearch_AC3a_ActiveAccountsOnlyFalse(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Test AC3a: active_accounts_only = false should include both active and completed accounts
        ResultActions allAccountsActions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": false,
                  "business_unit_ids": [78],
                  "reference_number": null,
                  "defendant": {
                    "include_aliases": true,
                    "organisation": false,
                    "address_line_1": null,
                    "postcode": null,
                    "organisation_name": null,
                    "exact_match_organisation_name": null,
                    "surname": "Graham",
                    "exact_match_surname": true,
                    "forenames": null,
                    "exact_match_forenames": null,
                    "birth_date": null,
                    "national_insurance_number": null
                  }
                }
                """));

        String body = allAccountsActions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC3a_ActiveAccountsOnlyFalse: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        allAccountsActions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(3))
            .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '77')]").exists())
            .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '9077')].account_number")
                .value("177B"))
            .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '444')]").exists())
            .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '444')].account_number")
                .value("444C"));
    }

    // AC5a: Forenames match filtering tests

    @DisplayName("AC5a: Fuzzy forenames match when exact_match_forenames = false [@PO-710]")
    void testPostDefendantAccountsSearch_AC5a_ForenamesPartialMatch(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                       "active_accounts_only": true,
                       "business_unit_ids": [78],
                       "reference_number": null,
                       "defendant": {
                         "include_aliases": false,
                         "organisation": false,
                         "organisation_name": null,
                         "exact_match_organisation_name": null,
                         "surname": "Graham",
                         "exact_match_surname": true,
                         "forenames": "An",
                         "exact_match_forenames": false,
                         "address_line_1": null,
                         "postcode": null,
                         "birth_date": null,
                         "national_insurance_number": null
                       }
                     }
                
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC5a_ForenamesPartialMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_firstnames").value("Anna"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"));
    }

    // AC9: Multi-parameter search tests for organisations - ALL search parameters must match

    @DisplayName("AC9: Company multi-parameter search - company name + address line 1 (both must match) [@PO-710]")
    void testPostDefendantAccountsSearch_AC9_CompanyNameAndAddress(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Search with company name "TechCorp Solutions Ltd" AND address "Business Park" - should match account 555
        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": null,
                      "defendant": {
                        "include_aliases": false,
                        "organisation": true,
                        "organisation_name": "TechCorp Solutions Ltd",
                        "exact_match_organisation_name": true,
                        "address_line_1": "Business Park",
                        "postcode": null,
                        "surname": null,
                        "exact_match_surname": null,
                        "forenames": null,
                        "exact_match_forenames": null,
                        "birth_date": null,
                        "national_insurance_number": null
                      }
                    }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC9_CompanyNameAndAddress: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"))
            .andExpect(jsonPath("$.defendant_accounts[0].address_line_1").value("Business Park"));
    }

    @DisplayName("AC9: Company multi-parameter search - company name + postcode (both must match) [@PO-710]")
    void testPostDefendantAccountsSearch_AC9_CompanyNameAndPostcode(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Search with company name "TechCorp Solutions Ltd" AND postcode "B15 3TG" - should match account 555
        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": null,
                      "defendant": {
                        "include_aliases": false,
                        "organisation": true,
                        "organisation_name": "TechCorp Solutions Ltd",
                        "exact_match_organisation_name": true,
                        "address_line_1": null,
                        "postcode": "B15 3TG",
                        "surname": null,
                        "exact_match_surname": null,
                        "forenames": null,
                        "exact_match_forenames": null,
                        "birth_date": null,
                        "national_insurance_number": null
                      }
                    }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC9_CompanyNameAndPostcode: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"))
            .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("B15 3TG"));
    }

    @DisplayName("AC9: Company multi-parameter search - partial company name + address (both must match) [@PO-710]")
    void testPostDefendantAccountsSearch_AC9_CompanyPartialNameAndAddress(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Search with partial company name "TechCorp" AND address "Business Park" - should match account 555
        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": null,
                      "defendant": {
                        "include_aliases": false,
                        "organisation": true,
                        "organisation_name": "TechCorp",
                        "exact_match_organisation_name": false,
                        "address_line_1": "Business Park",
                        "postcode": null,
                        "surname": null,
                        "exact_match_surname": null,
                        "forenames": null,
                        "exact_match_forenames": null,
                        "birth_date": null,
                        "national_insurance_number": null
                      }
                    }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC9_CompanyPartialNameAndAddress: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"));
    }

    @DisplayName("AC9: Company multi-parameter search - correct name + wrong address (no matches expected) [@PO-710]")
    void testPostDefendantAccountsSearch_AC9_CompanyNameAndWrongAddress(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Search with correct company name "TechCorp Solutions Ltd" BUT wrong address "Office Tower"
        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": null,
                      "defendant": {
                        "include_aliases": false,
                        "organisation": true,
                        "organisation_name": "TechCorp Solutions Ltd",
                        "exact_match_organisation_name": true,
                        "address_line_1": "Office Tower",
                        "postcode": null,
                        "surname": null,
                        "exact_match_surname": null,
                        "forenames": null,
                        "exact_match_forenames": null,
                        "birth_date": null,
                        "national_insurance_number": null
                      }
                    }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC9_CompanyNameAndWrongAddress: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(0));
    }

    @DisplayName("AC9: Company multi-parameter search - multiple address fields (all must match) [@PO-710]")
    void testPostDefendantAccountsSearch_AC9_CompanyMultipleAddressFields(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Search with company name AND multiple address fields - all must match
        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": null,
                      "defendant": {
                        "include_aliases": false,
                        "organisation": true,
                        "organisation_name": "TechCorp Solutions Ltd",
                        "exact_match_organisation_name": true,
                        "address_line_1": "Business Park",
                        "postcode": "B15 3TG",
                        "surname": null,
                        "exact_match_surname": null,
                        "forenames": null,
                        "exact_match_forenames": null,
                        "birth_date": null,
                        "national_insurance_number": null
                      }
                    }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC9_CompanyMultipleAddressFields: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"))
            .andExpect(jsonPath("$.defendant_accounts[0].address_line_1").value("Business Park"))
            .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("B15 3TG"));
    }

    // AC9a: Business unit filtering for company accounts

    @DisplayName("AC9a: Only company accounts within specified business units are returned [@PO-710]")
    void testPostDefendantAccountsSearch_AC9a_CompanyBusinessUnitFiltering(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Apply business unit filter to only BU 78 - should return only TechCorp Solutions Ltd
        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": null,
                      "defendant": {
                        "include_aliases": false,
                        "organisation": true,
                        "organisation_name": "TechCorp",
                        "exact_match_organisation_name": false,
                        "address_line_1": null,
                        "postcode": null,
                        "surname": null,
                        "exact_match_surname": null,
                        "forenames": null,
                        "exact_match_forenames": null,
                        "birth_date": null,
                        "national_insurance_number": null
                      }
                    }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC9a_CompanyBusinessUnitFiltering: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"));
    }

    // AC9b: Active accounts only filtering for company accounts

    @DisplayName("AC9b: Active accounts only filtering for company accounts - excludes completed accounts [@PO-710]")
    void testPostDefendantAccountsSearch_AC9b_CompanyActiveAccountsOnly(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // active_accounts_only = false should include both active and completed company accounts
        ResultActions allAccountsActions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                   "active_accounts_only": false,
                   "business_unit_ids": [78],
                   "reference_number": null,
                     "defendant": {
                     "include_aliases": false,
                     "organisation": true,
                     "organisation_name": "TechCorp",
                     "exact_match_organisation_name": false,
                     "address_line_1": null,
                     "postcode": null,
                     "surname": null,
                       "exact_match_surname": null,
                       "forenames": null,
                       "exact_match_forenames": null,
                       "birth_date": null,
                       "national_insurance_number": null
                     }
                   }
                """));

        String allBody = allAccountsActions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC9b_CompanyActiveAccountsOnly): Response body:\n{}",
            ToJsonString.toPrettyJson(allBody)
        );

        allAccountsActions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '555')]").exists())
            .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '777')]").exists());
    }

    @DisplayName("AC9d: Where company name or alias starts with input [@PO-710]")
    void testPostDefendantAccountsSearch_AC9d_CompanyAliasExactMatch(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Search with partial alias "TC Global" - should match "TC Global Ltd" alias (starts with)
        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [9999],
                      "reference_number": null,
                      "defendant": {
                        "include_aliases": true,
                        "organisation": true,
                        "organisation_name": "TC Global",
                        "exact_match_organisation_name": false,
                        "address_line_1": null,
                        "postcode": null,
                        "surname": null,
                        "exact_match_surname": null,
                        "forenames": null,
                        "exact_match_forenames": null,
                        "birth_date": null,
                        "national_insurance_number": null
                      }
                    }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC9d_CompanyAliasExactMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("666"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Global Ltd"));
    }

    @DisplayName("AC9di: Where company name or alias results exactly matches input [@PO-710]")
    void testPostDefendantAccountsSearch_AC9di_CompanyAliasPartialMatch(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Search with exact alias "TechCorp Ltd" - should match exactly
        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": null,
                      "defendant": {
                        "include_aliases": true,
                        "organisation": true,
                        "organisation_name": "TechCorp Ltd",
                        "exact_match_organisation_name": true,
                        "address_line_1": null,
                        "postcode": null,
                        "surname": null,
                        "exact_match_surname": null,
                        "forenames": null,
                        "exact_match_forenames": null,
                        "birth_date": null,
                        "national_insurance_number": null
                      }
                    }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC9di_CompanyAliasPartialMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"));
    }

    @DisplayName("AC9e: Company address partial match - Address Line 1 starts with input value [@PO-710]")
    void testPostDefendantAccountsSearch_AC9e_CompanyAddressPartialMatch(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Search with partial address "Business" - should match "Business Park"
        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": null,
                      "defendant": {
                        "include_aliases": false,
                        "organisation": true,
                        "organisation_name": "TechCorp",
                        "exact_match_organisation_name": false,
                        "address_line_1": "Business",
                        "postcode": null,
                        "surname": null,
                        "exact_match_surname": null,
                        "forenames": null,
                        "exact_match_forenames": null,
                        "birth_date": null,
                        "national_insurance_number": null
                      }
                    }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC9e_CompanyAddressPartialMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].address_line_1").value("Business Park"));
    }

    @DisplayName("AC9ei: Company postcode partial match - Postcode starts with input value [@PO-710]")
    void testPostDefendantAccountsSearch_AC9ei_CompanyPostcodePartialMatch(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Search with partial postcode "B15" - should match "B15 3TG"
        ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": null,
                      "defendant": {
                        "include_aliases": false,
                        "organisation": true,
                        "organisation_name": "TechCorp",
                        "exact_match_organisation_name": false,
                        "address_line_1": null,
                        "postcode": "B15",
                        "surname": null,
                        "exact_match_surname": null,
                        "forenames": null,
                        "exact_match_forenames": null,
                        "birth_date": null,
                        "national_insurance_number": null
                      }
                    }
                """));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(
            ":testPostDefendantAccountsSearch_AC9ei_CompanyPostcodePartialMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body)
        );

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("B15 3TG"));
    }

    void testGetPaymentTerms(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Make the 'date_last_amended' deterministic for acct 77
        jdbcTemplate.update(
            "UPDATE defendant_accounts SET last_changed_date = '2024-01-03 00:00:00' WHERE defendant_account_id = 77"
        );

        ResultActions resultActions = mockMvc.perform(
            get(URL_BASE + "/77/payment-terms/latest")
                .header("authorization", "Bearer some_value")
        );

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetPaymentTerms: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.payment_terms.days_in_default").value(120))
            .andExpect(jsonPath("$.payment_terms.date_days_in_default_imposed").isEmpty())
            .andExpect(jsonPath("$.payment_terms.reason_for_extension").isEmpty())
            .andExpect(jsonPath("$.payment_terms.payment_terms_type.payment_terms_type_code").value("B"))
            .andExpect(jsonPath("$.payment_terms.effective_date").value("2025-10-12"))
            .andExpect(jsonPath("$.payment_terms.instalment_period.instalment_period_code").value("W"))
            .andExpect(jsonPath("$.payment_terms.lump_sum_amount").isEmpty())
            .andExpect(jsonPath("$.payment_terms.instalment_amount").isEmpty())

            .andExpect(jsonPath("$.payment_terms.posted_details.posted_date").value("2023-11-03"))
            .andExpect(jsonPath("$.payment_terms.posted_details.posted_by").value("01000000A"))
            .andExpect(jsonPath("$.payment_terms.posted_details.posted_by_name").isEmpty())

            .andExpect(jsonPath("$.payment_card_last_requested").value("2024-01-01"))
            .andExpect(jsonPath("$.payment_terms.extension").value(false))
            .andExpect(jsonPath("$.last_enforcement").value("10"));

        jsonSchemaValidationService.validateOrError(body, getPaymentTermsResponseSchemaLocation());
    }

    void testGetPaymentTermsLatest_NoPaymentTermFoundForId(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/79/payment-terms/latest")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetPaymentTerms: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound()) // 404 HTTP status
            .andExpect(jsonPath("$.type")
                .value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"));

    }

    void getDefendantAccountPaymentTerms_500Error(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/500/payment-terms/latest")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    void testLegacyGetPaymentTerms(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/77/payment-terms/latest")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetPaymentTerms: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))

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
            .andExpect(jsonPath("$.extension").value(false))
            .andExpect(jsonPath("$.last_enforcement").value("REM"));

    }


    @DisplayName("OPAL: Get Defendant Account Party - Happy Path [@PO-1588]")
    public void opalGetDefendantAccountParty_Happy(Logger log) throws Exception {
        ResultActions actions = mockMvc.perform(get("/defendant-accounts/77/defendant-account-parties/77")
            .header("Authorization", "Bearer test-token"));
        log.info("Opal happy path response:\n" + actions.andReturn().getResponse().getContentAsString());
        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"))
            .andExpect(jsonPath("$.defendant_account_party.is_debtor").value(true))
            .andExpect(jsonPath("$.defendant_account_party.party_details.party_id").value("77"))
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.surname").value("Graham"))
            .andExpect(jsonPath("$.defendant_account_party.address.address_line_1").value("Lumber House"))
            .andExpect(header().string("ETag", matchesPattern("\"\\d+\"")));
        String body = actions.andReturn().getResponse().getContentAsString();
        // Schema validation
        jsonSchemaValidationService.validateOrError(body, getDefendantAccountPartyResponseSchemaLocation());
    }

    @DisplayName("OPAL: Get Defendant Account Party - Organisation Only [@PO-1588]")
    public void opalGetDefendantAccountParty_Organisation(Logger log) throws Exception {
        ResultActions actions = mockMvc.perform(get("/defendant-accounts/555/defendant-account-parties/555")
            .header("Authorization", "Bearer test-token"));
        log.info("Organisation response:\n" + actions.andReturn().getResponse().getContentAsString());
        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.defendant_account_party.party_details.organisation_flag").value(true))
            .andExpect(jsonPath("$.defendant_account_party.party_details.organisation_details.organisation_name")
                .value("TechCorp Solutions Ltd"))
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details").doesNotExist());
    }

    @DisplayName("OPAL: Get Defendant Account Party - Null/Optional Fields [@PO-1588]")
    public void opalGetDefendantAccountParty_NullFields(Logger log) throws Exception {
        ResultActions actions = mockMvc.perform(get("/defendant-accounts/88/defendant-account-parties/88")
            .header("Authorization", "Bearer test-token"));
        log.info("Null fields response:\n" + actions.andReturn().getResponse().getContentAsString());
        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.surname").doesNotExist())
            .andExpect(jsonPath("$.defendant_account_party.address.address_line_1").doesNotExist());
    }


    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an individual")
    public void opalGetAtAGlance_Individual(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/77/at-a-glance")
            .header("authorization", "Bearer some_value"));

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
            .andExpect(jsonPath("$.enforcement_status.last_enforcement_action.last_enforcement_action_id").value("10"))
            .andExpect(jsonPath("$.enforcement_status.last_enforcement_action.last_enforcement_action_title").value(
                IsNull.nullValue()))
            .andExpect(jsonPath("$.comments_and_notes").exists());

        jsonSchemaValidationService.validateOrError(body, getAtAGlanceResponseSchemaLocation());
    }

    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an individual (Parent/Guardian)")
    public void opalGetAtAGlance_Individual_ParentGuardian(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/10004/at-a-glance")
            .header("authorization", "Bearer some_value"));

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
        ;

        jsonSchemaValidationService.validateOrError(body, getAtAGlanceResponseSchemaLocation());
    }

    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an organisation")
    public void opalGetAtAGlance_Organisation(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/10001/at-a-glance")
            .header("authorization", "Bearer some_value"));

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
        ;

        jsonSchemaValidationService.validateOrError(body, getAtAGlanceResponseSchemaLocation());
    }

    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an organisation. \n"
        + "No language preferences set (as these are optional) \n"
        + "No account comments or notes set (as these are optional)")
    public void opalGetAtAGlance_Organisation_NoLanguagePrefs(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/10002/at-a-glance")
            .header("authorization", "Bearer some_value"));

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

        jsonSchemaValidationService.validateOrError(body, getAtAGlanceResponseSchemaLocation());
    }

    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an organisation. "
        + "One language preference not set (as this is optional)")
    public void opalGetAtAGlance_Organisation_NoHearingLanguagePref(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/10003/at-a-glance")
            .header("authorization", "Bearer some_value"));

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

        jsonSchemaValidationService.validateOrError(body, getAtAGlanceResponseSchemaLocation());
    }

    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - 401 Unauthorized \n"
        + "when no auth header provided \n")
    void opalGetAtAGlance_missingAuthHeader_returns401(Logger log) throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(get(URL_BASE + "/10003/at-a-glance")
                .accept(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""));
    }

    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - 403 Forbidden\n"
        + "No auth header provided \n")
    void opalGetAtAGlance_authenticatedWithoutPermission_returns403(Logger log) throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Forbidden"))
            .when(userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(get(URL_BASE + "/10003/at-a-glance")
                .accept(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().string(""));
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - Happy Path [@PO-1565]")
    void opalUpdateDefendantAccount_Happy(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Read the current version to avoid optimistic locking conflicts
        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class, 77L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("some_value");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\""); // use actual version

        String requestJson = commentAndNotesPayload("hello");

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        String body = a.andReturn().getResponse().getContentAsString();
        String etag = a.andReturn().getResponse().getHeader("ETag");

        log.info(":opal_updateDefendantAccount_Happy body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":opal_updateDefendantAccount_Happy ETag: {}", etag);

        a.andExpect(status().isOk())
            .andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // ETag should be a quoted integer, e.g. "1"
        assertNotNull(etag, "ETag must be present");
        assertTrue(etag.matches("^\"\\d+\"$"), "ETag should be a quoted number");

        // Validate response JSON against schema
        jsonSchemaValidationService.validateOrError(body, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
    }


    @DisplayName("OPAL: PATCH Update Defendant Account - If-Match Mismatch [@PO-1565]")
    void patch_conflict_whenIfMatchDoesNotMatch(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"999\"");

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentAndNotesPayload("no change"))
        );

        String body = a.andReturn().getResponse().getContentAsString();
        log.info(":patch_conflict_whenIfMatchDoesNotMatch response body:\n{}", body);

        a.andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/optimistic-locking"))
            .andExpect(jsonPath("$.title").value("Conflict"))
            .andExpect(jsonPath("$.status").value(409));
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - Missing If-Match [@PO-1565]")
    void patch_conflict_whenIfMatchMissing(org.slf4j.Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        // Intentionally DO NOT add If-Match

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentAndNotesPayload("hello"))
        );

        String body = a.andReturn().getResponse().getContentAsString();
        log.info(":patch_conflict_whenIfMatchMissing body:\n{}", body);

        a.andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            // Depending on VersionUtils, this may be resource-conflict or optimistic-locking.
            .andExpect(jsonPath("$.type", org.hamcrest.Matchers.anyOf(
                is("https://hmcts.gov.uk/problems/resource-conflict"),
                is("https://hmcts.gov.uk/problems/optimistic-locking")
            )));
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - Forbidden when user lacks permission [@PO-1565]")
    void patch_forbidden_whenUserLacksAccountMaintenance(Logger log) throws Exception {
        // user without ACCOUNT_MAINTENANCE
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            UserState.builder()
                .userId(999L)
                .userName("no-perm-user")
                .businessUnitUser(java.util.Collections.emptySet())
                .build()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token_without_perm");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        String body = commentAndNotesPayload("hello");

        mockMvc.perform(patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - Not Found (account not in BU) [@PO-1565]")
    void patch_notFound_whenAccountNotInHeaderBU(Logger log) throws Exception {
        // User is authenticated and has perms, but BU header doesn't match the DA's BU → 404
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "99");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        String requestJson = commentAndNotesPayload("hello");

        var result = mockMvc.perform(
            patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        log.info(":patch_notFound_whenAccountNotInHeaderBU response:\n{}",
            result.andReturn().getResponse().getContentAsString());

        result.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
    }


    @DisplayName("OPAL: PATCH Update Defendant Account - Wrong Business Unit [@PO-1565]")
    void patch_badRequest_whenMultipleGroupsProvided(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        mockMvc.perform(patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                      {
                        "comment_and_notes":{"account_comment":"x"},
                        "collection_order":{"collection_order_flag":true}
                      }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/json-schema-validation"));
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - Schema violation (multiple groups) [@PO-1565]")
    void patch_badRequest_whenTypesInvalid(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        mockMvc.perform(patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                      {"comment_and_notes":{"free_text_note_1": 123}}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/json-schema-validation"));
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - Update Enforcement Court [@PO-1565]")
    void patch_updatesEnforcementCourt_andValidatesResponseSchema(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // 🔑 Get the current version number from DB for account 77
        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class,
            77L
        );

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
            patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        String resp = a.andReturn().getResponse().getContentAsString();
        log.info("enforcement_court update resp:\n{}", resp);

        a.andExpect(status().isOk())
            .andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(77))
            .andExpect(jsonPath("$.enforcement_court.court_id").value(100))
            .andExpect(jsonPath("$.enforcement_court.court_name").value("Central Magistrates"));

        jsonSchemaValidationService.validateOrError(resp, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
    }


    @DisplayName("OPAL: PATCH Update Defendant Account - Update Collection Order [@PO-1565]")
    void patch_updatesCollectionOrder(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Use the live version from DB to avoid 409 conflicts
        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class,
            77L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        mockMvc.perform(patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                      {"collection_order":{"collection_order_flag":true,"collection_order_date":"2025-01-01"}}
                    """))
            .andExpect(status().isOk())
            .andExpect(header().exists("ETag"));
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - Update Enforcement Override [@PO-1565]")
    void patch_updatesEnforcementOverride(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Use the live version from DB to avoid 409 conflicts
        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class,
            77L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String body = """
            {
              "enforcement_override": {
                "enforcement_override_result": {
                  "enforcement_override_result_id": "FWEC",
                  "enforcement_override_result_title": "Further Warrant Execution Cancelled"
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
            patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        );

        String resp = a.andReturn().getResponse().getContentAsString();
        log.info("enforcement_override update resp:\n{}", ToJsonString.toPrettyJson(resp));

        a.andExpect(status().isOk())
            .andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        jsonSchemaValidationService.validateOrError(resp, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
    }

    @DisplayName("OPAL: PATCH Update Defendant Account - ETag present & Response Schema OK [@PO-1565]")
    void patch_returnsETag_andResponseConformsToSchema(org.slf4j.Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Pull the current version from DB to satisfy optimistic locking
        Integer currentVersion = jdbcTemplate.queryForObject(
            "SELECT version_number FROM defendant_accounts WHERE defendant_account_id = ?",
            Integer.class,
            77L
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("good_token");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"" + currentVersion + "\"");

        String requestJson = commentAndNotesPayload("etag test");

        ResultActions result = mockMvc.perform(
            patch(URL_BASE + "/77")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        String body = result.andReturn().getResponse().getContentAsString();
        String etag = result.andReturn().getResponse().getHeader("ETag");

        log.info(":patch_returnsETag_andResponseConformsToSchema body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":patch_returnsETag_andResponseConformsToSchema ETag: {}", etag);

        result.andExpect(status().isOk())
            .andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        assertNotNull(etag, "ETag must be present");
        assertTrue(etag.matches("^\"\\d+\"$"), "ETag should be a quoted number");

        jsonSchemaValidationService.validateOrError(body, SchemaPaths.PATCH_UPDATE_DEFENDANT_ACCOUNT_RESPONSE);
    }

    // -- test helpers ------

    private static String commentAndNotesPayload(String accountComment) {
        return commentAndNotesPayload(accountComment, null, null, null);
    }

    private static String commentAndNotesPayload(String accountComment,
        String note1,
        String note2,
        String note3) {
        return """
            {
              "comment_and_notes": {
                "account_comment": %s,
                "free_text_note_1": %s,
                "free_text_note_2": %s,
                "free_text_note_3": %s
              }
            }
            """.formatted(
            jsonValue(accountComment),
            jsonValue(note1),
            jsonValue(note2),
            jsonValue(note3)
        );
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


    @DisplayName("PO-2241 / AC1a+AC1b: Search core '177'; 177A and 177B returned (active flag ignored)")
    void testPostDefendantAccountsSearch_PO2241_Core177_InactiveStillReturned(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // Case 1: active_accounts_only = true (ignored because account_number provided)
        ResultActions activeTrue = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": true,
                  "business_unit_ids": [78],
                  "reference_number": {
                    "account_number": "177",
                    "prosecutor_case_reference": null,
                    "organisation": false
                  },
                  "defendant": null
                }
                """));

        String bodyTrue = activeTrue.andReturn().getResponse().getContentAsString();
        log.info(":PO-2241 AC1a+AC1b (active_accounts_only=true) response:\n{}", ToJsonString.toPrettyJson(bodyTrue));

        activeTrue.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.defendant_accounts[?(@.account_number == '177A')]").exists())
            .andExpect(jsonPath("$.defendant_accounts[?(@.account_number == '177B')]").exists());

        // Case 2: active_accounts_only = false (also ignored; set should be identical)
        ResultActions activeFalse = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "active_accounts_only": false,
                  "business_unit_ids": [78],
                  "reference_number": {
                    "account_number": "177",
                    "prosecutor_case_reference": null,
                    "organisation": false
                  },
                  "defendant": null
                }
                """));

        String bodyFalse = activeFalse.andReturn().getResponse().getContentAsString();
        log.info(":PO-2241 AC1a+AC1b (active_accounts_only=false) response:\n{}", ToJsonString.toPrettyJson(bodyFalse));

        activeFalse.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.defendant_accounts[?(@.account_number == '177A')]").exists())
            .andExpect(jsonPath("$.defendant_accounts[?(@.account_number == '177B')]").exists());
    }

    @DisplayName("PO-2298 / AC1+AC2: reference_number.organisation flag filters results")
    void testPostDefendantAccountsSearch_OrganisationFlagRespected(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        // === AC1: organisation = true → only organisation defendants (e.g. 333A) ===
        ResultActions orgTrue = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
        {
          "active_accounts_only": false,
          "business_unit_ids": [78],
          "reference_number": {
            "account_number": "3",
            "prosecutor_case_reference": null,
            "organisation": true
          },
          "defendant": null
        }
            """));

        String bodyTrue = orgTrue.andReturn().getResponse().getContentAsString();
        log.info(":PO-2298 AC1 (organisation=true) response:\n{}", ToJsonString.toPrettyJson(bodyTrue));

        orgTrue.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // should only return organisation accounts (organisation=true)
            .andExpect(jsonPath("$.defendant_accounts[*].organisation").value(everyItem(is(true))))
            // organisation_name should be present for org=true
            .andExpect(jsonPath("$.defendant_accounts[*].organisation_name").value(everyItem(is(notNullValue()))));

        // === AC2: organisation = false → only individual defendants (e.g. 177A, 177B) ===
        ResultActions orgFalse = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
        {
          "active_accounts_only": false,
          "business_unit_ids": [78],
          "reference_number": {
            "account_number": "177",
            "prosecutor_case_reference": null,
            "organisation": false
          },
          "defendant": null
        }
            """));

        String bodyFalse = orgFalse.andReturn().getResponse().getContentAsString();
        log.info(":PO-2298 AC2 (organisation=false) response:\n{}", ToJsonString.toPrettyJson(bodyFalse));

        orgFalse.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // should only return individual accounts (organisation=false)
            .andExpect(jsonPath("$.defendant_accounts[*].organisation").value(everyItem(is(false))))
            // organisation_name must be null for individuals
            .andExpect(jsonPath("$.defendant_accounts[*].organisation_name").value(everyItem(is(nullValue()))));

        //  Schema validation for both AC1 and AC2
        jsonSchemaValidationService.validateOrError(bodyTrue, getDefendantAccountsSearchResponseSchemaLocation());
        jsonSchemaValidationService.validateOrError(bodyFalse, getDefendantAccountsSearchResponseSchemaLocation());
    }

    void getDefendantAccountAtAGlance_500Error(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/500/at-a-glance")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    void testLegacyGetDefendantAtAGlance(Logger log) throws Exception {

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

    @DisplayName("OPAL: Get Defendant Account Fixed Penalty [@PO-1819]")
    void testGetDefendantAccountFixedPenalty(Logger log) throws Exception {
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
        jsonSchemaValidationService.validateOrError(body, getFixedPenaltyResponseSchemaLocation());
    }

    @DisplayName("OPAL: Get Defendant Account Fixed Penalty - 404 when not found [@PO-1819]")
    void testGetDefendantAccountFixedPenalty_NotFound(Logger log) throws Exception {
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
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"));
    }



    @DisplayName("LEGACY: Get Defendant Account Party - Happy Path [@PO-1973]")
    public void legacyGetDefendantAccountParty_Happy(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(
            get(URL_BASE + "/77/defendant-account-parties/77")
                .header("authorization", "Bearer some_value")
        );

        String body = actions.andReturn().getResponse().getContentAsString();
        String etag = actions.andReturn().getResponse().getHeader("ETag");
        long version = objectMapper.readTree(body).path("version").asLong();

        log.info(":legacy_getDefendantAccountParty_Happy body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":legacy_getDefendantAccountParty_Happy ETag: {}", etag);

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_party.defendant_account_party_type").value("Defendant"))
            .andExpect(jsonPath("$.defendant_account_party.is_debtor").value(true))
            .andExpect(jsonPath("$.defendant_account_party.party_details.party_id").value("77"))
            .andExpect(jsonPath("$.defendant_account_party.party_details.individual_details.surname").value("Graham"))
            .andExpect(jsonPath("$.defendant_account_party.address.address_line_1").value("Lumber House"))
            // Validate that ETag header exists and is numeric (e.g. "0", "1", etc.)
            .andExpect(header().string("ETag", matchesPattern("\"\\d+\"")));

        // Schema validation
        jsonSchemaValidationService.validateOrError(body, getDefendantAccountPartyResponseSchemaLocation());
    }

    @DisplayName("LEGACY: Get Defendant Account Party - Organisation Only [@PO-1973]")
    void legacyGetDefendantAccountParty_Organisation(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(
            get(URL_BASE + "/555/defendant-account-parties/555")
                .header("authorization", "Bearer some_value")
        );

        String body = actions.andReturn().getResponse().getContentAsString();
        String etag = actions.andReturn().getResponse().getHeader("ETag");
        final Long version = objectMapper.readTree(body).path("version").asLong();

        log.info(":legacy_getDefendantAccountParty_Organisation body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":legacy_getDefendantAccountParty_Organisation ETag: {}", etag);

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
    void legacyGetDefendantAccountParty_500Error(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions actions = mockMvc.perform(
            get(URL_BASE + "/500/defendant-account-parties/500")
                .header("authorization", "Bearer some_value")
        );

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":legacy_getDefendantAccountParty_500Error body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(header().doesNotExist("ETag")); // no ETag on error payloads
    }

    @DisplayName("PO-2119 / Problem JSON contains retriable field")
    void testEntityNotFoundExceptionContainsRetriable(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/12345/header-summary")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testRetriableIncludedInProblemDetail: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"))
            .andExpect(jsonPath("$.title").value("Entity Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("The requested entity could not be found"))
            .andExpect(jsonPath("$.retriable").value(false));

    }

    @DisplayName("PO-2119 / Problem JSON contains retriable field")
    void testWrongMediaTypeContainsRetriableField(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(anyString()))
            .thenReturn(new UserState.DeveloperUserState());

        ResultActions resultActions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_ATOM_XML)
            .content("""
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
            .andExpect(jsonPath("$.type")
                .value("https://hmcts.gov.uk/problems/unsupported-media-type"))
            .andExpect(jsonPath("$.title").value("Unsupported Media Type"))
            .andExpect(jsonPath("$.status").value(415))
            .andExpect(jsonPath("$.detail")
                .value("The Content-Type is not supported. Please use application/json"))
            .andExpect(jsonPath("$.retriable").value(false));

    }

    @DisplayName("PO-2119 / Problem JSON contains retriable for invalid request body")
    void testInvalidBodyContainsRetriable(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        var resultActions = mockMvc.perform(post("/defendant-accounts/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{ invalid json"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info("Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @DisplayName("OPAL: Get Defendant Account At A Glance - "
         + "Verify aliases array organisation [@PO-2312]")
    void testGetAtAGlance_VerifyAliasesArray_Organisation(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/10001/at-a-glance")
            .header("authorization", "Bearer some_value"));

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

        jsonSchemaValidationService.validateOrError(body, getAtAGlanceResponseSchemaLocation());
    }

    @DisplayName("OPAL: Get Defendant Account At A Glance - "
         + "Verify aliases array individual [@PO-2312]")
    void testGetAtAGlance_VerifyAliasesArray_Individual(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/77/at-a-glance")
            .header("authorization", "Bearer some_value"));

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

        jsonSchemaValidationService.validateOrError(body, getAtAGlanceResponseSchemaLocation());
    }

}
