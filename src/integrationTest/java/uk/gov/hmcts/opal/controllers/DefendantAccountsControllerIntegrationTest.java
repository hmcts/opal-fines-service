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

import java.util.Collections;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

        ResultActions resultActions = mockMvc.perform(
            get(URL_BASE + "/77/header-summary")
                .header("Authorization", "Bearer some_value")
        ).andDo(print());

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.account_number").value("177A"))
            .andExpect(jsonPath("$.defendant_party_id").value("77"))
            .andExpect(jsonPath("$.parent_guardian_party_id", nullValue()))
            .andExpect(jsonPath("$.account_status_reference.account_status_code").value("L"))
            .andExpect(jsonPath("$.account_status_reference.account_status_display_name").value("Live"))
            .andExpect(jsonPath("$.account_type").value("Fine"))
            .andExpect(jsonPath("$.prosecutor_case_reference").value("090A"))
            .andExpect(jsonPath("$.fixed_penalty_ticket_number").value("888"))
            .andExpect(jsonPath("$.business_unit_summary.business_unit_id").value("78"))
            .andExpect(jsonPath("$.business_unit_summary.business_unit_name").value("N E Region"))
            .andExpect(jsonPath("$.business_unit_summary.welsh_speaking").value("N"))
            .andExpect(jsonPath("$.payment_state_summary.imposed_amount").value(700.58))
            .andExpect(jsonPath("$.payment_state_summary.arrears_amount").value(0.0))
            .andExpect(jsonPath("$.payment_state_summary.paid_amount").value(200.0))
            .andExpect(jsonPath("$.payment_state_summary.account_balance").value(500.58))
            .andExpect(jsonPath("$.party_details.defendant_account_party_id").value("77"))
            .andExpect(jsonPath("$.party_details.organisation_flag").value(false))
            .andExpect(jsonPath("$.party_details.organisation_details.organisation_name").value("Sainsco"))
            .andExpect(jsonPath("$.party_details.individual_details.title").value("Ms"))
            .andExpect(jsonPath("$.party_details.individual_details.forenames").value("Anna"))
            .andExpect(jsonPath("$.party_details.individual_details.surname").value("Graham"))
            .andExpect(jsonPath("$.party_details.individual_details.date_of_birth").value("1980-02-03"))
            .andExpect(jsonPath("$.party_details.individual_details.age").value("45"))
            .andExpect(jsonPath("$.party_details.individual_details.individual_aliases").isArray());

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

    @DisplayName("Header Summary - 404 Not Found for non-existent ID")
    void getHeaderSummary_NotFound(Logger log) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        mockMvc.perform(get("/defendant-accounts/9999999/header-summary")
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType("application/problem+json"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").exists())
            .andExpect(jsonPath("$.detail").exists());
    }

    @DisplayName("Header Summary - 401 Unauthorized with no Authorization header")
    void getHeaderSummary_Unauthorized_NoHeader(Logger log) throws Exception {
        mockMvc.perform(get("/defendant-accounts/77/header-summary"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.title").exists())
            .andExpect(jsonPath("$.detail").exists());
    }

    @DisplayName("Header Summary - 403 Forbidden when not authorised")
    void getHeaderSummary_Forbidden(Logger log) throws Exception {
        UserState noPermissionsUser = UserState.builder()
            .userId(123L)
            .userName("unauthorised_user")
            .businessUnitUser(Collections.emptySet())
            .build();

        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(noPermissionsUser);

        mockMvc.perform(get("/defendant-accounts/77/header-summary")
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType("application/problem+json"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.title").exists())
            .andExpect(jsonPath("$.detail").exists());
    }

}
