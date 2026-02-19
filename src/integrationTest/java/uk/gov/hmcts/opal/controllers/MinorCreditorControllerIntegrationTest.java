package uk.gov.hmcts.opal.controllers;

import java.util.List;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.REQUEST_TIMEOUT;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

/**
 * Common tests for both Opal and Legacy modes, to ensure 100% compatibility.
 */
abstract class MinorCreditorControllerIntegrationTest extends AbstractIntegrationTest {

    protected static final String URL_BASE = "/minor-creditor-accounts";

    private static final String MINOR_CREDITOR_RESPONSE =
        "opal/minor-creditor/postMinorCreditorAccountSearchResponse.json";

    private static final String MINOR_CREDITOR_HEADER_SUMMARY_RESPONSE =
        "opal/minor-creditor/getMinorCreditorAccountHeaderSummaryResponse.json";

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @MockitoBean
    UserStateService userStateService;

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    void postSearchMinorCreditorImpl_Success(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .activeAccountsOnly(false)
            .accountNumber("12345678A")
            .build();

        ResultActions resultActions = mockMvc.perform(post(URL_BASE + "/search")
                                                          .contentType(MediaType.APPLICATION_JSON)
                                                          .content(objectMapper.writeValueAsString(search))
                                                          .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testPostMinorCreditorSearch: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(2))

            // --- first creditor account ---
            .andExpect(jsonPath("$.creditor_accounts[0].creditor_account_id").value("104"))
            .andExpect(jsonPath("$.creditor_accounts[0].account_number").value("12345678A"))
            .andExpect(jsonPath("$.creditor_accounts[0].organisation").value(false))
            .andExpect(jsonPath("$.creditor_accounts[0].organisation_name").value("Acme Supplies Ltd"))
            .andExpect(jsonPath("$.creditor_accounts[0].firstnames").value(nullValue()))
            .andExpect(jsonPath("$.creditor_accounts[0].surname").value(nullValue()))
            .andExpect(jsonPath("$.creditor_accounts[0].address_line_1").value("Acme House"))
            .andExpect(jsonPath("$.creditor_accounts[0].postcode").value("MA4 1AL"))
            .andExpect(jsonPath("$.creditor_accounts[0].business_unit_name").value("Derbyshire"))
            .andExpect(jsonPath("$.creditor_accounts[0].business_unit_id").value("10"))
            .andExpect(jsonPath("$.creditor_accounts[0].account_balance").value(150.0))

            // defendant object (first account)
            .andExpect(jsonPath("$.creditor_accounts[0].defendant.defendant_account_id").value(nullValue()))
            .andExpect(jsonPath("$.creditor_accounts[0].defendant.organisation").value(false))
            .andExpect(jsonPath("$.creditor_accounts[0].defendant.organisation_name").value(nullValue()))
            .andExpect(jsonPath("$.creditor_accounts[0].defendant.firstnames").value(nullValue()))
            .andExpect(jsonPath("$.creditor_accounts[0].defendant.surname").value(nullValue()))

            // --- second creditor account ---
            .andExpect(jsonPath("$.creditor_accounts[1].creditor_account_id").value("105"))
            .andExpect(jsonPath("$.creditor_accounts[1].account_number").value("12345678"))
            .andExpect(jsonPath("$.creditor_accounts[1].organisation").value(false))
            .andExpect(jsonPath("$.creditor_accounts[1].organisation_name").value("Acme Supplies Ltd"))
            .andExpect(jsonPath("$.creditor_accounts[1].firstnames").value(nullValue()))
            .andExpect(jsonPath("$.creditor_accounts[1].surname").value(nullValue()))
            .andExpect(jsonPath("$.creditor_accounts[1].address_line_1").value("Acme House"))
            .andExpect(jsonPath("$.creditor_accounts[1].postcode").value("MA4 1AL"))
            .andExpect(jsonPath("$.creditor_accounts[1].business_unit_name").value("Derbyshire"))
            .andExpect(jsonPath("$.creditor_accounts[1].business_unit_id").value("10"))
            .andExpect(jsonPath("$.creditor_accounts[1].account_balance").value(0.0))

            // defendant object (second account)
            .andExpect(jsonPath("$.creditor_accounts[1].defendant.defendant_account_id").value(nullValue()))
            .andExpect(jsonPath("$.creditor_accounts[1].defendant.organisation").value(false))
            .andExpect(jsonPath("$.creditor_accounts[1].defendant.organisation_name").value(nullValue()))
            .andExpect(jsonPath("$.creditor_accounts[1].defendant.firstnames").value(nullValue()))
            .andExpect(jsonPath("$.creditor_accounts[1].defendant.surname").value(nullValue()));

        jsonSchemaValidationService.validate(body, MINOR_CREDITOR_RESPONSE);

    }

    void legacyPostSearchMinorCreditorImpl_500Error(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(101))
            .activeAccountsOnly(false)
            .accountNumber("FAIL")
            .build();

        ResultActions resultActions = mockMvc.perform(post(URL_BASE + "/search")
                                                          .contentType(MediaType.APPLICATION_JSON)
                                                          .content(
            objectMapper.writeValueAsString(search)).header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostMinorCreditorSearch: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(
            status().is5xxServerError()).andExpect(
                content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    void search_checkLetter_returnsBoth(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .activeAccountsOnly(false)
            .accountNumber("12345678A").build(); // 9-char input        .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.creditor_accounts[*].account_number")
                           .value(hasItems("12345678A", "12345678")));
    }

    void search_noCheckLetter_returnsBoth(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .activeAccountsOnly(false)
            .accountNumber("12345678").build(); // 8-digit input        .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.creditor_accounts[*].account_number")
                           .value(hasItems("12345678A", "12345678")));
    }

    void search_noResultsForUnknownBusinessUnit_returnsEmpty(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(999))
            .activeAccountsOnly(false)
            .build();

        ResultActions ra = mockMvc.perform(post(URL_BASE + "/search")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .content(objectMapper.writeValueAsString(search))
                                               .header("authorization", "Bearer some_value"));

        ra.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.creditor_accounts").doesNotExist());
    }

    void search_orgNamePrefix_normalizedMatches(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // "Acme Supplies Ltd" normalized; mixed case + spaces + punctuation
        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .creditor(uk.gov.hmcts.opal.dto.Creditor.builder()
                          .organisationName(" ac-me  SUPPLIES, ltd. ")
                          .build())
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.creditor_accounts[*].account_number")
                           .value(org.hamcrest.Matchers.hasItems("12345678A", "12345678")));
    }

    void search_accountNumber_withWildcardChars_treatedLiterally(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // Your helper escapes user input then appends %; verify no matches for literal wildcards
        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .accountNumber("1234567_") // underscore should be escaped -> literal underscore
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(0));
    }

    void postSearch_missingAuthHeader_returns401() throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).checkForAuthorisedUser(any());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(java.util.List.of(10))
            .activeAccountsOnly(false)
            .accountNumber("12345678")
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_PROBLEM_JSON) // ok even if server doesn't set it
                            .content(objectMapper.writeValueAsString(search)))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""));
    }

    void postSearch_invalidToken_returns401ProblemJson() throws Exception {

        doThrow(new ResponseStatusException(UNAUTHORIZED, "Invalid token"))
            .when(userStateService).checkForAuthorisedUser(any());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(java.util.List.of(10))
            .activeAccountsOnly(false)
            .accountNumber("12345678")
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("authorization", "Bearer some_value")
                            .content(objectMapper.writeValueAsString(search)))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""));
    }

    void postSearch_authenticatedWithoutPermission_returns403ProblemJson() throws Exception {
        doThrow(new ResponseStatusException(FORBIDDEN, "Forbidden"))
            .when(userStateService).checkForAuthorisedUser(any());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(java.util.List.of(10))
            .activeAccountsOnly(false)
            .accountNumber("12345678")
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("authorization", "Bearer some_value")
                            .content(objectMapper.writeValueAsString(search)))
            .andExpect(status().isForbidden())
            .andExpect(content().string(""));
    }

    // AC1b: Test that both active and inactive accounts are returned regardless of activeAccountsOnly value
    void testAC1b_ActiveAccountsOnlyTrue(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .activeAccountsOnly(true)
            .accountNumber("12345678")
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.creditor_accounts[*].account_number")
                           .value(hasItems("12345678A", "12345678")));
    }

    void testAC1b_ActiveAccountsOnlyFalse(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .activeAccountsOnly(false)
            .accountNumber("12345678")
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.creditor_accounts[*].account_number")
                           .value(hasItems("12345678A", "12345678")));
    }

    // AC1a: Test multiple search parameters - creditor personal details + business unit
    void testAC1a_MultiParam_ForenamesAndSurname(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .creditor(uk.gov.hmcts.opal.dto.Creditor.builder()
                          .forenames("John")
                          .surname("Smith")
                          .build())
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.creditor_accounts[*].account_number")
                           .value(hasItems("JS987654")))
            .andExpect(jsonPath("$.creditor_accounts[*].firstnames")
                           .value(hasItems("John")))
            .andExpect(jsonPath("$.creditor_accounts[*].surname")
                           .value(hasItems("Smith")));
    }

    // AC1a: Test multiple search parameters - postcode + business unit + account number
    void testAC1a_MultiParam_PostcodeAndAccountNumber(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .accountNumber("12345678")
            .creditor(uk.gov.hmcts.opal.dto.Creditor.builder()
                          .postcode("MA4 1AL")
                          .build())
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.creditor_accounts[*].account_number")
                           .value(hasItems("12345678A", "12345678")))
            .andExpect(jsonPath("$.creditor_accounts[*].postcode")
                           .value(hasItems("MA4 1AL")));
    }

    // AC1a: Test multiple search parameters - organisation details + business unit + address
    void testAC1a_MultiParam_OrganisationAndAddress(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .creditor(uk.gov.hmcts.opal.dto.Creditor.builder()
                          .organisationName("Acme")
                          .addressLine1("Acme House")
                          .build())
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.creditor_accounts[*].address_line_1")
                           .value(hasItems("Acme House")));
    }

    // AC1ai: Test that accounts from different business units are not returned
    void testAC1ai_BusinessUnitFiltering(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10, 11)) // Multiple business units
            .activeAccountsOnly(false)
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.creditor_accounts[*].business_unit_id")
                           .value(org.hamcrest.Matchers.everyItem(
                               org.hamcrest.Matchers.anyOf(
                                   org.hamcrest.Matchers.equalTo("10"),
                                   org.hamcrest.Matchers.equalTo("11")
                               )
                           )));
    }

    // AC2a: Test exact match surname functionality - exact match enabled
    void testAC2a_ExactMatchSurnameEnabled(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .creditor(uk.gov.hmcts.opal.dto.Creditor.builder()
                          .surname("Smith")
                          .exactMatchSurname(true) // Exact match enabled
                          .build())
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.creditor_accounts[*].surname")
                           .value(hasItems("Smith"))) // Should only return exact "Smith"
            .andExpect(jsonPath("$.creditor_accounts[*].surname")
                           .value(org.hamcrest.Matchers.not(hasItems("Smithson")))); // Should NOT return "Smithson"
    }

    // AC2ai: Test exact match surname functionality - exact match disabled (starts with)
    void testAC2ai_ExactMatchSurnameDisabled(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .creditor(uk.gov.hmcts.opal.dto.Creditor.builder()
                          .surname("Smith")
                          .exactMatchSurname(false) // Exact match disabled - should do "starts with"
                          .build())
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.creditor_accounts[*].surname")
                           .value(hasItems("Smith", "Smithson"))); // Should return both "Smith" and "Smithson"
    }

    // AC2b: Test exact match forenames functionality - exact match enabled
    void testAC2b_ExactMatchForenamesEnabled(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .creditor(uk.gov.hmcts.opal.dto.Creditor.builder()
                          .forenames("John")
                          .exactMatchForenames(true) // Exact match enabled
                          .build())
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.creditor_accounts[*].firstnames")
                           .value(hasItems("John"))) // Should only return exact "John"
            .andExpect(jsonPath("$.creditor_accounts[*].firstnames")
                           .value(org.hamcrest.Matchers.not(hasItems("Johnathan")))); // Should NOT return "Jonathan"
    }

    // AC2bi: Test exact match forenames functionality - exact match disabled (starts with)
    void testAC2bi_ExactMatchForenamesDisabled(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .creditor(uk.gov.hmcts.opal.dto.Creditor.builder()
                          .forenames("John")
                          .exactMatchForenames(false) // Exact match disabled - should do "starts with"
                          .build())
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.creditor_accounts[*].firstnames")
                           .value(hasItems("John", "Johnathan"))); // Should return both "John" and "Jonathan"
    }

    // AC2c: Test "starts with" behavior for Address Line 1
    void testAC2c_AddressLine1StartsWith(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .creditor(uk.gov.hmcts.opal.dto.Creditor.builder()
                          .addressLine1("123") // Should match addresses starting with "123"
                          .build())
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.creditor_accounts[*].address_line_1")
                           .value(hasItems("123 Test Street"))); // Should return addresses starting with "123"
    }

    // AC2c: Test "starts with" behavior for Postcode
    void testAC2c_PostcodeStartsWith(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .creditor(uk.gov.hmcts.opal.dto.Creditor.builder()
                          .postcode("TS") // Should match postcodes starting with "TS"
                          .build())
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.creditor_accounts[*].postcode")
                           .value(hasItems("TS1 2AB", "TS3 4CD"))); // Should return postcodes starting with "TS"
    }

    // AC3a: Test exact match for Company name
    void testAC3a_CompanyNameExactMatch(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .creditor(uk.gov.hmcts.opal.dto.Creditor.builder()
                          .organisationName("Tech Solutions")
                          .exactMatchOrganisationName(true)
                          .organisation(true)
                          .build())
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.creditor_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.creditor_accounts[0].organisation_name")
                            .value("Tech Solutions"));
    }

    // AC3ai: Test "starts with" behavior for Company name when exact match is not selected
    void testAC3ai_CompanyNameStartsWith(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .creditor(uk.gov.hmcts.opal.dto.Creditor.builder()
                          .organisationName("Tech")
                          .exactMatchOrganisationName(false)
                          .organisation(true)
                          .build())
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(3)))
            .andExpect(jsonPath("$.creditor_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.creditor_accounts[*].organisation_name")
                           .value(hasItems("Tech Solutions", "Tech Solutions Ltd", "Technology Partner")));
    }

    // AC3ai: Test "starts with" behavior with partial Company name
    void testAC3ai_CompanyNameStartsWithPartial(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .creditor(uk.gov.hmcts.opal.dto.Creditor.builder()
                          .organisationName("Technology")
                          .exactMatchOrganisationName(false)
                          .organisation(true)
                          .build())
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.creditor_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.creditor_accounts[0].organisation_name")
                            .value("Technology Partner"));
    }

    // AC3b: Test "starts with" behavior for Company Address Line 1
    void testAC3b_CompanyAddressLine1StartsWith(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .creditor(uk.gov.hmcts.opal.dto.Creditor.builder()
                          .addressLine1("Tech") // Should match company addresses starting with "Tech"
                          .organisation(true)
                          .build())
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.creditor_accounts[*].address_line_1")
                           .value(hasItems("Tech House", "Tech Building")));
    }

    // AC3b: Test "starts with" behavior for Company Postcode
    void testAC3b_CompanyPostcodeStartsWith(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .creditor(uk.gov.hmcts.opal.dto.Creditor.builder()
                          .postcode("TP") // Match company postcodes starting with "T"
                          .organisation(true)
                          .build())
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.creditor_accounts[*].postcode")
                           .value(hasItems("TP3 4DE", "TP5 6FG"))); // Should return company postcodes starting with "T"
    }

    // AC3b: Test combined Address Line 1 and Postcode search for companies
    void testAC3b_CompanyAddressAndPostcodeCombined(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(10))
            .creditor(uk.gov.hmcts.opal.dto.Creditor.builder()
                          .addressLine1("Tech House") // Specific address
                          .postcode("TH1") // Specific postcode prefix
                          .organisation(true)
                          .build())
            .build();

        mockMvc.perform(post(URL_BASE + "/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search))
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.creditor_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.creditor_accounts[0].address_line_1").value("Tech House"))
            .andExpect(jsonPath("$.creditor_accounts[0].postcode").value("TH1 2BC"))
            .andExpect(jsonPath("$.creditor_accounts[0].organisation_name").value("Tech Solutions"));
    }

    void getHeaderSummaryImpl_Success(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        Long minorCreditorId = 99000000000800L;

        ResultActions resultActions = mockMvc.perform(
            get(URL_BASE + "/{id}/header-summary", minorCreditorId)
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer some_value")
        );

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetMinorCreditorHeaderSummary: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))

            .andExpect(jsonPath("$.creditor.account_id").value(String.valueOf(minorCreditorId)))
            .andExpect(jsonPath("$.creditor.account_number").value("87654321"))
            .andExpect(jsonPath("$.creditor.account_type.type").value("MN"))

            .andExpect(header().exists("ETag"))

            .andExpect(jsonPath("$.business_unit.business_unit_id").value("77"))
            .andExpect(jsonPath("$.business_unit.business_unit_name").value("Camberwell Green"))
            .andExpect(jsonPath("$.business_unit.welsh_speaking").value(matchesPattern("Y|N")))

            .andExpect(jsonPath("$.party.party_id").value("99000000000900"))
            .andExpect(jsonPath("$.party.organisation_flag").value(true))
            .andExpect(jsonPath("$.party.organisation_details.organisation_name")
                .value("Minor Creditor Test Ltd"))
            .andExpect(jsonPath("$.party.organisation_details.organisation_aliases")
                .value(nullValue()))

            .andExpect(jsonPath("$.financials.awarded").value(0))
            .andExpect(jsonPath("$.financials.paid_out").value(0))
            .andExpect(jsonPath("$.financials.awaiting_payout").value(0))
            .andExpect(jsonPath("$.financials.outstanding").value(0));

        jsonSchemaValidationService.validate(body, MINOR_CREDITOR_HEADER_SUMMARY_RESPONSE);
    }

    void getHeaderSummary_notFound_returns404(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        Long missingId = 999999L;

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/{id}/header-summary", missingId)
            .accept(MediaType.APPLICATION_JSON)
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetMinorCreditorHeaderSummary_NotFound: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound());
    }

    void getHeaderSummary_missingAuthHeader_returns401() throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(get(URL_BASE + "/{id}/header-summary", 104L)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(content().string(""));
    }

    void getHeaderSummary_authenticatedWithoutPermission_returns403() throws Exception {
        doThrow(new ResponseStatusException(FORBIDDEN, "Forbidden"))
            .when(userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(get(URL_BASE + "/{id}/header-summary", 104L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isForbidden())
            .andExpect(content().string(""));
    }

    void getHeaderSummary_timeout_returns408(Logger log) throws Exception {
        doThrow(new ResponseStatusException(REQUEST_TIMEOUT, "Timeout"))
            .when(userStateService).checkForAuthorisedUser(any());

        ResultActions resultActions = mockMvc.perform(
            get(URL_BASE + "/{id}/header-summary", 104L).header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":getHeaderSummary_timeout_returns408: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isRequestTimeout());
    }

    void getHeaderSummary_serviceUnavailable_returns503(Logger log) throws Exception {
        doThrow(new ResponseStatusException(SERVICE_UNAVAILABLE, "Gateway down"))
            .when(userStateService).checkForAuthorisedUser(any());

        ResultActions resultActions = mockMvc.perform(
            get(URL_BASE + "/{id}/header-summary", 104L).header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":getHeaderSummary_serviceUnavailable_returns503: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isServiceUnavailable());
    }

    void getHeaderSummary_serverError_returns500(Logger log) throws Exception {
        doThrow(new ResponseStatusException(INTERNAL_SERVER_ERROR, "Boom"))
            .when(userStateService).checkForAuthorisedUser(any());

        ResultActions resultActions = mockMvc.perform(
            get(URL_BASE + "/{id}/header-summary", 104L).header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":getHeaderSummary_serverError_returns500: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isInternalServerError());
    }
}
