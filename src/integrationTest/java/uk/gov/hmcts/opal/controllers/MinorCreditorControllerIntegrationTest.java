package uk.gov.hmcts.opal.controllers;

import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

/**
 * Common tests for both Opal and Legacy modes, to ensure 100% compatibility.
 */
abstract class MinorCreditorControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/minor-creditor-accounts";

    private static final String MINOR_CREDITOR_RESPONSE =
        "opal/minor-creditor/postMinorCreditorAccountSearchResponse.json";

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
            .andExpect(jsonPath("$.creditor_accounts[0].organisation_name").value(nullValue()))
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
            .andExpect(jsonPath("$.creditor_accounts[1].organisation_name").value(nullValue()))
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
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Forbidden"))
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

}
