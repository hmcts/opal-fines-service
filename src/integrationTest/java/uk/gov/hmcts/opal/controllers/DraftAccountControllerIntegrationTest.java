package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allFinesPermissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noFinesPermissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;
import uk.gov.hmcts.opal.logging.integration.service.LoggingService;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;


@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.DraftAccountControllerIntegrationTest")
@Sql(scripts = "classpath:db/insertData/insert_into_draft_accounts.sql", executionPhase = BEFORE_TEST_CLASS)
@DisplayName("DraftAccountController Integration Tests")
class DraftAccountControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/draft-accounts";
    private static final String GET_DRAFT_ACCOUNT_RESPONSE =
        SchemaPaths.DRAFT_ACCOUNT + "/getDraftAccountResponse.json";
    private static final String GET_DRAFT_ACCOUNTS_RESPONSE =
        SchemaPaths.DRAFT_ACCOUNT + "/getDraftAccountsResponse.json";

    private static final Short BU_ID = (short)73;

    @MockitoBean
    UserStateService userStateService;

    @MockitoBean
    LoggingService loggingService;

    @MockitoSpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    @DisplayName("Get Draft Account by ID [@PO-973, @PO-559]")
    void testGetDraftAccountById_success() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/1")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountById_success: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"0\""))
            .andExpect(jsonPath("$.draft_account_id").value(1))
            .andExpect(jsonPath("$.business_unit_id").value(77))
            .andExpect(jsonPath("$.account_type").value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.submitted_by").value("user_001"))
            .andExpect(jsonPath("$.account_status").value("Submitted"))
            .andExpect(jsonPath("$.account_status_date").value("2024-12-10T16:27:01.023126Z"))
            .andExpect(jsonPath("$.submitted_by_name").value("John Smith"))
            .andExpect(jsonPath("$.version").doesNotExist())
            .andExpect(jsonPath("$.status_message").doesNotExist())
            .andExpect(jsonPath("$.validated_by_name").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNT_RESPONSE);
    }

    @Test
    @DisplayName("Get draft accounts summaries - No query params [@PO-973, @PO-606]")
    void testGetDraftAccountsSummaries_noParams() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions =  mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_noParams: body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(33))
            .andExpect(jsonPath("$.summaries[2].draft_account_id").value(3))
            .andExpect(jsonPath("$.summaries[2].business_unit_id").value(73))
            .andExpect(jsonPath("$.summaries[2].account_type")
                .value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.summaries[2].submitted_by").value("user_003"))
            .andExpect(jsonPath("$.summaries[2].account_status").value("Publishing Failed"));

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get draft accounts summaries - Param business unit [@PO-973, @PO-606]")
    void testGetDraftAccountsSummaries_paramBusinessUnit() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .param("business_unit", BU_ID.toString())
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_paramBusinessUnit: body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(3))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(73))
            .andExpect(jsonPath("$.summaries[0].account_type")
                .value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.summaries[0].submitted_by").value("user_003"))
            .andExpect(jsonPath("$.summaries[0].account_status").value("Publishing Failed"));

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get draft accounts summaries - Params for status dates from and to")
    void testGetDraftAccountsSummaries_paramStatusDate() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        LocalDate fromDate = LocalDate.of(2025, 02, 03);
        LocalDate toDate = LocalDate.of(2025, 02, 03);

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .param("account_status_date_from", fromDate.toString())
            .param("account_status_date_to", toDate.toString())
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_paramStatusDate: body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(3))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(7))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(78))
            .andExpect(jsonPath("$.summaries[0].account_type")
                .value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.summaries[0].submitted_by").value("user_003"))
            .andExpect(jsonPath("$.summaries[0].account_status").value("Submitted"));

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get draft accounts summaries - Param status [@PO-973, @PO-606]")
    void testGetDraftAccountsSummaries_paramStatus() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .param("status", DraftAccountStatus.PUBLISHING_FAILED.name())
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_paramStatus: body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(4))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(3))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(73))
            .andExpect(jsonPath("$.summaries[0].account_type")
                .value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.summaries[0].submitted_by").value("user_003"))
            .andExpect(jsonPath("$.summaries[0].account_status").value("Publishing Failed"));

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get draft accounts summaries - Param submitted by [@PO-973, @PO-606]")
    void testGetDraftAccountsSummaries_paramSubmittedBy() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .param("submitted_by", "user_002")
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_paramSubmittedBy: body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(2))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(77))
            .andExpect(jsonPath("$.summaries[0].account_type")
                .value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.summaries[0].submitted_by").value("user_002"))
            .andExpect(jsonPath("$.summaries[0].account_status").value("Submitted"));

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get draft accounts summaries - Param not submitted by [@PO-973, @PO-832]")
    void testGetDraftAccountsSummaries_paramNotSubmittedBy() throws Exception {


        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .param("not_submitted_by", "user_003")
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_paramNotSubmittedBy: body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(24))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(1))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(77))
            .andExpect(jsonPath("$.summaries[1].draft_account_id").value(2))
            .andExpect(jsonPath("$.summaries[1].business_unit_id").value(77))
            .andExpect(jsonPath("$.summaries[2].draft_account_id").value(5))
            .andExpect(jsonPath("$.summaries[2].business_unit_id").value(78));

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get draft account summaries - Single business unit permission, no filter [@PO-973, @PO-829]")
    void testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits1() throws Exception {

        UserState user = permissionUser(BU_ID, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits1: body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(3))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(73));

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get draft account summaries - Multiple BU permissions, filtered to BU 73 [@PO-973, @PO-829]")
    void testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits2() throws Exception {

        UserState user = permissionUser(new Short[] {BU_ID, (short)77}, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .param("business_unit", "73")
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits2: body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(3))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(73));

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get draft account summaries - Multiple BU permissions, no filter [@PO-973, @PO-829]")
    void testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits3() throws Exception {

        UserState user = permissionUser(new Short[] {BU_ID, (short)77}, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits3: body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(23))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(1))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(77))
            .andExpect(jsonPath("$.summaries[1].draft_account_id").value(2))
            .andExpect(jsonPath("$.summaries[1].business_unit_id").value(77))
            .andExpect(jsonPath("$.summaries[2].draft_account_id").value(3))
            .andExpect(jsonPath("$.summaries[2].business_unit_id").value(73));

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Search draft accounts - POST with draftAccountId - Should return matching draft account"
        + " [@PO-973, @PO-559]")
    void testSearchDraftAccountsPost() throws Exception {

        ResultActions resultActions = mockMvc.perform(post(URL_BASE + "/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"draftAccountId\":\"1\"}"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testSearchDraftAccountsPost: body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].draft_account_id").value(1))
            .andExpect(jsonPath("$[0].business_unit_id").value(77));

    }


    @Test
    void testSearchDraftAccountsPost_whenDraftAccountDoesNotExist() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(URL_BASE + "/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"draftAccountId\":\"200\"}"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testSearchDraftAccountsPost_whenDraftAccountDoesNotExist: body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk()).andExpect(content().string("[]"));
    }

    @Test
    @DisplayName("Delete draft accounts [@PO-973, @PO-591]")
    void testDeleteDraftAccountById_success() throws Exception {

        ResultActions resultActions = mockMvc.perform(delete(URL_BASE + "/4")
            .header("If-Match", "0")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testDeleteDraftAccountById_success: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Draft Account '4' deleted"));
    }

    @Test
    @DisplayName("Replace draft account - Should return updated draft account [@PO-973, @PO-746]")
    void testReplaceDraftAccount_success() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());
        String requestBody = validReplaceRequestBody(0L);
        log.info(":testReplaceDraftAccount_success: Request Body:\n{}", ToJsonString.toPrettyJson(requestBody));

        ResultActions resultActions = mockMvc.perform(put(URL_BASE + "/" + 5)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testReplaceDraftAccount_success: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(5))
            .andExpect(jsonPath("$.business_unit_id").value(78))
            .andExpect(jsonPath("$.submitted_by").value("BUUID1"))
            .andExpect(jsonPath("$.account_type").value("Fines"))
            .andExpect(jsonPath("$.account_status").value("Resubmitted"))
            .andExpect(jsonPath("$.timeline_data").isArray());

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNT_RESPONSE);

    }

    private String validRawJsonCreateRequestBody() {
        AddDraftAccountRequestDto dto = AddDraftAccountRequestDto.builder()
            .businessUnitId((short) 78)
            .submittedBy("BUUID1")
            .submittedByName("John")
            .account(validAccountJsonString())
            .accountType("Fines")
            .timelineData(validTimelineDataString())
            .build();

        try {
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize AddDraftAccountRequestDto", e);
        }
    }

    private static String validAccountJsonString() {
        return """
            {
              "account_type": "Fine",
              "defendant_type": "Adult",
              "originator_name": "Police Force",
              "originator_id": 12345,
              "enforcement_court_id": 101,
              "payment_card_request": true,
              "account_sentence_date": "2023-12-01",
              "defendant": {
                "company_flag": false,
                "surname": "LNAME",
                "address_line_1": "123 Main Street"
              },
              "offences": [],
              "payment_terms": {
                "payment_terms_type_code": "P",
                "effective_date": "2023-11-01",
                "instalment_period": "M",
                "lump_sum_amount": 1000.00,
                "instalment_amount": 200.00,
                "default_days_in_jail": 5
              }
            }
            """;
    }

    private static String validTimelineDataString() {
        return """
            [
              {
                "username": "johndoe123",
                "status": "Active",
                "status_date": "2023-11-01",
                "reason_text": "Valid reason"
              }
            ]
            """;
    }

    @Test
    @DisplayName("Create draft account - POST with valid request - Should return newly created account "
        + "[@PO-973, @PO-591]")
    void testPostDraftAccount_permission() throws Exception {

        String validRequestBody = validRawJsonCreateRequestBody();
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(post(URL_BASE)
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequestBody));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDraftAccount_permission: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.submitted_by").value("BUUID1"))
            .andExpect(jsonPath("$.account_type").value("Fines"))
            .andExpect(jsonPath("$.account_status").value("Submitted"))
            .andExpect(jsonPath("$.account.defendant.surname")
                .value("LNAME"));
    }

    @Test
    @DisplayName("Should return 400 when submitted_by_name is blank")
    void shouldReturn400WhenSubmittedByNameIsBlank() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"submitted_by_name\": \"John\"", "\"submitted_by_name\": \"\"");

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        mockMvc.perform(post(URL_BASE)
                .header("Authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when submitted_by is blank")
    void shouldReturn400WhenSubmittedByIsBlank() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"submitted_by\": \"BUUID1\"", "\"submitted_by\": \"\"");

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        mockMvc.perform(post(URL_BASE)
                .header("Authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when account_type is blank")
    void shouldReturn400WhenAccountTypeIsBlank() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"account_type\": \"Fines\"", "\"account_type\": \"\"");

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        mockMvc.perform(post(URL_BASE)
                .header("Authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Update draft account - Should return updated account details [@PO-973, @PO-745]")
    void testUpdateDraftAccount_success() throws Exception {
        Long draftAccountId = 8L; // not touched by any other PATCH/PUT test
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending","A")));

        String response = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testUpdateDraftAccount_success: Response body:\n{}", ToJsonString.toPrettyJson(response));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"2\""))
            .andExpect(jsonPath("$.draft_account_id").value(draftAccountId))
            .andExpect(jsonPath("$.business_unit_id").value(65))
            .andExpect(jsonPath("$.account_status").value("Published"))
            .andExpect(jsonPath("$.timeline_data[0].username").value("johndoe456"));

        jsonSchemaValidationService.validateOrError(response, GET_DRAFT_ACCOUNT_RESPONSE);
    }

    @Test
    @DisplayName("Update draft account - If-Match Conflict [@PO-2117]")
    void testUpdateDraftAccount_conflict() throws Exception {
        Long draftAccountId = 6L;
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "\"9999999\"")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending","A")));

        String response = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testUpdateDraftAccount_success: Response body:\n{}", ToJsonString.toPrettyJson(response));

        resultActions.andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.resourceType").value("uk.gov.hmcts.opal.entity.draft.DraftAccountEntity"))
            .andExpect(jsonPath("$.resourceId").value("6"))
            .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    @DisplayName("Patch draft account - user with CHECK_VALIDATE permission should succeed [@PO-1820]")
    void testPatchDraftAccount_withCheckValidatePermission_shouldSucceed() throws Exception {
        Long draftAccountId = 7L; // not touched by any other PATCH/PUT test
        UserState user = permissionUser((short)78, FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("78","Rejected","A")));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPatchDraftAccount_withCheckValidatePermission_shouldSucceed: Response body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"1\""))
            .andExpect(jsonPath("$.draft_account_id").value(draftAccountId))
            .andExpect(jsonPath("$.account_status").value("Rejected"))
            .andExpect(jsonPath("$.timeline_data[0].username").value("johndoe456"));
    }

    @Test
    @DisplayName("Patch draft account - user with Publish Pending permission should succeed [@PO-991]")
    void testPatchDraftAccount_withPublishPending_shouldSucceed() throws Exception {
        Long draftAccountId = 9L; // not touched by any other PATCH/PUT test
        UserState user = permissionUser((short)65, FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending","D")));

        String response = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPatchDraftAccount_withPublishPending_shouldSucceed: PATCH Response body:\n{}",
            ToJsonString.toPrettyJson(response));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"2\""))
            .andExpect(jsonPath("$.draft_account_id").value(draftAccountId))
            .andExpect(jsonPath("$.account_status").value("Published"))
            .andExpect(jsonPath("$.timeline_data[0].username").value("johndoe456"));
    }


    @Test
    @DisplayName("Patch draft account - user with CREATE_MANAGE permission should be forbidden [@PO-1820]")
    void testPatchDraftAccount_withCreateManagePermission_shouldFail403() throws Exception {
        Long draftAccountId = 6L;
        UserState user = permissionUser((short)78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("78", "Publishing Pending","PO1820")));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPatchDraftAccount_withCreateManagePermission_shouldFail403: Response body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.status").value(403));
    }



    @Test
    @DisplayName("Get draft account by ID - User with wrong permission [@PO-973, @PO-828]")
    void testGetDraftAccountById_trap403Response_wrongPermission() throws Exception {

        UserState userState = permissionUser(BU_ID, FinesPermission.COLLECTION_ORDER);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);

        ResultActions actions = mockMvc.perform(get(URL_BASE + "/2")
            .header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountById_trap403Response_wrongPermission: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value(
                "You do not have permission to access this resource"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("Get draft account by ID - user with wrong permission for business unit [@PO-973, @PO-828]")
    void testGetDraftAccountById_trap403Response_wrongBusinessUnit() throws Exception {

        UserState userState = permissionUser((short)005, FinesPermission.DRAFT_ACCOUNT_PERMISSIONS);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);

        ResultActions resultActions = mockMvc.perform(
            get(URL_BASE + "/2")
                .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountById_trap403Response_wrongBusinessUnit: Response body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value(
                "You do not have permission to access this resource"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("Get draft account by ID - user with no permission [@PO-973, @PO-828]")
    void testGetDraftAccountsSummaries_trap403Response_noPermission() throws Exception {
        final Short businessId = (short)1;

        UserState user = permissionUser(businessId, FinesPermission.COLLECTION_ORDER);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .param("business_unit", businessId.toString())
            .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("Get draft account by ID - Should return 404 Not Found [@PO-973, @PO-690]")
    void testGetDraftAccountById_trap404Response() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        mockMvc.perform(get(URL_BASE + "/99")
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("Get draft account by ID - Should return 406 Not Acceptable [@PO-973, @PO-690]")
    void testGetDraftAccountById_trap406Response() throws Exception {
        mockMvc.perform(get(URL_BASE + "/99")
                .header("Authorization", "Bearer " + "some_value")
                .accept("application/xml"))
            .andExpect(status().isNotAcceptable());
    }

    @Test
    @DisplayName("Get draft account summaries - Should return 400 Bad Request [@PO-973, @PO-647]")
    void testGetDraftAccountsSummaries_trap400Response() throws Exception {
        final Short businessId = (short)1;

        UserState user = permissionUser(businessId, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .param("business_unit", businessId.toString())
            .param("submitted_by", "Dave")
            .param("not_submitted_by", "Tony")
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_trap400Response: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail")
                .value("Invalid arguments were provided in the request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.type")
                .value("https://hmcts.gov.uk/problems/illegal-argument"));
    }

    @Test
    @DisplayName("Replace draft account - user with no permission [@PO-973, @PO-830]")
    void testReplaceDraftAccount_trap403Response_noPermission() throws Exception {
        Long draftAccountId = 241L;

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noFinesPermissionUser());

        mockMvc.perform(put(URL_BASE + "/" + draftAccountId)
                .header("authorization", "Bearer some_value")
                .header("If-Match", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreateRequestBody()))
            .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("Update draft account - user with no permission [@PO-973, @PO-831]")
    void testUpdateDraftAccount_trap403Response_noPermission() throws Exception {
        Long draftAccountId = 241L;
        String requestBody = "            {\n"
            + "                \"account_status\": \"PENDING\",\n"
            + "                \"validated_by\": \"BUUID1\",\n"
            + "                \"business_unit_id\": 5,\n"
            + "                \"timeline_data\": "
            + validTimelineDataJson()
            + "\n"
            + "            }";

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noFinesPermissionUser());

        mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
                .header("authorization", "Bearer some_value")
                .header("If-Match", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("Create draft account - Should return 400 Bad Request [@PO-973, @PO-691]")
    void testPostDraftAccount_trap400Response() throws Exception {

        String expectedErrorMessageStart =
            "JSON Schema Validation Error: Validating against JSON schema 'addDraftAccountRequest.json',"
                + " found 15 validation errors:";

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(post(URL_BASE)
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidCreateRequestBody()));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDraftAccount_trap400Response: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").value("The request does not conform to the required JSON schema"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/json-schema-validation"));
    }

    @Test
    @DisplayName("Create draft account - user with no permission [@PO-973, @PO-827]")
    void testPostDraftAccount_trap403Response_noPermission() throws Exception {

        String validRequestBody = validCreateRequestBody();
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(post(URL_BASE)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequestBody));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDraftAccount_trap403Response_noPermission: Response body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value(
                "You do not have permission to access this resource"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("Create draft account - user with wrong permission [@PO-973, @PO-827]")
    void testPostDraftAccount_trap403Response_wrongPermission() throws Exception {

        String validRequestBody = validCreateRequestBody();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            permissionUser((short)5, FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS, FinesPermission.ACCOUNT_ENQUIRY));

        ResultActions resultActions = mockMvc.perform(post(URL_BASE)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequestBody));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDraftAccount_trap403Response_wrongPermission: Response body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value(
                "You do not have permission to access this resource"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("Create draft account - Should create and call PDPLLoggingService")
    void testPostDraftAccount_success_and_pdplServiceCalled() throws Exception {

        // arrange: request body from your helper
        String validRequestBody = validPostRequestBody(); // reuse your helper

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        // act: perform POST
        ResultActions resultActions = mockMvc.perform(post(URL_BASE)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequestBody));

        // assert response (controller returned 201 in your run)
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // verify PDPLLoggingService was called with a DraftAccountEntity
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor = ArgumentCaptor.forClass(
            PersonalDataProcessingLogDetails.class);

        verify(loggingService, times(2)).personalDataAccessLogAsync(captor.capture());

        PersonalDataProcessingLogDetails pdpl = captor.getValue();
        assertNotNull(pdpl);

        assertNotNull(pdpl.getCreatedBy());
        assertEquals("BUUID1", pdpl.getCreatedBy().getIdentifier()); // adapt to your ParticipantIdentifier API

        assertEquals("Submit Draft Account - Minor Creditor", pdpl.getBusinessIdentifier());

        OffsetDateTime createdAt = pdpl.getCreatedAt();
        assertNotNull(createdAt);

        assertEquals(PersonalDataProcessingCategory.COLLECTION, pdpl.getCategory()); // adapt to expected enum

        assertNull(pdpl.getRecipient());

        List<ParticipantIdentifier> individuals = pdpl.getIndividuals();
        assertNotNull(individuals);
        assertEquals(1, individuals.size());
        assertEquals("201", individuals.getFirst().getIdentifier());
    }

    //CEP 1 CEP1 - Invalid Request Payload (400)
    @ParameterizedTest
    @MethodSource("endpointsWithInvalidBodiesProvider")
    void methodsShouldReturn400_whenRequestPayloadIsInvalid(
        MockHttpServletRequestBuilder requestBuilder, String requestBody) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        mockMvc.perform(requestBuilder
                .header("Authorization", "Bearer some_value")
                .header("Accept", "application/json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> endpointsWithInvalidBodiesProvider() {
        return Stream.of(Arguments.of(post(URL_BASE), invalidCreateRequestBody()),
            Arguments.of(put(URL_BASE + "/1"), invalidCreateRequestBody()),
            Arguments.of(patch(URL_BASE + "/1"), invalidCreateRequestBody())
        );
    }

    //CEP3 - Not Authorised to perform the requested action (403)
    @ParameterizedTest
    @MethodSource("testCasesRequiringAuthorizationProvider")
    void methodsShouldReturn403_whenUserLacksPermission(
        MockHttpServletRequestBuilder requestBuilder, String requestBody) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(requestBuilder
            .header("Authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":methodsShouldReturn403_whenUserLacksPermission: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    private static Stream<Arguments> testCasesRequiringAuthorizationProvider() {
        return Stream.of(
            Arguments.of(post(URL_BASE), validCreateRequestBody()),
            Arguments.of(put(URL_BASE + "/1"), validCreateRequestBody()),
            Arguments.of(patch(URL_BASE + "/1"), validUpdateRequestBody("78", "Publishing Pending","B")),
            Arguments.of(get(URL_BASE), "")  // GET endpoints with empty body
        );
    }

    //CEP4 - Resource Not Found (404) - applies to GET PUT PATCH & DELETE
    @ParameterizedTest
    @MethodSource("testCasesForResourceNotFoundProvider")
    void methodsShouldReturn404_whenResourceNotFound(
        MockHttpServletRequestBuilder requestBuilder, String requestBody) throws Exception {
        // Set up a non-existent ID
        long nonExistentId = 999L;

        // Mock the service behavior
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        mockMvc.perform(requestBuilder
                .header("Authorization", "Bearer some_value")
                .header("If-Match", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound());
    }

    private static Stream<Arguments> testCasesForResourceNotFoundProvider() {
        return Stream.of(
            Arguments.of(get(URL_BASE + "/999"), ""),
            Arguments.of(put(URL_BASE + "/999"), validCreateRequestBody()),
            Arguments.of(patch(URL_BASE + "/999"), validUpdateRequestBody("78", "Publishing Pending","C"))
        );
    }

    //CEP5 - Unsupported Content Type for Response (406)
    @ParameterizedTest
    @MethodSource("testCasesWithValidBodiesProvider")
    void methodsShouldReturn406_whenAcceptHeaderIsNotSupported(
        MockHttpServletRequestBuilder requestBuilder, String requestBody) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());
        mockMvc.perform(requestBuilder
                .header("Authorization", "Bearer some_value")
                .header("Accept", "application/xml")
                .header("If-Match", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotAcceptable());
    }

    @Test
    @DisplayName("Re-submit - Defendant only -> Re-submit Draft Account - Defendant PDPL")
    void testResubmitDraftAccount_pdpl_defendantOnly() throws Exception {
        final long draftIdAccount = 105L;

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftIdAccount)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending", "X")));

        String response = resultActions.andReturn().getResponse().getContentAsString();

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(draftIdAccount));

        jsonSchemaValidationService.validateOrError(response, GET_DRAFT_ACCOUNT_RESPONSE);

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

        // For defendant-only we expect a single PDPL call (Re-submit Draft Account - Defendant)
        verify(loggingService, timeout(2000).times(1)).personalDataAccessLogAsync(captor.capture());

        PersonalDataProcessingLogDetails pdpl = captor.getValue();
        assertNotNull(pdpl);

        // Full contents assertions (deterministic fields)
        assertEquals("Re-submit Draft Account - Defendant", pdpl.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, pdpl.getCategory());
        assertNull(pdpl.getRecipient());
        assertNotNull(pdpl.getCreatedAt());
        assertNotNull(pdpl.getCreatedBy());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, pdpl.getCreatedBy().getType());
        assertNotNull(pdpl.getIndividuals());
        assertEquals(1, pdpl.getIndividuals().size());
        assertEquals(Long.toString(draftIdAccount), pdpl.getIndividuals().getFirst().getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, pdpl.getIndividuals().getFirst().getType());
    }

    @Test
    @DisplayName("Re-submit - pgToPay -> Parent or Guardian then Defendant PDPLs (order)")
    void testResubmitDraftAccount_pdpl_parentOrGuardianThenDefendant() throws Exception {
        final long draftIdAccount = 104L;

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftIdAccount)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending", "Y")));

        String response = resultActions.andReturn().getResponse().getContentAsString();

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(draftIdAccount));

        jsonSchemaValidationService.validateOrError(response, GET_DRAFT_ACCOUNT_RESPONSE);

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

        // Expect two calls: Parent or Guardian, then Defendant
        verify(loggingService, timeout(2000).times(2)).personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> calls = captor.getAllValues();
        assertEquals(2, calls.size());

        PersonalDataProcessingLogDetails first = calls.get(0);
        assertNotNull(first);
        assertEquals("Re-submit Draft Account - Parent or Guardian", first.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, first.getCategory());
        assertNull(first.getRecipient());
        assertNotNull(first.getCreatedAt());
        assertEquals(Long.toString(draftIdAccount), first.getIndividuals().getFirst().getIdentifier());

        PersonalDataProcessingLogDetails second = calls.get(1);
        assertNotNull(second);
        assertEquals("Re-submit Draft Account - Defendant", second.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, second.getCategory());
        assertNull(second.getRecipient());
        assertNotNull(second.getCreatedAt());
        assertEquals(Long.toString(draftIdAccount), second.getIndividuals().getFirst().getIdentifier());
    }

    @Test
    @DisplayName("Re-submit - adultOrYouthOnly WITH minor -> Defendant + Minor Creditor PDPLs (order)")
    void testResubmitDraftAccount_pdpl_defendantAndMinor() throws Exception {
        final long draftIdAccount = 8L; // previously used in your suite; confirm or replace if needed

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftIdAccount)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "2")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending", "A")));

        String response = resultActions.andReturn().getResponse().getContentAsString();

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"4\""))
            .andExpect(jsonPath("$.draft_account_id").value(draftIdAccount))
            .andExpect(jsonPath("$.business_unit_id").value(65));

        jsonSchemaValidationService.validateOrError(response, GET_DRAFT_ACCOUNT_RESPONSE);

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

        // Expect two calls (Defendant then Minor Creditor)
        verify(loggingService, timeout(2000).times(2))
            .personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> calls = captor.getAllValues();
        assertEquals(2, calls.size(), "expected two PDPL log calls");

        // Defendant call
        PersonalDataProcessingLogDetails defendantCall = calls.get(0);
        assertNotNull(defendantCall);
        assertEquals("Re-submit Draft Account - Defendant", defendantCall.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, defendantCall.getCategory());
        assertNull(defendantCall.getRecipient());
        assertNotNull(defendantCall.getCreatedAt());
        assertEquals(Long.toString(draftIdAccount), defendantCall.getIndividuals().getFirst().getIdentifier());

        // Minor Creditor call
        PersonalDataProcessingLogDetails minorCall = calls.get(1);
        assertNotNull(minorCall);
        assertEquals("Re-submit Draft Account - Minor Creditor", minorCall.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, minorCall.getCategory());
        assertNull(minorCall.getRecipient());
        assertNotNull(minorCall.getCreatedAt());
        assertEquals(Long.toString(draftIdAccount), minorCall.getIndividuals().getFirst().getIdentifier());
    }

    @Test
    @DisplayName("Update draft account (id=103) - company -> no PDPL logging occurs")
    void testUpdateDraftAccount_pdpl_id103_company_noPdpl() throws Exception {
        Long draftAccountId = 103L;

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending", "B")));

        String response = resultActions.andReturn().getResponse().getContentAsString();

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"2\""))
            .andExpect(jsonPath("$.draft_account_id").value(draftAccountId))
            .andExpect(jsonPath("$.business_unit_id").value(65))
            .andExpect(jsonPath("$.account_status").value("Published"))
            .andExpect(jsonPath("$.timeline_data[0].username").value("johndoe456"));

        jsonSchemaValidationService.validateOrError(response, GET_DRAFT_ACCOUNT_RESPONSE);

        verify(loggingService, times(0)).personalDataAccessLogAsync(any());

    }

    private static Stream<Arguments> testCasesWithValidBodiesProvider() {
        return Stream.of(Arguments.of(post(URL_BASE), validCreateRequestBody()),
            Arguments.of(put(URL_BASE + "/1"), "{}"),
            Arguments.of(patch(URL_BASE + "/1"), "{}"),
            Arguments.of(get(URL_BASE + "/1"), "{}")
        );
    }

    private final String validAccountJson() {
        return """
            {
              "account_type": "Fine",
              "defendant_type": "Adult",
              "originator_name": "Police Force",
              "originator_id": 12345,
              "enforcement_court_id": 101,
              "collection_order_made": true,
              "collection_order_made_today": false,
              "payment_card_request": true,
              "account_sentence_date": "2023-12-01",
              "defendant": {
                "company_flag": true,
                "company_name": "company",
                "dob": "1985-04-15",
                "address_line_1": "123 Elm Street",
                "address_line_2": "Suite 45",
                "post_code": "AB1 2CD",
                "telephone_number_home": "0123456789",
                "telephone_number_mobile": "07712345678",
                "email_address_1": "john.doe@example.com",
                "national_insurance_number": "AB123456C",
                "nationality_1": "British",
                "occupation": "Engineer",
                "debtor_detail": {
                  "document_language": "English",
                  "hearing_language": "English",
                  "vehicle_make": "Toyota",
                  "vehicle_registration_mark": "ABC123",
                  "aliases": [
                    {
                      "alias_forenames": "Jon",
                      "alias_surname": "Smith"
                    }
                  ]
                }
              },
              "offences": [
                {
                  "date_of_sentence": "2023-11-15",
                  "imposing_court_id": 202,
                  "offence_id": 1234,
                  "impositions": [
                    {
                      "result_id": "1",
                      "amount_imposed": 500.00,
                      "amount_paid": 200.00,
                      "major_creditor_id": 999
                    }
                  ]
                }
              ],
              "payment_terms": {
                "payment_terms_type_code": "P",
                "effective_date": "2023-11-01",
                "instalment_period": "M",
                "lump_sum_amount": 1000.00,
                "instalment_amount": 200.00,
                "default_days_in_jail": 5
              },
              "account_notes": [
                {
                  "account_note_serial": 1,
                  "account_note_text": "Defendant requested an installment plan.",
                  "note_type": "AC"
                }
              ]
            }""";
    }


    private static String validCreateRequestBody() {
        return """
            {
              "business_unit_id": 78,
              "submitted_by": "BUUID1",
              "submitted_by_name": "John",
              "account": {
                "account_type": "Fine",
                "defendant_type": "Adult",
                "originator_name": "Police Force",
                "originator_id": 12345,
                "enforcement_court_id": 101,
                "collection_order_made": true,
                "collection_order_made_today": false,
                "payment_card_request": true,
                "account_sentence_date": "2023-12-01",
                "defendant": {
                  "company_flag": false,
                  "title": "Mr",
                  "surname": "LNAME",
                  "forenames": "John",
                  "dob": "1985-04-15",
                  "address_line_1": "123 Elm Street",
                  "address_line_2": "Suite 45",
                  "post_code": "AB1 2CD",
                  "telephone_number_home": "0123456789",
                  "telephone_number_mobile": "07712345678",
                  "email_address_1": "john.doe@example.com",
                  "national_insurance_number": "AB123456C",
                  "nationality_1": "British",
                  "occupation": "Engineer",
                  "debtor_detail": {
                    "document_language": "English",
                    "hearing_language": "English",
                    "vehicle_make": "Toyota",
                    "vehicle_registration_mark": "ABC123",
                    "aliases": [
                      {
                        "alias_forenames": "Jon",
                        "alias_surname": "Smith"
                      }
                    ]
                  }
                },
                "offences": [
                  {
                    "date_of_sentence": "2023-11-15",
                    "imposing_court_id": 202,
                    "offence_id": 1234,
                    "impositions": [
                      {
                        "result_id": "1",
                        "amount_imposed": 500.00,
                        "amount_paid": 200.00,
                        "major_creditor_id": 999
                      }
                    ]
                  }
                ],
                "payment_terms": {
                  "payment_terms_type_code": "P",
                  "effective_date": "2023-11-01",
                  "instalment_period": "M",
                  "lump_sum_amount": 1000.00,
                  "instalment_amount": 200.00,
                  "default_days_in_jail": 5
                },
                "account_notes": [
                  {
                    "account_note_serial": 1,
                    "account_note_text": "Defendant requested an installment plan.",
                    "note_type": "AC"
                  }
                ]
              },
              "account_type": "Fines",
              "account_status": "Submitted",
              "version": 0,
              "timeline_data": [
                {
                  "username": "johndoe123",
                  "status": "Active",
                  "status_date": "2023-11-01",
                  "reason_text": "Account successfully activated after review."
                },
                {
                  "username": "janedoe456",
                  "status": "Pending",
                  "status_date": "2023-12-05",
                  "reason_text": "Awaiting additional documentation for verification."
                },
                {
                  "username": "mikebrown789",
                  "status": "Suspended",
                  "status_date": "2023-10-15",
                  "reason_text": "Violation of terms of service."
                }
              ]
            }""";
    }

    private static String validReplaceRequestBody(Long version) {
        return """
            {
              "business_unit_id": 78,
              "submitted_by": "BUUID1",
              "submitted_by_name": "John",
              "account": {
                "account_type": "Fine",
                "defendant_type": "Adult",
                "originator_name": "Police Force",
                "originator_id": 12345,
                "enforcement_court_id": 101,
                "collection_order_made": true,
                "collection_order_made_today": false,
                "payment_card_request": true,
                "account_sentence_date": "2023-12-01",
                "defendant": {
                  "company_flag": false,
                  "title": "Mr",
                  "surname": "LNAME",
                  "forenames": "John",
                  "dob": "1985-04-15",
                  "address_line_1": "123 Elm Street",
                  "address_line_2": "Suite 45",
                  "post_code": "AB1 2CD",
                  "telephone_number_home": "0123456789",
                  "telephone_number_mobile": "07712345678",
                  "email_address_1": "john.doe@example.com",
                  "national_insurance_number": "AB123456C",
                  "nationality_1": "British",
                  "occupation": "Engineer",
                  "debtor_detail": {
                    "document_language": "English",
                    "hearing_language": "English",
                    "vehicle_make": "Toyota",
                    "vehicle_registration_mark": "ABC123",
                    "aliases": [
                      {
                        "alias_forenames": "Jon",
                        "alias_surname": "Smith"
                      }
                    ]
                  }
                },
                "offences": [
                  {
                    "date_of_sentence": "2023-11-15",
                    "imposing_court_id": 202,
                    "offence_id": 1234,
                    "impositions": [
                      {
                        "result_id": "1",
                        "amount_imposed": 500.00,
                        "amount_paid": 200.00,
                        "major_creditor_id": 999
                      }
                    ]
                  }
                ],
                "payment_terms": {
                  "payment_terms_type_code": "P",
                  "effective_date": "2023-11-01",
                  "instalment_period": "M",
                  "lump_sum_amount": 1000.00,
                  "instalment_amount": 200.00,
                  "default_days_in_jail": 5
                },
                "account_notes": [
                  {
                    "account_note_serial": 1,
                    "account_note_text": "Defendant requested an installment plan.",
                    "note_type": "AC"
                  }
                ]
              },
              "account_type": "Fines",
              "account_status": "Submitted",
              "version": """ + version
            +
            """
            ,
            "timeline_data": [
              {
                "username": "johndoe123",
                "status": "Active",
                "status_date": "2023-11-01",
                "reason_text": "Account successfully activated after review."
              },
              {
                "username": "janedoe456",
                "status": "Pending",
                "status_date": "2023-12-05",
                "reason_text": "Awaiting additional documentation for verification."
              },
              {
                "username": "mikebrown789",
                "status": "Suspended",
                "status_date": "2023-10-15",
                "reason_text": "Violation of terms of service."
              }
            ]
          }""";
    }

    private static String validUpdateRequestBody(String businessUnit, String status, String delta) {
        return "{\n"
            + "    \"account_status\": \"" + status + "\",\n"
            + "    \"validated_by\": \"BUUID1" + delta + "\",\n"
            + "    \"validated_by_name\": \"" + delta + "\",\n"
            + "    \"business_unit_id\": " + businessUnit + ",\n"
            + "    \"version\": 0,\n"
            + "    \"timeline_data\": " + validTimelineDataJson() + "\n"
            + "}";
    }

    private static String validTimelineDataJson() {
        return """
            [
                {
                    "username": "johndoe456",
                    "status": "Active",
                    "status_date": "2023-11-01",
                    "reason_text": "Account successfully activated after review."
                },
                {
                    "username": "janedoe789",
                    "status": "Pending",
                    "status_date": "2023-12-05",
                    "reason_text": "Awaiting additional documentation for verification."
                },
                {
                    "username": "mikebrown012",
                    "status": "Suspended",
                    "status_date": "2023-10-15",
                    "reason_text": "Violation of terms of service."
                }
            ]""";
    }

    private static String invalidCreateRequestBody() {
        return """
            {
             "invalid_field": "This field shouldn't be here",
             "account": {
                "account_create_request": {
                "defendant": {
                    "company_name": "Company ABC",
                    "surname": "LNAME",
                    "fornames": "FNAME",
                    "dob": "2000-01-01"
                },
             "account": {
                "account_type": "Invalid"
             }
            }
            },
                "business_unit_id": 1
            }""";
    }

    private String validPostRequestBody() {
        return """
            {
              "draft_account_id": 5,
              "created_at": "2025-11-01T10:30:00+00:00",
              "business_unit_id": 78,
              "validated_by": null,
              "account": {
                "account_type": "Fines",
                "defendant_type": "adultOrYouthOnly",
                "originator_name": "LJS",
                "originator_id": 123,
                "prosecutor_case_reference": null,
                "enforcement_court_id": 456,
                "collection_order_made": null,
                "collection_order_made_today": null,
                "collection_order_date": null,
                "suspended_committal_date": null,
                "payment_card_request": false,
                "account_sentence_date": "2025-10-01",
                "defendant": {
                  "company_flag": false,
                  "title": null,
                  "surname": "Smith",
                  "company_name": null,
                  "forenames": "John",
                  "dob": "1985-07-20",
                  "address_line_1": "1 Justice Road",
                  "address_line_2": null,
                  "address_line_3": null,
                  "address_line_4": null,
                  "address_line_5": null,
                  "post_code": "AB1 2CD",
                  "telephone_number_home": null,
                  "telephone_number_business": null,
                  "telephone_number_mobile": "07123456789",
                  "email_address_1": "john.smith@example.com",
                  "email_address_2": null,
                  "national_insurance_number": "QQ123456C",
                  "driving_licence_number": null,
                  "pnc_id": null,
                  "nationality_1": "British",
                  "nationality_2": null,
                  "ethnicity_self_defined": null,
                  "ethnicity_observed": null,
                  "cro_number": null,
                  "occupation": "Engineer",
                  "gender": "M",
                  "custody_status": null,
                  "prison_number": null,
                  "interpreter_lang": null,
                  "debtor_detail": null,
                  "parent_guardian": null
                },
                "offences": [
                  {
                    "date_of_sentence": "2025-10-01",
                    "imposing_court_id": 789,
                    "offence_id": 10,
                    "impositions": [
                      {
                        "result_id": "FINE",
                        "amount_imposed": 100.00,
                        "amount_paid": 0.00,
                        "major_creditor_id": null,
                        "minor_creditor": {
                          "company_flag": false,
                          "title": null,
                          "company_name": null,
                          "surname": "Minor",
                          "forenames": "Alice",
                          "dob": "2010-05-05",
                          "address_line_1": "5 Minor St",
                          "address_line_2": null,
                          "address_line_3": null,
                          "address_line_4": null,
                          "address_line_5": null,
                          "post_code": "MN1 2OP",
                          "telephone": null,
                          "email_address": null,
                          "payout_hold": false,
                          "pay_by_bacs": false,
                          "bank_account_type": null,
                          "bank_sort_code": null,
                          "bank_account_number": null,
                          "bank_account_name": null,
                          "bank_account_ref": null
                        }
                      }
                    ]
                  }
                ],
                "fp_ticket_detail": null,
                "payment_terms": {
                  "payment_terms_type_code": "B",
                  "effective_date": null,
                  "instalment_period": null,
                  "lump_sum_amount": null,
                  "instalment_amount": null,
                  "default_days_in_jail": null,
                  "enforcements": null
                },
                "account_notes": null
              },
              "account_snapshot": null,
              "account_type": "Fines",
              "timeline_data": [
                {
                  "username": "johndoe123",
                  "status": "Active",
                  "status_date": "2025-10-15",
                  "reason_text": "Account created for testing"
                }
              ],
              "submitted_by": "BUUID1",
              "submitted_by_name": "Business User 1"
            }
            
            """;
    }

}