package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.htmlunit.util.MimeType.APPLICATION_JSON;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allFinesPermissionUser;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@ActiveProfiles({"integration", "opal"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.OpalDefendantsIntegrationTest01")
class OpalDefendantsSearchIntegrationTest extends AbstractIntegrationTest {

    private static final String DEFENDANTS_SEARCH_URL = "/defendant-accounts/search";
    private static final String DEFENDANTS_SEARCH_RESP_SCHEMA = SchemaPaths.DEFENDANT_ACCOUNT
        + "/postDefendantAccountsSearchResponse.json";

    @MockitoBean
    UserStateService userStateService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    JsonSchemaValidationService jsonSchemaValidationService;

    @MockitoBean
    private UserState userState;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @BeforeEach
    void setup() {
        Mockito.when(userState.anyBusinessUnitUserHasPermission(Mockito.any())).thenReturn(true);
        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.any())).thenReturn(userState);
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Search defendant accounts – POST with valid criteria (seed id=77)")
    void testPostDefendantAccountsSearch_Opal(boolean consolidated) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());
        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header(
            "authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content(searchCriteria2(consolidated)));
        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
        if (!consolidated) {
            actions
                .andExpect(jsonPath("$.defendant_accounts[0].has_collection_order").doesNotExist())
                .andExpect(jsonPath("$.defendant_accounts[0].account_version").doesNotExist())
                .andExpect(jsonPath("$.defendant_accounts[0].checks").doesNotExist());
        }
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Search defendant accounts – POST no matches (different BU)")
    void testPostDefendantAccountsSearch_Opal_NoResults(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                          },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_NoResults: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Search by exact name + BU = 1 match (seed id=77)")
    void testPostDefendantAccountsSearch_Opal_ByNameAndBU(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                         },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_ByNameAndBU: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Postcode match ignores spaces/hyphens (MA4 1AL vs MA41AL)")
    void testPostDefendantAccountsSearch_Opal_Postcode_IgnoresSpaces(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                                     }},
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_Postcode_IgnoresSpaces: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Account number 'starts with' (177*) — active flag respected in new search view")
    void testPostDefendantAccountsSearch_Opal_AccountNumberStartsWith(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
                            {
                                      "active_accounts_only": true,
                                      "business_unit_ids": [78],
                                      "reference_number": {
                                        "account_number": "177",
                                        "prosecutor_case_reference": null,
                                        "organisation": false
                                      },
                                      "defendant": null
                                    }},
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_AccountNumberStartsWith: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.defendant_accounts[*].defendant_account_id").value(containsInAnyOrder("77", "9077")))
            .andExpect(jsonPath("$.defendant_accounts[*].account_number").value(containsInAnyOrder("177A", "177B")))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: PCR exact (090A)")
    void testPostDefendantAccountsSearch_Opal_PcrExact(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": {
                        "account_number": null,
                        "prosecutor_case_reference": "090A",
                        "organisation": false
                      },
                      "defendant": null
                    },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_PcrExact: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: PCR no match -> 0 records")
    void testPostDefendantAccountsSearch_Opal_PcrNoMatch(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": {
                        "account_number": null,
                        "prosecutor_case_reference": "ZZZ999",
                        "organisation": false
                      },
                      "defendant": null
                    },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_PcrNoMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: NI starts-with (A111) -> 1 record")
    void testPostDefendantAccountsSearch_Opal_NiStartsWith(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                     },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_NiStartsWith: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Address line 1 starts-with (\"Lumber\") -> 1 record")
    void testPostDefendantAccountsSearch_Opal_AddressStartsWith(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                     },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_AddressStartsWith: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"))
            .andExpect(jsonPath("$.defendant_accounts[0].address_line_1").value("Lumber House"))
            .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("MA4 1AL"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: DOB exact (1980-02-03) -> 1 record")
    void testPostDefendantAccountsSearch_Opal_DobExact(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                      },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_DobExact: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_title").value("Ms"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_firstnames").value("Anna"))
            .andExpect(jsonPath("$.defendant_accounts[0].birth_date").value("1980-02-03"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Include aliases = true still returns match on main name (no alias in DB)")
    void testPostDefendantAccountsSearch_Opal_AliasFlag_UsesMainName(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                      },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_AliasFlag_UsesMainName: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Active accounts only = false → returns both active and inactive accounts (order-agnostic)")
    void testPostDefendantAccountsSearch_Opal_ActiveAccountsOnlyFalse(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                     },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_ActiveAccountsOnlyFalse: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.defendant_accounts[*].defendant_account_id").value(containsInAnyOrder("77", "9077")))
            .andExpect(jsonPath("$.defendant_accounts[*].account_number").value(containsInAnyOrder("177A", "177B")))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Account number request includes check letter -> still matches (strips check letter)")
    void testPostDefendantAccountsSearch_Opal_AccountNumber_WithCheckLetter(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": {
                        "account_number": "177A",
                        "prosecutor_case_reference": null,
                        "organisation": false
                      },
                      "defendant": null
                    },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_AccountNumber_WithCheckLetter: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: No defendant object in payload → party still resolved")
    void testPostDefendantAccountsSearch_Opal_NoDefendantObject_StillResolvesParty(boolean consolidation)
        throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": {
                        "account_number": "177A",
                        "prosecutor_case_reference": null,
                        "organisation": false
                      },
                      "defendant": null
                    },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_NoDefendantObject_StillResolvesParty: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].address_line_1").value("Lumber House"))
            .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("MA4 1AL"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Search without business_unit_ids → still returns results")
    void testPostDefendantAccountsSearch_Opal_WithoutBusinessUnitFilter(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [],
                      "reference_number": {
                        "account_number": "177A",
                        "prosecutor_case_reference": null,
                        "organisation": false
                      },
                      "defendant": null
                    },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_WithoutBusinessUnitFilter: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Personal party (Anna Graham) includes title, forenames, and surname")
    void testPostDefendantAccountsSearch_Opal_AnnaGraham_FullDetails(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                      },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_AnnaGraham_FullDetails: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_firstnames").value("Anna"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_title").value("Ms"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Organisation returns no personal fields (awaiting seeded org data)")
    void testPostDefendantAccountsSearch_Opal_OrganisationWithNoPersonalNames(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                      },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_OrganisationWithNoPersonalNames: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value(333))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("Sainsco"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_title").doesNotExist())
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_firstnames").doesNotExist())
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").doesNotExist());

    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Alias search fallback → matches on main name when no alias exists")
    void testPostDefendantAccountsSearch_Opal_AliasFallbackToMainName(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                      },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_AliasFallbackToMainName: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_firstnames").value("Anna"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Optional fields correctly mapped or excluded when null")
    void testPostDefendantAccountsSearch_Opal_OptionalFieldsPresentAndMissing(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                      },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_OptionalFieldsPresentAndMissing: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].unit_fine_value").doesNotExist())
            .andExpect(jsonPath("$.defendant_accounts[0].originator_name").doesNotExist())
            .andExpect(jsonPath("$.defendant_accounts[0].parent_guardian_surname").doesNotExist())
            .andExpect(jsonPath("$.defendant_accounts[0].parent_guardian_firstnames").doesNotExist());
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Alias fields are mapped when party personal details are null")
    void testPostDefendantAccountsSearch_Opal_AliasFieldsMapped(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": {
                        "account_number": "188A",
                        "prosecutor_case_reference": null,
                        "organisation": false
                      },
                      "defendant": null
                    },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_AliasFieldsMapped: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].alias_number").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].organisation_name").doesNotExist())
            .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].surname").value("AliasSurname"))
            .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].forenames").value("AliasForenames"));
    }

    // AC1: Multi-parameter search tests - ALL search parameters must match

    public void testPostDefendantAccountsSearch_Opal_BusinessUnitNullFallback(boolean consolidation) throws Exception {

        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        mockMvc.perform(post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(APPLICATION_JSON).content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [9999],
                      "reference_number": {
                        "account_number": "199A",
                        "prosecutor_case_reference": null,
                        "organisation": false
                      },
                      "defendant": null
                  },
                         "consolidation_search": %s
                    }""".formatted(consolidation)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_name").value(""))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("9999"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Fuzzy surname match when exact_match_surname = false")
    void testPostDefendantAccountsSearch_Opal_SurnamePartialMatch(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                          },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_SurnamePartialMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("OPAL: Match on alias when both alias and main name exist")
    void testPostDefendantAccountsSearch_Opal_MatchOnAlias_WhenMainPresent(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {
                          "active_accounts_only": true,
                          "business_unit_ids": [78],
                          "reference_number": null,
                          "defendant": {
                            "include_aliases": true,
                            "organisation": false,
                            "surname": "AliasSurname1",
                            "exact_match_surname": true,
                            "forenames": "AliasForenames1",
                            "exact_match_forenames": true,
                            "address_line_1": "Alias Street",
                            "postcode": "AL1 1AS",
                            "organisation_name": null,
                            "exact_match_organisation_name": null,
                            "birth_date": "1980-01-01",
                            "national_insurance_number": "XX999999X"
                         },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal_MatchOnAlias_WhenMainPresent: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].surname").value("AliasSurname1"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC1: Multi-parameter search - surname + postcode (both must match) [@PO-710]")
    void testPostDefendantAccountsSearch_AC1_SurnameAndPostcode(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Search with surname "Graham" AND postcode "MA4 1AL" - should match account 77
        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                     },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC1_SurnameAndPostcode: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC1: Multi-parameter search - surname + wrong postcode (no matches expected) [@PO-710]")
    void testPostDefendantAccountsSearch_AC1_SurnameAndWrongPostcode(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Search with surname "Graham" AND wrong postcode - should return 0 results
        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                     },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC1_SurnameAndWrongPostcode: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(0));
    }

    // AC2: Business unit filtering test

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC1: Multi-parameter search - forenames + surname + DOB + NI (all must match) [@PO-710]")
    void testPostDefendantAccountsSearch_AC1_CompletePersonalDetails(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                     },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC1_CompletePersonalDetails: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"));
    }

    // AC3a: Active accounts only filtering tests

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC1: Multi-parameter search - address + NI number (both must match) [@PO-710]")
    void testPostDefendantAccountsSearch_AC1_AddressAndNI(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Search with address line 1 starting "Lumber" AND NI starting "A111" - should match account 77
        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                     },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC1_AddressAndNI: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"));
    }

    // AC5a: Forenames match filtering tests

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC1: Multi-parameter search - wrong business unit excludes otherwise matching records [@PO-710]")
    void testPostDefendantAccountsSearch_AC1_WrongBusinessUnitExcludes(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Search with correct surname but wrong business unit - should return 0 results
        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                      },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC1_WrongBusinessUnitExcludes: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(0));
    }

    // AC9: Multi-parameter search tests for organisations - ALL search parameters must match

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC2: Only accounts within specified business units are returned [@PO-710]")
    void testPostDefendantAccountsSearch_AC2_BusinessUnitFiltering(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Should find accounts 77, 88, 901, 333 but filter to only return those in business unit 78
        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                     },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC2_BusinessUnitFiltering: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC3a: Active accounts only filtering - false includes both active and completed accounts [@PO-710]")
    void testPostDefendantAccountsSearch_AC3a_ActiveAccountsOnlyFalse(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Test AC3a: active_accounts_only = false should include both active and completed accounts
        ResultActions allAccountsActions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                     },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = allAccountsActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC3a_ActiveAccountsOnlyFalse: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        allAccountsActions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(3))
            .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '77')]").exists()).andExpect(
                jsonPath("$.defendant_accounts[?(@.defendant_account_id == '9077')].account_number").value("177B"))
            .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '444')]").exists()).andExpect(
                jsonPath("$.defendant_accounts[?(@.defendant_account_id == '444')].account_number").value("444C"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC5a: Fuzzy forenames match when exact_match_forenames = false [@PO-710]")
    void testPostDefendantAccountsSearch_AC5a_ForenamesPartialMatch(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                          },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC5a_ForenamesPartialMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_firstnames").value("Anna"))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC9: Company multi-parameter search - company name + address line 1 (both must match) [@PO-710]")
    void testPostDefendantAccountsSearch_AC9_CompanyNameAndAddress(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Search with company name "TechCorp Solutions Ltd" AND address "Business Park" - should match account 555
        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                          },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC9_CompanyNameAndAddress: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"))
            .andExpect(jsonPath("$.defendant_accounts[0].address_line_1").value("Business Park"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC9: Company multi-parameter search - company name + postcode (both must match) [@PO-710]")
    void testPostDefendantAccountsSearch_AC9_CompanyNameAndPostcode(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Search with company name "TechCorp Solutions Ltd" AND postcode "B15 3TG" - should match account 555
        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                         },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC9_CompanyNameAndPostcode: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"))
            .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("B15 3TG"));
    }

    // AC9a: Business unit filtering for company accounts

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC9: Company multi-parameter search - partial company name + address (both must match) [@PO-710]")
    void testPostDefendantAccountsSearch_AC9_CompanyPartialNameAndAddress(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Search with partial company name "TechCorp" AND address "Business Park" - should match account 555
        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                         },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC9_CompanyPartialNameAndAddress: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"));
    }

    // AC9b: Active accounts only filtering for company accounts

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC9: Company multi-parameter search - correct name + wrong address (no matches expected) [@PO-710]")
    void testPostDefendantAccountsSearch_AC9_CompanyNameAndWrongAddress(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Search with correct company name "TechCorp Solutions Ltd" BUT wrong address "Office Tower"
        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                         },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC9_CompanyNameAndWrongAddress: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(0));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC9: Company multi-parameter search - multiple address fields (all must match) [@PO-710]")
    void testPostDefendantAccountsSearch_AC9_CompanyMultipleAddressFields(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Search with company name AND multiple address fields - all must match
        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                         },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC9_CompanyMultipleAddressFields: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"))
            .andExpect(jsonPath("$.defendant_accounts[0].address_line_1").value("Business Park"))
            .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("B15 3TG"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC9a: Only company accounts within specified business units are returned [@PO-710]")
    void testPostDefendantAccountsSearch_AC9a_CompanyBusinessUnitFiltering(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Apply business unit filter to only BU 78 - should return only TechCorp Solutions Ltd
        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                         },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC9a_CompanyBusinessUnitFiltering: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC9b: Active accounts only filtering for company accounts - excludes completed accounts [@PO-710]")
    void testPostDefendantAccountsSearch_AC9b_CompanyActiveAccountsOnly(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // active_accounts_only = false should include both active and completed company accounts
        ResultActions allAccountsActions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                        },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String allBody = allAccountsActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC9b_CompanyActiveAccountsOnly): Response body:\n{}",
            ToJsonString.toPrettyJson(allBody));

        allAccountsActions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '555')]").exists())
            .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '777')]").exists());
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC9d: Where company name or alias starts with input [@PO-710]")
    void testPostDefendantAccountsSearch_AC9d_CompanyAliasExactMatch(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Search with partial alias "TC Global" - should match "TC Global Ltd" alias (starts with)
        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                         },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC9d_CompanyAliasExactMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("666"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Global Ltd"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC9di: Where company name or alias results exactly matches input [@PO-710]")
    void testPostDefendantAccountsSearch_AC9di_CompanyAliasPartialMatch(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Search with exact alias "TechCorp Ltd" - should match exactly
        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                         },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC9di_CompanyAliasPartialMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC9e: Company address partial match - Address Line 1 starts with input value [@PO-710]")
    void testPostDefendantAccountsSearch_AC9e_CompanyAddressPartialMatch(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Search with partial address "Business" - should match "Business Park"
        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                         },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC9e_CompanyAddressPartialMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].address_line_1").value("Business Park"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("AC9ei: Company postcode partial match - Postcode starts with input value [@PO-710]")
    void testPostDefendantAccountsSearch_AC9ei_CompanyPostcodePartialMatch(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Search with partial postcode "B15" - should match "B15 3TG"
        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
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
                        },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_AC9ei_CompanyPostcodePartialMatch: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
            .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("B15 3TG"));
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("PO-2241 / AC1a+AC1b: Search core '177'; 177A and 177B returned (active flag ignored)")
    void testPostDefendantAccountsSearch_PO2241_Core177_InactiveStillReturned(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // Case 1: active_accounts_only = true (ignored because account_number provided)
        ResultActions activeTrue = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {
                      "active_accounts_only": true,
                      "business_unit_ids": [78],
                      "reference_number": {
                        "account_number": "177",
                        "prosecutor_case_reference": null,
                        "organisation": false
                      },
                      "defendant": null
                   },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String bodyTrue = activeTrue.andReturn().getResponse().getContentAsString();
        log.info(":PO-2241 AC1a+AC1b (active_accounts_only=true) response:\n{}", ToJsonString.toPrettyJson(bodyTrue));

        activeTrue.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.defendant_accounts[?(@.account_number == '177A')]").exists())
            .andExpect(jsonPath("$.defendant_accounts[?(@.account_number == '177B')]").exists());

        // Case 2: active_accounts_only = false (also ignored; set should be identical)
        ResultActions activeFalse = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {
                      "active_accounts_only": false,
                      "business_unit_ids": [78],
                      "reference_number": {
                        "account_number": "177",
                        "prosecutor_case_reference": null,
                        "organisation": false
                      },
                      "defendant": null
                   },
                         "consolidation_search": %s
                    }""".formatted(consolidation)));

        String bodyFalse = activeFalse.andReturn().getResponse().getContentAsString();
        log.info(":PO-2241 AC1a+AC1b (active_accounts_only=false) response:\n{}", ToJsonString.toPrettyJson(bodyFalse));

        activeFalse.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.defendant_accounts[?(@.account_number == '177A')]").exists())
            .andExpect(jsonPath("$.defendant_accounts[?(@.account_number == '177B')]").exists());
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("PO-2298 / AC1+AC2: reference_number.organisation flag filters results")
    void testPostDefendantAccountsSearch_OrganisationFlagRespected1(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // === AC1: organisation = true → only organisation defendants (e.g. 333A) ===
        ResultActions orgTrue = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content(searchCriteria(true)));

        String bodyTrue = orgTrue.andReturn().getResponse().getContentAsString();
        log.info(":PO-2298 AC1 (organisation=true) response:\n{}", ToJsonString.toPrettyJson(bodyTrue));

        orgTrue.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // should only return organisation accounts (organisation=true)
            .andExpect(jsonPath("$.defendant_accounts[*].organisation").value(everyItem(is(true))))
            // organisation_name should be present for org=true
            .andExpect(jsonPath("$.defendant_accounts[*].organisation_name").value(everyItem(is(notNullValue()))));

        jsonSchemaValidationService.validateOrError(bodyTrue, DEFENDANTS_SEARCH_RESP_SCHEMA);
    }

    @ParameterizedTest(name = "consolidated={0}")
    @ValueSource(booleans = { false, true })
    @DisplayName("PO-2298 / AC1+AC2: reference_number.organisation flag filters results")
    void testPostDefendantAccountsSearch_OrganisationFlagRespected2(boolean consolidation) throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        // === AC2: organisation = false → only individual defendants (e.g. 177A, 177B) ===
        ResultActions orgFalse = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content(searchCriteria(false)));

        String bodyFalse = orgFalse.andReturn().getResponse().getContentAsString();
        log.info(":PO-2298 AC2 (organisation=false) response:\n{}", ToJsonString.toPrettyJson(bodyFalse));

        orgFalse.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // should only return individual accounts (organisation=false)
            .andExpect(jsonPath("$.defendant_accounts[*].organisation").value(everyItem(is(false))))
            // organisation_name must be null for individuals
            .andExpect(jsonPath("$.defendant_accounts[*].organisation_name").value(everyItem(is(nullValue()))));

        jsonSchemaValidationService.validateOrError(bodyFalse, DEFENDANTS_SEARCH_RESP_SCHEMA);
    }

    private String searchCriteria(boolean orgFlag) {
        return """
            {
              "active_accounts_only": false,
              "business_unit_ids": [78],
              "reference_number": {
                "account_number": "%s",
                "prosecutor_case_reference": null,
                "organisation": %s
              },
              "defendant": null
            }
            """.formatted(orgFlag ? "333" : "177", orgFlag);

    }

    private String searchCriteria2(boolean consolidation) {
        return """
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
              },
              "consolidation_search": %s
            }""".formatted(consolidation);

    }

    private String searchCriteria3(boolean consolidation) {
        return """
              {
              "active_accounts_only": true,
              "business_unit_ids": [77],
              "reference_number": null,
              "defendant": {
                "include_aliases": false,
                "organisation": false,
                "address_line_1": null,
                "postcode": null,
                "organisation_name": null,
                "exact_match_organisation_name": null,
                "surname": "Williams",
                "exact_match_surname": true,
                "forenames": "Sarah Louise",
                "exact_match_forenames": true,
                "birth_date": null,
                "national_insurance_number": null
              },
              "consolidation_search": %s
            }""".formatted(consolidation);

    }

    private String searchCriteriaByAccountNumber(String accNo, boolean consolidation) {
        return """
              {
              "active_accounts_only": false,
              "business_unit_ids": [78],
              "reference_number": {
                "account_number": %s
                ,
                "prosecutor_case_reference": null,
                "organisation": false
              },
              "defendant": null,
              "consolidation_search": %s
            }""".formatted("\"" + accNo + "\"", consolidation);

    }

    @Test
    @DisplayName("PO-2296: Consolidated search of defendant accounts")
    void testPostDefendantAccountsConsolidatedSearch() throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content(searchCriteria2(true)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"))
            .andExpect(jsonPath("$.defendant_accounts[0].has_collection_order").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].account_version").value(0))
            .andExpect(jsonPath("$.defendant_accounts[0].checks.errors[0].reference")
                .value("CON.ER.4"))
            .andExpect(jsonPath("$.defendant_accounts[0].checks.errors[0].message")
                .value("Account has days in default"));
    }

    @Test
    @DisplayName("PO-2966: AC3:"
        + "consolidation_search is true API should return new fields where account has no warnings/errors")
    void testPostDefendantAccountsConsolidatedSearch_noWarningsNoErrors() throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content(searchCriteria3(true)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value(
                "99000000000002"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("23456789A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("77"))
            .andExpect(jsonPath("$.defendant_accounts[0].has_collection_order").value(false))
            .andExpect(jsonPath("$.defendant_accounts[0].checks.errors").isArray())
            .andExpect(jsonPath("$.defendant_accounts[0].checks.errors", hasSize(0)))
            .andExpect(jsonPath("$.defendant_accounts[0].checks.warnings").isArray())
            .andExpect(jsonPath("$.defendant_accounts[0].checks.warnings", hasSize(0)));
    }

    @Test
    @DisplayName("PO-2966: AC5:"
        + "consolidation_search is true API should return new fields where account has no errors but has warnings")
    void testPostDefendantAccountsConsolidatedSearch_hasWarningsNoErrors() throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content(
                    searchCriteriaByAccountNumber("1989", true)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value(
                "991199"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("1989"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"))
            .andExpect(jsonPath("$.defendant_accounts[0].has_collection_order").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].checks.errors").isArray())
            .andExpect(jsonPath("$.defendant_accounts[0].checks.errors", hasSize(0)))
            .andExpect(jsonPath("$.defendant_accounts[0].checks.warnings").isArray())
            .andExpect(jsonPath("$.defendant_accounts[0].checks.warnings", hasSize(1)));
    }

    @Test
    @DisplayName("PO-2966: AC6:"
        + "consolidation_search is true API should return new fields where account has warnings and errors")
    void testPostDefendantAccountsConsolidatedSearch_hasWarningsHasErrors() throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(
            post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON).content(
                    searchCriteriaByAccountNumber("1988", true)));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch_Opal: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value(
                "991198"))
            .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("1988"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"))
            .andExpect(jsonPath("$.defendant_accounts[0].has_collection_order").value(true))
            .andExpect(jsonPath("$.defendant_accounts[0].checks.errors").isArray())
            .andExpect(jsonPath("$.defendant_accounts[0].checks.errors", hasSize(1)))
            .andExpect(jsonPath("$.defendant_accounts[0].checks.warnings").isArray())
            .andExpect(jsonPath("$.defendant_accounts[0].checks.warnings", hasSize(1)));
    }
}
