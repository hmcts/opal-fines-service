package uk.gov.hmcts.opal.controllers;

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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@ActiveProfiles({"integration", "legacy"})
@Sql(scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql", executionPhase = AFTER_TEST_CLASS)
@Slf4j(topic = "opal.LegacyDefendantsIntegrationTest01")
class LegacyDefendantsSearchIntegrationTest extends AbstractIntegrationTest {

    private static final String DEFENDANTS_SEARCH_URL = "/defendant-accounts/search";

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
    void setupUserState() {
        Mockito.when(userState.anyBusinessUnitUserHasPermission(Mockito.any())).thenReturn(true);
        Mockito.when(userStateService.checkForAuthorisedUser(Mockito.any())).thenReturn(userState);
    }

    @Test
    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @DisplayName("Search defendant accounts - POST with valid criteria [@PO-33, @PO-119]")
    void testPostDefendantAccountsSearch() throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(post(DEFENDANTS_SEARCH_URL)
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

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("defendant_accounts[0].defendant_account_id").value("1"))
            .andExpect(jsonPath("defendant_accounts[0].account_number").value("100A"))
            .andExpect(jsonPath("$.defendant_accounts[0].business_unit_id").value("78"));
    }

    @Test
    @Disabled("A running instance of Legacy Stub App is required to execute this test")
    @DisplayName("Search defendant accounts - No Accounts found [@PO-33, @PO-119]")
    void testPostDefendantAccountsSearch_WhenNoDefendantAccountsFound() throws Exception {
        when(userStateService.checkForAuthorisedUser(anyString())).thenReturn(allFinesPermissionUser());

        ResultActions actions = mockMvc.perform(post(DEFENDANTS_SEARCH_URL).header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON).content("""
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

        actions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(0));
    }


}
