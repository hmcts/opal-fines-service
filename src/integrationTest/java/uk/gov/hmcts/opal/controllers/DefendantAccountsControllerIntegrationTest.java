package uk.gov.hmcts.opal.controllers;

import static org.htmlunit.util.MimeType.APPLICATION_JSON;
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
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.UserStateService;

/**
 * Common tests for both Opal and Legacy modes, to ensure 100% compatibility.
 */
abstract class DefendantAccountsControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/defendant-accounts";

    abstract String getHeaderSummaryResponseSchemaLocation();

    abstract String getPaymentTermsResponseSchemaLocation();

    abstract String getAtAGlanceResponseSchemaLocation();

    @MockitoBean
    UserStateService userStateService;

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    // Suppressed until @MockBean is replaced with new approach (Spring Boot 3.3+)
    @SuppressWarnings("removal")
    @MockBean
    private UserState userState;

    @BeforeEach
    void setupUserState() {
        Mockito.when(userState.anyBusinessUnitUserHasPermission(Mockito.any()))
            .thenReturn(true);

        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.any()))
            .thenReturn(userState);
    }

    @DisplayName("Get header summary for defendant account [@PO-985]")
    void getHeaderSummaryImpl(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/77/header-summary")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.defendant_account_id").value(77))
            .andExpect(jsonPath("$.account_number").value("100A"))
            .andExpect(jsonPath("$.has_parent_guardian").value(true))
            .andExpect(jsonPath("$.fixed_penalty_ticket_number").value(888))
            .andExpect(jsonPath("$.business_unit_id").value(78))
            .andExpect(jsonPath("$.imposed").value(1000.0f))
            .andExpect(jsonPath("$.arrears").value(300.0d))
            .andExpect(jsonPath("$.organisation_name").value("Sainsco"))
            .andExpect(jsonPath("$.firstnames").value("Keith"))
            .andExpect(jsonPath("$.surname").value("Thief"));

        jsonSchemaValidationService.validateOrError(body, getHeaderSummaryResponseSchemaLocation());
    }

    @DisplayName("Get header summary for defendant account - 500 Error [@PO-985]")
    void getHeaderSummaryImpl_500Error(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/500/header-summary")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
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
        log.info(":testPostDefendantAccountsSearch_WhenNoDefendantAccountsFound: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_Opal_NoResults: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_Opal_ByNameAndBU: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_Opal_Postcode_IgnoresSpaces: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"));
    }

    @DisplayName("OPAL: Account number 'starts with' (177*)")
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
        log.info(":testPostDefendantAccountsSearch_Opal_AccountNumberStartsWith: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
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
        log.info(":testPostDefendantAccountsSearch_Opal_PcrExact: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_Opal_PcrNoMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_Opal_NiStartsWith: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_Opal_AddressStartsWith: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_Opal_AliasFlag_UsesMainName: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"));
    }

    @DisplayName("OPAL: Active accounts only = false -> returns active (and would return inactive if present)")
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
        log.info(":testPostDefendantAccountsSearch_Opal_ActiveAccountsOnlyFalse: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
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
        log.info(":testPostDefendantAccountsSearch_Opal_AccountNumber_WithCheckLetter: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_Opal_NoDefendantObject_StillResolvesParty: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_Opal_WithoutBusinessUnitFilter: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_Opal_AnnaGraham_FullDetails: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_Opal_OrganisationWithNoPersonalNames: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_Opal_AliasFallbackToMainName: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_Opal_OptionalFieldsPresentAndMissing: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_Opal_AliasFieldsMapped: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].alias_number").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].organisation_name").value("AliasOrg"))
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
        log.info(":testPostDefendantAccountsSearch_Opal_SurnamePartialMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_Opal_MatchOnAlias_WhenMainPresent: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
                }"""));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC1_SurnameAndPostcode: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
                }"""));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC1_SurnameAndWrongPostcode: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
                }"""));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC1_CompletePersonalDetails: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_AC1_AddressAndNI: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
                }"""));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC1_WrongBusinessUnitExcludes: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_AC2_BusinessUnitFiltering: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_AC3a_ActiveAccountsOnlyFalse: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        allAccountsActions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '77')]").exists())
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
        log.info(":testPostDefendantAccountsSearch_AC5a_ForenamesPartialMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_AC9_CompanyNameAndAddress: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_AC9_CompanyNameAndPostcode: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_AC9_CompanyPartialNameAndAddress: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_AC9_CompanyNameAndWrongAddress: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_AC9_CompanyMultipleAddressFields: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_AC9a_CompanyBusinessUnitFiltering: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_AC9b_CompanyActiveAccountsOnly): Response body:\n{}",
            ToJsonString.toPrettyJson(allBody));

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
        log.info(":testPostDefendantAccountsSearch_AC9d_CompanyAliasExactMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_AC9di_CompanyAliasPartialMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_AC9e_CompanyAddressPartialMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

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
        log.info(":testPostDefendantAccountsSearch_AC9ei_CompanyPostcodePartialMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("B15 3TG"));
    }

    void testGetPaymentTerms(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/77/payment-terms/latest")
                                                          .header("authorization", "Bearer some_value"));

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

            .andExpect(jsonPath("$.posted_details.posted_date").value("2023-11-03"))
            .andExpect(jsonPath("$.posted_details.posted_by").value("01000000A"))
            .andExpect(jsonPath("$.posted_details.posted_by_name").isEmpty())

            .andExpect(jsonPath("$.payment_card_last_requested").value("2024-01-01"))
            .andExpect(jsonPath("$.date_last_amended").value("2024-01-03"))
            .andExpect(jsonPath("$.extension").value(false))
            .andExpect(jsonPath("$.last_enforcement").value("REM"));

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
            .andExpect(jsonPath("$.defendant_account_party.address.address_line_1").value("Lumber House"));
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
        log.info(":testGetAtAGlance: Party is an individual. etag header: \n" + headers);
        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetAtAGlance: Party is an individual. Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // verify the header contains an ETag value
            .andExpect(header().string("etag", "\"1\""))
            .andExpect(jsonPath("$.defendant_account_id").value("77"))
            .andExpect(jsonPath("$.account_number").value("177A"))
            .andExpect(jsonPath("$.debtor_type").value("Defendant"))
            .andExpect(jsonPath("$.is_youth").exists())
            .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
            .andExpect(jsonPath("$.party_details.organisation_details").doesNotExist())
            .andExpect(jsonPath("$.party_details.individual_details.age").value("45"))
            .andExpect(jsonPath("$.address").exists())
            .andExpect(jsonPath("$.payment_terms").exists())
            .andExpect(jsonPath("$.enforcement_status").exists());;

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
            .andExpect(jsonPath("$.enforcement_status").exists());;

        jsonSchemaValidationService.validateOrError(body, getAtAGlanceResponseSchemaLocation());
    }

    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an organisation")
    public void opalGetAtAGlance_Organisation(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/10001/at-a-glance")
                                                          .header("authorization", "Bearer some_value"));

        String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
        log.info(":testGetAtAGlance: Party is an individual. etag header: \n" + headers);
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
            .andExpect(jsonPath("$.enforcement_status").exists());

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
        log.info(":testGetAtAGlance: Party is an individual. etag header: \n" + headers);
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
            .andExpect(jsonPath("$.language_preferences").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, getAtAGlanceResponseSchemaLocation());
    }

    @DisplayName("OPAL: Get Defendant Account At A Glance [@PO-1564] - Party is an organisation. "
        + "One language preference not set (as this is optional)")
    public void opalGetAtAGlance_Organisation_NoHearingLanguagePref(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/10003/at-a-glance")
                                                          .header("authorization", "Bearer some_value"));

        String headers = resultActions.andReturn().getResponse().getHeaders("etag").toString();
        log.info(":testGetAtAGlance: Party is an individual. etag header: \n" + headers);
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

    //
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
}
