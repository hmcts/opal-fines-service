package uk.gov.hmcts.opal.controllers;

import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
