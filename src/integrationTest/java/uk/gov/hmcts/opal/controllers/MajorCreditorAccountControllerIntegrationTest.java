package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.REQUEST_TIMEOUT;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@Sql(scripts = "classpath:db/insertData/insert_into_major_creditor_accounts_at_a_glance.sql",
    executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/deleteData/delete_from_major_creditor_accounts_at_a_glance.sql",
    executionPhase = AFTER_TEST_METHOD)
@Slf4j(topic = "opal.MajorCreditorAccountControllerIntegrationTest")
@DisplayName("Major Creditor Account Controller Integration Tests")
class MajorCreditorAccountControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/major-creditor-accounts";
    private static final String RESPONSE_SCHEMA = SchemaPaths.GET_MAJOR_CREDITOR_ACCOUNT_AT_A_GLANCE_RESPONSE;

    @MockitoBean
    private UserStateService userStateService;

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    @JiraStory("PO-2132")
    @JiraEpic("PO-2122")
    @DisplayName("PO-2132: GET /major-creditor-accounts/{id}/at-a-glance returns 200 for a major creditor account")
    void getAtAGlance_returnsMajorCreditorResponse() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/{id}/at-a-glance", 978010L)
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":getAtAGlance_returnsMajorCreditorResponse: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"7\""))
            .andExpect(jsonPath("$.major_creditor.creditor_account_id").value(978010))
            .andExpect(jsonPath("$.major_creditor.name").value("Major Creditor Services Ltd"))
            .andExpect(jsonPath("$.major_creditor.code").value("MC01"))
            .andExpect(jsonPath("$.major_creditor.pay_by_bacs").value(true))
            .andExpect(jsonPath("$.major_creditor.address.address_line_1").value("1 Credit Lane"))
            .andExpect(jsonPath("$.major_creditor.address.address_line_2").value("Creditville"))
            .andExpect(jsonPath("$.major_creditor.address.address_line_3").value("Credittown"))
            .andExpect(jsonPath("$.major_creditor.address.postcode").value("MC1 1AA"))
            .andExpect(jsonPath("$.major_creditor.account_version").doesNotExist())
            .andExpect(jsonPath("$.major_creditor.bacs_details").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, RESPONSE_SCHEMA);
    }

    @Test
    @JiraStory("PO-2132")
    @JiraEpic("PO-2122")
    @DisplayName(
        "PO-2132: GET /major-creditor-accounts/{id}/at-a-glance returns central fund fields without MJ-only values"
    )
    void getAtAGlance_returnsCentralFundResponse() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/{id}/at-a-glance", 978011L)
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":getAtAGlance_returnsCentralFundResponse: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"3\""))
            .andExpect(jsonPath("$.major_creditor.creditor_account_id").value(978011))
            .andExpect(jsonPath("$.major_creditor.name").value("HM Courts & Tribunals Service"))
            .andExpect(jsonPath("$.major_creditor.code").doesNotExist())
            .andExpect(jsonPath("$.major_creditor.pay_by_bacs").doesNotExist())
            .andExpect(jsonPath("$.major_creditor.address.address_line_1").value("HMCS add 1"))
            .andExpect(jsonPath("$.major_creditor.address.address_line_2").value("HMCS add 2"))
            .andExpect(jsonPath("$.major_creditor.address.address_line_3").value("HMCS add 3"))
            .andExpect(jsonPath("$.major_creditor.address.postcode").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, RESPONSE_SCHEMA);
    }

    @Test
    @JiraStory("PO-2132")
    @JiraEpic("PO-2122")
    @DisplayName("PO-2132: GET /major-creditor-accounts/{id}/at-a-glance returns 200 when permission exists in same BU")
    void getAtAGlance_returnsOkWhenUserHasPermissionInSameBusinessUnit() throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(permissionUser((short) 978, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS));

        mockMvc.perform(get(URL_BASE + "/{id}/at-a-glance", 978010L)
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(header().string("ETag", "\"7\""));
    }

    @Test
    @JiraStory("PO-2132")
    @JiraEpic("PO-2122")
    @DisplayName(
        "PO-2132: GET /major-creditor-accounts/{id}/at-a-glance returns 200 when permission exists in different BU"
    )
    void getAtAGlance_returnsOkWhenUserHasPermissionInDifferentBusinessUnit() throws Exception {
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(permissionUser((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS));

        mockMvc.perform(get(URL_BASE + "/{id}/at-a-glance", 978010L)
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(header().string("ETag", "\"7\""));
    }

    @Test
    @JiraStory("PO-2132")
    @JiraEpic("PO-2122")
    @DisplayName(
        "PO-2132: GET /major-creditor-accounts/{id}/at-a-glance returns consistent body and ETag on repeated requests"
    )
    void getAtAGlance_returnsConsistentBodyAndEtag() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        ResultActions first = mockMvc.perform(get(URL_BASE + "/{id}/at-a-glance", 978010L)
            .header("authorization", "Bearer some_value"));
        ResultActions second = mockMvc.perform(get(URL_BASE + "/{id}/at-a-glance", 978010L)
            .header("authorization", "Bearer some_value"));

        String firstBody = first.andReturn().getResponse().getContentAsString();
        String secondBody = second.andReturn().getResponse().getContentAsString();
        String firstEtag = first.andReturn().getResponse().getHeader("ETag");
        String secondEtag = second.andReturn().getResponse().getHeader("ETag");

        org.junit.jupiter.api.Assertions.assertEquals(firstBody, secondBody);
        org.junit.jupiter.api.Assertions.assertEquals(firstEtag, secondEtag);
    }

    @Test
    @JiraStory("PO-2132")
    @JiraEpic("PO-2122")
    @DisplayName("PO-2132: GET /major-creditor-accounts/{id}/at-a-glance returns 404 for an unknown account")
    void getAtAGlance_returnsNotFoundForUnknownAccount() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        mockMvc.perform(get(URL_BASE + "/{id}/at-a-glance", 999999L)
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @JiraStory("PO-2132")
    @JiraEpic("PO-2122")
    @DisplayName("PO-2132: GET /major-creditor-accounts/{id}/at-a-glance returns 401 when authentication fails")
    void getAtAGlance_returnsUnauthorized() throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(get(URL_BASE + "/{id}/at-a-glance", 978010L))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("Unauthorized"))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @JiraStory("PO-2132")
    @JiraEpic("PO-2122")
    @DisplayName("PO-2132: GET /major-creditor-accounts/{id}/at-a-glance returns 403 when permission is missing")
    void getAtAGlance_returnsForbidden() throws Exception {
        doThrow(new ResponseStatusException(FORBIDDEN, "Forbidden"))
            .when(userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(get(URL_BASE + "/{id}/at-a-glance", 978010L)
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("Forbidden"))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @JiraStory("PO-2132")
    @JiraEpic("PO-2122")
    @DisplayName("PO-2132: GET /major-creditor-accounts/{id}/at-a-glance returns 408 on timeout")
    void getAtAGlance_returnsRequestTimeout() throws Exception {
        doThrow(new ResponseStatusException(REQUEST_TIMEOUT, "Timeout"))
            .when(userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(get(URL_BASE + "/{id}/at-a-glance", 978010L)
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("Timeout"));
    }

    @Test
    @JiraStory("PO-2132")
    @JiraEpic("PO-2122")
    @DisplayName("PO-2132: GET /major-creditor-accounts/{id}/at-a-glance returns 503 when service is unavailable")
    void getAtAGlance_returnsServiceUnavailable() throws Exception {
        doThrow(new ResponseStatusException(SERVICE_UNAVAILABLE, "Gateway down"))
            .when(userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(get(URL_BASE + "/{id}/at-a-glance", 978010L)
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("Gateway down"));
    }

    @Test
    @JiraStory("PO-2132")
    @JiraEpic("PO-2122")
    @DisplayName("PO-2132: GET /major-creditor-accounts/{id}/at-a-glance returns 500 on server error")
    void getAtAGlance_returnsInternalServerError() throws Exception {
        doThrow(new ResponseStatusException(INTERNAL_SERVER_ERROR, "Boom"))
            .when(userStateService).checkForAuthorisedUser(any());

        mockMvc.perform(get(URL_BASE + "/{id}/at-a-glance", 978010L)
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.detail").value("Boom"));
    }
}
