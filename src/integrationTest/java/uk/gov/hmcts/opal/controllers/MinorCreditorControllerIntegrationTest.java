package uk.gov.hmcts.opal.controllers;

import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorSearch;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.List;

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
abstract class MinorCreditorControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/minor-creditor-accounts";

    private static final String GET_HEADER_SUMMARY_RESPONSE = "opal/postMinorCreditorAccountSearchResponse.json";

    @MockitoBean
    UserStateService userStateService;

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    void postSearchMinorCreditorImpl_Success(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(1))
            .activeAccountsOnly(true)
            .accountNumber("ACC-987654").build();

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/search")
                                                          .contentType(MediaType.APPLICATION_JSON).content(
            objectMapper.writeValueAsString(search)).header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.creditor_accounts").isArray())
            .andExpect(jsonPath("$.creditor_accounts[0].creditorAccountId").value("CA123456"))
            .andExpect(jsonPath("$.creditor_accounts[0].accountNumber").value("ACC-987654"))
            .andExpect(jsonPath("$.creditor_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.creditor_accounts[0].organisationName")
                           .value("Acme Corp Ltd"))
            .andExpect(jsonPath("$.creditor_accounts[0].addressLine1").value("123 Main Street"))
            .andExpect(jsonPath("$.creditor_accounts[0].postcode").value("AB1 2CD"))
            .andExpect(jsonPath("$.creditor_accounts[0].businessUnitName")
                           .value("Finance Department"))
            .andExpect(jsonPath("$.creditor_accounts[0].businessUnitId").value("BU123"))
            .andExpect(jsonPath("$.creditor_accounts[0].defendantAccountId").value("DA001"))
            .andExpect(jsonPath("$.creditor_accounts[0].accountBalance").value(1000.0))
            .andExpect(jsonPath("$.defendant[0].organisationName")
                           .value("Example Holdings PLC"));

        jsonSchemaValidationService.validateOrError(body, GET_HEADER_SUMMARY_RESPONSE);
    }

    void postSearchMinorCreditorImpl_500Error(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder().businessUnitIds(List.of(
            101,
            202,
            303
        )).activeAccountsOnly(false).accountNumber("FAIL").build();

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "search")
                                                          .contentType(MediaType.APPLICATION_JSON)
                                                          .content(
            objectMapper.writeValueAsString(search)).header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(
            status().is5xxServerError()).andExpect(
                content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }
}
