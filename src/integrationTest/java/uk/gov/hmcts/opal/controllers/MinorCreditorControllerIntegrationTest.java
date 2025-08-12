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

    private static final String GET_HEADER_SUMMARY_RESPONSE =
        "opal/postMinorCreditorAccountSearchResponse.json";

    @MockitoBean
    UserStateService userStateService;

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    void postSearchMinorCreditorImpl_Success(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(1))
            .activeAccountsOnly(true)
            .accountNumber("ACC123456")
            .build();

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/search")
                                                          .contentType(MediaType.APPLICATION_JSON)
                                                          .content(objectMapper.writeValueAsString(search))
                                                          .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.creditor_accounts").isArray())
            .andExpect(jsonPath("$.creditor_accounts[0].creditorAccountId").value("CA123"))
            .andExpect(jsonPath("$.creditor_accounts[0].accountNumber").value("100A"))
            .andExpect(jsonPath("$.creditor_accounts[0].organisation").value(true))
            .andExpect(jsonPath("$.creditor_accounts[0].organisationName").value("Sainsco"))
            .andExpect(jsonPath("$.creditor_accounts[0].firstnames").value("Keith"))
            .andExpect(jsonPath("$.creditor_accounts[0].surname").value("Thief"))
            .andExpect(jsonPath("$.creditor_accounts[0].addressLine1").value("1 Main St"))
            .andExpect(jsonPath("$.creditor_accounts[0].postcode").value("AB12 3CD"))
            .andExpect(jsonPath("$.creditor_accounts[0].businessUnitName").value("UnitA"))
            .andExpect(jsonPath("$.creditor_accounts[0].businessUnitId").value("78"))
            .andExpect(jsonPath("$.creditor_accounts[0].defendantAccountId").value("77"))
            .andExpect(jsonPath("$.creditor_accounts[0].accountBalance").value(1000.0));

        jsonSchemaValidationService.validateOrError(body, GET_HEADER_SUMMARY_RESPONSE);
    }

    void postSearchMinorCreditorImpl_500Error(Logger log) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MinorCreditorSearch search = MinorCreditorSearch.builder()
            .businessUnitIds(List.of(101, 202, 303))
            .activeAccountsOnly(false)
            .accountNumber("FAIL")
            .build();

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "search")
                                                          .contentType(MediaType.APPLICATION_JSON)
                                                          .content(objectMapper.writeValueAsString(search))
                                                          .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetHeaderSummary: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().is5xxServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }
}
