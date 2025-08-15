package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

/**
 * Common tests for both Opal and Legacy modes, to ensure 100% compatibility.
 */
abstract class DefendantAccountsControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/defendant-accounts";

    private static final String GET_HEADER_SUMMARY_RESPONSE =
        SchemaPaths.DEFENDANT_ACCOUNT + "/getDefendantAccountHeaderSummaryResponse.json";

    @MockitoBean
    UserStateService userStateService;

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

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

        jsonSchemaValidationService.validateOrError(body, GET_HEADER_SUMMARY_RESPONSE);
    }

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


}
