package uk.gov.hmcts.opal.controllers.defendant;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;

@DisplayName("Integration tests for /defendant-accounts/search")
@Slf4j
public class DefendantAccountSearchIntegrationTest {

    protected static final String URL_BASE = "/defendant-accounts";

    protected final String getDefendantAccountsSearchResponseSchemaLocation() {
        return SchemaPaths.DEFENDANT_ACCOUNT + "/postDefendantAccountsSearchResponse.json";
    }

    // ======================================================
    // LEGACY TESTS
    // ======================================================
    @Nested
    @DisplayName("Legacy Tests")
    @ActiveProfiles({"integration", "legacy"})
    @AutoConfigureMockMvc
    public class Legacy extends AbstractIntegrationTest {

        @Test
        @DisplayName("Search defendant accounts - POST with valid criteria [@PO-33, @PO-119]")
        void testPostDefendantAccountsSearch() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

        @Test
        @DisplayName("Search defendant accounts - No Accounts found [@PO-33, @PO-119]")
        void testPostDefendantAccountsSearch_WhenNoDefendantAccountsFound() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
    }

    // ======================================================
    // OPAL TESTS
    // ======================================================
    @Nested
    @DisplayName("Opal Tests")
    @ActiveProfiles({"integration", "opal"})
    @Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
    @Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
    @AutoConfigureMockMvc
    public class Opal extends AbstractIntegrationTest {

        @Test
        @DisplayName(" Search defendant accounts – POST with valid criteria (seed id=77)")
        public void testPostDefendantAccountsSearch_Opal() throws Exception {

            ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

        @Test
        @DisplayName("OPAL: Search defendant accounts – POST no matches (different BU)")
        public void testPostDefendantAccountsSearch_Opal_NoResults() throws Exception {

            ResultActions actions = mockMvc.perform(post("/defendant-accounts/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

        @Test
        @DisplayName("PO-2298 / AC1+AC2: reference_number.organisation flag filters results")
        public void testPostDefendantAccountsSearch_OrganisationFlagRespected() throws Exception {

            // === AC1: organisation = true → only organisation defendants (e.g. 333A) ===
            ResultActions orgTrue = mockMvc.perform(post("/defendant-accounts/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
                .andExpect(jsonPath("$.defendant_accounts[*].organisation").value(everyItem(is(true))))
                .andExpect(jsonPath("$.defendant_accounts[*].organisation_name")
                    .value(everyItem(is(org.hamcrest.Matchers.notNullValue()))));

            // === AC2: organisation = false → only individual defendants (e.g. 177A, 177B) ===
            ResultActions orgFalse = mockMvc.perform(post("/defendant-accounts/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
                .andExpect(jsonPath("$.defendant_accounts[*].organisation")
                    .value(everyItem(is(false))))
                .andExpect(jsonPath("$.defendant_accounts[*].organisation_name")
                    .value(everyItem(is(org.hamcrest.Matchers.nullValue()))));

            validateJsonSchema(bodyTrue, getDefendantAccountsSearchResponseSchemaLocation());
            validateJsonSchema(bodyFalse, getDefendantAccountsSearchResponseSchemaLocation());
        }

        @Test
        @DisplayName("PO-2241 / AC1a+AC1b: Search core '177'; 177A and 177B returned (active flag ignored)")
        public void testPostDefendantAccountsSearch_PO2241_Core177_InactiveStillReturned() throws Exception {

            // Case 1: active_accounts_only = true (ignored because account_number provided)
            ResultActions activeTrue = mockMvc.perform(post("/defendant-accounts/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            activeTrue.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.defendant_accounts[?(@.account_number == '177A')]").exists())
                .andExpect(jsonPath("$.defendant_accounts[?(@.account_number == '177B')]").exists());

            // Case 2: active_accounts_only = false (also ignored; set should be identical)
            ResultActions activeFalse = mockMvc.perform(post("/defendant-accounts/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            activeFalse.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.defendant_accounts[?(@.account_number == '177A')]").exists())
                .andExpect(jsonPath("$.defendant_accounts[?(@.account_number == '177B')]").exists());
        }

        @Test
        @DisplayName("AC9: Company multi-parameter search - multiple address fields (all must match) [@PO-710]")
        void testPostDefendantAccountsSearch_AC9_CompanyMultipleAddressFields() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
                .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"))
                .andExpect(jsonPath("$.defendant_accounts[0].address_line_1").value("Business Park"))
                .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("B15 3TG"));
        }

        @Test
        @DisplayName("AC9a: Only company accounts within specified business units are returned [@PO-710]")
        void testPostDefendantAccountsSearch_AC9a_CompanyBusinessUnitFiltering() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
                .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"))
                .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"));
        }


        @Test
        @DisplayName("AC9b: Active accounts only filtering for company accounts -excludes completed accounts [@PO-710]")
        void testPostDefendantAccountsSearch_AC9b_CompanyActiveAccountsOnly() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions(

                ))
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

            String body = actions.andReturn().getResponse().getContentAsString();

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '555')]").exists())
                .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '777')]").exists());
        }


        @Test
        @DisplayName("AC9d: Where company name or alias starts with input [@PO-710]")
        void testPostDefendantAccountsSearch_AC9d_CompanyAliasExactMatch() throws Exception {
            when(userStateService.checkForAuthorisedUser(anyString()))
                .thenReturn(new UserState.DeveloperUserState());

            // Search with partial alias "TC Global" - should match "TechCorp Global Ltd" alias (starts with)
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("666"))
                .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
                .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Global Ltd"));
        }

        @Test
        @DisplayName("AC9di: Where company name or alias results exactly matches input [@PO-710]")
        void testPostDefendantAccountsSearch_AC9di_CompanyAliasPartialMatch() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
                .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
                .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"));
        }

        @Test
        @DisplayName("AC9e: Company address partial match - Address Line 1 starts with input value [@PO-710]")
        void testPostDefendantAccountsSearch_AC9e_CompanyAddressPartialMatch() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
                .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
                .andExpect(jsonPath("$.defendant_accounts[0].address_line_1").value("Business Park"));
        }

        @Test
        @DisplayName("AC9ei: Company postcode partial match - Postcode starts with input value [@PO-710]")
        void testPostDefendantAccountsSearch_AC9ei_CompanyPostcodePartialMatch() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
                .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
                .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("B15 3TG"));
        }

        @Test
        @DisplayName("OPAL: Organisation returns no personal fields (awaiting seeded org data)")
        void testPostDefendantAccountsSearch_Opal_OrganisationWithNoPersonalNames() throws Exception {
            // Simulate authorised user with all permissions
            when(userStateService.checkForAuthorisedUser(anyString()))
                .thenReturn(new UserState.DeveloperUserState());

            // Perform search for an organisation defendant (no personal name fields expected)
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
            log.info("OPAL: Organisation with no personal names response:\n{}", ToJsonString.toPrettyJson(body));

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


        @Test
        @DisplayName("OPAL: Alias search fallback → matches on main name when no alias exists")
        void testPostDefendantAccountsSearch_Opal_AliasFallbackToMainName() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_firstnames").value("Anna"))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"));
        }

        @Test
        @DisplayName("OPAL: Optional fields correctly mapped or excluded when null")
        void testPostDefendantAccountsSearch_Opal_OptionalFieldsPresentAndMissing() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].unit_fine_value").doesNotExist())
                .andExpect(jsonPath("$.defendant_accounts[0].originator_name").doesNotExist())
                .andExpect(jsonPath("$.defendant_accounts[0].parent_guardian_surname").doesNotExist())
                .andExpect(jsonPath("$.defendant_accounts[0].parent_guardian_firstnames").doesNotExist());
        }

        @Test
        @DisplayName("OPAL: Alias fields are mapped when party personal details are null")
        void testPostDefendantAccountsSearch_Opal_AliasFieldsMapped() throws Exception {
            // Simulate authorized user
            when(userStateService.checkForAuthorisedUser(anyString()))
                .thenReturn(new UserState.DeveloperUserState());

            // Perform search for account with alias records, where personal details are null
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
            log.info("OPAL: Alias Fields Mapped Response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].alias_number").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].organisation_name").doesNotExist())
                .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].surname").value("AliasSurname"))
                .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].forenames").value("AliasForenames"));
        }


        @Test
        @DisplayName("OPAL: Account number 'starts with' (177*) — active flag respected in new search view")
        void testPostDefendantAccountsSearch_Opal_AccountNumberStartsWith() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.defendant_accounts[*].defendant_account_id")
                    .value(containsInAnyOrder("77", "9077")))
                .andExpect(jsonPath("$.defendant_accounts[*].account_number")
                    .value(containsInAnyOrder("177A", "177B")))
                .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
        }

        @Test
        @DisplayName("OPAL: Postcode match ignores spaces/hyphens (MA4 1AL vs MA41AL)")
        void testPostDefendantAccountsSearch_Opal_Postcode_IgnoresSpaces() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
            }
                """));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"));
        }

        @Test
        @DisplayName("OPAL: NI starts-with (A111) -> 1 record")
        void testPostDefendantAccountsSearch_Opal_NiStartsWith() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
                .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
                .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
        }


        @Test
        @DisplayName("OPAL: DOB exact (1980-02-03) -> 1 record")
        void testPostDefendantAccountsSearch_Opal_DobExact() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

        @Test
        @DisplayName("AC1: Multi-parameter search - surname + postcode (both must match) [@PO-710]")
        void testPostDefendantAccountsSearch_AC1_SurnameAndPostcode() throws Exception {
            // Search with surname "Graham" AND postcode "MA4 1AL" - should match account 77
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"));
        }

        @Test
        @DisplayName("PO-2119: Wrong media type returns 415 with retriable=false")
        void testWrongMediaTypeContainsRetriableField() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                post(URL_BASE + "/search")
                    .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
                    .contentType(MediaType.APPLICATION_ATOM_XML)
                    .content("{}")
            );

            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info("Wrong media type response:\n{}", ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.retriable").value(false));
        }

        @Test
        @DisplayName("PO-2119: Invalid body returns 400 with retriable=false")
        void testInvalidBodyContainsRetriable() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                post(URL_BASE + "/search")
                    .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ \"invalid\": \"json\" ")
            );


            String body = resultActions.andReturn().getResponse().getContentAsString();
            log.info("Invalid JSON response:\n{}", ToJsonString.toPrettyJson(body));

            resultActions.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.retriable").value(false));
        }

        @Test
        @DisplayName("OPAL: Search by exact name + BU = 1 match (seed id=77)")
        void testPostDefendantAccountsSearch_Opal_ByNameAndBU() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
                """));

            String body = actions.andReturn().getResponse().getContentAsString();
            log.info("Opal search by name + BU:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
                .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
                .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));

            validateJsonSchema(body, getDefendantAccountsSearchResponseSchemaLocation());
        }

        @Test
        @DisplayName("OPAL: PCR no match → 0 records")
        void testPostDefendantAccountsSearch_Opal_PcrNoMatch() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
            log.info("OPAL: PCR no match response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(0));
        }

        @Test
        @DisplayName("OPAL: Address line 1 starts-with (\"Lumber\") → 1 record")
        void testPostDefendantAccountsSearch_Opal_AddressStartsWith() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
            log.info("OPAL: Address starts-with response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
                .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
                .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"))
                .andExpect(jsonPath("$.defendant_accounts[0].address_line_1").value("Lumber House"))
                .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("MA4 1AL"));
        }

        @Test
        @DisplayName("OPAL: Search without business_unit_ids → still returns results")
        void testPostDefendantAccountsSearch_Opal_WithoutBusinessUnitFilter() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
            log.info("OPAL: Without BU filter response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
                .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
        }

        @Test
        @DisplayName("OPAL: Personal party (Anna Graham) includes title, forenames, and surname")
        void testPostDefendantAccountsSearch_Opal_AnnaGraham_FullDetails() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_firstnames").value("Anna"))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_title").value("Ms"));
        }

        @Test
        @DisplayName("OPAL: Business unit null fallback — business_unit_name blank when missing")
        void testPostDefendantAccountsSearch_Opal_BusinessUnitNullFallback() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
                .contentType(MediaType.APPLICATION_JSON)
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
                """));

            String body = actions.andReturn().getResponse().getContentAsString();
            log.info("OPAL: Business unit null fallback response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].business_unit_name").value(""))
                .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("9999"));
        }

        @Test
        @DisplayName("OPAL: Fuzzy surname match when exact_match_surname = false")
        void testPostDefendantAccountsSearch_Opal_SurnamePartialMatch() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"));
        }

        @Test
        @DisplayName("OPAL: Match on alias when both alias and main name exist")
        void testPostDefendantAccountsSearch_Opal_MatchOnAlias_WhenMainPresent() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].aliases[0].surname").value("AliasSurname"));
        }

        @Test
        @DisplayName("OPAL: Active accounts only = false → returns both active and inactive accounts (order-agnostic)")
        void testPostDefendantAccountsSearch_Opal_ActiveAccountsOnlyFalse() throws Exception {
            // Simulate authorized user
            when(userStateService.checkForAuthorisedUser(anyString()))
                .thenReturn(new UserState.DeveloperUserState());

            // Perform search where active flag is false (should return both active and inactive accounts)
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
            log.info("OPAL: Active accounts only = false response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.defendant_accounts[*].defendant_account_id")
                    .value(containsInAnyOrder("77", "9077")))
                .andExpect(jsonPath("$.defendant_accounts[*].account_number")
                    .value(containsInAnyOrder("177A", "177B")))
                .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
        }

        @Test
        @DisplayName("OPAL: Include aliases = true still returns match on main name (no alias in DB)")
        void testPostDefendantAccountsSearch_Opal_AliasFlag_UsesMainName() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
            log.info("OPAL: AliasFlag uses main name response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
                .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"));

            validateJsonSchema(body, getDefendantAccountsSearchResponseSchemaLocation());
        }

        @Test
        @DisplayName("OPAL: Account number request includes check letter -> still matches (strips check letter)")
        void testPostDefendantAccountsSearch_Opal_AccountNumber_WithCheckLetter() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
            log.info("OPAL: AccountNumber with check letter response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
                .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
                .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));

            validateJsonSchema(body, getDefendantAccountsSearchResponseSchemaLocation());
        }

        @Test
        @DisplayName("OPAL: No defendant object in payload → party still resolved")
        void testPostDefendantAccountsSearch_Opal_NoDefendantObject_StillResolvesParty() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
            log.info("OPAL: No defendant object still resolves party response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
                .andExpect(jsonPath("$.defendant_accounts[0].address_line_1").value("Lumber House"))
                .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("MA4 1AL"));

            validateJsonSchema(body, getDefendantAccountsSearchResponseSchemaLocation());
        }

        @Test
        @DisplayName("AC1: Multi-parameter search - surname + wrong postcode (no matches expected) [@PO-710]")
        void testPostDefendantAccountsSearch_AC1_SurnameAndWrongPostcode() throws Exception {
            // Search with surname "Graham" AND wrong postcode - should return 0 results
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(0));
        }


        @Test
        @DisplayName("AC1: Multi-parameter search - forenames + surname + DOB + NI (all must match) [@PO-710]")
        void testPostDefendantAccountsSearch_AC1_CompletePersonalDetails() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"));
        }

        @Test
        @DisplayName("AC1: Multi-parameter search - address + NI number (both must match) [@PO-710]")
        void testPostDefendantAccountsSearch_AC1_AddressAndNI() throws Exception {
            // Search with address line 1 starting "Lumber" AND NI starting "A111" - should match account 77
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"));
        }

        @Test
        @DisplayName("AC1: Multi-parameter search - wrong business unit excludes otherwise matching records [@PO-710]")
        void testPostDefendantAccountsSearch_AC1_WrongBusinessUnitExcludes() throws Exception {
            // Search with correct surname but wrong business unit - should return 0 results
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(0));
        }


        @Test
        @DisplayName("AC2: Only accounts within specified business units are returned [@PO-710]")
        void testPostDefendantAccountsSearch_AC2_BusinessUnitFiltering() throws Exception {
            // Should find accounts 77, 88, 901, 333 but filter to only return those in business unit 78
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
                .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"));
        }

        @Test
        @DisplayName("AC3a:Active accounts only filtering- false includes both active and completed accounts "
            + "[@PO-710]")
        void testPostDefendantAccountsSearch_AC3a_ActiveAccountsOnlyFalse() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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

            String body = actions.andReturn().getResponse().getContentAsString();
            log.info("AC3a: Active accounts only = false response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '77')]").exists())
                .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '9077')].account_number")
                    .value("177B"))
                .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '444')]")
                    .exists())
                .andExpect(jsonPath("$.defendant_accounts[?(@.defendant_account_id == '444')].account_number")
                    .value("444C"));
        }

        @Test
        @DisplayName("AC5a: Fuzzy forenames match when exact_match_forenames = false [@PO-710]")
        void testPostDefendantAccountsSearch_AC5a_ForenamesPartialMatch() throws Exception {
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
            log.info("AC5a: Forenames partial match response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_firstnames").value("Anna"))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_surname").value("Graham"));
        }

        @Test
        @DisplayName("AC9: Company multi-parameter search - company name + address line 1 (both must match) [@PO-710]")
        void testPostDefendantAccountsSearch_AC9_CompanyNameAndAddress() throws Exception {
            // Search with company name "TechCorp Solutions Ltd" AND address "Business Park" - should match account 555
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
            log.info("AC9: Company name + address line 1 response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
                .andExpect(jsonPath("$.defendant_accounts[0].organisation").value(true))
                .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"))
                .andExpect(jsonPath("$.defendant_accounts[0].address_line_1").value("Business Park"));
        }

        @Test
        @DisplayName("AC9: Company multi-parameter search - company name + postcode (both must match) [@PO-710]")
        void testPostDefendantAccountsSearch_AC9_CompanyNameAndPostcode() throws Exception {
            // Search with company name "TechCorp Solutions Ltd" AND postcode "B15 3TG" - should match account 555
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
            log.info("AC9: Company name + postcode response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
                .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"))
                .andExpect(jsonPath("$.defendant_accounts[0].postcode").value("B15 3TG"));
        }

        @Test
        @DisplayName("AC9: Company multi-parameter search - partial company name + address (both must match) [@PO-710]")
        void testPostDefendantAccountsSearch_AC9_CompanyPartialNameAndAddress() throws Exception {
            // Search with partial company name "TechCorp" AND address "Business Park" - should match account 555
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
            log.info("AC9: Partial company name + address response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("555"))
                .andExpect(jsonPath("$.defendant_accounts[0].organisation_name").value("TechCorp Solutions Ltd"));
        }

        @Test
        @DisplayName("AC9: Company multi-parameter search - correct name + wrong address (no matches expected) "
            + "[@PO-710]")
        void testPostDefendantAccountsSearch_AC9_CompanyNameAndWrongAddress() throws Exception {
            // Search with correct company name "TechCorp Solutions Ltd" BUT wrong address "Office Tower"
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
            log.info("AC9: Company name + wrong address response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(0));
        }


        @Test
        @DisplayName("Search defendant accounts - No Accounts found [@PO-33, @PO-119]")
        void testPostDefendantAccountsSearch_WhenNoDefendantAccountsFound() throws Exception {
            // Simulate authorized user
            when(userStateService.checkForAuthorisedUser(anyString()))
                .thenReturn(new UserState.DeveloperUserState());

            // Perform search with non-matching surname and valid structure
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
                    }
                """));

            String body = actions.andReturn().getResponse().getContentAsString();
            log.info("No Accounts Found Response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(0));
        }

        @Test
        @DisplayName("OPAL: PCR exact (090A)")
        void testPostDefendantAccountsSearch_Opal_PcrExact() throws Exception {
            // Simulate authorized user with full Opal permissions
            when(userStateService.checkForAuthorisedUser(anyString()))
                .thenReturn(new UserState.DeveloperUserState());

            // Perform search using exact Prosecutor Case Reference (PCR)
            ResultActions actions = mockMvc.perform(post(URL_BASE + "/search")
                .header(HttpHeaders.AUTHORIZATION, getBearerTokenWithAllPermissions())
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
            log.info("OPAL: PCR Exact Response:\n{}", ToJsonString.toPrettyJson(body));

            actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.defendant_accounts[0].defendant_account_id").value("77"))
                .andExpect(jsonPath("$.defendant_accounts[0].account_number").value("177A"))
                .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
        }


    }
}

