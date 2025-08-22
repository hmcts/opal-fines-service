package uk.gov.hmcts.opal.controllers;

import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

/**
 * Common tests for both Opal and Legacy modes, to ensure 100% compatibility.
 */
abstract class MinorCreditorControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/minor-creditor-accounts";

    private static final String GET_HEADER_SUMMARY_RESPONSE = "postMinorCreditorAccountSearchResponse.json";

    @MockitoBean
    UserStateService userStateService;

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    void postSearchMinorCreditorImpl_Success(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .accountNumber("12345678A").build();

        ResultActions resultActions = mockMvc.perform(post(URL_BASE + "/search")
                                                          .contentType(MediaType.APPLICATION_JSON).content(
            objectMapper.writeValueAsString(search)).header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostMinorCreditorSearch: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.creditor_accounts[0].creditor_account_id").value("CA123456"))
            .andExpect(jsonPath("$.creditor_accounts[0].account_number").value("ACC-987654"))
            .andExpect(jsonPath("$.creditor_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.creditor_accounts[0].organisation_name").value("Acme Corp Ltd"))
            .andExpect(jsonPath("$.creditor_accounts[0].firstnames").value(nullValue()))
            .andExpect(jsonPath("$.creditor_accounts[0].surname").value(nullValue()))
            .andExpect(jsonPath("$.creditor_accounts[0].address_line_1").value("123 Main Street"))
            .andExpect(jsonPath("$.creditor_accounts[0].postcode").value("AB1 2CD"))
            .andExpect(jsonPath("$.creditor_accounts[0].business_unit_name").value("Finance Department"))
            .andExpect(jsonPath("$.creditor_accounts[0].business_unit_id").value("BU123"))
            .andExpect(jsonPath("$.creditor_accounts[0].account_balance").value(1000.0))
            .andExpect(jsonPath("$.creditor_accounts[0].defendant.defendant_account_id").value("DA001"))
            .andExpect(jsonPath("$.creditor_accounts[0].defendant.organisation").value(true))
            .andExpect(jsonPath("$.creditor_accounts[0].defendant.organisation_name").value("Example Holdings PLC"))
            .andExpect(jsonPath("$.creditor_accounts[0].defendant.firstnames").value(nullValue()))
            .andExpect(jsonPath("$.creditor_accounts[0].defendant.surname").value(nullValue()));

        jsonSchemaValidationService.validateOrError(body, GET_HEADER_SUMMARY_RESPONSE);
    }

    void postSearchMinorCreditorImpl_500Error(Logger log) throws Exception {

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
}
