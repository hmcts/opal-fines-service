package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.authentication.aspect.LogAuditDetailsAspect;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noPermissionsUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;
import static uk.gov.hmcts.opal.entity.DraftAccountStatus.ERROR_IN_PUBLISHING;

@Slf4j(topic = "opal.DraftAccountControllerIntegrationTest")
@Sql(scripts = "classpath:db/draftAccounts/insert_into_draft_accounts.sql",
    executionPhase = BEFORE_TEST_CLASS)
@DisplayName("DraftAccountController Integration Tests")
class DraftAccountControllerIntegrationTest extends AbstractIntegrationTest {
    private static final String URL_BASE = "/draft-accounts";
    private static final String GET_DRAFT_ACCOUNT_RESPONSE = "getDraftAccountResponse.json";
    private static final String GET_DRAFT_ACCOUNTS_RESPONSE = "getDraftAccountsResponse.json";

    private static final Short BU_ID = (short)73;

    @MockBean
    UserStateService userStateService;

    @MockBean
    LogAuditDetailsAspect logAuditDetailsAspect;

    @MockBean
    AccessTokenService tokenService;

    @SpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;


    @Test
    @DisplayName("Get Draft Account by ID [@PO-973, @PO-559]")
    void testGetDraftAccountById_success() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MvcResult result = mockMvc.perform(get(URL_BASE + "/1")
                                               .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(1))
            .andExpect(jsonPath("$.business_unit_id").value(77))
            .andExpect(jsonPath("$.account_type").value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.submitted_by").value("user_001"))
            .andExpect(jsonPath("$.account_status").value("Submitted"))
            .andExpect(jsonPath("$.account_status_date").value("2024-12-10T16:27:01.023126Z"))
            .andReturn();

        String body = result.getResponse().getContentAsString();

        log.info(":testGetDraftAccountById: Response body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNT_RESPONSE));
    }

    @Test
    @DisplayName("Get draft accounts summaries - No query params [@PO-973, @PO-606]")
    void testGetDraftAccountsSummaries_noParams() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        String body = mockMvc.perform(get(URL_BASE)
                                          .header("authorization", "Bearer some_value")
                                          .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(6))
            .andExpect(jsonPath("$.summaries[2].draft_account_id").value(3))
            .andExpect(jsonPath("$.summaries[2].business_unit_id").value(73))
            .andExpect(jsonPath("$.summaries[2].account_type")
                           .value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.summaries[2].submitted_by").value("user_003"))
            .andExpect(jsonPath("$.summaries[2].account_status").value("Error in publishing"))
            .andReturn().getResponse().getContentAsString();

        log.info(":testGetDraftAccountsSummaries_noParams: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    @DisplayName("Get draft accounts summaries - Param business unit [@PO-973, @PO-606]")
    void testGetDraftAccountsSummaries_paramBusinessUnit() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        String body = mockMvc.perform(get(URL_BASE)
                                          .header("authorization", "Bearer some_value")
                                          .param("business_unit", BU_ID.toString())
                                          .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(3))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(73))
            .andExpect(jsonPath("$.summaries[0].account_type")
                           .value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.summaries[0].submitted_by").value("user_003"))
            .andExpect(jsonPath("$.summaries[0].account_status").value("Error in publishing"))
            .andReturn().getResponse().getContentAsString();

        log.info(":testGetDraftAccountsSummaries_permission: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    @DisplayName("Get draft accounts summaries - Param status [@PO-973, @PO-606]")
    void testGetDraftAccountsSummaries_paramStatus() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        String body = mockMvc.perform(get(URL_BASE)
                                          .header("authorization", "Bearer some_value")
                                          .param("status", ERROR_IN_PUBLISHING.getLabel())
                                          .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(3))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(73))
            .andExpect(jsonPath("$.summaries[0].account_type")
                           .value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.summaries[0].submitted_by").value("user_003"))
            .andExpect(jsonPath("$.summaries[0].account_status").value("Error in publishing"))
            .andReturn().getResponse().getContentAsString();

        log.info(":testGetDraftAccountsSummaries_permission: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    @DisplayName("Get draft accounts summaries - Param submitted by [@PO-973, @PO-606]")
    void testGetDraftAccountsSummaries_paramSubmittedBy() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        String body = mockMvc.perform(get(URL_BASE)
                                          .header("authorization", "Bearer some_value")
                                          .param("submitted_by", "user_002")
                                          .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(2))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(77))
            .andExpect(jsonPath("$.summaries[0].account_type")
                           .value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.summaries[0].submitted_by").value("user_002"))
            .andExpect(jsonPath("$.summaries[0].account_status").value("Submitted"))
            .andReturn().getResponse().getContentAsString();

        log.info(":testGetDraftAccountsSummaries_permission: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    @DisplayName("Get draft accounts summaries - Param not submitted by [@PO-973, @PO-832]")
    void testGetDraftAccountsSummaries_paramNotSubmittedBy() throws Exception {


        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        String body = mockMvc.perform(get(URL_BASE)
                                          .header("authorization", "Bearer some_value")
                                          .param("not_submitted_by", "user_003")
                                          .contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(3))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(1))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(77))
            .andExpect(jsonPath("$.summaries[1].draft_account_id").value(2))
            .andExpect(jsonPath("$.summaries[1].business_unit_id").value(77))
            .andExpect(jsonPath("$.summaries[2].draft_account_id").value(5))
            .andExpect(jsonPath("$.summaries[2].business_unit_id").value(78))
            .andReturn().getResponse().getContentAsString();

        log.info(":testGetDraftAccountsSummaries_permission: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    @DisplayName("Get draft account summaries - Single business unit permission, no filter [@PO-973, @PO-829]")
    void testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits1() throws Exception {

        UserState user = permissionUser(BU_ID, Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        String body =
            mockMvc.perform(get(URL_BASE)
                                .header("authorization", "Bearer some_value")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.summaries[0].draft_account_id").value(3))
                .andExpect(jsonPath("$.summaries[0].business_unit_id").value(73))
                .andReturn().getResponse().getContentAsString();

        log.info(":testGetDraftAccountsSummaries_permission: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    @DisplayName("Get draft account summaries - Multiple BU permissions, filtered to BU 73 [@PO-973, @PO-829]")
    void testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits2() throws Exception {

        UserState user = permissionUser(new Short[] {BU_ID, (short)77}, Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        String body =
            mockMvc.perform(get(URL_BASE)
                                .header("authorization", "Bearer some_value")
                                .param("business_unit", "73")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.summaries[0].draft_account_id").value(3))
                .andExpect(jsonPath("$.summaries[0].business_unit_id").value(73))
                .andReturn().getResponse().getContentAsString();;

        log.info(":testGetDraftAccountsSummaries_permission: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    @DisplayName("Get draft account summaries - Multiple BU permissions, no filter [@PO-973, @PO-829]")
    void testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits3() throws Exception {

        UserState user = permissionUser(new Short[] {BU_ID, (short)77}, Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        String body =
            mockMvc.perform(get(URL_BASE)
                                .header("authorization", "Bearer some_value")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.summaries[0].draft_account_id").value(1))
                .andExpect(jsonPath("$.summaries[0].business_unit_id").value(77))
                .andExpect(jsonPath("$.summaries[1].draft_account_id").value(2))
                .andExpect(jsonPath("$.summaries[1].business_unit_id").value(77))
                .andExpect(jsonPath("$.summaries[2].draft_account_id").value(3))
                .andExpect(jsonPath("$.summaries[2].business_unit_id").value(73))
                .andReturn().getResponse().getContentAsString();

        log.info(":testGetDraftAccountsSummaries_permission: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    @DisplayName("Search draft accounts - POST with draftAccountId - Should return matching draft account"
        + " [@PO-973, @PO-559]")
    void testSearchDraftAccountsPost() throws Exception {

        String body = mockMvc.perform(post(URL_BASE + "/search")
                                          .header("authorization", "Bearer some_value")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content("{\"draftAccountId\":\"1\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].draft_account_id").value(1))
            .andExpect(jsonPath("$[0].business_unit_id").value(77))
            .andReturn().getResponse().getContentAsString();

        log.info(":testSearchDraftAccountsPost: body:\n" + ToJsonString.toPrettyJson(body));
    }


    @Test
    void testSearchDraftAccountsPost_whenDraftAccountDoesNotExist() throws Exception {
        String body = mockMvc.perform(post(URL_BASE + "/search")
                                          .header("authorization", "Bearer some_value")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content("{\"draftAccountId\":\"100\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string("[]"))
            .andReturn().getResponse().getContentAsString();

        log.info(":testSearchDraftAccountsPost_whenDraftAccountDoesNotExist: body:\n"
                     + ToJsonString.toPrettyJson(body));
    }

    @Test
    @DisplayName("Delete draft accounts [@PO-973, @PO-591]")
    void testDeleteDraftAccountById_success() throws Exception {

        MvcResult result = mockMvc.perform(delete(URL_BASE + "/4")
                                               .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Draft Account '4' deleted"))
            .andReturn();

        String body = result.getResponse().getContentAsString();
        log.info(":testGetDraftAccountById: Response body:\n" + ToJsonString.toPrettyJson(body));
    }

    @Test
    @DisplayName("Replace draft account - Should return updated draft account [@PO-973, @PO-746]")
    void testReplaceDraftAccount_success() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());
        String requestBody = validReplaceRequestBody(0L);
        log.info(":testReplaceDraftAccount_success: Request Body:\n{}", ToJsonString.toPrettyJson(requestBody));

        String body  = mockMvc.perform(put(URL_BASE + "/" + 5)
                                           .header("authorization", "Bearer some_value")
                                           .contentType(MediaType.APPLICATION_JSON)
                                           .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(5))
            .andExpect(jsonPath("$.business_unit_id").value(78))
            .andExpect(jsonPath("$.submitted_by").value("BUUID1"))
            .andExpect(jsonPath("$.account_type").value("Fines"))
            .andExpect(jsonPath("$.account_status").value("Resubmitted"))
            .andExpect(jsonPath("$.timeline_data").isArray())
            .andReturn().getResponse().getContentAsString();

        log.info(":testReplaceDraftAccount_success: Response body:\n{}", ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNT_RESPONSE));

    }

    @Test
    @DisplayName("Create draft account - POST with valid request - Should return newly created account "
        + "[@PO-973, @PO-591]")
    void testPostDraftAccount_permission() throws Exception {

        String validRequestBody = validCreateRequestBody();
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MvcResult result = mockMvc.perform(post(URL_BASE)
                                               .header("authorization", "Bearer some_value")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .content(validRequestBody))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.submitted_by").value("BUUID1"))
            .andExpect(jsonPath("$.account_type").value("Fines"))
            .andExpect(jsonPath("$.account_status").value("Submitted"))
            .andExpect(jsonPath("$.account.defendant.surname")
                           .value("LNAME"))
            .andReturn();

        String body = result.getResponse().getContentAsString();

        log.info(":testPostDraftAccount_permission: Response body:\n" + ToJsonString.toPrettyJson(body));
    }

    @Test
    @DisplayName("Update draft account - Should return updated account details [@PO-973, @PO-745]")
    void testUpdateDraftAccount_success() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MvcResult result = mockMvc.perform(patch(URL_BASE + "/" + 6)
                                               .header("authorization", "Bearer some_value")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .content(validUpdateRequestBody("A")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(6))
            .andExpect(jsonPath("$.business_unit_id").value(78))
            .andExpect(jsonPath("$.timeline_data[0].username").value("johndoe456"))
            .andReturn();

        String body = result.getResponse().getContentAsString();
        log.info(":testUpdateDraftAccount_success: Response body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNT_RESPONSE));
    }

    @Test
    @DisplayName("Get draft account by ID - User with wrong permission [@PO-973, @PO-828]")
    void testGetDraftAccountById_trap403Response_wrongPermission() throws Exception {

        UserState userState = permissionUser(BU_ID, Permissions.COLLECTION_ORDER);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);

        mockMvc.perform(
                get(URL_BASE + "/2")
                    .header("authorization", "Bearer some_value"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value(
                "For user null, [CREATE_MANAGE_DRAFT_ACCOUNTS, CHECK_VALIDATE_DRAFT_ACCOUNTS] "
                    + "permission(s) are not enabled for the user."))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("Get draft account by ID - user with wrong permission for business unit [@PO-973, @PO-828]")
    void testGetDraftAccountById_trap403Response_wrongBusinessUnit() throws Exception {

        UserState userState = permissionUser((short)005, Permissions.DRAFT_ACCOUNT_PERMISSIONS);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);

        mockMvc.perform(
                get(URL_BASE + "/2")
                    .header("authorization", "Bearer some_value"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value(
                "For user null, [CREATE_MANAGE_DRAFT_ACCOUNTS, CHECK_VALIDATE_DRAFT_ACCOUNTS] "
                    + "permission(s) are not enabled in business unit: 77"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("Get draft account by ID - user with no permission [@PO-973, @PO-828]")
    void testGetDraftAccountsSummaries_trap403Response_noPermission() throws Exception {
        final Short businessId = (short)1;

        UserState user = permissionUser(businessId, Permissions.COLLECTION_ORDER);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        mockMvc.perform(get(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .param("business_unit", businessId.toString())
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("Get draft account by ID - Should return 404 Not Found [@PO-973, @PO-690]")
    void testGetDraftAccountById_trap404Response() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        mockMvc.perform(
                get(URL_BASE + "/99")
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

        UserState user = permissionUser(businessId, Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        mockMvc.perform(get(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .param("business_unit", businessId.toString())
                            .param("submitted_by", "Dave")
                            .param("not_submitted_by", "Tony")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail")
                           .value("Cannot include both 'submitted_by' and 'not_submitted_by' parameters."))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.type")
                           .value("https://hmcts.gov.uk/problems/illegal-argument"));
    }

    @Test
    @DisplayName("Replace draft account - user with no permission [@PO-973, @PO-830]")
    void testReplaceDraftAccount_trap403Response_noPermission() throws Exception {
        Long draftAccountId = 241L;

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionsUser());

        mockMvc.perform(put(URL_BASE + "/" + draftAccountId)
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCreateRequestBody()))
            .andExpect(status().isForbidden())
            .andReturn();

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

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionsUser());

        mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
            .andExpect(status().isForbidden())
            .andReturn();

    }

    @Test
    @DisplayName("Create draft account - Should return 400 Bad Request [@PO-973, @PO-691]")
    void testPostDraftAccount_trap400Response() throws Exception {

        String expectedErrorMessageStart =
            "JSON Schema Validation Error: Validating against JSON schema 'addDraftAccountRequest.json',"
                + " found 14 validation errors:";

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        mockMvc.perform(post(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidCreateRequestBody()))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").value(containsString(expectedErrorMessageStart)))
            .andExpect(jsonPath("$.detail").value(containsString("required property 'account_type' not found")))
            .andExpect(jsonPath("$.detail").value(containsString("required property 'submitted_by' not found")))
            .andExpect(jsonPath("$.detail").value(containsString("required property 'submitted_by_name' not found")))
            .andExpect(jsonPath("$.detail").value(containsString("required property 'timeline_data' not found")))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/json-schema-validation"));
    }

    @Test
    @DisplayName("Create draft account - user with no permission [@PO-973, @PO-827]")
    void testPostDraftAccount_trap403Response_noPermission() throws Exception {

        String validRequestBody = validCreateRequestBody();
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionsUser());

        MvcResult result = mockMvc.perform(post(URL_BASE)
                                               .header("authorization", "Bearer some_value")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .content(validRequestBody))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value(
                "For user null, [CREATE_MANAGE_DRAFT_ACCOUNTS] permission(s) are not enabled for the user."))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"))
            .andReturn();

        String body = result.getResponse().getContentAsString();

        log.info(":testPostDraftAccount_permission: Response body:\n" + ToJsonString.toPrettyJson(body));
    }

    @Test
    @DisplayName("Create draft account - user with wrong permission [@PO-973, @PO-827]")
    void testPostDraftAccount_trap403Response_wrongPermission() throws Exception {

        String validRequestBody = validCreateRequestBody();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            permissionUser((short)5, Permissions.CHECK_VALIDATE_DRAFT_ACCOUNTS, Permissions.ACCOUNT_ENQUIRY));

        MvcResult result = mockMvc.perform(post(URL_BASE)
                                               .header("authorization", "Bearer some_value")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .content(validRequestBody))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value(
                "For user null, [CREATE_MANAGE_DRAFT_ACCOUNTS] permission(s) are not enabled for the user."))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"))
            .andReturn();

        String body = result.getResponse().getContentAsString();

        log.info(":testPostDraftAccount_permission: Response body:\n" + ToJsonString.toPrettyJson(body));
    }

    //CEP 1 CEP1 - Invalid Request Payload (400)
    @ParameterizedTest
    @MethodSource("endpointsWithInvalidBodiesProvider")
    void methodsShouldReturn400_whenRequestPayloadIsInvalid(
        MockHttpServletRequestBuilder requestBuilder, String requestBody) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

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

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionsUser());

        mockMvc.perform(requestBuilder
                            .header("Authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));

    }

    private static Stream<Arguments> testCasesRequiringAuthorizationProvider() {
        return Stream.of(
            Arguments.of(post(URL_BASE), validCreateRequestBody()),
            Arguments.of(put(URL_BASE + "/1"), validCreateRequestBody()),
            Arguments.of(patch(URL_BASE + "/1"), validUpdateRequestBody("B")),
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
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        mockMvc.perform(requestBuilder
                            .header("Authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
            .andExpect(status().isNotFound());
    }

    private static Stream<Arguments> testCasesForResourceNotFoundProvider() {
        return Stream.of(
            Arguments.of(get(URL_BASE + "/999"), ""),
            Arguments.of(put(URL_BASE + "/999"), validCreateRequestBody()),
            Arguments.of(patch(URL_BASE + "/999"), validUpdateRequestBody("C"))
        );
    }

    //CEP5 - Unsupported Content Type for Response (406)
    @ParameterizedTest
    @MethodSource("testCasesWithValidBodiesProvider")
    void methodsShouldReturn406_whenAcceptHeaderIsNotSupported(
        MockHttpServletRequestBuilder requestBuilder, String requestBody) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());
        mockMvc.perform(requestBuilder
                            .header("Authorization", "Bearer some_value")
                            .header("Accept", "application/xml")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
            .andExpect(status().isNotAcceptable());
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

    private static String validUpdateRequestBody(String delta) {
        return "{\n"
            + "    \"account_status\": \"PENDING\",\n"
            + "    \"validated_by\": \"BUUID1" + delta + "\",\n"
            + "    \"validated_by_name\": \"" + delta + "\",\n"
            + "    \"business_unit_id\": 78,\n"
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

}
