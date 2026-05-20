package uk.gov.hmcts.opal.controllers;

import feign.FeignException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.REQUEST_TIMEOUT;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noPermissionsUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;

/**
 * Common tests for both Opal and Legacy modes, to ensure 100% compatibility.
 */
abstract class MinorCreditorControllerIntegrationTest extends AbstractIntegrationTest {

    protected static final String URL_BASE = "/minor-creditor-accounts";
    private static final String AUTH_HEADER = "Bearer some_value";
    private static final long PATCH_MINOR_CREDITOR_ACCOUNT_ID = 607L;
    private static final short PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID = 10;

    private static final String MINOR_CREDITOR_RESPONSE =
        "opal/minor-creditor/postMinorCreditorAccountSearchResponse.json";

    private static final String MINOR_CREDITOR_HEADER_SUMMARY_RESPONSE =
        "opal/minor-creditor/getMinorCreditorAccountHeaderSummaryResponse.json";

    @MockitoBean
    UserStateService userStateService;

    @MockitoBean
    UserStateClientService userStateClientService;

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

    void patchMinorCreditor_payoutHold_success(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(permissionUser(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID,
                FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD,
                FinesPermission.ACCOUNT_MAINTENANCE));

        final boolean initialHoldPayout = getCurrentCreditorAccountHoldPayout();
        Integer currentVersion = getCurrentCreditorAccountVersion();
        String currentEtag = "\"" + currentVersion + "\"";

        String requestJson = objectMapper.writeValueAsString(patchMinorCreditorPayoutHoldRequest());

        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", AUTH_HEADER)
                .header("If-Match", currentEtag)
                .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                .content(requestJson));

        String body = a.andReturn().getResponse().getContentAsString();

        log.info(":patchMinorCreditor_payoutHold_success body:\n{}", ToJsonString.toPrettyJson(body));

        a.andExpect(status().isCreated())
            .andExpect(header().exists("ETag"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.creditor_account_id").value(PATCH_MINOR_CREDITOR_ACCOUNT_ID))
            .andExpect(jsonPath("$.party_details.individual_details.surname").value("Updated"))
            .andExpect(jsonPath("$.party_details.individual_details.forenames").value("Creditor"))
            .andExpect(jsonPath("$.address.postcode").value("NW1 1AA"))
            .andExpect(jsonPath("$.payment.account_name").value("Creditor Updated"))
            .andExpect(jsonPath("$.payment.sort_code").value("112233"))
            .andExpect(jsonPath("$.payment.account_number").value("12345678"))
            .andExpect(jsonPath("$.payment.account_reference").value("MC-REF-01"))
            .andExpect(jsonPath("$.payment.hold_payment").value(true))
            .andExpect(jsonPath("$.payment.pay_by_bacs").value(true));

        assertEquals(true, getCurrentCreditorAccountHoldPayout());
        assertEquals("Creditor Updated", getCurrentCreditorAccountBankAccountName());
        assertEquals("112233", getCurrentCreditorAccountBankSortCode());
        assertEquals("12345678", getCurrentCreditorAccountBankAccountNumber());
        assertEquals("MC-REF-01", getCurrentCreditorAccountBankAccountReference());
        assertEquals(true, getCurrentCreditorAccountPayByBacs());
        Integer updatedVersion = getCurrentCreditorAccountVersion();
        assertEquals(initialHoldPayout ? currentVersion : currentVersion + 1, updatedVersion);
    }

    void patchMinorCreditor_success_createsAmendments(Logger log) throws Exception {
        // Arrange
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(permissionUser(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID,
                FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD,
                FinesPermission.ACCOUNT_MAINTENANCE));

        Integer currentVersion = getCurrentCreditorAccountVersion();
        int amendmentsBefore = getCurrentAmendmentCountForCreditorAccount();

        // Act
        ResultActions a = mockMvc.perform(
            patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", AUTH_HEADER)
                .header("If-Match", "\"" + currentVersion + "\"")
                .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                .content(objectMapper.writeValueAsString(patchMinorCreditorPayoutHoldRequest())));

        String body = a.andReturn().getResponse().getContentAsString();
        log.info(":patchMinorCreditor_success_createsAmendments body:\n{}", ToJsonString.toPrettyJson(body));

        // Assert
        a.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        int amendmentsAfter = getCurrentAmendmentCountForCreditorAccount();
        assertTrue(amendmentsAfter > amendmentsBefore);
        assertEquals("ACCOUNT_ENQUIRY", getLatestAmendmentFunctionCodeForCreditorAccount());
    }

    void patchMinorCreditor_withoutPermission_returns403() throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(noPermissionsUser());

        UserState userState = UserState.builder().userId(123L).build();
        when(userStateClientService.getUserStateByAuthenticatedUser()).thenReturn(Optional.of(userState));

        Integer currentVersion = getCurrentCreditorAccountVersion();

        mockMvc.perform(patch(URL_BASE + "/606")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer some_value")
                            .header("If-Match", currentVersion)
                            .header("Business-Unit-Id", "10")
                            .content(objectMapper.writeValueAsString(patchMinorCreditorWithoutPermissionRequest())))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    void patchMinorCreditor_withoutHoldPermission_returns403() throws Exception {
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(permissionUser(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID, FinesPermission.ACCOUNT_MAINTENANCE));

        Integer currentVersion = getCurrentCreditorAccountVersion();

        mockMvc.perform(patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", AUTH_HEADER)
                            .header("If-Match", currentVersion)
                            .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                            .content(objectMapper.writeValueAsString(patchMinorCreditorPayoutHoldRequest())))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    void patchMinorCreditor_withoutHoldPermission_paymentUnchanged_returns403() throws Exception {
        // Arrange
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(permissionUser(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID, FinesPermission.ACCOUNT_MAINTENANCE));

        boolean currentHoldPayout = getCurrentCreditorAccountHoldPayout();
        Integer currentVersion = getCurrentCreditorAccountVersion();

        mockMvc.perform(patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", AUTH_HEADER)
                            .header("If-Match", currentVersion)
                            .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                            .content(objectMapper.writeValueAsString(patchMinorCreditorRequest(currentHoldPayout))))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    void patchMinorCreditor_withoutAccountMaintenancePermission_returns403() throws Exception {
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(permissionUser(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID,
                FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD));

        Integer currentVersion = getCurrentCreditorAccountVersion();

        mockMvc.perform(patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", AUTH_HEADER)
                            .header("If-Match", currentVersion)
                            .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                            .content(objectMapper.writeValueAsString(patchMinorCreditorPayoutHoldRequest())))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    void patchMinorCreditor_notFound_returns404() throws Exception {
        // Arrange
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(permissionUser(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID,
                FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD,
                FinesPermission.ACCOUNT_MAINTENANCE));

        // Act & Assert
        mockMvc.perform(patch(URL_BASE + "/999999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", AUTH_HEADER)
                            .header("If-Match", "\"1\"")
                            .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                            .content(objectMapper.writeValueAsString(patchMinorCreditorPayoutHoldRequest())))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    void patchMinorCreditor_staleVersion_returns409() throws Exception {
        // Arrange
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(permissionUser(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID,
                FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD,
                FinesPermission.ACCOUNT_MAINTENANCE));

        Integer currentVersion = getCurrentCreditorAccountVersion();

        // Act & Assert
        mockMvc.perform(patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", AUTH_HEADER)
                            .header("If-Match", "\"" + (currentVersion + 1) + "\"")
                            .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                            .content(objectMapper.writeValueAsString(patchMinorCreditorPayoutHoldRequest())))
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    void patchMinorCreditor_missingAuthHeader_returns401() throws Exception {
        FeignException.Unauthorized unauthorized = mock(FeignException.Unauthorized.class);
        when(unauthorized.status()).thenReturn(UNAUTHORIZED.value());
        when(unauthorized.getMessage()).thenReturn("Unauthorized");

        doThrow(unauthorized).when(userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("If-Match", "\"1\"")
                            .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                            .content(objectMapper.writeValueAsString(patchMinorCreditorPayoutHoldRequest())))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(jsonPath("$.title").value("Not Authorised for Connection"))
            .andExpect(jsonPath("$.detail").value("Unauthorized"));
    }

    void patchMinorCreditor_timeout_returns408(Logger log) throws Exception {
        doThrow(new ResponseStatusException(REQUEST_TIMEOUT, "Timeout"))
            .when(userStateService).checkForAuthorisedUser(any());

        ResultActions resultActions = mockMvc.perform(
            patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", AUTH_HEADER)
                .header("If-Match", "\"1\"")
                .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                .content(objectMapper.writeValueAsString(patchMinorCreditorPayoutHoldRequest())));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":patchMinorCreditor_timeout_returns408: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isRequestTimeout());
    }

    void patchMinorCreditor_serviceUnavailable_returns503(Logger log) throws Exception {
        doThrow(new ResponseStatusException(SERVICE_UNAVAILABLE, "Gateway down"))
            .when(userStateService).checkForAuthorisedUser(any());

        ResultActions resultActions = mockMvc.perform(
            patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", AUTH_HEADER)
                .header("If-Match", "\"1\"")
                .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                .content(objectMapper.writeValueAsString(patchMinorCreditorPayoutHoldRequest())));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":patchMinorCreditor_serviceUnavailable_returns503: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isServiceUnavailable());
    }

    void patchMinorCreditor_serverError_returns500(Logger log) throws Exception {
        doThrow(new ResponseStatusException(INTERNAL_SERVER_ERROR, "Boom"))
            .when(userStateService).checkForAuthorisedUser(any());

        ResultActions resultActions = mockMvc.perform(
            patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", AUTH_HEADER)
                .header("If-Match", "\"1\"")
                .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                .content(objectMapper.writeValueAsString(patchMinorCreditorPayoutHoldRequest())));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":patchMinorCreditor_serverError_returns500: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isInternalServerError());
    }

    void patchMinorCreditor_missingPayload_returns400() throws Exception {
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER))
            .thenReturn(permissionUser(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID,
                FinesPermission.ADD_AND_REMOVE_PAYMENT_HOLD,
                FinesPermission.ACCOUNT_MAINTENANCE));

        Integer currentVersion = getCurrentCreditorAccountVersion();

        mockMvc.perform(patch(URL_BASE + "/" + PATCH_MINOR_CREDITOR_ACCOUNT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", AUTH_HEADER)
                            .header("If-Match", currentVersion)
                            .header("Business-Unit-Id", String.valueOf(PATCH_MINOR_CREDITOR_BUSINESS_UNIT_ID))
                            .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(org.hamcrest.Matchers.anything()));
    }

    private PatchMinorCreditorAccountRequest patchMinorCreditorPayoutHoldRequest() {
        return patchMinorCreditorRequest(true);
    }

    private PatchMinorCreditorAccountRequest patchMinorCreditorRequest(boolean holdPayment) {
        return new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon()
                              .partyId("99008")
                              .organisationFlag(false)
                              .individualDetails(new IndividualDetailsCommon()
                                                     .surname("Updated")
                                                     .forenames("Creditor")))
            .address(new AddressDetailsCommon()
                         .addressLine1("99 Updated Road")
                         .postcode("NW1 1AA"))
            .payment(new CreditorAccountPaymentDetailsCommon()
                         .accountName("Creditor Updated")
                         .sortCode("112233")
                         .accountNumber("12345678")
                         .accountReference("MC-REF-01")
                         .payByBacs(true)
                         .holdPayment(holdPayment));
    }

    private PatchMinorCreditorAccountRequest patchMinorCreditorWithoutPermissionRequest() {
        return new PatchMinorCreditorAccountRequest()
            .partyDetails(new PartyDetailsCommon()
                              .partyId("99007")
                              .organisationFlag(false)
                              .individualDetails(new IndividualDetailsCommon()
                                                     .surname("Deleted")))
            .address(new AddressDetailsCommon()
                         .addressLine1("33 Delete St.")
                         .postcode("DE1 2DE"))
            .payment(new CreditorAccountPaymentDetailsCommon()
                         .accountName("Delete Account")
                         .sortCode("445566")
                         .accountNumber("87654321")
                         .accountReference("DEL-REF-01")
                         .payByBacs(true)
                         .holdPayment(false));
    }

    private Integer getCurrentCreditorAccountVersion() {
        return jdbcTemplate.queryForObject(
            "SELECT version_number FROM creditor_accounts WHERE creditor_account_id = ?",
            Integer.class,
            PATCH_MINOR_CREDITOR_ACCOUNT_ID
        );
    }

    private boolean getCurrentCreditorAccountHoldPayout() {
        Boolean holdPayout = jdbcTemplate.queryForObject(
            "SELECT hold_payout FROM creditor_accounts WHERE creditor_account_id = ?",
            Boolean.class,
            PATCH_MINOR_CREDITOR_ACCOUNT_ID
        );
        return Boolean.TRUE.equals(holdPayout);
    }

    private boolean getCurrentCreditorAccountPayByBacs() {
        Boolean payByBacs = jdbcTemplate.queryForObject(
            "SELECT pay_by_bacs FROM creditor_accounts WHERE creditor_account_id = ?",
            Boolean.class,
            PATCH_MINOR_CREDITOR_ACCOUNT_ID
        );
        return Boolean.TRUE.equals(payByBacs);
    }

    private String getCurrentCreditorAccountBankAccountName() {
        return jdbcTemplate.queryForObject(
            "SELECT bank_account_name FROM creditor_accounts WHERE creditor_account_id = ?",
            String.class,
            PATCH_MINOR_CREDITOR_ACCOUNT_ID
        );
    }

    private String getCurrentCreditorAccountBankSortCode() {
        return jdbcTemplate.queryForObject(
            "SELECT bank_sort_code FROM creditor_accounts WHERE creditor_account_id = ?",
            String.class,
            PATCH_MINOR_CREDITOR_ACCOUNT_ID
        );
    }

    private String getCurrentCreditorAccountBankAccountNumber() {
        return jdbcTemplate.queryForObject(
            "SELECT bank_account_number FROM creditor_accounts WHERE creditor_account_id = ?",
            String.class,
            PATCH_MINOR_CREDITOR_ACCOUNT_ID
        );
    }

    private String getCurrentCreditorAccountBankAccountReference() {
        return jdbcTemplate.queryForObject(
            "SELECT bank_account_reference FROM creditor_accounts WHERE creditor_account_id = ?",
            String.class,
            PATCH_MINOR_CREDITOR_ACCOUNT_ID
        );
    }

    private int getCurrentAmendmentCountForCreditorAccount() {
        Integer amendmentCount = jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM amendments
                WHERE associated_record_type = 'creditor_accounts'
                  AND associated_record_id = ?
                """,
            Integer.class,
            String.valueOf(PATCH_MINOR_CREDITOR_ACCOUNT_ID)
        );
        return amendmentCount != null ? amendmentCount : 0;
    }

    private String getLatestAmendmentFunctionCodeForCreditorAccount() {
        return jdbcTemplate.queryForObject(
            """
                SELECT function_code
                FROM amendments
                WHERE associated_record_type = 'creditor_accounts'
                  AND associated_record_id = ?
                ORDER BY amendment_id DESC
                LIMIT 1
                """,
            String.class,
            String.valueOf(PATCH_MINOR_CREDITOR_ACCOUNT_ID)
        );
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
            .andExpect(jsonPath("$.creditor.account_type.display_name").value("Minor Creditor"))
            .andExpect(jsonPath("$.creditor.has_associated_defendant").value(false))

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

    void getMinorCreditorAtAGlanceImpl_Success(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/{id}/at-a-glance", "99000000000801")
            .contentType(MediaType.APPLICATION_JSON)
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();

        log.info(":testGetMinorCreditorAtAGlance_Success: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("etag", "\"1\""))

            // party
            .andExpect(jsonPath("$.party.party_id").value("99000000000901"))
            .andExpect(jsonPath("$.party.organisation_flag").value(true))

            // individual_details
            .andExpect(jsonPath("$.party.organisation_details.organisation_name").value("Speed Camera Services Ltd"))

            // address
            .andExpect(jsonPath("$.address.address_line_1").value("10 Technology Way"))
            .andExpect(jsonPath("$.address.address_line_2").value("Reading"))
            .andExpect(jsonPath("$.address.postcode").value("RG6 1PT"))

            // creditor_account_id
            .andExpect(jsonPath("$.creditor_account_id").value(99000000000801L))

            // defendant
            .andExpect(jsonPath("$.defendant.account_number").value("12345678"))
            .andExpect(jsonPath("$.defendant.account_id").value(99000000000001L))
            .andExpect(jsonPath("$.defendant.title").value("Mr"))
            .andExpect(jsonPath("$.defendant.forenames").value("Michael James"))
            .andExpect(jsonPath("$.defendant.surname").value("Johnson"))

            // payment
            .andExpect(jsonPath("$.payment.is_bacs").value(true))
            .andExpect(jsonPath("$.payment.hold_payment").value(false));
    }

    void getMinorCreditorAtAGlanceImpl_failure_creditorNotFound(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        Long missingId = 999999L;

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/{id}/at-a-glance", missingId)
            .accept(MediaType.APPLICATION_JSON)
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetMinorCreditorAtAGlance_creditorNotFound: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isNotFound());
    }

    void getMinorCreditorAtAGlanceImpl_serverError_throws500(Logger log) throws Exception {
        doThrow(new ResponseStatusException(INTERNAL_SERVER_ERROR, "Boom"))
            .when(userStateService).checkForAuthorisedUser(any());

        ResultActions resultActions = mockMvc.perform(
            get(URL_BASE + "/{id}/at-a-glance", 104L).header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":getAtAGlance_serverError_returns500: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isInternalServerError());
    }

    void legacyGetMinorCreditorAtAGlanceImpl_500Error(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());


        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/{id}/at-a-glance", "500")
            .contentType(MediaType.APPLICATION_JSON)
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetMinorGcreditorAtAGlance: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(
            status().is5xxServerError()).andExpect(
            content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }


}
